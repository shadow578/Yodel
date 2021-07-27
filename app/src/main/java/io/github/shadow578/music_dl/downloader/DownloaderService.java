package io.github.shadow578.music_dl.downloader;

import android.app.Notification;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.lifecycle.LifecycleService;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;
import com.yausername.youtubedl_android.YoutubeDLResponse;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import io.github.shadow578.music_dl.R;
import io.github.shadow578.music_dl.db.TracksDB;
import io.github.shadow578.music_dl.db.model.TrackInfo;
import io.github.shadow578.music_dl.db.model.TrackStatus;
import io.github.shadow578.music_dl.downloader.wrapper.MP3agicWrapper;
import io.github.shadow578.music_dl.downloader.wrapper.YoutubeDLWrapper;
import io.github.shadow578.music_dl.util.Util;
import io.github.shadow578.music_dl.util.notifications.NotificationChannels;
import io.github.shadow578.music_dl.util.preferences.Prefs;
import io.github.shadow578.music_dl.util.storage.StorageHelper;
import io.github.shadow578.music_dl.util.storage.StorageKey;

/**
 * tracks downloading service
 */
public class DownloaderService extends LifecycleService {

    /**
     * tag for logging
     */
    private static final String TAG = "DLService";

    /**
     * retries for youtube-dl operations
     */
    private static final int YOUTUBE_DL_RETRIES = 10;

    /**
     * notification id of the progress notification
     */
    private static final int PROGRESS_NOTIFICATION_ID = 123456;

    /**
     * a list of all tracks that are scheduled to be downloaded.
     * tracks are only removed from the set after they have been downloaded, and updated in the database
     * this list is processed sequentially by {@link #downloadThread}
     */
    private final BlockingQueue<TrackInfo> scheduledDownloads = new LinkedBlockingQueue<>();

    /**
     * the main download thread. runs in {@link #downloadThread()}
     */
    private final Thread downloadThread = new Thread(this::downloadThread);

    /**
     * gson instance
     */
    private final Gson gson = new Gson();

    /**
     * notification manager, for progress notification
     */
    private NotificationManagerCompat notificationManager;

    /**
     * is the service currently in foreground?
     */
    private boolean isInForeground = false;

    @Override
    public void onCreate() {
        super.onCreate();
        // ensure downloads are accessible
        if (!checkDownloadsDirSet()) {
            Toast.makeText(this, "Downloads directory not accessible, stopping Downloader!", Toast.LENGTH_LONG).show();
            Log.i(TAG, "downloads dir not accessible, stopping service");
            stopSelf();
            return;
        }

        // create progress notification
        notificationManager = NotificationManagerCompat.from(this);

        // init db and observe changes to pending tracks
        Log.i(TAG, "start observing pending tracks...");
        TracksDB.init(this);
        TracksDB.getInstance().tracks().observePending().observe(this, pendingTracks -> {
            Log.i(TAG, String.format("pendingTracks update! size= %d", pendingTracks.size()));

            // enqueue all that are not scheduled already
            boolean trackAdded = false;
            for (TrackInfo track : pendingTracks) {
                // ignore if track not pending
                if (track == null
                        || scheduledDownloads.contains(track)
                        || track.status != TrackStatus.DownloadPending) {
                    continue;
                }

                //enqueue the track
                scheduledDownloads.add(track);
                trackAdded = true;
            }

            // notify downloader
            if (trackAdded) {
                synchronized (scheduledDownloads) {
                    scheduledDownloads.notifyAll();
                }
            }
        });

        // start downloader thread as daemon
        downloadThread.setName("io.github.shadow578.music_dl.downloader.DOWNLOAD_THREAD");
        downloadThread.setDaemon(true);
        downloadThread.start();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "destroying service...");
        downloadThread.interrupt();
        hideNotification();
        super.onDestroy();
    }

    /**
     * check if the downloads directory is set and accessible
     *
     * @return is the downloads dir set and accessible?
     */
    private boolean checkDownloadsDirSet() {
        final StorageKey downloadsKey = Prefs.DownloadsDirectory.get();
        if (!downloadsKey.equals(StorageKey.EMPTY)) {
            final Optional<DocumentFile> downloadsDir = StorageHelper.getPersistedFilePermission(this, downloadsKey, true);
            return downloadsDir.isPresent()
                    && downloadsDir.get().exists()
                    && downloadsDir.get().canWrite();
        }

        return false;
    }

    //region downloader top- level

    /**
     * the main download thread
     */
    private void downloadThread() {
        try {
            // reset in- progress downloads back to pending
            TracksDB.getInstance().tracks().resetDownloadingToPending();

            // init youtube-dl
            Log.i(TAG, "downloader thread starting...");
            if (!YoutubeDLWrapper.init(this)) {
                Log.e(TAG, "youtube-dl init failed, stopping service");
                stopSelf();
                return;
            }

            // main loop
            while (!Thread.interrupted()) {
                // process all enqueued tracks
                TrackInfo trackToDownload;
                while ((trackToDownload = scheduledDownloads.peek()) != null) {
                    downloadTrack(trackToDownload);
                    scheduledDownloads.poll();
                }

                // remove notification
                hideNotification();

                // wait for changes to the set
                synchronized (scheduledDownloads) {
                    scheduledDownloads.wait();
                }
            }
        } catch (InterruptedException ignored) {
        }
    }

    /**
     * download a track
     *
     * @param track the track to download
     */
    private void downloadTrack(@NonNull TrackInfo track) {
        // double- check the track is not downloaded
        final TrackInfo dbTrack = TracksDB.getInstance().tracks().get(track.id);
        if (dbTrack == null || dbTrack.status != TrackStatus.DownloadPending) {
            Log.i(TAG, String.format("skipping download of %s: appears to already be downloaded", track.id));
            return;
        }

        // set status to downloading
        track.status = TrackStatus.Downloading;
        TracksDB.getInstance().tracks().update(track);

        // download the track and update the entry in the DB
        final TrackDownloadFormat format = Prefs.DownloadFormat.get();
        final boolean downloadOk = download(track, format);
        track.status = downloadOk ? TrackStatus.Downloaded : TrackStatus.DownloadFailed;
        TracksDB.getInstance().tracks().update(track);
    }

    /**
     * download the track and resolve metadata
     *
     * @param track  the track to download
     * @param format the file format to download the track in
     * @return was the download successful?
     */
    private boolean download(@NonNull TrackInfo track, @NonNull TrackDownloadFormat format) {
        TempFiles files = null;
        try {
            // create session
            updateNotification(createStatusNotification(track, R.string.dl_status_starting_download));
            final YoutubeDLWrapper session = createSession(track, format);
            files = createTempFiles(track, format);

            // download the track and metadata using youtube-dl
            downloadTrack(track, session, files);

            // parse the metadata
            updateNotification(createStatusNotification(track, R.string.dl_status_process_metadata));
            parseMetadata(track, files);

            // write id3v2 metadata for mp3 files
            // if this fails, we do not fail the whole operation
            if (format.isID3Supported() && Prefs.EnableMetadataTagging.get()) {
                try {
                    writeID3Tag(track, files);
                } catch (DownloaderException e) {
                    Log.e(TAG, "failed to write id3v2 tags of " + track.id + "! (this is not fatal, the rest of the download was successful)", e);
                }
            }

            // copy audio file to downloads dir
            updateNotification(createStatusNotification(track, R.string.dl_status_finish));
            copyAudioToFinal(track, files, format);

            // copy cover to cover store
            // if this fails, we do not fail the whole operation
            try {
                copyCoverToFinal(track, files);
            } catch (DownloaderException e) {
                Log.e(TAG, "failed to copy cover of " + track.id + "! (this is not fatal, the rest of the download was successful)", e);
            }
            return true;
        } catch (DownloaderException e) {
            Log.e(TAG, "download of " + track.id + " failed!", e);
            return false;
        } finally {
            // delete temp files
            if (files != null && !files.delete()) {
                Log.w(TAG, "could not delete temp files for " + track.id);
            }
        }
    }

    //endregion

    //region downloader implementation

    /**
     * prepare a new youtube-dl session for the track
     *
     * @param track  the track to prepare the session for
     * @param format the file format to download the track in
     * @return the youtube-dl session
     * @throws DownloaderException if the cache directory could not be created (needed for the session)
     */
    @NonNull
    private YoutubeDLWrapper createSession(@NonNull TrackInfo track, @NonNull TrackDownloadFormat format) throws DownloaderException {
        final YoutubeDLWrapper session = YoutubeDLWrapper.create(resolveVideoUrl(track))
                .cacheDir(getDownloadCacheDirectory())
                .audioOnly(format.fileExtension());

        // enable ssl fix
        if (Prefs.EnableSSLFix.get()) {
            session.fixSsl();
        }

        return session;
    }

    /**
     * create the temporary files for the download
     *
     * @param track  the track to create the files for
     * @param format the file format to download in
     * @return the tempoary files
     */
    @NonNull
    private TempFiles createTempFiles(@NonNull TrackInfo track, @NonNull TrackDownloadFormat format) {
        final File tempAudio = Util.getTempFile("dl_" + track.id, "", getCacheDir());
        return new TempFiles(tempAudio, format.fileExtension());
    }

    /**
     * invoke youtube-dl to download the track + metadata + thumbnail
     *
     * @param track   the track to download
     * @param session the current youtube-dl session
     * @param files   the files to write
     * @throws DownloaderException if download fails
     */
    private void downloadTrack(@NonNull TrackInfo track, @NonNull YoutubeDLWrapper session, @NonNull TempFiles files) throws DownloaderException {
        // make sure all files to create are non- existent
        files.delete();

        // download
        final YoutubeDLResponse downloadResponse = session.output(files.getAudio())
                //.overwriteExisting()
                .writeMetadata()
                .writeThumbnail()
                .download(((progress, etaInSeconds) -> updateNotification(createProgressNotification(track, progress / 100.0, etaInSeconds))), YOUTUBE_DL_RETRIES);
        if (downloadResponse == null
                || !files.getAudio().exists()
                || !files.getMetadataJson().exists()) {
            throw new DownloaderException("youtube-dl download failed!");
        }
    }

    /**
     * parse the metadata file and update the values in the track
     *
     * @param track the track to update
     * @param files the files created by youtube-dl
     * @throws DownloaderException if parsing fails
     */
    private void parseMetadata(@NonNull TrackInfo track, @NonNull TempFiles files) throws DownloaderException {
        // check metadata file exists
        if (!files.getMetadataJson().exists()) {
            throw new DownloaderException("metadata file not found!");
        }

        // deserialize the file
        final TrackMetadata metadata;
        try (final FileReader reader = new FileReader(files.getMetadataJson())) {
            metadata = gson.fromJson(reader, TrackMetadata.class);
        } catch (IOException | JsonIOException | JsonSyntaxException e) {
            throw new DownloaderException("deserialization of the metadata file failed", e);
        }

        // set track data
        metadata.getTrackTitle().ifPresent(title -> track.title = title);
        metadata.getArtistName().ifPresent(artist -> track.artist = artist);
        metadata.getUploadDate().ifPresent(uploadDate -> track.releaseDate = uploadDate);
        if (metadata.duration != null) {
            track.duration = metadata.duration;
        }

        if (metadata.album != null && !metadata.album.trim().isEmpty()) {
            track.albumName = metadata.album;
        }
    }

    /**
     * copy the temporary audio file to the final destination
     *
     * @param track  the track to download
     * @param files  the temporary files, of which the audio file is copied to the downloads dir
     * @param format the file format that was used for the download
     * @throws DownloaderException if creating the final file or the copy operation fails
     */
    private void copyAudioToFinal(@NonNull TrackInfo track, @NonNull TempFiles files, @NonNull TrackDownloadFormat format) throws DownloaderException {
        // check audio file exists
        if (!files.getAudio().exists()) {
            throw new DownloaderException("cannot find audio file to copy");
        }

        // find root folder for saving downloaded tracks to
        // find using storage framework, and only allow persisted folders we can write to
        final Optional<DocumentFile> downloadRoot = getDownloadsDirectory();
        if (!downloadRoot.isPresent()) {
            throw new DownloaderException("failed to find downloads folder");
        }

        // create file to write the track to
        final DocumentFile finalFile = downloadRoot.get().createFile(format.mimeType(), track.title + "." + format.fileExtension());
        if (finalFile == null || !finalFile.canWrite()) {
            throw new DownloaderException("Could not create final output file!");
        }

        // copy the temp file to the final destination
        try (final InputStream in = new FileInputStream(files.getAudio());
             final OutputStream out = getContentResolver().openOutputStream(finalFile.getUri())) {
            Util.streamTransfer(in, out, 1024);
        } catch (IOException e) {
            // try to remove the final file
            if (!finalFile.delete()) {
                Log.w(TAG, "failed to delete final file on copy fail");
            }

            throw new DownloaderException(String.format(Locale.US, "error copying temp file (%s) to final destination (%s)",
                    files.getAudio().toString(), finalFile.getUri().toString()),
                    e);
        }

        // set the final file in track info
        track.audioFileKey = StorageHelper.encodeFile(finalFile);
    }

    /**
     * copy the album cover to the final destination
     *
     * @param track the track to copy the cover of
     * @param files the files downloaded by youtube-dl
     * @throws DownloaderException if copying the cover fails
     */
    private void copyCoverToFinal(@NonNull TrackInfo track, @NonNull TempFiles files) throws DownloaderException {
        // check thumbnail file exists
        final Optional<File> thumbnail = files.getThumbnail();
        if (!thumbnail.isPresent() || !thumbnail.get().exists()) {
            throw new DownloaderException("cannot find thumbnail file");
        }

        // get covers directory
        final File coverRoot = getCoverArtDirectory();

        // create file for the thumbnail
        final File coverFile = new File(coverRoot, String.format("%s_%s.webp", track.id, UUID.randomUUID()));

        // read temporary thumbnail file and write as webp in cover art directory
        try (final InputStream in = new FileInputStream(thumbnail.get());
             final OutputStream out = new FileOutputStream(coverFile)) {
            final Bitmap cover = BitmapFactory.decodeStream(in);
            cover.compress(Bitmap.CompressFormat.WEBP, 100, out);
            cover.recycle();
        } catch (IOException e) {
            throw new DownloaderException("failed to save cover as webp", e);
        }

        // set the cover file key in track
        track.coverKey = StorageHelper.encodeFile(DocumentFile.fromFile(coverFile));
    }

    /**
     * write the track metadata to the id3v2 tag of the file
     *
     * @param track the track data
     * @param files the files downloaded by youtube-dl
     * @throws DownloaderException if writing the id3 tag fails
     */
    private void writeID3Tag(@NonNull TrackInfo track, @NonNull TempFiles files) throws DownloaderException {
        try {
            // clear all previous id3 tags, and create a new & empty one
            final MP3agicWrapper mp3Wrapper = MP3agicWrapper.create(files.getAudio());
            final ID3v2 tag = mp3Wrapper
                    .clearAllTags()
                    .editTag();

            // write basic metadata (title, artist, album, ...)
            tag.setTitle(track.title);

            if (track.artist != null) {
                tag.setArtist(track.artist);
            }

            if (track.releaseDate != null) {
                tag.setYear(String.format(Locale.US, "%04d", track.releaseDate.getYear()));
            }

            if (track.albumName != null) {
                tag.setAlbum(track.albumName);
            }

            // set cover art (if thumbnail was downloaded)
            final Optional<File> thumbnail = files.getThumbnail();
            if (thumbnail.isPresent() && thumbnail.get().exists()) {
                try (final FileInputStream src = new FileInputStream(thumbnail.get());
                     final ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    // convert to png
                    final Bitmap cover = BitmapFactory.decodeStream(src);
                    cover.compress(Bitmap.CompressFormat.PNG, 100, out);
                    cover.recycle();

                    // write cover to tag
                    tag.setAlbumImage(out.toByteArray(), "image/png");
                } catch (IOException e) {
                    Log.e(TAG, "failed to convert cover image to PNG", e);
                }
            }

            // save the file with tags
            mp3Wrapper.save();
        } catch (IOException | NotSupportedException | InvalidDataException | UnsupportedTagException e) {
            throw new DownloaderException("could not write id3v2 tag to file!", e);
        }
    }

    //region util

    /**
     * get the video url youtube-dl should use for a track
     *
     * @param track the track to get the video url of
     * @return the video url
     */
    @NonNull
    private String resolveVideoUrl(@NonNull TrackInfo track) {
        // youtube-dl is happy with just the track id
        return track.id;
    }

    /**
     * get the {@link Prefs#DownloadsDirectory} of the app, using storage framework
     *
     * @return the optional download root directory
     */
    @NonNull
    public Optional<DocumentFile> getDownloadsDirectory() {
        final StorageKey key = Prefs.DownloadsDirectory.get();
        if (key.equals(StorageKey.EMPTY)) {
            return Optional.empty();
        }

        return StorageHelper.getPersistedFilePermission(this, key, true);
    }

    /**
     * get the cover art directory
     *
     * @return the directory to save cover art to
     * @throws DownloaderException if the directory could not be created
     */
    @NonNull
    public File getCoverArtDirectory() throws DownloaderException {
        final File coversDir = new File(getNoBackupFilesDir(), "cover_store");
        if (!coversDir.exists() && !coversDir.mkdirs()) {
            throw new DownloaderException("could not create cover_store directory");
        }
        return coversDir;
    }

    /**
     * get the youtube-dl cache directory
     *
     * @return the cache directory
     * @throws DownloaderException if creating the directory failed
     */
    @NonNull
    public File getDownloadCacheDirectory() throws DownloaderException {
        final File cacheDir = new File(getCacheDir(), "youtube-dl_cache");
        if (!cacheDir.exists() && !cacheDir.mkdirs()) {
            throw new DownloaderException("could not create youtube-dl_cache directory");
        }
        return cacheDir;
    }
    //endregion
    //endregion

    //region status notification

    /**
     * update the progress notification
     *
     * @param newNotification the updated notification
     */
    private void updateNotification(@NonNull Notification newNotification) {
        if (isInForeground) {
            // already in foreground, update the notification
            notificationManager.notify(PROGRESS_NOTIFICATION_ID, newNotification);
        } else {
            // create foreground notification
            isInForeground = true;
            startForeground(PROGRESS_NOTIFICATION_ID, newNotification);
        }
    }

    /**
     * cancel the progress notification and call {@link #stopForeground(boolean)}
     */
    private void hideNotification() {
        if (notificationManager == null) {
            return;
        }

        notificationManager.cancel(PROGRESS_NOTIFICATION_ID);
        stopForeground(true);
        isInForeground = false;
    }

    /**
     * create a download progress display notification (during track download)
     *
     * @param track    the track that is being downloaded
     * @param progress the current download progress, from 0.0 to 1.0
     * @param eta      the estimated download time remaining, in seconds
     * @return the progress notification
     */
    @NonNull
    private Notification createProgressNotification(@NonNull TrackInfo track, double progress, long eta) {
        return getBaseNotification()
                .setContentTitle(track.title)
                .setSubText(getString(R.string.dl_notification_subtext, Util.secondsToTimeString(eta)))
                .setProgress(100, (int) Math.floor(progress * 100), false)
                .build();
    }

    /**
     * create a download prepare display notification (before or after track download)
     *
     * @param track     the track that is being downloaded
     * @param statusRes the status string
     * @return the status notification
     */
    @NonNull
    private Notification createStatusNotification(@NonNull TrackInfo track, @StringRes int statusRes) {
        return getBaseNotification()
                .setContentTitle(track.title)
                .setSubText(getString(statusRes))
                .setProgress(1, 0, true)
                .build();
    }

    /**
     * get the base notification, shared between all status notifications
     *
     * @return the builder, with base settings applied
     */
    @NonNull
    private NotificationCompat.Builder getBaseNotification() {
        return new NotificationCompat.Builder(this, NotificationChannels.DownloadProgress.id())
                .setSmallIcon(R.drawable.ic_round_tpose_24)
                .setShowWhen(false)
                .setOnlyAlertOnce(true);
    }

    //endregion
}

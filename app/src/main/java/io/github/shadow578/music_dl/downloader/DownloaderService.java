package io.github.shadow578.music_dl.downloader;

import android.app.Notification;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.lifecycle.LifecycleService;

import com.yausername.youtubedl_android.YoutubeDLResponse;
import com.yausername.youtubedl_android.mapper.VideoInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import io.github.shadow578.music_dl.R;
import io.github.shadow578.music_dl.db.TracksDB;
import io.github.shadow578.music_dl.db.model.TrackInfo;
import io.github.shadow578.music_dl.db.model.TrackStatus;
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

            // notifiy downloader
            if (trackAdded) {
                scheduledDownloads.notifyAll();
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
        final StorageKey downloadsKey = Prefs.DOWNLOADS_DIRECTORY.get();
        if (downloadsKey != null) {
            final Optional<DocumentFile> downloadsDir = StorageHelper.getPersistedFilePermission(this, downloadsKey, true);
            return downloadsDir.isPresent()
                    && downloadsDir.get().exists()
                    && downloadsDir.get().canWrite();
        }

        return false;
    }

    //region downloading

    /**
     * the main download thread
     */
    private void downloadThread() {
        try {
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
        final boolean downloadOk = download(track, ".mp3");
        track.status = downloadOk ? TrackStatus.Downloaded : TrackStatus.DownloadFailed;
        TracksDB.getInstance().tracks().update(track);
    }

    /**
     * download a track using youtube-dl
     *
     * @param track      the track to use. if available, the track title is changed to the one extracted using youtube-dl
     * @param fileFormat the file format to use for the download. must be a audio format, as {@link YoutubeDLWrapper#audioOnly()} is hardcoded at the moment TODO make this non hard-coded
     * @return was the download successful?
     */
    private boolean download(@NonNull TrackInfo track, @NonNull String fileFormat) {
        File tempFile = null;
        try {
            // prepare youtube-dl session
            final YoutubeDLWrapper youtubeDl = YoutubeDLWrapper.create(resolveVideoUrl(track))
                    .fixSsl() // TODO make ssl fix a option in props
                    .audioOnly();

            // get title using youtube-dl, fallback to app- provided title
            updateNotification(createStatusNotification(track, "Resolving Video Info"));
            final VideoInfo videoInfo = youtubeDl.getInfo(YOUTUBE_DL_RETRIES);
            if (videoInfo != null) {
                final String extractedTitle = videoInfo.getFulltitle();
                if (extractedTitle != null && !extractedTitle.isEmpty()) {
                    Log.i(TAG, String.format("using extracted title (%s) from youtube-dl", extractedTitle));
                    track.title = extractedTitle;
                }
            }
            if (track.title.isEmpty()) {
                Log.w(TAG, "no title, fallback to 'unknown'!");
                track.title = "Unknown";
            }

            // download the track to a temporary file in /cache of this app
            updateNotification(createStatusNotification(track, "Starting Download"));
            tempFile = Util.getTempFile("youtube-dl_", "." + fileFormat, getCacheDir());
            tempFile.deleteOnExit();
            final YoutubeDLResponse downloadResponse = youtubeDl.output(tempFile)
                    //.overwriteExisting() //no longer needed as the file is created by youtube-dl now
                    .download(((progress, etaInSeconds) -> updateNotification(createProgressNotification(track, progress, etaInSeconds))), YOUTUBE_DL_RETRIES);
            if (downloadResponse == null || !tempFile.exists()) {
                // download failed
                Log.e(TAG, "youtube-dl download failed!");
                return false;
            }

            // find root folder for saving downloaded tracks to
            // find using storage framework, and only allow persisted folders we can write to
            updateNotification(createStatusNotification(track, "Finishing Download"));
            final Optional<DocumentFile> downloadRoot = getDownloadsDirectory(this);
            if (!downloadRoot.isPresent()) {
                // download folder not found
                Log.e(TAG, "failed to find downloads folder!");
                return false;
            }

            // create file to write the track to
            final DocumentFile finalFile = downloadRoot.get().createFile("audio/mp3", track.title + "." + fileFormat);
            if (finalFile == null || !finalFile.canWrite()) {
                Log.e(TAG, "Could not create final output file!");
                return false;
            }

            // copy the temp file to the final destination
            try (final InputStream in = new FileInputStream(tempFile);
                 final OutputStream out = getContentResolver().openOutputStream(finalFile.getUri())) {
                Util.streamTransfer(in, out, 1024);
            } catch (IOException e) {
                Log.e(TAG, String.format(Locale.US, "error copying temp file (%s) to final destination (%s)",
                        tempFile.toString(), finalFile.getUri().toString()),
                        e);

                // delete final destination file
                if (!finalFile.delete()) {
                    Log.w(TAG, "failed to delete final file on copy fail");
                }
                return false;
            }

            // set the final file in track info
            track.fileKey = StorageHelper.encodeFile(finalFile);

            // everything worked, call this success
            return true;
        } finally {
            // delete the temp file
            if (tempFile != null
                    && tempFile.exists()
                    && !tempFile.delete()) {
                Log.w(TAG, "failed to directly delete temp file. this does not matter too much, as .deleteOnExit() should take care of it later.");
            }
        }
    }

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
     * get the {@link Prefs#DOWNLOADS_DIRECTORY} of the app, using storage framework
     *
     * @param ctx the context to work in
     * @return the optional download root directory
     */
    @NonNull
    public static Optional<DocumentFile> getDownloadsDirectory(@NonNull Context ctx) {
        final StorageKey key = Prefs.DOWNLOADS_DIRECTORY.get();
        if (key == null) {
            return Optional.empty();
        }

        return StorageHelper.getPersistedFilePermission(ctx, key, true);
    }
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
        // convert eta into HH:MM:SS
        final String etaStr = String.format(Locale.US, "%02d:%02d:%02d",
                eta / 3600,
                (eta % 3600) / 60,
                eta % 60);

        return getBaseNotification()
                .setContentTitle(String.format("Downloading %s", track.title))
                .setSubText(String.format("Downloading - ETA %s", etaStr))
                .setProgress(100, (int) Math.floor(progress * 100), false)
                .build();
    }

    /**
     * create a download prepare display notification (before or after track download)
     *
     * @param track  the track that is being downloaded
     * @param status the status string
     * @return the status notification
     */
    @NonNull
    private Notification createStatusNotification(@NonNull TrackInfo track, @NonNull String status) {
        return getBaseNotification()
                .setContentTitle(String.format("Preparing %s", track.title))
                .setSubText(status)
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

package io.github.shadow578.music_dl.downloader;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;
import androidx.lifecycle.LifecycleService;

import com.yausername.youtubedl_android.YoutubeDLResponse;
import com.yausername.youtubedl_android.mapper.VideoInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.github.shadow578.music_dl.db.TracksDB;
import io.github.shadow578.music_dl.db.model.TrackInfo;
import io.github.shadow578.music_dl.downloader.wrapper.YoutubeDLWrapper;
import io.github.shadow578.music_dl.util.Util;
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
     * a list of all tracks that are currently being downloaded
     */
    private final Set<TrackInfo> currentDownloads = new HashSet<>();

    /**
     * executor for downloading async.
     * using a single thread for this to ensure tracks are downloaded in order
     */
    private final Executor downloadExecutor = Executors.newSingleThreadExecutor();

    @Override
    public void onCreate() {
        super.onCreate();
        TracksDB.init(this);
        Util.runAsync(() -> {
            // init youtube-dl
            Log.i(TAG, "downloader service starting...");
            if (!YoutubeDLWrapper.init(this)) {
                Log.e(TAG, "youtube-dl init failed");
                return;
            }

            Util.runOnMain(() -> {
                // init db and observe changes to pending tracks
                Log.i(TAG, "start observing pending tracks...");
                TracksDB.getInstance().tracks().observePending().observe(this, pendingTracks -> {
                    Log.i(TAG, "pendingTracks update!");

                    // skip if no tracks pending
                    if (pendingTracks.size() <= 0) {
                        Log.i(TAG, "no pending tracks, ignoring update");
                        return;
                    }

                    // process all tracks
                    for (TrackInfo track : pendingTracks) {
                        // ignore if already downloading this track
                        if (track == null
                                || currentDownloads.contains(track)
                                || track.isDownloaded) {
                            continue;
                        }

                        // download this track
                        currentDownloads.add(track);
                        downloadExecutor.execute(() -> downloadTrack(track));
                    }
                });
            });
        });
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "destroying service...");
        super.onDestroy();
    }

    /**
     * download a track
     *
     * @param track the track to download
     */
    private void downloadTrack(@NonNull TrackInfo track) {
        try {
            // insert into current downloads
            currentDownloads.add(track);

            // double- check the track is not downloaded
            final TrackInfo dbTrack = TracksDB.getInstance().tracks().get(track.id);
            if (dbTrack == null || dbTrack.isDownloaded || track.isDownloaded) {
                Log.i(TAG, String.format("skipping download of %s: appears to already be downloaded", track.id));
                return;
            }

            // download the track and update the entry in the DB
            if (download(track, ".mp3")) {
                track.isDownloaded = true;
            } else {
                track.isDownloaded = false;
                track.didDownloadFail = true;
            }
            TracksDB.getInstance().tracks().insert(track);
        } finally {
            // remove from current downloads
            currentDownloads.remove(track);
        }
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
            tempFile = Util.getTempFile("youtube-dl_", "." + fileFormat, getCacheDir());
            tempFile.deleteOnExit();
            final YoutubeDLResponse downloadResponse = youtubeDl.output(tempFile)
                    //.overwriteExisting() //no longer needed as the file is created by youtube-dl now
                    .download(null, YOUTUBE_DL_RETRIES);
            if (downloadResponse == null || !tempFile.exists()) {
                // download failed
                Log.e(TAG, "youtube-dl download failed!");
                return false;
            }

            // find root folder for saving downloaded tracks to
            // find using storage framework, and only allow persisted folders we can write to
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
}

package io.github.shadow578.music_dl.ui.ytmusic;

import android.app.Application;
import android.content.UriPermission;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.yausername.youtubedl_android.YoutubeDLResponse;
import com.yausername.youtubedl_android.mapper.VideoInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Optional;

import io.github.shadow578.music_dl.db.TracksDB;
import io.github.shadow578.music_dl.downloader.wrapper.YoutubeDLWrapper;
import io.github.shadow578.music_dl.util.Util;

/**
 * viewmodel for the {@link YoutubeMusicActivity}
 */
public class YoutubeMusicViewModel extends AndroidViewModel {

    /**
     * the current title, null if no track is playing
     */
    @Nullable
    private String currentTitle = null;

    /**
     * is auto- downloading enabled?
     */
    private final MutableLiveData<Boolean> autoDownloadEnabled = new MutableLiveData<>(false);

    /**
     * create the view model
     *
     * @param application the application reference
     */
    public YoutubeMusicViewModel(@NonNull Application application) {
        super(application);
        TracksDB.init(getApplication());
    }

    /**
     * start downloading a track
     *
     * @param id the track to download
     */
    public void downloadTrack(@NonNull String id) {
        // check if a track is playing
        if (!isTrackPlaying()) {
            return;
        }


        //TODO downloader impl
        Log.i("DL", "download track " + id);
    }

    public void dlTest() {
        Util.runAsync(() -> {
            // 'parameters' for later, currently just constants
            final String videoUrl = "CuklIb9d3fI";
            String title = currentTitle;
            final String fileFormat = "mp3";


            File tempFile = null;
            try {
                // init youtube-dl
                if (!YoutubeDLWrapper.init(getApplication())) {
                    Log.e("DLService", "youtube-dl init failed");
                    return;
                }

                // prepare youtube-dl session
                final YoutubeDLWrapper youtubeDl = YoutubeDLWrapper.create(videoUrl)
                        .fixSsl() // TODO make ssl fix a option in props
                        .audioOnly();

                // get title using youtube-dl, fallback to app- provided title
                final VideoInfo videoInfo = youtubeDl.getInfo(10);
                if (videoInfo != null) {
                    Log.i("DLService", "using title from video info");
                    title = videoInfo.getFulltitle();
                }
                if (title == null || title.isEmpty()) {
                    Log.w("DLService", "no title, fallback to 'unknown'!");
                    title = "Unknown";
                }

                // download the track to a temporary file in /cache of this app
                tempFile = Util.getTempFile("youtube-dl_", "." + fileFormat, getApplication().getCacheDir());
                tempFile.deleteOnExit();
                final YoutubeDLResponse downloadResponse = youtubeDl.output(tempFile)
                        //.overwriteExisting() //no longer needed as the file is created by youtube-dl now
                        .download(null, 10);
                if (downloadResponse == null || !tempFile.exists()) {
                    // download failed
                    Log.e("DLService", "youtube-dl download failed!");
                    return;
                }

                // find root folder for saving downloaded tracks to
                // find using storage framework, and only allow persisted folders we can write to
                final Optional<DocumentFile> downloadRoot = getApplication().getContentResolver().getPersistedUriPermissions()
                        .stream()
                        .filter(UriPermission::isWritePermission)
                        .map(uri -> DocumentFile.fromTreeUri(getApplication(), uri.getUri()))
                        .filter(doc -> doc != null && doc.exists() && doc.canRead() && doc.canWrite() && doc.isDirectory())
                        .findFirst();
                if (!downloadRoot.isPresent()) {
                    // download folder not found
                    return;
                }

                // create file to write the track to
                final DocumentFile finalFile = downloadRoot.get().createFile("audio/mp3", title + "." + fileFormat);
                if (finalFile == null || !finalFile.canWrite()) {
                    Log.e("DLService", "Could not create final output file!");
                    return;
                }

                // copy the temp file to the final destination
                try (final InputStream in = new FileInputStream(tempFile);
                     final OutputStream out = getApplication().getContentResolver().openOutputStream(finalFile.getUri())) {
                    Util.streamTransfer(in, out, 1024);
                } catch (IOException e) {
                    Log.e("DLService", String.format(Locale.US, "error copying temp file (%s) to final destination (%s)",
                            tempFile.toString(), finalFile.getUri().toString()),
                            e);

                    // delete final destination file
                    if (!finalFile.delete()) {
                        Log.w("DLService", "failed to delete final file on copy fail");
                    }
                }
            } finally {
                // delete the temp file
                if (tempFile != null && !tempFile.delete()) {
                    Log.w("DLService", "failed to directly delete temp file. this does not matter too much, as .deleteOnExit() should take care of it later.");
                }
            }




            /* OLD code

            // build url from ID
            final String watchUrl = "CuklIb9d3fI";//Util.getYoutubeWatchLink(id);

            // prepa

            // get track title using youtube-dl
            String title = currentTitle;
            final VideoInfo
/
                final VideoInfo info = YoutubeDL.getInstance().getInfo(watchUrl);
                if (info != null
                        && info.getTitle() != null
                        && !info.getTitle().isEmpty()) {
                    title = info.getTitle();
                }
                /


            // get where to save the video (final)
            final DocumentFile root = DocumentFile.fromTreeUri(getApplication(), getApplication().getContentResolver().getPersistedUriPermissions().get(0).getUri());
            final DocumentFile trackFile = root.createFile("audio/mp3", title);

            // get a path youtube-dl can download to (in cache)
            final File tempDownloadFile = File.createTempFile("youtube-dl", ".mp3", getApplication().getCacheDir());

            // insert into DB with pending download status
            final TrackInfo track = new TrackInfo("id",
                    title,
                    trackFile.getUri().toString(),
                    false);


            // download video using youtube-dl
            final YoutubeDLRequest request = new YoutubeDLRequest(watchUrl)
            //.addOption("--verbose")
            // .addOption("--no-continue")//force- overwrite existing files
            //.addOption("-f", "bestaudio")
            //.addOption("--no-check-certificate")
            //.addOption("--prefer-insecure")
            //.addOption("-o", tempDownloadFile.getAbsolutePath());
            final YoutubeDLResponse response = YoutubeDL.getInstance().execute(request);
            Log.i("YTDL", response.getOut());
            Log.e("YTDL", response.getErr());
            */
        });
    }

    /**
     * set the auto- download mode enable
     *
     * @param enabled enable auto- download mode?
     */
    public void setAutoDownloadEnabled(boolean enabled) {
        autoDownloadEnabled.postValue(enabled);
    }

    /**
     * @return auto- download mode enable
     */
    @NonNull
    public LiveData<Boolean> getAutoDownloadEnabled() {
        return autoDownloadEnabled;
    }

    /**
     * set the current track title
     *
     * @param title the title of the current track. null or empty if no track is playing
     */
    public void setCurrentTitle(@Nullable String title) {
        currentTitle = title;
    }

    /**
     * @return is there currently a track playing?
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isTrackPlaying() {
        return currentTitle != null && !currentTitle.isEmpty();
    }
}

package io.github.shadow578.music_dl.downloader.wrapper;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.yausername.ffmpeg.FFmpeg;
import com.yausername.youtubedl_android.DownloadProgressCallback;
import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLException;
import com.yausername.youtubedl_android.YoutubeDLRequest;
import com.yausername.youtubedl_android.YoutubeDLResponse;

import java.io.File;

import io.github.shadow578.music_dl.BuildConfig;

/**
 * wrapper for {@link com.yausername.youtubedl_android.YoutubeDL}.
 * all functions in this class should be run in a background thread only
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public class YoutubeDLWrapper {

    /**
     * tag for logging
     */
    private static final String TAG = "Youtube-DL";

    /**
     * did the YoutubeDl library initialize once? in {@link #init(Context)}
     */
    private static boolean initialized = false;

    /**
     * create a new youtube-dl session
     *
     * @param videoUrl the url (or id) of the video to download. this is passed to youtube-dl directly
     * @return the youtube-dl session wrapper
     */
    @NonNull
    public static YoutubeDLWrapper create(@NonNull String videoUrl) {
        return new YoutubeDLWrapper(videoUrl);
    }

    /**
     * initialize the youtube-dl library
     *
     * @param ctx the context to work in
     * @return did initialization succeed
     */
    public static boolean init(@NonNull Context ctx) {
        // only once
        if (initialized) {
            return true;
        }
        initialized = true;

        try {
            // initialize and update youtube-dl
            YoutubeDL.getInstance().init(ctx);
            YoutubeDL.getInstance().updateYoutubeDL(ctx);

            // initialize FFMPEG library
            FFmpeg.getInstance().init(ctx);
            return true;
        } catch (YoutubeDLException e) {
            Log.e(TAG, "youtube-dl init failed", e);
            initialized = false;
            return false;
        }
    }

    /**
     * the video url to download
     */
    @NonNull
    private final String videoUrl;

    /**
     * the download request
     */
    @NonNull
    private final YoutubeDLRequest request;

    /**
     * should the command output be printed to log?
     */
    private boolean printOutput = false;

    /**
     * create a new wrapper instance
     *
     * @param videoUrl the url or id of the video to download
     */
    protected YoutubeDLWrapper(@NonNull String videoUrl) {
        this.videoUrl = videoUrl;
        this.request = new YoutubeDLRequest(videoUrl);

        // enable verbose output on debug builds
        if (BuildConfig.DEBUG) {
            request.addOption("--verbose");
        }
        printOutput(BuildConfig.DEBUG);
    }

    //region parameter wrapper

    /**
     * make youtube-dl overwrite existing files, using the '--no-continue' option.
     * only for use with {@link #download(DownloadProgressCallback)} functions
     *
     * @return self instance
     */
    public YoutubeDLWrapper overwriteExisting() {
        request.addOption("--no-continue");
        return this;
    }

    /**
     * (try) to fix ssl certificate validation errors, using the '--no-check-certificate' and '--prefer-insecure' options.
     *
     * @return self instance
     */
    public YoutubeDLWrapper fixSsl() {
        request.addOption("--no-check-certificate")
                .addOption("--prefer-insecure");
        return this;
    }

    /**
     * download audio and video in the best quality, using '-f best'.
     * only for use with {@link #download(DownloadProgressCallback)} functions
     *
     * @return self instance
     */
    public YoutubeDLWrapper audioAndVideo() {
        request.addOption("-f", "best");
        return this;
    }

    /**
     * download best quality video only, using '-f bestvideo'.
     * only for use with {@link #download(DownloadProgressCallback)} functions
     *
     * @return self instance
     */
    public YoutubeDLWrapper videoOnly() {
        request.addOption("-f", "bestvideo");
        return this;
    }

    /**
     * download best quality audio only, using '-f bestaudio'.
     * only for use with {@link #download(DownloadProgressCallback)} functions
     *
     * @return self instance
     */
    public YoutubeDLWrapper audioOnly() {
        request.addOption("-f", "bestaudio");
        return this;
    }

    /**
     * write the metadata to disk as a json file. path is {@link #output(File)} + .info.json
     *
     * @return self instance
     */
    public YoutubeDLWrapper writeMetadata() {
        request.addOption("--write-info-json");
        return this;
    }

    /**
     * write the main thumbnail to disk as webp file. path is {@link #output(File)} + .webp
     *
     * @return self instance
     */
    public YoutubeDLWrapper writeThumbnail() {
        request.addOption("--write-thumbnail");
        return this;
    }

    /**
     * set the file to download to, using '-o OUTPUT'.
     * only for use with {@link #download(DownloadProgressCallback)} functions
     *
     * @param output the file to output to. unless called with {@link #overwriteExisting()}, this file must not exist
     * @return self instance
     */
    public YoutubeDLWrapper output(@NonNull File output) {
        request.addOption("-o", output.getAbsolutePath());
        return this;
    }

    /**
     * set the youtube-dl cache directory, using '-cache-dir CACHE'.
     *
     * @param cache the cache directory
     * @return self instance
     */
    public YoutubeDLWrapper cacheDir(@NonNull File cache) {
        request.addOption("--cache-dir", cache.getAbsolutePath());
        return this;
    }

    /**
     * set a option.
     * only for use with {@link #download(DownloadProgressCallback)} functions
     *
     * @param key   the parameter name (eg. '-f')
     * @param value the parameter value (eg. 'best'). this may be null for options without value (like '--continue')
     * @return self instance
     */
    public YoutubeDLWrapper setOption(@NonNull String key, @Nullable String value) {
        if (value == null) {
            request.addOption(key);
        } else {
            request.addOption(key, value);
        }
        return this;
    }

    /**
     * get the raw {@link YoutubeDLRequest} object, to directly manipulate options.
     * only use this if you know what you're doing.
     * only for use with {@link #download(DownloadProgressCallback)} functions
     *
     * @return the youtube-dl request object
     */
    @NonNull
    public YoutubeDLRequest getRequest() {
        return request;
    }

    /**
     * enable printing of the youtube-dl command output.
     * by default on on DEBUG builds, and off on RELEASE builds.
     * only for use with {@link #download(DownloadProgressCallback)} functions
     *
     * @return self instance
     */
    @NonNull
    public YoutubeDLWrapper printOutput(boolean print) {
        printOutput = print;
        return this;
    }
    //endregion

    //region download

    /**
     * download the video using youtube-dl, with retires
     *
     * @param progressCallback callback to report back download progress
     * @param tries            the number of retries for downloading
     * @return the response, or null if the download failed
     */
    public YoutubeDLResponse download(@Nullable DownloadProgressCallback progressCallback, int tries) {
        if (!initialized) {
            throw new IllegalStateException("youtube-dl was not initialized! call YoutubeDLWrapper.init() first!");
        }

        YoutubeDLResponse response;
        do {
            response = download(progressCallback);
            if (response != null) {
                break;
            }
        } while (--tries > 0);

        return response;
    }

    /**
     * download the video using youtube-dl, without retires
     *
     * @param progressCallback callback to report back download progress
     * @return the response, or null if the download failed
     */
    public YoutubeDLResponse download(@Nullable DownloadProgressCallback progressCallback) {
        if (!initialized) {
            throw new IllegalStateException("youtube-dl was not initialized! call YoutubeDLWrapper.init() first!");
        }

        try {
            Log.i(TAG, "downloading " + videoUrl);
            final YoutubeDLResponse response = YoutubeDL.getInstance().execute(request, progressCallback);
            if (printOutput) {
                print(response);
            }
            return response;
        } catch (YoutubeDLException | InterruptedException e) {
            Log.e(TAG, "download of '" + videoUrl + "' using youtube-dl failed", e);
            return null;
        }
    }

    /**
     * print response details to log
     *
     * @param response the response to print
     */
    private void print(@NonNull YoutubeDLResponse response) {
        Log.i(TAG, "-------------");
        Log.i(TAG, " url: " + videoUrl);
        Log.i(TAG, " command: " + response.getCommand());
        Log.i(TAG, " exit code: " + response.getExitCode());
        Log.i(TAG, " stdout: \n" + response.getOut());
        Log.i(TAG, " stderr: \n" + response.getErr());
    }
    //endregion
}

package io.github.shadow578.yodel.downloader.wrapper

import android.content.Context
import android.util.Log
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.*
import io.github.shadow578.music_dl.BuildConfig
import java.io.File

/**
 * wrapper for [com.yausername.youtubedl_android.YoutubeDL].
 * all functions in this class should be run in a background thread only
 *
 * @param videoUrl the video url to download
 */
class YoutubeDLWrapper(
    private val videoUrl: String
) {
    companion object {
        /**
         * tag for logging
         */
        private const val TAG = "Youtube-DL"

        /**
         * did the YoutubeDl library initialize once? in [.init]
         */
        private var initialized = false

        /**
         * initialize the youtube-dl library
         *
         * @param ctx the context to work in
         * @return did initialization succeed
         */
        fun init(ctx: Context): Boolean {
            // only once
            if (initialized) return true
            initialized = true

            return try {
                // initialize and update youtube-dl
                YoutubeDL.getInstance().init(ctx)
                YoutubeDL.getInstance().updateYoutubeDL(ctx)

                // initialize FFMPEG library
                FFmpeg.getInstance().init(ctx)
                true
            } catch (e: YoutubeDLException) {
                Log.e(TAG, "youtube-dl init failed", e)
                initialized = false
                false
            }
        }
    }

    /**
     * the download request
     */
    val request: YoutubeDLRequest = YoutubeDLRequest(videoUrl)

    /**
     * should the command output be printed to log?
     */
    private var printOutput = false

    init {
        // enable verbose output on debug builds
        if (BuildConfig.DEBUG) {
            request.addOption("--verbose")
        }
        printOutput(BuildConfig.DEBUG)
    }

    //region parameter wrapper
    /**
     * make youtube-dl overwrite existing files, using the '--no-continue' option.
     * only for use with [.download] functions
     *
     * @return self instance
     */
    fun overwriteExisting(): YoutubeDLWrapper {
        request.addOption("--no-continue")
        return this
    }

    /**
     * (try) to fix ssl certificate validation errors, using the '--no-check-certificate' and '--prefer-insecure' options.
     *
     * @return self instance
     */
    fun fixSsl(): YoutubeDLWrapper {
        request.addOption("--no-check-certificate")
            .addOption("--prefer-insecure")
        return this
    }

    /**
     * download audio and video in the best quality, using '-f best'.
     * only for use with [.download] functions
     *
     * @return self instance
     */
    fun audioAndVideo(): YoutubeDLWrapper {
        request.addOption("-f", "best")
        return this
    }

    /**
     * download best quality video only, using '-f bestvideo'.
     * only for use with [.download] functions
     *
     * @return self instance
     */
    fun videoOnly(): YoutubeDLWrapper {
        request.addOption("-f", "bestvideo")
        return this
    }

    /**
     * download best quality audio only, using '-f bestaudio' with '--extract-audio'. '--audio-quality 0' and '--audio-format FORMAT'.
     * only for use with [.download] functions
     *
     * @param format the format of the audio to download, like 'mp3'
     * @return self instance
     */
    fun audioOnly(format: String): YoutubeDLWrapper {
        request.addOption("-f", "bestaudio").addOption("--extract-audio")
            .addOption("--audio-format", format)
            .addOption("--audio-quality", 0)
        return this
    }

    /**
     * write the metadata to disk as a json file. path is [.output] + .info.json
     *
     * @return self instance
     */
    fun writeMetadata(): YoutubeDLWrapper {
        request.addOption("--write-info-json")
        return this
    }

    /**
     * write the main thumbnail to disk as webp file. path is [.output] + .webp
     *
     * @return self instance
     */
    fun writeThumbnail(): YoutubeDLWrapper {
        request.addOption("--write-thumbnail")
        return this
    }

    /**
     * set the file to download to, using '-o OUTPUT'.
     * only for use with [.download] functions
     *
     * @param output the file to output to. unless called with [.overwriteExisting], this file must not exist
     * @return self instance
     */
    fun output(output: File): YoutubeDLWrapper {
        request.addOption("-o", output.absolutePath)
        return this
    }

    /**
     * set the youtube-dl cache directory, using '-cache-dir CACHE'.
     *
     * @param cache the cache directory
     * @return self instance
     */
    fun cacheDir(cache: File): YoutubeDLWrapper {
        request.addOption("--cache-dir", cache.absolutePath)
        return this
    }

    /**
     * set a option.
     * only for use with [.download] functions
     *
     * @param key   the parameter name (eg. '-f')
     * @param value the parameter value (eg. 'best'). this may be null for options without value (like '--continue')
     * @return self instance
     */
    fun setOption(key: String, value: String?): YoutubeDLWrapper {
        if (value == null) {
            request.addOption(key)
        } else {
            request.addOption(key, value)
        }
        return this
    }

    /**
     * enable printing of the youtube-dl command output.
     * by default on on DEBUG builds, and off on RELEASE builds.
     * only for use with [.download] functions
     *
     * @return self instance
     */
    fun printOutput(print: Boolean): YoutubeDLWrapper {
        printOutput = print
        return this
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
    fun download(progressCallback: DownloadProgressCallback?, tries: Int = 1): YoutubeDLResponse? {
        var retry = tries
        check(initialized) { "youtube-dl was not initialized! call YoutubeDLWrapper.init() first!" }
        var response: YoutubeDLResponse?
        do {
            response = download(progressCallback)
            if (response != null) {
                break
            }
        } while (--retry > 0)
        return response
    }

    /**
     * download the video using youtube-dl, without retires
     *
     * @param progressCallback callback to report back download progress
     * @return the response, or null if the download failed
     */
    fun download(progressCallback: DownloadProgressCallback?): YoutubeDLResponse? {
        check(initialized) { "youtube-dl was not initialized! call YoutubeDLWrapper.init() first!" }
        return try {
            Log.i(TAG, "downloading $videoUrl")
            val response = YoutubeDL.getInstance().execute(request, progressCallback)
            if (printOutput) {
                print(response)
            }
            response
        } catch (e: YoutubeDLException) {
            Log.e(TAG, "download of '$videoUrl' using youtube-dl failed", e)
            null
        } catch (e: InterruptedException) {
            Log.e(TAG, "download of '$videoUrl' using youtube-dl failed", e)
            null
        }
    }

    /**
     * print response details to log
     *
     * @param response the response to print
     */
    private fun print(response: YoutubeDLResponse) {
        Log.i(TAG, "-------------")
        Log.i(TAG, " url: $videoUrl")
        Log.i(TAG, " command: ${response.command}")
        Log.i(TAG, " exit code: ${response.exitCode}")
        Log.i(TAG, " stdout: ${response.out}")
        Log.i(TAG, " stderr: ${response.err}")
    }
//endregion
}
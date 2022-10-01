package io.github.shadow578.yodel.util.preferences

import io.github.shadow578.yodel.downloader.wrapper.YoutubeDLWrapper

/**
 * contains 'Developer Flags' that enable or disable certain behaviour of the app.
 * this could be to control a feature of the app, or to apply a fix to some component that should have the possibility to be disabled.
 * Currently, changing the flags is only possible in devtools. Maybe, in the future, this could also be done from a online config file.
 *
 * @param key the key to use for the preference
 * @param default the default value of the flag
 * @param displayName a name used for displaying the flag in devtools. This should start by stating what component the flag affects
 */
enum class Flags(val key: String, val default: Boolean, val displayName: String) {

    /**
     * only use the video id instead of the full video url when creating a [YoutubeDLWrapper] session
     */
    OnlyUseVideoID("video_id_only", false, "Downloader: only use video id for downloads"),

    /**
     * make youtube-dl ignore ssl certificate errors using [YoutubeDLWrapper.fixSsl].
     * if [OnlyUseVideoID] is not set, use http instead of https for download url
     */
    NonSSLDownloads("skip_ssl", false, "Downloader: don't use https and skip certificate verification"),

    /**
     * enable verbose output on youtube-dl using [YoutubeDLWrapper.verboseOutput]
     */
    DownloaderVerboseOutput("dl_verbose_output", false, "Downloader: enable verbose output"),

    /**
     * enable sending error notifications from the downloader in case a download fails
     */
    NotificationsOnDownloadError("notify_on_dl_error", false, "Downloader: enable error notifications")

    ;

    /**
     * create the appropriate preference wrapper for this flag
     */
    val preference: PreferenceWrapper<Boolean>
        get() = PreferenceWrapper.create(
                Boolean::class.java,
                key,
                default
        )

    /**
     * get the current value of this flag
     */
    val value: Boolean
        get() = preference.get()
}
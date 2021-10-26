package io.github.shadow578.yodel.util.preferences

import io.github.shadow578.yodel.LocaleOverride
import io.github.shadow578.yodel.downloader.*
import io.github.shadow578.yodel.downloader.wrapper.YoutubeDLWrapper
import io.github.shadow578.yodel.util.storage.StorageKey

/**
 * app preferences storage
 */
object Prefs {
    /**
     * the main downloads directory file key
     */
    val DownloadsDirectory = PreferenceWrapper.create(
        StorageKey::class.java,
        "downloads_dir",
        StorageKey.EMPTY
    )

    /**
     * download format [DownloaderService] should use for future downloads. existing downloads are not affected
     */
    val DownloadFormat = PreferenceWrapper.create(
        TrackDownloadFormat::class.java,
        "track_download_format",
        TrackDownloadFormat.MP3
    )

    /**
     * enable writing ID3 metadata on downloaded tracks (if format supports it)
     */
    val EnableMetadataTagging = PreferenceWrapper.create(
        Boolean::class.java,
        "enable_meta_tagging",
        true
    )

    /**
     * override for the app locale
     */
    val AppLocaleOverride = PreferenceWrapper.create(
        LocaleOverride::class.java,
        "locale_override",
        LocaleOverride.SystemDefault
    )

    //region devtools flags
    /**
     * enable sending notifications when the download service encounters a error when downloading a track
     */
    val EnableDownloaderErrorNotifications = PreferenceWrapper.create(
        Boolean::class.java,
        "downloader_error_notifications",
        false
    )

    /**
     * enable verbose output on youtube-dl
     */
    val EnableDownloaderVerboseOutput = PreferenceWrapper.create(
        Boolean::class.java,
        "downloader_verbose_output",
        false
    )

    /**
     * enable [YoutubeDLWrapper.fixSsl] on track downloads
     */
    val EnableSSLFix = PreferenceWrapper.create(
            Boolean::class.java,
            "enable_ssl_fix",
            false
    )

    /**
     * only use the video id instead of the full video url when creating a [YoutubeDLWrapper] session
     */
    val UseVideoIdOnly = PreferenceWrapper.create(
            Boolean::class.java,
            "video_id_only",
            false
    )
    //endregion
}
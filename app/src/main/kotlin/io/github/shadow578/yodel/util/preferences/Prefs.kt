package io.github.shadow578.yodel.util.preferences

import io.github.shadow578.yodel.downloader.TrackDownloadFormat
import io.github.shadow578.yodel.LocaleOverride
import io.github.shadow578.yodel.util.storage.StorageKey
import io.github.shadow578.yodel.downloader.wrapper.YoutubeDLWrapper
import io.github.shadow578.yodel.downloader.DownloaderService

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
     * enable [YoutubeDLWrapper.fixSsl] on track downloads
     */
    val EnableSSLFix = PreferenceWrapper.create(
        Boolean::class.java,
        "enable_ssl_fix",
        false
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
}
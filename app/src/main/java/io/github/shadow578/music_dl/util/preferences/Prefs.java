package io.github.shadow578.music_dl.util.preferences;

import io.github.shadow578.music_dl.downloader.TrackDownloadFormat;
import io.github.shadow578.music_dl.downloader.wrapper.YoutubeDLWrapper;
import io.github.shadow578.music_dl.util.storage.StorageKey;

/**
 * app preferences storage
 */
public final class Prefs {

    /**
     * the main downloads directory file key
     */
    public static final PreferenceWrapper<StorageKey> DownloadsDirectory = PreferenceWrapper.create(StorageKey.class, "downloads_dir", StorageKey.EMPTY);

    /**
     * enable {@link YoutubeDLWrapper#fixSsl()} on track downloads
     */
    public static final PreferenceWrapper<Boolean> EnableSSLFix = PreferenceWrapper.create(Boolean.class, "enable_ssl_fix", false);

    /**
     * download format {@link YoutubeDLWrapper} should use for future downloads. existing downloads are not affected
     */
    public static final PreferenceWrapper<TrackDownloadFormat> DownloadFormat = PreferenceWrapper.create(TrackDownloadFormat.class, "track_download_format", TrackDownloadFormat.MP3);
}

package io.github.shadow578.music_dl.downloader;

import androidx.annotation.NonNull;

import java.io.IOException;

import io.github.shadow578.music_dl.KtPorted;

/**
 * exception used by {@link DownloaderService}
 */
@KtPorted
public class DownloaderException extends IOException {

    public DownloaderException(@NonNull String message) {
        super(message);
    }

    public DownloaderException(@NonNull String message, @NonNull Throwable cause) {
        super(message, cause);
    }
}

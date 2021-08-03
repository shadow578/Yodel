package io.github.shadow578.yodel.downloader

import io.github.shadow578.music_dl.downloader.DownloaderService
import java.io.IOException

/**
 * exception used by [DownloaderService]
 */
class DownloaderException : IOException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}
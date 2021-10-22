package io.github.shadow578.yodel.downloader

import java.io.IOException

/**
 * exception used by [DownloaderService]
 */
class DownloaderException : IOException {
    /**
     * if this exception was thrown while downloading using youtube-dl, this value contains the
     * command line output of youtube-dl.
     * Otherwise, this is null
     */
    val downloaderOutput: String?

    constructor(message: String, dlOutput: String? = null) : super(message) {
        downloaderOutput = dlOutput
    }

    constructor(message: String, cause: Throwable, dlOutput: String? = null) : super(
        message,
        cause
    ) {
        downloaderOutput = dlOutput
    }
}
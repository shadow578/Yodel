package io.github.shadow578.yodel.downloader

import androidx.annotation.StringRes
import io.github.shadow578.yodel.R

/**
 * file formats for track download
 *
 *
 * TODO validate all formats actually work
 * TODO check if more formats support ID3
 */
enum class TrackDownloadFormat(
    val mimetype: String,
    val fileExtension: String,
    val supportsID3Tags: Boolean,
    @StringRes val displayName: Int
) {


    /**
     * mp3 (with metadata in id3 tags)
     */
    MP3("audio/mp3", "mp3", true, R.string.file_format_mp3),

    /**
     * aac
     */
    AAC("audio/aac", "aac", false, R.string.file_format_aac),

    /**
     * webm audio
     */
    WEBM("audio/weba", "weba", false, R.string.file_format_webm),

    /**
     * ogg
     */
    OGG("audio/ogg", "ogg", false, R.string.file_format_ogg),

    /**
     * flac
     */
    FLAC("audio/flac", "flac", false, R.string.file_format_flac),

    /**
     * wav
     */
    WAV("audio/wav", "wav", false, R.string.file_format_wav)
}
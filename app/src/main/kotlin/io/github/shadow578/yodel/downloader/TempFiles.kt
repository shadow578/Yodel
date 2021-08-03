package io.github.shadow578.yodel.downloader

import java.io.File

/**
 * temporary files created by youtube-dl
 *
 * @param tempFile the base file. this file is not used directly
 * @param format   the format of the output file
 */
class TempFiles(tempFile: File, format: String) {
    companion object {
        /**
         * suffix for the metadata file
         */
        private const val METADATA_FILE_SUFFIX = ".info.json"

        /**
         * suffixes (file types) for the thumbnail file
         */
        private val THUMBNAIL_FILE_SUFFIXES = arrayOf(".webp", ".webm", ".jpg", ".jpeg", ".png")
    }

    /**
     * the main audio file downloaded by youtube-dl.
     * this file will be the same as [.convertedAudio], but with a .tmp extension
     */
    private val downloadedAudio: File = File(tempFile.absolutePath + ".tmp")

    /**
     * the converted audio file, created by ffmpeg with the --extract-audio option
     */
    private val convertedAudio: File = File(tempFile.absolutePath + "." + format)

    /**
     * delete all files
     *
     * @return did all deletes succeed?
     */
    fun delete(): Boolean {
        return (maybeDelete(downloadedAudio)
                and maybeDelete(convertedAudio)
                and maybeDelete(metadataJson)
                and (thumbnail?.delete() == true))
    }

    /**
     * delete the file if it still exists
     *
     * @param file the file to delete
     * @return does the file no longer exist?
     */
    private fun maybeDelete(file: File): Boolean {
        return !file.exists() || file.delete()
    }

    /**
     * get the audio file.
     * first tries to get [convertedAudio], if that does not exist gets [downloadedAudio]
     */
    val audio: File
        get() = if (convertedAudio.exists()) convertedAudio else downloadedAudio

    /**
     * the metadata json downloaded by youtube-dl
     */
    val metadataJson: File
        get() = File(downloadedAudio.absolutePath + METADATA_FILE_SUFFIX)

    /**
     * @return the thumbnail downloaded by youtube-dl, webp format
     */
    val thumbnail: File?
        get() {
            // check all suffixes, use the first that exists
            // youtube-dl downloads the thumbnail for us, but does not tell us the file name / type (with no way to tell it what to use :|)
            for (suffix in THUMBNAIL_FILE_SUFFIXES) {
                val thumbnailFile = File(downloadedAudio.absolutePath + suffix)
                if (thumbnailFile.exists()) {
                    return thumbnailFile
                }
            }
            return null
        }
}
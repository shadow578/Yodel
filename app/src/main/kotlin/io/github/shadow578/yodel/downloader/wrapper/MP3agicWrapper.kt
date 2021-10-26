package io.github.shadow578.yodel.downloader.wrapper

import com.mpatric.mp3agic.*
import timber.log.Timber
import java.io.*

/**
 * wrapper for MP3agic to make working with it on android easier
 *
 * @param file the mp3 file to tag
 */
class MP3agicWrapper(
    private val file: File
) {
    /**
     * the mp3agic file instance
     */
    private val mp3: Mp3File = Mp3File(file)

    /**
     * remove all tags that mp3agic supports
     *
     * @return self instance
     */
    fun clearAllTags(): MP3agicWrapper {
        if (mp3.hasId3v1Tag())
            mp3.removeId3v1Tag()
        if (mp3.hasId3v2Tag())
            mp3.removeId3v2Tag()
        if (mp3.hasCustomTag())
            mp3.removeCustomTag()
        return this
    }

    /**
     * edit the id3v2 tags on the mp3 file.
     * gets a existing id3v2 tag, or creates a new one if needed
     */
    val tag: ID3v2
        get() = if (mp3.hasId3v2Tag()) mp3.id3v2Tag else {
            val tag = ID3v24Tag()
            mp3.id3v2Tag = tag
            tag
        }

    /**
     * save the mp3 file, overwriting the original file
     *
     * @throws IOException           if io operation fails
     * @throws NotSupportedException if mp3agic fails to save the file (see [Mp3File.save])
     */
    @Throws(IOException::class, NotSupportedException::class)
    fun save() {
        var tagged: File? = null
        try {
            // create file to write to (original appended with .tagged)
            tagged = File(file.absolutePath + ".tagged")

            // save mp3 to tagged file
            mp3.save(tagged.absolutePath)

            // delete original file
            if (!file.delete())
                Timber.i("could not delete original file on save!")

            // move tagged file to its place
            FileInputStream(tagged.absolutePath).use { src ->
                FileOutputStream(file.absolutePath, false).use { out ->
                    src.copyTo(out)
                }
            }
        } finally {
            if (tagged != null && tagged.exists() && !tagged.delete())
                Timber.i("failed to delete temporary tagged mp3 file")
        }
    }
}
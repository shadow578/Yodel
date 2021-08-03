package io.github.shadow578.yodel.db.model

/**
 * status of a track
 *
 * @param key the key to set
 */
enum class TrackStatus(
    val key: String
) {
    /**
     * the track is not yet downloaded.
     * The next pass of the download service will download this track
     */
    DownloadPending("pending"),

    /**
     * the track is currently being downloaded
     */
    Downloading("downloading"),

    /**
     * the track was downloaded. it will not re- download again
     */
    Downloaded("downloaded"),

    /**
     * the download of the track failed.  it will not re- download again
     */
    DownloadFailed("failed"),

    /**
     * the track was deleted on the file system. it will not re- download again
     * the database record remains, but with the fileKey cleared (as its invalid)
     */
    FileDeleted("deleted");

    companion object {
        /**
         * find a status by its key
         *
         * @param key the key to find
         * @return the status. if not found, returns null
         */
        fun findByKey(key: String): TrackStatus? {
            for (status in values())
                if (status.key == key)
                    return status

            return null
        }
    }

}
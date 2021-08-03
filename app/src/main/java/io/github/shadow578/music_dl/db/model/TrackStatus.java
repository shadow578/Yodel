package io.github.shadow578.music_dl.db.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.shadow578.music_dl.KtPorted;

/**
 * status of a track
 */
@KtPorted
public enum TrackStatus {
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

    /**
     * the key, for SQL entry
     */
    private final String key;

    /**
     * create a track status with a key
     *
     * @param key the key to set
     */
    TrackStatus(@NonNull String key) {
        this.key = key;
    }

    /**
     * @return the SQL key
     */
    public String key() {
        return key;
    }

    /**
     * find a status by its key
     *
     * @param key the key to find
     * @return the status. if not found, returns null
     */
    @Nullable
    public static TrackStatus findByKey(@NonNull String key) {
        for (TrackStatus status : values()) {
            if (status.key().equals(key)) {
                return status;
            }
        }

        return null;
    }
}

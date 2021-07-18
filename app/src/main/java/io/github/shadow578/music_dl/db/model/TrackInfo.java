package io.github.shadow578.music_dl.db.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * information about a track
 */
@Entity(tableName = "tracks")
public class TrackInfo {
    /**
     * the youtube id of the track
     */
    @NonNull
    @PrimaryKey
    public String id = "";

    /**
     * the title of the track
     */
    public String title;

    /**
     * the uri of the file this track was downloaded to
     */
    public String fileUri;

    /**
     * is this track fully downloaded?
     */
    public boolean isDownloaded;
}

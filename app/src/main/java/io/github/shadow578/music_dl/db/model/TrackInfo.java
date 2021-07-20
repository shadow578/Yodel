package io.github.shadow578.music_dl.db.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

import io.github.shadow578.music_dl.util.storage.StorageKey;

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
    public String id;

    /**
     * the title of the track
     */
    public String title;

    /**
     * the key of the file this track was downloaded to
     */
    public StorageKey fileKey;

    /**
     * is this track fully downloaded?
     */
    public boolean isDownloaded;

    public TrackInfo(@NonNull String id, String title, StorageKey fileKey, boolean isDownloaded) {
        this.id = id;
        this.title = title;
        this.fileKey = fileKey;
        this.isDownloaded = isDownloaded;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrackInfo trackInfo = (TrackInfo) o;
        return id.equals(trackInfo.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

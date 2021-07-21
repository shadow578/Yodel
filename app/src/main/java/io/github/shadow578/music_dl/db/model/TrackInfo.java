package io.github.shadow578.music_dl.db.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

import io.github.shadow578.music_dl.db.TracksDB;
import io.github.shadow578.music_dl.util.storage.StorageKey;

/**
 * information about a track
 */
@Entity(tableName = "tracks")
public class TrackInfo {

    /**
     * create a new track that is still not downloaded
     *
     * @param id    the youtube id of the track
     * @param title the title of the track
     * @return the track instance
     */
    @NonNull
    public static TrackInfo createNew(@NonNull String id, @NonNull String title) {
        return new TrackInfo(id, title, StorageKey.EMPTY, TrackStatus.DownloadPending);
    }

    /**
     * the youtube id of the track
     */
    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "id")
    public String id;

    /**
     * the title of the track
     */
    @NonNull
    @ColumnInfo(name = "track_title")
    public String title;

    /**
     * the key of the file this track was downloaded to
     */
    @NonNull
    @ColumnInfo(name = "download_file_key")
    public StorageKey fileKey;

    /**
     * is this track fully downloaded?
     */
    @NonNull
    @ColumnInfo(name = "status")
    public TrackStatus status;

    public TrackInfo(@NonNull String id, @NonNull String title, @NonNull StorageKey fileKey, @NonNull TrackStatus status) {
        this.id = id;
        this.title = title;
        this.fileKey = fileKey;
        this.status = status;
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

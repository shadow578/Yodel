package io.github.shadow578.music_dl.db.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
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
     * create a new track that is still not downloaded
     *
     * @param id    the youtube id of the track
     * @param title the title of the track
     * @return the track instance
     */
    @NonNull
    public static TrackInfo createNew(@NonNull String id, @NonNull String title) {
        return new TrackInfo(id, title, StorageKey.EMPTY, false, false);
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
    @ColumnInfo(name = "is_downloaded")
    public boolean isDownloaded;

    /**
     * did the download fail? if this flag is set, the download will not be retried.
     */
    @ColumnInfo(name = "did_download_fail")
    public boolean didDownloadFail;

    public TrackInfo(@NonNull String id, @NonNull String title, @NonNull StorageKey fileKey, boolean isDownloaded, boolean didDownloadFail) {
        this.id = id;
        this.title = title;
        this.fileKey = fileKey;
        this.isDownloaded = isDownloaded;
        this.didDownloadFail = didDownloadFail;
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

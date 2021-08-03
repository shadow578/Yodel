package io.github.shadow578.music_dl.db.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.time.LocalDate;
import java.util.Objects;

import io.github.shadow578.music_dl.KtPorted;
import io.github.shadow578.music_dl.util.storage.StorageKey;

/**
 * information about a track
 */
@Entity(tableName = "tracks",
        indices = {
                @Index("first_added_at")
        })
@KtPorted
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
        return new TrackInfo(id, System.currentTimeMillis(), title, null, null, null, null, StorageKey.EMPTY, StorageKey.EMPTY, TrackStatus.DownloadPending);
    }

    /**
     * the youtube id of the track
     */
    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "id")
    public final String id;

    /**
     * when this track was first added. millis timestamp, from {@link System#currentTimeMillis()}
     */
    @ColumnInfo(name = "first_added_at")
    public final long firstAddedAt;

    /**
     * the title of the track
     */
    @NonNull
    @ColumnInfo(name = "track_title")
    public String title;

    /**
     * the name of the artist
     */
    @Nullable
    @ColumnInfo(name = "artist_name")
    public String artist;

    /**
     * the day the track was released / uploaded
     */
    @Nullable
    @ColumnInfo(name = "release_date")
    public LocalDate releaseDate;

    /**
     * duration of the track, in seconds
     */
    @Nullable
    @ColumnInfo(name = "duration")
    public Long duration;

    /**
     * the album name, if this track is part of one
     */
    @Nullable
    @ColumnInfo(name = "album_name")
    public String albumName;

    /**
     * the key of the file this track was downloaded to
     */
    @NonNull
    @ColumnInfo(name = "audio_file_key")
    public StorageKey audioFileKey;

    /**
     * the key of the track cover image file
     */
    @NonNull
    @ColumnInfo(name = "cover_file_key")
    public StorageKey coverKey;

    /**
     * is this track fully downloaded?
     */
    @NonNull
    @ColumnInfo(name = "status")
    public TrackStatus status;

    public TrackInfo(@NonNull String id, long firstAddedAt, @NonNull String title, @Nullable String artist, @Nullable LocalDate releaseDate, @Nullable Long duration, @Nullable String albumName, @NonNull StorageKey audioFileKey, @NonNull StorageKey coverKey, @NonNull TrackStatus status) {
        this.id = id;
        this.firstAddedAt = firstAddedAt;
        this.title = title;
        this.artist = artist;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.albumName = albumName;
        this.audioFileKey = audioFileKey;
        this.coverKey = coverKey;
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

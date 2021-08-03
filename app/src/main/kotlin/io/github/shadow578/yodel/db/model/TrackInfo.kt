package io.github.shadow578.yodel.db.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.shadow578.yodel.util.storage.StorageKey
import java.time.LocalDate

/**
 * information about a track
 */
@Entity(
    tableName = "tracks",
    indices = [
        Index("first_added_at")
    ]
)
data class TrackInfo(
    /**
     * the youtube id of the track
     */
    @field:ColumnInfo(name = "id")
    @field:PrimaryKey
    val id: String,

    /**
     * the title of the track
     */
    @field:ColumnInfo(name = "track_title")
    var title: String,

    /**
     * the name of the artist
     */
    @field:ColumnInfo(name = "artist_name")
    var artist: String? = null,

    /**
     * the day the track was released / uploaded
     */
    @field:ColumnInfo(name = "release_date")
    var releaseDate: LocalDate? = null,

    /**
     * duration of the track, in seconds
     */
    @field:ColumnInfo(name = "duration")
    var duration: Long? = null,

    /**
     * the album name, if this track is part of one
     */
    @field:ColumnInfo(name = "album_name")
    var albumName: String? = null,

    /**
     * the key of the file this track was downloaded to
     */
    @field:ColumnInfo(name = "audio_file_key")
    var audioFileKey: StorageKey = StorageKey.EMPTY,

    /**
     * the key of the track cover image file
     */
    @field:ColumnInfo(name = "cover_file_key")
    var coverKey: StorageKey = StorageKey.EMPTY,

    /**
     * is this track fully downloaded?
     */
    @field:ColumnInfo(name = "status")
    var status: TrackStatus = TrackStatus.DownloadPending,

    /**
     * when this track was first added. millis timestamp, from [System.currentTimeMillis]
     */
    @field:ColumnInfo(name = "first_added_at")
    val firstAddedAt: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TrackInfo

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
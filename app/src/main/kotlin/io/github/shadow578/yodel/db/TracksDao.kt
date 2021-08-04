package io.github.shadow578.yodel.db

import androidx.lifecycle.LiveData
import androidx.room.*
import io.github.shadow578.yodel.db.model.TrackInfo

/**
 * DAO for tracks
 */
@Dao
interface TracksDao {
    /**
     * observe all tracks
     *
     * @return the tracks that can be observed
     */
    @Query("SELECT * FROM tracks ORDER BY first_added_at ASC")
    fun observe(): LiveData<List<TrackInfo>>

    /**
     * observe all tracks that have to be downloaded
     *
     * @return the tracks that can be observed
     */
    @Query("SELECT * FROM tracks WHERE status = 'pending'")
    fun observePending(): LiveData<List<TrackInfo>>

    /**
     * get a list of all tracks
     *
     * @return a list of all tracks
     */
    @get:Query("SELECT * FROM tracks")
    val all: List<TrackInfo>

    /**
     * get a list of all tracks that are marked as downloaded
     *
     * @return a list of all downloaded tracks
     */
    @get:Query("SELECT * FROM tracks WHERE status = 'downloaded'")
    val downloaded: List<TrackInfo>

    /**
     * reset all tracks in downloading status back to pending
     */
    @Query("UPDATE tracks SET status = 'pending' WHERE status = 'downloading'")
    fun resetDownloadingToPending()

    /**
     * get a track from the db
     *
     * @param id the id of the track
     * @return the track, or null if not found
     */
    @Query("SELECT * FROM tracks WHERE id = :id")
    operator fun get(id: String?): TrackInfo?

    /**
     * insert a track into the db
     *
     * @param track the track to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(track: TrackInfo)

    /**
     * insert multiple tracks into the db, replacing existing entries
     *
     * @param tracks the tracks to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(tracks: List<TrackInfo>)

    /**
     * insert multiple tracks into the db, skipping existing entries
     *
     * @param tracks the tracks to insert
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAllNew(tracks: List<TrackInfo>)

    /**
     * update a track
     *
     * @param track the track to update
     */
    @Update
    fun update(track: TrackInfo)

    /**
     * remove a single track from the db
     *
     * @param track the track to remove
     */
    @Delete
    fun remove(track: TrackInfo)
}
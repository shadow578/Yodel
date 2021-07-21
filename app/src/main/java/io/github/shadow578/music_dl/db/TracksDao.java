package io.github.shadow578.music_dl.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.github.shadow578.music_dl.db.model.TrackInfo;

/**
 * DAO for tracks
 */
@Dao
@SuppressWarnings("unused")
public interface TracksDao {

    /**
     * observe all tracks
     *
     * @return the tracks that can be observed
     */
    @Query("SELECT * FROM tracks")
    LiveData<List<TrackInfo>> observe();

    /**
     * observe all tracks that have to be downloaded
     *
     * @return the tracks that can be observed
     */
    @Query("SELECT * FROM tracks WHERE status = 'pending'")
    LiveData<List<TrackInfo>> observePending();

    /**
     * get a list of all tracks that are marked as downloaded
     *
     * @return a list of all downloaded tracks
     */
    @Query("SELECT * FROM tracks WHERE status = 'downloaded'")
    List<TrackInfo> getDownloaded();

    /**
     * get a track from the db
     *
     * @param id the id of the track
     * @return the track, or null if not found
     */
    @Query("SELECT * FROM tracks WHERE id = :id")
    TrackInfo get(String id);

    /**
     * insert a track into the db
     *
     * @param track the track to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TrackInfo track);

    /**
     * update a track
     *
     * @param track the track to update
     */
    @Update
    void update(TrackInfo track);

    /**
     * remove a single track from the db
     *
     * @param track the track to remove
     */
    @Delete
    void remove(TrackInfo track);

    /**
     * remove multiple tracks from the db
     *
     * @param tracks the tracks to remove
     */
    @Delete
    void removeAll(List<TrackInfo> tracks);
}

package io.github.shadow578.music_dl.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.github.shadow578.music_dl.db.model.TrackInfo;

/**
 * DAO for tracks
 */
@Dao
public interface TracksDao {

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

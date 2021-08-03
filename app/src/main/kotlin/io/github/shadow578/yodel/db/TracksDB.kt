package io.github.shadow578.yodel.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.github.shadow578.yodel.db.model.TrackInfo
import io.github.shadow578.yodel.db.model.TrackStatus
import io.github.shadow578.yodel.util.storage.decodeToFile

/**
 * the tracks database
 */
@TypeConverters(DBTypeConverters::class)
@Database(
    entities = [
        TrackInfo::class
    ],
    version = 7
)
abstract class TracksDB : RoomDatabase() {
    /**
     * mark all tracks db that no longer exist (or are not accessible) in the file system as [TrackStatus.FileDeleted]
     *
     * @param ctx the context to get the files in
     * @return the number of removed tracks
     */
    fun markDeletedTracks(ctx: Context): Int {
        // get all tracks that are (supposedly) downloaded
        val supposedlyDownloadedTracks = tracks().downloaded

        // check on every track if the downloaded file still exists
        var count = 0
        for (track in supposedlyDownloadedTracks) {
            // get file for this track
            val trackFile = track.audioFileKey.decodeToFile(ctx)

            // if the file could not be decoded,
            // the file cannot be read OR it does not exist
            // assume it was removed
            if (trackFile != null
                && trackFile.canRead()
                && trackFile.exists()
            ) {
                // file still there, do no more
                continue
            }

            // mark as deleted track
            track.status = TrackStatus.FileDeleted
            tracks().update(track)
            count++
        }
        return count
    }

    /**
     * @return the tracks DAO
     */
    abstract fun tracks(): TracksDao

    companion object {
        /**
         * database name
         */
        const val DB_NAME = "tracks"

        /**
         * the instance singleton
         */
        private lateinit var instance: TracksDB

        /**
         * initialize the database
         *
         * @param ctx the context to work in
         * @return the freshly initialized instance
         */
        fun get(ctx: Context): TracksDB {
            if (!this::instance.isInitialized) {
                // have to initialize db
                instance = Room.databaseBuilder(
                    ctx,
                    TracksDB::class.java, DB_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
            }

            return instance
        }
    }
}
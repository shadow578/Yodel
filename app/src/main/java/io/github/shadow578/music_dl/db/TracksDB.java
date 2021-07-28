package io.github.shadow578.music_dl.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import java.io.File;
import java.util.List;
import java.util.Optional;

import io.github.shadow578.music_dl.db.model.TrackInfo;
import io.github.shadow578.music_dl.db.model.TrackStatus;
import io.github.shadow578.music_dl.util.storage.StorageHelper;

/**
 * the tracks database
 */
@TypeConverters({
        DBTypeConverters.class
})
@Database(entities = {
        TrackInfo.class
}, version = 6)
public abstract class TracksDB extends RoomDatabase {

    /**
     * database name
     */
    private static final String DB_NAME = "tracks";

    /**
     * the instance singleton
     */
    private static TracksDB INSTANCE;

    /**
     * initialize the database
     *
     * @param ctx the context to work in
     * @return the freshly initialized instance
     */
    public static TracksDB init(@NonNull Context ctx) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(ctx, TracksDB.class, DB_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }

        return getInstance();
    }

    /**
     * get the absolute path to the database file
     *
     * @param ctx the context to work in
     * @return the path to the database file
     */
    public static File getDatabasePath(@NonNull Context ctx) {
        return ctx.getDatabasePath(DB_NAME);
    }

    /**
     * @return the singleton instance
     */
    public static TracksDB getInstance() {
        return INSTANCE;
    }

    /**
     * mark all tracks db that no longer exist (or are not accessible) in the file system as {@link io.github.shadow578.music_dl.db.model.TrackStatus#FileDeleted}
     *
     * @param ctx the context to get the files in
     * @return the number of removed tracks
     */
    public int markDeletedTracks(@NonNull Context ctx) {
        // get all tracks that are (supposedly) downloaded
        final List<TrackInfo> supposedlyDownloadedTracks = tracks().getDownloaded();

        // check on every track if the downloaded file still exists
        int count = 0;
        for (TrackInfo track : supposedlyDownloadedTracks) {
            // get file for this track
            final Optional<DocumentFile> trackFile = StorageHelper.decodeFile(ctx, track.audioFileKey);

            // if the file could not be decoded,
            // the file cannot be read OR it does not exist
            // assume it was removed
            if (trackFile.isPresent()
                    && trackFile.get().canRead()
                    && trackFile.get().exists()) {
                // file still there, do no more
                continue;
            }

            // mark as deleted track
            track.status = TrackStatus.FileDeleted;
            tracks().update(track);
            count++;
        }
        return count;
    }

    /**
     * @return the tracks DAO
     */
    public abstract TracksDao tracks();
}

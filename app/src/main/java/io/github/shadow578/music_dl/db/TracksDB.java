package io.github.shadow578.music_dl.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.github.shadow578.music_dl.db.model.TrackInfo;
import io.github.shadow578.music_dl.util.storage.StorageHelper;

/**
 * the tracks database
 */
@TypeConverters({
        DBTypeConverters.class
})
@Database(entities = {
        TrackInfo.class
}, version = 3)
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
     * remove all tracks from the db that no longer exist (or are not accessible) in the file system
     *
     * @param ctx the context to get the files in
     * @return the number of removed tracks
     */
    public int removeDeletedTracks(@NonNull Context ctx) {
        // get all tracks that are (supposedly) downloaded
        final List<TrackInfo> supposedlyDownloadedTracks = tracks().getDownloaded();

        // check on every track if the downloaded file still exists
        final List<TrackInfo> deletedTracks = new ArrayList<>();
        for (TrackInfo track : supposedlyDownloadedTracks) {
            // get file for this track
            final Optional<DocumentFile> trackFile = StorageHelper.decodeFile(ctx, track.fileKey);

            // if the file could not be decoded, assume it was deleted
            if (!trackFile.isPresent()) {
                deletedTracks.add(track);
                continue;
            }

            // if the file does not exist OR we have no read permissions, assume it was deleted
            final DocumentFile file = trackFile.get();
            if (!file.canRead() || !file.exists()) {
                deletedTracks.add(track);
            }
        }

        // remove deleted tracks from DB
        tracks().removeAll(deletedTracks);
        return deletedTracks.size();
    }

    /**
     * @return the tracks DAO
     */
    public abstract TracksDao tracks();
}

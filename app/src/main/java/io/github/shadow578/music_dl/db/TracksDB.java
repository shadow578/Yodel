package io.github.shadow578.music_dl.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import java.io.File;

import io.github.shadow578.music_dl.db.model.TrackInfo;

/**
 * the tracks database
 */
@TypeConverters({
        DBTypeConverters.class
})
@Database(entities = {
        TrackInfo.class
}, version = 2)
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
     */
    public static void init(@NonNull Context ctx) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(ctx, TracksDB.class, DB_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
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
     * @return the tracks DAO
     */
    public abstract TracksDao tracks();
}

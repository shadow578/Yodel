package io.github.shadow578.music_dl.backup;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import io.github.shadow578.music_dl.KtPorted;
import io.github.shadow578.music_dl.db.TracksDB;
import io.github.shadow578.music_dl.db.model.TrackInfo;

/**
 * tracks db backup helper class.
 * all functions must be called from a background thread
 */
@KtPorted
public class BackupHelper {
    /**
     * gson for backup serialization and deserialization
     */
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new BackupGSONAdapters.LocalDateTimeAdapter())
            .registerTypeAdapter(LocalDate.class, new BackupGSONAdapters.LocalDateAdapter())
            .create();

    /**
     * tag for logging
     */
    private static final String TAG = "BackupHelper";

    /**
     * create a new backup of all tracks
     *
     * @param ctx  the context to work in
     * @param file the file to write the backup to
     * @return was the backup successful
     */
    public static boolean createBackup(@NonNull Context ctx, @NonNull DocumentFile file) {
        // get all tracks in DB
        final List<TrackInfo> tracks = TracksDB.init(ctx).tracks().getAll();
        if (tracks == null || tracks.size() <= 0) {
            return false;
        }

        // create backup data
        final BackupData backup = new BackupData(tracks, LocalDateTime.now());

        // serialize and write to file
        try (final OutputStreamWriter out = new OutputStreamWriter(ctx.getContentResolver().openOutputStream(file.getUri()))) {
            gson.toJson(backup, out);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "writing backup file failed!", e);
            return false;
        }
    }

    /**
     * read backup data from a file
     *
     * @param ctx  the context to read in
     * @param file the file to read the data from
     * @return the backup data
     */
    @NonNull
    public static Optional<BackupData> readBackupData(@NonNull Context ctx, @NonNull DocumentFile file) {
        try (final InputStreamReader src = new InputStreamReader(ctx.getContentResolver().openInputStream(file.getUri()))) {
            return Optional.ofNullable(gson.fromJson(src, BackupData.class));
        } catch (IOException | JsonSyntaxException | JsonIOException e) {
            Log.e(TAG, "failed to read backup data!", e);
            return Optional.empty();
        }
    }

    /**
     * restore a backup into the db
     *
     * @param ctx             the context to work in
     * @param data            the data to restore
     * @param replaceExisting if true, existing entries are overwritten. if false, existing entries are not added
     */
    public static void restoreBackup(@NonNull Context ctx, @NonNull BackupData data, boolean replaceExisting) {
        // check there are tracks to import
        if (data.tracks.size() <= 0) {
            return;
        }

        // insert the tracks
        if (replaceExisting) {
            TracksDB.init(ctx).tracks().insertAll(data.tracks);
        } else {
            TracksDB.init(ctx).tracks().insertAllNew(data.tracks);
        }
    }
}

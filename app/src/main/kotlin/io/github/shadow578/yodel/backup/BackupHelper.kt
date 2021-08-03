package io.github.shadow578.yodel.backup

import android.content.Context
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.google.gson.GsonBuilder
import com.google.gson.JsonIOException
import com.google.gson.JsonSyntaxException
import io.github.shadow578.music_dl.db.TracksDB
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

/**
 * tracks db backup helper class.
 * all functions must be called from a background thread
 */
object BackupHelper {
    /**
     * gson for backup serialization and deserialization
     */
    private val gson = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
        .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
        .create()

    /**
     * tag for logging
     */
    private const val TAG = "BackupHelper"

    /**
     * create a new backup of all tracks
     *
     * @param ctx  the context to work in
     * @param file the file to write the backup to
     * @return was the backup successful
     */
    fun createBackup(ctx: Context, file: DocumentFile): Boolean {
        // get all tracks in DB
        val tracks = TracksDB.init(ctx).tracks().all
        if (tracks.size <= 0) return false

        // create backup data
        val backup = BackupData(tracks, LocalDateTime.now())

        // serialize and write to file
        try {
            OutputStreamWriter(ctx.contentResolver.openOutputStream(file.uri)).use { out ->
                gson.toJson(backup, out)
                return true
            }
        } catch (e: IOException) {
            Log.e(TAG, "writing backup file failed!", e)
            return false
        }
    }

    /**
     * read backup data from a file
     *
     * @param ctx  the context to read in
     * @param file the file to read the data from
     * @return the backup data
     */
    fun readBackupData(ctx: Context, file: DocumentFile): Optional<BackupData> {
        try {
            InputStreamReader(ctx.contentResolver.openInputStream(file.uri)).use { src ->
                return Optional.ofNullable(
                    gson.fromJson(
                        src,
                        BackupData::class.java
                    )
                )
            }
        } catch (e: IOException) {
            Log.e(TAG, "failed to read backup data!", e)
            return Optional.empty()
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "failed to read backup data!", e)
            return Optional.empty()
        } catch (e: JsonIOException) {
            Log.e(TAG, "failed to read backup data!", e)
            return Optional.empty()
        }
    }

    /**
     * restore a backup into the db
     *
     * @param ctx             the context to work in
     * @param data            the data to restore
     * @param replaceExisting if true, existing entries are overwritten. if false, existing entries are not added
     */
    fun restoreBackup(ctx: Context, data: BackupData, replaceExisting: Boolean) {
        // check there are tracks to import
        if (data.tracks.size <= 0) return

        // insert the tracks
        if (replaceExisting)
            TracksDB.init(ctx).tracks().insertAll(data.tracks)
        else
            TracksDB.init(ctx).tracks().insertAllNew(data.tracks)
    }
}
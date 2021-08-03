package io.github.shadow578.music_dl.db;

import androidx.room.TypeConverter;

import java.time.LocalDate;

import io.github.shadow578.music_dl.KtPorted;
import io.github.shadow578.music_dl.db.model.TrackStatus;
import io.github.shadow578.music_dl.util.storage.StorageKey;

/**
 * type converters for room
 */
@KtPorted
class DBTypeConverters {

    //region StorageKey
    @TypeConverter
    public static String fromStorageKey(StorageKey key) {
        if (key == null) {
            return null;
        }

        return key.toString();
    }

    @TypeConverter
    public static StorageKey toStorageKey(String key) {
        if (key == null) {
            return null;
        }

        return new StorageKey(key);
    }
    //endregion

    //region TrackStatus
    @TypeConverter
    public static String fromTrackStatus(TrackStatus status) {
        if (status == null) {
            return null;
        }

        return status.key();
    }

    @TypeConverter
    public static TrackStatus toTrackStatus(String key) {
        if (key == null) {
            return null;
        }

        final TrackStatus s = TrackStatus.findByKey(key);
        if (s != null) {
            return s;
        }

        return TrackStatus.DownloadPending;
    }
    //endregion

    //region LocalDate
    @TypeConverter
    public static String fromLocalDate(LocalDate date) {
        if (date == null) {
            return null;
        }

        return date.toString();
    }

    @TypeConverter
    public static LocalDate toLocalDate(String string) {
        if (string == null) {
            return null;
        }

        return LocalDate.parse(string);
    }
    //endregion
}

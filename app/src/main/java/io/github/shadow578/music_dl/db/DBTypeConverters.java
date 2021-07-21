package io.github.shadow578.music_dl.db;

import androidx.room.TypeConverter;

import io.github.shadow578.music_dl.db.model.TrackStatus;
import io.github.shadow578.music_dl.util.storage.StorageKey;

/**
 * type converters for room
 */
class DBTypeConverters {

    //region StorageKey
    @TypeConverter
    public String fromStorageKey(StorageKey key) {
        return key.toString();
    }

    @TypeConverter
    public StorageKey toStorageKey(String key) {
        return new StorageKey(key);
    }
    //endregion

    //region TrackStatus
    @TypeConverter
    public String fromTrackStatus(TrackStatus status) {
        return status.key();
    }

    @TypeConverter
    public TrackStatus toTrackStatus(String key) {
        final TrackStatus s = TrackStatus.findByKey(key);
        if (s != null) {
            return s;
        }

        return TrackStatus.DownloadPending;
    }

    //endregion
}

package io.github.shadow578.music_dl.db;

import androidx.room.TypeConverter;

import io.github.shadow578.music_dl.util.storage.StorageKey;

/**
 * type converters for room
 */
class DBTypeConverters {

    @TypeConverter
    public String fromStorageKey(StorageKey key)
    {
        return key.toString();
    }

    @TypeConverter
    public StorageKey toStorageKey(String key)
    {
        return new StorageKey(key);
    }
}

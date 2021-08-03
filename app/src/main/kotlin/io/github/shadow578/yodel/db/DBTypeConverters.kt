package io.github.shadow578.yodel.db

import androidx.room.TypeConverter
import io.github.shadow578.yodel.db.model.TrackStatus
import io.github.shadow578.yodel.util.storage.StorageKey
import java.time.LocalDate

/**
 * type converters for room
 */
class DBTypeConverters {
    //region StorageKey
    @TypeConverter
    fun fromStorageKey(key: StorageKey?): String? {
        return key?.toString()
    }

    @TypeConverter
    fun toStorageKey(key: String?): StorageKey? {
        return if (key == null) {
            null
        } else StorageKey(key)
    }

    //endregion

    //region TrackStatus
    @TypeConverter
    fun fromTrackStatus(status: TrackStatus?): String? {
        return status?.key
    }

    @TypeConverter
    fun toTrackStatus(key: String?): TrackStatus? {
        if (key == null) {
            return null
        }
        val s = TrackStatus.findByKey(key)
        return s ?: TrackStatus.DownloadPending
    }

    //endregion

    //region LocalDate
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.toString()
    }

    @TypeConverter
    fun toLocalDate(string: String?): LocalDate? {
        return if (string == null) {
            null
        } else LocalDate.parse(string)
    } //endregion
}
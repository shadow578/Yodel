package io.github.shadow578.yodel.backup

import com.google.gson.annotations.SerializedName
import io.github.shadow578.yodel.db.model.TrackInfo
import java.time.LocalDateTime

/**
 * backup data written by [BackupHelper]
 */
@Suppress("unused")
data class BackupData(
    /**
     * the tracks in this backup
     */
    @SerializedName("tracks")
    val tracks: List<TrackInfo>,

    /**
     * the time the backup was created
     */
    @SerializedName("backup_time")
    val backupTime: LocalDateTime
)
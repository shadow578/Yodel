package io.github.shadow578.yodel.backup

import io.github.shadow578.yodel.db.model.TrackInfo
import java.time.LocalDateTime

/**
 * backup data written by [BackupHelper]
 */
@Suppress("unused")
class BackupData(
    /**
     * the tracks in this backup
     */
    val tracks: List<TrackInfo>,

    /**
     * the time the backup was created
     */
    val backupTime: LocalDateTime
)
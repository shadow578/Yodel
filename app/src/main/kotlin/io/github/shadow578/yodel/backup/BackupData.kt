package io.github.shadow578.yodel.backup

import io.github.shadow578.music_dl.backup.BackupHelper
import io.github.shadow578.music_dl.db.model.TrackInfo
import java.time.LocalDateTime

/**
 * backup data written by [BackupHelper]
 */
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
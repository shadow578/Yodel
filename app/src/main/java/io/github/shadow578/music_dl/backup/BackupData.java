package io.github.shadow578.music_dl.backup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.time.LocalDateTime;
import java.util.List;

import io.github.shadow578.music_dl.db.model.TrackInfo;

/**
 * backup data written by {@link BackupHelper}
 */
public class BackupData {
    /**
     * the tracks in this backup
     */
    @NonNull
    public List<TrackInfo> tracks;

    /**
     * the time the backup was created
     */
    @Nullable
    public LocalDateTime backupTime;

    public BackupData(@NonNull List<TrackInfo> tracks, @Nullable LocalDateTime backupTime) {
        this.tracks = tracks;
        this.backupTime = backupTime;
    }
}

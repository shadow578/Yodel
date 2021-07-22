package io.github.shadow578.music_dl;

import android.app.Application;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;

import io.github.shadow578.music_dl.db.TracksDB;
import io.github.shadow578.music_dl.downloader.DownloaderService;
import io.github.shadow578.music_dl.util.Util;
import io.github.shadow578.music_dl.util.notifications.NotificationChannels;
import io.github.shadow578.music_dl.util.preferences.PreferenceWrapper;

/**
 * application instance, for boilerplate init
 */
public class MusicDLApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        PreferenceWrapper.init(PreferenceManager.getDefaultSharedPreferences(this));
        NotificationChannels.registerAll(this);

        // remove tracks that were deleted
        Util.runAsync(() -> {
            final int removedCount = TracksDB.init(this).markDeletedTracks(this);
            Log.i("MusicDL", String.format("removed %d tracks that were deleted in the file system", removedCount));
        });

        // start downloader service
        final Intent serviceIntent = new Intent(this, DownloaderService.class);
        startService(serviceIntent);
    }
}

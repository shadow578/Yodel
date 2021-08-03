package io.github.shadow578.music_dl;

import android.app.Application;
import android.preference.PreferenceManager;
import android.util.Log;

import io.github.shadow578.music_dl.db.TracksDB;
import io.github.shadow578.music_dl.util.Async;
import io.github.shadow578.music_dl.util.notifications.NotificationChannels;
import io.github.shadow578.music_dl.util.preferences.PreferenceWrapper;

/**
 * application instance, for boilerplate init
 */
@KtPorted
public class MusicDLApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        PreferenceWrapper.init(PreferenceManager.getDefaultSharedPreferences(this));
        NotificationChannels.registerAll(this);

        // remove tracks that were deleted
        Async.runAsync(() -> {
            final int removedCount = TracksDB.init(this).markDeletedTracks(this);
            Log.i("MusicDL", String.format("removed %d tracks that were deleted in the file system", removedCount));
        });
    }
}

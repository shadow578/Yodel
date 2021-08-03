package io.github.shadow578.yodel

import android.app.Application
import android.util.Log
import androidx.preference.PreferenceManager
import io.github.shadow578.music_dl.db.TracksDB
import io.github.shadow578.yodel.util.NotificationChannels
import io.github.shadow578.yodel.util.launchIO
import io.github.shadow578.yodel.util.preferences.PreferenceWrapper

/**
 * application class, for boilerplate init
 */
class YodelApp : Application() {
    override fun onCreate() {
        super.onCreate()
        PreferenceWrapper.init(PreferenceManager.getDefaultSharedPreferences(this))
        NotificationChannels.registerAll(this)

        // find tracks that were deleted
        launchIO {
            val removedCount = TracksDB.init(this@YodelApp).markDeletedTracks(this@YodelApp)
            Log.i("Yodel", "found $removedCount tracks that were deleted in the file system")
        }
    }
}
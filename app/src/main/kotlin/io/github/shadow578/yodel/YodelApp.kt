package io.github.shadow578.yodel

import android.app.Application
import android.util.Log
import androidx.preference.PreferenceManager
import io.github.shadow578.yodel.db.TracksDB
import io.github.shadow578.yodel.util.*
import io.github.shadow578.yodel.util.preferences.PreferenceWrapper

/**
 * application class, for boilerplate init
 */
@Suppress("unused")
class YodelApp : Application() {
    override fun onCreate() {
        super.onCreate()
        PreferenceWrapper.init(PreferenceManager.getDefaultSharedPreferences(this))
        NotificationChannels.registerAll(this)

        // enable strict mode for diagnostics
        maybeEnableStrictMode()

        // find tracks that were deleted
        launchIO {
            val removedCount = TracksDB.get(this@YodelApp).markDeletedTracks(this@YodelApp)
            Log.i("Yodel", "found $removedCount tracks that were deleted in the file system")
        }
    }
}
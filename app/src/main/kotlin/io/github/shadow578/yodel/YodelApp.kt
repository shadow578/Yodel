package io.github.shadow578.yodel

import android.app.Application
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColors
import io.github.shadow578.yodel.db.TracksDB
import io.github.shadow578.yodel.util.*
import io.github.shadow578.yodel.util.preferences.PreferenceWrapper
import timber.log.Timber

/**
 * application class, for boilerplate init
 */
@Suppress("unused")
class YodelApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // init timber
        if (BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())

        // apply material3 dynamic colors
        DynamicColors.applyToActivitiesIfAvailable(this)

        // initialize stuff
        PreferenceWrapper.init(PreferenceManager.getDefaultSharedPreferences(this))
        NotificationChannels.registerAll(this)

        // enable strict mode for diagnostics
        maybeEnableStrictMode()

        // find tracks that were deleted
        launchIO {
            val removedCount = TracksDB.get(this@YodelApp).markDeletedTracks(this@YodelApp)
            Timber.i("found $removedCount tracks that were deleted in the file system")
        }
    }
}
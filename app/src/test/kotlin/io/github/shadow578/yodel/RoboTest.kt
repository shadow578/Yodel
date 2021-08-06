package io.github.shadow578.yodel

import android.app.Application
import android.content.Context
import android.os.Build.VERSION_CODES.*
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import io.github.shadow578.yodel.util.NotificationChannels
import io.github.shadow578.yodel.util.preferences.PreferenceWrapper
import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * base class for all robolectric test classes.
 * this handles the config. put shared test code in here.
 *
 * tests from M (minSdk) to R (targetSdk)
 */
@RunWith(RobolectricTestRunner::class)
@Config(application = YodelTestApp::class, sdk = [M, /*N,*/ /*N_MR1,*/ O, /*O_MR1,*/ P, /*Q,*/ R])
open class RoboTest {

    /**
     * check the instrumentation setup
     */
    @Test
    fun validateInstrumentation() {
        context.shouldNotBeNull()
    }

    /**
     * the instrumentation context
     */
    protected val context: Context
        get() = ApplicationProvider.getApplicationContext()
}

/**
 * application class, for boilerplate init.
 * special version for robolectric tests, does not try to mark removed tracks in db
 */
class YodelTestApp : Application() {
    override fun onCreate() {
        super.onCreate()
        PreferenceWrapper.init(PreferenceManager.getDefaultSharedPreferences(this))
        NotificationChannels.registerAll(this)
    }
}
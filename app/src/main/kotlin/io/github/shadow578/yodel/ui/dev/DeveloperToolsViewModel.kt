package io.github.shadow578.yodel.ui.dev

import android.app.*
import android.content.*
import android.os.Build
import android.widget.Toast
import androidx.lifecycle.*
import io.github.shadow578.yodel.BuildConfig
import io.github.shadow578.yodel.db.TracksDB
import io.github.shadow578.yodel.db.model.TrackStatus
import io.github.shadow578.yodel.util.*
import io.github.shadow578.yodel.util.preferences.Prefs
import timber.log.Timber
import java.io.File
import java.time.LocalDateTime
import java.util.*

/**
 * view model for the developer tools activity
 */
class DeveloperToolsViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * current state of downloader_error_notifications enable
     */
    val enableDownloaderErrorNotifications =
        MutableLiveData(Prefs.EnableDownloaderErrorNotifications.get())

    /**
     * set downloader_error_notifications enable
     *
     * @param enable enable downloader_error_notifications?
     */
    fun setEnableDownloaderErrorNotifications(enable: Boolean) {
        if (java.lang.Boolean.valueOf(enable) == enableDownloaderErrorNotifications.value) {
            return
        }
        Prefs.EnableDownloaderErrorNotifications.set(enable)
        enableDownloaderErrorNotifications.value = enable
    }

    /**
     * current state of downloader_verbose_output enable
     */
    val enableDownloaderVerboseOutput =
        MutableLiveData(Prefs.EnableDownloaderVerboseOutput.get())

    /**
     * set downloader_verbose_output enable
     *
     * @param enable enable downloader_verbose_output?
     */
    fun setEnableDownloaderVerboseOutput(enable: Boolean) {
        if (java.lang.Boolean.valueOf(enable) == enableDownloaderVerboseOutput.value) {
            return
        }
        Prefs.EnableDownloaderVerboseOutput.set(enable)
        enableDownloaderVerboseOutput.value = enable
    }

    /**
     * current state of ssl_fix enable
     */
    val enableSSLFix = MutableLiveData(Prefs.EnableSSLFix.get())

    /**
     * set ssl fix enable
     *
     * @param enable enable ssl fix?
     */
    fun setEnableSSLFix(enable: Boolean) {
        if (java.lang.Boolean.valueOf(enable) == enableSSLFix.value) {
            return
        }
        Prefs.EnableSSLFix.set(enable)
        enableSSLFix.value = enable
    }

    /**
     * remove all downloaded files, and mark all tracks as 'pending'
     */
    fun reloadAllTracks() {
        // get all tracks from db
        val ctx = getApplication<Application>().applicationContext
        launchIO {
            TracksDB.get(ctx).tracks().all.forEach {
                // remove files
                it.deleteLocalFiles(ctx)

                // reset track to pending status
                it.status = TrackStatus.DownloadPending

                // update the track
                TracksDB.get(ctx).tracks().update(it)
            }
        }

        // show toast
        ctx.toast("Reloading all tracks...", Toast.LENGTH_SHORT)
    }

    /**
     * get device and app debug info, formatted into a nice string
     */
    val debugInfo: String
        get() = """
            -- App Info --
            App ID:       ${BuildConfig.APPLICATION_ID}
            Version:      ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})
            Build Type:   ${BuildConfig.BUILD_TYPE}
            Is Debug:     ${BuildConfig.DEBUG}
            
            -- Android Info --
            Version:      ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT}
            Build ID:     ${Build.DISPLAY}
            Tags:         ${Build.TAGS}
            ABIs:         ${Build.SUPPORTED_ABIS.joinToString(separator = ", ")}}
            Default Lang: ${Locale.getDefault().displayName}
            
            -- Device Info --
            Brand:        ${Build.BRAND}
            Manufacturer: ${Build.MANUFACTURER}
            Name:         ${Build.DEVICE}
            Model:        ${Build.MODEL}
            Product Name: ${Build.PRODUCT}  
        """.trimIndent()

    /**
     * dump all errors in logcat, append [debugInfo] and share the file
     *
     * @param parent the parent activity, used for sharing
     */
    fun dumpLogcat(parent: Activity) {
        parent.toast("Dumping Logcat...", Toast.LENGTH_SHORT)
        launchIO {
            try {
                // create a temporary file in cache
                val dir = File(parent.cacheDir, "logcat_dumps")
                val logFile = dir.getTempFile("logcat_", ".txt")
                dir.mkdirs()

                // add a timestamp
                Timber.e("-- dumpLogcat at ${LocalDateTime.now()} --")

                // exec logcat command, write to the file
                //FIXME: warning "Inappropriate blocking method call"
                Runtime.getRuntime().exec("logcat *:E -d -f ${logFile.absolutePath}").waitFor()

                // append debug info
                logFile.appendText(debugInfo)

                // share the file
                launchMain {
                    shareLogcatDump(parent, logFile)
                }
            } catch (e: Throwable) {
                // failed to get logs
                Timber.e(e, "could not dump logcat")
                launchMain {
                    parent.toast("could not get logs: ${e.message}")
                }
            }
        }
    }

    /**
     * share a logcat dump file
     *
     * @param parent the parent activity, to start the app chooser with
     * @param file the log file to share / open
     */
    private fun shareLogcatDump(parent: Activity, file: File) {
        // create content uri for the file
        val uri = parent.getContentUri(file)

        // create a intent for sending the file
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            setDataAndType(uri, "text/plain")
            putExtra(Intent.EXTRA_STREAM, uri)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            clipData = ClipData.newRawUri(null, uri)
        }

        // create a intent for directly viewing the file
        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "text/plain")
            putExtra(Intent.EXTRA_STREAM, uri)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            clipData = ClipData.newRawUri(null, uri)
        }

        // create a chooser intent for sharing, and add the view intent as a initial intent
        // this way, we effectively have a intent with two actions (ACTION_SEND and ACTION_VIEW)
        val chooserIntent = Intent.createChooser(shareIntent, "Logcat Dump").apply {
            putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(viewIntent))
        }

        // show the chooser
        parent.startActivity(chooserIntent)
    }
}
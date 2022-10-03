package io.github.shadow578.yodel.ui.more

import android.app.*
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.*
import com.mikepenz.aboutlibraries.LibsBuilder
import io.github.shadow578.yodel.*
import io.github.shadow578.yodel.backup.BackupHelper
import io.github.shadow578.yodel.db.model.TrackStatus
import io.github.shadow578.yodel.downloader.DownloaderService
import io.github.shadow578.yodel.downloader.TrackDownloadFormat
import io.github.shadow578.yodel.ui.base.BaseActivity
import io.github.shadow578.yodel.ui.dev.DeveloperToolsActivity
import io.github.shadow578.yodel.util.*
import io.github.shadow578.yodel.util.preferences.Prefs
import java.util.concurrent.atomic.AtomicBoolean

/**
 * view model for more fragment
 */
class MoreViewModel(application: Application) : AndroidViewModel(application) {
    /**
     * currently selected download format
     */
    val downloadFormat = MutableLiveData(Prefs.DownloadFormat.get())

    /**
     * binder for [Prefs.EnableMetadataTagging]
     */
    val enableMetadataTaggingBinder = SwitchPreferenceBinder(Prefs.EnableMetadataTagging)

    /**
     * currently selected locale override
     */
    val localeOverride = MutableLiveData(Prefs.AppLocaleOverride.get())

    /**
     * how often [countAndOpenDeveloperTools] was called (== how often the app_icon was clicked)
     */
    private var developerToolsCounter: Int = 0

    /**
     * count how many times this function was called.
     * if it was called more than 5 times, open the developer tools activity
     *
     * @param parent parent activity
     */
    fun countAndOpenDeveloperTools(parent: Activity) {
        developerToolsCounter++
        if (developerToolsCounter >= 5) {
            parent.startActivity(Intent(parent, DeveloperToolsActivity::class.java))
            developerToolsCounter = 0
        }
    }

    /**
     * open the about page
     *
     * @param activity parent activity
     */
    fun openAboutPage(activity: Activity) {
        val libs = LibsBuilder()
        libs.aboutAppName = activity.getString(R.string.app_name)
        libs.start(activity)
    }

    /**
     * choose the download directory
     *
     * @param activity parent activity
     */
    fun chooseDownloadsDir(activity: Activity) {
        if (activity is BaseActivity)
            activity.maybeSelectDownloadsDir(true)
    }

    /**
     * import tracks from a backup file
     *
     * @param file the file to import from
     */
    fun importTracks(file: DocumentFile, parent: Activity) {
        val application = getApplication<Application>()
        val backupHelper = BackupHelper(getApplication())
        launchIO {
            // read the backup data
            val backup = backupHelper.readBackup(file)
            if (backup == null) {
                launchMain {
                    application.toast(
                            R.string.restore_toast_failed,
                            Toast.LENGTH_SHORT
                    )
                }
                return@launchIO
            }

            // show confirmation dialog
            launchMain {
                val replaceExisting = AtomicBoolean(false)
                val tracksCount = backup.tracks.size
                AlertDialog.Builder(parent)
                        .setTitle(
                                application.getString(
                                        R.string.restore_dialog_title,
                                        tracksCount
                                )
                        )
                        .setSingleChoiceItems(
                                R.array.restore_dialog_modes,
                                0
                        ) { _, mode -> replaceExisting.set(mode == 1) }
                        .setNegativeButton(R.string.restore_dialog_negative) { dialog, _ -> dialog.dismiss() }
                        .setPositiveButton(R.string.restore_dialog_positive) { _, _ ->
                            // restore the backup
                            application.toast(
                                    getApplication<Application>().getString(
                                            R.string.restore_toast_success,
                                            tracksCount
                                    ),
                                    Toast.LENGTH_SHORT
                            )

                            launchIO {
                                // restore, transform all restored to pending state
                                backupHelper.restoreBackup(
                                        backup,
                                        replaceExisting.get()
                                ) { status = TrackStatus.DownloadPending }

                                // start service on demand
                                DownloaderService.startOnDemand(application)
                            }
                        }
                        .show()
            }
        }
    }

    /**
     * export tracks to a backup file
     *
     * @param file the file to export to
     */
    fun exportTracks(file: DocumentFile) {
        launchIO {
            val success = BackupHelper(getApplication()).createBackup(file)
            if (!success) {
                launchMain {
                    getApplication<Application>().toast(
                            R.string.backup_toast_failed,
                            Toast.LENGTH_SHORT
                    )
                }
            }
        }
    }

    /**
     * set the download format
     *
     * @param format new download format
     */
    fun setDownloadFormat(format: TrackDownloadFormat) {
        // ignore if no change
        if (format == downloadFormat.value) {
            return
        }
        Prefs.DownloadFormat.set(format)
        downloadFormat.value = format
    }

    /**
     * set the currently selected locale override
     *
     * @param localeOverride the currently selected locale override
     * @return  was the locale changed?
     */
    fun setLocaleOverride(localeOverride: LocaleOverride): Boolean {
        if (localeOverride == this.localeOverride.value)
            return false

        Prefs.AppLocaleOverride.set(localeOverride)
        this.localeOverride.value = localeOverride
        return true
    }
}
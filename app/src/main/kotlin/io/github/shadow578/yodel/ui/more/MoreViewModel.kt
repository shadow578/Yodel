package io.github.shadow578.yodel.ui.more

import android.app.Activity
import android.app.Application
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.mikepenz.aboutlibraries.LibsBuilder
import io.github.shadow578.music_dl.R
import io.github.shadow578.music_dl.util.Async
import io.github.shadow578.yodel.LocaleOverride
import io.github.shadow578.yodel.backup.BackupHelper
import io.github.shadow578.yodel.downloader.TrackDownloadFormat
import io.github.shadow578.yodel.ui.base.BaseActivity
import io.github.shadow578.yodel.util.launchIO
import io.github.shadow578.yodel.util.launchMain
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
     * current state of ssl_fix enable
     */
    val enableSSLFix = MutableLiveData(Prefs.EnableSSLFix.get())

    /**
     * current state of metadata tagging enable
     */
    val enableTagging = MutableLiveData(Prefs.EnableMetadataTagging.get())

    /**
     * currently selected locale override
     */
    val localeOverride = MutableLiveData(Prefs.AppLocaleOverride.get())

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
        launchIO {
            // read the backup data
            val backup = BackupHelper.readBackupData(getApplication(), file)
            if (!backup.isPresent) {
                Async.runOnMain {
                    Toast.makeText(
                        getApplication(),
                        R.string.restore_toast_failed,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return@launchIO
            }

            // show confirmation dialog
            launchMain {
                val replaceExisting = AtomicBoolean(false)
                val tracksCount = backup.get().tracks.size
                AlertDialog.Builder(parent)
                    .setTitle(
                        getApplication<Application>().getString(
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
                        Toast.makeText(
                            getApplication(),
                            getApplication<Application>().getString(
                                R.string.restore_toast_success,
                                tracksCount
                            ),
                            Toast.LENGTH_SHORT
                        ).show()

                        launchIO {
                            BackupHelper.restoreBackup(
                                getApplication(),
                                backup.get(),
                                replaceExisting.get()
                            )
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
            val success =
                BackupHelper.createBackup(
                    getApplication(),
                    file
                )
            if (!success) {
                launchMain {
                    Toast.makeText(
                        getApplication(),
                        R.string.backup_toast_failed,
                        Toast.LENGTH_SHORT
                    ).show()
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
     * enable or disable metadata tagging
     *
     * @param enable is tagging enabled?
     */
    fun setEnableTagging(enable: Boolean) {
        if (enable == enableTagging.value) {
            return
        }
        Prefs.EnableMetadataTagging.set(enable)
        enableTagging.value = enable
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
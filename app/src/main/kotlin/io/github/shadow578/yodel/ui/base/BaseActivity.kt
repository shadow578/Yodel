package io.github.shadow578.yodel.ui.base

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.OpenDocumentTree
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import io.github.shadow578.yodel.R
import io.github.shadow578.yodel.util.preferences.Prefs
import io.github.shadow578.yodel.util.storage.*
import io.github.shadow578.yodel.util.toast
import io.github.shadow578.yodel.util.wrapLocale
import timber.log.Timber

/**
 * topmost base activity. this is to be extended when creating a new activity.
 * handles app- specific stuff
 */
open class BaseActivity : AppCompatActivity() {
    /**
     * result launcher for download directory select
     */
    private lateinit var downloadDirectorySelectLauncher: ActivityResultLauncher<Uri?>

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase.wrapLocale())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // create result launcher for download directory select
        downloadDirectorySelectLauncher =
            registerForActivityResult(OpenDocumentTree()) { treeUri: Uri? ->
                if (treeUri != null) {
                    val treeFile = DocumentFile.fromTreeUri(this, treeUri)

                    // check access
                    if (treeFile != null && treeFile.exists()
                        && treeFile.canRead()
                        && treeFile.canWrite()
                    ) {
                        // persist the permission & save
                        val treeKey = treeUri.persistFilePermission(applicationContext)
                        Prefs.DownloadsDirectory.set(treeKey)
                        Timber.i("selected and saved new track downloads directory: $treeUri")
                    } else {
                        // bad selection
                        this.toast(R.string.base_toast_set_download_directory_fail)
                        maybeSelectDownloadsDir(true)
                    }
                }
            }
    }

    /**
     * prompt the user to select the downloads dir, if not set
     *
     * @param force force select a new directory?
     */
    fun maybeSelectDownloadsDir(force: Boolean) {
        // check if downloads dir is set and accessible
        val downloadsKey = Prefs.DownloadsDirectory.get()
        if (downloadsKey != StorageKey.EMPTY && !force) {
            val downloadsDir = downloadsKey.getPersistedFilePermission(this, true)
            if (downloadsDir != null
                && downloadsDir.exists()
                && downloadsDir.canWrite()
            ) {
                // download directory exists and can write, all OK!
                return
            }
        }

        // select directory
        this.toast(R.string.base_toast_select_download_directory)
        downloadDirectorySelectLauncher.launch(null)
    }
}
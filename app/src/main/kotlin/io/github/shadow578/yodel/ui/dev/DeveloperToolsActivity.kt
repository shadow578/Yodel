package io.github.shadow578.yodel.ui.dev

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import io.github.shadow578.yodel.databinding.ActivityDeveloperToolsBinding
import io.github.shadow578.yodel.ui.base.BaseActivity
import io.github.shadow578.yodel.util.copyToClipboard

/**
 * developer tools activity
 */
class DeveloperToolsActivity : BaseActivity() {

    /**
     * the view model instance
     */
    private lateinit var model: DeveloperToolsViewModel

    /**
     * the view binding instance
     */
    private lateinit var b: ActivityDeveloperToolsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityDeveloperToolsBinding.inflate(layoutInflater)
        setContentView(b.root)

        // create model
        model = ViewModelProvider(this).get(DeveloperToolsViewModel::class.java)

        // setup debug info
        b.debugInfo.text = model.debugInfo
        b.debugInfo.setOnLongClickListener {
            // copy to clipboard on long press
            copyToClipboard("Debug Info", model.debugInfo)
            true
        }

        // setup logcat dump
        b.dumpLogcat.setOnClickListener {
            model.dumpLogcat(this)
        }

        // listen to error notifications
        b.downloaderErrorNotifications.setOnCheckedChangeListener { _, isChecked ->
            model.setEnableDownloaderErrorNotifications(isChecked)
        }

        model.enableDownloaderErrorNotifications.observe(this,
            { sslFix: Boolean ->
                b.downloaderErrorNotifications.isChecked = sslFix
            })
    }
}
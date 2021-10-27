package io.github.shadow578.yodel.ui.dev

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.switchmaterial.SwitchMaterial
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

        // setup reload all
        b.reloadAllTracks.setOnClickListener {
            model.reloadAllTracks()
        }

        // init and bind switches for dev flags
        model.bindAllFlags { flag, binder ->
            // create the material switch
            val switch = SwitchMaterial(this).apply {
                layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )

                // suppressing this warning since this is in devtools and we don't care about hardcoded strings here
                @SuppressLint("SetTextI18n")
                text = "${flag.displayName} \r\n#${flag.key}"
            }

            // bind switch to preference
            binder.bind(this, switch)

            // add the switch to the container
            b.flagsContainer.addView(switch)
        }
    }
}
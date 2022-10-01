package io.github.shadow578.yodel.ui.more

import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModelProvider
import io.github.shadow578.yodel.*
import io.github.shadow578.yodel.databinding.FragmentMoreBinding
import io.github.shadow578.yodel.downloader.TrackDownloadFormat
import io.github.shadow578.yodel.ui.base.BaseFragment
import io.github.shadow578.yodel.util.toast
import java.util.stream.Collectors

/**
 * more / about fragment
 */
class MoreFragment : BaseFragment() {
    /**
     * view binding instance
     */
    private lateinit var b: FragmentMoreBinding

    /**
     * view model instance
     */
    private lateinit var model: MoreViewModel

    /**
     * launcher for export file choose action
     */
    private lateinit var chooseExportFileLauncher: ActivityResultLauncher<String>

    /**
     * launcher for import file choose action
     */
    private lateinit var chooseImportFileLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        b = FragmentMoreBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chooseExportFileLauncher = registerForActivityResult(CreateDocument("application/json")) { uri: Uri? ->
            if (uri == null) {
                return@registerForActivityResult
            }

            val file = DocumentFile.fromSingleUri(requireContext(), uri)
            if (file != null && file.canWrite()) {
                model.exportTracks(file)
                requireContext().toast(
                        R.string.backup_toast_starting,
                        Toast.LENGTH_SHORT
                )
            } else {
                requireContext().toast(
                        R.string.backup_toast_failed,
                        Toast.LENGTH_SHORT
                )
            }
        }

        chooseImportFileLauncher = registerForActivityResult(
                OpenDocument()
        ) { uri: Uri? ->
            if (uri == null) {
                return@registerForActivityResult
            }
            val file = DocumentFile.fromSingleUri(requireContext(), uri)
            if (file != null && file.canRead()) {
                model.importTracks(file, requireActivity())
                requireContext().toast(
                        R.string.restore_toast_starting,
                        Toast.LENGTH_SHORT
                )
            } else {
                requireContext().toast(
                        R.string.restore_toast_failed,
                        Toast.LENGTH_SHORT
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        model = ViewModelProvider(requireActivity()).get(
                MoreViewModel::class.java
        )

        // clicking the app icon multiple times opens developer options screen
        b.appIcon.setOnClickListener {
            model.countAndOpenDeveloperTools(requireActivity())
        }

        // show app version
        b.appVersion.text = BuildConfig.VERSION_NAME

        // about button
        b.about.setOnClickListener { model.openAboutPage(requireActivity()) }

        // select downloads dir
        b.selectDownloadsDir.setOnClickListener { model.chooseDownloadsDir(requireActivity()) }

        // populate language selection
        setupLanguageSelection()

        // populate download formats
        setupFormatSelection()

        // bind metadata toggle
        model.enableMetadataTaggingBinder.bind(this, b.enableTagging)

        // backup / restore buttons
        b.restoreTracks.setOnClickListener {
            chooseImportFileLauncher.launch(arrayOf("application/json"))
        }
        b.backupTracks.setOnClickListener { chooseExportFileLauncher.launch("tracks_export.json") }
    }

    /**
     * setup the download format selector
     */
    private fun setupFormatSelection() {
        // create a list of the formats and a list of display names
        // both lists are in the same order
        val ctx = requireContext()
        val formatValues = listOf(*TrackDownloadFormat.values())
        val formatDisplayNames = formatValues.stream()
                .map { ctx.getString(it.displayName) }
                .collect(Collectors.toList())

        // set values to display
        val adapter =
                ArrayAdapter(ctx, android.R.layout.simple_dropdown_item_1line, formatDisplayNames)
        b.downloadsFormat.setAdapter(adapter)

        // set change listener
        b.downloadsFormat.setOnItemClickListener { _, _, position, _ ->
            model.setDownloadFormat(formatValues[position])
        }

        // sync with model
        model.downloadFormat.observe(
                requireActivity(),
                {
                    val i = formatValues.indexOf(it)
                    b.downloadsFormat.setText(formatDisplayNames[i], false)
                })

        // always show all items
        b.downloadsFormat.setOnClickListener {
            adapter.filter.filter(null)
            b.downloadsFormat.showDropDown()
        }
    }

    /**
     * setup the language override selector
     */
    private fun setupLanguageSelection() {
        // create a list of the locale overrides and a list of display names
        // both lists are in the same order
        val ctx = requireContext()
        val localeValues = listOf(*LocaleOverride.values())
        val localeDisplayNames = localeValues.stream()
                .map { it.getDisplayName(ctx) }
                .collect(Collectors.toList())

        // set values to display
        val adapter =
                ArrayAdapter(ctx, android.R.layout.simple_dropdown_item_1line, localeDisplayNames)
        b.languageOverride.setAdapter(adapter)

        // set change listener
        b.languageOverride.setOnItemClickListener { _, _, position, _ ->
            val changed = model.setLocaleOverride(localeValues[position])
            if (changed) {
                requireActivity().recreate()
            }
        }

        // sync with model
        model.localeOverride.observe(requireActivity(), { localeOverride: LocaleOverride ->
            val i = localeValues.indexOf(localeOverride)
            b.languageOverride.setText(localeDisplayNames[i], false)
        })

        // always show all items
        b.languageOverride.setOnClickListener {
            adapter.filter.filter(null)
            b.languageOverride.showDropDown()
        }
    }
}
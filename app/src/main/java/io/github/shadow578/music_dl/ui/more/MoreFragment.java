package io.github.shadow578.music_dl.ui.more;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;
import androidx.lifecycle.ViewModelProvider;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.github.shadow578.music_dl.KtPorted;
import io.github.shadow578.music_dl.LocaleOverride;
import io.github.shadow578.music_dl.R;
import io.github.shadow578.music_dl.databinding.FragmentMoreBinding;
import io.github.shadow578.music_dl.downloader.TrackDownloadFormat;
import io.github.shadow578.music_dl.ui.base.BaseFragment;

/**
 * more / about fragment
 */
@KtPorted
public class MoreFragment extends BaseFragment {

    /**
     * view binding instance
     */
    @SuppressWarnings("FieldCanBeLocal")
    private FragmentMoreBinding b;

    /**
     * view model instance
     */
    private MoreViewModel model;

    /**
     * launcher for export file choose action
     */
    private ActivityResultLauncher<String> chooseExportFileLauncher;

    /**
     * launcher for import file choose action
     */
    private ActivityResultLauncher<String[]> chooseImportFileLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        b = FragmentMoreBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chooseExportFileLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument(),
                uri -> {
                    if (uri == null) {
                        return;
                    }

                    final DocumentFile file = DocumentFile.fromSingleUri(requireContext(), uri);
                    if (file != null && file.canWrite()) {
                        model.exportTracks(file);
                        Toast.makeText(requireContext(), R.string.backup_toast_starting, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), R.string.backup_toast_failed, Toast.LENGTH_SHORT).show();
                    }
                });
        chooseImportFileLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri == null) {
                        return;
                    }

                    final DocumentFile file = DocumentFile.fromSingleUri(requireContext(), uri);
                    if (file != null && file.canRead()) {
                        model.importTracks(file, requireActivity());
                        Toast.makeText(requireContext(), R.string.restore_toast_starting, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), R.string.restore_toast_failed, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        model = new ViewModelProvider(requireActivity()).get(MoreViewModel.class);

        // about button
        b.about.setOnClickListener(v -> model.openAboutPage(requireActivity()));

        // select downloads dir
        b.selectDownloadsDir.setOnClickListener(v -> model.chooseDownloadsDir(requireActivity()));

        // populate language selection
        setupLanguageSelection();

        // populate download formats
        setupFormatSelection();

        // listen to ssl fix
        b.enableSslFix.setOnCheckedChangeListener((buttonView, isChecked) -> model.setEnableSSLFix(isChecked));
        model.getEnableSSLFix().observe(requireActivity(), sslFix -> b.enableSslFix.setChecked(sslFix));

        // listen to write metadata
        b.enableTagging.setOnCheckedChangeListener((buttonView, isChecked) -> model.setEnableTagging(isChecked));
        model.getEnableTagging().observe(requireActivity(), enableTagging -> b.enableTagging.setChecked(enableTagging));

        // backup / restore buttons
        b.restoreTracks.setOnClickListener(v
                -> chooseImportFileLauncher.launch(new String[]{"application/json"}));
        b.backupTracks.setOnClickListener(v
                -> chooseExportFileLauncher.launch("tracks_export.json"));
    }

    /**
     * setup the download format selector
     */
    private void setupFormatSelection() {
        // create a list of the formats and a list of display names
        // both lists are in the same order
        final Context ctx = requireContext();
        final List<TrackDownloadFormat> formatValues = Arrays.asList(TrackDownloadFormat.values());
        final List<String> formatDisplayNames = formatValues.stream()
                .map(format -> ctx.getString(format.displayNameRes()))
                .collect(Collectors.toList());

        // set values to display
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(ctx, android.R.layout.simple_dropdown_item_1line, formatDisplayNames);
        b.downloadsFormat.setAdapter(adapter);

        // set change listener
        b.downloadsFormat.setOnItemClickListener((parent, view, position, id)
                -> model.setDownloadFormat(formatValues.get(position)));

        // sync with model
        model.getDownloadFormat().observe(requireActivity(), trackDownloadFormat
                -> {
            final int i = formatValues.indexOf(trackDownloadFormat);
            b.downloadsFormat.setText(formatDisplayNames.get(i), false);
        });

        // always show all items
        b.downloadsFormat.setOnClickListener(v -> {
            adapter.getFilter().filter(null);
            b.downloadsFormat.showDropDown();
        });
    }

    /**
     * setup the language override selector
     */
    private void setupLanguageSelection() {
        // create a list of the locale overrides and a list of display names
        // both lists are in the same order
        final Context ctx = requireContext();
        final List<LocaleOverride> localeValues = Arrays.asList(LocaleOverride.values());
        final List<String> localeDisplayNames = localeValues.stream()
                .map(locale -> locale.displayName(ctx))
                .collect(Collectors.toList());

        // set values to display
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(ctx, android.R.layout.simple_dropdown_item_1line, localeDisplayNames);
        b.languageOverride.setAdapter(adapter);

        // set change listener
        b.languageOverride.setOnItemClickListener((parent, view, position, id) -> {
            final boolean changed = model.setLocaleOverride(localeValues.get(position));
            if (changed) {
                requireActivity().recreate();
            }
        });

        // sync with model
        model.getLocaleOverride().observe(requireActivity(), localeOverride -> {
            final int i = localeValues.indexOf(localeOverride);
            b.languageOverride.setText(localeDisplayNames.get(i), false);
        });

        // always show all items
        b.languageOverride.setOnClickListener(v -> {
            adapter.getFilter().filter(null);
            b.languageOverride.showDropDown();
        });
    }
}
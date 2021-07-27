package io.github.shadow578.music_dl.ui.more;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.github.shadow578.music_dl.databinding.FragmentMoreBinding;
import io.github.shadow578.music_dl.downloader.TrackDownloadFormat;
import io.github.shadow578.music_dl.ui.BaseFragment;

/**
 * more / about fragment
 */
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        b = FragmentMoreBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        model = new ViewModelProvider(requireActivity()).get(MoreViewModel.class);

        // about button
        b.about.setOnClickListener(v -> model.openAboutPage(requireActivity()));

        // select downloads dir
        b.selectDownloadsDir.setOnClickListener(v -> model.chooseDownloadsDir(requireActivity()));

        // populate download formats
        setupFormatSelection();

        // listen to ssl fix
        b.enableSslFix.setOnCheckedChangeListener((buttonView, isChecked) -> model.setEnableSSLFix(isChecked));
        model.getEnableSSLFix().observe(requireActivity(), sslFix -> b.enableSslFix.setChecked(sslFix));

        // listen to write metadata
        b.enableTagging.setOnCheckedChangeListener((buttonView, isChecked) -> model.setEnableTagging(isChecked));
        model.getEnableTagging().observe(requireActivity(), enableTagging -> b.enableTagging.setChecked(enableTagging));
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
        b.downloadsFormat.setAdapter(new ArrayAdapter<>(ctx, android.R.layout.simple_dropdown_item_1line, formatDisplayNames));

        // set change listener
        b.downloadsFormat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                model.setDownloadFormat(formatValues.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // sync with model
        model.getDownloadFormat().observe(requireActivity(), trackDownloadFormat
                -> b.downloadsFormat.setSelection(formatValues.indexOf(trackDownloadFormat)));
    }
}
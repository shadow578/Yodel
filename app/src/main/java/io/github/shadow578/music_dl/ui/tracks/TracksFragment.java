package io.github.shadow578.music_dl.ui.tracks;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.Optional;

import io.github.shadow578.music_dl.R;
import io.github.shadow578.music_dl.databinding.FragmentTracksBinding;
import io.github.shadow578.music_dl.db.model.TrackInfo;
import io.github.shadow578.music_dl.ui.BaseFragment;
import io.github.shadow578.music_dl.util.storage.StorageHelper;

/**
 * downloaded and downloading tracks UI
 */
public class TracksFragment extends BaseFragment {

    /**
     * the view binding instance
     */
    private TracksViewModel model;

    /**
     * the view model instance
     */
    private FragmentTracksBinding b;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        b = FragmentTracksBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        model = new ViewModelProvider(this).get(TracksViewModel.class);

        // setup recycler with data from model
        b.tracksRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        b.tracksRecycler.setAdapter(new TracksAdapter(requireActivity(), model.getTracks(), this::playTrack));

        // show empty label if no tracks available
        model.getTracks().observe(requireActivity(), tracks
                -> b.emptyLabel.setVisibility(tracks.size() > 0 ? View.GONE : View.VISIBLE));
    }

    /**
     * play a track
     *
     * @param track the track to play
     */
    private void playTrack(@NonNull TrackInfo track) {
        // decode track audio file key
        final Optional<Uri> trackUri = StorageHelper.decodeUri(track.audioFileKey);
        if (!trackUri.isPresent()) {
            Toast.makeText(requireContext(), R.string.tracks_play_failed, Toast.LENGTH_SHORT).show();
            return;
        }

        // start external player
        final Intent playIntent = new Intent(Intent.ACTION_VIEW)
                .setDataAndType(trackUri.get(), "audio/*")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP
                        | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(playIntent);
    }
}
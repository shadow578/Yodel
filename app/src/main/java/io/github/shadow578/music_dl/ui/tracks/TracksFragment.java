package io.github.shadow578.music_dl.ui.tracks;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import io.github.shadow578.music_dl.R;
import io.github.shadow578.music_dl.databinding.FragmentTracksBinding;
import io.github.shadow578.music_dl.db.model.TrackInfo;

/**
 * downloaded and downloading tracks UI
 */
public class TracksFragment extends Fragment {

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
    }

    /**
     * play a track
     *
     * @param track the track to play
     */
    private void playTrack(@NonNull TrackInfo track) {
        Toast.makeText(requireContext(), "track play: " + track.title, Toast.LENGTH_LONG).show();
    }
}
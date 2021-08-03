package io.github.shadow578.music_dl.ui.tracks;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Optional;

import io.github.shadow578.music_dl.KtPorted;
import io.github.shadow578.music_dl.R;
import io.github.shadow578.music_dl.databinding.FragmentTracksBinding;
import io.github.shadow578.music_dl.db.TracksDB;
import io.github.shadow578.music_dl.db.model.TrackInfo;
import io.github.shadow578.music_dl.db.model.TrackStatus;
import io.github.shadow578.music_dl.ui.base.BaseFragment;
import io.github.shadow578.music_dl.util.Async;
import io.github.shadow578.music_dl.util.SwipeToDeleteCallback;
import io.github.shadow578.music_dl.util.storage.StorageHelper;

/**
 * downloaded and downloading tracks UI
 */
@KtPorted
public class TracksFragment extends BaseFragment {

    /**
     * the view binding instance
     */
    @SuppressWarnings("FieldCanBeLocal")
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
        final TracksAdapter tracksAdapter = new TracksAdapter(requireActivity(), model.getTracks(), this::playTrack, this::reDownloadTrack);
        b.tracksRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        // show empty label if no tracks available
        model.getTracks().observe(requireActivity(), tracks
                -> b.emptyLabel.setVisibility(tracks.size() > 0 ? View.GONE : View.VISIBLE));

        // setup swipe to delete
        final ItemTouchHelper swipeToDelete = new ItemTouchHelper(new SwipeToDeleteCallback(requireContext(),
                resolveColor(R.attr.colorError),
                R.drawable.ic_round_close_24,
                resolveColor(R.attr.colorOnError),
                15) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if (!(viewHolder instanceof TracksAdapter.Holder)) {
                    throw new IllegalStateException("got a wrong typed view holder");
                }

                tracksAdapter.deleteLater((TracksAdapter.Holder) viewHolder, track
                        -> Async.runAsync(() -> TracksDB.getInstance().tracks().remove(track)));
            }
        });
        swipeToDelete.attachToRecyclerView(b.tracksRecycler);


        b.tracksRecycler.setAdapter(tracksAdapter);
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

    /**
     * re- download a track
     *
     * @param track the track to re- download
     */
    private void reDownloadTrack(@NonNull TrackInfo track) {
        // reset status to pending
        track.status = TrackStatus.DownloadPending;

        // overwrite entry in db
        Async.runAsync(() -> TracksDB.init(requireContext()).tracks().insert(track));
    }

    /**
     * resolve a color from a attribute
     *
     * @param attr the color attribute to resolve
     * @return the resolved color int
     */
    @ColorInt
    private int resolveColor(@AttrRes int attr) {
        final TypedValue value = new TypedValue();
        requireContext().getTheme().resolveAttribute(attr, value, true);
        return value.data;

    }
}
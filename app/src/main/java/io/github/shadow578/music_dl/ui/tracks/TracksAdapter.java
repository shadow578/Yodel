package io.github.shadow578.music_dl.ui.tracks;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.github.shadow578.music_dl.databinding.RecyclerTrackViewBinding;
import io.github.shadow578.music_dl.db.model.TrackInfo;

/**
 * recyclerview adapter for tracks livedata
 */
public class TracksAdapter extends RecyclerView.Adapter<TracksAdapter.Holder> {

    @NonNull
    private List<TrackInfo> tracks = new ArrayList<>();

    @NonNull
    private final ItemClickListener clickListener;

    public TracksAdapter(@NonNull LifecycleOwner owner, @NonNull LiveData<List<TrackInfo>> tracks, @NonNull ItemClickListener clickListener) {
        this.clickListener = clickListener;
        tracks.observe(owner, trackInfos -> {
            if (trackInfos == null)
                return;

            this.tracks = trackInfos;
            notifyDataSetChanged();
        });
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(RecyclerTrackViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        final TrackInfo track = tracks.get(position);

        // set title
        holder.b.title.setText(track.title);

        // set status
        holder.b.status.setText(track.status.key());

        // set click listener
        holder.b.getRoot().setOnClickListener(v -> clickListener.onClick(track));
    }

    @Override
    public int getItemCount() {
        return tracks.size();
    }

    /**
     * a view holder for the items of this adapter
     */
    public static class Holder extends RecyclerView.ViewHolder {
        /**
         * view binding of the view this holder holds
         */
        public final RecyclerTrackViewBinding b;

        public Holder(@NonNull RecyclerTrackViewBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }

    /**
     * a click listener for track items
     */
    @FunctionalInterface
    public interface ItemClickListener {
        /**
         * called when a track view is selected
         *
         * @param track the track the view shows
         */
        void onClick(@NonNull TrackInfo track);
    }
}

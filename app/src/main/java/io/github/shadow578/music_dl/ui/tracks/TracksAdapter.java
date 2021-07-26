package io.github.shadow578.music_dl.ui.tracks;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.github.shadow578.music_dl.R;
import io.github.shadow578.music_dl.databinding.RecyclerTrackViewBinding;
import io.github.shadow578.music_dl.db.model.TrackInfo;
import io.github.shadow578.music_dl.util.Util;
import io.github.shadow578.music_dl.util.storage.StorageHelper;

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

        // cover
        final Optional<Uri> coverUri = StorageHelper.decodeUri(track.coverKey);
        coverUri.ifPresent(cover -> Glide.with(holder.b.coverArt)
                .load(cover)
                .fallback(R.drawable.ic_round_tpose_24)
                .into(holder.b.coverArt));

        // title
        holder.b.title.setText(track.title);

        // build and set artist + album
        final String albumAndArtist;
        if (track.artist != null && track.albumName != null) {
            albumAndArtist = holder.b.getRoot().getContext().getString(R.string.tracks_artist_and_album,
                    track.artist, track.albumName);
        } else if (track.artist != null) {
            albumAndArtist = track.artist;
        } else if (track.albumName != null) {
            albumAndArtist = track.albumName;
        } else {
            albumAndArtist = "";
        }

        holder.b.albumAndArtist.setText(albumAndArtist);

        // duration
        if (track.duration != null) {
            holder.b.duration.setText(Util.secondsToTimeString(track.duration));
            holder.b.duration.setVisibility(View.VISIBLE);
        } else {
            holder.b.duration.setVisibility(View.GONE);
        }

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

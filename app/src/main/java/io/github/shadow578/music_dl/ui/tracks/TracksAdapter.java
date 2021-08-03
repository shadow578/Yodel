package io.github.shadow578.music_dl.ui.tracks;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import io.github.shadow578.music_dl.KtPorted;
import io.github.shadow578.music_dl.R;
import io.github.shadow578.music_dl.databinding.RecyclerTrackViewBinding;
import io.github.shadow578.music_dl.db.model.TrackInfo;
import io.github.shadow578.music_dl.db.model.TrackStatus;
import io.github.shadow578.music_dl.util.Async;
import io.github.shadow578.music_dl.util.Util;
import io.github.shadow578.music_dl.util.storage.StorageHelper;

/**
 * recyclerview adapter for tracks livedata
 */
@KtPorted
public class TracksAdapter extends RecyclerView.Adapter<TracksAdapter.Holder> {

    @NonNull
    private List<TrackInfo> tracks = new ArrayList<>();

    @NonNull
    private final ItemListener clickListener;

    @NonNull
    private final ItemListener reDownloadListener;

    /**
     * items that should be removed later.
     * key is item position, value if remove was aborted
     */
    @NonNull
    private final HashMap<Integer, Boolean> itemsToDelete = new HashMap<>();

    public TracksAdapter(@NonNull LifecycleOwner owner, @NonNull LiveData<List<TrackInfo>> tracks,
                         @NonNull ItemListener clickListener,
                         @NonNull ItemListener reDownloadListener) {
        this.clickListener = clickListener;
        this.reDownloadListener = reDownloadListener;
        tracks.observe(owner, trackInfoList -> {
            if (trackInfoList == null)
                return;

            this.tracks = trackInfoList;
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
        if (coverUri.isPresent()) {
            // load cover from fs using glide
            Glide.with(holder.b.coverArt)
                    .load(coverUri.get())
                    .placeholder(R.drawable.ic_splash_foreground)
                    .fallback(R.drawable.ic_splash_foreground)
                    .into(holder.b.coverArt);
        } else {
            // load fallback image
            Glide.with(holder.b.coverArt)
                    .load(R.drawable.ic_splash_foreground)
                    .into(holder.b.coverArt);
        }

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

        // status icon
        @DrawableRes final int statusDrawable;
        switch (track.status) {
            case DownloadPending:
                statusDrawable = R.drawable.ic_round_timer_24;
                break;
            case Downloading:
                statusDrawable = R.drawable.ic_downloading_black_24dp;
                break;
            case Downloaded:
                statusDrawable = R.drawable.ic_round_check_circle_outline_24;
                break;
            case DownloadFailed:
                statusDrawable = R.drawable.ic_round_error_outline_24;
                break;
            case FileDeleted:
                statusDrawable = R.drawable.ic_round_remove_circle_outline_24;
                break;
            default:
                throw new IllegalArgumentException("invalid track status: " + track.status);
        }
        holder.b.statusIcon.setImageResource(statusDrawable);

        // retry download button
        final boolean canRetry = track.status.equals(TrackStatus.DownloadFailed) || track.status.equals(TrackStatus.FileDeleted);
        holder.b.retryDownloadContainer.setVisibility(canRetry ? View.VISIBLE : View.GONE);
        holder.b.retryDownloadContainer.setOnClickListener(v -> reDownloadListener.onClick(track));

        // hide on- cover views if retry is shown
        holder.b.statusIcon.setVisibility(canRetry ? View.GONE : View.VISIBLE);
        holder.b.duration.setVisibility(canRetry ? View.GONE : View.VISIBLE);

        // duration
        if (track.duration != null) {
            holder.b.duration.setText(Util.secondsToTimeString(track.duration));
            holder.b.duration.setVisibility(View.VISIBLE);
        } else {
            holder.b.duration.setVisibility(View.GONE);
        }

        // set click listener
        holder.b.getRoot().setOnClickListener(v -> clickListener.onClick(track));


        // deleted mode:
        // setup delete listener
        holder.b.undo.setOnClickListener(v -> {
            itemsToDelete.put(position, false);
            notifyItemChanged(position);
        });

        // make view go into delete mode
        final Boolean isToDelete = itemsToDelete.get(position);
        holder.setDeletedMode(isToDelete != null && isToDelete);
    }

    /**
     * show a undo button for a while, then remove the item
     *
     * @param item           the item to remove
     * @param deleteCallback callback to actually delete the item. called on main thread
     */
    public void deleteLater(@NonNull Holder item, @NonNull ItemListener deleteCallback) {
        final int position = item.getAdapterPosition();
        final TrackInfo track = tracks.get(position);

        // mark as to delete
        itemsToDelete.put(position, true);

        // delete after a delay
        Async.runLaterOnMain(() -> {
            if (Boolean.FALSE.equals(itemsToDelete.get(position))) {
                return;
            }

            // remove from map
            itemsToDelete.remove(position);

            // animate removal nicely
            notifyItemRemoved(position);

            // actually remove
            Async.runLaterOnMain(() -> deleteCallback.onClick(track), 100);
        }, 5000);

        // update to reflect new deleted state
        notifyItemChanged(position);
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

        /**
         * set if the 'deleted mode' view should be used
         *
         * @param deletedMode is this item in deleted mode?
         */
        public void setDeletedMode(boolean deletedMode) {
            b.containerMain.setVisibility(deletedMode ? View.INVISIBLE : View.VISIBLE);
            b.containerUndo.setVisibility(deletedMode ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * a click listener for track items
     */
    @FunctionalInterface
    public interface ItemListener {
        /**
         * called when a track view is selected
         *
         * @param track the track the view shows
         */
        void onClick(@NonNull TrackInfo track);
    }
}

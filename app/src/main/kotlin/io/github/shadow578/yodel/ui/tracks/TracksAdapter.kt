package io.github.shadow578.yodel.ui.tracks

import android.view.*
import androidx.annotation.DrawableRes
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import io.github.shadow578.yodel.R
import io.github.shadow578.yodel.databinding.RecyclerTrackViewBinding
import io.github.shadow578.yodel.db.model.*
import io.github.shadow578.yodel.util.*
import io.github.shadow578.yodel.util.storage.decodeToUri
import kotlinx.coroutines.delay
import java.util.*
import kotlin.collections.set

/**
 * recyclerview adapter for tracks livedata
 */
class TracksAdapter(
        owner: LifecycleOwner,
        tracks: LiveData<List<TrackInfo>>,
        private val clickListener: ItemListener,
        private val reDownloadListener: ItemListener
) : RecyclerView.Adapter<TracksAdapter.Holder?>() {
    init {
        tracks.observe(owner, { newTracks: List<TrackInfo> ->
            // calculate difference
            val difference = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize(): Int {
                    return currentTracks.size
                }

                override fun getNewListSize(): Int {
                    return newTracks.size
                }

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return currentTracks[oldItemPosition].id == newTracks[newItemPosition].id
                }

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return currentTracks[oldItemPosition].equalsContent(newTracks[newItemPosition])
                }
            })

            // update data and apply the changes
            currentTracks = newTracks
            difference.dispatchUpdatesTo(this)
        })
    }

    /**
     * current tracks list displayed
     */
    private var currentTracks: List<TrackInfo> = listOf()

    /**
     * items that should be removed later.
     * key is item position, value if remove was aborted
     */
    private val itemsToDelete = HashMap<Int, Boolean>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
                RecyclerTrackViewBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                )
        )
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val track = currentTracks[position]

        // cover
        val coverUri = track.coverKey.decodeToUri()
        if (coverUri != null) {
            // load cover from fs using glide
            Glide.with(holder.b.coverArt)
                    .load(coverUri)
                    .placeholder(R.drawable.ic_splash_foreground)
                    .fallback(R.drawable.ic_splash_foreground)
                    .into(holder.b.coverArt)
        } else {
            // load fallback image
            Glide.with(holder.b.coverArt)
                    .load(R.drawable.ic_splash_foreground)
                    .into(holder.b.coverArt)
        }

        // title
        holder.b.title.text = track.title

        // build and set artist + album
        val albumAndArtist: String? =
                if (track.artist != null && track.albumName != null) {
                    holder.b.root.context.getString(
                            R.string.tracks_artist_and_album,
                            track.artist,
                            track.albumName
                    )
                } else if (track.artist != null) {
                    track.artist
                } else if (track.albumName != null) {
                    track.albumName
                } else {
                    null
                }
        holder.b.albumAndArtist.text = albumAndArtist ?: ""

        // status icon
        @DrawableRes val statusDrawable: Int = when (track.status) {
            TrackStatus.DownloadPending -> R.drawable.ic_round_timer_24
            TrackStatus.Downloading -> R.drawable.ic_downloading_black_24dp
            TrackStatus.Downloaded -> R.drawable.ic_round_check_circle_outline_24
            TrackStatus.DownloadFailed -> R.drawable.ic_round_error_outline_24
            TrackStatus.FileDeleted -> R.drawable.ic_round_remove_circle_outline_24
        }
        holder.b.statusIcon.setImageResource(statusDrawable)

        // retry download button
        val canRetry = track.status == TrackStatus.DownloadFailed
                || track.status == TrackStatus.FileDeleted
        holder.b.retryDownloadContainer.visibility = if (canRetry) View.VISIBLE else View.GONE
        holder.b.retryDownloadContainer.setOnClickListener { reDownloadListener.onClick(track) }

        // hide on- cover views if retry is shown
        holder.b.statusIcon.visibility = if (canRetry) View.GONE else View.VISIBLE
        holder.b.duration.visibility = if (canRetry) View.GONE else View.VISIBLE

        // duration
        if (track.duration != null) {
            holder.b.duration.text = track.duration?.secondsToTimeString()
            holder.b.duration.visibility = View.VISIBLE
        } else {
            holder.b.duration.visibility = View.GONE
        }

        // set click listener
        holder.b.root.setOnClickListener { clickListener.onClick(track) }


        // deleted mode:
        // setup delete listener
        holder.b.undo.setOnClickListener {
            itemsToDelete[position] = false
            notifyItemChanged(position)
        }

        // make view go into delete mode
        val isToDelete = itemsToDelete[position]
        holder.setDeletedMode(isToDelete != null && isToDelete)
    }

    /**
     * show a undo button for a while, then remove the item
     *
     * @param item           the item to remove
     * @param deleteCallback callback to actually delete the item. called on main thread
     */
    fun deleteLater(item: Holder, deleteCallback: ItemListener) {
        val position = item.bindingAdapterPosition
        val track = currentTracks[position]

        // mark as to delete
        itemsToDelete[position] = true

        // delete after a delay
        launchMain {
            delay(5000)
            if (itemsToDelete[position] == false)
                return@launchMain

            // remove from map
            itemsToDelete.remove(position)

            // animate removal nicely
            notifyItemRemoved(position)

            // actually remove after short delay
            delay(100)
            deleteCallback.onClick(track)
        }

        // update to reflect new deleted state
        notifyItemChanged(position)
    }

    override fun getItemCount(): Int {
        return currentTracks.size
    }

    /**
     * a view holder for the items of this adapter
     */
    class Holder(
            /**
             * view binding of the view this holder holds
             */
            val b: RecyclerTrackViewBinding
    ) : RecyclerView.ViewHolder(b.root) {

        /**
         * set if the 'deleted mode' view should be used
         *
         * @param deletedMode is this item in deleted mode?
         */
        fun setDeletedMode(deletedMode: Boolean) {
            b.containerMain.visibility = if (deletedMode) View.INVISIBLE else View.VISIBLE
            b.containerUndo.visibility = if (deletedMode) View.VISIBLE else View.GONE
        }

    }

    /**
     * a click listener for track items
     */
    fun interface ItemListener {
        /**
         * called when a track view is selected
         *
         * @param track the track the view shows
         */
        fun onClick(track: TrackInfo)
    }
}
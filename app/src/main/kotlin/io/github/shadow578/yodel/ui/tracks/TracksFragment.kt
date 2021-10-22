package io.github.shadow578.yodel.ui.tracks

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.widget.Toast
import androidx.annotation.*
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.*
import io.github.shadow578.yodel.R
import io.github.shadow578.yodel.databinding.FragmentTracksBinding
import io.github.shadow578.yodel.db.TracksDB
import io.github.shadow578.yodel.db.model.*
import io.github.shadow578.yodel.ui.base.BaseFragment
import io.github.shadow578.yodel.util.*
import io.github.shadow578.yodel.util.storage.decodeToFile
import io.github.shadow578.yodel.util.storage.decodeToUri

/**
 * downloaded and downloading tracks UI
 */
class TracksFragment : BaseFragment() {
    /**
     * the view binding instance
     */
    private lateinit var model: TracksViewModel

    /**
     * the view model instance
     */
    private lateinit var b: FragmentTracksBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        b = FragmentTracksBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        model = ViewModelProvider(this).get(TracksViewModel::class.java)

        // setup recycler with data from model
        val tracksAdapter = TracksAdapter(requireActivity(), model.tracks,
            { track: TrackInfo ->
                playTrack(
                    track
                )
            },
            { track: TrackInfo ->
                reDownloadTrack(
                    track
                )
            })
        b.tracksRecycler.layoutManager = LinearLayoutManager(requireContext())

        // show empty label if no tracks available
        model.tracks.observe(requireActivity(),
            { tracks: List<TrackInfo?> ->
                b.emptyLabel.visibility = if (tracks.isNotEmpty()) View.GONE else View.VISIBLE
            })

        // setup swipe to delete
        val swipeToDelete = ItemTouchHelper(object : SwipeToDeleteCallback(
            requireContext(),
            resolveColor(R.attr.colorError),
            R.drawable.ic_round_close_24,
            resolveColor(R.attr.colorOnError),
            15
        ) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                if (viewHolder !is TracksAdapter.Holder) {
                    throw IllegalStateException("got a wrong typed view holder")
                }
                tracksAdapter.deleteLater(
                    viewHolder
                ) { track: TrackInfo ->

                    launchIO {
                        // remove files
                        track.deleteLocalFiles(this@TracksFragment.requireContext())

                        // remove from db
                        TracksDB.get(this@TracksFragment.requireContext()).tracks()
                            .remove(track)
                    }
                }
            }
        })
        swipeToDelete.attachToRecyclerView(b.tracksRecycler)
        b.tracksRecycler.adapter = tracksAdapter
    }

    /**
     * play a track
     *
     * @param track the track to play
     */
    private fun playTrack(track: TrackInfo) {
        // decode track audio file key
        val trackUri = track.audioFileKey.decodeToUri()
        if (trackUri != null) {
            Toast.makeText(requireContext(), R.string.tracks_play_failed, Toast.LENGTH_SHORT).show()
            return
        }

        // start external player
        val playIntent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(trackUri, "audio/*")
            .addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        or Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        startActivity(playIntent)
    }

    /**
     * re- download a track
     *
     * @param track the track to re- download
     */
    private fun reDownloadTrack(track: TrackInfo) {
        // reset status to pending
        track.status = TrackStatus.DownloadPending

        // overwrite entry in db
        launchIO {
            TracksDB.get(requireContext()).tracks().insert(track)
        }
    }

    /**
     * resolve a color from a attribute
     *
     * @param attr the color attribute to resolve
     * @return the resolved color int
     */
    @ColorInt
    private fun resolveColor(@AttrRes attr: Int): Int {
        val value = TypedValue()
        requireContext().theme.resolveAttribute(attr, value, true)
        return value.data
    }
}
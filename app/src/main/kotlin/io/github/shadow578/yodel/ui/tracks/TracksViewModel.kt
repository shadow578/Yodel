package io.github.shadow578.yodel.ui.tracks

import android.app.*
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.*
import io.github.shadow578.yodel.R
import io.github.shadow578.yodel.db.TracksDB
import io.github.shadow578.yodel.db.model.*
import io.github.shadow578.yodel.util.*
import io.github.shadow578.yodel.util.storage.decodeToUri

/**
 * view model for tracks
 */
class TracksViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * observable list of all tracks
     */
    val tracks: LiveData<List<TrackInfo>>
        get() = TracksDB.get(getApplication()).tracks().observe()

    /**
     * play a track
     *
     * @param parent the parent activity
     * @param track the track to play
     */
    fun playTrack(parent: Activity, track: TrackInfo) {
        // decode track audio file key
        val trackUri = track.audioFileKey.decodeToUri()
        if (trackUri == null) {
            Toast.makeText(parent, R.string.tracks_play_failed, Toast.LENGTH_SHORT).show()
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
        parent.startActivity(playIntent)
    }

    /**
     * re- download a track
     *
     * @param track the track to re- download
     */
    fun reDownloadTrack(track: TrackInfo) {
        // reset status to pending
        track.status = TrackStatus.DownloadPending

        // overwrite entry in db
        launchIO {
            TracksDB.get(getApplication()).tracks().insert(track)
        }
    }

    /**
     * remove a track and all downloaded files
     *
     * @param track the track to remove
     */
    fun removeTrack(track: TrackInfo) {
        launchIO {
            val ctx = getApplication<Application>()

            // remove files
            track.deleteLocalFiles(ctx)

            // remove from db
            TracksDB.get(ctx).tracks()
                .remove(track)
        }
    }
}
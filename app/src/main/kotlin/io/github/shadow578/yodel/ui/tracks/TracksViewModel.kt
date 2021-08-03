package io.github.shadow578.yodel.ui.tracks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import io.github.shadow578.yodel.db.TracksDB
import io.github.shadow578.yodel.db.model.TrackInfo

/**
 * view model for tracks
 */
class TracksViewModel(application: Application) : AndroidViewModel(application) {
    val tracks: LiveData<List<TrackInfo>>
        get() = TracksDB.get(getApplication()).tracks().observe()
}
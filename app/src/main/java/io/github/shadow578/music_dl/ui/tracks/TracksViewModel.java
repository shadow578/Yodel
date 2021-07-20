package io.github.shadow578.music_dl.ui.tracks;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import io.github.shadow578.music_dl.db.TracksDB;
import io.github.shadow578.music_dl.db.model.TrackInfo;

public class TracksViewModel extends AndroidViewModel {
    public TracksViewModel(@NonNull Application application) {
        super(application);
        TracksDB.init(getApplication());
    }

    public LiveData<List<TrackInfo>> getTracks(){
        return TracksDB.getInstance().tracks().observe();
    }

}
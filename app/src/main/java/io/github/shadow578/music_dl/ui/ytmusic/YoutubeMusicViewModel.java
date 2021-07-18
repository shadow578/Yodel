package io.github.shadow578.music_dl.ui.ytmusic;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import io.github.shadow578.music_dl.db.TracksDB;

/**
 * viewmodel for the {@link YoutubeMusicActivity}
 */
public class YoutubeMusicViewModel extends AndroidViewModel {

    /**
     * the current title, null if no track is playing
     */
    @Nullable
    private String currentTitle = null;

    /**
     * is auto- downloading enabled?
     */
    private final MutableLiveData<Boolean> autoDownloadEnabled = new MutableLiveData<>(false);

    /**
     * create the view model
     *
     * @param application the application reference
     */
    public YoutubeMusicViewModel(@NonNull Application application) {
        super(application);
        TracksDB.init(getApplication());
    }

    /**
     * start downloading a track
     *
     * @param id the track to download
     */
    public void downloadTrack(@NonNull String id) {
        // check if a track is playing
        if (!isTrackPlaying()) {
            return;
        }


        //TODO downloader impl
        Log.i("DL", "download track " + id);
    }

    /**
     * set the auto- download mode enable
     *
     * @param enabled enable auto- download mode?
     */
    public void setAutoDownloadEnabled(boolean enabled) {
        autoDownloadEnabled.postValue(enabled);
    }

    /**
     * @return auto- download mode enable
     */
    @NonNull
    public LiveData<Boolean> getAutoDownloadEnabled() {
        return autoDownloadEnabled;
    }

    /**
     * set the current track title
     *
     * @param title the title of the current track. null or empty if no track is playing
     */
    public void setCurrentTitle(@Nullable String title) {
        currentTitle = title;
    }

    /**
     * @return is there currently a track playing?
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isTrackPlaying() {
        return currentTitle != null && !currentTitle.isEmpty();
    }
}

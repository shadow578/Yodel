package io.github.shadow578.music_dl.ui.ytmusic;

import android.app.Application;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import io.github.shadow578.music_dl.db.TracksDB;
import io.github.shadow578.music_dl.db.model.TrackInfo;
import io.github.shadow578.music_dl.downloader.DownloaderService;
import io.github.shadow578.music_dl.util.Async;

/**
 * viewmodel for the {@link YoutubeMusicFragment}
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

        //TODO do not overwrite existing entries -> show dialog to choose to delete existing file or just ignore

        // insert into database
        Async.runAsync(()
                -> TracksDB.getInstance().tracks().insert(TrackInfo.createNew(id, currentTitle)));

        // start downloader
        maybeStartDownloaderService();
    }

    /**
     * start the downloader service, if needed
     */
    private void maybeStartDownloaderService() {
        final Intent serviceIntent = new Intent(getApplication(), DownloaderService.class);
        getApplication().startService(serviceIntent);
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

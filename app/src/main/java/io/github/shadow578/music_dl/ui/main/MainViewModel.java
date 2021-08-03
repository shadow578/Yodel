package io.github.shadow578.music_dl.ui.main;

import android.app.Application;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import io.github.shadow578.music_dl.KtPorted;
import io.github.shadow578.music_dl.downloader.DownloaderService;

/**
 * view model for the main activity
 */
@KtPorted
public class MainViewModel extends AndroidViewModel {
    public MainViewModel(@NonNull Application application) {
        super(application);
        startDownloadService();
    }

    /**
     * the currently open section
     */
    @NonNull
    private final MutableLiveData<MainActivity.Section> currentSection = new MutableLiveData<>(MainActivity.Section.Tracks);

    /**
     * start the downloader service
     */
    private void startDownloadService() {
        final Intent serviceIntent = new Intent(getApplication(), DownloaderService.class);
        getApplication().startService(serviceIntent);
    }

    /**
     * @return the currently visible section
     */
    @NonNull
    public LiveData<MainActivity.Section> getSection() {
        return currentSection;
    }

    /**
     * switch the currently active section
     *
     * @param section the section to switch to
     */
    public void switchToSection(@NonNull MainActivity.Section section) {
        // ignore if same section
        if (section.equals(currentSection.getValue())) {
            return;
        }

        currentSection.setValue(section);
    }
}

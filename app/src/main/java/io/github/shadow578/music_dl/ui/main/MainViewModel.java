package io.github.shadow578.music_dl.ui.main;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * viewmodel for the main activity
 */
public class MainViewModel extends ViewModel {

    /**
     * the currently open section
     */
    @NonNull
    private final MutableLiveData<MainActivity.Section> currentSection = new MutableLiveData<>(MainActivity.Section.Tracks);


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
        currentSection.setValue(section);
    }
}

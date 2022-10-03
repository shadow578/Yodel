package io.github.shadow578.yodel.ui.main

import android.app.Application
import androidx.lifecycle.*

/**
 * view model for the main activity
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {
    /**
     * the currently open section
     */
    private val currentSection = MutableLiveData(MainActivity.Section.Tracks)

    /**
     * @return the currently visible section
     */
    val section: LiveData<MainActivity.Section>
        get() = currentSection

    /**
     * switch the currently active section
     *
     * @param section the section to switch to
     */
    fun switchToSection(section: MainActivity.Section) {
        // ignore if same section
        if (section == currentSection.value) {
            return
        }
        currentSection.value = section
    }
}

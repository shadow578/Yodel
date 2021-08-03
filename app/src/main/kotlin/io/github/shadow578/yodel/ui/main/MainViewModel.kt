package io.github.shadow578.yodel.ui.main

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.github.shadow578.yodel.downloader.DownloaderService

/**
 * view model for the main activity
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {
    init {
        startDownloadService()
    }

    /**
     * the currently open section
     */
    private val currentSection = MutableLiveData(MainActivity.Section.Tracks)

    /**
     * start the downloader service
     */
    private fun startDownloadService() {
        val serviceIntent = Intent(getApplication(), DownloaderService::class.java)
        getApplication<Application>().startService(serviceIntent)
    }

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

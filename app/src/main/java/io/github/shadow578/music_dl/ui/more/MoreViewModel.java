package io.github.shadow578.music_dl.ui.more;

import android.app.Activity;
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import io.github.shadow578.music_dl.downloader.TrackDownloadFormat;
import io.github.shadow578.music_dl.ui.BaseActivity;
import io.github.shadow578.music_dl.util.preferences.Prefs;

/**
 * view model for more fragment
 */
public class MoreViewModel extends AndroidViewModel {
    public MoreViewModel(@NonNull Application application) {
        super(application);
    }

    /**
     * currently selected download format
     */
    private final MutableLiveData<TrackDownloadFormat> downloadFormat = new MutableLiveData<>(Prefs.DownloadFormat.get());

    /**
     * current state of ssl_fix enable
     */
    private final MutableLiveData<Boolean> enableSSLFix = new MutableLiveData<>(Prefs.EnableSSLFix.get());

    /**
     * current state of metadata tagging enable
     */
    private final MutableLiveData<Boolean> enableTagging = new MutableLiveData<>(Prefs.EnableMetadataTagging.get());

    /**
     * open the about page
     *
     * @param activity parent activity
     */
    public void openAboutPage(@NonNull Activity activity) {

    }

    /**
     * choose the download directory
     *
     * @param activity parent activity
     */
    public void chooseDownloadsDir(@NonNull Activity activity) {
        if (activity instanceof BaseActivity) {
            ((BaseActivity) activity).maybeSelectDownloadsDir(true);
        }
    }

    /**
     * set the download format
     *
     * @param format new download format
     */
    public void setDownloadFormat(@NonNull TrackDownloadFormat format) {
        // ignore if no change
        if (format.equals(downloadFormat.getValue())) {
            return;
        }

        Prefs.DownloadFormat.set(format);
        downloadFormat.setValue(format);
    }

    /**
     * @return currently selected download format
     */
    @NonNull
    public LiveData<TrackDownloadFormat> getDownloadFormat() {
        return downloadFormat;
    }

    /**
     * set ssl fix enable
     *
     * @param enable enable ssl fix?
     */
    public void setEnableSSLFix(boolean enable) {
        Prefs.EnableSSLFix.set(enable);
        enableSSLFix.setValue(enable);
    }

    /**
     * @return current state of ssl_fix enable
     */
    @NonNull
    public LiveData<Boolean> getEnableSSLFix() {
        return enableSSLFix;
    }

    /**
     * enable or disable metadata tagging
     *
     * @param enable is tagging enabled?
     */
    public void setEnableTagging(boolean enable) {
        Prefs.EnableMetadataTagging.set(enable);
        enableTagging.setValue(enable);
    }

    /**
     * @return current state of metadata tagging enable
     */
    @NonNull
    public LiveData<Boolean> getEnableTagging() {
        return enableTagging;
    }
}

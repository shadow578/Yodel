package io.github.shadow578.music_dl.ui.more;

import android.app.Activity;
import android.app.Application;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.documentfile.provider.DocumentFile;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mikepenz.aboutlibraries.LibsBuilder;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import io.github.shadow578.music_dl.LocaleOverride;
import io.github.shadow578.music_dl.R;
import io.github.shadow578.music_dl.backup.BackupData;
import io.github.shadow578.music_dl.backup.BackupHelper;
import io.github.shadow578.music_dl.downloader.TrackDownloadFormat;
import io.github.shadow578.music_dl.ui.base.BaseActivity;
import io.github.shadow578.music_dl.util.Async;
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
     * currently selected locale override
     */
    private final MutableLiveData<LocaleOverride> localeOverride = new MutableLiveData<>(Prefs.LocaleOverride.get());

    /**
     * open the about page
     *
     * @param activity parent activity
     */
    public void openAboutPage(@NonNull Activity activity) {
        final LibsBuilder libs = new LibsBuilder();
       // libs.setAboutShowIcon(true);
        libs.setAboutAppName(activity.getString(R.string.app_name));
        //libs.setShowLicense(true);
        //libs.setShowVersion(true);
        libs.start(activity);
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
     * import tracks from a backup file
     *
     * @param file the file to import from
     */
    public void importTracks(@NonNull DocumentFile file, @NonNull Activity parent) {
        Async.runAsync(() -> {
            // read the backup data
            final Optional<BackupData> backup = BackupHelper.readBackupData(getApplication(), file);
            if (!backup.isPresent()) {
                Async.runOnMain(()
                        -> Toast.makeText(getApplication(), R.string.restore_toast_failed, Toast.LENGTH_SHORT).show());
                return;
            }

            // show confirmation dialog
            Async.runOnMain(() -> {
                final AtomicBoolean replaceExisting = new AtomicBoolean(false);
                final int tracksCount = backup.get().tracks.size();
                new AlertDialog.Builder(parent)
                        .setTitle(getApplication().getString(R.string.restore_dialog_title, tracksCount))
                        .setSingleChoiceItems(R.array.restore_dialog_modes, 0, (dialog, mode)
                                -> replaceExisting.set(mode == 1))
                        .setNegativeButton(R.string.restore_dialog_negative, (dialog, w) -> dialog.dismiss())
                        .setPositiveButton(R.string.restore_dialog_positive, (dialog, w) -> {
                            // restore the backup
                            Toast.makeText(getApplication(), getApplication().getString(R.string.restore_toast_success, tracksCount), Toast.LENGTH_SHORT).show();
                            Async.runAsync(()
                                    -> BackupHelper.restoreBackup(getApplication(), backup.get(), replaceExisting.get()));
                        })
                        .show();
            });
        });
    }

    /**
     * export tracks to a backup file
     *
     * @param file the file to export to
     */
    public void exportTracks(@NonNull DocumentFile file) {
        Async.runAsync(() -> {
            final boolean success = BackupHelper.createBackup(getApplication(), file);
            if (!success) {
                Async.runOnMain(()
                        -> Toast.makeText(getApplication(), R.string.backup_toast_failed, Toast.LENGTH_SHORT).show());
            }
        });
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
        if(Boolean.valueOf(enable).equals(enableSSLFix.getValue()))
        {
            return;
        }

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
        if(Boolean.valueOf(enable).equals(enableTagging.getValue()))
        {
            return;
        }

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

    /**
     * set the currently selected locale override
     *
     * @param localeOverride the currently selected locale override
     * @return  was the locale changed?
     */
    public boolean setLocaleOverride(@NonNull LocaleOverride localeOverride) {
        if(localeOverride.equals(this.localeOverride.getValue()))
        {
            return false;
        }

        Prefs.LocaleOverride.set(localeOverride);
        this.localeOverride.setValue(localeOverride);
        return true;
    }

    /**
     * @return the currently selected locale override
     */
    @NonNull
    public LiveData<LocaleOverride> getLocaleOverride() {
        return localeOverride;
    }
}

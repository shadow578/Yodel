package io.github.shadow578.music_dl.ui.base;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import java.util.Optional;

import io.github.shadow578.music_dl.KtPorted;
import io.github.shadow578.music_dl.R;
import io.github.shadow578.music_dl.downloader.DownloaderService;
import io.github.shadow578.music_dl.util.LocaleUtil;
import io.github.shadow578.music_dl.util.preferences.Prefs;
import io.github.shadow578.music_dl.util.storage.StorageHelper;
import io.github.shadow578.music_dl.util.storage.StorageKey;

/**
 * topmost base activity. this is to be extended when creating a new activity.
 * handles app- specific stuff
 */
@KtPorted
public class BaseActivity extends AppCompatActivity {

    /**
     * result launcher for download directory select
     */
    private ActivityResultLauncher<Uri> downloadDirectorySelectLauncher;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleUtil.wrapContext(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // create result launcher for download directory select
        downloadDirectorySelectLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocumentTree(),
                treeUri -> {
                    if (treeUri != null) {
                        final DocumentFile treeFile = DocumentFile.fromTreeUri(this, treeUri);

                        // check access
                        if (treeFile != null
                                && treeFile.exists()
                                && treeFile.canRead()
                                && treeFile.canWrite()) {
                            // persist the permission & save
                            final StorageKey treeKey = StorageHelper.persistFilePermission(getApplicationContext(), treeUri);
                            Prefs.DownloadsDirectory.set(treeKey);
                            Log.i("MusicDL", String.format("selected and saved new track downloads directory: %s", treeUri.toString()));

                            // restart downloader
                            final Intent serviceIntent = new Intent(getApplication(), DownloaderService.class);
                            getApplication().startService(serviceIntent);
                        } else {
                            // bad selection
                            Toast.makeText(this, R.string.base_toast_set_download_directory_fail, Toast.LENGTH_LONG).show();
                            maybeSelectDownloadsDir(true);
                        }
                    }
                }
        );
    }

    /**
     * prompt the user to select the downloads dir, if not set
     *
     * @param force force select a new directory?
     */
    public void maybeSelectDownloadsDir(boolean force) {
        // check if downloads dir is set and accessible
        final StorageKey downloadsKey = Prefs.DownloadsDirectory.get();
        if (!downloadsKey.equals(StorageKey.EMPTY) && !force) {
            final Optional<DocumentFile> downloadsDir = StorageHelper.getPersistedFilePermission(this, downloadsKey, true);
            if (downloadsDir.isPresent()
                    && downloadsDir.get().exists()
                    && downloadsDir.get().canWrite()) {
                // download directory exists and can write, all OK!
                return;
            }
        }

        // select directory
        Toast.makeText(this, R.string.base_toast_select_download_directory, Toast.LENGTH_LONG).show();
        downloadDirectorySelectLauncher.launch(null);
    }
}

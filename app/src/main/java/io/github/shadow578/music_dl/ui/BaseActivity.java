package io.github.shadow578.music_dl.ui;

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

import io.github.shadow578.music_dl.R;
import io.github.shadow578.music_dl.util.preferences.Prefs;
import io.github.shadow578.music_dl.util.storage.StorageHelper;
import io.github.shadow578.music_dl.util.storage.StorageKey;

/**
 * base activity, with some common functionality
 */
public class BaseActivity extends AppCompatActivity {

    /**
     * result launcher for download directory select
     */
    private ActivityResultLauncher<Intent> downloadDirectorySelectLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // create result launcher for download directory select
        downloadDirectorySelectLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // make sure result is ok
                    final Intent data = result.getData();
                    if (result.getResultCode() == RESULT_OK && data != null) {
                        final Uri treeUri = data.getData();
                        if (treeUri != null) {
                            final DocumentFile treeFile = DocumentFile.fromTreeUri(this, treeUri);

                            // check access
                            if (treeFile != null
                                    && treeFile.exists()
                                    && treeFile.canRead()
                                    && treeFile.canWrite()) {
                                // persist the permission & save
                                final StorageKey treeKey = StorageHelper.persistFilePermission(getApplicationContext(), treeUri);
                                Prefs.DOWNLOADS_DIRECTORY.set(treeKey);
                                Log.i("MusicDL", String.format("selected and saved new track downloads directory: %s", treeUri.toString()));
                            } else {
                                // bad selection
                                Toast.makeText(this, R.string.base_toast_set_download_directory_fail, Toast.LENGTH_LONG).show();
                                maybeSelectDownloadsDir();
                            }
                        }
                    }
                }
        );
    }

    /**
     * prompt the user to select the downloads dir, if not set
     */
    protected void maybeSelectDownloadsDir() {
        // check if downloads dir is set and accessible
        final StorageKey downloadsKey = Prefs.DOWNLOADS_DIRECTORY.get();
        if (downloadsKey != null) {
            final Optional<DocumentFile> downloadsDir = StorageHelper.getPersistedFilePermission(this, downloadsKey, true);
            if (downloadsDir.isPresent()
                    && downloadsDir.get().exists()
                    && downloadsDir.get().canWrite()) {
                // download directory exists and can write, all OK!
                return;
            }
        }

        // reset preference key so next check could be faster
        Prefs.DOWNLOADS_DIRECTORY.reset();

        // show toast with prompt
        Toast.makeText(this, R.string.base_toast_select_download_directory, Toast.LENGTH_LONG).show();

        // select downloads dir
        final Intent openTree = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        downloadDirectorySelectLauncher.launch(openTree);
    }
}

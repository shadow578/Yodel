package io.github.shadow578.music_dl.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;
import java.util.StringJoiner;

import io.github.shadow578.music_dl.databinding.ActivityScopedStorageTestBinding;
import io.github.shadow578.music_dl.util.storage.StorageHelper;
import io.github.shadow578.music_dl.util.preferences.PreferenceWrapper;
import io.github.shadow578.music_dl.util.preferences.Prefs;
import io.github.shadow578.music_dl.util.storage.StorageKey;

/**
 * Storage API testing (really crappy but shows the basics)
 * Android 12 ready
 */
public class ScopedStorageTestActivity extends AppCompatActivity {

    private ActivityScopedStorageTestBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityScopedStorageTestBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        PreferenceWrapper.init(PreferenceManager.getDefaultSharedPreferences(this));

        final Optional<DocumentFile> root = StorageHelper.getPersistedFilePermission(this, Prefs.DOWNLOADS_DIR.get(), true);
        if (root.isPresent()) {
            b.tstExtDir.setText(root.get().getUri().toString());
        } else {
            b.tstExtDir.setText("NONE");
        }

        final ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK) {
                            final Uri treeUri = result.getData().getData();
                            final StorageKey key = StorageHelper.persistFilePermission(ScopedStorageTestActivity.this, treeUri);
                            Prefs.DOWNLOADS_DIR.set(key);
                        }
                    }
                }
        );


        b.tstStorage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            resultLauncher.launch(intent);
        });

        b.tstRead.setOnClickListener(v -> {
            // get ext dir
            final DocumentFile treeRoot = StorageHelper.getPersistedFilePermission(this, Prefs.DOWNLOADS_DIR.get(), true).get();

            // enumerate all files, write to string
            final StringJoiner treeString = new StringJoiner("\n");
            for (DocumentFile file : treeRoot.listFiles())
                treeString.add(file.getName() + " AT " + file.getUri());

            b.tstDirOut.setText(treeString.toString());
        });

        b.tstWrite.setOnClickListener(v -> {
            // get ext dir
            final DocumentFile treeRoot = StorageHelper.getPersistedFilePermission(this, Prefs.DOWNLOADS_DIR.get(), true).get();
            final DocumentFile file = writeRnd(treeRoot);

            Prefs.LAST_FILE.set(StorageHelper.encodeFile(file));
        });

        b.tstGetFromPrefs.setOnClickListener(v -> {
            // get file
            final DocumentFile file = StorageHelper.decodeFile(this, Prefs.LAST_FILE.get()).get();

            Log.i("YTDL", "R: " + file.canRead() + "; W: " + file.canWrite());
            Log.i("YTDL", "DEL: " + file.delete());
        });
    }

    private DocumentFile writeRnd(DocumentFile treeRoot) {
        Log.i("YTDL", "W:" + treeRoot.canWrite());

        final DocumentFile file = treeRoot.createFile("text/plain", "Test");
        try (OutputStream out = getContentResolver().openOutputStream(file.getUri())) {
            out.write("YEE Yeee ree".getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }
}
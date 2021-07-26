package io.github.shadow578.music_dl.share;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Optional;

import io.github.shadow578.music_dl.R;
import io.github.shadow578.music_dl.db.TracksDB;
import io.github.shadow578.music_dl.db.model.TrackInfo;
import io.github.shadow578.music_dl.downloader.DownloaderService;
import io.github.shadow578.music_dl.util.Async;
import io.github.shadow578.music_dl.util.Util;

/**
 * activity that handles shared youtube links (for download)
 */
public class ShareTargetActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intent = getIntent();

        // action is SHARE
        if (intent != null
                && Intent.ACTION_SEND.equals(intent.getAction())) {
            if (handleShare(intent)) {
                Toast.makeText(this, R.string.share_toast_ok, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.share_toast_fail, Toast.LENGTH_SHORT).show();
            }
        }

        // done
        finish();
    }

    /**
     * handle a shared video url
     *
     * @param intent the share intent (with ACTION_SEND)
     * @return could the url be handled successfully?
     */
    private boolean handleShare(@NonNull Intent intent) {
        // type is text/plain
        if (!"text/plain".equalsIgnoreCase(intent.getType())) {
            return false;
        }

        // has EXTRA_TEXT
        if (!intent.hasExtra(Intent.EXTRA_TEXT)) {
            return false;
        }

        // get shared url from text
        final String url = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (url == null || url.isEmpty()) {
            return false;
        }

        // get video ID
        final Optional<String> trackId = Util.extractTrackId(url);
        if (!trackId.isPresent()) {
            return false;
        }

        // get title if possible
        String title = null;
        if (intent.hasExtra(Intent.EXTRA_TITLE)) {
            title = intent.getStringExtra(Intent.EXTRA_TITLE);

        }

        final String trackTitle;
        if (title != null && !title.isEmpty()) {
            trackTitle = title;
        } else {
            trackTitle = "Unknown Track";
        }

        // add to db as pending download
        Async.runAsync(()
                -> TracksDB.getInstance().tracks().insert(TrackInfo.createNew(trackId.get(), trackTitle)));

        // start downloader as needed
        startDownloadService();
        return true;
    }

    /**
     * start the downloader service
     */
    private void startDownloadService() {
        final Intent serviceIntent = new Intent(this, DownloaderService.class);
        startService(serviceIntent);
    }
}

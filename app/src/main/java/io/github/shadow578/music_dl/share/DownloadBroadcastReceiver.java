package io.github.shadow578.music_dl.share;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.util.Optional;

import io.github.shadow578.music_dl.db.TracksDB;
import io.github.shadow578.music_dl.db.model.TrackInfo;
import io.github.shadow578.music_dl.downloader.DownloaderService;
import io.github.shadow578.music_dl.util.Async;
import io.github.shadow578.music_dl.util.Util;

/**
 * a broadcast receiver for receiving download requests from other apps.<br>
 * usage example:
 * <pre>
 * final Intent broadcast = new Intent()
 *  .putExtra("io.github.shadow578.music_dl.extra.VIDEO_URL", "Youtube URL")
 *  //or .putExtra("io.github.shadow578.music_dl.extra.VIDEO_ID", "Youtube ID")
 *  .putExtra("io.github.shadow578.music_dl.extra.TITLE", "Track Title")
 *  .setAction("io.github.shadow578.music_dl.action.QUEUE_DOWNLOAD")
 *  .setPackage("io.github.shadow578.music_dl")
 *  .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
 * this.sendBroadcast(broadcast);
 * </pre>
 */
public class DownloadBroadcastReceiver extends BroadcastReceiver {

    /**
     * action string for queueing a new track download
     */
    public static final String ACTION_QUEUE_DOWNLOAD = "io.github.shadow578.music_dl.action.QUEUE_DOWNLOAD";

    /**
     * intent extra for the youtube track/video id.
     * either this or VIDEO_URL is needed
     */
    public static final String EXTRA_VIDEO_ID = "io.github.shadow578.music_dl.extra.VIDEO_ID";

    /**
     * intent extra for the youtube video url.
     * either this or VIDEO_ID is needed
     */
    public static final String EXTRA_VIDEO_URL = "io.github.shadow578.music_dl.extra.VIDEO_URL";

    /**
     * intent extra for the track title.
     * this is optional
     */
    public static final String EXTRA_TITLE = "io.github.shadow578.music_dl.extra.TITLE";

    /**
     * logger tag
     */
    private static final String TAG = "DL_Receiver";

    @Override
    public void onReceive(Context ctx, Intent intent) {
        // ensure valid parameters
        if (ctx == null || intent == null) {
            return;
        }

        // check action
        if (!ACTION_QUEUE_DOWNLOAD.equals(intent.getAction())) {
            Log.w(TAG, "received intent with invalid action: " + intent.getAction());
            return;
        }

        // find VIDEO_ID
        Optional<String> videoId = Optional.ofNullable(intent.getStringExtra(EXTRA_VIDEO_ID));

        // try with VIDEO_URL
        if (!videoId.isPresent()) {
            final String url = intent.getStringExtra(EXTRA_VIDEO_URL);
            if (url != null && !url.isEmpty()) {
                videoId = Util.extractTrackId(url);
            }
        }

        // check if we have a id for the download
        if (!videoId.isPresent()) {
            Log.w(TAG, "could not find video url or id from intent extras!");
            return;
        }

        // find title
        final Optional<String> title = Optional.ofNullable(intent.getStringExtra(EXTRA_TITLE));

        // enqueue track download
        final String trackId = videoId.get();
        Async.runAsync(()
                -> TracksDB.init(ctx).tracks().insert(TrackInfo.createNew(trackId, title.orElse("Unknown Track"))));

        // start downloader service
        final Intent serviceIntent = new Intent(ctx, DownloaderService.class);
        ContextCompat.startForegroundService(ctx, serviceIntent);
    }
}

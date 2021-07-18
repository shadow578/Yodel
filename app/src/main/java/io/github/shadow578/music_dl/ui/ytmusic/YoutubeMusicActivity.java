package io.github.shadow578.music_dl.ui.ytmusic;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import io.github.shadow578.music_dl.databinding.ActivityYoutubeMusicBinding;
import io.github.shadow578.music_dl.util.BaseActivity;
import io.github.shadow578.music_dl.util.Payload;
import io.github.shadow578.music_dl.util.Url;
import io.github.shadow578.music_dl.util.Util;

/**
 * the yt music browsing activity
 */
public class YoutubeMusicActivity extends BaseActivity {

    /**
     * the view binding instance
     */
    private ActivityYoutubeMusicBinding b;

    /**
     * the view model instance
     */
    private YoutubeMusicViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // inflate the view using view binding
        b = ActivityYoutubeMusicBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        // create downloader instance
        model = new ViewModelProvider(this).get(YoutubeMusicViewModel.class);

        // setup buttons
        b.downloadTrack.setOnClickListener(v -> handleDownloadTrack());
        b.autoDownloadList.setOnCheckedChangeListener((v, checked) -> handleAutoDownload(checked));

        // sync auto- download with model value
        model.getAutoDownloadEnabled().observe(this, autoDl -> {
            b.autoDownloadList.setChecked(autoDl);
            b.autoDownloadOverlay.setVisibility(autoDl ? View.VISIBLE : View.GONE);
        });

        // init webview
        initWebView();

        // navigate to yt music start page
        b.webview.loadUrl(Url.YoutubeMusicMain.url());
    }

    /**
     * initialize the webview
     */
    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {
        // enable remote debugging (from chrome://inspect)
        WebView.setWebContentsDebuggingEnabled(true);

        // set the webview client to one we have events on
        b.webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                injectStaticPayload();
            }
        });

        // enable js and ...
        b.webview.getSettings().setJavaScriptEnabled(true);
        b.webview.getSettings().setDomStorageEnabled(true);
        b.webview.getSettings().setAppCacheEnabled(true);

        // add javascript interface
        b.webview.addJavascriptInterface(new JSInterface(), "JSI");
    }

    /**
     * inject static javascript payloads
     */
    private void injectStaticPayload() {
        // monitor if the video changed
        Payload.MonitorVideoChange.run(b.webview);

        // hide popups
        Payload.HidePopups.runForever(b.webview);
    }

    /**
     * download the current track
     */
    private void handleDownloadTrack() {
        // extract id
        final String id = Util.extractTrackId(b.webview.getUrl());

        // show toast if could not find id or no track is playing
        if (id == null
                || id.isEmpty()
                || !model.isTrackPlaying()) {
            Toast.makeText(this, "could not find track info! is a track playing?", Toast.LENGTH_SHORT).show();
            return;
        }

        // call model download
        model.downloadTrack(id);
    }

    /**
     * start or stop auto- downloading the current playlist
     *
     * @param enable enable auto- download?
     */
    private void handleAutoDownload(boolean enable) {
        model.setAutoDownloadEnabled(enable);

        if (enable) {
            autoDownloadNext(true);
        }
    }

    /**
     * if in auto- download mode, go to the next track
     *
     * @param force disable check if auto- download is actually enabled
     */
    private void autoDownloadNext(boolean force) {
        // abort if not auto- downloading
        if (!force && !Boolean.TRUE.equals(model.getAutoDownloadEnabled().getValue())) {
            return;
        }

        // download this track
        handleDownloadTrack();

        // go to the next track
        Payload.NextTrack.runLater(b.webview, 1000);
    }

    /**
     * page javascript -> android bridge
     */
    @SuppressWarnings("unused")
    public class JSInterface {

        /**
         * notification from the page js that the main video just changed
         */
        @JavascriptInterface
        public void videoChanged() {
            // update the title
            Util.runOnMain(() -> Payload.ExtractTrackTitle.run(b.webview));
        }

        /**
         * report the video title
         *
         * @param title the video title. empty or null if a ad is playing
         */
        @JavascriptInterface
        public void reportTitle(@Nullable String title) {
            Util.runOnMain(() -> {
                // report new title
                model.setCurrentTitle(title);

                // handle auto- downloading
                autoDownloadNext(false);
            });
        }
    }
}
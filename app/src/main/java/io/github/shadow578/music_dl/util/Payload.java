package io.github.shadow578.music_dl.util;

import android.webkit.WebView;

import androidx.annotation.NonNull;

/**
 * javascript payloads that can be injected into music.youtube.com
 */
public enum Payload {

    /**
     * extract the title of the track. if a ad is currently playing, will result in a empty string. calls JSI.reportTitle() with the result
     */
    ExtractTrackTitle("JSI.reportTitle(document.querySelectorAll('.title.ytmusic-player-bar')[0].innerText)"),

    /**
     * go to the next track
     */
    NextTrack("document.querySelectorAll('.next-button')[0].click()"),

    /**
     * go to the previous track
     */
    PreviousTrack("document.querySelectorAll('.previous-button')[0].click()"),

    /**
     * hide popups (the 'buy youtube premium' ones
     */
    HidePopups("document.querySelectorAll('.ytmusic-popup-container').forEach(function(v) { v.style.display=\"none\" })"),

    /**
     * monitor if the main video changed. calls JSI.videoChanged() when the video changes
     */
    MonitorVideoChange("document.querySelectorAll(\".html5-main-video\")[0].onplaying = function() { JSI.videoChanged() }");

    /**
     * the javascript expression of this payload
     */
    @NonNull
    private final String jsExpression;

    /**
     * create a new JS payload
     *
     * @param jsExpression the javascript expression to execute
     */
    Payload(@NonNull String jsExpression) {
        this.jsExpression = jsExpression;
    }

    /**
     * run the payload in the webview
     *
     * @param webView the webview to run the payload in
     */
    public void run(@NonNull WebView webView) {
        webView.evaluateJavascript(jsExpression, null);
    }


    /**
     * run the payload in the webview with a initial delay
     *
     * @param webView the webview to run the payload in
     * @param delay   the delay before running the payload, in milliseconds
     */
    public void runLater(@NonNull WebView webView, int delay) {
        if (delay <= 0)
            throw new IllegalArgumentException("delay must be > 0 ms");

        webView.evaluateJavascript("setTimeout(function() { " + jsExpression + " }, " + delay + ")", null);
    }

    /**
     * run the payload in the webview every x ms, forever
     *
     * @param webView the webview to run the payload in
     */
    public void runForever(@NonNull WebView webView) {
        webView.evaluateJavascript("setInterval(function() { " + jsExpression + " }, 100)", null);
    }
}

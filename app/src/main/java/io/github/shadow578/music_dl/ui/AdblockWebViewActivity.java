package io.github.shadow578.music_dl.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.service.autofill.FieldClassification;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.shadow578.music_dl.databinding.ActivityAdblockWebViewBinding;

public class AdblockWebViewActivity extends AppCompatActivity {

    ActivityAdblockWebViewBinding b;

    final Set<String> blockedHosts = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityAdblockWebViewBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        final Handler h = new Handler(Looper.getMainLooper());


        // no cookies
        deleteDatabase("webview.db");
        deleteDatabase("webviewCache.db");

        // javascript injected during page load
        final String[] jsInjects = {
                "console.log('jsInjects running')",
                "ytInitialPlayerResponse.adPlacements = undefined",
                "playerResponse.adPlacements = undefined"
        };


        final WebView w = b.webview;

        w.getSettings().setJavaScriptEnabled(true);
        w.getSettings().setDomStorageEnabled(true);
        w.getSettings().setAppCacheEnabled(true);

        final Pattern pattern = Pattern.compile("(?:.+\\.)?(.+\\..+)");
        final WebViewClient wwc = new WebViewClient() {
            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                // do javascript buckaroo
                h.post(() -> {
                    for (String js : jsInjects)
                        view.evaluateJavascript(js, null);
                });


                // get hosts to check
                // the partHost value only contains the last part of the host
                // eg. for tcp.ads.com, this would be ads.com
                final String fullHost = request.getUrl().getHost().toLowerCase();
                String partHost = fullHost;
                final Matcher m = pattern.matcher(fullHost);
                if (m.find()) {
                    partHost = m.group(1);
                }

                // check if host is blocked
                final String s = String.format("(host %s; phost %s) %s via method %s ", fullHost, partHost, request.getUrl(), request.getMethod());
                if (blockedHosts.contains(fullHost) || blockedHosts.contains(partHost)) {
                    Log.w("BLOCK", s);
                    return new WebResourceResponse("text/plain", "utf-8", new ByteArrayInputStream(new byte[0]));
                } else {
                    Log.i("PASS", s);
                }

                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                Log.e("PAGE", "FINISHED");
            }

            @Override
            public void onFormResubmission(WebView view, Message dontResend, Message resend) {
                super.onFormResubmission(view, dontResend, resend);
            }
        };
        w.setWebViewClient(wwc);


        Executors.newSingleThreadExecutor().execute(() -> {
            // download adblock list
            // from https://pgl.yoyo.org/adservers/serverlist.php?hostformat=nohtml&showintro=1&mimetype=plaintext
            loadFilterList("https://pgl.yoyo.org/adservers/serverlist.php?hostformat=nohtml&showintro=1&mimetype=plaintext", true);
            loadFilterList("https://raw.githubusercontent.com/jerryn70/GoodbyeAds/master/Extension/GoodbyeAds-YouTube-AdBlock.txt", false);

            Log.i("ADBLOCK", "loaded " + blockedHosts.size() + " hosts to block!");

            // load page
            h.post(() -> {
                w.loadUrl("https://music.youtube.com");
            });
        });


        b.btnYtmNext.setOnClickListener(v -> {
            w.evaluateJavascript("document.querySelectorAll('.next-button')[0].click()", null);
        });

        final Pattern ytIdPattern = Pattern.compile("(?:https?://)?(?:music.)?(?:youtube.com)(?:/.*watch?\\?)(?:.*)?(?:v=)([^&]+)(?:&)?(?:.*)?");
        b.btnYtmGetId.setOnClickListener(v -> {
            final Matcher m = ytIdPattern.matcher(w.getUrl());
            if(m.find())
            {
                Log.i("ID", m.group(1));
            }
        });

    }

    private void loadFilterList(String url, boolean rawFormat) {
        final Pattern pattern = Pattern.compile("(?:.* )?(.+\\..+)");
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {
            String host;
            while ((host = in.readLine()) != null) {
                // trim spaces
                host = host.trim();

                // check if comment
                if (host.startsWith("#") || host.startsWith("//")) {
                    continue;
                }

                // convert HOSTS format to raw
                if (!rawFormat) {
                    final Matcher m = pattern.matcher(host);
                    if (m.find()) {
                        host = m.group(1);
                    }
                }

                // add host to list
                blockedHosts.add(host.toLowerCase());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
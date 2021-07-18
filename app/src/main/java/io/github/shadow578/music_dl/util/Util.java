package io.github.shadow578.music_dl.util;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * general utility
 */
public final class Util {

    /**
     * youtube track ID regex.
     * CG1 = ID
     */
    private static final Pattern ID_PATTERN = Pattern.compile("(?:https?://)?(?:music.)?(?:youtube.com)(?:/.*watch?\\?)(?:.*)?(?:v=)([^&]+)(?:&)?(?:.*)?");

    /**
     * extract the track ID from a youtube (music) url (like [music.]youtube.com/watch?v=xxxxx)
     *
     * @param url the url to extract the id from
     * @return the id, or null if could not extract
     */
    @Nullable
    public static String extractTrackId(@NonNull String url) {
        final Matcher m = ID_PATTERN.matcher(url);
        if (m.find()) {
            return m.group(1);
        }

        return null;
    }

    /**
     * handler to run functions on the main thread.
     */
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    /**
     * run a function on the main thread
     *
     * @param runnable the function to run
     */
    public static void runOnMain(@NonNull Runnable runnable) {
        MAIN_HANDLER.post(runnable);
    }
}

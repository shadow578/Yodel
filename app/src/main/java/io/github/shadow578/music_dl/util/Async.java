package io.github.shadow578.music_dl.util;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.github.shadow578.music_dl.KtPorted;

/**
 * async operation utilities
 */
@SuppressWarnings("unused")
@KtPorted
public class Async {
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

    /**
     * run a function on the main thread
     *
     * @param runnable the function to run
     * @param delay    the delay, in milliseconds
     */
    public static void runLaterOnMain(@NonNull Runnable runnable, long delay) {
        MAIN_HANDLER.postDelayed(runnable, delay);
    }

    /**
     * executor for background operations
     */
    private static final Executor asyncExecutor = Executors.newCachedThreadPool();

    /**
     * run a function in the background
     *
     * @param runnable the function to run
     */
    public static void runAsync(@NonNull Runnable runnable) {
        asyncExecutor.execute(runnable);
    }
}

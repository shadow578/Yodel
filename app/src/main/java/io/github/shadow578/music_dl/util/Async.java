package io.github.shadow578.music_dl.util;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Async {
    //region runOnMain / runAsync
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
    //endregion
}

package io.github.shadow578.music_dl.util;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * general utility
 */
public final class Util {

    //region youtube util
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
     * create a youtube music watch link
     *
     * @param id the id of the track to watch
     * @return the full page link url
     */
    @NonNull
    public static String getYoutubeMusicWatchLink(@NonNull String id) {
        return Url.YoutubeMusicWatch.url() + id;
    }
    //endregion

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

    //region file / IO util

    /**
     * non- cryptographic random number generator
     */
    private static final Random random = new Random();

    /**
     * generate a random alphanumeric string with lenght characters
     *
     * @param length the length of the string to generate
     * @return the random string
     */
    @NonNull
    public static String generateRandomAlphaNumeric(int length) {
        final char[] CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++)
            sb.append(CHARS[random.nextInt(CHARS.length)]);

        return sb.toString();
    }

    /**
     * get a randomly named file in the parent directory. the file will not exist
     *
     * @param prefix          the prefix to the file name
     * @param suffix          the suffix to the file name
     * @param parentDirectory the parent directory to create the file in
     * @return the file, with randomized filename. the file is <b>not</b> created by this function
     */
    @NonNull
    public static File getTempFile(@NonNull String prefix, @NonNull String suffix, @NonNull File parentDirectory) {
        File tempFile;
        do {
            tempFile = new File(parentDirectory, prefix + generateRandomAlphaNumeric(32) + suffix);
        } while (tempFile.exists());
        return tempFile;
    }

    /**
     * copy a stream
     *
     * @param source     the source stream
     * @param target     the target stream
     * @param bufferSize the buffer size to use, in bytes. something like 1024 bytes should work fine
     * @return the total number of bytes transferred
     * @throws IOException if reading the source or writing the target fails
     */
    @SuppressWarnings("UnusedReturnValue")
    public static long streamTransfer(@NonNull InputStream source, @NonNull OutputStream target, int bufferSize) throws IOException {
        long totalBytes = 0;
        final byte[] buffer = new byte[bufferSize];
        int read;
        while ((read = source.read(buffer)) > 0) {
            target.write(buffer, 0, read);
            totalBytes += read;
        }

        return totalBytes;
    }
    //endregion


    /**
     * format a seconds value to HH:mm:ss or mm:ss format
     *
     * @param seconds the seconds value
     * @return the formatted string
     */
    @NonNull
    public static String secondsToTimeString(long seconds) {
        final long hours = seconds / 3600;
        if (hours <= 0) {
            // less than 1h, use mm:ss
            return String.format(Locale.US, "%02d:%02d",
                    (seconds % 3600) / 60,
                    seconds % 60);
        }

        // more than 1h, use HH:mm:ss
        return String.format(Locale.US, "%02d:%02d:%02d",
                hours,
                (seconds % 3600) / 60,
                seconds % 60);
    }
}

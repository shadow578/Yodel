package io.github.shadow578.music_dl.util.preferences;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;

/**
 * wrapper class for shared preferences. init before first use using {@link #init(SharedPreferences)}
 *
 * @param <T> the type of this preference
 */
public class PreferenceWrapper<T> {

    /**
     * internal gson reference. all values are internally saved as json
     */
    private static final Gson gson = new Gson();

    /**
     * the shared preferences to store values in
     */
    private static SharedPreferences prefs;

    /**
     * initialize all preference wrappers. you'll have to call this before using any preference
     *
     * @param prefs the shared preferences to wrap
     */
    public static void init(@NonNull SharedPreferences prefs) {
        PreferenceWrapper.prefs = prefs;
    }

    /**
     * create a new preference wrapper
     *
     * @param type         the type of the preference
     * @param key          the key of the preference
     * @param defaultValue the default value of the preference
     * @param <T>          type of the preference
     * @return the preference wrapper
     */
    @NonNull
    public static <T> PreferenceWrapper<T> create(@NonNull Class<T> type, @NonNull String key, @Nullable T defaultValue) {
        return new PreferenceWrapper<>(key, type, defaultValue);
    }

    /**
     * the internal type of this preference, used for gson deserialization
     */
    @NonNull
    private final Class<T> type;

    /**
     * the default value of the preference
     */
    @Nullable
    private final T defaultValue;

    /**
     * the preference key of the preference
     */
    @NonNull
    private final String key;

    /**
     * create a preference wrapper
     *
     * @param key          the preference key of the preference
     * @param type         the internal type of this preference, used for gson deserialization
     * @param defaultValue the default value of the preference
     */
    private PreferenceWrapper(@NonNull String key, @NonNull Class<T> type, @Nullable T defaultValue) {
        this.key = key;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    /**
     * get the value. if the preference is not set, uses the default value
     *
     * @return the preference value
     */
    @Nullable
    public T get() {
        return get(defaultValue);
    }

    /**
     * get the value. if the preference is not set, uses the provided value
     *
     * @return the preference value
     */
    @Nullable
    public T get(@Nullable T defaultValueOverwrite) {
        assertInit();
        final String json = prefs.getString(key, null);
        if (json == null || json.isEmpty()) {
            return defaultValueOverwrite;
        }

        final T value = gson.fromJson(json, type);
        if (value == null) {
            return defaultValueOverwrite;
        }

        return value;
    }

    /**
     * set the value of this preference
     *
     * @param value the value to set
     */
    public void set(@Nullable T value) {
        assertInit();

        // reset if value is null
        if (value == null) {
            reset();
            return;
        }

        // write value as json
        final String json = gson.toJson(value);
        prefs.edit()
                .putString(key, json)
                .apply();
    }

    /**
     * remove this preference from the values set
     */
    public void reset() {
        assertInit();
        prefs.edit()
                .remove(key)
                .apply();
    }

    /**
     * assert that the preference wrapper class was initialized. throws a exception if not
     */
    private void assertInit() {
        if (prefs == null) {
            throw new IllegalStateException("PreferenceWrapper must be initialized using .init() before you can use it!");
        }
    }
}

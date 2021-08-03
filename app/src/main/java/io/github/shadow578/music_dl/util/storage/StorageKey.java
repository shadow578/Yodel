package io.github.shadow578.music_dl.util.storage;

import androidx.annotation.NonNull;

import io.github.shadow578.music_dl.KtPorted;

/**
 * a storage key, used by {@link StorageHelper}
 */
@KtPorted
public class StorageKey {

    /**
     * a empty storage key
     */
    public static final StorageKey EMPTY = new StorageKey("");

    /**
     * the internal string key
     */
    private final String key;

    /**
     * create a new storage key
     *
     * @param key the key of the file
     */
    public StorageKey(String key) {
        this.key = key;
    }

    /**
     * get the key as a string
     *
     * @return the string key
     */
    @NonNull
    @Override
    public String toString() {
        return key;
    }
}

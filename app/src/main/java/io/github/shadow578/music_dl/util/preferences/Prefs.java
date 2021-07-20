package io.github.shadow578.music_dl.util.preferences;

import io.github.shadow578.music_dl.util.storage.StorageKey;

/**
 * app preferences storage
 */
public final class Prefs {

    /**
     * the main downloads directory file key
     */
    public static final PreferenceWrapper<StorageKey> DOWNLOADS_DIR = PreferenceWrapper.create(StorageKey.class, "downloads_dir", StorageKey.EMPTY);

    /**
     * for ScopedStorageTestActivity: the last file written
     */
    public static final PreferenceWrapper<StorageKey> LAST_FILE = PreferenceWrapper.create(StorageKey.class, "last_file", StorageKey.EMPTY);


}

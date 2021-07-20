package io.github.shadow578.music_dl;

import android.app.Application;
import android.preference.PreferenceManager;

import io.github.shadow578.music_dl.util.preferences.PreferenceWrapper;

public class MusicDLApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        PreferenceWrapper.init(PreferenceManager.getDefaultSharedPreferences(this));
    }
}

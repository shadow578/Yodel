package io.github.shadow578.music_dl.util;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.os.Build;
import android.os.LocaleList;

import androidx.annotation.NonNull;

import io.github.shadow578.music_dl.KtPorted;
import io.github.shadow578.music_dl.LocaleOverride;
import io.github.shadow578.music_dl.util.preferences.Prefs;

/**
 * locale utility class
 */
@KtPorted
public class LocaleUtil {
    /**
     * wrap the config to use the target locale from {@link Prefs#LocaleOverride}
     *
     * @param originalContext the original context to use as a base
     * @return the (maybe) wrapped context with the target locale
     */
    @NonNull
    public static Context wrapContext(@NonNull Context originalContext) {
        // get preference setting
        final LocaleOverride localeOverride = Prefs.LocaleOverride.get();

        // do no overrides when using system default
        if (localeOverride.equals(LocaleOverride.SystemDefault)) {
            return originalContext;
        }

        // create configuration with that locale
        final Configuration config = new Configuration(originalContext.getResources().getConfiguration());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(new LocaleList(localeOverride.locale()));
        } else {
            config.setLocale(localeOverride.locale());
        }

        // wrap the context
        return new ContextWrapper(originalContext.createConfigurationContext(config));
    }
}

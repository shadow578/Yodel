package io.github.shadow578.music_dl;


import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Locale;

/**
 * locale overrides
 */
@KtPorted
public enum LocaleOverride {

    SystemDefault,

    /**
     * english locale
     */
    English(new Locale("en", "")),

    /**
     * german (germany) locale
     */
    German(new Locale("de", "DE"));

    /**
     * the internal locale. if null use system default
     */
    @Nullable
    private final Locale locale;

    /**
     * extra: use system default locale
     */
    LocaleOverride() {
        this.locale = null;
    }

    /**
     * create a new locale override entry
     *
     * @param locale the locale to use in this override
     */
    LocaleOverride(@NonNull Locale locale) {
        this.locale = locale;
    }

    /**
     * @return locale for this override. invalid for {@link LocaleOverride#SystemDefault}
     */
    @NonNull
    public Locale locale() {
        if (locale == null) {
            throw new IllegalArgumentException("SystemDefault does not have a locale!");
        }

        return locale;
    }

    /**
     * @param ctx the context to work in
     * @return the display name of the locale override
     */
    @NonNull
    public String displayName(@NonNull Context ctx) {
        // check if this is FollowSystem
        if (this.equals(SystemDefault)) {
            return ctx.getString(R.string.locale_system_default);
        }

        // not SystemDefault, get locale name
        final Locale locale = locale();
        return locale.getDisplayName(locale);
    }
}

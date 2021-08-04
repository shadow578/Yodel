package io.github.shadow578.yodel

import android.content.Context
import java.util.*

/**
 * locale overrides
 *
 * @param locale local to use for the override
 */
enum class LocaleOverride(val locale: Locale? = null) {

    /**
     * follow system default locale
     */
    SystemDefault,

    /**
     * english locale
     */
    English(Locale("en", "")),

    /**
     * german (germany) locale
     */
    German(Locale("de", "DE"));

    /**
     * get the display name for this locale override
     *
     * @param ctx the context to work in
     * @return the display name of the locale override
     */
    fun getDisplayName(ctx: Context): String {
        // check if this is FollowSystem
        if (this == SystemDefault)
            return ctx.getString(R.string.locale_system_default)

        // for any other case throw a exception if locale == null
        // to make kotlin shut up
        if (locale == null)
            throw NullPointerException("locale null, but not SystemDefault!")

        // not SystemDefault, get locale name
        return locale.getDisplayName(locale)

    }
}
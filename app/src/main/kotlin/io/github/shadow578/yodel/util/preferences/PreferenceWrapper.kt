package io.github.shadow578.yodel.util.preferences

import android.content.SharedPreferences
import com.google.gson.Gson

/**
 * wrapper class for shared preferences. init before first use using [.init]
 *
 * @param <T> the type of this preference
 * @param type the type of this preference
 * @param key preference key
 * @param defaultValue default value to use in [get]
 */
//TODO use KClass<T> instead of java Class<T>
class PreferenceWrapper<T> private constructor(
    private val key: String,
    private val type: Class<T>,
    private val defaultValue: T
) {
    companion object {
        /**
         * internal gson reference. all values are internally saved as json
         */
        private val gson = Gson()

        /**
         * the shared preferences to store values in
         */
        private lateinit var prefs: SharedPreferences

        /**
         * initialize all preference wrappers. you'll have to call this before using any preference
         *
         * @param prefs the shared preferences to wrap
         */
        fun init(prefs: SharedPreferences) {
            Companion.prefs = prefs
        }

        /**
         * create a new preference wrapper
         *
         * @param type         the type of the preference
         * @param key          the key of the preference
         * @param defaultValue the default value of the preference
         * @param <T>          type of the preference
         * @return the preference wrapper
        </T> */
        fun <T> create(type: Class<T>, key: String, defaultValue: T): PreferenceWrapper<T> =
            PreferenceWrapper(key, type, defaultValue)
    }

    /**
     * get the value. if the preference is not set, uses the provided value
     *
     * @return the preference value
     */
    fun get(fallback: T = defaultValue): T {
        val json = prefs.getString(key, null)
        val value = if (json.isNullOrBlank()) fallback else gson.fromJson(json, type)
        return value ?: fallback
    }

    /**
     * set the value of this preference
     *
     * @param value the value to set. if null, the value is reset to default
     */
    fun set(value: T?) {
        // reset if value is null
        if (value == null) {
            reset()
            return
        }

        // write value as json
        val json = gson.toJson(value)
        prefs.edit()
            .putString(key, json)
            .apply()
    }

    /**
     * remove this preference from the values set
     */
    fun reset() {
        prefs.edit()
            .remove(key)
            .apply()
    }
}
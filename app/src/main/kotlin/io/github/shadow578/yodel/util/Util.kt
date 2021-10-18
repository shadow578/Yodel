package io.github.shadow578.yodel.util

import android.content.*
import android.content.res.Configuration
import android.net.Uri
import android.os.*
import androidx.core.content.FileProvider
import io.github.shadow578.yodel.LocaleOverride
import io.github.shadow578.yodel.util.preferences.Prefs
import java.io.File
import java.util.regex.Pattern

// region Youtube Util
/**
 * youtube full link ID regex.
 * CG1 = ID
 */
private val FULL_LINK_PATTERN =
    Pattern.compile("""(?:https?://)?(?:music.)?(?:youtube.com)(?:/.*watch?\?)(?:.*)?(?:v=)([^&]+)(?:&)?(?:.*)?""")

/**
 * youtube short link ID regex.
 * CG1 = ID
 */
private val SHORT_LINK_PATTERN =
    Pattern.compile("""(?:https?://)?(?:youtu.be/)([^&]+)(?:&)?(?:.*)?""")

/**
 * extract the track ID from a youtube (music) url (like (music.)youtube.com/watch?v=xxxxx)
 *
 * @param url the url to extract the id from
 * @return the id
 */
fun extractTrackId(url: String): String? {
    // first try full link
    var m = FULL_LINK_PATTERN.matcher(url)
    if (m.find())
        return m.group(1)

    // try short link
    m = SHORT_LINK_PATTERN.matcher(url)
    return if (m.find()) m.group(1) else null
}
//endregion

//region file / IO util
/**
 * generate a random alphanumeric string with length characters
 *
 * @param length the length of the string to generate
 * @return the random string
 */
fun generateRandomAlphaNumeric(length: Int): String {
    val chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
    val sb = StringBuilder()
    repeat(length) { sb.append(chars.random()) }
    return sb.toString()
}

/**
 * get a randomly named file with this directory as the parent directory. the file will not exist
 *
 * @param prefix          the prefix to the file name
 * @param suffix          the suffix to the file name
 * @return the file, with randomized filename. the file is **not** created by this function
 */
fun File.getTempFile(prefix: String, suffix: String): File {
    var tempFile: File
    do {
        tempFile = File(this, prefix + generateRandomAlphaNumeric(32) + suffix)
    } while (tempFile.exists())
    return tempFile
}
//endregion

/**
 * format a seconds value to HH:mm:ss or mm:ss format
 *
 * @return the formatted string
 */
fun Int.secondsToTimeString(): String {
    return this.toLong().secondsToTimeString()
}

/**
 * format a seconds value to HH:mm:ss or mm:ss format
 *
 * @return the formatted string
 */
fun Long.secondsToTimeString(): String {
    val hours = this / 3600
    return if (hours <= 0) {
        // less than 1h, use mm:ss
        "%01d:%02d".format(
            this % 3600 / 60,
            this % 60
        )
    } else {
        // more than 1h, use HH:mm:ss
        "%01d:%02d:%02d".format(
            hours,
            this % 3600 / 60,
            this % 60
        )
    }
}

/**
 * wrap the config to use the target locale from [LocaleOverride]
 *
 * @return the (maybe) wrapped context with the target locale
 */
fun Context.wrapLocale(): Context {
    // get preference setting
    val localeOverride = Prefs.AppLocaleOverride.get()

    // do no overrides when using system default
    if (localeOverride == LocaleOverride.SystemDefault)
        return this

    // create configuration with that locale
    val config = Configuration(this.resources.configuration)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        config.setLocales(LocaleList(localeOverride.locale))
    else
        config.setLocale(localeOverride.locale)

    // wrap the context
    return ContextWrapper(this.createConfigurationContext(config))
}

/**
 * copy a message to the clipboard
 *
 * @param label the label for the data
 * @param text the message to copy
 */
fun Context.copyToClipboard(label: String, text: String) {
    val clipManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val data = ClipData.newPlainText(label, text)
    clipManager.setPrimaryClip(data)
}

/**
 * create a content uri for the file using the global file provider.
 * the file's path has to be covered by provider_paths.xml
 */
fun Context.getContentUri(file: File): Uri {
    return FileProvider.getUriForFile(this, this.packageName + ".global_file_provider", file)
}

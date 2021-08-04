package io.github.shadow578.yodel.util

import android.content.Context
import androidx.annotation.StringRes
import androidx.core.app.*
import io.github.shadow578.yodel.R

/**
 * class to handle notification channels
 *
 * @param displayName display name resource
 * @param description description text resource
 * @param importance importance of this channel
 */
enum class NotificationChannels(
    @StringRes private val displayName: Int? = null,
    @StringRes private val description: Int? = null,
    @StringRes private val importance: Int = NotificationManagerCompat.IMPORTANCE_DEFAULT
) {
    /**
     * default notification channel.
     * <p>
     * only for use when testing stuff (and the actual channel is not setup yet) or for notifications that are normally not shown
     */
    Default(
        R.string.channel_default_name,
        R.string.channel_default_description
    ),

    /**
     * notification channel used by {@link io.github.shadow578.music_dl.downloader.DownloaderService} to show download progress
     */
    DownloadProgress(
        R.string.channel_downloader_name,
        R.string.channel_downloader_description,
        NotificationManagerCompat.IMPORTANCE_LOW
    );


    // region boring background stuff
    /**
     * the id of this channel definition
     */
    val id: String
        get() = "io.github.shadow578.youtube_dl.${name.uppercase()}"

    /**
     * create the notification channel from the definition
     *
     * @param ctx the context to resolve strings in
     * @return the channel, with id, name, desc and importance set
     */
    private fun createChannel(ctx: Context): NotificationChannelCompat {
        return NotificationChannelCompat.Builder(id, importance).apply {
            // set name with fallback
            setName(if (displayName != null) ctx.getString(displayName) else id)

            // set description
            setDescription(if (description != null) ctx.getString(description) else null)
        }.build()
    }

    companion object {
        /**
         * register all notification channels
         *
         * @param ctx the context to register in
         */
        fun registerAll(ctx: Context) {
            // get notification manager
            val notificationManager = NotificationManagerCompat.from(ctx)

            // register channels
            for (ch in values())
                notificationManager.createNotificationChannel(ch.createChannel(ctx))
        }
    }
    //endregion
}
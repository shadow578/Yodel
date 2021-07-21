package io.github.shadow578.music_dl.util.notifications;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationManagerCompat;

/**
 * class to handle notification channels
 */
@SuppressWarnings("unused")
public enum NotificationChannels {
    /**
     * default notification channel.
     * <p>
     * only for use when testing stuff (and the actual channel is not setup yet) or for notifications that are normally not shown
     */
    Default(),

    /**
     * notification channel used by {@link io.github.shadow578.music_dl.downloader.DownloaderService} to show download progress
     */
    DownloadProgress(NotificationManagerCompat.IMPORTANCE_LOW);

// region boring background stuff

    /**
     * prefix for notification channel IDs
     */
    private static final String ID_PREFIX = "io.github.shadow578.youtube_dl.";

    /**
     * display name of this channel.
     */
    @Nullable
    @StringRes
    private final Integer name;

    /**
     * display description of this channel.
     */
    @Nullable
    @StringRes
    private final Integer description;

    /**
     * importance int
     */
    @Nullable
    private final Integer importance;

    /**
     * define a new notification channel. the channel will have no description and a fallback name
     */
    NotificationChannels() {
        name = null;
        description = null;
        importance = null;
    }

    /**
     * define a new notification channel. the channel will have no description and a fallback name
     *
     * @param importance the importance of the channel
     */
    NotificationChannels(int importance) {
        name = null;
        description = null;
        this.importance = importance;
    }

    /**
     * define a new notification channel. the channel will have no description
     *
     * @param name       the display name of the channel
     * @param importance the importance of the channel
     */
    NotificationChannels(@StringRes int name, int importance) {
        this.name = name;
        description = null;
        this.importance = importance;
    }

    /**
     * define a new notification channel
     *
     * @param name        the display name of the channel
     * @param description the display description of the channel
     * @param importance  the importance of the channel
     */
    NotificationChannels(@StringRes int name, @StringRes int description, int importance) {
        this.name = name;
        this.description = description;
        this.importance = importance;
    }

    /**
     * @return id of this channel definition
     */
    @NonNull
    public String id() {
        return ID_PREFIX + name().toUpperCase();
    }

    /**
     * create the notification channel from the definition
     *
     * @param ctx the context to resolve strings in
     * @return the channel, with id, name, desc and importance set
     */
    @NonNull
    private NotificationChannelCompat createChannel(@NonNull Context ctx) {
        // create channel builder
        final int importance;
        if (this.importance != null) {
            importance = this.importance;
        } else {
            importance = NotificationManagerCompat.IMPORTANCE_DEFAULT;
        }
        final NotificationChannelCompat.Builder channelBuilder = new NotificationChannelCompat.Builder(id(), importance);

        // set name with fallback
        channelBuilder.setName(name != null ? ctx.getString(name) : id());

        // set description
        if (description != null) {
            channelBuilder.setDescription(ctx.getString(description));
        }

        return channelBuilder.build();
    }

    /**
     * register all notification channels
     *
     * @param ctx the context to register in
     */
    public static void registerAll(@NonNull Context ctx) {
        // get notification manager
        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(ctx);

        // register channels
        for (NotificationChannels ch : NotificationChannels.values()) {
            notificationManager.createNotificationChannel(ch.createChannel(ctx));
        }
    }
    //endregion
}

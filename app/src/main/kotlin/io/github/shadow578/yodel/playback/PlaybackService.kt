package io.github.shadow578.yodel.playback

import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.media.AudioManager
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.media.AudioFocusRequestCompat
import androidx.media.AudioManagerCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import io.github.shadow578.yodel.util.NotificationChannels
import io.github.shadow578.yodel.util.YodelForegroundService
import io.github.shadow578.yodel.util.storage.StorageKey
import io.github.shadow578.yodel.util.storage.decodeToUri


/**
 * playback background service
 */
class PlaybackService : YodelForegroundService(
        45,
        NotificationChannels.PlaybackInfo
) {
    companion object {

        const val EXTRA_PLAYBACK_URI_KEY = "io.github.shadow578.yodel.playback.PLAYBACK_URI_KEY"

    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return Binder()
    }

    inner class Binder : android.os.Binder() {

        fun play(key: StorageKey)
        {
            this@PlaybackService.play(key)
        }

    }


    lateinit var player: SimpleExoPlayer

    lateinit var playerNotificationManager: PlayerNotificationManager

    lateinit var mediaSessionConnector: MediaSessionConnector

    override fun onCreate() {
        super.onCreate()

        // create dummy foreground notification so we're officially running in foreground
        // if we don't do this, we stay as a background service and are killed with the rest of the app
        updateForeground(newNotification().build())

        // init player
        player = SimpleExoPlayer.Builder(this)
                .build()

        // create media session
        mediaSessionConnector = MediaSessionConnector(MediaSessionCompat(this, packageName))
                .apply {
                    setPlayer(player)
                    mediaSession.isActive = true
                }


        // create notification that is linked with the session
        playerNotificationManager = PlayerNotificationManager.Builder(this, notificationId, notificationChannel.id, DescriptionAdapter())
                .build()
                .apply {
                    setControlDispatcher(DefaultControlDispatcher(0, 0))
                    setPlayer(player)
                    setMediaSessionToken(mediaSessionConnector.mediaSession.sessionToken)
                }


        // request audio focus and listen for focus lost
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        AudioManagerCompat.requestAudioFocus(audioManager, AudioFocusRequestCompat.Builder(AudioManagerCompat.AUDIOFOCUS_GAIN)
                .setOnAudioFocusChangeListener {
                    Log.e("AudioFoc", "EVENT_ARGS: " + when (it) {
                        AudioManager.AUDIOFOCUS_GAIN -> "AUDIOFOCUS_GAIN"
                        AudioManager.AUDIOFOCUS_LOSS -> "AUDIOFOCUS_LOSS"
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> "AUDIOFOCUS_LOSS_TRANSIENT"
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK"
                        else -> "unknown"
                    })
                }
                .build())


    }

    private fun play(key: StorageKey) {
        val item = MediaItem.Builder()
                .setUri(key.decodeToUri()!!)
                .setMediaId("aabbccdd")
                .setMediaMetadata(MediaMetadata.Builder()
                        .setTitle("(Meta-Title)")
                        .setAlbumArtist("(Meta-AArtist)")
                        .build())
                .build()


        player.apply {
            stop()
            setMediaItem(item)
            prepare()
            play()
        }
    }

    inner class DescriptionAdapter : PlayerNotificationManager.MediaDescriptionAdapter {
        /**
         * Gets the content title for the current media item.
         *
         *
         * See [NotificationCompat.Builder.setContentTitle].
         *
         * @param player The [Player] for which a notification is being built.
         */
        override fun getCurrentContentTitle(player: Player): CharSequence {
            return player.currentMediaItem?.mediaMetadata?.title ?: "unknown"
            //return "(ContentTitle)"
        }

        /**
         * Creates a content intent for the current media item.
         *
         *
         * See [NotificationCompat.Builder.setContentIntent].
         *
         * @param player The [Player] for which a notification is being built.
         */
        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            return null
        }

        /**
         * Gets the content text for the current media item.
         *
         *
         * See [NotificationCompat.Builder.setContentText].
         *
         * @param player The [Player] for which a notification is being built.
         */
        override fun getCurrentContentText(player: Player): CharSequence? {
            return "(ContentText)"
        }

        /**
         * Gets the large icon for the current media item.
         *
         *
         * When a bitmap needs to be loaded asynchronously, a placeholder bitmap (or null) should be
         * returned. The actual bitmap should be passed to the [BitmapCallback] once it has been
         * loaded. Because the adapter may be called multiple times for the same media item, bitmaps
         * should be cached by the app and returned synchronously when possible.
         *
         *
         * See [NotificationCompat.Builder.setLargeIcon].
         *
         * @param player The [Player] for which a notification is being built.
         * @param callback A [BitmapCallback] to provide a [Bitmap] asynchronously.
         */
        override fun getCurrentLargeIcon(player: Player, callback: PlayerNotificationManager.BitmapCallback): Bitmap? {
            return null
        }

    }

}
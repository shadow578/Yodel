package io.github.shadow578.yodel.playback

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.AudioManager
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.media.AudioFocusRequestCompat
import androidx.media.AudioManagerCompat
import com.google.android.exoplayer2.DefaultControlDispatcher
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.MediaMetadata
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import io.github.shadow578.yodel.R
import io.github.shadow578.yodel.db.model.TrackInfo
import io.github.shadow578.yodel.util.NotificationChannels
import io.github.shadow578.yodel.util.YodelForegroundService
import io.github.shadow578.yodel.util.storage.decodeToUri


/**
 * playback background service
 */
class PlaybackService : YodelForegroundService(
        NOTIFICATION_ID,
        NOTIFICATION_CHANNEL
) {
    companion object {
        /**
         * logging tag
         */
        const val TAG = "PlaybackService"

        /**
         * id for the playback notification
         */
        const val NOTIFICATION_ID = 45

        /**
         * channel for the playback notification
         */
        val NOTIFICATION_CHANNEL = NotificationChannels.PlaybackInfo

        /**
         * create the service and bind it
         *
         * @param context the context to work in
         * @param serviceConnection the connection to bind to. may be null to just start the service
         */
        fun bind(context: Context, serviceConnection: ServiceConnection? = null) {
            val serviceIntent = Intent(context, PlaybackService::class.java)

            // startService is required for the playback service to be able to run independent of the activity
            context.startService(serviceIntent)

            // bind if we have a connection istance
            if (serviceConnection != null)
                context.bindService(serviceIntent, serviceConnection, Service.BIND_AUTO_CREATE or Service.BIND_ABOVE_CLIENT)
        }
    }

    // region binding
    override fun onBind(intent: Intent): IBinder {
        Log.i(TAG, "binding service...")
        super.onBind(intent)
        return Binder()
    }

    /**
     * service binder class. this acts as a interface between UI and Service
     */
    inner class Binder : android.os.Binder() {

        /**
         * play a single track, stopping ongoing playback if needed
         *
         * @param track the track to play
         */
        fun playSingle(track: TrackInfo) {
            play(track)
        }

    }
    //endregion

    /**
     * the main player instance
     */
    private lateinit var player: SimpleExoPlayer

    /**
     * the manager for the player notification
     */
    private lateinit var playerNotificationManager: PlayerNotificationManager

    /**
     * the connection between player and media session
     */
    private lateinit var mediaSessionConnector: MediaSessionConnector

    override fun onCreate() {
        super.onCreate()

        // do player and session initialization
        initPlayer()
        createMediaSession()
        createPlaybackNotification()


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

    override fun onDestroy() {
        mediaSessionConnector.mediaSession.isActive = false
        playerNotificationManager.setPlayer(null)
        player.release()
        super.onDestroy()
    }

    /**
     * play a single track, restarting the player if needed
     *
     * @param track the track to play
     */
    private fun play(track: TrackInfo) {
        Log.i(TAG, "playing single track $track")

        // create metadata
        val meta = MediaMetadata.Builder()
                .setTitle(track.title)
                .setArtist(track.artist)
                .setAlbumTitle(track.albumName)
                .setArtworkUri(track.coverKey.decodeToUri())
                .build()

        // create media
        val media = MediaItem.Builder()
                .setUri(track.audioFileKey.decodeToUri()!!)
                .setMediaId(track.id)
                .setMediaMetadata(meta)
                .build()

        // restart playback with new track
        player.apply {
            stop()
            setMediaItem(media)
            prepare()
            play()
        }
    }

    // region init
    /**
     * initialize the [player] instance
     */
    private fun initPlayer() {
        player = SimpleExoPlayer.Builder(this)
                .build()
    }

    /**
     * initialize the [mediaSessionConnector] and link it to [player]
     */
    private fun createMediaSession() {
        mediaSessionConnector = MediaSessionConnector(MediaSessionCompat(this, packageName))
                .apply {
                    setPlayer(player)
                    mediaSession.isActive = true
                }
    }

    /**
     * create the [playerNotificationManager] and link it to [player] and [mediaSessionConnector].
     * this also promotes the service to a foreground service
     */
    private fun createPlaybackNotification() {
        // create dummy foreground notification so we're officially running in foreground
        // if we don't do this, we stay as a background service and are killed with the rest of the app
        updateForeground(newNotification().build())

        // create adapter
        val adapter = MetadataDescriptionAdapter(this)

        // create notification
        playerNotificationManager = PlayerNotificationManager.Builder(this, notificationId, notificationChannel.id, adapter)
                .setSmallIconResourceId(R.drawable.ic_launcher_foreground)
                .build()
                .apply {
                    setControlDispatcher(DefaultControlDispatcher(0, 0))
                    setPlayer(player)
                    setMediaSessionToken(mediaSessionConnector.mediaSession.sessionToken)
                }
    }
    // endregion
}
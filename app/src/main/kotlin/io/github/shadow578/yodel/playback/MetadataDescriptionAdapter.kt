package io.github.shadow578.yodel.playback

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager

/**
 * description adapter that just uses the metadata given to the media item
 *
 * @param context context to work in
 * @param titleFallback fallback string to use if no title is found
 * @param contentIntent intent to use for [createCurrentContentIntent]
 */
class MetadataDescriptionAdapter(
        private val context: Context,
        private val titleFallback: String = "(Unknown)",
        private val contentIntent: PendingIntent? = null
) : PlayerNotificationManager.MediaDescriptionAdapter {
    override fun createCurrentContentIntent(player: Player): PendingIntent? {
        return contentIntent
    }

    override fun getCurrentContentTitle(player: Player): CharSequence {
        val meta = player.currentMediaItem?.mediaMetadata
        return meta?.title ?: titleFallback
    }

    override fun getCurrentContentText(player: Player): CharSequence? {
        val meta = player.currentMediaItem?.mediaMetadata
        val artist = meta?.artist
        val album = meta?.albumTitle

        // show artist + album whenever possible. if one of the two is missing, just show one
        // if both are missing, show nothing
        //TODO hardcoded text
        return if (artist != null && album != null)
            "$artist • $album"
        else artist ?: album
    }

    override fun getCurrentLargeIcon(player: Player, callback: PlayerNotificationManager.BitmapCallback): Bitmap? {
        val meta = player.currentMediaItem?.mediaMetadata

        // load artwork using glide
        // prefer preloaded image (artworkData), then use uri
        when {
            meta?.artworkData != null -> {
                Glide.with(context)
                        .asBitmap()
                        .load(meta.artworkData)
            }
            meta?.artworkUri != null -> {
                Glide.with(context)
                        .asBitmap()
                        .load(meta.artworkUri)
            }
            else -> null
        }?.into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                callback.onBitmap(resource)
            }

            override fun onLoadCleared(placeholder: Drawable?) {
                //TODO this may cause a memory leak or something?
            }
        })

        // we always load async, so no bitmap is returned here
        return null
    }
}
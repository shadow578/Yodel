package io.github.shadow578.yodel.ui

import android.content.Context
import androidx.appcompat.app.AlertDialog
import io.github.shadow578.yodel.R
import io.github.shadow578.yodel.db.TracksDB
import io.github.shadow578.yodel.db.model.TrackInfo
import io.github.shadow578.yodel.util.*

/**
 * helper class for inserting tracks into the db from UI
 */
object InsertTrackUIHelper {
    /**
     * insert a new, not yet downloaded track into the db.
     * if the track already exists, displays a dialog to replace it
     *
     * @param ctx   the context to work in
     * @param id    the track id
     * @param title the track title
     */
    fun insertTrack(ctx: Context, id: String, title: String?) {
        var trackTitle = title
        val fallbackTitle = ctx.getString(R.string.fallback_title)
        if (trackTitle == null || trackTitle.isEmpty()) {
            trackTitle = fallbackTitle
        }

        launchIO {
            // check if track already in db
            // if yes, show a dialog prompting the user to replace the existing track
            val existingTrack = TracksDB.get(ctx).tracks()[id]
            if (existingTrack != null) {
                launchMain {
                    showReplaceDialog(
                        ctx,
                        id,
                        existingTrack.title
                    )
                }
            } else {
                insert(ctx, id, trackTitle)
            }
        }
    }

    /**
     * show a dialog prompting the user if he wants to replace a existing track
     * has to be called on main ui thread
     *
     * @param ctx   the context to work in
     * @param id    the track id
     * @param title the track title
     */
    private fun showReplaceDialog(ctx: Context, id: String, title: String) {
        AlertDialog.Builder(ctx)
            .setTitle(R.string.tracks_replace_existing_title)
            .setMessage(ctx.getString(R.string.tracks_replace_existing_message, title))
            .setPositiveButton(R.string.tracks_replace_existing_positive) { dialog, _ ->
                launchIO {
                    insert(
                        ctx,
                        id,
                        title
                    )
                }
                dialog.dismiss()
            }
            .setNegativeButton(R.string.tracks_replace_existing_negative) { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    /**
     * insert a new, not yet downloaded track into the db.
     * has to be called on a background thread
     *
     * @param ctx   the context to work in
     * @param id    the track id
     * @param title the track title
     */
    private fun insert(ctx: Context, id: String, title: String) {
        TracksDB.get(ctx).tracks().insert(TrackInfo(id, title))
    }
}
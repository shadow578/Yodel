package io.github.shadow578.music_dl.ui;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import io.github.shadow578.music_dl.KtPorted;
import io.github.shadow578.music_dl.R;
import io.github.shadow578.music_dl.db.TracksDB;
import io.github.shadow578.music_dl.db.model.TrackInfo;
import io.github.shadow578.music_dl.util.Async;

/**
 * helper class for inserting tracks into the db from UI
 */
@KtPorted
public class InsertTrackUIHelper {

    /**
     * insert a new, not yet downloaded track into the db.
     * if the track already exists, displays a dialog to replace it
     *
     * @param ctx   the context to work in
     * @param id    the track id
     * @param title the track title
     */
    public static void insertTrack(@NonNull Context ctx, @NonNull String id, @Nullable String title) {
        final String fallbackTitle = ctx.getString(R.string.fallback_title);
        if (title == null || title.isEmpty()) {
            title = fallbackTitle;
        }
        final String titleF = title;

        Async.runAsync(() -> {
            // check if track already in db
            // if yes, show a dialog prompting the user to replace the existing track
            final TrackInfo existingTrack = TracksDB.init(ctx).tracks().get(id);
            if (existingTrack != null) {
                Async.runOnMain(() -> showReplaceDialog(ctx, id, existingTrack.title));
            } else {
                insert(ctx, id, titleF);
            }
        });
    }

    /**
     * show a dialog prompting the user if he wants to replace a existing track
     * has to be called on main ui thread
     *
     * @param ctx   the context to work in
     * @param id    the track id
     * @param title the track title
     */
    private static void showReplaceDialog(@NonNull Context ctx, @NonNull String id, @NonNull String title) {
        new AlertDialog.Builder(ctx)
                .setTitle(R.string.tracks_replace_existing_title)
                .setMessage(ctx.getString(R.string.tracks_replace_existing_message, title))
                .setPositiveButton(R.string.tracks_replace_existing_positive, (dialog, w) -> {
                    Async.runAsync(() -> insert(ctx, id, title));
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.tracks_replace_existing_negative, (dialog, w) -> dialog.dismiss())
                .create()
                .show();

    }

    /**
     * insert a new, not yet downloaded track into the db.
     * has to be called on a background thread
     *
     * @param ctx   the context to work in
     * @param id    the track id
     * @param title the track title
     */
    private static void insert(@NonNull Context ctx, @NonNull String id, @NonNull String title) {
        TracksDB.init(ctx).tracks().insert(TrackInfo.createNew(id, title));
    }
}

package io.github.shadow578.music_dl.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import io.github.shadow578.music_dl.KtPorted;

/**
 * swipe to delete handler for {@link ItemTouchHelper}
 */
@KtPorted
public abstract class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

    /**
     * background drawable
     */
    private final Drawable background;

    /**
     * icon to draw
     */
    private final Drawable icon;

    /**
     * margins of the icon
     */
    private final int iconMargin;

    /**
     * create a new handler
     *
     * @param ctx             context to work in
     * @param backgroundColor background drawable
     * @param iconRes         icon to draw
     * @param iconTint        icon tint color
     * @param iconMarginDp    margins of the icon, in dp
     */
    public SwipeToDeleteCallback(@NonNull Context ctx, @ColorInt int backgroundColor, @DrawableRes int iconRes, @ColorInt int iconTint, int iconMarginDp) {
        super(0, ItemTouchHelper.LEFT);

        // create background color drawable
        background = new ColorDrawable(backgroundColor);

        // load icon drawable
        icon = ContextCompat.getDrawable(ctx, iconRes);
        if (icon == null) {
            throw new IllegalArgumentException("could not load icon resource");
        }

        icon.setColorFilter(iconTint, PorterDuff.Mode.SRC_ATOP);

        // load margin
        iconMargin = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                iconMarginDp,
                ctx.getResources().getDisplayMetrics());
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        // don't care
        return false;
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        // skip for views outside of view
        if (viewHolder.getAdapterPosition() < 0) {
            return;
        }

        final View view = viewHolder.itemView;

        // draw the red background
        background.setBounds((int) (view.getRight() + dX),
                view.getTop(),
                view.getRight(),
                view.getBottom());
        background.draw(c);

        // calculate icon bounds
        final int iconLeft = view.getRight() - iconMargin - icon.getIntrinsicWidth();
        final int iconRight = view.getRight() - iconMargin;
        final int iconTop = view.getTop() + ((view.getHeight() - icon.getIntrinsicHeight()) / 2);
        final int iconBottom = iconTop + icon.getIntrinsicHeight();

        // draw icon
        icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
        icon.draw(c);

        // do normal stuff, idk
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
}

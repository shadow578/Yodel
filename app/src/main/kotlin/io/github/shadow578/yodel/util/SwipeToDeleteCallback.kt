package io.github.shadow578.yodel.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

/**
 * swipe to delete handler for [ItemTouchHelper]
 *
 * @param ctx             context to work in
 * @param backgroundColor background drawable
 * @param iconRes         icon to draw
 * @param iconTint        icon tint color
 * @param iconMarginDp    margins of the icon, in dp
 */
abstract class SwipeToDeleteCallback(
    ctx: Context,
    @ColorInt backgroundColor: Int,
    @DrawableRes iconRes: Int,
    @ColorInt iconTint: Int,
    iconMarginDp: Int
) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
    /**
     * background drawable
     */
    private val background: Drawable

    /**
     * icon to draw
     */
    private val icon: Drawable

    /**
     * margins of the icon
     */
    private val iconMargin: Int

    init {
        // create background color drawable
        background = ColorDrawable(backgroundColor)

        // load icon drawable
        icon = ContextCompat.getDrawable(ctx, iconRes)!!
        icon.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
            iconTint,
            BlendModeCompat.SRC_ATOP
        )

        // get margin
        iconMargin = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            iconMarginDp.toFloat(),
            ctx.resources.displayMetrics
        ).toInt()
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        // don't care
        return false
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        // skip for views outside of view
        if (viewHolder.absoluteAdapterPosition < 0) return
        val view = viewHolder.itemView

        // draw the red background
        background.setBounds(
            (view.right + dX).toInt(),
            view.top,
            view.right,
            view.bottom
        )
        background.draw(c)

        // calculate icon bounds
        val iconLeft = view.right - iconMargin - icon.intrinsicWidth
        val iconRight = view.right - iconMargin
        val iconTop = view.top + (view.height - icon.intrinsicHeight) / 2
        val iconBottom = iconTop + icon.intrinsicHeight

        // draw icon
        icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
        icon.draw(c)

        // do normal stuff, idk
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}

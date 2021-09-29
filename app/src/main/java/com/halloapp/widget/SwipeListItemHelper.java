package com.halloapp.widget;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public abstract class SwipeListItemHelper extends ItemSwipeHelper.SimpleCallback {

    private final float SPRING_CONSTANT = 0.05f;

    private final Drawable icon;
    private final int backgroundColor;
    private final int iconMargin;

    private @ColorInt int iconColor;

    private final RectF tmpRect = new RectF();
    private final Rect tempR = new Rect();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private boolean hapticSent = false;

    public interface SwipeableViewHolder {
        View getSwipeView();
    }

    protected SwipeListItemHelper(@NonNull Drawable icon, int backgroundColor, int iconMargin) {
        super(ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.icon = icon;
        this.backgroundColor = backgroundColor;
        this.iconMargin = iconMargin;
    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        return 0.2f;
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        hapticSent = false;
    }

    @Override
    public void onChildDraw(@NonNull Canvas canvas, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (!canSwipe(viewHolder)) {
            return;
        }

        final View itemView = viewHolder.itemView;
        final View swipeView = (viewHolder instanceof SwipeableViewHolder) ? ((SwipeableViewHolder) viewHolder).getSwipeView() : viewHolder.itemView;

        swipeView.getDrawingRect(tempR);
        ((ViewGroup)itemView.getParent()).offsetDescendantRectToMyCoords(swipeView, tempR);
        final int iconTop = tempR.top + (swipeView.getHeight() - icon.getIntrinsicHeight()) / 2;
        final int iconBottom = iconTop + icon.getIntrinsicHeight();

        final float thresholdDist = recyclerView.getWidth() * getSwipeThreshold(viewHolder);

        if (Math.abs(dX) > thresholdDist) {
            if (!hapticSent) {
                viewHolder.itemView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                hapticSent = true;
            }
            float springX = (float) Math.sqrt((Math.abs(dX) - thresholdDist) * 2 / SPRING_CONSTANT);
            if (dX > 0) {
                dX = thresholdDist + springX;
            } else {
                dX = -thresholdDist - springX;
            }
        }

        swipeView.setTranslationX(dX);
        swipeView.setTranslationY(dY);
        //super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        if (iconColor != 0) {
            icon.setTint(iconColor);
        } else {
            icon.setTintList(null);
        }

        int iconWidth = icon.getIntrinsicWidth();
        int alpha = Math.min(255, (int) (255 * ((Math.abs(dX) / 2f) / iconWidth)));
        icon.setAlpha(alpha);

        if (dX > 0) {
            final int iconLeft = viewHolder.itemView.getLeft() + iconMargin;
            final int iconRight = iconLeft + iconWidth;
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            tmpRect.set(viewHolder.itemView.getLeft(), tempR.top, itemView.getLeft() + dX, tempR.bottom);
        } else if (dX < 0) {
            final int iconRight = viewHolder.itemView.getRight() - iconMargin;
            final int iconLeft = iconRight - iconWidth;
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            tmpRect.set(itemView.getRight() + dX, tempR.top, itemView.getRight(), tempR.bottom);
        } else {
            tmpRect.set(0, 0, 0, 0);
        }
        if (!tmpRect.isEmpty()) {
            paint.setColor(backgroundColor);
            canvas.drawRect(tmpRect, paint);
            icon.draw(canvas);
        }
    }

    public void setIconTint(@ColorInt int color) {
        this.iconColor = color;
    }

    protected boolean canSwipe(@NonNull RecyclerView.ViewHolder viewHolder) {
        return true;
    }
}

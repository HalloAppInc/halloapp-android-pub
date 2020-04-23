package com.halloapp.widget;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public abstract class SwipeListItemHelper extends ItemTouchHelper.SimpleCallback {

    private final Drawable icon;
    private final int backgroundColor;
    private final int iconMargin;

    private final RectF tmpRect = new RectF();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    protected SwipeListItemHelper(@NonNull Drawable icon, int backgroundColor, int iconMargin) {
        super(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.icon = icon;
        this.backgroundColor = backgroundColor;
        this.iconMargin = iconMargin;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onChildDraw(@NonNull Canvas canvas, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (!canSwipe(viewHolder)) {
            return;
        }

        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        final View itemView = viewHolder.itemView;

        final int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        final int iconBottom = iconTop + icon.getIntrinsicHeight();

        if (dX > 0) {
            final int iconLeft = itemView.getLeft() + iconMargin;
            final int iconRight = iconLeft + icon.getIntrinsicWidth();
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            tmpRect.set(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + dX, itemView.getBottom());
        } else if (dX < 0) {
            final int iconRight = itemView.getRight() - iconMargin;
            final int iconLeft = iconRight - icon.getIntrinsicWidth();
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            tmpRect.set(itemView.getRight() + dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
        } else {
            tmpRect.set(0, 0, 0, 0);
        }
        if (!tmpRect.isEmpty()) {
            paint.setColor(backgroundColor);
            canvas.drawRect(tmpRect, paint);
            icon.draw(canvas);
        }
    }

    protected boolean canSwipe(@NonNull RecyclerView.ViewHolder viewHolder) {
        return true;
    }
}

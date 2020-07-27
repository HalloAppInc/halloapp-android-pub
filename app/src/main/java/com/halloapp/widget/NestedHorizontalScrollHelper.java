package com.halloapp.widget;

import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Helper class for fixing scroll issues with nested RecyclerViews.
 *
 * Allows you to specify a ratio such that you must scroll {ratio} times more vertically
 * than horizontally to trigger a vertical scroll.
 *
 * This gives you a pseudo angle that determines when a scroll should be vertical or horizontal
 */
public class NestedHorizontalScrollHelper extends RecyclerView.OnScrollListener implements RecyclerView.OnItemTouchListener {

    private static final float DEFAULT_SCROLL_RATIO = 1.3f;

    private int scrollState = RecyclerView.SCROLL_STATE_IDLE;
    private int scrollPointerId = -1;
    private int initialTouchX = 0;
    private int initialTouchY = 0;
    private int dx = 0;
    private int dy = 0;

    private final float ratio;

    public NestedHorizontalScrollHelper(float ratio) {
        this.ratio = ratio;
    }

    public NestedHorizontalScrollHelper() {
        this(DEFAULT_SCROLL_RATIO);
    }

    public static void applyScrollRatio(@NonNull RecyclerView recyclerView, float ratio) {
        NestedHorizontalScrollHelper horizontalScrollHelper = new NestedHorizontalScrollHelper(ratio);
        recyclerView.addOnScrollListener(horizontalScrollHelper);
        recyclerView.addOnItemTouchListener(horizontalScrollHelper);
    }

    public static void applyDefaultScrollRatio(@NonNull RecyclerView recyclerView) {
        applyScrollRatio(recyclerView, DEFAULT_SCROLL_RATIO);
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                scrollPointerId = e.getPointerId(0);
                initialTouchX = Math.round(e.getX() + 0.5f);
                initialTouchY = Math.round(e.getY() + 0.5f);
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                int actionIndex = e.getActionIndex();
                scrollPointerId = e.getPointerId(actionIndex);
                initialTouchX = Math.round(e.getX(actionIndex) + 0.5f);
                initialTouchY = Math.round(e.getY(actionIndex) + 0.5f);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                int index = e.findPointerIndex(scrollPointerId);
                if (index >= 0 && scrollState != RecyclerView.SCROLL_STATE_DRAGGING) {
                    int x = Math.round(e.getX(index) + 0.5f);
                    int y = Math.round(e.getY(index) + 0.5f);
                    dx = x - initialTouchX;
                    dy = y - initialTouchY;
                }
                break;
            }

        }
        return false;
    }

    @Override
    public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }

    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        int oldState = scrollState;
        scrollState = newState;
        if (oldState == RecyclerView.SCROLL_STATE_IDLE && newState == RecyclerView.SCROLL_STATE_DRAGGING) {
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            if (layoutManager != null) {
                boolean canScrollHorizontally = layoutManager.canScrollHorizontally();
                boolean canScrollVertically = layoutManager.canScrollVertically();
                if (canScrollHorizontally != canScrollVertically) {
                    if ((canScrollVertically && ratio * Math.abs(dx) > Math.abs(dy))) {
                        recyclerView.stopScroll();
                    }
                }
            }
        }
    }
}

package com.halloapp.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class LongPressInterceptView extends FrameLayout {

    private GestureDetector gestureDetector;

    public LongPressInterceptView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public LongPressInterceptView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LongPressInterceptView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private OnLongClickListener longClickListener;

    @Override
    public void setOnLongClickListener(@Nullable OnLongClickListener l) {
        this.longClickListener = l;
    }

    private void init(Context context) {
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                triggerLongClick();
            }

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }
        });

        setOnTouchListener((v, e) -> gestureDetector.onTouchEvent(e));
    }

    private void triggerLongClick() {
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_LONG_CLICKED);

        boolean handled = false;
        if (longClickListener != null) {
            handled = longClickListener.onLongClick(LongPressInterceptView.this);
        }
        if (handled) {
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        }
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(disallowIntercept);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onInterceptTouchEvent(event);
    }
}

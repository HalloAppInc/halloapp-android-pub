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

public class PressInterceptView extends FrameLayout {

    private GestureDetector gestureDetector;

    public PressInterceptView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public PressInterceptView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PressInterceptView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private OnLongClickListener longClickListener;
    private OnClickListener clickListener;

    @Override
    public void setOnLongClickListener(@Nullable OnLongClickListener l) {
        this.longClickListener = l;
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener c) {
        this.clickListener = c;
    }

    private void init(Context context) {
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                triggerLongClick();
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                triggerClick();
                return true;
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
            handled = longClickListener.onLongClick(PressInterceptView.this);
        }
        if (handled) {
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        }
    }

    private void triggerClick() {
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_CLICKED);

        if (clickListener != null) {
            clickListener.onClick(PressInterceptView.this);
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

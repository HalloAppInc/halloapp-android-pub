package com.halloapp.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public class SeenDetectorLayout extends LinearLayout {

    private OnSeenListener listener;

    public interface OnSeenListener {
        void onSeen();
    }

    public SeenDetectorLayout(Context context) {
        super(context);
    }

    public SeenDetectorLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SeenDetectorLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SeenDetectorLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setOnSeenListener(OnSeenListener listener) {
        this.listener = listener;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (listener != null) {
            listener.onSeen();
        }
    }
}
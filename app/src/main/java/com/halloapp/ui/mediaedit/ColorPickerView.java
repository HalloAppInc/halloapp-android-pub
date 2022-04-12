package com.halloapp.ui.mediaedit;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewOutlineProvider;

import androidx.annotation.Nullable;

import com.halloapp.util.logs.Log;

public class ColorPickerView extends View {
    @FunctionalInterface
    public interface ColorUpdateListener {
        void onUpdate(int color);
    }

    private static final int[] COLORS = new int[] {
            Color.rgb(255, 69, 0),
            Color.rgb(255, 138, 0),
            Color.rgb(241, 243, 120),
            Color.rgb(140, 220, 77),
            Color.rgb(85, 210, 203),
            Color.rgb(63, 119, 227),
            Color.rgb(239, 50, 163),
            Color.rgb(222, 51, 237),
            Color.rgb(34, 34, 34),
            Color.rgb(255, 255, 255),
    };
    private static final float[] POSITIONS = new float[] {0, 0.11f, 0.21f, 0.35f, 0.48f, 0.62f, 0.72f, 0.79f, 0.89f, 0.96f};

    private final Paint paint = new Paint();
    private final RectF rect = new RectF();
    private int color = Color.RED;
    private ColorUpdateListener colorUpdateListener;

    public ColorPickerView(Context context) {
        super(context);
        init();
    }

    public ColorPickerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ColorPickerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ColorPickerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        // required for elevation (and shadow) to work
        setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                float radius = (float) view.getWidth() / 2;
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
            }
        });
        setClipToOutline(true);
    }

    public int getColor() {
        return color;
    }

    public void setColorUpdateListener(ColorUpdateListener listener) {
        colorUpdateListener = listener;
    }

    private void notifyColorUpdated() {
        if (colorUpdateListener != null) {
            colorUpdateListener.onUpdate(color);
        }
    }

    private int getColor(float y) {
        float position = (getHeight() - Math.min(getHeight(), Math.max(0, y))) / getHeight();
        int index = 0;

        for (int i = 0; i < POSITIONS.length; ++i) {
            if (POSITIONS[i] >= position) {
                break;
            }
            index = i;
        }
        index = Math.min(index, POSITIONS.length - 2);

        float fraction = (position - POSITIONS[index]) / (POSITIONS[index + 1] - POSITIONS[index]);
        fraction = Math.max(0, Math.min(fraction, 1));

        return (Integer)new ArgbEvaluator().evaluate(fraction, COLORS[index], COLORS[index + 1]);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_MOVE || action == MotionEvent.ACTION_DOWN) {
            color = getColor(event.getY());
            notifyColorUpdated();
        }

        return true;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        paint.setShader(new LinearGradient(0, getHeight(), 0, 0, COLORS, POSITIONS, Shader.TileMode.CLAMP));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float radius = (float)getWidth() / 2;
        rect.set(0, 0, getWidth(), getHeight());
        canvas.drawRoundRect(rect, radius, radius, paint);
    }
}

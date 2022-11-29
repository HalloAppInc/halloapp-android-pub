package com.halloapp.newapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class JellybeanClipView extends FrameLayout {

    private static final float OVAL_HEIGHT = 0.7f;
    private static final float OVAL_ROTATE_DEG = -12;

    private final Path path = new Path();
    private final Matrix matrix = new Matrix();

    private int lastWidth;
    private int lastHeight;

    public JellybeanClipView(@NonNull Context context) {
        super(context);

        setWillNotDraw(false);
    }

    public JellybeanClipView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        setWillNotDraw(false);
    }

    public JellybeanClipView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setWillNotDraw(false);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (lastWidth != w || lastHeight != h) {
            path.reset();
            matrix.reset();

            final float halfWidth = w / 2f;
            final float halfHeight = h / 2f;
            final float radius = Math.max(halfWidth, halfHeight);

            path.addCircle(halfWidth, halfHeight, radius, Path.Direction.CCW);
            matrix.postScale(1, OVAL_HEIGHT, halfWidth, halfHeight);
            matrix.postRotate(OVAL_ROTATE_DEG, halfWidth, halfHeight);
            path.transform(matrix);

            lastWidth = w;
            lastHeight = h;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.clipPath(path);

        super.onDraw(canvas);
    }
}

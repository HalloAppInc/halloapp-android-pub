package com.halloapp.katchup;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.R;

public class JellybeanClipView extends FrameLayout {

    private static final float OVAL_HEIGHT = 0.7f;
    private static final int OVAL_ROTATE_DEG = -12;

    private final Path path = new Path();
    private final Matrix matrix = new Matrix();

    private int lastWidth;
    private int lastHeight;

    private int rotateAngle = OVAL_ROTATE_DEG;

    public JellybeanClipView(@NonNull Context context) {
        super(context);
        init(null, 0);
    }

    public JellybeanClipView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public JellybeanClipView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(@Nullable AttributeSet attrs, int defStyleAttr) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.JellybeanClipView, defStyleAttr, 0);
        rotateAngle = a.getInt(R.styleable.JellybeanClipView_jcvRotation, rotateAngle);
        a.recycle();

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
            matrix.postRotate(rotateAngle, halfWidth, halfHeight);
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

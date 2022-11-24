package com.halloapp.newapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

public class NewAppAvatarView extends androidx.appcompat.widget.AppCompatImageView {

    private static final float OVAL_HEIGHT = 0.7f;
    private static final float OVAL_ROTATE_DEG = -12;

    private final Path path = new Path();
    private final Matrix matrix = new Matrix();

    public NewAppAvatarView(Context context) {
        super(context);
    }

    public NewAppAvatarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public NewAppAvatarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onDraw(Canvas canvas) {
        path.reset();
        matrix.reset();

        final float halfWidth = canvas.getWidth()/2;
        final float halfHeight = canvas.getHeight()/2;
        final float radius = Math.max(halfWidth, halfHeight);

        path.addCircle(halfWidth, halfHeight, radius, Path.Direction.CCW);
        matrix.postScale(1, OVAL_HEIGHT, halfWidth, halfHeight);
        matrix.postRotate(OVAL_ROTATE_DEG, halfWidth, halfHeight);
        path.transform(matrix);
        canvas.clipPath(path);

        super.onDraw(canvas);
    }
}

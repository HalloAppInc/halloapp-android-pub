package com.halloapp.newapp;

import android.graphics.Matrix;
import android.graphics.Outline;
import android.graphics.Path;
import android.os.Build;
import android.view.View;
import android.view.ViewOutlineProvider;

public class JellybeanOutlineProvider extends ViewOutlineProvider {

    private static final float OVAL_HEIGHT = 0.7f;
    private static final float OVAL_ROTATE_DEG = -12;

    private final Path path = new Path();
    private final Matrix matrix = new Matrix();

    private int lastWidth;
    private int lastHeight;

    @Override
    public void getOutline(View view, Outline outline) {
        int viewWidth = view.getWidth();
        int viewHeight = view.getHeight();

        if (lastWidth != viewWidth || lastHeight != viewHeight) {
            path.reset();
            matrix.reset();

            final float halfWidth = viewWidth/2f;
            final float halfHeight = viewHeight/2f;
            final float radius = Math.max(halfWidth, halfHeight);

            path.addCircle(halfWidth, halfHeight, radius, Path.Direction.CCW);
            matrix.postScale(1, OVAL_HEIGHT, halfWidth, halfHeight);
            matrix.postRotate(OVAL_ROTATE_DEG, halfWidth, halfHeight);
            path.transform(matrix);

            lastWidth = viewWidth;
            lastHeight = viewHeight;
        }
        if (Build.VERSION.SDK_INT >= 30) {
            outline.setPath(path);
        } else {
            outline.setConvexPath(path);
        }
    }
}

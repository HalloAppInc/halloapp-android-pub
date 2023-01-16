package com.halloapp.katchup;

import android.graphics.Matrix;
import android.graphics.Outline;
import android.graphics.Path;
import android.os.Build;
import android.view.View;
import android.view.ViewOutlineProvider;

import com.halloapp.Constants;

public class JellybeanOutlineProvider extends ViewOutlineProvider {

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
            matrix.postScale(1, Constants.PROFILE_PHOTO_OVAL_HEIGHT_RATIO, halfWidth, halfHeight);
            matrix.postRotate(Constants.PROFILE_PHOTO_OVAL_DEG, halfWidth, halfHeight);
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

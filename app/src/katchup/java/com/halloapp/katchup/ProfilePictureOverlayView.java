package com.halloapp.katchup;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.halloapp.Constants;
import com.halloapp.R;

public class ProfilePictureOverlayView extends View {

    private final int color = getResources().getColor(R.color.black_30);
    private final Path path = new Path();
    private final Matrix matrix = new Matrix();

    public ProfilePictureOverlayView(Context context) {
        super(context);
    }

    public ProfilePictureOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ProfilePictureOverlayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ProfilePictureOverlayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        path.reset();
        matrix.reset();

        path.setFillType(Path.FillType.INVERSE_EVEN_ODD);

        final float halfWidth = w / 2f;
        final float halfHeight = h / 2f;
        final float radius = Math.max(halfWidth, halfHeight);

        path.addCircle(halfWidth, halfHeight, radius, Path.Direction.CCW);
        matrix.postScale(1, Constants.PROFILE_PHOTO_OVAL_HEIGHT_RATIO, halfWidth, halfHeight);
        matrix.postRotate(Constants.PROFILE_PHOTO_OVAL_DEG, halfWidth, halfHeight);
        path.transform(matrix);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        canvas.save();
        canvas.clipPath(path);
        canvas.drawColor(color);
        canvas.restore();
    }
}

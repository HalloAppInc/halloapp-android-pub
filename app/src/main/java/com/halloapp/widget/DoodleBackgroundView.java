package com.halloapp.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.halloapp.R;

public class DoodleBackgroundView extends FrameLayout {

    private static final int INITIAL_RADIAL_ALPHA = 102;
    private static final int FINAL_RADIAL_ALPHA = 51;
    private static final int INITIAL_LINEAR_ALPHA = 41;
    private static final int FINAL_LINEAR_ALPHA = 77;

    private float radialScale = 1;
    private int radialOffset;

    private BitmapDrawable doodle;
    private Drawable radial;
    private Drawable linear;

    private int rows;
    private int cols;

    private int doodleWidth;
    private int doodleHeight;

    private final Paint overlayPaint = new Paint();

    public DoodleBackgroundView(@NonNull Context context) {
        this(context, null, 0);
    }

    public DoodleBackgroundView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DoodleBackgroundView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    private void init(@NonNull Context context) {
        doodle = (BitmapDrawable) AppCompatResources.getDrawable(context, R.drawable.doodle);

        radial = AppCompatResources.getDrawable(context, R.drawable.bg_registration_doodle_radial_overlay);
        linear = AppCompatResources.getDrawable(context, R.drawable.bg_registration_doodle_linear_overlay);

        radial.setAlpha(INITIAL_RADIAL_ALPHA);
        linear.setAlpha(INITIAL_LINEAR_ALPHA);

        doodleWidth = doodle.getIntrinsicWidth();
        doodleHeight = doodle.getIntrinsicHeight();

        overlayPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.OVERLAY));
        overlayPaint.setAlpha(46);
        setWillNotDraw(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawARGB(255, 255, 103, 69);
        int width = getWidth();
        int height = getHeight();
        linear.setBounds(0, 0, width, height);
        linear.draw(canvas);
        canvas.save();
        canvas.scale(radialScale, 2);
        canvas.translate(0, radialOffset);
        int offset = (int)((1-radialScale) * (width / 2));
        radial.setBounds(offset, -getHeight() / 2, width, height);
        radial.draw(canvas);
        canvas.restore();
        canvas.save();
        canvas.scale(0.8f, 0.8f);
        Bitmap doodleBitmap = doodle.getBitmap();
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                int left = x * doodleWidth;
                int top = y * doodleHeight;
                canvas.drawBitmap(doodleBitmap, left, top, overlayPaint);
            }
        }
        canvas.restore();

        super.onDraw(canvas);
    }

    public void setPos(float f) {
        radialScale = 1 + (1 * f);
        radialOffset = (int)( - ((getHeight() / 4) * f));
        linear.setAlpha((int)((f * (FINAL_LINEAR_ALPHA - INITIAL_LINEAR_ALPHA)) + INITIAL_LINEAR_ALPHA));
        radial.setAlpha(INITIAL_RADIAL_ALPHA - (int)(FINAL_RADIAL_ALPHA * f));
        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            int width = right - left;
            int height = bottom - top;

            rows = ((height + doodleHeight - 1) / doodleHeight) + 1;
            cols = (width + doodleWidth - 1) / doodleWidth;
        }
    }
}

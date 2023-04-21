package com.halloapp.katchup;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;

import androidx.annotation.Nullable;

import com.halloapp.R;

public class LoadingView extends View {
    private final static float LETTER_SPACING = 0.2f;
    private final static int ROTATION_TIME_MS = 2500;

    public LoadingView(Context context) {
        super(context);
        init();
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private static String text;
    private final Path arcPath = new Path();
    private Paint textPaint;
    private final RectF ovalRect = new RectF();

    private void init() {
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        textPaint.setColor(Color.WHITE);
        textPaint.setLetterSpacing(LETTER_SPACING);

        text = getResources().getString(R.string.loading_spinner);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float size = Math.min(getWidth(), getHeight());
        float textSize = size / 5;

        ovalRect.set(0, 0, size, size);
        arcPath.reset();
        arcPath.addArc(ovalRect, -180, 200);
        textPaint.setTextSize(textSize);

        canvas.save();
        canvas.drawTextOnPath(text, arcPath, 0, textSize, textPaint);
        canvas.rotate(180, size / 2, size / 2);
        canvas.drawTextOnPath(text, arcPath, 0, textSize, textPaint);
        canvas.restore();
    }

    private void startRotation() {
        if (getVisibility() != VISIBLE || getWindowVisibility() != VISIBLE || getWidth() == 0 || getHeight() == 0) {
            return;
        }

        Animation animation = new RotateAnimation(0, 360, (float)getWidth() / 2, (float)getHeight() / 2);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        animation.setDuration(ROTATION_TIME_MS);
        startAnimation(animation);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        post(this::startRotation);
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);

        if (visibility == View.VISIBLE) {
            post(this::startRotation);
        } else {
            post(this::clearAnimation);
        }
    }
}

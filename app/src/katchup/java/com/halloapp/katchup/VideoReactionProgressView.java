package com.halloapp.katchup;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.R;

public class VideoReactionProgressView extends FrameLayout {

    private static final int OVAL_ROTATE_DEG = -12;

    private int lastWidth;
    private int lastHeight;

    private int rotateAngle = OVAL_ROTATE_DEG;

    private final Paint progressPaint = new Paint();
    private final Paint outlinePaint = new Paint();

    private final int[] colorGradient = new int[]{
            0xFFC2D69B,
            0xFFE8D161,
            0xFFE29163,
            0xFFE26666,
            0xFFE273CA,
            0xFFB19BD6,
            0xFF5B68E2,
            0xFF63B9DE,
            0xFF77E282
    };

    private final float[] colorDist = new float[] {
            0f,
            48.7f / 360f,
            97.5f / 360f,
            144.37f / 360f,
            193.12f / 360f,
            240f / 360f,
            288.7f / 360f,
            328.12f / 360f,
            1f
    };

    private int strokeWidth;
    private int outlineWidth;

    private float progress;

    private ValueAnimator valueAnimator;

    public VideoReactionProgressView(@NonNull Context context) {
        super(context);
        init(null, 0);
    }

    public VideoReactionProgressView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public VideoReactionProgressView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(@Nullable AttributeSet attrs, int defStyleAttr) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.VideoReactionProgressView, defStyleAttr, 0);
        rotateAngle = a.getInt(R.styleable.VideoReactionProgressView_vrpvRotation, OVAL_ROTATE_DEG);
        a.recycle();

        setWillNotDraw(false);

        strokeWidth = getContext().getResources().getDimensionPixelSize(R.dimen.video_reaction_stroke_width);
        outlineWidth = getContext().getResources().getDimensionPixelSize(R.dimen.video_reaction_stroke_outline);

        progressPaint.setColor(Color.WHITE);
        progressPaint.setStrokeWidth(strokeWidth);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        outlinePaint.setColor(Color.BLACK);
        outlinePaint.setStrokeWidth(strokeWidth + (2 * outlineWidth));
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeCap(Paint.Cap.ROUND);

    }

    public void startProgress(int durationSeconds) {
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        valueAnimator = ValueAnimator.ofFloat(0, 1f);
        valueAnimator.setDuration(durationSeconds * 1000L);
        valueAnimator.addUpdateListener(animation -> updateProgress(animation.getAnimatedFraction()));
        valueAnimator.start();
    }

    public void stopProgress() {
        if (valueAnimator != null) {
            valueAnimator.cancel();
            valueAnimator = null;
            progress = 0f;
        }
    }

    private void updateProgress(float progress) {
        this.progress = progress;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (lastWidth != w || lastHeight != h) {
            final float halfWidth = w / 2f;
            final float halfHeight = h / 2f;
            progressPaint.setShader(new SweepGradient(halfWidth, halfHeight, colorGradient, colorDist));

            lastWidth = w;
            lastHeight = h;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();
        float hW = w / 2f;
        float hH = h / 2f;
        canvas.rotate(rotateAngle, hW, hH);
        canvas.drawArc(0, 0, w, h, 0, 360f * progress, false, outlinePaint);
        canvas.drawArc(0, 0, w, h, 0, 360f * progress, false, progressPaint);
    }

}

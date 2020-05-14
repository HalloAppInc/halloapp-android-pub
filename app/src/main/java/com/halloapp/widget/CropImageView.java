package com.halloapp.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.halloapp.R;

public class CropImageView extends com.github.chrisbanes.photoview.PhotoView {

    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int gridColor;
    private boolean gridEnabled;
    private final RectF displayCropRect = new RectF();
    private final RectF relativeCropRect = new RectF();
    private float cornerRadius;
    private final Path cornerPath = new Path();
    private final Paint cornerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private boolean drawGrid;
    private float gridAlpha;

    private OnCropListener listener;

    public interface OnCropListener {
        void onCrop(RectF rect);
    }

    public CropImageView(Context context) {
        super(context);
        init(null, 0);
    }

    public CropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public CropImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CropImageView, defStyle, 0);

        cornerPaint.setColor(a.getColor(R.styleable.CropImageView_civCornerColor, 0));
        cornerRadius = a.getDimension(R.styleable.CropImageView_civCornerRadius, 0);
        gridColor = a.getColor(R.styleable.CropImageView_civGridColor, 0);
        gridPaint.setStrokeWidth(a.getDimension(R.styleable.CropImageView_civGridSize, 0));

        a.recycle();

        setOnMatrixChangeListener(rect -> {
            updateCropRect(rect);
            if (listener != null) {
                listener.onCrop(relativeCropRect);
            }
        });

        setOnViewDragListener((dx, dy) -> {
            if (gridEnabled) {
                drawGrid = true;
            }
        });
    }

    public void setGridEnabled(boolean enabled) {
        gridEnabled = enabled;
    }

    public void setOnCropListener(OnCropListener listener) {
        this.listener = listener;
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        if (gridEnabled) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                if (drawGrid) {
                    invalidate();
                    final FadeGridAnimation animation = new FadeGridAnimation();
                    animation.setDuration(500);
                    animation.setStartOffset(1000);
                    clearAnimation();
                    startAnimation(animation);
                    drawGrid = false;
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        updateCropRect(getDisplayRect());
        if (gridEnabled && (drawGrid || gridAlpha > 0)) {
            final float alpha = drawGrid ? 1 : gridAlpha;
            final int color =  (gridColor & 0xffffff) + (((int)(255 * alpha)) << 24);
            gridPaint.setColor(color);
            canvas.drawLine(displayCropRect.left + displayCropRect.width() / 3, displayCropRect.top, displayCropRect.left + displayCropRect.width() / 3, displayCropRect.bottom, gridPaint);
            canvas.drawLine(displayCropRect.right - displayCropRect.width() / 3, displayCropRect.top, displayCropRect.right - displayCropRect.width() / 3, displayCropRect.bottom, gridPaint);
            canvas.drawLine(displayCropRect.left, displayCropRect.top + displayCropRect.height() / 3, displayCropRect.right, displayCropRect.top + displayCropRect.height() / 3, gridPaint);
            canvas.drawLine(displayCropRect.left, displayCropRect.bottom - displayCropRect.height() / 3, displayCropRect.right, displayCropRect.bottom - displayCropRect.height() / 3, gridPaint);
        }

        if (cornerRadius != 0 && cornerPaint.getColor() != 0) {
            cornerPath.reset();
            cornerPath.addRoundRect(displayCropRect.left, displayCropRect.top, displayCropRect.right, displayCropRect.bottom, cornerRadius, cornerRadius, Path.Direction.CW);
            cornerPath.setFillType(Path.FillType.INVERSE_EVEN_ODD);
            canvas.drawPath(cornerPath, cornerPaint);
        }
    }

    private void updateCropRect(RectF displayRect) {
        if (displayRect == null) {
            return;
        }
        displayCropRect.set(displayRect);
        if (displayRect.left < 0) {
            displayCropRect.left = 0;
            relativeCropRect.left = -displayRect.left / displayRect.width();
        } else {
            relativeCropRect.left = 0;
        }
        if (displayRect.top < 0) {
            displayCropRect.top = 0;
            relativeCropRect.top = -displayRect.top / displayRect.height();
        } else {
            relativeCropRect.top = 0;
        }
        if (displayRect.right > getWidth()) {
            displayCropRect.right = getWidth();
            relativeCropRect.right = 1 - (displayRect.right - getWidth()) / displayRect.width();
        } else {
            relativeCropRect.right = 1;
        }
        if (displayRect.bottom > getHeight()) {
            displayCropRect.bottom = getHeight();
            relativeCropRect.bottom = 1 - (displayRect.bottom - getHeight()) / displayRect.height();
        } else {
            relativeCropRect.bottom = 1;
        }
    }

    class FadeGridAnimation extends Animation {

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            gridAlpha = 1 - interpolatedTime;
            postInvalidate();
        }
    }
}

package com.halloapp.widget;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.halloapp.Constants;
import com.halloapp.R;

public class PostScreenshotPhotoView extends AppCompatImageView {

    private float maxAspectRatio = Constants.MAX_IMAGE_ASPECT_RATIO;

    private int borderColor;
    private float cornerRadius;
    private final Paint outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint clearPaint = new Paint();

    private Path path = new Path();
    private RectF rectF;

    private DrawDelegateView drawDelegateView;
    private ProgressBar progressView;

    public PostScreenshotPhotoView(Context context) {
        super(context);
        init(null, 0);
    }

    public PostScreenshotPhotoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public PostScreenshotPhotoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        rectF = new RectF(0, 0, w, h);
        resetPath();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        if (drawable instanceof PlaceholderDrawable) {
            if (progressView != null) {
                progressView.setVisibility(View.VISIBLE);
            }
        } else {
            if (progressView != null) {
                progressView.setVisibility(View.GONE);
            }
        }
    }

    private void resetPath() {
        path.reset();
        path.addRoundRect(rectF, cornerRadius, cornerRadius, Path.Direction.CW);
        path.close();
    }

    private void init(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ContentPhotoView, defStyle, 0);

        borderColor = a.getColor(R.styleable.ContentPhotoView_contentPvBorderColor, 0);
        outlinePaint.setColor(borderColor);
        outlinePaint.setStyle(Paint.Style.STROKE);
        cornerRadius = a.getDimension(R.styleable.ContentPhotoView_contentPvCornerRadius, 0);
        maxAspectRatio = a.getFloat(R.styleable.ContentPhotoView_contentPvMaxAspectRatio, maxAspectRatio);

        clearPaint.setAntiAlias(true);
        clearPaint.setColor(Color.WHITE);
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));

        a.recycle();
    }

    public void setCornerRadius(float cornerRadius) {
        this.cornerRadius = cornerRadius;
    }
    public void setMaxAspectRatio(float maxAspectRatio) {
        this.maxAspectRatio = maxAspectRatio;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (drawDelegateView != null) {
            drawDelegateView.invalidateDelegateView(this);
        }
    }

    @Override
    public void setImageBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            super.setImageBitmap(null);
        } else {
            final int height = computeConstrainedHeight(bitmap.getWidth(), bitmap.getHeight());
            final int heightPadding = (bitmap.getHeight() - height) / 2;
            final Rect clipRect = new Rect(0, heightPadding, bitmap.getWidth(), heightPadding + height);
            final ClippedBitmapDrawable drawable = new ClippedBitmapDrawable(bitmap, clipRect);
            setImageDrawable(drawable);
        }
    }

    private int computeConstrainedHeight(int width, int height) {
        return maxAspectRatio > 0 ? Math.min(height, (int) (width * maxAspectRatio)) : height;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (drawDelegateView != null) {
            drawDelegateView.resetDelegateView(this);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();

        final Drawable drawable = getDrawable();
        if (drawable != null && drawable.getIntrinsicWidth() > 0) {
            int drawableWidth = drawable.getIntrinsicWidth();
            int drawableHeight = computeConstrainedHeight(drawableWidth, drawable.getIntrinsicHeight());

            if (getScaleType() == ScaleType.FIT_CENTER) {
                float scale = Math.min(1f * width / drawableWidth, 1f * height / drawableHeight);
                setMeasuredDimension((int) (drawableWidth * scale), (int) (drawableHeight * scale));
            } else {
                float drawableAspectRatio = 1f * drawableHeight / drawableWidth;
                setMeasuredDimension(width, (int) (width * drawableAspectRatio));
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int save = canvas.save();
        canvas.clipPath(path);
        super.onDraw(canvas);
        canvas.restoreToCount(save);

        if (borderColor != 0) {
            int w = getWidth();
            int h = getHeight();
            canvas.drawRoundRect(0, 0, w, h, cornerRadius, cornerRadius, outlinePaint);
        }
    }

}

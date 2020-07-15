package com.halloapp.widget;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ProgressBar;

import com.halloapp.Constants;
import com.halloapp.R;

public class ContentPhotoView extends com.github.chrisbanes.photoview.PhotoView {

    private float maxAspectRatio = Constants.MAX_IMAGE_ASPECT_RATIO;

    private float cornerRadius;
    private final Paint cornerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private DrawDelegateView drawDelegateView;
    private ProgressBar progressView;

    public ContentPhotoView(Context context) {
        super(context);
        init(null, 0);
    }

    public ContentPhotoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ContentPhotoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
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

    private void init(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ContentPhotoView, defStyle, 0);

        cornerPaint.setColor(a.getColor(R.styleable.ContentPhotoView_contentPvCornerColor, 0));
        cornerRadius = a.getDimension(R.styleable.ContentPhotoView_contentPvCornerRadius, 0);
        maxAspectRatio = a.getFloat(R.styleable.ContentPhotoView_contentPvMaxAspectRatio, maxAspectRatio);

        a.recycle();

        setOnScaleChangeListener((scaleFactor, focusX, focusY) -> {
            if (drawDelegateView != null) {
                if (getScale() > 1) {
                    drawDelegateView.setDelegateView(this);
                    final float decorationFactor = Math.min(.25f, getScale() - 1);
                    drawDelegateView.setDecoration(this, decorationFactor / .25f, decorationFactor >= .25f);
                } else {
                    drawDelegateView.resetDelegateView(this);
                }
            }
        });

        setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), cornerRadius);
            }
        });
        setClipToOutline(true);
    }

    public void setCornerRadius(float cornerRadius) {
        this.cornerRadius = cornerRadius;
    }

    public void setDrawDelegate(DrawDelegateView drawDelegateView) {
        this.drawDelegateView = drawDelegateView;
    }

    public void setProgressView(ProgressBar progressView) {
        this.progressView = progressView;
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

    public void playTransition(int duration) {
        Drawable drawable = getDrawable();
        if (drawable != null) {
            drawable.setAlpha(0);
            ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(drawable, PropertyValuesHolder.ofInt("alpha", 255));
            animator.setTarget(drawable);
            animator.setDuration(duration);
            animator.start();
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
        final Drawable drawable = getDrawable();
        if (drawable != null && drawable.getIntrinsicWidth() > 0) {
            int drawableWidth = drawable.getIntrinsicWidth();
            int drawableHeight = computeConstrainedHeight(drawableWidth, drawable.getIntrinsicHeight());
            float drawableAspectRatio = 1f * drawableHeight / drawableWidth;
            final int height = (int) (width * drawableAspectRatio);
            setMeasuredDimension(width, height);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final Drawable drawable = getDrawable();
        if (drawable instanceof ClippedBitmapDrawable) {
            final ClippedBitmapDrawable clippedBitmapDrawable = ((ClippedBitmapDrawable) drawable);
            if (getScale() <= 1) {
                clippedBitmapDrawable.allowOverdraw(1 - getScale());
            } else {
                clippedBitmapDrawable.allowOverdraw(0);
            }
        }
        super.onDraw(canvas);
    }

}

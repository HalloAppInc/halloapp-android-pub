package com.halloapp.widget;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.Constants;
import com.halloapp.R;

public class PostImageView extends com.github.chrisbanes.photoview.PhotoView {

    private float maxAspectRatio = Constants.MAX_IMAGE_ASPECT_RATIO;

    private float cornerRadius;
    private final Path cornerPath = new Path();
    private final Paint cornerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private DrawDelegateView drawDelegateView;

    public PostImageView(Context context) {
        super(context);
        init(null, 0);
    }

    public PostImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public PostImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.PostImageView, defStyle, 0);

        cornerPaint.setColor(a.getColor(R.styleable.PostImageView_pivCornerColor, 0));
        cornerRadius = a.getDimension(R.styleable.PostImageView_pivCornerRadius, 0);
        maxAspectRatio = a.getDimension(R.styleable.PostImageView_pivMaxAspectRatio, maxAspectRatio);

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
    }

    public void setDrawDelegate(DrawDelegateView drawDelegateView) {
        this.drawDelegateView = drawDelegateView;
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
            final int height = Math.min(bitmap.getHeight(), (int)(bitmap.getWidth() * maxAspectRatio));
            final int heightPadding =  (bitmap.getHeight() - height) / 2;
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
            final int height = (int) (width * Math.min(maxAspectRatio, 1f * drawable.getIntrinsicHeight() / drawable.getIntrinsicWidth()));
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
        if (getScale() <= 1) {
            if (cornerRadius != 0 && cornerPaint.getColor() != 0) {
                // TODO (ds): think about doing it with Outline, like CardView does, there seems to be some issues with ViewPager during scroll
                cornerPath.reset();
                cornerPath.addRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius, Path.Direction.CW);
                cornerPath.setFillType(Path.FillType.INVERSE_EVEN_ODD);
                canvas.drawPath(cornerPath, cornerPaint);
            }
        }
    }

    private static class ClippedBitmapDrawable extends Drawable {

        final Bitmap bitmap;
        final Rect clipRect;
        final Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        float allowedOverdraw;
        final Rect overdrawSrcRect = new Rect();
        final Rect overdrawDstRect = new Rect();

        ClippedBitmapDrawable(@NonNull Bitmap bitmap, @NonNull Rect clipRect) {
            this.bitmap = bitmap;
            this.clipRect = clipRect;
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            if (allowedOverdraw == 0) {
                canvas.drawBitmap(bitmap, clipRect, getBounds(), paint);
            } else {
                overdrawSrcRect.set(clipRect);
                final Rect bounds = getBounds();
                overdrawDstRect.set(bounds);

                overdrawSrcRect.left -= clipRect.width() * allowedOverdraw;
                overdrawSrcRect.right += clipRect.width() * allowedOverdraw;
                overdrawSrcRect.top -= clipRect.height() * allowedOverdraw;
                overdrawSrcRect.bottom += clipRect.height() * allowedOverdraw;

                overdrawDstRect.left -= bounds.width() * allowedOverdraw;
                overdrawDstRect.right += bounds.width() * allowedOverdraw;
                overdrawDstRect.top -= bounds.height() * allowedOverdraw;
                overdrawDstRect.bottom += bounds.height() * allowedOverdraw;
                canvas.drawBitmap(bitmap, overdrawSrcRect, overdrawDstRect, paint);
            }
        }

        @Override
        public void setAlpha(int alpha) {
            paint.setAlpha(alpha);
            invalidateSelf();
        }

        public int getAlpha() {
            return paint.getAlpha();
        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) {
        }

        @Override
        public int getOpacity() {
            return PixelFormat.OPAQUE;
        }

        @Override
        public int getIntrinsicWidth() {
            return clipRect.width();
        }

        @Override
        public int getIntrinsicHeight() {
            return clipRect.height();
        }

        public void allowOverdraw(float allowedOverdraw) {
            this.allowedOverdraw = allowedOverdraw;
        }
    }
}

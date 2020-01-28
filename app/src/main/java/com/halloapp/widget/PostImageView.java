package com.halloapp.widget;

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

import com.halloapp.R;

public class PostImageView extends com.github.chrisbanes.photoview.PhotoView {

    private float maxAspectRatio = 1.25f;

    private int cornerColor;
    private float cornerRadius;
    private Path cornerPath = new Path();

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

        cornerColor = a.getColor(R.styleable.PostImageView_pivCornerColor, 0);
        cornerRadius = a.getDimension(R.styleable.PostImageView_pivCornerRadius, 0);
        maxAspectRatio = a.getDimension(R.styleable.PostImageView_pivMaxAspectRatio, maxAspectRatio);

        a.recycle();
    }

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

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        Drawable drawable = getDrawable();
        if (drawable != null) {
            int height = (int) (width * Math.min(maxAspectRatio, 1f * drawable.getIntrinsicHeight() / drawable.getIntrinsicWidth()));
            setMeasuredDimension(width, height);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (cornerRadius != 0 && cornerColor != 0) {
            // TODO (ds): think about doing it with Outline, like CardView does, there seems to be some issues with ViewPager during scroll
            cornerPath.reset();
            cornerPath.addRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius, Path.Direction.CW);
            cornerPath.setFillType(Path.FillType.INVERSE_EVEN_ODD);
            canvas.clipPath(cornerPath);
            canvas.drawColor(cornerColor);
        }
    }

    private static class ClippedBitmapDrawable extends Drawable {

        final Bitmap bitmap;
        final Rect clipRect;
        final Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);

        ClippedBitmapDrawable(@NonNull Bitmap bitmap, @NonNull Rect clipRect) {
            this.bitmap = bitmap;
            this.clipRect = clipRect;
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            canvas.drawBitmap(bitmap, clipRect, getBounds(), paint);
        }

        @Override
        public void setAlpha(int alpha) {
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
    }
}

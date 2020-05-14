package com.halloapp.widget;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ClippedBitmapDrawable extends Drawable {

    private final Bitmap bitmap;
    private final Rect clipRect;
    private final Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
    private float allowedOverdraw;
    private final Rect overdrawSrcRect = new Rect();
    private final Rect overdrawDstRect = new Rect();

    public ClippedBitmapDrawable(@NonNull Bitmap bitmap, @NonNull Rect clipRect) {
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

package com.halloapp.widget;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PlaceholderDrawable extends Drawable {

    private final int width;
    private final int height;
    private final int color;

    public PlaceholderDrawable(int width, int height, int color) {
        this.width = width;
        this.height = height;
        this.color = color;
    }

    public int getIntrinsicWidth() {
        return width;
    }

    public int getIntrinsicHeight() {
        return height;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.drawColor(color);
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
}

package com.halloapp.widget;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class TextDrawable extends Drawable {

    private final String text;
    private final int textSize;
    private final int padding;

    private final TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private StaticLayout layout;

    public TextDrawable(String text, int textSize, int padding, int color) {
        this.text = text;
        this.textSize = textSize;
        this.padding = padding;
        textPaint.setColor(color);
    }

    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);
        updateLayout(right - left, bottom - top);
    }

    public void setBounds(@NonNull Rect bounds) {
        super.setBounds(bounds);
        updateLayout(bounds.width(), bounds.height());
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (layout == null) {
            return;
        }
        final Rect bounds = getBounds();
        canvas.translate(padding, (bounds.height() - layout.getHeight()) / 2);
        layout.draw(canvas);
    }

    private void updateLayout(int width, int height) {
        textPaint.setTextSize(textSize);
        do {
            layout = new StaticLayout(text, textPaint, width - padding * 2, Layout.Alignment.ALIGN_CENTER, 1, 0, false);
            textPaint.setTextSize(textPaint.getTextSize() - 1);
        } while (textPaint.getTextSize() > 4 &&
                (layout.getHeight() > height - padding * 2 || getLayoutWidth(layout) > width - padding * 2));

    }

    private static float getLayoutWidth(final @NonNull Layout layout) {
        float maxWidth = 0;
        for (int i = 0; i < layout.getLineCount(); i++) {
            maxWidth = Math.max(maxWidth, layout.getLineMax(i));
        }
        return maxWidth;
    }

    @Override
    public void setAlpha(int i) {

    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}
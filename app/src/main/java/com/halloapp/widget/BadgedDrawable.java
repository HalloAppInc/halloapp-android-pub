package com.halloapp.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.text.TextPaint;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;

import com.halloapp.util.Rtl;

public class BadgedDrawable extends InsetDrawable {

    private final Paint badgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final float size;
    private final boolean rtl;

    private String text;
    private final Rect tmpRect = new Rect();
    private final RectF badgeRect = new RectF();

    public BadgedDrawable(@NonNull Context context, @Nullable Drawable drawable, int textColor, int badgeColor, float size) {
        super(drawable, Rtl.isRtl(context) ? 0 : (int)(size / 4), (int)(size / 8), Rtl.isRtl(context) ? (int)(size / 4) : 0, (int)(size / 8));
        this.size = size;
        rtl = Rtl.isRtl(context);
        textPaint.setColor(textColor);
        textPaint.setFakeBoldText(true);
        textPaint.setTextSize(size * .8f);
        badgePaint.setColor(badgeColor);
    }

    public void setBadge(String text) {
        this.text = text;
        invalidateSelf();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);

        final Rect bounds = getBounds();
        badgeRect.set(
                rtl ? bounds.left : bounds.right - size, bounds.top,
                rtl ? bounds.left + size : bounds.right, bounds.top + size);
        canvas.drawOval(badgeRect, badgePaint);

        textPaint.getTextBounds(text, 0, text.length(), tmpRect);
        final float x = badgeRect.left + badgeRect.width() / 2f - tmpRect.width() / 2f - tmpRect.left;
        final float y = badgeRect.top + badgeRect.height() / 2f + tmpRect.height() / 2f - tmpRect.bottom;
        canvas.drawText(text, x, y, textPaint);
    }
}

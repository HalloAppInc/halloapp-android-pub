package com.halloapp.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Insets;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.util.Rtl;

public class BadgedDrawable extends Drawable {

    private final Paint badgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final float size;
    private final boolean rtl;

    private final Drawable drawable;
    private String text;
    private final Rect tmpRect = new Rect();
    private final RectF badgeRect = new RectF();

    public BadgedDrawable(@NonNull Context context, @NonNull Drawable drawable, int textColor, int badgeColor, int borderColor, float size) {
        this.drawable = drawable;
        this.size = size;
        rtl = Rtl.isRtl(context);
        textPaint.setColor(textColor);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);
        textPaint.setTextSize(size * .8f);
        badgePaint.setColor(badgeColor);
        borderPaint.setColor(borderColor);
    }

    public void setBadge(String text) {
        this.text = text;
        invalidateSelf();
    }

    public String getBadge() {
        return text;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {

        drawable.draw(canvas);

        if (!TextUtils.isEmpty(text)) {
            final Rect bounds = getBounds();
            badgeRect.set(
                    rtl ? bounds.left : bounds.right - size + size/4, bounds.top,
                    rtl ? bounds.left + size : bounds.right + size/4, bounds.top + size);

            float borderSize = size / 10;
            badgeRect.left -= borderSize;
            badgeRect.right += borderSize;
            badgeRect.top -= borderSize;
            badgeRect.bottom += borderSize;
            canvas.drawOval(badgeRect, borderPaint);

            badgeRect.left += borderSize;
            badgeRect.right -= borderSize;
            badgeRect.top += borderSize;
            badgeRect.bottom -= borderSize;
            canvas.drawOval(badgeRect, badgePaint);

            textPaint.getTextBounds(text, 0, text.length(), tmpRect);
            final float y = badgeRect.top + badgeRect.height() / 2f + tmpRect.height() / 2f - tmpRect.bottom;
            canvas.drawText(text, badgeRect.centerX(), y, textPaint);
        }
    }

    @Override
    public boolean getPadding(@NonNull Rect padding) {
        return drawable.getPadding(padding);
    }

    @Override
    public @NonNull Insets getOpticalInsets() {
        return drawable.getOpticalInsets();
    }

    @Override
    public void setHotspot(float x, float y) {
        drawable.setHotspot(x, y);
    }

    @Override
    public void setHotspotBounds(int left, int top, int right, int bottom) {
        drawable.setHotspotBounds(left, top, right, bottom);
    }

    @Override
    public void getHotspotBounds(@NonNull Rect outRect) {
        drawable.getHotspotBounds(outRect);
    }

    @Override
    public boolean setVisible(boolean visible, boolean restart) {
        final boolean superChanged = super.setVisible(visible, restart);
        final boolean changed = drawable.setVisible(visible, restart);
        return superChanged | changed;
    }

    @Override
    public void setAlpha(int alpha) {
        drawable.setAlpha(alpha);
    }

    @Override
    public int getAlpha() {
        return drawable.getAlpha();
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        drawable.setColorFilter(colorFilter);
    }

    @Override
    public ColorFilter getColorFilter() {
        return drawable.getColorFilter();
    }

    @Override
    public void setTintList(@Nullable ColorStateList tint) {
        drawable.setTintList(tint);
    }

    @Override
    public void setTintBlendMode(@Nullable BlendMode blendMode) {
        drawable.setTintBlendMode(blendMode);
    }

    @Override
    public int getOpacity() {
        return drawable.getOpacity();
    }

    @Override
    public boolean isStateful() {
        return drawable.isStateful();
    }

    @Override
    protected boolean onStateChange(int[] state) {
        if (drawable.isStateful()) {
            final boolean changed = drawable.setState(state);
            if (changed) {
                onBoundsChange(getBounds());
            }
            return changed;
        }
        return false;
    }

    @Override
    protected boolean onLevelChange(int level) {
        return drawable.setLevel(level);
    }

    @Override
    protected void onBoundsChange(@NonNull Rect bounds) {
        drawable.setBounds(bounds);
    }

    @Override
    public int getIntrinsicWidth() {
        return drawable.getIntrinsicWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        return drawable.getIntrinsicHeight();
    }

    @Override
    public void getOutline(@NonNull Outline outline) {
        drawable.getOutline(outline);
    }
}

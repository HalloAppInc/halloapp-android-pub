package com.halloapp.katchup.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Locale;

public abstract class CountingInteractionView extends FrameLayout {

    protected static final int TEXT_SIZE_DP = 18;
    protected static final int TEXT_PADDING_BOTTOM_DP = 4;

    private final TextPaint textPaint = new TextPaint();
    private int interactionCount = 0;

    public CountingInteractionView(@NonNull Context context) {
        super(context);
        init(null, 0);
    }

    public CountingInteractionView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public CountingInteractionView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(@Nullable AttributeSet attrs, int defStyleAttr) {
        setWillNotDraw(false);
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        textPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(dpToPx(TEXT_SIZE_DP));
        textPaint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        textPaint.setAntiAlias(true);
    }

    public void setInteractionCount(int count) {
        this.interactionCount = count;
        invalidate();
    }

    protected float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    abstract protected Drawable getDrawable();

    protected TextPaint getTextPaint() {
        return textPaint;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Drawable icon = getDrawable();
        icon.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        icon.draw(canvas);

        if (interactionCount > 0) {
            canvas.drawText(String.format(Locale.getDefault(), "%d", interactionCount), getWidth() / 2f, getHeight() / 2f + dpToPx(TEXT_PADDING_BOTTOM_DP), getTextPaint());
        }
    }

}

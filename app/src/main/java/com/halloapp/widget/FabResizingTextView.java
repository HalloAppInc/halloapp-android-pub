package com.halloapp.widget;

import android.content.Context;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import androidx.appcompat.widget.AppCompatTextView;

public class FabResizingTextView extends AppCompatTextView {

    public static final float MIN_TEXT_SIZE = 10;

    private final float targetTextSize;

    private int intrinsicWidth = 0;

    public FabResizingTextView(Context context) {
        this(context, null);
    }

    public FabResizingTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FabResizingTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        targetTextSize = getTextSize();
        resizeText();
    }

    public int getIntrinsicWidth() {
        return intrinsicWidth;
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        resizeText();
    }

    public void resizeText() {
        CharSequence text = getText();
        if (text == null || text.length() == 0 || targetTextSize == 0) {
            return;
        }
        TextPaint textPaint = new TextPaint(getPaint());

        float targetTextSize = this.targetTextSize;
        int width = getMaxTextWidth();
        StaticLayout layout = measureText(text, textPaint, width, targetTextSize);
        int lineCount = layout.getLineCount();
        while (lineCount > 1 && targetTextSize > MIN_TEXT_SIZE) {
            targetTextSize = Math.max(targetTextSize - 2, MIN_TEXT_SIZE);
            layout = measureText(text, textPaint, width, targetTextSize);
            lineCount = layout.getLineCount();
        }
        intrinsicWidth = (int) layout.getLineWidth(0);
        setTextSize(TypedValue.COMPLEX_UNIT_PX, targetTextSize);
    }

    private StaticLayout measureText(CharSequence source, TextPaint paint, int width, float textSize) {
        paint.setTextSize(textSize);
        return new StaticLayout(source, paint, width, Alignment.ALIGN_NORMAL, 1.0f, 0, false);
    }

    private int getMaxViewWidth() {
        DisplayMetrics displaymetrics = getResources().getDisplayMetrics();
        return (displaymetrics.widthPixels / 2);
    }

    private int getMaxTextWidth() {
        return getMaxViewWidth() - getCompoundPaddingStart() - getCompoundPaddingEnd();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode == MeasureSpec.UNSPECIFIED || widthMode == MeasureSpec.AT_MOST) {
            super.onMeasure(MeasureSpec.makeMeasureSpec(getMaxViewWidth(), MeasureSpec.AT_MOST), heightMeasureSpec);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}

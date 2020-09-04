package com.halloapp.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.KeyEvent;

import androidx.appcompat.widget.AppCompatEditText;

import com.halloapp.R;

public class CodeEditText extends AppCompatEditText {

    private int codeLength;

    private float requestedCellSpacing;
    private float cellCornerRadius;

    private final Paint cellPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final RectF cellRect = new RectF();
    private final Rect textRect = new Rect();

    private boolean blink;
    private final Runnable blinkRunnable = new Runnable() {
        @Override
        public void run() {
            blink = !blink;
            invalidate();
            postDelayed(this, 500);
        }
    };

    public CodeEditText(Context context) {
        super(context);
        init(null, 0);
    }

    public CodeEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public CodeEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CodeEditText, defStyle, 0);
        requestedCellSpacing = a.getDimension(R.styleable.CodeEditText_cetCellSpacing, -1);
        cellCornerRadius = a.getDimension(R.styleable.CodeEditText_cetCellRadius, 0);

        cellPaint.setColor(a.getColor(R.styleable.CodeEditText_cetBorderColor, 0xff000000));
        cellPaint.setStyle(Paint.Style.FILL);

        codeLength = a.getInteger(R.styleable.CodeEditText_cetCodeLength, 6);

        a.recycle();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        setSelection(getText() == null ? 0 : getText().length());
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        post(blinkRunnable);

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(blinkRunnable);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.translate(getScrollX(), 0);
        final String text = getText() == null ? "" : getText().toString();

        final int left = getPaddingLeft();
        final int right = getWidth() - getPaddingRight();
        final int top = 0; //getPaddingTop();
        final int bottom = getHeight(); // - getPaddingBottom();
        final int width = right - left;
        final int height = bottom - top;
        float cellWidth = Math.min(1f * width / codeLength, height);
        float cellSpacing = cellWidth / 12f;

        if (requestedCellSpacing >= 0) {
            cellSpacing = requestedCellSpacing;
        }

        cellRect.top = top;
        cellRect.bottom = bottom;

        textPaint.setColor(getCurrentTextColor());
        textPaint.setTextSize(getTextSize());
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);

        final float xOffset = left + width / 2f - ((cellWidth + cellSpacing) * codeLength) / 2 + cellSpacing/2;
        for (int i = 0; i < codeLength; i++) {
            cellRect.left = xOffset + i * (cellWidth + cellSpacing);
            cellRect.right = cellRect.left + cellWidth;
            canvas.drawRoundRect(cellRect, cellCornerRadius, cellCornerRadius, cellPaint);
            if (i < text.length()) {
                String digit = text.substring(i, i+1);
                textPaint.getTextBounds(digit, 0, digit.length(), textRect);
                final float y = cellRect.top + cellRect.height() / 2f + textRect.height() / 2f - textRect.bottom;
                canvas.drawText(digit, cellRect.centerX(), y, textPaint);
            } else if (i == text.length() && blink) {
                canvas.drawLine(cellRect.centerX(), cellRect.top + cellSpacing, cellRect.centerX(), cellRect.bottom - cellSpacing, textPaint);
            }
        }
    }
}

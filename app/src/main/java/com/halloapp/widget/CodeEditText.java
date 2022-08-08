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
    private float requestedCellSize;
    private float cellBorderWidth;

    private final Paint cellPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final Paint cursorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF cellRect = new RectF();
    private final RectF strokeRect = new RectF();
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
        cellBorderWidth = a.getDimension(R.styleable.CodeEditText_cetBorderWidth, 0);

        cellPaint.setColor(a.getColor(R.styleable.CodeEditText_cetCellColor, 0xff000000));
        cellPaint.setStyle(Paint.Style.FILL);

        strokePaint.setColor(a.getColor(R.styleable.CodeEditText_cetBorderColor, 0xff000000));
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(cellBorderWidth);

        codeLength = a.getInteger(R.styleable.CodeEditText_cetCodeLength, 6);
        cursorPaint.setColor(a.getColor(R.styleable.CodeEditText_cetCursorColor, 0xff000000));
        cursorPaint.setStrokeWidth(cellBorderWidth * 2);

        requestedCellSize = a.getDimension(R.styleable.CodeEditText_cetCellSize, getResources().getDimension(R.dimen.code_edit_box_size));

        a.recycle();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        setSelection(getText() == null ? 0 : getText().length());
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
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
        final int width = right - left;
        float cellSize = requestedCellSize;
        float cellSpacing = cellSize / 12f;

        if (requestedCellSpacing >= 0) {
            cellSpacing = requestedCellSpacing;
        }

        float expectedWidth = 2 * cellBorderWidth + ((requestedCellSize + cellSpacing) * codeLength) - cellSpacing;
        float scaleFactor = Math.min(width / expectedWidth, 1.0f);
        cellSize = requestedCellSize * scaleFactor;
        cellSpacing *= scaleFactor;
        cellRect.top = strokeRect.top = cellBorderWidth;
        cellRect.bottom = strokeRect.bottom = (cellSize * 1.2f) + cellBorderWidth;

        float textSize = getTextSize();

        if (textSize > cellSize) {
            textSize = cellSize * 0.75f;
        }

        textPaint.setColor(getCurrentTextColor());
        textPaint.setTextSize(textSize);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.DEFAULT);

        final float xOffset = cellBorderWidth;
        for (int i = 0; i < codeLength; i++) {
            cellRect.left = strokeRect.left = xOffset + i * (cellSize + cellSpacing);
            cellRect.right = strokeRect.right = cellRect.left + cellSize;
            canvas.drawRoundRect(cellRect, cellCornerRadius, cellCornerRadius, cellPaint);
            canvas.drawRoundRect(cellRect, cellCornerRadius, cellCornerRadius, strokePaint);
            if (i < text.length()) {
                String digit = text.substring(i, i+1);
                textPaint.getTextBounds(digit, 0, digit.length(), textRect);
                final float y = cellRect.top + cellRect.height() / 2f + textRect.height() / 2f - textRect.bottom;
                canvas.drawText(digit, cellRect.centerX(), y, textPaint);
            } else if (i == text.length() && blink) {
                canvas.drawLine(cellRect.centerX(), cellRect.top + cellSpacing * 1.5f, cellRect.centerX(), cellRect.bottom - cellSpacing * 1.5f, cursorPaint);
            }
        }
    }
}

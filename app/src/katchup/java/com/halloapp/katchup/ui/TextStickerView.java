package com.halloapp.katchup.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

public class TextStickerView extends AppCompatTextView {
    public TextStickerView(@NonNull Context context) {
        super(context);
    }

    public TextStickerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TextStickerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        TextPaint paint = getPaint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);
        int color = getCurrentTextColor();
        super.setTextColor(Color.BLACK);
        super.onDraw(canvas);

        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(0);
        super.setTextColor(color);
        super.onDraw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        float angle = getRotation();
        if (getRotation() != 0) {
            double ang = Math.toRadians(angle);
            setMeasuredDimension((int)(Math.abs(height * Math.sin(ang)) + width), (int)(height + Math.abs(width * Math.sin(Math.toRadians(angle)))));
        }
    }
}

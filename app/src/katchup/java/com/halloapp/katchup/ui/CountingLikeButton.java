package com.halloapp.katchup.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.halloapp.R;

public class CountingLikeButton extends CountingInteractionView {
    private boolean isLiked;
    private final TextPaint likedTextPaint = new TextPaint();

    public CountingLikeButton(@NonNull Context context) {
        super(context);
        init();
    }

    public CountingLikeButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CountingLikeButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        likedTextPaint.setColor(Color.WHITE);
        likedTextPaint.setTextAlign(Paint.Align.CENTER);
        likedTextPaint.setTextSize(dpToPx(TEXT_SIZE_DP));
        likedTextPaint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        likedTextPaint.setAntiAlias(true);
    }

    @Override
    protected Drawable getDrawable() {
        return ContextCompat.getDrawable(getContext(), isLiked ? R.drawable.ic_heart_pressed : R.drawable.ic_heart);
    }

    @Override
    protected TextPaint getTextPaint() {
        return isLiked ? likedTextPaint : super.getTextPaint();
    }

    public void setIsLiked(boolean isLiked) {
        this.isLiked = isLiked;
        invalidate();
    }

    public boolean getIsLiked() {
        return isLiked;
    }
}

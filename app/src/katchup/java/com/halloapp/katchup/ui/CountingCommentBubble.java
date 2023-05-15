package com.halloapp.katchup.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.halloapp.R;

public class CountingCommentBubble extends CountingInteractionView {
    public CountingCommentBubble(@NonNull Context context) {
        super(context);
    }

    public CountingCommentBubble(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CountingCommentBubble(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected Drawable getDrawable() {
        return ContextCompat.getDrawable(getContext(), R.drawable.ic_comment_bubble_shadow);
    }
}

package com.halloapp.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ContentComposerScrollView extends ScrollView {
    private boolean shouldScrollToBottom = false;

    public ContentComposerScrollView(@NonNull Context context) {
        super(context);
    }

    public ContentComposerScrollView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ContentComposerScrollView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setShouldScrollToBottom(boolean shouldScrollToBottom) {
        this.shouldScrollToBottom = shouldScrollToBottom;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (shouldScrollToBottom) {
            post(() -> {
                scrollToBottom();
            });
        }
    }

    private void scrollToBottom() {
        final int count = getChildCount();
        final int height = getHeight();
        final int visibleBottom = getScrollY() + height;
        int bottom = height;

        if (count > 0) {
            View view = getChildAt(count - 1);
            bottom = view.getBottom() + getPaddingBottom();
        }

        if (bottom > visibleBottom) {
            final int delta = bottom - visibleBottom;
            scrollBy(0, delta);
        }
    }
}

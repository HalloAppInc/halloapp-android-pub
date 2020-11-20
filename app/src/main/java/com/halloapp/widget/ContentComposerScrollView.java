package com.halloapp.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ContentComposerScrollView extends ScrollView {
    public interface OnOverScrolledListener {
        void onOverScrolled(ContentComposerScrollView view, int scrollX, int scrollY, boolean clampedX, boolean clampedY);
    }

    public interface OnScrollChangeListener {
        void onScrollChanged(ContentComposerScrollView view, int scrollX, int scrollY, int oldScrollX, int oldScrollY);
    }

    private OnScrollChangeListener onScrollChangeListener = null;
    private OnOverScrolledListener onOverScrollChangeListener = null;
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

    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
        if (onOverScrollChangeListener != null) {
            onOverScrollChangeListener.onOverScrolled(this, scrollX, scrollY, clampedX, clampedY);
        }
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldX, int oldY) {
        super.onScrollChanged(x, y, oldX, oldY);
        if (onScrollChangeListener != null) {
            onScrollChangeListener.onScrollChanged(this, x, y, oldX, oldY);
        }
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

    public void setOnScrollChangeListener(@NonNull OnScrollChangeListener onScrollChangeListener) {
        this.onScrollChangeListener = onScrollChangeListener;
    }

    public void setOnOverScrollChangeListener(@NonNull OnOverScrolledListener onOverScrollChangeListener) {
        this.onOverScrollChangeListener = onOverScrollChangeListener;
    }

    public void setShouldScrollToBottom(boolean shouldScrollToBottom) {
        this.shouldScrollToBottom = shouldScrollToBottom;
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

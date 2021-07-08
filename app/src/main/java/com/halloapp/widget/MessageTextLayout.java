package com.halloapp.widget;

import android.content.Context;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MessageTextLayout extends FrameLayout {

    private boolean forceSeparateLine;

    public MessageTextLayout(@NonNull Context context) {
        super(context);
    }

    public MessageTextLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MessageTextLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MessageTextLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setForceSeparateLine(boolean forceSeparateLine) {
        this.forceSeparateLine = forceSeparateLine;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
            return;
        }
        final TextView messageView = (TextView)getChildAt(0);
        final Layout layout = messageView.getLayout();
        if (layout != null && layout.getLineCount() == 0) {
            return;
        }
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int addWidth = 0;
        int addHeight = 0;
        final View statusView = getChildAt(1);
        final int statusViewWidth = statusView.getMeasuredWidth();
        final int statusViewHeight = statusView.getMeasuredHeight();
        final int messageViewWidth = messageView.getMeasuredWidth();
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        if (layout == null || layout.getLineCount() == 1) {
            if (messageViewWidth + statusViewWidth <= widthSize && !forceSeparateLine) {
                if (widthMode != MeasureSpec.EXACTLY) {
                    width = messageViewWidth + statusViewWidth;
                }
            } else {
                addHeight = statusViewHeight;
            }
        } else {
            final boolean lastParagraphRtl = Layout.DIR_RIGHT_TO_LEFT == layout.getParagraphDirection(layout.getLineCount() - 1);
            final boolean viewRtl = messageView.getLayoutDirection() == LAYOUT_DIRECTION_RTL;
            if (lastParagraphRtl != viewRtl) {
                addHeight = statusViewHeight;
            } else {
                final float lastLineWidth = layout.getLineWidth(layout.getLineCount() - 1) + messageView.getPaddingStart() + messageView.getPaddingEnd();
                if (lastLineWidth + statusViewWidth > widthSize) {
                    addHeight = statusViewHeight;
                } else if (lastLineWidth + statusViewWidth > width) {
                    addWidth = (int) lastLineWidth + statusViewWidth - width;
                }
            }
        }
        if (widthMode != MeasureSpec.EXACTLY) {
            width += addWidth;
        }
        if (heightMode != MeasureSpec.EXACTLY) {
            height += addHeight;
        }
        if (widthMode == MeasureSpec.AT_MOST && width > widthSize) {
            width = widthSize;
        }
        if (heightMode == MeasureSpec.AT_MOST && height > heightSize) {
            height = heightSize;
        }
        setMeasuredDimension(width, height);
    }
}

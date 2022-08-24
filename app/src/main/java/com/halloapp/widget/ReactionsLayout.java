package com.halloapp.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Outline;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.halloapp.content.Reaction;

import java.util.List;

public class ReactionsLayout extends FrameLayout {

    private int reactionSize;

    public ReactionsLayout(@NonNull Context context) {
        super(context);
        init(null, 0);
    }

    public ReactionsLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ReactionsLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    public ReactionsLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyle) {
        setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), view.getHeight() / 2f);
            }
        });
        setClipToOutline(true);

        reactionSize = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
        int padding = 4;
        setPadding(padding, padding, padding, padding);

        if (isInEditMode()) {
            setReactionCount(3);
        }
    }

    private void setReactionCount(int count) {
        int childCount = getChildCount();
        if (childCount < count) {
            for (int i = 0; i < count - childCount; i++) {
                final AppCompatTextView view = new AppCompatTextView(getContext());
                view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15f);
                view.setGravity(Gravity.CENTER);
                view.setTextColor(Color.BLACK);
                addView(view, 0);
            }
        }
        childCount = getChildCount();
        for (int i = childCount - 1; i >= childCount - count; i--) {
            getChildAt(i).setVisibility(View.VISIBLE);
        }
        for (int i = childCount - count - 1; i >= 0; i--) {
            getChildAt(i).setVisibility(View.GONE);
        }
        requestLayout();
    }

    public TextView getReactionView(int index) {
        return (TextView) getChildAt(index);
    }

    public void setReactions(@NonNull List<Reaction> reactions) {
        setReactionCount(Math.min(3, reactions.size()));
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (reactions.size() <= i) {
                break;
            }
            getReactionView(i).setText(reactions.get(i).reactionType);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int reactionMeasureSpec = MeasureSpec.makeMeasureSpec(reactionSize, MeasureSpec.EXACTLY);
        final int childCount = getChildCount();
        int visibleChildCount = 0;
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != View.GONE) {
                getChildAt(i).measure(reactionMeasureSpec, reactionMeasureSpec);
                visibleChildCount++;
            }
        }
        if (visibleChildCount == 0) {
            setMeasuredDimension(0, reactionSize + getPaddingTop() + getPaddingBottom());
        } else {
            setMeasuredDimension(
                    reactionSize + (int)((visibleChildCount - 1) * (2f * reactionSize / 3f)) + getPaddingLeft() + getPaddingRight(),
                    reactionSize + getPaddingTop() + getPaddingBottom());
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int childCount = getChildCount();
        final int paddingLeft = getPaddingLeft();
        final int paddingTop = getPaddingTop();
        int visibleChildCount = 0;
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != View.GONE) {
                visibleChildCount++;
            }
        }
        int index = 0;
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != View.GONE) {
                if (getLayoutDirection() == LAYOUT_DIRECTION_RTL) {
                    child.layout(
                            paddingLeft + index * 2 * reactionSize / 3, paddingTop,
                            paddingLeft + index * 2 * reactionSize / 3 + reactionSize, paddingTop + reactionSize);
                } else {
                    child.layout(
                            paddingLeft + (visibleChildCount - index - 1) * 2 * reactionSize / 3, paddingTop,
                            paddingLeft + (visibleChildCount - index - 1) * 2 * reactionSize / 3 + reactionSize, paddingTop + reactionSize);
                }
                index++;
            }
        }
    }
}

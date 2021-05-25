package com.halloapp.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AspectRatioFrameLayout extends FrameLayout {

    private int maxHeight = Integer.MAX_VALUE;
    private float aspectRatio = 0;

    public AspectRatioFrameLayout(@NonNull Context context) {
        super(context);
    }

    public AspectRatioFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AspectRatioFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setAspectRatio(float aspectRatio) {
        this.aspectRatio = aspectRatio;
        requestLayout();
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int hPadding = getPaddingLeft() + getPaddingRight();
        int vPadding = getPaddingTop() + getPaddingBottom();

        if (aspectRatio != 0) {
            final int height = (int) ((getMeasuredWidth() - hPadding) * aspectRatio) + vPadding;

            if (maxHeight > 0) {
                setMeasuredDimension(getMeasuredWidth(), Math.min(maxHeight, height));
            } else {
                setMeasuredDimension(getMeasuredWidth(), height);
            }
        } else if (maxHeight > 0) {
            setMeasuredDimension(getMeasuredWidth(), Math.min(maxHeight, getMeasuredHeight()));
        }

        if (maxHeight > 0 || aspectRatio != 0) {
            int width = getMeasuredWidth() - hPadding;
            int height = getMeasuredHeight() - vPadding;

            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                ViewGroup.LayoutParams params = child.getLayoutParams();

                child.measure(spec(params.width, width), spec(params.height, height));
            }
        }
    }

    private int spec(int param, int size) {
        if (param == ViewGroup.LayoutParams.MATCH_PARENT) {
            return MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
        } else {
            return MeasureSpec.makeMeasureSpec(size, MeasureSpec.AT_MOST);
        }
    }
}

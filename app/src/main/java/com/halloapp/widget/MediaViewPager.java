package com.halloapp.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

public class MediaViewPager extends ViewPager {


    public MediaViewPager(@NonNull Context context) {
        super(context);
    }

    public MediaViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int measuredHeight = getMeasuredHeight();
        int height = 0;
        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            int childHeight = child.getMeasuredHeight();
            if (childHeight > height) {
                height = childHeight;
            }
        }
        if (height < measuredHeight || measuredHeight == 0) {
            setMeasuredDimension(getMeasuredWidth(), height);
        }
        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY));
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        requestLayout(); // to workaround bug in ViewPager;
        // ViewPager.onAttachedToWindow sets mFirstLayout to true, but layout is never called to be recalculated
        // in case view pager is inside recycler view and the view just detaches and attaches back during
        // recycler view scrolling; it will recalculate when user swipes
        // the view pager; due to this the first swipe would not have an animation; this, most likely
        // is resolved in ViewPager2
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            //There are some ViewGroups (ones that utilize onInterceptTouchEvent) that throw exceptions when
            // a PhotoView is placed within them, most notably ViewPager and DrawerLayout.
            // This is a framework issue that has not been resolved.
            //uncomment if you really want to see these errors
            //e.printStackTrace();
            return false;
        }
    }
}

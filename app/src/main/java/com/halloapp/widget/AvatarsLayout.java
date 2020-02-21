package com.halloapp.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.halloapp.R;

public class AvatarsLayout extends FrameLayout {

    private int avatarSize;
    private int paddingColor;
    private int paddingSize;
    private float elevation;

    public AvatarsLayout(@NonNull Context context) {
        super(context);
        init(null, 0);
    }

    public AvatarsLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public AvatarsLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    public AvatarsLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.AvatarsLayout, defStyle, 0);

        avatarSize = a.getDimensionPixelSize(R.styleable.AvatarsLayout_alAvatarSize, 0);
        paddingColor = a.getColor(R.styleable.AvatarsLayout_alPaddingColor, 0);
        paddingSize = a.getDimensionPixelSize(R.styleable.AvatarsLayout_alPaddingSize, 0);
        elevation = a.getDimension(R.styleable.AvatarsLayout_alElevation, 0);

        a.recycle();

        setClipChildren(false);
        setClipToPadding(false);

        if (isInEditMode()) {
            setAvatarCount(3);
        }
    }

    public void setAvatarCount(int count) {
        int childCount = getChildCount();
        if (childCount < count) {
            for (int i = 0; i < count - childCount; i++) {
                final AvatarImageView view = new AvatarImageView(getContext());
                view.paint.setColor(paddingColor);
                view.paint.setStrokeWidth(2 * paddingSize);
                view.paint.setStyle(Paint.Style.STROKE);
                view.setClipToOutline(true);
                view.setElevation(elevation);
                view.setScaleType(ImageView.ScaleType.FIT_CENTER);
                view.setBackgroundResource(R.drawable.avatar_circle);
                view.setPadding(paddingSize, paddingSize, paddingSize, paddingSize);
                view.setImageResource(R.drawable.avatar_person);
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

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int avatarMeasureSpec = MeasureSpec.makeMeasureSpec(avatarSize, MeasureSpec.EXACTLY);
        final int childCount = getChildCount();
        int visibleChildCount = 0;
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != View.GONE) {
                getChildAt(i).measure(avatarMeasureSpec, avatarMeasureSpec);
                visibleChildCount++;
            }
        }
        if (visibleChildCount == 0) {
            setMeasuredDimension(0, avatarSize +  + getPaddingTop() + getPaddingBottom());
        } else {
            setMeasuredDimension(
                    avatarSize + (int)((visibleChildCount - 1) * (avatarSize / 2f)) + getPaddingLeft() + getPaddingRight(),
                    avatarSize +  + getPaddingTop() + getPaddingBottom());
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
                            paddingLeft + index * avatarSize / 2, paddingTop,
                            paddingLeft + index * avatarSize / 2 + avatarSize, paddingTop + avatarSize);
                } else {
                    child.layout(
                            paddingLeft + (visibleChildCount - index - 1) * avatarSize / 2, paddingTop,
                            paddingLeft + (visibleChildCount - index - 1) * avatarSize / 2 + avatarSize, paddingTop + avatarSize);
                }
                index++;
            }
        }
    }

    public static class AvatarImageView extends AppCompatImageView {

        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        public AvatarImageView(Context context) {
            super(context);
        }

        public AvatarImageView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public AvatarImageView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawOval(0, 0, getWidth(), getHeight(), paint);
        }
    }
}

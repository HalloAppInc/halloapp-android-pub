package com.halloapp.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.R;
import com.halloapp.id.UserId;
import com.halloapp.ui.avatar.AvatarLoader;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AvatarsLayout extends FrameLayout {

    private int avatarSize;
    private int paddingColor;
    private int paddingSize;
    private float elevation;
    private AvatarLoader avatarLoader;

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
                view.paint.setStrokeWidth(3 * paddingSize);
                view.paint.setStyle(Paint.Style.STROKE);
                view.setElevation(elevation);
                view.setScaleType(ImageView.ScaleType.CENTER_CROP);
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

    public void setAvatarLoader(AvatarLoader avatarLoader) {
        this.avatarLoader = avatarLoader;
    }

    public void setUsers(@NonNull List<UserId> users) {
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (users.size() <= i) {
                break;
            }
            avatarLoader.load((ImageView) getChildAt(childCount - i - 1), users.get(i), false);
        }
    }

    public void setImageResource(@DrawableRes int resource) {
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            ((ImageView) getChildAt(i)).setImageResource(resource);
        }
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
            setMeasuredDimension(0, avatarSize + getPaddingTop() + getPaddingBottom());
        } else {
            setMeasuredDimension(
                    avatarSize + (int)((visibleChildCount - 1) * (2f * avatarSize / 3f)) + getPaddingLeft() + getPaddingRight(),
                    avatarSize + getPaddingTop() + getPaddingBottom());
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
                            paddingLeft + index * 2 * avatarSize / 3, paddingTop,
                            paddingLeft + index * 2 * avatarSize / 3 + avatarSize, paddingTop + avatarSize);
                } else {
                    child.layout(
                            paddingLeft + (visibleChildCount - index - 1) * 2 * avatarSize / 3, paddingTop,
                            paddingLeft + (visibleChildCount - index - 1) * 2 * avatarSize / 3 + avatarSize, paddingTop + avatarSize);
                }
                index++;
            }
        }
    }

    public static class AvatarImageView extends CircleImageView {

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

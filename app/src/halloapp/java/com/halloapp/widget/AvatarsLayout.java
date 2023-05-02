package com.halloapp.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.halloapp.R;
import com.halloapp.content.Reaction;
import com.halloapp.id.UserId;
import com.halloapp.ui.avatar.AvatarLoader;

import java.util.List;

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
                final AvatarContainerView view = new AvatarContainerView(getContext());
                view.paint.setColor(paddingColor);
                view.paint.setStrokeWidth(3 * paddingSize);
                view.paint.setStyle(Paint.Style.STROKE);
                view.setElevation(elevation);
                view.setPadding(paddingSize, paddingSize, paddingSize, paddingSize);
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

    public ImageView getAvatarView(int index) {
        AvatarContainerView avatarContainerView = (AvatarContainerView) getChildAt(index);
        return avatarContainerView.getImageView();
    }

    public TextView getReactionView(int index) {
        AvatarContainerView avatarContainerView = (AvatarContainerView) getChildAt(index);
        return avatarContainerView.getReactionView();
    }

    public void setAvatarLoader(AvatarLoader avatarLoader) {
        this.avatarLoader = avatarLoader;
    }

    public void setUsers(@NonNull List<UserId> users) {
        setUsersAndReactions(users, null);
    }

    public void setUsersAndReactions(@NonNull List<UserId> users, @Nullable List<Reaction> reactions) {
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (users.size() <= i) {
                break;
            }
            avatarLoader.load(getAvatarView(childCount - i - 1), users.get(i), false);
            TextView reactionView = getReactionView(childCount - i - 1);
            if (reactions == null) {
                reactionView.setText("");
            } else {
                String reactionText = "";
                for (Reaction reaction : reactions) {
                    if (reaction.senderUserId.equals(users.get(i))) {
                        reactionText = reaction.reactionType;
                        break;
                    }
                }
                reactionView.setText(reactionText);
            }
        }
    }

    public void setAllImageResource(@DrawableRes int resource) {
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            getAvatarView(i).setImageResource(resource);
        }
    }

    public void clearReactions() {
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            getReactionView(i).setText("");
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

    public static class AvatarContainerView extends ConstraintLayout {

        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        private ShapeableImageView imageView;
        private TextView reaction;

        public AvatarContainerView(Context context) {
            super(context);
            init(context);
        }

        public AvatarContainerView(Context context, AttributeSet attrs) {
            super(context, attrs);
            init(context);
        }

        public AvatarContainerView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init(context);
        }

        private void init(Context context) {
            reaction = new AppCompatTextView(context);
            reaction.setId(View.generateViewId());
            reaction.setTextColor(Color.BLACK);
            reaction.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);

            imageView = new ShapeableImageView(context);
            imageView.setId(View.generateViewId());
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageResource(R.drawable.avatar_person);
            imageView.setShapeAppearanceModel(ShapeAppearanceModel.builder(context,  R.style.CircularImageView, 0).build());

            addView(imageView, new ConstraintLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            int endMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -5, displayMetrics);
            int bottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -3, displayMetrics);
            layoutParams.setMargins(isRtl() ? endMargin : 0, 0, isRtl() ? 0 : endMargin, bottomMargin);
            addView(reaction, layoutParams);

            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(this);
            constraintSet.connect(reaction.getId(), ConstraintSet.END, imageView.getId(), ConstraintSet.END);
            constraintSet.connect(reaction.getId(), ConstraintSet.BOTTOM, imageView.getId(), ConstraintSet.BOTTOM);
            constraintSet.applyTo(this);
            setWillNotDraw(false);
            setClipToPadding(false);
        }

        public ImageView getImageView() {
            return imageView;
        }

        public TextView getReactionView() {
            return reaction;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawOval(0, 0, getWidth(), getHeight(), paint);
        }
    }
}

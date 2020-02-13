package com.halloapp.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class SquareImageView extends AppCompatImageView {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({HORIZONTAL, VERTICAL})
    public @interface Orientation {}
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;

    private Drawable selector;
    private int orientation = HORIZONTAL;

    public SquareImageView(Context context) {
        super(context);
        init(context);
    }

    public SquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SquareImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        final TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        setSelector(ContextCompat.getDrawable(context, outValue.resourceId));
    }

    public void setOrientation(@Orientation int orientation) {
        if (this.orientation != orientation) {
            this.orientation = orientation;
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int size = orientation == HORIZONTAL ?
                getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec) :
                getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(size, size);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();

        final Drawable selector = this.selector;
        if (selector != null && selector.isStateful()) {
            selector.setState(getDrawableState());
        }
    }

    @Override
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();

        final Drawable selector = this.selector;
        if (selector != null) {
            selector.jumpToCurrentState();
        }
    }

    @Override
    public void drawableHotspotChanged(final float x, final float y) {
        super.drawableHotspotChanged(x, y);

        final Drawable selector = this.selector;
        if (selector != null) {
            selector.setHotspot(x, y);
        }
    }

    @Override
    protected boolean verifyDrawable(@NonNull final Drawable who) {
        return who == selector || super.verifyDrawable(who);
    }

    @Override
    public void draw(final Canvas canvas) {
        super.draw(canvas);

        final Drawable selector = this.selector;
        if (selector != null) {
            selector.setBounds(0, 0, getWidth(), getHeight());
            selector.draw(canvas);
        }
    }

    public void setSelector(final Drawable selector) {
        if (this.selector == selector) {
            return;
        }
        if (this.selector != null) {
            this.selector.setCallback(null);
        }
        this.selector = selector;
        if (this.selector != null) {
            this.selector.setCallback(this);
        }
    }
}

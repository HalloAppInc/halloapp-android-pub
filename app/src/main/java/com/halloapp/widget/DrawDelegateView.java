package com.halloapp.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;

import com.halloapp.R;
import com.halloapp.ui.SystemUiVisibility;

public class DrawDelegateView extends View {

    private View delegateView;
    private float x;
    private float y;

    private int backgroundColor;
    private boolean systemUiHidden;
    private int defaultSystemUiColor;
    private int windowColor;

    public DrawDelegateView(Context context) {
        super(context);
        init(context);
    }

    public DrawDelegateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DrawDelegateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        final TypedValue typedValue = new TypedValue();
        final TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[] { R.attr.colorPrimaryDark });
        defaultSystemUiColor = a.getColor(0, 0);
        a.recycle();

        windowColor = getContext().getResources().getColor(R.color.window_background);
    }

    public void setDelegateView(@NonNull View delegateView) {
        if (this.delegateView == delegateView) {
            return;
        }
        this.delegateView = delegateView;
        recomputeOffsets();
    }

    private void recomputeOffsets() {
        if (delegateView == null) {
            return;
        }
        final int[] thisLocation = new int[2];
        getLocationInWindow(thisLocation);
        final int[] childLocation = new int[2];
        delegateView.getLocationInWindow(childLocation);
        this.x = childLocation[0] - thisLocation[0];
        this.y = childLocation[1] - thisLocation[1];
    }

    public void resetDelegateView(@NonNull View delegateView) {
        if (this.delegateView != delegateView) {
            return;
        }
        this.delegateView = null;
        backgroundColor = 0;
        systemUiHidden = false;

        final Window window = ((Activity)getContext()).getWindow();
        window.setStatusBarColor(defaultSystemUiColor);
        window.setNavigationBarColor(defaultSystemUiColor);

        setSystemUiVisibility(SystemUiVisibility.getDefaultSystemUiVisibility(getContext()));
    }

    public void setDecoration(ContentPhotoView delegateView, float decorationFactor, boolean hideSystemUi) {
        if (this.delegateView != delegateView) {
            return;
        }
        backgroundColor = ((int)(255 * decorationFactor * .3f)) << 24;
        final int systemBarColor = ColorUtils.compositeColors(backgroundColor,
                defaultSystemUiColor == windowColor ? windowColor : ColorUtils.blendARGB(defaultSystemUiColor, windowColor, decorationFactor));
        final Window window = ((Activity)getContext()).getWindow();
        window.setStatusBarColor(systemBarColor);
        window.setNavigationBarColor(systemBarColor);
        if (hideSystemUi && !systemUiHidden) {
            systemUiHidden = true;
            setSystemUiVisibility(SystemUiVisibility.getFullScreenSystemUiVisibility(getContext()));
        }
    }

    public void invalidateDelegateView(@NonNull View delegateView) {
        if (this.delegateView == delegateView) {
            invalidate();
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (delegateView != null && delegateView.isAttachedToWindow()) {
            if (backgroundColor != 0) {
                canvas.drawColor(backgroundColor);
            }
            canvas.translate(x, y);
            delegateView.draw(canvas);
            canvas.translate(-x, -y);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            recomputeOffsets();
        }
    }
}

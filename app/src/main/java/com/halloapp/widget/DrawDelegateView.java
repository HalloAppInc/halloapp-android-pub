package com.halloapp.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DrawDelegateView extends View {

    private View delegateView;
    private float x;
    private float y;

    public DrawDelegateView(Context context) {
        super(context);
    }

    public DrawDelegateView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DrawDelegateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setDelegateView(@NonNull View delegateView) {
        if (this.delegateView == delegateView) {
            return;
        }
        this.delegateView = delegateView;
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
            canvas.translate(x, y);
            delegateView.draw(canvas);
            canvas.translate(-x, -y);
        }
    }
}

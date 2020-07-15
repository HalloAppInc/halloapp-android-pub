package com.halloapp.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Outline;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.ui.PlayerView;
import com.halloapp.Constants;
import com.halloapp.R;

public class ContentPlayerView extends PlayerView {
    private float maxAspectRatio = Constants.MAX_IMAGE_ASPECT_RATIO;
    private float cornerRadius;
    private float aspectRatio;

    public ContentPlayerView(Context context) {
        super(context);
        init(null, 0);
    }

    public ContentPlayerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ContentPlayerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ContentPlayerView, defStyle, 0);

        cornerRadius = a.getDimension(R.styleable.ContentPlayerView_contentPlayerVideoCornerRadius, 0f);
        maxAspectRatio = a.getFloat(R.styleable.ContentPlayerView_contentPlayerVideoMaxAspectRatio, maxAspectRatio);

        setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), cornerRadius);
            }
        });
        setClipToOutline(true);
    }

    public void setAspectRatio(float aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (aspectRatio != 0) {
            final int width = getMeasuredWidth();
            final int height = (int) (width * Math.min(maxAspectRatio, aspectRatio));
            setMeasuredDimension(width, height);

            for (int i = 0; i < getChildCount(); i++) {
                final View child = getChildAt(i);
                child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY));
            }
        }
    }
}

package com.halloapp.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Outline;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.halloapp.Constants;
import com.halloapp.R;

public class ContentPlayerView extends PlayerView {
    private float maxAspectRatio = Constants.MAX_IMAGE_ASPECT_RATIO;
    private float cornerRadius;
    private float aspectRatio;
    private boolean shouldPauseHiddenPlayer = false;

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

        a.recycle();
    }

    public void setMaxAspectRatio(float maxAspectRatio) {
        this.maxAspectRatio = maxAspectRatio;
    }

    public void setAspectRatio(float aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    private int computeConstrainedHeight(int width, int height) {
        return maxAspectRatio > 0 ? Math.min(height, (int) (width * maxAspectRatio)) : height;
    }

    public boolean isPlaying() {
        return getPlayer() != null && getPlayer().isPlaying();
    }

    public void play() {
        Player player = getPlayer();
        if (player != null) {
            player.setPlayWhenReady(true);
        }
    }

    public void pause() {
        Player player = getPlayer();
        if (player != null) {
            player.setPlayWhenReady(false);
        }
    }

    public void setPauseHiddenPlayerOnScroll(boolean pause) {
        if (pause) {
            getViewTreeObserver().addOnScrollChangedListener(this::pauseHiddenPlayer);
        } else if (shouldPauseHiddenPlayer) {
            getViewTreeObserver().removeOnScrollChangedListener(this::pauseHiddenPlayer);
        }

        shouldPauseHiddenPlayer = pause;
    }

    private void pauseHiddenPlayer() {
        if (getPlayer() != null && getPlayer().isPlaying()) {
            Rect rect = new Rect();
            Point point = new Point();
            boolean isVisible = getGlobalVisibleRect(rect, point);

            if (!isVisible) {
                getPlayer().setPlayWhenReady(false);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (aspectRatio != 0) {
            if (getResizeMode() == AspectRatioFrameLayout.RESIZE_MODE_FIT) {
                final int width = getMeasuredWidth();
                final int height = getMeasuredHeight();
                final float viewAspectRatio = 1f *  height / width;

                if (aspectRatio > viewAspectRatio) {
                    setMeasuredDimension((int) (height / aspectRatio), height);
                } else {
                    setMeasuredDimension(width, (int) (width * aspectRatio));
                }
            } else {
                final int width = getMeasuredWidth();
                final int height = computeConstrainedHeight(width, (int) (width * aspectRatio));
                setMeasuredDimension(width, height);
            }

            for (int i = 0; i < getChildCount(); i++) {
                final View child = getChildAt(i);
                child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY));
            }

            int widthLimit = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
            int heightLimit = getMeasuredHeight() - getPaddingBottom() - getPaddingTop();

            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                ViewGroup.LayoutParams params = child.getLayoutParams();

                child.measure(spec(params.width, widthLimit), spec(params.height, heightLimit));
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

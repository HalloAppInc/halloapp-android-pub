package com.halloapp.widget;

import android.content.Context;
import android.graphics.Outline;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.R;

public class PostLinkPreviewView extends LinkPreviewComposeView {

    public PostLinkPreviewView(@NonNull Context context) {
        super(context);
    }

    public PostLinkPreviewView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PostLinkPreviewView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setCloseButtonVisible(boolean visible) {
        linkPreviewClose.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    protected @LayoutRes
    int getLayoutId() {
        return R.layout.view_post_link_preview_compose;
    }

    protected void init() {
        super.init();
        setBackgroundResource(R.drawable.bg_post_link_preview);
        setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                int left = 0;
                int top = 0;
                int right = view.getWidth();
                int bottom = view.getHeight();
                float cornerRadius = getContext().getResources().getDimension(R.dimen.message_bubble_corner_radius);
                outline.setRoundRect(left, top, right, bottom, cornerRadius);
            }
        });
        View contentClip = findViewById(R.id.content_clip);
        contentClip.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                int left = 0;
                int top = 0;
                int right = view.getWidth();
                int bottom = view.getHeight();
                float cornerRadius = getContext().getResources().getDimension(R.dimen.message_bubble_corner_radius);
                outline.setRoundRect(left, top, right, bottom, cornerRadius);
            }
        });
        contentClip.setClipToOutline(true);
        setClipToOutline(true);
    }

}

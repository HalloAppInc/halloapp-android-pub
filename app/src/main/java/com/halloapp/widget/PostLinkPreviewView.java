package com.halloapp.widget;

import android.content.Context;
import android.graphics.Outline;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;

import com.halloapp.R;
import com.halloapp.UrlPreview;

public class PostLinkPreviewView extends LinkPreviewComposeView {

    private ConstraintLayout contentClip;

    private TextView linkDescription;
    private View domainContainer;
    private int squareDimension;
    private int landscapeHeight;

    private boolean inSquareOrientation;

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

    private void configureLandscapeOrientation() {
        inSquareOrientation = false;
        ConstraintSet constraintSet = new ConstraintSet();
        ConstraintLayout.LayoutParams imageLp = (ConstraintLayout.LayoutParams) linkImagePreview.getLayoutParams();
        imageLp.width = ConstraintLayout.LayoutParams.MATCH_PARENT;
        imageLp.height = landscapeHeight;
        linkImagePreview.setLayoutParams(imageLp);
        linkTitle.setMaxLines(2);

        domainContainer.setPadding(domainContainer.getPaddingLeft(), 0, domainContainer.getPaddingRight(), domainContainer.getPaddingBottom());

        constraintSet.clone(contentClip);
        constraintSet.connect(R.id.link_details_container, ConstraintSet.TOP, R.id.link_preview_image, ConstraintSet.BOTTOM);
        constraintSet.connect(R.id.link_details_container, ConstraintSet.BOTTOM, R.id.domain_container, ConstraintSet.TOP);
        constraintSet.connect(R.id.link_details_container, ConstraintSet.START, R.id.link_preview_image, ConstraintSet.START);
        constraintSet.connect(R.id.domain_container, ConstraintSet.TOP, R.id.link_details_container, ConstraintSet.BOTTOM);
        constraintSet.applyTo(contentClip);

        linkDescription.setVisibility(View.GONE);
        domainContainer.setBackground(null);
    }

    private void configureSquareOrientation() {
        inSquareOrientation = true;
        ConstraintSet constraintSet = new ConstraintSet();
        ConstraintLayout.LayoutParams imageLp = (ConstraintLayout.LayoutParams) linkImagePreview.getLayoutParams();
        imageLp.width = squareDimension;
        imageLp.height = squareDimension;
        linkImagePreview.setLayoutParams(imageLp);
        linkTitle.setMaxLines(Integer.MAX_VALUE);

        domainContainer.setPadding(domainContainer.getPaddingLeft(), domainContainer.getPaddingBottom() / 2, domainContainer.getPaddingRight(), domainContainer.getPaddingBottom());

        constraintSet.clone(contentClip);
        constraintSet.connect(R.id.link_details_container, ConstraintSet.TOP, R.id.link_preview_image, ConstraintSet.TOP);
        constraintSet.connect(R.id.link_details_container, ConstraintSet.BOTTOM, R.id.link_preview_image, ConstraintSet.BOTTOM);
        constraintSet.connect(R.id.link_details_container, ConstraintSet.START, R.id.link_preview_image, ConstraintSet.END);
        constraintSet.connect(R.id.domain_container, ConstraintSet.TOP, R.id.link_preview_image, ConstraintSet.BOTTOM);
        constraintSet.applyTo(contentClip);

        domainContainer.setBackgroundColor(ContextCompat.getColor(domainContainer.getContext(), R.color.url_preview_domain_background_square));
    }

    protected void init() {
        super.init();
        linkDescription = findViewById(R.id.link_description);
        domainContainer = findViewById(R.id.domain_container);
        squareDimension = getResources().getDimensionPixelSize(R.dimen.url_preview_square_size);
        landscapeHeight = getResources().getDimensionPixelSize(R.dimen.url_preview_landscape_height);
        linkImagePreview.setOutlineProvider(null);
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
        contentClip = findViewById(R.id.content_clip);
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

    @Override
    public void updateUrlPreview(@Nullable UrlPreview urlPreview) {
        super.updateUrlPreview(urlPreview);
        boolean useSquarelayout = shouldUseSquareLayout(urlPreview);
        if (useSquarelayout) {
            if (!inSquareOrientation) {
                configureSquareOrientation();
            }
            if (TextUtils.isEmpty(urlPreview.description)) {
                linkDescription.setVisibility(View.GONE);
            } else {
                linkDescription.setText(urlPreview.description);
                linkDescription.setVisibility(View.VISIBLE);
            }
        } else {
            if (inSquareOrientation) {
                configureLandscapeOrientation();
            }
            linkDescription.setVisibility(View.GONE);
        }
    }

    private boolean shouldUseSquareLayout(@Nullable UrlPreview urlPreview) {
        if (urlPreview == null || urlPreview.imageMedia == null) {
            return false;
        }
        int width = urlPreview.imageMedia.width;
        int height = urlPreview.imageMedia.height;
        if (height == 0) {
            return false;
        }
        float aspectRatio = (float) width / (float) height;
        return !(aspectRatio >= 1.25f);
    }

}

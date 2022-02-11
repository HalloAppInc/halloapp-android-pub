package com.halloapp.widget;

import android.content.Context;
import android.graphics.Outline;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.R;
import com.halloapp.UrlPreview;
import com.halloapp.content.ContentItem;
import com.halloapp.media.MediaThumbnailLoader;

public class LinkPreviewComposeView extends FrameLayout {

    protected ImageView linkImagePreview;
    protected TextView linkTitle;
    protected TextView linkDomainUrl;
    protected View linkPreviewClose;
    protected View loadingView;
    protected View linkIcon;

    protected UrlPreview urlPreview;

    protected boolean isLoading;
    protected String loadingUrl;

    private MediaThumbnailLoader mediaThumbnailLoader;

    public LinkPreviewComposeView(@NonNull Context context) {
        super(context);

        init();
    }

    public LinkPreviewComposeView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public LinkPreviewComposeView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    public void setMediaThumbnailLoader(@Nullable MediaThumbnailLoader mediaThumbnailLoader) {
        this.mediaThumbnailLoader = mediaThumbnailLoader;
    }

    protected @LayoutRes int getLayoutId() {
        return R.layout.view_link_preview_compose;
    }

    protected void init() {
        final Context context = getContext();
        inflate(context, getLayoutId(), this);

        linkDomainUrl = findViewById(R.id.link_domain);
        linkImagePreview = findViewById(R.id.link_preview_image);
        linkTitle = findViewById(R.id.link_title);
        linkPreviewClose = findViewById(R.id.link_preview_close);
        loadingView = findViewById(R.id.loading);
        linkIcon = findViewById(R.id.link_icon);
        linkImagePreview.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), linkImagePreview.getContext().getResources().getDimension(R.dimen.reply_media_corner_radius));
            }
        });
        linkImagePreview.setClipToOutline(true);
    }

    public void setOnRemovePreviewClickListener(View.OnClickListener listener) {
        linkPreviewClose.setOnClickListener(listener);
    }

    public void setLoadingUrl(String url) {
        this.loadingUrl = url;
    }

    public String getLoadingUrl() {
        return loadingUrl;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setLoading(boolean loading) {
        if (isLoading != loading) {
            isLoading = loading;
            if (isLoading) {
                setVisibility(View.VISIBLE);
                loadingView.setVisibility(View.VISIBLE);
                linkTitle.setVisibility(View.GONE);
                linkDomainUrl.setVisibility(View.GONE);
                linkIcon.setVisibility(View.GONE);
                linkImagePreview.setVisibility(View.GONE);
            } else {
                loadingView.setVisibility(View.GONE);
                linkTitle.setVisibility(View.VISIBLE);
                linkDomainUrl.setVisibility(View.VISIBLE);
                linkIcon.setVisibility(View.VISIBLE);
                loadingUrl = null;
            }
        }
    }

    public UrlPreview getUrlPreview() {
        return urlPreview;
    }

    public void attachPreview(@NonNull ContentItem contentItem) {
        if (urlPreview != null) {
            contentItem.urlPreview = new UrlPreview(urlPreview);
        } else {
            if (!TextUtils.isEmpty(loadingUrl)) {
                contentItem.loadingUrlPreview = loadingUrl;
            } else {
                contentItem.loadingUrlPreview = null;
            }
        }
    }

    public void updateUrlPreview(@Nullable UrlPreview urlPreview) {
        if (mediaThumbnailLoader != null) {
            mediaThumbnailLoader.cancel(linkImagePreview);
        }
        if (urlPreview == null) {
            setVisibility(View.GONE);
        } else {
            setLoading(false);
            setVisibility(View.VISIBLE);
            linkTitle.setText(urlPreview.title);
            linkDomainUrl.setText(urlPreview.tld);
            if (urlPreview.imageMedia != null) {
                linkImagePreview.setVisibility(View.VISIBLE);
                if (mediaThumbnailLoader != null) {
                    mediaThumbnailLoader.load(linkImagePreview, urlPreview.imageMedia);
                }
            } else {
                linkImagePreview.setVisibility(View.GONE);
            }
        }
        this.urlPreview = urlPreview;
    }

}

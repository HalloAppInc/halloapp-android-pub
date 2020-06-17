package com.halloapp.ui;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.viewpager.widget.PagerAdapter;

import com.halloapp.R;
import com.halloapp.content.Media;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.util.Rtl;
import com.halloapp.widget.DrawDelegateView;
import com.halloapp.widget.ContentPhotoView;

import java.util.List;
import java.util.Stack;

public class MediaPagerAdapter extends PagerAdapter {

    private final MediaPagerAdapterParent parent;
    private final float mediaCornerRadius;
    private List<Media> media;
    private String contentId;

    private boolean overrideMediaPadding = false;
    private int mediaInsetLeft;
    private int mediaInsetRight;
    private int mediaInsetBottom;
    private int mediaInsetTop;

    public interface MediaPagerAdapterParent {
        Stack<View> getRecycledMediaViews();
        DrawDelegateView getDrawDelegateView();
        MediaThumbnailLoader getMediaThumbnailLoader();
        void startActivity(@NonNull Intent intent);
        void startActivity(@NonNull Intent intent, @NonNull ActivityOptionsCompat options);
    }

    public static String getTransitionName(String contentId, int mediaIndex) {
        return "image-transition-" + contentId + "-" + mediaIndex;
    }

    public MediaPagerAdapter(@NonNull MediaPagerAdapterParent parent, float mediaCornerRadius) {
        this.parent = parent;
        this.mediaCornerRadius = mediaCornerRadius;
    }

    public void setMedia(@NonNull List<Media> media) {
        this.media = media;
        notifyDataSetChanged();
    }

    public void setMediaInset(int leftInsetPx, int topInsetPx, int rightInsetPx, int bottomInsetPx) {
        overrideMediaPadding = true;
        this.mediaInsetLeft = leftInsetPx;
        this.mediaInsetRight = rightInsetPx;
        this.mediaInsetBottom = bottomInsetPx;
        this.mediaInsetTop = topInsetPx;
        notifyDataSetChanged();
    }

    public void setContentId(@NonNull String contentId) {
        this.contentId = contentId;
    }

    public int getItemPosition(@NonNull Object object) {
        int index = 0;
        final Object tag = ((View) object).getTag();
        for (Media mediaItem : media) {
            if (mediaItem.equals(tag)) {
                return index;
            }
            index++;
        }
        return POSITION_NONE;
    }

    @Override
    public @NonNull Object instantiateItem(@NonNull ViewGroup container, int position) {
        final View view;
        if (parent.getRecycledMediaViews().empty()) {
            view = LayoutInflater.from(container.getContext()).inflate(R.layout.media_pager_item, container, false);
        } else {
            view = parent.getRecycledMediaViews().pop();
        }
        if (overrideMediaPadding) {
            view.setPadding(mediaInsetLeft, mediaInsetTop, mediaInsetRight, mediaInsetBottom);
        }
        final Media mediaItem = media.get(Rtl.isRtl(container.getContext()) ? media.size() - 1 - position : position);
        view.setTag(mediaItem);
        final ContentPhotoView imageView = view.findViewById(R.id.image);
        imageView.setTransitionName(getTransitionName(contentId, position));
        imageView.setCornerRadius(mediaCornerRadius);
        imageView.setSinglePointerDragStartDisabled(true);
        imageView.setDrawDelegate(parent.getDrawDelegateView());
        parent.getMediaThumbnailLoader().load(imageView, mediaItem);
        final View playButton = view.findViewById(R.id.play);
        if (mediaItem.type == Media.MEDIA_TYPE_VIDEO) {
            playButton.setVisibility(View.VISIBLE);
            if (mediaItem.file != null) {
                imageView.setOnClickListener(v -> {
                    final Intent intent = new Intent(container.getContext(), VideoPlaybackActivity.class);
                    intent.setData(Uri.fromFile(mediaItem.file));
                    parent.startActivity(intent);
                });
            } else {
                imageView.setOnClickListener(null);
            }
        } else {
            playButton.setVisibility(View.GONE);
            imageView.setOnClickListener(null);
        }

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        final View view = (View) object;
        final ContentPhotoView imageView = view.findViewById(R.id.image);
        imageView.setImageDrawable(null);
        container.removeView(view);
        parent.getRecycledMediaViews().push(view);
    }

    @Override
    public void finishUpdate(@NonNull ViewGroup container) {
        final int childCount = container.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = container.getChildAt(i);
            final Object tag = child.getTag();
            final ContentPhotoView imageView = child.findViewById(R.id.image);
            for (Media mediaItem : media) {
                if (mediaItem.equals(tag)) {
                    parent.getMediaThumbnailLoader().load(imageView, mediaItem);
                    break;
                }
            }
        }
    }

    @Override
    public int getCount() {
        return media == null ? 0 : media.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }
}

package com.halloapp.ui;

import android.content.Intent;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.halloapp.BuildConfig;
import com.halloapp.R;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.posts.Media;
import com.halloapp.util.Rtl;
import com.halloapp.widget.DrawDelegateView;
import com.halloapp.widget.PostImageView;

import java.util.List;
import java.util.Stack;

public class MediaPagerAdapter extends PagerAdapter {

    private final MediaPagerAdapterParent parent;
    private List<Media> media;

    public interface MediaPagerAdapterParent {
        Stack<View> getRecycledMediaViews();
        DrawDelegateView getDrawDelegateView();
        MediaThumbnailLoader getMediaThumbnailLoader();
        void startActivity(Intent intent);
    }

    public MediaPagerAdapter(@NonNull MediaPagerAdapterParent parent) {
        this.parent = parent;
    }

    public void setMedia(@NonNull List<Media> media) {
        this.media = media;
        notifyDataSetChanged();
    }

    public int getItemPosition(@NonNull Object object) {
        int index = 0;
        final Object tag = ((View) object).getTag();
        for (Media mediaItem : media) {
            if (Long.valueOf(mediaItem.rowId).equals(tag)) {
                return index;
            }
            index++;
        }
        return POSITION_NONE;
    }

    @Override
    public @NonNull
    Object instantiateItem(@NonNull ViewGroup container, int position) {
        final View view;
        if (parent.getRecycledMediaViews().empty()) {
            view = LayoutInflater.from(container.getContext()).inflate(R.layout.post_feed_media_pager_item, container, false);
            if (BuildConfig.DEBUG_MEDIA) {
                final TextView mediaInfoView = new TextView(container.getContext());
                mediaInfoView.setTextColor(0xffffffff);
                mediaInfoView.setShadowLayer(1, 1, 1, 0xff000000);
                int padding = container.getContext().getResources().getDimensionPixelSize(R.dimen.media_gallery_grid_spacing);
                mediaInfoView.setPadding(2 * padding, padding, 2 * padding, padding);
                mediaInfoView.setId(R.id.comment);
                ((ViewGroup) view).addView(mediaInfoView, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM));
            }
        } else {
            view = parent.getRecycledMediaViews().pop();
        }
        final Media mediaItem = media.get(Rtl.isRtl(container.getContext()) ? media.size() - 1 - position : position);
        view.setTag(mediaItem.rowId);
        final PostImageView imageView = view.findViewById(R.id.image);
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
        final PostImageView imageView = view.findViewById(R.id.image);
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
            final PostImageView imageView = child.findViewById(R.id.image);
            for (Media mediaItem : media) {
                if (Long.valueOf(mediaItem.rowId).equals(tag)) {
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

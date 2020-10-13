package com.halloapp.ui;

import android.app.ActivityOptions;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.R;
import com.halloapp.content.Media;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.ui.mediaexplorer.MediaExplorerActivity;
import com.halloapp.util.Rtl;
import com.halloapp.util.ThreadUtils;
import com.halloapp.widget.AspectRatioFrameLayout;
import com.halloapp.widget.DrawDelegateView;
import com.halloapp.widget.ContentPhotoView;

import java.util.ArrayList;
import java.util.List;

public class MediaPagerAdapter extends RecyclerView.Adapter<MediaPagerAdapter.MediaViewHolder> {

    private final MediaPagerAdapterParent parent;
    private final float mediaCornerRadius;
    private final float maxAspectRatio;
    private List<Media> media;
    private String contentId;

    private boolean overrideMediaPadding = false;
    private int mediaInsetLeft;
    private int mediaInsetRight;
    private int mediaInsetBottom;
    private int mediaInsetTop;

    private float fixedAspectRatio;
    
    public interface MediaPagerAdapterParent {
        RecyclerView.RecycledViewPool getMediaViewPool();
        DrawDelegateView getDrawDelegateView();
        MediaThumbnailLoader getMediaThumbnailLoader();
        void startActivity(@NonNull Intent intent);
        void startActivity(@NonNull Intent intent, @NonNull ActivityOptionsCompat options);
    }

    public static String getTransitionName(String contentId, int mediaIndex) {
        return "image-transition-" + contentId + "-" + mediaIndex;
    }

    public MediaPagerAdapter(@NonNull MediaPagerAdapter.MediaPagerAdapterParent parent, float mediaCornerRadius) {
        this(parent, mediaCornerRadius, 0);
    }

    public MediaPagerAdapter(@NonNull MediaPagerAdapter.MediaPagerAdapterParent parent, float mediaCornerRadius, float maxAspectRatio) {
        this.parent = parent;
        this.mediaCornerRadius = mediaCornerRadius;
        this.maxAspectRatio = maxAspectRatio;
    }

    public void setMedia(@NonNull List<Media> media) {
        this.media = media;
        this.fixedAspectRatio = Media.getMaxAspectRatio(media);
        if (maxAspectRatio != 0) {
            fixedAspectRatio = Math.min(fixedAspectRatio, maxAspectRatio);
        }
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

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MediaViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.media_pager_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        if (overrideMediaPadding) {
            holder.itemView.setPadding(mediaInsetLeft, mediaInsetTop, mediaInsetRight, mediaInsetBottom);
        }
        final Media mediaItem = media.get(Rtl.isRtl(holder.itemView.getContext()) ? media.size() - 1 - position : position);
        holder.imageView.setTransitionName(MediaPagerAdapter.getTransitionName(contentId, position));
        holder.container.setAspectRatio(fixedAspectRatio);
        parent.getMediaThumbnailLoader().load(holder.imageView, mediaItem);

        if (mediaItem.type == Media.MEDIA_TYPE_VIDEO) {
            holder.playButton.setVisibility(View.VISIBLE);
        } else {
            holder.playButton.setVisibility(View.GONE);
        }

        if (mediaItem.file != null) {
            holder.imageView.setOnClickListener(v -> {
                ArrayList<MediaExplorerActivity.Model> data = new ArrayList<>(media.size());
                for (final Media item : media) {
                    data.add(new MediaExplorerActivity.Model(Uri.fromFile(item.file), item.type));
                }

                Intent intent = new Intent(v.getContext(), MediaExplorerActivity.class);
                intent.putExtra(MediaExplorerActivity.EXTRA_MEDIA, data);
                intent.putExtra(MediaExplorerActivity.EXTRA_SELECTED, position);

                ThreadUtils.runWithoutStrictModeRestrictions(() -> {
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(v.getContext(), R.anim.slide_up, R.anim.keep_still);
                    parent.startActivity(intent, options);
                });
            });
        } else {
            holder.imageView.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return media == null ? 0 : media.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setRecycledViewPool(parent.getMediaViewPool());
    }

    public class MediaViewHolder extends RecyclerView.ViewHolder {

        final ContentPhotoView imageView;
        final ProgressBar progressView;
        final View playButton;
        final AspectRatioFrameLayout container;

        public MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.container);
            imageView = itemView.findViewById(R.id.image);
            progressView = itemView.findViewById(R.id.progress);
            playButton = itemView.findViewById(R.id.play);

            imageView.setCornerRadius(mediaCornerRadius);
            imageView.setSinglePointerDragStartDisabled(true);
            imageView.setDrawDelegate(parent.getDrawDelegateView());
            imageView.setMaxAspectRatio(maxAspectRatio);
            imageView.setProgressView(progressView);
        }
    }
}

package com.halloapp.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.content.Media;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.ui.mediaexplorer.MediaExplorerActivity;
import com.halloapp.util.Rtl;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.AspectRatioFrameLayout;
import com.halloapp.widget.ContentPhotoView;
import com.halloapp.widget.ContentPlayerView;
import com.halloapp.widget.DrawDelegateView;

import java.util.ArrayList;
import java.util.List;

public class MediaPagerAdapter extends RecyclerView.Adapter<MediaPagerAdapter.MediaViewHolder> implements LifecycleEventObserver {

    private final MediaPagerAdapterParent parent;
    private final float mediaCornerRadius;
    private final float maxAspectRatio;
    private List<Media> media;
    private String contentId;
    private RecyclerView recyclerView;

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
        LifecycleOwner getLifecycleOwner();
    }

    public static String getPagerTag(String contentId) {
        return "pager-tag-" + contentId;
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

        parent.getLifecycleOwner().getLifecycle().addObserver(this);
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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        holder.releasePlayer();

        if (overrideMediaPadding) {
            holder.itemView.setPadding(mediaInsetLeft, mediaInsetTop, mediaInsetRight, mediaInsetBottom);
        }
        final Media mediaItem = media.get(Rtl.isRtl(holder.itemView.getContext()) ? media.size() - 1 - position : position);
        holder.container.setAspectRatio(fixedAspectRatio);

        if (mediaItem.type == Media.MEDIA_TYPE_VIDEO) {
            if (mediaItem.height > Constants.MAX_IMAGE_ASPECT_RATIO * mediaItem.width) {
                holder.playerView.setResizeMode(com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
            } else {
                holder.playerView.setResizeMode(com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT);
            }

            if (mediaItem.width > 0) {
                holder.playerView.setAspectRatio(1f * mediaItem.height / mediaItem.width);
            }

            holder.imageView.setTransitionName("");
            holder.playerView.setTransitionName(MediaPagerAdapter.getTransitionName(contentId, position));
            holder.imageView.setVisibility(View.GONE);
            holder.playerView.setVisibility(View.VISIBLE);

            holder.initPlayer(mediaItem);
        } else {
            holder.imageView.setTransitionName(MediaPagerAdapter.getTransitionName(contentId, position));
            holder.playerView.setTransitionName("");
            holder.imageView.setVisibility(View.VISIBLE);
            holder.playerView.setVisibility(View.GONE);

            parent.getMediaThumbnailLoader().load(holder.imageView, mediaItem);
        }

        holder.imageView.setOnClickListener(null);
        holder.playerView.setOnTouchListener(null);

        if (mediaItem.file != null && mediaItem.type == Media.MEDIA_TYPE_IMAGE) {
            holder.imageView.setOnClickListener(v -> exploreMedia(holder.imageView, position));
        } else if (mediaItem.file != null && mediaItem.type == Media.MEDIA_TYPE_VIDEO) {
            GestureDetector doubleTapDetector = new GestureDetector(holder.playerView.getContext(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    exploreMedia(holder.playerView, position);
                    return true;
                }

                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    if (holder.isPlaying()) {
                        holder.pause();
                    } else {
                        holder.play();
                    }
                    return true;
                }
            });

            ScaleGestureDetector scaleDetector = new ScaleGestureDetector(holder.playerView.getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                private float scale = 1.0f;

                @Override
                public boolean onScaleBegin(ScaleGestureDetector detector) {
                    scale = 1.0f;
                    return true;
                }

                @Override
                public boolean onScale(ScaleGestureDetector detector) {
                    scale *= detector.getScaleFactor();
                    return true;
                }

                @Override
                public void onScaleEnd(ScaleGestureDetector detector) {
                    if (scale >= 1.0f) {
                        exploreMedia(holder.playerView, position);
                    }
                }
            });

            holder.playerView.setOnTouchListener((view, event) -> doubleTapDetector.onTouchEvent(event) || scaleDetector.onTouchEvent(event));
        }
    }

    private void exploreMedia(View view, int position) {
        Context ctx = recyclerView.getContext();
        ArrayList<MediaExplorerActivity.Model> data = new ArrayList<>(media.size());
        for (final Media item : media) {
            data.add(new MediaExplorerActivity.Model(Uri.fromFile(item.file), item.type));
        }

        Intent intent = new Intent(ctx, MediaExplorerActivity.class);
        intent.putExtra(MediaExplorerActivity.EXTRA_MEDIA, data);
        intent.putExtra(MediaExplorerActivity.EXTRA_SELECTED, position);
        intent.putExtra(MediaExplorerActivity.EXTRA_CONTENT_ID, contentId);

        if (ctx instanceof HalloActivity) {
            HalloActivity activity = (HalloActivity)ctx;
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, view, view.getTransitionName());
            activity.startActivityForResult(intent,  0, options.toBundle());
        } else {
            parent.startActivity(intent);
        }
    }

    @Override
    public int getItemCount() {
        return media == null ? 0 : media.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setRecycledViewPool(parent.getMediaViewPool());
        this.recyclerView = recyclerView;
    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        switch (event) {
            case ON_DESTROY:
                releasePlayers();
                recyclerView = null;
                parent.getLifecycleOwner().getLifecycle().removeObserver(this);
            case ON_PAUSE:
                pausePlayers();
                break;
        }
    }

    private void pausePlayers() {
        if (recyclerView == null) {
            return;
        }

        if (media != null) {
            for (Media m : media) {
                ContentPlayerView playerView = recyclerView.findViewWithTag(m);

                if (playerView != null && playerView.getPlayer() != null && playerView.getPlayer().isPlaying()) {
                    playerView.getPlayer().setPlayWhenReady(false);
                }
            }
        }
    }

    private void releasePlayers() {
        if (recyclerView == null) {
            return;
        }

        if (media != null) {
            for (Media m : media) {
                ContentPlayerView playerView = recyclerView.findViewWithTag(m);

                if (playerView != null) {
                    Player player = playerView.getPlayer();

                    if (player != null) {
                        player.setPlayWhenReady(false);
                        player.release();
                    }

                    playerView.setPauseHiddenPlayerOnScroll(false);
                    playerView.setPlayer(null);
                    playerView.setTag(null);
                }
            }
        }
    }

    public class MediaViewHolder extends RecyclerView.ViewHolder {

        final ContentPhotoView imageView;
        final ContentPlayerView playerView;
        final ProgressBar progressView;
        final AspectRatioFrameLayout container;

        public MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.container);
            imageView = itemView.findViewById(R.id.image);
            playerView = itemView.findViewById(R.id.video);
            progressView = itemView.findViewById(R.id.progress);

            imageView.setCornerRadius(mediaCornerRadius);
            imageView.setSinglePointerDragStartDisabled(true);
            imageView.setDrawDelegate(parent.getDrawDelegateView());
            imageView.setMaxAspectRatio(maxAspectRatio);
            imageView.setProgressView(progressView);
        }

        public void initPlayer(Media media) {
            releasePlayer();

            if (media.file == null) {
                Log.w("MediaPagerAdapter: video missing file.");
                return;
            }

            playerView.setPauseHiddenPlayerOnScroll(true);
            playerView.setTag(media);
            playerView.setControllerAutoShow(true);

            final DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(playerView.getContext(), Constants.USER_AGENT);
            MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.fromFile(media.file));

            SimpleExoPlayer player = new SimpleExoPlayer.Builder(playerView.getContext()).build();
            playerView.setPlayer(player);

            player.setRepeatMode(Player.REPEAT_MODE_ALL);
            player.prepare(mediaSource);
        }

        public void play() {
            Player player = playerView.getPlayer();
            if (player != null) {
                player.setPlayWhenReady(true);
            }
        }

        public void pause() {
            Player player = playerView.getPlayer();
            if (player != null) {
                player.setPlayWhenReady(false);
            }
        }

        public boolean isPlaying() {
            return playerView.getPlayer() != null && playerView.getPlayer().isPlaying();
        }

        public void releasePlayer() {
            Player player = playerView.getPlayer();
            if (player != null) {
                player.setPlayWhenReady(false);
                player.release();
            }

            playerView.setPauseHiddenPlayerOnScroll(false);
            playerView.setPlayer(null);
            playerView.setTag(null);
        }
    }
}

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
import android.view.ViewTreeObserver;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.exoplayer2.ControlDispatcher;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.content.Media;
import com.halloapp.id.ChatId;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.ui.mediaexplorer.MediaExplorerActivity;
import com.halloapp.ui.mediaexplorer.MediaExplorerViewModel;
import com.halloapp.util.Rtl;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.AspectRatioFrameLayout;
import com.halloapp.widget.ContentPhotoView;
import com.halloapp.widget.ContentPlayerView;
import com.halloapp.widget.DrawDelegateView;

import java.util.List;

public class MediaPagerAdapter extends RecyclerView.Adapter<MediaPagerAdapter.MediaViewHolder> implements LifecycleEventObserver {

    private static final float MAX_MEDIA_RATIO = 0.6f;

    private final MediaPagerAdapterParent parent;
    private final float mediaCornerRadius;
    private final float maxAspectRatio;
    private List<Media> media;
    private String contentId;
    private RecyclerView recyclerView;
    private boolean isChatMedia = false;
    private ChatId chatId;

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

    public static void preparePagerForTransition(@NonNull View root, @NonNull String contentId, int mediaIndex, @NonNull Runnable runnable) {
        ViewPager2 pager = root.findViewWithTag(getPagerTag(contentId));

        if (pager != null && pager.getCurrentItem() != mediaIndex) {
            pager.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    pager.getViewTreeObserver().removeOnPreDrawListener(this);

                    MediaPagerAdapter adapter = (MediaPagerAdapter) pager.getAdapter();
                    if (adapter == null) {
                        root.post(runnable);
                        return true;
                    }

                    View view = pager.findViewWithTag(adapter.media.get(mediaIndex));

                    if (view instanceof ContentPhotoView) {
                        ContentPhotoView photoView = (ContentPhotoView) view;

                        if (photoView.getDrawable() == null) {
                            photoView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                                @Override
                                public boolean onPreDraw() {
                                    photoView.getViewTreeObserver().removeOnPreDrawListener(this);
                                    root.post(runnable);
                                    return true;
                                }
                            });
                        } else {
                            root.post(runnable);
                        }
                    } else if (view instanceof PlayerView) {
                        ContentPlayerView playerView = (ContentPlayerView) view;
                        Player player = playerView.getPlayer();

                        if (player != null && player.getPlaybackState() == Player.STATE_READY) {
                            root.post(runnable);
                        } else if (player != null) {
                            player.addListener(new Player.EventListener() {
                                @Override
                                public void onPlaybackStateChanged(int state) {
                                    if (state == Player.STATE_READY) {
                                        player.removeListener(this);
                                        root.post(runnable);
                                    }
                                }
                            });
                        } else {
                            root.post(runnable);
                        }
                    } else {
                        root.post(runnable);
                    }

                    return true;
                }
            });

            pager.setCurrentItem(mediaIndex, false);
        } else {
            root.post(runnable);
        }
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
        this.fixedAspectRatio = Media.getMaxAspectRatio(media);
        if (maxAspectRatio != 0) {
            fixedAspectRatio = Math.min(fixedAspectRatio, maxAspectRatio);
        }
        if (this.media == null || this.media.size() != media.size()) {
            notifyDataSetChanged();
        } else {
            for (int i = 0; i < media.size(); i++) {
                Media newMedia = media.get(i);
                Media oldMedia = this.media.get(i);
                if (!oldMedia.equals(newMedia)) {
                    notifyItemChanged(i);
                }
            }
        }
        this.media = media;
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

    public String getContentId() {
        return contentId;
    }

    public void setChat(ChatId chatId) {
        isChatMedia = true;
        this.chatId = chatId;
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
        holder.imageView.setTransitionName("");
        holder.playerView.setTransitionName("");
        holder.imageView.setVisibility(View.GONE);
        holder.playerView.setVisibility(View.GONE);
        holder.imageView.setTag(null);
        holder.playerView.setTag(null);

        if (overrideMediaPadding) {
            holder.itemView.setPadding(mediaInsetLeft, mediaInsetTop, mediaInsetRight, mediaInsetBottom);
        }
        final Media mediaItem = media.get(Rtl.isRtl(holder.itemView.getContext()) ? media.size() - 1 - position : position);
        holder.container.setAspectRatio(fixedAspectRatio);

        int displayHeight = holder.container.getContext().getResources().getDisplayMetrics().heightPixels;
        holder.container.setMaxHeight((int) (displayHeight * MAX_MEDIA_RATIO));

        String transitionName = MediaPagerAdapter.getTransitionName(contentId, position);

        if (mediaItem.type == Media.MEDIA_TYPE_VIDEO) {
            holder.playerView.setResizeMode(com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT);

            if (mediaItem.width > 0) {
                holder.playerView.setAspectRatio(1f * mediaItem.height / mediaItem.width);
            }

            holder.playerView.setTransitionName(transitionName);
            holder.playerView.setVisibility(View.VISIBLE);
            holder.initPlayer(mediaItem);
        } else {
            holder.imageView.setTransitionName(transitionName);
            holder.imageView.setTag(mediaItem);
            holder.imageView.setVisibility(View.VISIBLE);

            parent.getMediaThumbnailLoader().load(holder.imageView, mediaItem);
        }

        holder.imageView.setOnClickListener(null);
        holder.playerView.setOnTouchListener(null);

        if (mediaItem.file != null && mediaItem.type == Media.MEDIA_TYPE_IMAGE) {
            holder.imageView.setOnClickListener(v -> exploreMedia(holder.imageView, position, 0));
        } else if (mediaItem.file != null && mediaItem.type == Media.MEDIA_TYPE_VIDEO) {
            GestureDetector doubleTapDetector = new GestureDetector(holder.playerView.getContext(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    Player player = holder.playerView.getPlayer();
                    exploreMedia(holder.playerView, position, player != null ? player.getCurrentPosition() : 0);
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
                        Player player = holder.playerView.getPlayer();
                        exploreMedia(holder.playerView, position, player != null ? player.getCurrentPosition() : 0);
                    }
                }
            });

            holder.playerView.setOnTouchListener((view, event) -> doubleTapDetector.onTouchEvent(event) || scaleDetector.onTouchEvent(event));
        }
    }

    private void exploreMedia(View view, int position, long currentTime) {
        Context ctx = recyclerView.getContext();

        Intent intent = new Intent(ctx, MediaExplorerActivity.class);
        intent.putExtra(MediaExplorerActivity.EXTRA_MEDIA, MediaExplorerViewModel.MediaModel.fromMedia(media));
        intent.putExtra(MediaExplorerActivity.EXTRA_SELECTED, position);
        intent.putExtra(MediaExplorerActivity.EXTRA_CONTENT_ID, contentId);
        intent.putExtra(MediaExplorerActivity.EXTRA_INITIAL_TIME, currentTime);
        if (isChatMedia) {
            intent.putExtra(MediaExplorerActivity.EXTRA_CHAT_ID, chatId);
        }

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
                View view = recyclerView.findViewWithTag(m);

                if (view instanceof ContentPlayerView) {
                    ContentPlayerView playerView = (ContentPlayerView) view;
                    Player player = playerView.getPlayer();

                    if (player != null && player.isPlaying()) {
                        player.setPlayWhenReady(false);
                    }
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
                View view = recyclerView.findViewWithTag(m);

                if (view instanceof ContentPlayerView) {
                    ContentPlayerView playerView = (ContentPlayerView) view;
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
        private final long INITIAL_FRAME_TIME = 1000;

        final ContentPhotoView imageView;
        final ContentPlayerView playerView;
        final ProgressBar progressView;
        final AspectRatioFrameLayout container;

        private boolean isVideoAtStart = false;
        private boolean isPlayerInitialized = false;

        public MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.container);
            imageView = itemView.findViewById(R.id.image);
            playerView = itemView.findViewById(R.id.video);
            progressView = itemView.findViewById(R.id.media_progress);

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
            MediaItem mediaItem = MediaItem.fromUri(Uri.fromFile(media.file));
            MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem);

            isPlayerInitialized = false;
            SimpleExoPlayer player = new SimpleExoPlayer.Builder(playerView.getContext()).build();

            player.addListener(new Player.EventListener() {
                @Override
                public void onPlaybackStateChanged(int state) {
                    if (state == Player.STATE_READY && !isPlayerInitialized) {
                        isPlayerInitialized = true;
                        seekToThumbnailFrame(player);
                    }
                }

                @Override
                public void onIsPlayingChanged(boolean isPlaying) {
                    playerView.setKeepScreenOn(isPlaying);
                }
            });

            PlayerControlView controlView = playerView.findViewById(R.id.exo_controller);
            controlView.setControlDispatcher(new ControlDispatcher() {
                @Override
                public boolean dispatchPrepare(Player player) { return false; }

                @Override
                public boolean dispatchSetPlayWhenReady(@NonNull Player player, boolean playWhenReady) {
                    if (playWhenReady) {
                        play();
                    } else {
                        pause();
                    }

                    return true;
                }

                @Override
                public boolean dispatchSeekTo(Player player, int windowIndex, long positionMs) { return false; }

                @Override
                public boolean dispatchPrevious(Player player) { return false; }

                @Override
                public boolean dispatchNext(Player player) { return false; }

                @Override
                public boolean dispatchRewind(Player player) { return false; }

                @Override
                public boolean dispatchFastForward(Player player) { return false; }

                @Override
                public boolean dispatchSetRepeatMode(Player player, int repeatMode) { return false; }

                @Override
                public boolean dispatchSetShuffleModeEnabled(Player player, boolean shuffleModeEnabled) { return false; }

                @Override
                public boolean dispatchStop(Player player, boolean reset) { return false; }

                @Override
                public boolean dispatchSetPlaybackParameters(Player player, PlaybackParameters playbackParameters) { return false; }

                @Override
                public boolean isRewindEnabled() { return false; }

                @Override
                public boolean isFastForwardEnabled() { return false; }
            });

            playerView.setPlayer(player);

            player.setRepeatMode(Player.REPEAT_MODE_ALL);
            player.setMediaSource(mediaSource);
            player.prepare();
        }

        private void seekToThumbnailFrame(Player player) {
            if (!player.isPlaying() && (player.getDuration() == player.getCurrentPosition() || player.getCurrentPosition() == 0)) {
                isVideoAtStart = true;

                if (player.getDuration() > INITIAL_FRAME_TIME) {
                    player.seekTo(INITIAL_FRAME_TIME);
                } else {
                    player.seekTo(player.getDuration() / 2);
                }
            }
        }

        public void play() {
            Player player = playerView.getPlayer();
            if (player != null) {
                if (isVideoAtStart) {
                    player.seekTo(0);
                    isVideoAtStart = false;
                }

                player.setPlayWhenReady(true);
            }
        }

        public void pause() {
            Player player = playerView.getPlayer();
            if (player != null) {
                player.setPlayWhenReady(false);
                seekToThumbnailFrame(player);
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

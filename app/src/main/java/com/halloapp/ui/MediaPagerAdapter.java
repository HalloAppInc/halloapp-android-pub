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
import androidx.annotation.Nullable;
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
import com.google.android.exoplayer2.upstream.DataSource;
import com.halloapp.R;
import com.halloapp.content.Media;
import com.halloapp.id.ChatId;
import com.halloapp.media.ChunkedMediaParameters;
import com.halloapp.media.ChunkedMediaParametersException;
import com.halloapp.media.ExoUtils;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.ui.mediaexplorer.MediaExplorerActivity;
import com.halloapp.ui.mediaexplorer.MediaExplorerViewModel;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.AspectRatioFrameLayout;
import com.halloapp.widget.ContentPhotoView;
import com.halloapp.widget.ContentPlayerView;
import com.halloapp.widget.DrawDelegateView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MediaPagerAdapter extends RecyclerView.Adapter<MediaPagerAdapter.MediaViewHolder> implements LifecycleEventObserver {
    public static final float MAX_MEDIA_RATIO = 0.6f;
    public static final int OFFSCREEN_PLAYER_LIMIT_DEFAULT = -1;

    private final Map<Media, WrappedPlayer> playerMap = new HashMap<>();
    private final MediaPagerAdapterParent parent;
    private final float mediaCornerRadius;
    private final float maxAspectRatio;
    private final boolean representVideoByThumbnail;
    private ArrayList<Media> media;
    private String contentId;
    private RecyclerView recyclerView;
    private boolean isChatMedia = false;
    private ChatId chatId;
    private boolean allowSaving;

    private boolean overrideMediaPadding = false;
    private int mediaInsetLeft;
    private int mediaInsetRight;
    private int mediaInsetBottom;
    private int mediaInsetTop;

    private float fixedAspectRatio;
    private int offscreenPlayerLimit = OFFSCREEN_PLAYER_LIMIT_DEFAULT;

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

    @Nullable
    public static View getTransitionView(@NonNull View root, @NonNull String transitionName) {
        final String prefix = "image-transition-";

        if (transitionName.startsWith(prefix)) {
            int split = transitionName.lastIndexOf("-");

            if (split != -1 && prefix.length() < split && (split + 1) < transitionName.length()) {
                String contentId = transitionName.substring(prefix.length(), split);
                int mediaIndex;
                try {
                     mediaIndex = Integer.parseInt(transitionName.substring(split + 1));
                } catch (NumberFormatException e) {
                    Log.e("MediaPagerAdapter.getTransitionView: media index not number", e);
                    return null;
                }

                ViewPager2 pager = root.findViewWithTag(getPagerTag(contentId));
                if (pager == null) {
                    Log.d("MediaPagerAdapter.getTransitionView: pager not found");
                    return null;
                }

                MediaPagerAdapter adapter = (MediaPagerAdapter) pager.getAdapter();
                if (adapter == null) {
                    Log.d("MediaPagerAdapter.getTransitionView: missing adapter");
                    return null;
                }

                Media media = adapter.media.get(mediaIndex);
                View container = pager.findViewWithTag(media);
                if (container == null) {
                    Log.d("MediaPagerAdapter.getTransitionView: missing transition view container");
                    return null;
                }

                if (adapter.isMediaRepresentedByImage(media)) {
                    return container.findViewById(R.id.image);
                } else if (adapter.isMediaRepresentedByVideo(media)) {
                    return container.findViewById(R.id.video);
                }
            }
        }

        return null;
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

                    Media media = adapter.media.get(mediaIndex);
                    View container = pager.findViewWithTag(media);
                    if (container == null) {
                        return true;
                    }

                    if (adapter.isMediaRepresentedByImage(media)) {
                        ContentPhotoView photoView = container.findViewById(R.id.image);

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
                    } else if (adapter.isMediaRepresentedByVideo(media)) {
                        ContentPlayerView playerView = container.findViewById(R.id.video);
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
        this(parent, mediaCornerRadius, maxAspectRatio, true);
    }

    public MediaPagerAdapter(@NonNull MediaPagerAdapter.MediaPagerAdapterParent parent, float mediaCornerRadius, float maxAspectRatio, boolean representVideoByThumbnail) {
        this.parent = parent;
        this.mediaCornerRadius = mediaCornerRadius;
        this.maxAspectRatio = maxAspectRatio;
        this.representVideoByThumbnail = representVideoByThumbnail;

        parent.getLifecycleOwner().getLifecycle().addObserver(this);
    }

    private boolean isMediaRepresentedByImage(@NonNull Media media) {
        return media.type == Media.MEDIA_TYPE_IMAGE || (media.type == Media.MEDIA_TYPE_VIDEO && representVideoByThumbnail);
    }

    private boolean isMediaRepresentedByVideo(@NonNull Media media) {
        return media.type == Media.MEDIA_TYPE_VIDEO && !representVideoByThumbnail;
    }

    public void setMedia(@NonNull List<Media> media) {
        Log.d("MediaPagerAdapter.setMedia");
        if (this.media == null || !this.media.equals(media)) {
            releasePlayers();

            this.media = new ArrayList<>(media);
            fixedAspectRatio = Media.getMaxAspectRatio(media);

            if (maxAspectRatio != 0) {
                fixedAspectRatio = Math.min(fixedAspectRatio, maxAspectRatio);
            }
            notifyDataSetChanged();
        }
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

    public void setAllowSaving(boolean allowSaving) {
        this.allowSaving = allowSaving;
    }

    public void setChat(ChatId chatId) {
        isChatMedia = true;
        this.chatId = chatId;
    }

    public void setOffscreenPlayerLimit(int limit) {
        if (limit >= 1 || limit == OFFSCREEN_PLAYER_LIMIT_DEFAULT) {
            offscreenPlayerLimit = limit;
        }
    }

    private Media getMediaForPosition(int position) {
        return 0 <= position && position < media.size() ? media.get(position) : null;
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("MediaPagerAdapter.onCreateViewHolder");
        return new MediaViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.media_pager_item, parent, false));
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        Log.d("MediaPagerAdapter.onDetachedFromRecyclerView");
        super.onDetachedFromRecyclerView(recyclerView);
        releasePlayers();
    }

    @Override
    public void onViewAttachedToWindow(@NonNull MediaViewHolder holder) {
        Log.d("MediaPagerAdapter.onViewAttachedToWindow");
        super.onViewAttachedToWindow(holder);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull MediaViewHolder holder) {
        Log.d("MediaPagerAdapter.onViewDetachedFromWindow");
        super.onViewDetachedFromWindow(holder);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewRecycled(@NonNull MediaViewHolder holder) {
        Log.d("MediaPagerAdapter.onViewRecycled");
        super.onViewRecycled(holder);
        holder.imageView.setOnClickListener(null);
        holder.playerView.setOnTouchListener(null);
        if (holder.mediaItem != null) {
            releasePlayer(holder.mediaItem, holder.playerView);
            holder.mediaItem = null;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        Log.d("MediaPagerAdapter.onBindViewHolder");
        holder.imageView.setTransitionName("");
        holder.playerView.setTransitionName("");
        holder.imageView.setVisibility(View.GONE);
        holder.playerView.setVisibility(View.GONE);

        if (overrideMediaPadding) {
            holder.itemView.setPadding(mediaInsetLeft, mediaInsetTop, mediaInsetRight, mediaInsetBottom);
        }
        final Media mediaItem = media.get(position);
        holder.mediaItem = mediaItem;
        holder.container.setTag(mediaItem);
        holder.container.setAspectRatio(fixedAspectRatio);

        int displayHeight = holder.container.getContext().getResources().getDisplayMetrics().heightPixels;
        holder.container.setMaxHeight((int) (displayHeight * MAX_MEDIA_RATIO));

        String transitionName = MediaPagerAdapter.getTransitionName(contentId, position);

        holder.imageView.setOnClickListener(null);
        holder.playerView.setOnTouchListener(null);

        if (isMediaRepresentedByVideo(mediaItem)) {
            holder.playerView.setResizeMode(com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT);

            if (mediaItem.width > 0) {
                holder.playerView.setAspectRatio(1f * mediaItem.height / mediaItem.width);
            }

            holder.playerView.setTransitionName(transitionName);
            holder.playerView.setVisibility(View.VISIBLE);
            initPlayer(mediaItem, holder.playerView);
        } else {
            holder.imageView.setTransitionName(transitionName);
            holder.imageView.setVisibility(View.VISIBLE);

            parent.getMediaThumbnailLoader().load(holder.imageView, mediaItem);

            if (mediaItem.type == Media.MEDIA_TYPE_VIDEO) {
                holder.playButton.setVisibility(View.VISIBLE);
            } else {
                holder.playButton.setVisibility(View.GONE);
            }

            if (mediaItem.file != null) {
                holder.imageView.setOnClickListener(v -> exploreMedia(holder.imageView, mediaItem, 0));
            }
        }
    }

    private void exploreMedia(@NonNull View view, @NonNull Media mediaItem, long currentTime) {
        int position = media.indexOf(mediaItem);
        Log.d("MediaPagerAdapter.exploreMedia " + position);

        if (position != -1) {
            Context ctx = recyclerView.getContext();

            Intent intent = new Intent(ctx, MediaExplorerActivity.class);
            intent.putExtra(MediaExplorerActivity.EXTRA_MEDIA, MediaExplorerViewModel.MediaModel.fromMedia(media));
            intent.putExtra(MediaExplorerActivity.EXTRA_SELECTED, position);
            intent.putExtra(MediaExplorerActivity.EXTRA_CONTENT_ID, contentId);
            intent.putExtra(MediaExplorerActivity.EXTRA_INITIAL_TIME, currentTime);
            intent.putExtra(MediaExplorerActivity.EXTRA_ALLOW_SAVING, allowSaving);
            if (isChatMedia) {
                intent.putExtra(MediaExplorerActivity.EXTRA_CHAT_ID, chatId);
            }

            if (ctx instanceof HalloActivity) {
                HalloActivity activity = (HalloActivity) ctx;
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, view, view.getTransitionName());
                activity.startActivityForResult(intent, 0, options.toBundle());
            } else {
                parent.startActivity(intent);
            }
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
        Log.d("MediaPagerAdapter.pausePlayers");
        if (recyclerView == null) {
            return;
        }

        if (media != null) {
            for (Media mediaItem : media) {
                WrappedPlayer wrappedPlayer = playerMap.get(mediaItem);
                if (wrappedPlayer != null) {
                    wrappedPlayer.pause();
                }
            }
        }
    }

    private void releasePlayers() {
        Log.d("MediaPagerAdapter.releasePlayers");
        if (recyclerView == null) {
            return;
        }

        if (media != null) {
            for (Media mediaItem : media) {
                if (isMediaRepresentedByVideo(mediaItem)) {
                    View container = recyclerView.findViewWithTag(mediaItem);

                    if (container != null) {
                        releasePlayer(mediaItem, container.findViewById(R.id.video));
                    }
                }
            }
        }
    }

    public void refreshPlayers(int currentPosition) {
        Log.d("MediaPagerAdapter.refreshPlayers " + currentPosition);
        if (recyclerView == null) {
            return;
        }

        if (media != null) {
            for (int position = 0; position < media.size(); position++) {
                final Media mediaItem = getMediaForPosition(position);
                if (mediaItem != null && isMediaRepresentedByVideo(mediaItem)) {
                    final View container = recyclerView.findViewWithTag(mediaItem);
                    if (container == null) {
                        continue;
                    }

                    final ContentPlayerView playerView = container.findViewById(R.id.video);
                    final WrappedPlayer wrappedPlayer = playerMap.get(mediaItem);

                    if (Math.abs(currentPosition - position) > offscreenPlayerLimit) {
                        releasePlayer(mediaItem, playerView);
                    } else {
                        if (playerView != null && wrappedPlayer == null) {
                            initPlayer(mediaItem, playerView);
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initPlayer(@NonNull final Media mediaItem,
                            @NonNull final ContentPlayerView playerView) {
        if (playerMap.get(mediaItem) != null) {
            Log.w("MediaPagerAdapter.initVideoPlayer called on a view with already attached media player!");
            releasePlayer(mediaItem, playerView);
        }

        if (mediaItem.file != null) {
            Log.d("MediaPagerAdapter.initPlayer " + mediaItem);

            final DataSource.Factory dataSourceFactory;
            final MediaItem exoMediaItem;
            if (mediaItem.isStreamingVideo()) {
                final ChunkedMediaParameters chunkedParameters;
                try {
                    chunkedParameters = ChunkedMediaParameters.computeFromBlobSize(mediaItem.blobSize, mediaItem.chunkSize);
                } catch (ChunkedMediaParametersException e) {
                    Log.e("MediaPagerAdapter.initPlayer invalid chunk parameters", e);
                    return;
                }
                dataSourceFactory = ExoUtils.getChunkedMediaDataSourceFactory(mediaItem.rowId, mediaItem.url, chunkedParameters, mediaItem.file);
                exoMediaItem = ExoUtils.getChunkedMediaItem(mediaItem.rowId, mediaItem.url);
            } else {
                dataSourceFactory = ExoUtils.getDefaultDataSourceFactory(playerView.getContext());
                exoMediaItem = ExoUtils.getUriMediaItem(Uri.fromFile(mediaItem.file));
            }

            playerView.setPauseHiddenPlayerOnScroll(true);
            playerView.setControllerAutoShow(true);
            final MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(exoMediaItem);

            final SimpleExoPlayer player = new SimpleExoPlayer.Builder(playerView.getContext()).build();
            final WrappedPlayer wrappedPlayer = new WrappedPlayer(player);

            playerMap.put(mediaItem, wrappedPlayer);
            playerView.setPlayer(player);

            player.addListener(new Player.EventListener() {
                @Override
                public void onPlaybackStateChanged(int state) {
                    if (state == Player.STATE_READY && !wrappedPlayer.isPlayerInitialized) {
                        wrappedPlayer.isPlayerInitialized = true;
                        wrappedPlayer.seekToThumbnailFrame();
                    }
                }

                @Override
                public void onIsPlayingChanged(boolean isPlaying) {
                    playerView.setKeepScreenOn(isPlaying);
                }
            });

            player.setRepeatMode(Player.REPEAT_MODE_ALL);
            player.setMediaSource(mediaSource);
            player.prepare();

            PlayerControlView controlView = playerView.findViewById(R.id.exo_controller);
            controlView.setControlDispatcher(new ControlDispatcher() {
                @Override
                public boolean dispatchPrepare(Player player) {
                    return false;
                }

                @Override
                public boolean dispatchSetPlayWhenReady(@NonNull Player player, boolean playWhenReady) {
                    exploreMedia(playerView, mediaItem, player.getCurrentPosition());
                    return false;
                }

                @Override
                public boolean dispatchSeekTo(Player player, int windowIndex, long positionMs) {
                    return false;
                }

                @Override
                public boolean dispatchPrevious(Player player) {
                    return false;
                }

                @Override
                public boolean dispatchNext(Player player) {
                    return false;
                }

                @Override
                public boolean dispatchRewind(Player player) {
                    return false;
                }

                @Override
                public boolean dispatchFastForward(Player player) {
                    return false;
                }

                @Override
                public boolean dispatchSetRepeatMode(Player player, int repeatMode) {
                    return false;
                }

                @Override
                public boolean dispatchSetShuffleModeEnabled(Player player, boolean shuffleModeEnabled) {
                    return false;
                }

                @Override
                public boolean dispatchStop(Player player, boolean reset) {
                    return false;
                }

                @Override
                public boolean dispatchSetPlaybackParameters(Player player, PlaybackParameters playbackParameters) {
                    return false;
                }

                @Override
                public boolean isRewindEnabled() {
                    return false;
                }

                @Override
                public boolean isFastForwardEnabled() {
                    return false;
                }
            });

            GestureDetector doubleTapDetector = new GestureDetector(playerView.getContext(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    Player player = playerView.getPlayer();
                    exploreMedia(playerView, mediaItem, player != null ? player.getCurrentPosition() : 0);
                    return true;
                }
            });

            ScaleGestureDetector scaleDetector = new ScaleGestureDetector(playerView.getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
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
                        Player player = playerView.getPlayer();
                        exploreMedia(playerView, mediaItem, player != null ? player.getCurrentPosition() : 0);
                    }
                }
            });

            playerView.setOnTouchListener((view, event) -> doubleTapDetector.onTouchEvent(event) || scaleDetector.onTouchEvent(event));
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void releasePlayer(@NonNull Media mediaItem, @Nullable ContentPlayerView playerView) {
        if (playerView != null) {
            playerView.setPauseHiddenPlayerOnScroll(false);
            playerView.setPlayer(null);
            playerView.setOnTouchListener(null);
        }
        final WrappedPlayer wrappedPlayer = playerMap.get(mediaItem);
        if (wrappedPlayer != null) {
            wrappedPlayer.getPlayer().stop();
            wrappedPlayer.getPlayer().release();
            playerMap.remove(mediaItem);
            Log.d("MediaPagerAdapter.releaseVideoPlayer " + mediaItem);
        }
    }

    private static class WrappedPlayer {
        private static final long INITIAL_FRAME_TIME = 1000;

        private final SimpleExoPlayer exoPlayer;
        private boolean isVideoAtStart;
        boolean isPlayerInitialized;

        WrappedPlayer(SimpleExoPlayer exoPlayer) {
            this.exoPlayer = exoPlayer;
        }

        SimpleExoPlayer getPlayer() {
            return exoPlayer;
        }

        void seekToThumbnailFrame() {
            if (!exoPlayer.isPlaying() && (exoPlayer.getDuration() == exoPlayer.getCurrentPosition() || exoPlayer.getCurrentPosition() == 0)) {
                isVideoAtStart = true;

                if (exoPlayer.getDuration() > INITIAL_FRAME_TIME) {
                    exoPlayer.seekTo(INITIAL_FRAME_TIME);
                } else {
                    exoPlayer.seekTo(exoPlayer.getDuration() / 2);
                }
            }
        }

        void play() {
            if (exoPlayer != null) {
                if (isVideoAtStart) {
                    exoPlayer.seekTo(0);
                    isVideoAtStart = false;
                }

                exoPlayer.setPlayWhenReady(true);
            }
        }

        void pause() {
            if (exoPlayer != null) {
                exoPlayer.setPlayWhenReady(false);
                seekToThumbnailFrame();
            }
        }
    }

    public class MediaViewHolder extends RecyclerView.ViewHolder {
        final ContentPhotoView imageView;
        final ContentPlayerView playerView;
        final ProgressBar progressView;
        final View playButton;
        final AspectRatioFrameLayout container;
        Media mediaItem;

        public MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.container);
            imageView = itemView.findViewById(R.id.image);
            playerView = itemView.findViewById(R.id.video);
            progressView = itemView.findViewById(R.id.media_progress);
            playButton = itemView.findViewById(R.id.play);

            imageView.setCornerRadius(mediaCornerRadius);
            imageView.setSinglePointerDragStartDisabled(true);
            imageView.setDrawDelegate(parent.getDrawDelegateView());
            imageView.setMaxAspectRatio(maxAspectRatio);
            imageView.setProgressView(progressView);
        }
    }
}

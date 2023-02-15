package com.halloapp.katchup.media;

import android.net.Uri;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.halloapp.content.Media;
import com.halloapp.media.ExoUtils;
import com.halloapp.widget.ContentPlayerView;

public class KatchupExoPlayer implements LifecycleEventObserver {

    private static final long INITIAL_FRAME_TIME = 1000;
    private static final int LOAD_CONTROL_MIN_BUFFER_MS = 1000;
    private static final int LOAD_CONTROL_MAX_BUFFER_MS = 2000;

    private final SimpleExoPlayer exoPlayer;
    private boolean isVideoAtStart;
    boolean isPlayerInitialized;

    private LifecycleOwner lifecycleOwner;

    public static KatchupExoPlayer forSelfieView(@NonNull ContentPlayerView contentPlayerView, @NonNull Media media) {
        contentPlayerView.setVisibility(View.VISIBLE);
        KatchupExoPlayer wrappedPlayer = KatchupExoPlayer.fromPlayerView(contentPlayerView);
        contentPlayerView.setPlayer(wrappedPlayer.getPlayer());

        final DataSource.Factory dataSourceFactory = ExoUtils.getDefaultDataSourceFactory(contentPlayerView.getContext());
        final MediaItem exoMediaItem = ExoUtils.getUriMediaItem(Uri.fromFile(media.file));

        contentPlayerView.setPauseHiddenPlayerOnScroll(false);
        contentPlayerView.setControllerAutoShow(true);
        final MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(exoMediaItem);

        contentPlayerView.setUseController(false);

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.CONTENT_TYPE_MOVIE)
                .build();
        SimpleExoPlayer player = wrappedPlayer.getPlayer();
        player.setAudioAttributes(audioAttributes, false);
        player.setRepeatMode(Player.REPEAT_MODE_ALL);
        player.setMediaSource(mediaSource);
        player.setPlayWhenReady(true);
        player.setVolume(0);
        player.prepare();

        return wrappedPlayer;
    }

    public static KatchupExoPlayer forVideoReaction(@NonNull ContentPlayerView contentPlayerView, @NonNull Media media) {
        contentPlayerView.setVisibility(View.VISIBLE);
        KatchupExoPlayer wrappedPlayer = KatchupExoPlayer.fromPlayerView(contentPlayerView);
        contentPlayerView.setPlayer(wrappedPlayer.getPlayer());
        contentPlayerView.setAspectRatio(1f);

        contentPlayerView.setPauseHiddenPlayerOnScroll(true);
        contentPlayerView.setControllerAutoShow(true);
        contentPlayerView.setUseController(false);

        SimpleExoPlayer player = wrappedPlayer.getPlayer();
        player.setRepeatMode(Player.REPEAT_MODE_ALL);
        if (media.file != null) {
            final DataSource.Factory dataSourceFactory;
            final MediaItem exoMediaItem;
            dataSourceFactory = ExoUtils.getDefaultDataSourceFactory(contentPlayerView.getContext());
            exoMediaItem = ExoUtils.getUriMediaItem(Uri.fromFile(media.file));
            final MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(exoMediaItem);
            player.setMediaSource(mediaSource);
        }
        player.setPlayWhenReady(true);
        player.setVolume(0);
        player.prepare();

        return wrappedPlayer;
    }

    public static KatchupExoPlayer fromPlayerView(@NonNull ContentPlayerView contentPlayerView) {
        /**
         * I attempted this due to this SO post: https://stackoverflow.com/a/64900778/11817085
         * The claim is that when the repeat mode is set to REPEAT_MODE_ALL, ExoPlayer will
         * choose to line up multiple copies of the video in memory. I found this difficult to
         * believe, but on adding limits to the amount of loading to be performed the memory
         * usage by ExoPlayer decreased by approximately 80%. If ExoPlayer is attempting to use
         * the bufferForPlaybackAfterRebuffer value to determine how much video to load into
         * memory then this reduction would 'make sense' as we currently use 1000ms for the
         * selfie and DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS is 5000ms, so under this
         * theory ExoPlayer would be loading 4 extra copies of the video into memory in addition
         * to the one necessary copy, so getting rid of the 4 extras would be an 80% decrease.
         */
        LoadControl defaultLoadControl = new DefaultLoadControl.Builder()
                .setBufferDurationsMs(LOAD_CONTROL_MIN_BUFFER_MS, LOAD_CONTROL_MAX_BUFFER_MS, LOAD_CONTROL_MIN_BUFFER_MS, LOAD_CONTROL_MIN_BUFFER_MS)
                .build();
        final SimpleExoPlayer player = new SimpleExoPlayer.Builder(contentPlayerView.getContext())
                .setLoadControl(defaultLoadControl)
                .build();
        return new KatchupExoPlayer(player);
    }

    public KatchupExoPlayer(SimpleExoPlayer exoPlayer) {
        this.exoPlayer = exoPlayer;
        if (exoPlayer != null) {
            exoPlayer.addListener(new Player.EventListener() {
                @Override
                public void onPlaybackStateChanged(int state) {
                    if (state == Player.STATE_READY && !isPlayerInitialized) {
                        isPlayerInitialized = true;
                        seekToThumbnailFrame();
                    }
                }
            });
        }
    }

    public SimpleExoPlayer getPlayer() {
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

    public void play() {
        if (exoPlayer != null) {
            if (isVideoAtStart) {
                exoPlayer.seekTo(0);
                isVideoAtStart = false;
            }

            exoPlayer.setPlayWhenReady(true);
        }
    }

    public void pause() {
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(false);
            seekToThumbnailFrame();
        }
    }

    public void destroy() {
        if (exoPlayer != null) {
            exoPlayer.stop(true);
        }
        if (lifecycleOwner != null) {
            lifecycleOwner.getLifecycle().removeObserver(this);
        }
    }

    public void observeLifecycle(LifecycleOwner lifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner;
        lifecycleOwner.getLifecycle().addObserver(this);
    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        if (event.getTargetState() == Lifecycle.State.DESTROYED) {
            this.lifecycleOwner.getLifecycle().removeObserver(this);
            this.lifecycleOwner = null;
            destroy();
        }
    }
}

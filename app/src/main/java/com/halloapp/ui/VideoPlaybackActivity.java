package com.halloapp.ui;

import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.halloapp.R;
import com.halloapp.util.Log;

public class VideoPlaybackActivity extends HalloActivity {

    private PlayerView playerView;
    private SimpleExoPlayer player;
    private MediaSource mediaSource;

    private int startWindow;
    private long startPosition;

    private static final String KEY_WINDOW = "window";
    private static final String KEY_POSITION = "position";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video_playback);

        if (Build.VERSION.SDK_INT >= 28) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        playerView = findViewById(R.id.player_view);
        playerView.requestFocus();

        playerView.setSystemUiVisibility(SystemUiVisibility.getDefaultSystemUiVisibilityDark(this));

        final PlayerControlView playerControlView = playerView.findViewById(R.id.exo_controller);
        playerControlView.setFitsSystemWindows(true);
        ((View)playerControlView.findViewById(R.id.exo_next).getParent()).setVisibility(View.GONE);
        playerControlView.getChildAt(0).setBackgroundColor(0x60000000);

        playerView.setOnSystemUiVisibilityChangeListener(visibility -> {
            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                playerView.showController();
            }
        });
        playerControlView.addVisibilityListener(visibility -> {
            if (visibility == View.GONE) {
                playerView.setSystemUiVisibility(SystemUiVisibility.getFullScreenSystemUiVisibilityDark(getBaseContext()));
            }
        });

        if (savedInstanceState != null) {
            startWindow = savedInstanceState.getInt(KEY_WINDOW);
            startPosition = savedInstanceState.getLong(KEY_POSITION);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initializePlayer();
            if (playerView != null) {
                playerView.onResume();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Util.SDK_INT <= 23 || player == null) {
            initializePlayer();
            if (playerView != null) {
                playerView.onResume();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            if (playerView != null) {
                playerView.onPause();
            }
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            if (playerView != null) {
                playerView.onPause();
            }
            releasePlayer();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        updateStartPosition();
        outState.putInt(KEY_WINDOW, startWindow);
        outState.putLong(KEY_POSITION, startPosition);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return playerView.dispatchKeyEvent(event) || super.dispatchKeyEvent(event);
    }

    private void initializePlayer() {
        if (player == null) {
            final DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, "hallo");
            mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(getIntent().getData());

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.CONTENT_TYPE_MOVIE)
                    .build();

            player = new SimpleExoPlayer.Builder(this).build();
            player.setPlayWhenReady(true);
            player.setRepeatMode(Player.REPEAT_MODE_ALL);
            player.setAudioAttributes(audioAttributes, true);
            playerView.setPlayer(player);
        }
        boolean haveStartPosition = startWindow != C.INDEX_UNSET;
        if (haveStartPosition) {
            player.seekTo(startWindow, startPosition);
        }
        player.prepare(mediaSource, !haveStartPosition, false);
    }

    private void releasePlayer() {
        if (player != null) {
            updateStartPosition();
            player.release();
            player = null;
            mediaSource = null;
        }
    }

    private void updateStartPosition() {
        if (player != null) {
            startWindow = player.getCurrentWindowIndex();
            startPosition = Math.max(0, player.getContentPosition());
        }
    }
}

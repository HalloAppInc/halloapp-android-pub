package com.halloapp.katchup;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.transition.ChangeBounds;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import com.google.android.exoplayer2.ControlDispatcher;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.halloapp.Constants;
import com.halloapp.Preferences;
import com.halloapp.R;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Media;
import com.halloapp.katchup.compose.CameraComposeFragment;
import com.halloapp.katchup.compose.ComposeFragment;
import com.halloapp.katchup.compose.SelfieComposerViewModel;
import com.halloapp.katchup.compose.TextComposeFragment;
import com.halloapp.katchup.media.KatchupExoPlayer;
import com.halloapp.media.ExoUtils;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.proto.server.MomentNotification;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.camera.HalloCamera;
import com.halloapp.util.StringUtils;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.ContentPlayerView;
import com.halloapp.widget.SnackbarHelper;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

import pub.devrel.easypermissions.EasyPermissions;


public class SelfiePostComposerActivity extends HalloActivity {

    public static Intent startFromNotification(@NonNull Context context, long notificationId, long notificationTime, int type, String prompt) {
        Intent i = new Intent(context, SelfiePostComposerActivity.class);
        i.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
        if (type == MomentNotification.Type.LIVE_CAMERA_VALUE) {
            i.putExtra(EXTRA_TYPE, Type.LIVE_CAPTURE);
        } else {
            i.putExtra(EXTRA_TYPE, Type.TEXT_COMPOSE);
        }
        if (!TextUtils.isEmpty(prompt)) {
            i.putExtra(EXTRA_PROMPT, prompt);
        }
        i.putExtra(EXTRA_NOTIFICATION_TIME, notificationTime);
        return i;
    }

    @WorkerThread
    public static Intent startFromApp(@NonNull Context context) {
        Preferences preferences = Preferences.getInstance();
        int type = preferences.getMomentNotificationType();
        long notificationId = preferences.getMomentNotificationId();
        long timestamp = preferences.getMomentNotificationTimestamp();
        String prompt = preferences.getMomentNotificationPrompt();

        if (type == MomentNotification.Type.LIVE_CAMERA_VALUE) {
            return SelfiePostComposerActivity.startCapture(context, notificationId, timestamp, prompt);
        } else if (type == MomentNotification.Type.TEXT_POST_VALUE) {
            return SelfiePostComposerActivity.startText(context, notificationId, timestamp, prompt);
        } else if (type == MomentNotification.Type.PROMPT_POST_VALUE) {
            return SelfiePostComposerActivity.startPrompt(context, notificationId, timestamp, prompt);
        } else {
            throw new IllegalStateException("Unexpected moment notification type " + type);
        }
    }

    public static Intent startCapture(@NonNull Context context, long notificationId, long notificationTime, @Nullable String prompt) {
        Intent i = new Intent(context, SelfiePostComposerActivity.class);
        i.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
        i.putExtra(EXTRA_TYPE, Type.LIVE_CAPTURE);
        i.putExtra(EXTRA_NOTIFICATION_TIME, notificationTime);
        if (!TextUtils.isEmpty(prompt)) {
            i.putExtra(EXTRA_PROMPT, prompt);
        }
        return i;
    }

    public static Intent startText(@NonNull Context context, long notificationId, long notificationTime, @Nullable String prompt) {
        Intent i = new Intent(context, SelfiePostComposerActivity.class);
        i.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
        i.putExtra(EXTRA_TYPE, Type.TEXT_COMPOSE);
        i.putExtra(EXTRA_NOTIFICATION_TIME, notificationTime);
        if (!TextUtils.isEmpty(prompt)) {
            i.putExtra(EXTRA_PROMPT, prompt);
        }
        return i;
    }

    public static Intent startPrompt(@NonNull Context context, long notificationId, long notificationTime, @Nullable String prompt) {
        Intent i = new Intent(context, SelfiePostComposerActivity.class);
        i.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
        i.putExtra(EXTRA_TYPE, Type.LIVE_CAPTURE);
        i.putExtra(EXTRA_NOTIFICATION_TIME, notificationTime);
        if (!TextUtils.isEmpty(prompt)) {
            i.putExtra(EXTRA_PROMPT, prompt);
        }
        return i;
    }

    private static final String EXTRA_TYPE = "compose_type";
    private static final String EXTRA_NOTIFICATION_ID = "notification_id";
    private static final String EXTRA_NOTIFICATION_TIME = "notification_time";
    private static final String EXTRA_PROMPT = "notification_prompt";

    private static final int SELFIE_COUNTDOWN_DURATION_MS = 3000;
    private static final int SELFIE_CAPTURE_DURATION_MS = 1000;

    private static final int REQUEST_CODE_ASK_CAMERA_AND_AUDIO_PERMISSION = 1;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Type.LIVE_CAPTURE, Type.TEXT_COMPOSE})
    public @interface Type {
        int LIVE_CAPTURE = 1;
        int TEXT_COMPOSE = 2;
    }

    private HalloCamera camera;
    private ComposeFragment composerFragment;

    private MediaThumbnailLoader mediaThumbnailLoader;

    private SelfieComposerViewModel viewModel;
    private int permissionRequestRetries = 0;

    private FrameLayout fragmentContainer;

    private View selfieCameraContainer;
    private ImageView capturedSelfiePreview;
    private ContentPlayerView selfiePlayerView;

    private PreviewView selfieCameraPreview;

    private TextView selfieCountdownTextView;
    private TextView selfieCountdownHeaderView;

    private TextView composerCountdownTextView;

    private View sendContainer;
    private View capturedSelfieContainer;
    private View selfieCountdownContainer;
    private View removeSelfieButton;

    private File selfieFile;
    private int selfieType;
    private KatchupExoPlayer selfiePlayer;

    private long composerStartTimeMs;

    private CountDownTimer selfieCountDownTimer;
    private CountDownTimer composerCountdownTimer;

    private float selfieTranslationX;
    private float selfieTranslationY;

    private int selfieVerticalMargin;
    private int selfieHorizontalMargin;

    private @Type int composeType;
    private String prompt;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getBooleanExtra(Notifications.EXTRA_IS_NOTIFICATION, false)) {
            Analytics.getInstance().notificationOpened(getIntent().getStringExtra(Notifications.EXTRA_NOTIFICATION_TYPE));
        }

        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);

        final Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        mediaThumbnailLoader = new MediaThumbnailLoader(this, Math.min(Constants.MAX_IMAGE_DIMENSION, Math.max(point.x, point.y)));

        selfieVerticalMargin = getResources().getDimensionPixelSize(R.dimen.compose_selfie_vertical_margin);
        selfieHorizontalMargin = getResources().getDimensionPixelSize(R.dimen.compose_selfie_horizontal_margin);

        setContentView(R.layout.activity_selfie_composer);

        prompt = getIntent().getExtras().getString(EXTRA_PROMPT, null);

        composeType = getIntent().getIntExtra(EXTRA_TYPE, Type.LIVE_CAPTURE);

        fragmentContainer = findViewById(R.id.fragment_container);
        selfieCameraContainer = findViewById(R.id.selfie_container);
        selfieCameraPreview = findViewById(R.id.selfie_preview);

        capturedSelfieContainer = findViewById(R.id.preview_container);
        capturedSelfiePreview = findViewById(R.id.selfie_image);
        selfiePlayerView = findViewById(R.id.selfie_player);

        selfieCountdownContainer = findViewById(R.id.selfie_countdown_container);
        selfieCountdownTextView = findViewById(R.id.selfie_countdown_text);
        selfieCountdownHeaderView = findViewById(R.id.selfie_countdown_header);

        composerCountdownTextView = findViewById(R.id.remaining_timer_counter);
        removeSelfieButton = findViewById(R.id.remove_selfie);

        sendContainer = findViewById(R.id.send_container);
        View sendButton = findViewById(R.id.send_button);
        sendButton.setOnClickListener(v -> {
            sendButton.setEnabled(false);

            View transitionView = composerFragment.getPreview();
            transitionView.setTransitionName(MainFragment.COMPOSER_VIEW_TRANSITION_NAME);

            viewModel.sendPost(composerFragment.getComposedMedia()).observe(this, post -> {
                if (post == null) {
                    SnackbarHelper.showWarning(this, R.string.failed_to_post);
                    sendButton.setEnabled(true);
                    return;
                }
                Analytics.getInstance().posted(composeType);
                post.addToStorage(ContentDb.getInstance());

                setResult(RESULT_OK);
                finishAfterTransition();
            });
        });

        findViewById(R.id.back).setOnClickListener(v -> onBackPressed());

        removeSelfieButton.setOnClickListener(v -> {
            onBackPressed();
        });

        composerStartTimeMs = getIntent().getLongExtra(EXTRA_NOTIFICATION_TIME, System.currentTimeMillis());

        viewModel = new ViewModelProvider(this, new SelfieComposerViewModel.Factory(composeType)).get(SelfieComposerViewModel.class);
        viewModel.getComposerState().observe(this, this::configureViewsForState);
        viewModel.setNotification(getIntent().getLongExtra(EXTRA_NOTIFICATION_ID, 0), getIntent().getLongExtra(EXTRA_NOTIFICATION_TIME, 0));

        initializeComposerFragment();
        // disable selfie drag
        //makeSelfieDraggable();

        requestCameraAndAudioPermission();

        initializeCamera();
        startComposerTimer();
    }

    private void makeSelfieDraggable() {
        capturedSelfieContainer.setOnTouchListener(new View.OnTouchListener() {

            private float startX;
            private float startY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    startX = event.getRawX();
                    startY = event.getRawY();
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    capturedSelfieContainer.setTranslationX(selfieTranslationX + (event.getRawX() - startX));
                    capturedSelfieContainer.setTranslationY(selfieTranslationY + (event.getRawY() - startY));
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    selfieTranslationX = capturedSelfieContainer.getTranslationX();
                    selfieTranslationY = capturedSelfieContainer.getTranslationY();
                    forceWithinBounds();
                    updateSelfieSelectedPosition();
                }
                return false;
            }

            private void forceWithinBounds() {
                float posX = capturedSelfieContainer.getX();
                float posY = capturedSelfieContainer.getY();

                int width = capturedSelfieContainer.getWidth();
                int height = capturedSelfieContainer.getHeight();

                if (capturedSelfieContainer.getX() + width > fragmentContainer.getRight() - selfieHorizontalMargin) {
                    selfieTranslationX = (fragmentContainer.getRight() - selfieHorizontalMargin) - capturedSelfieContainer.getRight();
                }
                if (posX < selfieHorizontalMargin) {
                    selfieTranslationX = selfieHorizontalMargin - capturedSelfieContainer.getLeft();
                }

                if (posY < fragmentContainer.getY() + selfieVerticalMargin) {
                    selfieTranslationY = (fragmentContainer.getTop() + selfieVerticalMargin) - capturedSelfieContainer.getTop();
                }
                if (posY + height > fragmentContainer.getBottom() - selfieVerticalMargin) {
                    selfieTranslationY = (fragmentContainer.getBottom() - selfieVerticalMargin) - capturedSelfieContainer.getBottom();
                }

                capturedSelfieContainer.setTranslationX(selfieTranslationX);
                capturedSelfieContainer.setTranslationY(selfieTranslationY);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!hasCameraAndAudioPermission()) {
            if (permissionRequestRetries > 0) {
                finish();
            } else {
                permissionRequestRetries++;
                requestCameraAndAudioPermission();
            }
        } else {
            permissionRequestRetries = 0;
        }
        if (camera != null && Objects.equals(viewModel.getComposerState().getValue(), SelfieComposerViewModel.ComposeState.COMPOSING_SELFIE)) {
            camera.bindCameraUseCases();
        }

        if (composeType == Type.LIVE_CAPTURE) {
            Analytics.getInstance().openScreen("composeMedia");
        } else if (composeType == Type.TEXT_COMPOSE) {
            Analytics.getInstance().openScreen("composeText");
        } else {
            Analytics.getInstance().openScreen("composeUnknown");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaThumbnailLoader.destroy();
        if (composerCountdownTimer != null) {
            composerCountdownTimer.cancel();
            composerCountdownTimer = null;
        }
        if (selfiePlayer != null) {
            selfiePlayer.destroy();
            selfiePlayer = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (viewModel.onBackPressed()) {
            super.onBackPressed();
        }
    }

    private void updateSelfieSelectedPosition() {
        float fragmentX = fragmentContainer.getX() + selfieHorizontalMargin;
        float fragmentY = fragmentContainer.getY() + selfieVerticalMargin;

        float selfiePosX = (capturedSelfieContainer.getX() - fragmentX) / ((fragmentContainer.getWidth() - (2 * selfieHorizontalMargin)) - capturedSelfieContainer.getWidth());
        float selfiePosY = (capturedSelfieContainer.getY() - fragmentY) / ((fragmentContainer.getHeight() - (2 * selfieVerticalMargin)) - capturedSelfieContainer.getHeight());

        viewModel.setSelfiePosition(selfiePosX, selfiePosY);
    }

    public MediaThumbnailLoader getMediaThumbnailLoader() {
        return mediaThumbnailLoader;
    }

    private void startComposerTimer() {
        long msRemaining = (DateUtils.MINUTE_IN_MILLIS * 2) - (System.currentTimeMillis() - composerStartTimeMs);
        composerCountdownTimer = new CountDownTimer(msRemaining, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                composerCountdownTextView.setText(StringUtils.formatVoiceNoteDuration(SelfiePostComposerActivity.this, millisUntilFinished + 500));
            }

            @Override
            public void onFinish() {
                // TODO: actually do something when the user runs out of time
            }
        }.start();
    }

    private void initializeComposerFragment() {
        if (composeType == Type.TEXT_COMPOSE) {
            composerFragment = TextComposeFragment.newInstance(prompt);
        } else {
            composerFragment = CameraComposeFragment.newInstance(prompt);
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.fragment_container, composerFragment).commit();
    }

    private void initializeCamera() {
        camera = new HalloCamera(this, selfieCameraPreview, true, false, Surface.ROTATION_0, new HalloCamera.DefaultListener() {
            @Override
            public void onCaptureSuccess(File file, int type) {
                runOnUiThread(() -> {
                    selfieFile = file;
                    selfieType = type;
                    capturedSelfiePreview.setImageBitmap(selfieCameraPreview.getBitmap());
                    viewModel.onCapturedSelfie(file);
                });
            }

            @Override
            public void onCameraPermissionsMissing() {
                runOnUiThread(SelfiePostComposerActivity.this::requestCameraAndAudioPermission);
            }
        });
    }

    private void configureViewsForState(@SelfieComposerViewModel.ComposeState int state) {
        switch (state) {
            case SelfieComposerViewModel.ComposeState.COMPOSING_SELFIE:
                showSelfieCapture();
                break;
            case SelfieComposerViewModel.ComposeState.COMPOSING_CONTENT:
                showContentComposer();
                break;
            case SelfieComposerViewModel.ComposeState.READY_TO_SEND:
                showSendableState();
                break;
        }
    }

    private void showSendableState() {
        selfieCameraContainer.setVisibility(View.INVISIBLE);
        capturedSelfieContainer.setVisibility(View.VISIBLE);
        sendContainer.setVisibility(View.VISIBLE);
        mediaThumbnailLoader.load(capturedSelfiePreview, Media.createFromFile(selfieType, selfieFile), new ViewDataLoader.Displayer<ImageView, Bitmap>() {
            @Override
            public void showResult(@NonNull ImageView view, @Nullable Bitmap result) {
                if (result != null) {
                    capturedSelfiePreview.setImageBitmap(result);
                }
            }

            @Override
            public void showLoading(@NonNull ImageView view) {

            }
        });
        bindSelfieVideo();
        Transition changeBounds = new ChangeBounds();
        changeBounds.setInterpolator(new OvershootInterpolator());
        TransitionManager.beginDelayedTransition((ViewGroup) capturedSelfieContainer.getParent(), changeBounds);

        moveCaptureToCorner();
        viewModel.setSelfiePosition(1.0f, 0.0f);
        selfieCountdownContainer.setVisibility(View.GONE);
    }

    private void bindSelfieVideo() {
        if (selfieFile != null) {
            final DataSource.Factory dataSourceFactory;
            final MediaItem exoMediaItem;
                dataSourceFactory = ExoUtils.getDefaultDataSourceFactory(selfiePlayerView.getContext());
                exoMediaItem = ExoUtils.getUriMediaItem(Uri.fromFile(selfieFile));

            selfiePlayerView.setPauseHiddenPlayerOnScroll(true);
            selfiePlayerView.setControllerAutoShow(true);
            final MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(exoMediaItem);

            final SimpleExoPlayer player = new SimpleExoPlayer.Builder(selfiePlayerView.getContext()).build();
            final KatchupExoPlayer wrappedPlayer = new KatchupExoPlayer(player);
            if (selfiePlayer != null) {
                selfiePlayer.destroy();
            }
            selfiePlayer = wrappedPlayer;

            selfiePlayerView.setPlayer(player);
            selfiePlayerView.setUseController(false);
            selfiePlayerView.setVisibility(View.VISIBLE);

            player.addListener(new Player.EventListener() {
                @Override
                public void onIsPlayingChanged(boolean isPlaying) {
                    selfiePlayerView.setKeepScreenOn(isPlaying);
                    capturedSelfiePreview.setVisibility(isPlaying ? View.GONE : View.VISIBLE);
                }
            });

            player.setRepeatMode(Player.REPEAT_MODE_ALL);
            player.setMediaSource(mediaSource);
            player.setPlayWhenReady(true);
            player.setVolume(0);
            player.prepare();

            PlayerControlView controlView = selfiePlayerView.findViewById(R.id.exo_controller);
            controlView.setControlDispatcher(new ControlDispatcher() {
                @Override
                public boolean dispatchPrepare(Player player) {
                    return false;
                }

                @Override
                public boolean dispatchSetPlayWhenReady(@NonNull Player player, boolean playWhenReady) {
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
        }
    }

    private void showContentComposer() {
        selfieCameraContainer.setVisibility(View.GONE);
        capturedSelfieContainer.setVisibility(View.GONE);
        sendContainer.setVisibility(View.GONE);
        forceAbortSelfieCountdown();
    }

    private void showSelfieCapture() {
        selfieCameraContainer.setVisibility(View.VISIBLE);
        sendContainer.setVisibility(View.GONE);
        capturedSelfieContainer.setVisibility(View.INVISIBLE);
        moveCaptureToPreview();
        camera.bindCameraUseCases();
        startSelfieCaptureSequence();
        if (selfiePlayer != null) {
            selfiePlayer.destroy();
            selfiePlayer = null;
        }
    }

    private void forceAbortSelfieCountdown() {
        if (selfieCountDownTimer != null) {
            selfieCountDownTimer.cancel();
            selfieCountdownContainer.setVisibility(View.GONE);
            selfieCountDownTimer = null;
        }
    }

    private void moveCaptureToPreview() {
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) capturedSelfieContainer.getLayoutParams();
        layoutParams.height = 0;
        layoutParams.bottomMargin = layoutParams.leftMargin = layoutParams.rightMargin = 0;
        layoutParams.topToTop = layoutParams.endToEnd = layoutParams.startToStart = layoutParams.bottomToBottom = selfieCameraContainer.getId();
        layoutParams.matchConstraintPercentWidth = 1;

        capturedSelfieContainer.setLayoutParams(layoutParams);
    }

    private void moveCaptureToCorner() {
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) capturedSelfieContainer.getLayoutParams();
        layoutParams.bottomMargin = layoutParams.topMargin = selfieVerticalMargin;
        layoutParams.leftMargin = layoutParams.rightMargin = selfieHorizontalMargin;
        layoutParams.height = ConstraintLayout.LayoutParams.WRAP_CONTENT;

        layoutParams.topToTop = layoutParams.endToEnd = fragmentContainer.getId();
        layoutParams.bottomToBottom = layoutParams.startToStart = ConstraintLayout.LayoutParams.UNSET;
        layoutParams.matchConstraintPercentWidth = 0.4f;
        layoutParams.constrainedWidth = true;

        selfieTranslationY = 0;
        selfieTranslationX = 0;
        capturedSelfieContainer.setTranslationY(0);
        capturedSelfieContainer.setTranslationX(0);
        capturedSelfieContainer.setLayoutParams(layoutParams);
    }

    private void startSelfieCaptureSequence() {
        selfieCountdownContainer.setVisibility(View.VISIBLE);
        selfieCountdownHeaderView.setVisibility(View.VISIBLE);
        selfieCountdownTextView.setText(String.valueOf(SELFIE_COUNTDOWN_DURATION_MS / 1000));

        if (selfieCountDownTimer != null) {
            Log.i("SelfiePostComposerActivity/startSelfieCapture countdown in progress");
            return;
        }
        selfieCountDownTimer = new CountDownTimer(SELFIE_COUNTDOWN_DURATION_MS + SELFIE_CAPTURE_DURATION_MS, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                if (millisUntilFinished > SELFIE_CAPTURE_DURATION_MS) {
                    long seconds = (millisUntilFinished + 500 - SELFIE_CAPTURE_DURATION_MS) / 1000;

                    selfieCountdownTextView.setVisibility(View.VISIBLE);
                    selfieCountdownTextView.setText(String.valueOf(seconds));
                } else {
                    selfieCountdownTextView.setVisibility(View.GONE);
                    camera.startRecordingVideo();
                }
            }

            @Override
            public void onFinish() {
                camera.stopRecordingVideo();
                selfieCountDownTimer = null;
            }
        }.start();
    }

    private boolean hasCameraAndAudioPermission() {
        final String[] permissions = { Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO };
        return EasyPermissions.hasPermissions(this, permissions);
    }

    private void requestCameraAndAudioPermission() {
        final String[] permissions = { Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO };
        if (!EasyPermissions.hasPermissions(this, permissions)) {
            EasyPermissions.requestPermissions(this, getString(R.string.camera_record_audio_permission_rationale),
                    REQUEST_CODE_ASK_CAMERA_AND_AUDIO_PERMISSION, permissions);
        }
    }
}

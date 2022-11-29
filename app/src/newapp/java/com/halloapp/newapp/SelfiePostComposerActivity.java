package com.halloapp.newapp;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.format.DateUtils;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.transition.ChangeBounds;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.content.Media;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.newapp.compose.CameraComposeFragment;
import com.halloapp.newapp.compose.SelfieComposerViewModel;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.camera.HalloCamera;
import com.halloapp.util.StringUtils;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.util.logs.Log;

import java.io.File;
import java.util.Objects;

import pub.devrel.easypermissions.EasyPermissions;

public class SelfiePostComposerActivity extends HalloActivity {

    private static final int SELFIE_COUNTDOWN_DURATION_MS = 3000;

    private static final int REQUEST_CODE_ASK_CAMERA_AND_AUDIO_PERMISSION = 1;

    private HalloCamera camera;
    private Fragment composerFragment;

    private MediaThumbnailLoader mediaThumbnailLoader;

    private SelfieComposerViewModel viewModel;
    private int permissionRequestRetries = 0;

    private FrameLayout fragmentContainer;

    private View selfieCameraContainer;
    private ImageView capturedSelfiePreview;

    private PreviewView selfieCameraPreview;

    private TextView selfieCountdownTextView;
    private TextView selfieCountdownHeaderView;

    private TextView composerCountdownTextView;

    private View sendContainer;
    private View capturedSelfieContainer;
    private View selfieCountdownContainer;

    private File selfieFile;
    private int selfieType;

    private long composerStartTimeMs;

    private CountDownTimer selfieCountDownTimer;
    private CountDownTimer composerCountdownTimer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        mediaThumbnailLoader = new MediaThumbnailLoader(this, Math.min(Constants.MAX_IMAGE_DIMENSION, Math.max(point.x, point.y)));

        setContentView(R.layout.activity_selfie_composer);

        fragmentContainer = findViewById(R.id.fragment_container);
        selfieCameraContainer = findViewById(R.id.selfie_container);
        selfieCameraPreview = findViewById(R.id.selfie_preview);

        capturedSelfieContainer = findViewById(R.id.preview_container);
        capturedSelfiePreview = findViewById(R.id.selfie_image);

        selfieCountdownContainer = findViewById(R.id.selfie_countdown_container);
        selfieCountdownTextView = findViewById(R.id.selfie_countdown_text);
        selfieCountdownHeaderView = findViewById(R.id.selfie_countdown_header);

        composerCountdownTextView = findViewById(R.id.remaining_timer_counter);


        sendContainer = findViewById(R.id.send_container);
        View sendButton = findViewById(R.id.send_button);
        sendButton.setOnClickListener(v -> {
            // TODO: actually send the media files
            finish();
        });

        // TODO: pass in actual start time
        composerStartTimeMs = System.currentTimeMillis();

        viewModel = new ViewModelProvider(this).get(SelfieComposerViewModel.class);
        viewModel.getComposerState().observe(this, this::configureViewsForState);

        initializeComposerFragment();

        requestCameraAndAudioPermission();

        initializeCamera();
        startComposerTimer();
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaThumbnailLoader.destroy();
        if (composerCountdownTimer != null) {
            composerCountdownTimer.cancel();
            composerCountdownTimer = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (viewModel.onBackPressed()) {
            super.onBackPressed();
        }
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
        // TODO: switch composer fragment based on text/gallery/camera
        composerFragment = new CameraComposeFragment();

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
                    viewModel.onCapturedSelfie(Uri.fromFile(file));
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
        Transition changeBounds = new ChangeBounds();
        changeBounds.setInterpolator(new OvershootInterpolator());
        TransitionManager.beginDelayedTransition((ViewGroup) capturedSelfieContainer.getParent(), changeBounds);

        moveCaptureToCorner();
        selfieCountdownContainer.setVisibility(View.GONE);
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
        layoutParams.bottomMargin = layoutParams.leftMargin = layoutParams.rightMargin = 0;
        layoutParams.topToTop = layoutParams.endToEnd = layoutParams.startToStart = layoutParams.bottomToBottom = selfieCameraContainer.getId();
        layoutParams.matchConstraintPercentWidth = 1;
        layoutParams.dimensionRatio = null;

        capturedSelfieContainer.setLayoutParams(layoutParams);
    }

    private void moveCaptureToCorner() {
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) capturedSelfieContainer.getLayoutParams();
        layoutParams.bottomMargin = getResources().getDimensionPixelSize(R.dimen.compose_selfie_bottom_margin);
        layoutParams.leftMargin = layoutParams.rightMargin = getResources().getDimensionPixelSize(R.dimen.compose_selfie_horizontal_margin);

        layoutParams.topToTop = layoutParams.endToEnd = ConstraintLayout.LayoutParams.UNSET;
        layoutParams.bottomToBottom = layoutParams.startToStart = fragmentContainer.getId();
        layoutParams.matchConstraintPercentWidth = 0.4f;
        layoutParams.dimensionRatio = "1:1";
        layoutParams.constrainedWidth = true;

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
        selfieCountDownTimer = new CountDownTimer(SELFIE_COUNTDOWN_DURATION_MS, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = (millisUntilFinished + 500) / 1000;

                selfieCountdownTextView.setText(String.valueOf(seconds));
            }

            @Override
            public void onFinish() {
                camera.takePhoto();
                selfieCountDownTimer = null;

                selfieCountdownHeaderView.setVisibility(View.GONE);
                selfieCountdownTextView.setText(R.string.selfie_countdown_complete);
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

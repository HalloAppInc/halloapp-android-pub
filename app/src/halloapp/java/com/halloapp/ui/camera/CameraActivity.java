package com.halloapp.ui.camera;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.transition.Fade;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import androidx.annotation.IntDef;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.halloapp.R;
import com.halloapp.content.Media;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.props.ServerProps;
import com.halloapp.ui.ContentComposerActivity;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.MomentComposerActivity;
import com.halloapp.ui.MomentViewerActivity;
import com.halloapp.ui.avatar.AvatarPreviewActivity;
import com.halloapp.ui.mediaedit.MediaEditActivity;
import com.halloapp.ui.mediapicker.GalleryItem;
import com.halloapp.ui.mediapicker.GalleryThumbnailLoader;
import com.halloapp.ui.mediapicker.MediaPickerActivity;
import com.halloapp.util.OrientationListener;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.SnackbarHelper;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class CameraActivity extends HalloActivity implements EasyPermissions.PermissionCallbacks {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({CameraMediaType.PHOTO, CameraMediaType.VIDEO, CameraMediaType.MOMENT, CameraMediaType.INVALID})
    public @interface CameraMediaType {
        int INVALID = -1;
        int PHOTO = 0;
        int VIDEO = 1;
        int MOMENT = 2;
    }

    public static final String EXTRA_CHAT_ID = "chat_id";
    public static final String EXTRA_GROUP_ID = "group_id";
    public static final String EXTRA_REPLY_POST_ID = "reply_post_id";
    public static final String EXTRA_REPLY_POST_MEDIA_INDEX = "reply_post_media_index";
    public static final String EXTRA_PURPOSE = "purpose";
    public static final String EXTRA_TARGET_MOMENT = "target_moment";
    public static final String EXTRA_TARGET_MOMENT_USER_ID = "target_moment_user_id";

    private static final int REQUEST_CODE_ASK_CAMERA_AND_AUDIO_PERMISSION = 1;
    private static final int REQUEST_CODE_SET_AVATAR = 2;
    private static final int REQUEST_CODE_SEND_MOMENT = 3;
    private static final int REQUEST_CODE_OPEN_COMPOSER = 4;
    private static final int REQUEST_CODE_CHOOSE_GALLERY = 5;

    public static final int PURPOSE_COMPOSE_ANY = 1;
    public static final int PURPOSE_USER_AVATAR = 2;
    public static final int PURPOSE_GROUP_AVATAR = 3;
    public static final int PURPOSE_MOMENT = 4;
    public static final int PURPOSE_MOMENT_PSA = 5;
    public static final int PURPOSE_COMPOSE = 6;

    private static final int MOMENT_DEADLINE_SEC = 120;
    private static final int VIDEO_WARNING_DURATION_SEC = 10;

    private final Object messageToken = new Object();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private @CameraMediaType int mediaTypeMode = CameraMediaType.PHOTO;

    private int shortAnimationDuration = 0;

    private OrientationListener orientationListener;

    private FrameLayout cameraCardView;
    private PreviewView cameraPreviewView;

    private CameraViewModel viewModel;
    private HalloCamera camera;

    private ActionBar actionBar;

    private Chronometer videoTimer;
    private Chronometer videoTimeLimitTimer;

    private View captureButton;
    private ImageView captureInner;
    private ImageButton flipCameraButton;

    private ImageButton toggleFlashButtonMoments;
    private ImageButton toggleFlashButton;

    private ImageView galleryPreview;
    private View galleryButton;

    private Drawable flashOnDrawable;
    private Drawable flashOffDrawable;
    private Drawable captureButtonDrawable;
    private AnimatedVectorDrawableCompat recordVideoStartDrawable;
    private AnimatedVectorDrawableCompat recordVideoStopDrawable;
    private AnimatedVectorDrawableCompat takePhotoStartDrawable;
    private AnimatedVectorDrawableCompat takePhotoStopDrawable;

    private int pagerMarginOffset;
    private ViewPager2 mediaTypePager;

    private int permissionRequestRetries = 0;

    private int purpose = PURPOSE_COMPOSE;
    private int orientationAngle = 0;
    private int maxVideoDurationSeconds;

    private GalleryThumbnailLoader galleryThumbnailLoader;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        getWindow().setExitTransition(new Fade());

        setContentView(R.layout.activity_camera);

        galleryThumbnailLoader = new GalleryThumbnailLoader(this, getResources().getDimensionPixelSize(R.dimen.camera_gallery_preview_size));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = Preconditions.checkNotNull(getSupportActionBar());
        actionBar.setDisplayHomeAsUpEnabled(true);

        purpose = getIntent().getIntExtra(EXTRA_PURPOSE, PURPOSE_COMPOSE_ANY);

        viewModel = new ViewModelProvider(this,
                new CameraViewModel.Factory(getApplication(), isRecordingVideoAllowed())).get(CameraViewModel.class);

        shortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);

        flashOnDrawable = ContextCompat.getDrawable(this, R.drawable.ic_flash_on);
        flashOffDrawable = ContextCompat.getDrawable(this, R.drawable.ic_flash_off);
        captureButtonDrawable = ContextCompat.getDrawable(this, R.drawable.ic_camera_capture_inner);
        recordVideoStartDrawable = AnimatedVectorDrawableCompat.create(this, R.drawable.record_video_start_animation);
        recordVideoStopDrawable = AnimatedVectorDrawableCompat.create(this, R.drawable.record_video_stop_animation);
        takePhotoStartDrawable = AnimatedVectorDrawableCompat.create(this, R.drawable.take_photo_start_animation);
        takePhotoStopDrawable = AnimatedVectorDrawableCompat.create(this, R.drawable.take_photo_stop_animation);

        galleryButton = findViewById(R.id.gallery_button);
        galleryPreview = findViewById(R.id.gallery_preview);

        final float cameraCardRadius = getResources().getDimension(R.dimen.camera_card_border_radius);
        cameraCardView = findViewById(R.id.cameraCard);
        cameraCardView.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), cameraCardRadius);
            }
        });
        cameraCardView.setClipToOutline(true);

        final float cameraViewRadius = getResources().getDimension(R.dimen.camera_preview_border_radius);
        cameraPreviewView = findViewById(R.id.cameraPreview);
        cameraPreviewView.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), cameraViewRadius);
            }
        });
        cameraPreviewView.setClipToOutline(true);
        videoTimer = findViewById(R.id.video_timer);
        videoTimeLimitTimer = findViewById(R.id.video_countdown_timer);
        captureButton = findViewById(R.id.capture);
        captureInner = findViewById(R.id.capture_inner);

        captureButton.setOnClickListener(v -> {
            Log.d("CameraActivity: capture button onClick");
            if (camera.isRecordingVideo()) {
                stopRecordingVideo();
            } else if (!camera.isCapturingPhoto()) {
                if (mediaTypeMode == CameraMediaType.PHOTO || mediaTypeMode == CameraMediaType.MOMENT) {
                    playCaptureStartAnimation(Media.MEDIA_TYPE_IMAGE);
                    camera.takePhoto();
                } else {
                    playCaptureStartAnimation(Media.MEDIA_TYPE_VIDEO);

                    hideOptionButtons();
                    startRecordingTimer();
                    mainHandler.postAtTime(this::stopRecordingVideo, messageToken, SystemClock.uptimeMillis() + maxVideoDurationSeconds * 1000L);
                    mainHandler.postAtTime(this::startTimeLimitTimer, messageToken, SystemClock.uptimeMillis() + (maxVideoDurationSeconds - VIDEO_WARNING_DURATION_SEC) * 1000L);


                    camera.startRecordingVideo();
                }
            }
        });

        viewModel.getLastGalleryItem().observe(this, this::updateGalleryButton);
        flipCameraButton = findViewById(R.id.flip_camera);
        flipCameraButton.setOnClickListener(v -> camera.flip());
        toggleFlashButtonMoments = findViewById(R.id.toggle_moment_flash);
        toggleFlashButton = findViewById(R.id.toggle_flash);
        toggleFlashButton.setOnClickListener(v -> {
            camera.toggleFlash();
            updateGalleryOrFlashButton();
        });
        toggleFlashButtonMoments.setOnClickListener(v -> {
            camera.toggleFlash();
            updateGalleryOrFlashButton();
        });
        galleryButton.setOnClickListener(v -> {
            Intent galleryIntent = MediaPickerActivity.pickFromCamera(this, isRecordingVideoAllowed());
            startActivityForResult(galleryIntent, REQUEST_CODE_CHOOSE_GALLERY);
        });
        toggleFlashButton.setImageDrawable(flashOffDrawable);

        orientationListener = new OrientationListener(this);
        orientationListener.getRotationMode().observe(this, this::onRotationChanged);

        setupCamera();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        pagerMarginOffset = displayMetrics.widthPixels / 3;

        final MediaTypeAdapter adapter;
        if (getIntent().getParcelableExtra(EXTRA_CHAT_ID) != null || getIntent().getParcelableExtra(EXTRA_GROUP_ID) != null || purpose == PURPOSE_COMPOSE) {
            adapter = new MediaTypeAdapter(new int[]{CameraMediaType.PHOTO, CameraMediaType.VIDEO,});
        } else {
            adapter = new MediaTypeAdapter(new int[]{CameraMediaType.MOMENT, CameraMediaType.PHOTO, CameraMediaType.VIDEO,});
        }
        mediaTypePager = findViewById(R.id.camera_mode_pager);
        if (isRecordingVideoAllowed()) {
            mediaTypePager.setAdapter(adapter);
            mediaTypePager.setClipToPadding(false);
            mediaTypePager.setClipChildren(false);
            mediaTypePager.setOffscreenPageLimit(1);
            mediaTypePager.setPageTransformer((page, position) -> {
                float offset = position * -2.2f * pagerMarginOffset;
                if (mediaTypePager.getOrientation() == ViewPager2.ORIENTATION_HORIZONTAL) {
                    if (ViewCompat.getLayoutDirection(mediaTypePager) == ViewCompat.LAYOUT_DIRECTION_RTL) {
                        page.setTranslationX(-offset);
                    } else {
                        page.setTranslationX(offset);
                    }
                } else {
                    page.setTranslationX(offset);
                }
            });
            int initialPosition = adapter.indexOfMediaType(mediaTypeMode);
            if (initialPosition != -1) {
                mediaTypePager.setCurrentItem(initialPosition);
                setMediaTypeMode(mediaTypeMode);
            }
        } else {
            mediaTypePager.setVisibility(View.GONE);
        }
        mediaTypePager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                final @CameraMediaType int mediaType = adapter.getItemMediaType(position);
                if (mediaType != -1) {
                    setMediaTypeMode(mediaType);
                } else if (position == 0) {
                    mediaTypePager.setCurrentItem(adapter.getItemCount() - 2);
                } else {
                    mediaTypePager.setCurrentItem(1);
                }
            }
        });

        if (getIntent().getParcelableExtra(EXTRA_CHAT_ID) != null) {
            maxVideoDurationSeconds = ServerProps.getInstance().getMaxChatVideoDuration();
        } else {
            maxVideoDurationSeconds = ServerProps.getInstance().getMaxFeedVideoDuration();
        }

        if (purpose == PURPOSE_MOMENT || purpose == PURPOSE_MOMENT_PSA) {
            setupViewForMoments();
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) cameraCardView.getLayoutParams();
            params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
            cameraCardView.setLayoutParams(params);
        } else {
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    private void updateGalleryOrFlashButton() {
        if (purpose == PURPOSE_MOMENT || purpose == PURPOSE_MOMENT_PSA || mediaTypeMode == CameraMediaType.MOMENT) {
            galleryButton.setVisibility(View.GONE);
            toggleFlashButtonMoments.setVisibility(camera.isFlashSupported() ? View.VISIBLE : View.INVISIBLE);
            toggleFlashButtonMoments.setImageResource(camera.isFlashOn() ? R.drawable.ic_flash_on : R.drawable.ic_flash_off);
            toggleFlashButton.setVisibility(View.GONE);
        } else {
            toggleFlashButtonMoments.setVisibility(View.GONE);
            toggleFlashButton.setVisibility(camera.isFlashSupported() ? View.VISIBLE : View.INVISIBLE);
            toggleFlashButton.setImageDrawable(camera.isFlashOn() ? flashOnDrawable : flashOffDrawable);
            galleryButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.do_nothing, R.anim.slide_out_bottom);
    }

    @Override
    protected void onStart() {
        super.onStart();
        orientationListener.enable();
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
            cameraCardView.setVisibility(View.VISIBLE);
        }

//        if (purpose == PURPOSE_MOMENT || purpose == PURPOSE_MOMENT_PSA) {
//            if (!isMomentDeadlineTimerCounting()) {
//                startMomentDeadlineTimer();
//            }
//        }

        camera.bindCameraUseCases();
    }

    @Override
    protected void onStop() {
        super.onStop();
        orientationListener.disable();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainHandler.removeCallbacksAndMessages(messageToken);
    }


    private void setupCamera() {
        Integer rotation = orientationListener.getRotationMode().getValue();
        camera = new HalloCamera(this, cameraPreviewView, useSquareAspectRatio(), true, rotation != null ? rotation : Surface.ROTATION_0, new HalloCamera.Listener() {
            @Override
            public void onCaptureSuccess(File file, int type) {
                runOnUiThread(() -> {
                    playCaptureStopAnimation(type);

                    if (type == Media.MEDIA_TYPE_VIDEO) {
                        stopRecordingVideo();
                    }

                    handleMediaUri(Uri.fromFile(file));
                });
            }

            @Override
            public void onCaptureFailure(int type) {
                runOnUiThread(() -> {
                    playCaptureStopAnimation(type);

                    if (type == Media.MEDIA_TYPE_IMAGE) {
                        showErrorMessage(getResources().getString(R.string.camera_error_photo), false);
                    } else if (type == Media.MEDIA_TYPE_VIDEO) {
                        stopRecordingVideo();
                        showErrorMessage(getResources().getString(R.string.camera_error_video), false);
                    }
                });
            }

            @Override
            public void onStateUpdated(HalloCamera camera) {
                runOnUiThread(() -> {
                    flipCameraButton.setVisibility(!camera.hasBackCamera() || !camera.hasFrontCamera() ? View.INVISIBLE : View.VISIBLE);
                    updateGalleryOrFlashButton();
                });
            }

            @Override
            public void onCameraInitFailure() {
                runOnUiThread(() -> showErrorMessage(getResources().getString(R.string.camera_error_generic), true));
            }

            @Override
            public void onCameraNotFound() {
                runOnUiThread(() -> showErrorMessage(getResources().getString(R.string.camera_error_generic), true));
            }

            @Override
            public void onCameraPermissionsMissing() {
                runOnUiThread(() -> requestCameraAndAudioPermission());
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (!camera.isRecordingVideo() && !camera.isCapturingPhoto()) {
                playCaptureStartAnimation(Media.MEDIA_TYPE_IMAGE);
                camera.takePhoto();
            }

            return true;
        }
        return super.onKeyDown(keyCode, event);
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

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Log.d("CameraActivity: onPermissionsGranted");
        if (requestCode == REQUEST_CODE_ASK_CAMERA_AND_AUDIO_PERMISSION) {
            Log.d("CameraActivity: onPermissionsGranted camera and audio");
            cameraCardView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Log.d("CameraActivity: onPermissionDenied");
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            if (requestCode == REQUEST_CODE_ASK_CAMERA_AND_AUDIO_PERMISSION) {
                Log.d("CameraActivity: onPermissionsDenied permanent camera and audio");
                cameraCardView.setVisibility(View.GONE);
                new AppSettingsDialog.Builder(this)
                        .setRationale(getString(R.string.camera_record_audio_permission_rationale_denied))
                        .build().show();
            }
        } else {
            if (requestCode == REQUEST_CODE_ASK_CAMERA_AND_AUDIO_PERMISSION) {
                Log.d("CameraActivity: onPermissionsDenied camera and audio");
                finish();
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private void onRotationChanged(int rotation) {
        updateOrientation(OrientationListener.getRotationAngle(rotation));
        camera.onRotationChanged(rotation);
    }

    private void updateOrientation(int orientationAngle) {
        if (this.orientationAngle != orientationAngle && (orientationAngle % 90 == 0)) {
            int diff = orientationAngle - this.orientationAngle;
            if (diff > 180) {
                diff -= 360;
            } else if (diff < -180) {
                diff += 360;
            }

            toggleFlashButton.animate().setDuration(shortAnimationDuration).rotationBy(-diff).start();
            toggleFlashButtonMoments.animate().setDuration(shortAnimationDuration).rotationBy(-diff).start();
            flipCameraButton.animate().setDuration(shortAnimationDuration).rotationBy(-diff).start();
            galleryButton.animate().setDuration(shortAnimationDuration).rotationBy(-diff).start();

            this.orientationAngle = orientationAngle;
        }
    }

    @MainThread
    private void playCaptureStartAnimation(@Media.MediaType int mediaType) {
        captureInner.setScaleX(1);
        captureInner.setScaleY(1);
        final AnimatedVectorDrawableCompat captureStartDrawable = mediaType == Media.MEDIA_TYPE_VIDEO ? recordVideoStartDrawable : takePhotoStartDrawable;
        captureInner.setImageDrawable(captureStartDrawable);
        recordVideoStartDrawable.start();
        int colorTo = getResources().getColor(R.color.white);
        animateCaptureRingToColor(colorTo);
    }

    @MainThread
    private void playCaptureStopAnimation(@Media.MediaType int mediaType) {
        if (mediaType == Media.MEDIA_TYPE_VIDEO) {
            captureInner.setScaleX(0.5f);
            captureInner.setScaleY(0.5f);
        } else {
            captureInner.setScaleX(1);
            captureInner.setScaleY(1);
        }
        int colorTo = getResources().getColor(R.color.color_primary);
        animateCaptureRingToColor(colorTo);
        final AnimatedVectorDrawableCompat captureStartDrawable = mediaType == Media.MEDIA_TYPE_VIDEO ? recordVideoStartDrawable : takePhotoStartDrawable;
        final AnimatedVectorDrawableCompat captureStopDrawable = mediaType == Media.MEDIA_TYPE_VIDEO ? recordVideoStopDrawable : takePhotoStopDrawable;
        if (captureInner.getDrawable() == captureStartDrawable) {
            captureInner.setImageDrawable(captureStopDrawable);
            captureStopDrawable.registerAnimationCallback(new Animatable2Compat.AnimationCallback() {
                @Override
                public void onAnimationEnd(Drawable drawable) {
                    super.onAnimationEnd(drawable);
                    captureInner.setImageDrawable(captureButtonDrawable);
                    captureStopDrawable.unregisterAnimationCallback(this);
                }
            });
            captureStopDrawable.start();
        }
    }

    private void showErrorMessage(String message, boolean abortOnDismiss) {
        if (abortOnDismiss) {
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage(message)
                    .setNeutralButton(android.R.string.ok, null)
                    .setOnDismissListener(dialogInterface -> finish())
                    .show();
        } else {
            SnackbarHelper.showWarning(this, message);
        }
    }

    private void hideOptionButtons() {
        toggleFlashButton.setVisibility(View.INVISIBLE);
        flipCameraButton.setVisibility(View.INVISIBLE);
        galleryButton.setVisibility(View.INVISIBLE);
    }

    private void restoreOptionButtons() {
        if (camera.isFlashSupported()) {
            toggleFlashButton.setVisibility(View.VISIBLE);
        }
        if (camera.hasFrontCamera() && camera.hasBackCamera()) {
            flipCameraButton.setVisibility(View.VISIBLE);
        }
        galleryButton.setVisibility(View.VISIBLE);
    }

    private boolean isRecordingVideoAllowed() {
        return purpose == PURPOSE_COMPOSE || purpose == PURPOSE_COMPOSE_ANY;
    }

    private boolean useSquareAspectRatio() {
        return purpose == PURPOSE_MOMENT || purpose == PURPOSE_MOMENT_PSA || mediaTypeMode == CameraMediaType.MOMENT;
    }

    private void setupViewForMoments() {
        actionBar.setTitle(R.string.moment_title);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setSubtitle(R.string.share_moment_subtitle);

        flipCameraButton.setBackground(null);

        final Drawable cardBackgroundDrawable = ContextCompat.getDrawable(this, R.drawable.camera_card_background);
        cameraCardView.setBackground(cardBackgroundDrawable);

        ConstraintLayout.LayoutParams cameraPreviewLp = (ConstraintLayout.LayoutParams) cameraPreviewView.getLayoutParams();
        cameraPreviewLp.dimensionRatio = "1:1";
        cameraPreviewView.setLayoutParams(cameraPreviewLp);
    }

    private void setupViewForCamera() {
        actionBar.setDisplayShowTitleEnabled(false);
        cameraCardView.setBackground(null);

        ConstraintLayout.LayoutParams cameraPreviewLp = (ConstraintLayout.LayoutParams) cameraPreviewView.getLayoutParams();
        cameraPreviewLp.dimensionRatio = "3:4";
        cameraPreviewView.setLayoutParams(cameraPreviewLp);
    }

    @SuppressLint("RestrictedApi")
    @MainThread
    void stopRecordingVideo() {
        Log.d("CameraActivity: stopRecordingVideo");
        if (!camera.isRecordingVideo()) {
            return;
        }
        Log.d("CameraActivity: stopRecordingVideo entered");
        mainHandler.removeCallbacksAndMessages(messageToken);
        stopRecordingTimer();
        stopTimeLimitTimer();
        playCaptureStopAnimation(Media.MEDIA_TYPE_VIDEO);
        restoreOptionButtons();

        camera.stopRecordingVideo();
    }

    private void startRecordingTimer() {
        videoTimer.setVisibility(View.VISIBLE);
        videoTimer.setBase(SystemClock.elapsedRealtime());
        videoTimer.start();
        mediaTypePager.setVisibility(View.INVISIBLE);
    }

    private void stopRecordingTimer() {
        if (camera.isRecordingVideo()) {
            videoTimer.setVisibility(View.GONE);
            videoTimer.stop();
        }
        mediaTypePager.setVisibility(View.VISIBLE);
    }

    private void startTimeLimitTimer() {
        String message = getResources().getString(R.string.video_countdown_message);
        videoTimeLimitTimer.setFormat(message);
        videoTimeLimitTimer.setBase(SystemClock.elapsedRealtime() + VIDEO_WARNING_DURATION_SEC * 1000);
        videoTimeLimitTimer.setVisibility(View.VISIBLE);
        videoTimeLimitTimer.start();
    }

    private void stopTimeLimitTimer() {
        videoTimeLimitTimer.setVisibility(View.GONE);
        videoTimeLimitTimer.stop();
    }

    private void startMomentDeadlineTimer() {
        videoTimeLimitTimer.setFormat(null);
        videoTimeLimitTimer.setBase(SystemClock.elapsedRealtime() + MOMENT_DEADLINE_SEC * 1000);
        videoTimeLimitTimer.setVisibility(View.VISIBLE);
        videoTimeLimitTimer.start();

        mainHandler.postAtTime(this::finish, messageToken, SystemClock.uptimeMillis() + MOMENT_DEADLINE_SEC * 1000);
    }

    private void cancelMomentDeadlineTimer() {
        videoTimeLimitTimer.stop();
        videoTimeLimitTimer.setVisibility(View.GONE);
        mainHandler.removeCallbacksAndMessages(messageToken);
    }

    private boolean isMomentDeadlineTimerCounting() {
        return videoTimeLimitTimer.getVisibility() == View.VISIBLE;
    }

    private void handleMediaUri(@NonNull Uri uri) {
        switch (purpose) {
            case PURPOSE_COMPOSE:
            case PURPOSE_COMPOSE_ANY:
                startComposerForUri(uri);
                break;
            case PURPOSE_MOMENT:
            case PURPOSE_MOMENT_PSA:
                startMomentForUri(uri);
                break;
            case PURPOSE_GROUP_AVATAR:
            case PURPOSE_USER_AVATAR:
                startAvatarPreviewForUri(uri);
                break;
        }
    }

    private void startMomentForUri(@NonNull Uri uri) {
        final Intent intent = MomentComposerActivity.unlockMoment(getBaseContext(), getIntent().getParcelableExtra(EXTRA_TARGET_MOMENT_USER_ID));
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        if (purpose == PURPOSE_MOMENT_PSA) {
            intent.putExtra(MomentComposerActivity.EXTRA_SHOW_PSA_TAG, true);
        }

        intent.putExtra(MomentComposerActivity.EXTRA_SELFIE_MEDIA_INDEX, camera.isUsingBackCamera() ? 1 : 0 );

        startActivityForResult(intent, REQUEST_CODE_SEND_MOMENT, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }

    private void startComposerForUri(@NonNull Uri uri) {
        final Intent intent = new Intent(getBaseContext(), ContentComposerActivity.class);
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, new ArrayList<>(Collections.singleton(uri)));
        intent.putExtra(ContentComposerActivity.EXTRA_CALLED_FROM_CAMERA, true);
        ChatId chatId = getIntent().getParcelableExtra(EXTRA_CHAT_ID);
        GroupId groupId = getIntent().getParcelableExtra(EXTRA_GROUP_ID);
        intent.putExtra(ContentComposerActivity.EXTRA_CHAT_ID, chatId);
        intent.putExtra(ContentComposerActivity.EXTRA_GROUP_ID, groupId);
        intent.putExtra(ContentComposerActivity.EXTRA_REPLY_POST_ID, getIntent().getStringExtra(EXTRA_REPLY_POST_ID));
        intent.putExtra(ContentComposerActivity.EXTRA_REPLY_POST_MEDIA_INDEX, getIntent().getIntExtra(EXTRA_REPLY_POST_MEDIA_INDEX, -1));
        startActivityForResult(intent, REQUEST_CODE_OPEN_COMPOSER);
    }

    private void startAvatarPreviewForUri(@NonNull Uri uri) {
        Intent intent = AvatarPreviewActivity.open(this, uri, purpose == PURPOSE_GROUP_AVATAR);
        startActivityForResult(intent, REQUEST_CODE_SET_AVATAR);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SET_AVATAR && resultCode == RESULT_OK) {
            setResult(RESULT_OK, data);
            finish();
        } else if (requestCode == REQUEST_CODE_SEND_MOMENT && resultCode == RESULT_OK) {
            String targetMoment = getIntent().getStringExtra(EXTRA_TARGET_MOMENT);
            if (targetMoment != null) {
                startActivity(MomentViewerActivity.viewMoment(this, targetMoment));
            }
            finish();
        } else if (requestCode == REQUEST_CODE_OPEN_COMPOSER && resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        } else if (requestCode == REQUEST_CODE_CHOOSE_GALLERY && resultCode == RESULT_OK) {
            if (data != null) {
                final ArrayList<Uri> uris = data.getParcelableArrayListExtra(MediaEditActivity.EXTRA_MEDIA);
                if (uris == null) {
                    Log.e("CommentsActivity: Got null media");
                } else if (uris.size() == 1) {
                    handleMediaUri(uris.get(0));
                } else {
                    Log.w("CommentsActivity: Invalid comment media count " + uris.size());
                }
            }
        }
    }

    private void animateCaptureRingToColor(int colorTo) {
        ColorStateList list = captureButton.getBackgroundTintList();
        int colorFrom = list == null ? colorTo : list.getDefaultColor();
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(250);
        colorAnimation.addUpdateListener(animator -> captureButton.setBackgroundTintList(ColorStateList.valueOf((int) animator.getAnimatedValue())));
        colorAnimation.start();
    }

    private void setMediaTypeMode(@CameraMediaType int mediaTypeMode) {
        Log.d("CameraActivity: setMediaTypeMode " + mediaTypeMode);
        final int prevMediaTypeMode = this.mediaTypeMode;
        this.mediaTypeMode = mediaTypeMode;
        final MediaTypeAdapter adapter = (MediaTypeAdapter) mediaTypePager.getAdapter();
        if (adapter != null) {
            for (int i = 0; i < adapter.getItemCount(); ++i) {
                final @CameraMediaType int itemMediaType = adapter.getItemMediaType(i);
                RadioButton radioButton = mediaTypePager.findViewWithTag(itemMediaType);
                if (radioButton != null) {
                    radioButton.setChecked(itemMediaType == mediaTypeMode);
                }
            }
        }
        if (mediaTypeMode == CameraMediaType.VIDEO) {
            captureInner.animate().scaleX(0.5f).scaleY(0.5f).setDuration(200).start();
            int colorTo = getResources().getColor(R.color.color_primary);
            animateCaptureRingToColor(colorTo);
        } else {
            captureInner.animate().scaleX(1).scaleY(1).setDuration(200).start();
            int colorTo = getResources().getColor(R.color.white);
            animateCaptureRingToColor(colorTo);
        }

        if (mediaTypeMode == CameraMediaType.MOMENT) {
            setupViewForMoments();
            flipCameraButton.setBackground(null);
            flipCameraButton.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.camera_icon_tint)));
            if (prevMediaTypeMode != CameraMediaType.MOMENT) {
                camera.setSquareAspectRatio(true);
                camera.bindCameraUseCases();
                purpose = PURPOSE_MOMENT;
            }
        } else {
            setupViewForCamera();
            flipCameraButton.setBackgroundResource(R.drawable.ic_camera_button_outer_ring);
            flipCameraButton.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white)));
            if (prevMediaTypeMode == CameraMediaType.MOMENT) {
                camera.setSquareAspectRatio(false);
                camera.bindCameraUseCases();
                purpose = PURPOSE_COMPOSE;
            }
        }
        updateGalleryOrFlashButton();
    }

    private void updateGalleryButton(@Nullable GalleryItem galleryItem) {
        if (galleryItem == null) {
            galleryThumbnailLoader.cancel(galleryPreview);
            galleryPreview.setScaleType(ImageView.ScaleType.CENTER);
            galleryPreview.setImageResource(R.drawable.ic_media_collection);
            galleryPreview.setImageTintList(ColorStateList.valueOf(Color.WHITE));
        } else {
            galleryPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
            galleryPreview.setImageTintList(null);
            galleryThumbnailLoader.load(galleryPreview, galleryItem);
        }
    }

    static class MediaTypeViewHolder extends RecyclerView.ViewHolder {
        public MediaTypeViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    class MediaTypeAdapter extends RecyclerView.Adapter<MediaTypeViewHolder> {
        private final int[] mediaTypes;

        MediaTypeAdapter(@CameraMediaType int[] mediaTypes) {
            this.mediaTypes = new int[mediaTypes.length + 2];
            for (int i = 0; i < mediaTypes.length; i++) {
                this.mediaTypes[i + 1] = mediaTypes[i];
            }
            this.mediaTypes[0] = CameraMediaType.INVALID;
            this.mediaTypes[this.mediaTypes.length - 1] = CameraMediaType.INVALID;
        }

        @NonNull
        @Override
        public MediaTypeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.camera_mode_item, parent, false);
            final Button button = view.findViewById(R.id.button);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(button.getLayoutParams());
            layoutParams.setMargins(pagerMarginOffset, 0, pagerMarginOffset, 0);
            button.setLayoutParams(layoutParams);
            return new MediaTypeViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MediaTypeViewHolder holder, int position) {
            final CharSequence buttonText;
            switch (mediaTypes[position]) {
                case CameraMediaType.PHOTO:
                    buttonText = getResources().getString(R.string.camera_media_type_option_photo);
                    break;
                case CameraMediaType.VIDEO:
                    buttonText = getResources().getString(R.string.camera_media_type_option_video);
                    break;
                case CameraMediaType.MOMENT:
                    buttonText = getResources().getString(R.string.camera_media_type_option_moment);
                    break;
                default:
                case CameraMediaType.INVALID:
                    buttonText = "";
                    break;
            }
            final RadioButton button = holder.itemView.findViewById(R.id.button);
            button.setText(buttonText);
            button.setTag(mediaTypes[position]);
            if (mediaTypes[position] == mediaTypeMode) {
                button.setChecked(true);
            }
            button.setOnClickListener(view -> {
                if (mediaTypePager.getCurrentItem() != position) {
                    mediaTypePager.setCurrentItem(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mediaTypes.length;
        }

        public int indexOfMediaType(@CameraMediaType int mediaType) {
            for (int i = 0; i < mediaTypes.length; ++i) {
                if (mediaType == mediaTypes[i]) {
                    return i;
                }
            }
            return -1;
        }

        public @CameraMediaType int getItemMediaType(int index) {
            if (0 <= index && index < mediaTypes.length) {
                return mediaTypes[index];
            }
            return CameraMediaType.INVALID;
        }
    }
}

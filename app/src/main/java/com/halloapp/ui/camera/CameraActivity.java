package com.halloapp.ui.camera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.CameraCharacteristics;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Rational;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.camera.camera2.interop.Camera2CameraInfo;
import androidx.camera.camera2.interop.ExperimentalCamera2Interop;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.Preview;
import androidx.camera.core.SurfaceOrientedMeteringPointFactory;
import androidx.camera.core.UseCaseGroup;
import androidx.camera.core.VideoCapture;
import androidx.camera.core.ViewPort;
import androidx.camera.core.ZoomState;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.exifinterface.media.ExifInterface;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.halloapp.Constants;
import com.halloapp.FileStore;
import com.halloapp.Preferences;
import com.halloapp.R;
import com.halloapp.content.Media;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.props.ServerProps;
import com.halloapp.ui.ContentComposerActivity;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.MomentComposerActivity;
import com.halloapp.ui.MomentViewerActivity;
import com.halloapp.ui.MomentsNuxBottomSheetDialogFragment;
import com.halloapp.ui.SystemUiVisibility;
import com.halloapp.ui.avatar.AvatarPreviewActivity;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.DialogFragmentUtils;
import com.halloapp.util.OrientationListener;
import com.halloapp.util.RandomId;
import com.halloapp.util.ThreadUtils;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.SnackbarHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class CameraActivity extends HalloActivity implements EasyPermissions.PermissionCallbacks, MomentsNuxBottomSheetDialogFragment.Parent {
    private enum CameraMediaType {
        PHOTO, VIDEO
    }

    public static final String EXTRA_CHAT_ID = "chat_id";
    public static final String EXTRA_GROUP_ID = "group_id";
    public static final String EXTRA_REPLY_POST_ID = "reply_post_id";
    public static final String EXTRA_REPLY_POST_MEDIA_INDEX = "reply_post_media_index";
    public static final String EXTRA_PURPOSE = "purpose";
    public static final String EXTRA_TARGET_MOMENT = "target_moment";
    public static final String EXTRA_TARGET_MOMENT_SENDER_NAME = "target_moment_sender_name";
    public static final String EXTRA_TARGET_MOMENT_USER_ID = "target_moment_user_id";

    private static final int REQUEST_CODE_ASK_CAMERA_AND_AUDIO_PERMISSION = 1;
    private static final int REQUEST_CODE_SET_AVATAR = 2;
    private static final int REQUEST_CODE_SEND_MOMENT = 3;

    public static final int PURPOSE_COMPOSE = 1;
    public static final int PURPOSE_USER_AVATAR = 2;
    public static final int PURPOSE_GROUP_AVATAR = 3;
    public static final int PURPOSE_MOMENT = 4;
    public static final int PURPOSE_MOMENT_PSA = 5;

    private static final int VIDEO_WARNING_DURATION_SEC = 10;
    private static final int ASPECT_RATIO = AspectRatio.RATIO_4_3;
    private static final int FOCUS_AUTO_CANCEL_DURATION_SEC = 2;

    private final Object messageToken = new Object();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private CameraMediaType mediaTypeMode = CameraMediaType.PHOTO;

    private BgWorkers bgWorkers;
    private ExecutorService cameraExecutor;

    private ProcessCameraProvider cameraProvider;
    private Camera camera;
    private ImageCapture imageCapture;
    private VideoCapture videoCapture;
    private OrientationListener orientationListener;

    private CardView cameraCardView;
    private PreviewView cameraPreviewView;

    private File mediaFile;

    private Chronometer videoTimer;
    private Chronometer videoTimeLimitTimer;

    private View momentOverlay;
    private View momentNuxContentContainer;

    private ImageButton captureButton;
    private ImageButton flipCameraButton;
    private ImageButton toggleFlashButton;

    private Drawable flashOnDrawable;
    private Drawable flashOffDrawable;
    private Drawable captureButtonDrawable;
    private AnimatedVectorDrawableCompat recordVideoStartDrawable;
    private AnimatedVectorDrawableCompat recordVideoStopDrawable;
    private AnimatedVectorDrawableCompat takePhotoStartDrawable;
    private AnimatedVectorDrawableCompat takePhotoStopDrawable;

    private TextView subtitleView;
    private TextView titleView;

    private String mediaTypeOptionPhoto;
    private String mediaTypeOptionVideo;

    private int pagerMarginOffset;
    private ViewPager2 mediaTypePager;

    private boolean isFlashSupported;
    private boolean hasBackCamera;
    private boolean hasFrontCamera;

    private boolean isLimitedLevelSupported = true;
    private boolean isUsingBackCamera = true;
    private boolean isFlashOn = false;
    private boolean isRecordingVideo = false;
    private boolean isTakingPreviewSnapshot = false;
    private boolean isPreviewFlashEnabled = false;
    private boolean isCapturingPhoto = false;

    private int permissionRequestRetries = 0;

    private int purpose = PURPOSE_COMPOSE;
    private int orientationAngle = 0;
    private int maxVideoDurationSeconds;

    private @StringRes int titleRes = R.string.camera_post;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        purpose = getIntent().getIntExtra(EXTRA_PURPOSE, PURPOSE_COMPOSE);

        if (Build.VERSION.SDK_INT >= 28) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        getWindow().getDecorView().setSystemUiVisibility(SystemUiVisibility.getDefaultSystemUiVisibility(this));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        setContentView(R.layout.activity_camera);

        titleView = findViewById(R.id.title);
        subtitleView = findViewById(R.id.subtitle);
        TextView momentNuxText = findViewById(R.id.moment_nux_text);
        if (purpose == PURPOSE_MOMENT || purpose == PURPOSE_MOMENT_PSA) {
            titleRes = R.string.moment_title;
            titleView.setText(R.string.moment_title);
            titleView.setVisibility(View.VISIBLE);
            String momentSenderName = getIntent().getStringExtra(EXTRA_TARGET_MOMENT_SENDER_NAME);
            subtitleView.setVisibility(View.VISIBLE);
            if (TextUtils.isEmpty(momentSenderName)) {
                subtitleView.setText(R.string.share_moment_subtitle);
                momentNuxText.setText(R.string.share_moment_subtitle);
            } else {
                String s = getString(R.string.unlock_moment_subtitle, momentSenderName);
                subtitleView.setText(s);
                momentNuxText.setText(s);
            }
        }
        bgWorkers = BgWorkers.getInstance();
        cameraExecutor = Executors.newSingleThreadExecutor();

        flashOnDrawable = getDrawable(R.drawable.ic_flash_on);
        flashOffDrawable = getDrawable(R.drawable.ic_flash_off);
        captureButtonDrawable = getDrawable(R.drawable.ic_camera_capture_inner);
        recordVideoStartDrawable = AnimatedVectorDrawableCompat.create(this, R.drawable.record_video_start_animation);
        recordVideoStopDrawable = AnimatedVectorDrawableCompat.create(this, R.drawable.record_video_stop_animation);
        takePhotoStartDrawable = AnimatedVectorDrawableCompat.create(this, R.drawable.take_photo_start_animation);
        takePhotoStopDrawable = AnimatedVectorDrawableCompat.create(this, R.drawable.take_photo_stop_animation);

        final Drawable cardBackgroundDrawable = getDrawable(R.drawable.camera_card_background);
        final float cameraCardRadius = getResources().getDimension(R.dimen.camera_card_border_radius);
        cameraCardView = findViewById(R.id.cameraCard);
        cameraCardView.setBackground(cardBackgroundDrawable);
        cameraCardView.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), cameraCardRadius);
            }
        });

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

        captureButton.setOnClickListener(v -> {
            Log.d("CameraActivity: capture button onClick");
            if (isRecordingVideo) {
                stopRecordingVideo();
            } else if (!isCapturingPhoto && !isTakingPreviewSnapshot){
                if (mediaTypeMode == CameraMediaType.PHOTO) {
                    takePhoto();
                } else {
                    startRecordingVideo();
                }
            }
        });
        flipCameraButton = findViewById(R.id.flip_camera);
        flipCameraButton.setOnClickListener(v -> flipCamera());
        toggleFlashButton = findViewById(R.id.toggle_flash);
        toggleFlashButton.setOnClickListener(v -> toggleFlash());
        toggleFlashButton.setImageDrawable(isFlashOn ? flashOnDrawable : flashOffDrawable);

        orientationListener = new OrientationListener(this);
        orientationListener.getRotationMode().observe(this, this::onRotationChanged);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        pagerMarginOffset = displayMetrics.widthPixels / 3;

        mediaTypeOptionPhoto = getResources().getString(R.string.camera_media_type_option_photo);
        mediaTypeOptionVideo = getResources().getString(R.string.camera_media_type_option_video);

        final MediaTypeAdapter adapter = new MediaTypeAdapter();
        mediaTypePager = (ViewPager2) findViewById(R.id.camera_mode_pager);
        if (isRecordingVideoAllowed()) {
            mediaTypePager.setAdapter(adapter);
            mediaTypePager.setClipToPadding(false);
            mediaTypePager.setClipChildren(false);
            mediaTypePager.setOffscreenPageLimit(1);
            mediaTypePager.setPageTransformer((page, position) -> {
                float offset = position * -2 * pagerMarginOffset;
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
            }
        } else {
            mediaTypePager.setVisibility(View.GONE);
        }
        mediaTypePager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                final CameraMediaType mediaType = adapter.getItemMediaType(position);
                if (mediaType != null) {
                    setMediaTypeMode(mediaType);
                }
            }
        });

        if (getIntent().getParcelableExtra(EXTRA_CHAT_ID) != null) {
            maxVideoDurationSeconds = ServerProps.getInstance().getMaxChatVideoDuration();
        } else {
            maxVideoDurationSeconds = ServerProps.getInstance().getMaxFeedVideoDuration();
        }

        if (purpose == PURPOSE_MOMENT || purpose == PURPOSE_MOMENT_PSA) {
            ConstraintLayout.LayoutParams cameraPreviewLp = (ConstraintLayout.LayoutParams) cameraPreviewView.getLayoutParams();
            cameraPreviewLp.dimensionRatio = "1:1";
            cameraPreviewView.setLayoutParams(cameraPreviewLp);
            momentOverlay = findViewById(R.id.moment_nux_overlay);
            momentOverlay.setOnTouchListener((v, e) -> {
                return true;
            });
            momentOverlay.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), cameraViewRadius);
                }
            });
            momentNuxContentContainer = findViewById(R.id.moment_nux_content);
            View momentOk = findViewById(R.id.moment_ok);
            bgWorkers.execute(() -> {
                if (!Preferences.getInstance().getShowedMomentsNux()) {
                    subtitleView.setVisibility(View.GONE);
                    momentOverlay.setVisibility(View.VISIBLE);
                    momentOk.setOnClickListener(v -> {
                        momentOverlay.setVisibility(View.GONE);
                        subtitleView.setVisibility(View.VISIBLE);
                    });
                    DialogFragmentUtils.showDialogFragmentOnce(MomentsNuxBottomSheetDialogFragment.newInstance(), getSupportFragmentManager());
                }
            });
        }

        setupCamera();
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
        cameraExecutor.shutdown();

        clearTempMediaFile();
    }

    private void setupCamera() {
        Log.d("CameraActivity: setupCamera");
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
            } catch (ExecutionException | InterruptedException e) {
                // Should never happen
                Log.e("CameraActivity: cameraProvider init failed", e);
                showErrorMessage(getResources().getString(R.string.camera_error_generic), true);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private ImageCapture setupImageCapture() {
        final Integer rotation = orientationListener.getRotationMode().getValue();
        return new ImageCapture.Builder()
                .setTargetRotation(rotation != null ? rotation : Surface.ROTATION_0)
                .setTargetAspectRatio(ASPECT_RATIO)
                .build();
    }

    @SuppressLint("RestrictedApi")
    private VideoCapture setupVideoCapture() {
        final Integer rotation = orientationListener.getRotationMode().getValue();
        return new VideoCapture.Builder()
                .setTargetRotation(rotation != null ? rotation : Surface.ROTATION_0)
                .setTargetAspectRatio(ASPECT_RATIO)
                .build();
    }

    private void setFocusMode(float x, float y, boolean isAutoFocus) {
        if (camera == null) {
            return;
        }

        final MeteringPointFactory meteringPointFactory =
                new SurfaceOrientedMeteringPointFactory(cameraPreviewView.getWidth(), cameraPreviewView.getHeight());
        final MeteringPoint focusPoint = meteringPointFactory.createPoint(x, y);
        final FocusMeteringAction.Builder focusMeteringActionBuilder = new FocusMeteringAction.Builder(
                focusPoint,
                FocusMeteringAction.FLAG_AF | FocusMeteringAction.FLAG_AE
        );
        if (isAutoFocus) {
            focusMeteringActionBuilder.setAutoCancelDuration(FOCUS_AUTO_CANCEL_DURATION_SEC, TimeUnit.SECONDS);
        } else {
            focusMeteringActionBuilder.disableAutoCancel();
        }
        camera.getCameraControl().startFocusAndMetering(focusMeteringActionBuilder.build());
    }

    private void zoomCamera(float scaleFactor) {
        if (camera == null) {
            return;
        }
        final ZoomState zoomState = camera.getCameraInfo().getZoomState().getValue();
        if (zoomState != null) {
            final float ratio = zoomState.getZoomRatio() * scaleFactor;
            if (zoomState.getMinZoomRatio() <= ratio && ratio <= zoomState.getMaxZoomRatio()) {
                camera.getCameraControl().setZoomRatio(ratio);
            }
        }
    }

    void bindCameraUseCases() {
        Log.d("CameraActivity: bindPreview");
        if (cameraProvider == null) {
            return;
        }

        cameraProvider.unbindAll();

        final CameraSelector backCameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        final CameraSelector frontCameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build();
        hasBackCamera = hasCamera(cameraProvider, backCameraSelector);
        hasFrontCamera = hasCamera(cameraProvider, frontCameraSelector);

        if (!hasBackCamera && !hasFrontCamera) {
            Log.e("CameraActivity: bindPreview detected no front or back camera");
            showErrorMessage(getResources().getString(R.string.camera_error_generic), true);
            return;
        } else if (!hasBackCamera) {
            isUsingBackCamera = false;
            flipCameraButton.setVisibility(View.INVISIBLE);
        } else if (!hasFrontCamera) {
            isUsingBackCamera = true;
            flipCameraButton.setVisibility(View.INVISIBLE);
        }

        isLimitedLevelSupported = true;
        if (hasFrontCamera) {
            isLimitedLevelSupported = isLimitedLevelSupported && isCameraHardwareLevelLimitedOrBetter(cameraProvider, frontCameraSelector);
        }
        if (hasBackCamera) {
            isLimitedLevelSupported = isLimitedLevelSupported && isCameraHardwareLevelLimitedOrBetter(cameraProvider, backCameraSelector);
        }
        Log.d("CameraActivity: bindPreview isLegacyLevelSupported = " + isLimitedLevelSupported);

        UseCaseGroup.Builder useCaseGroupBuilder = new UseCaseGroup.Builder();
        if (isLimitedLevelSupported) {
            imageCapture = setupImageCapture();
            useCaseGroupBuilder.addUseCase(imageCapture);
        }
        videoCapture = setupVideoCapture();
        useCaseGroupBuilder.addUseCase(videoCapture);

        if (useSquareAspectRatio()) {
            ViewPort viewPort = new ViewPort.Builder(
                    new Rational(1, 1),
                    Surface.ROTATION_0).build();

            useCaseGroupBuilder.setViewPort(viewPort);
        }
        final Preview preview = new Preview.Builder().setTargetAspectRatio(ASPECT_RATIO).build();
        preview.setSurfaceProvider(cameraPreviewView.getSurfaceProvider());
        useCaseGroupBuilder.addUseCase(preview);

        final CameraSelector cameraSelector = isUsingBackCamera ? backCameraSelector : frontCameraSelector;
        camera = cameraProvider.bindToLifecycle(this, cameraSelector, useCaseGroupBuilder.build());

        isFlashSupported = camera.getCameraInfo().hasFlashUnit();
        final boolean isFlashEnabled = isFlashSupported && isFlashOn;
        toggleFlashButton.setVisibility(isFlashSupported ? View.VISIBLE : View.INVISIBLE);
        if (imageCapture != null) {
            imageCapture.setFlashMode(isFlashEnabled ? ImageCapture.FLASH_MODE_ON : ImageCapture.FLASH_MODE_OFF);
        }

        cameraPreviewView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void onGlobalLayout() {
                if (cameraPreviewView.getMeasuredWidth() > 0 && cameraPreviewView.getMeasuredHeight() > 0) {
                    cameraPreviewView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    setFocusMode((float) cameraPreviewView.getWidth() / 2,(float) cameraPreviewView.getHeight() / 2,true);

                    final ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(getBaseContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                        @Override
                        public boolean onScale(ScaleGestureDetector detector) {
                            zoomCamera(detector.getScaleFactor());
                            return true;
                        }
                    });
                    scaleGestureDetector.setQuickScaleEnabled(false);
                    cameraPreviewView.setOnTouchListener((view, motionEvent) -> {
                        scaleGestureDetector.onTouchEvent(motionEvent);
                        if (!scaleGestureDetector.isInProgress() && motionEvent.getAction() == MotionEvent.ACTION_UP) {
                            setFocusMode(motionEvent.getX(), motionEvent.getY(), false);
                        }
                        return true;
                    });
                }
            }
        });
    }

    boolean hasCamera(@NonNull ProcessCameraProvider cameraProvider, @NonNull CameraSelector cameraSelector) {
        try {
            return cameraProvider.hasCamera(cameraSelector);
        } catch (CameraInfoUnavailableException e) {
            return false;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            takePhoto();
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
        if (imageCapture != null) {
            imageCapture.setTargetRotation(rotation);
        }
        if (videoCapture != null) {
            videoCapture.setTargetRotation(rotation);
        }
    }

    private void updateOrientation(int orientationAngle) {
        if (this.orientationAngle != orientationAngle) {
            this.orientationAngle = orientationAngle;
            toggleFlashButton.setRotation(360 - orientationAngle);
            flipCameraButton.setRotation(360 - orientationAngle);
        }
    }

    private void playCaptureStartAnimation(@NonNull CameraMediaType mediaType) {
        final AnimatedVectorDrawableCompat captureStartDrawable = mediaType == CameraMediaType.VIDEO ? recordVideoStartDrawable : takePhotoStartDrawable;
        captureButton.setImageDrawable(captureStartDrawable);
        recordVideoStartDrawable.start();
    }

    private void playCaptureStopAnimation(@NonNull CameraMediaType mediaType) {
        final AnimatedVectorDrawableCompat captureStartDrawable = mediaType == CameraMediaType.VIDEO ? recordVideoStartDrawable : takePhotoStartDrawable;
        final AnimatedVectorDrawableCompat captureStopDrawable = mediaType == CameraMediaType.VIDEO ? recordVideoStopDrawable : takePhotoStopDrawable;
        if (captureButton.getDrawable() == captureStartDrawable) {
            captureButton.setImageDrawable(captureStopDrawable);
            captureStopDrawable.registerAnimationCallback(new Animatable2Compat.AnimationCallback() {
                @Override
                public void onAnimationEnd(Drawable drawable) {
                    super.onAnimationEnd(drawable);
                    captureButton.setImageDrawable(captureButtonDrawable);
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

    void flipCamera() {
        if (hasBackCamera && hasFrontCamera && !isRecordingVideo) {
            Log.d("CameraActivity: flipCamera");
            isUsingBackCamera = !isUsingBackCamera;
            bindCameraUseCases();
        }
    }

    void toggleFlash() {
        Log.d("CameraActivity: toggleFlash");
        if (isFlashSupported && !isRecordingVideo) {
            isFlashOn = !isFlashOn;
            toggleFlashButton.setImageDrawable(isFlashOn ? flashOnDrawable : flashOffDrawable);
            if (imageCapture != null) {
                final boolean isFlashEnabled = camera.getCameraInfo().hasFlashUnit() && isFlashOn;
                imageCapture.setFlashMode(isFlashEnabled ? ImageCapture.FLASH_MODE_ON : ImageCapture.FLASH_MODE_OFF);
            }
        }
    }

    private void hideOptionButtons() {
        toggleFlashButton.setVisibility(View.INVISIBLE);
        flipCameraButton.setVisibility(View.INVISIBLE);
    }

    private void restoreOptionButtons() {
        if (isFlashSupported) {
            toggleFlashButton.setVisibility(View.VISIBLE);
        }
        if (hasFrontCamera && hasBackCamera) {
            flipCameraButton.setVisibility(View.VISIBLE);
        }
    }

    private boolean isRecordingVideoAllowed() {
        return purpose == PURPOSE_COMPOSE;
    }

    private boolean useSquareAspectRatio() {
        return purpose == PURPOSE_MOMENT || purpose == PURPOSE_MOMENT_PSA;
    }

    @OptIn(markerClass = ExperimentalCamera2Interop.class)
    private Integer getCameraHardwareLevel(@NonNull CameraInfo cameraInfo) {
        return Camera2CameraInfo.from(cameraInfo).getCameraCharacteristic(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
    }

    boolean isCameraHardwareLevelLimitedOrBetter(@NonNull ProcessCameraProvider cameraProvider, @NonNull CameraSelector cameraSelector) {
        List<CameraInfo> filteredCameraInfos = cameraSelector.filter(cameraProvider.getAvailableCameraInfos());
        if (!filteredCameraInfos.isEmpty()) {
            final Integer deviceLevel = getCameraHardwareLevel(filteredCameraInfos.get(0));
            if (deviceLevel != null) {
                if (deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED||
                        deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL) {
                    return true;
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    if (deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL) {
                        return false;
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    return deviceLevel >= CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3;
                }
            }
        }
        return false;
    }

    private void applyExifOrientation(@NonNull File imageFile, int angle) {
        if (angle == 0 || angle % 90 != 0) {
            return;
        }
        int sector = (angle % 360) / 90;
        if (sector < 0) {
            sector += 4;
        }
        final int[] exif_sector_table = new int[] {
                ExifInterface.ORIENTATION_NORMAL,
                ExifInterface.ORIENTATION_ROTATE_90,
                ExifInterface.ORIENTATION_ROTATE_180,
                ExifInterface.ORIENTATION_ROTATE_270,
        };
        try {
            ExifInterface exifInterface = new ExifInterface(imageFile);
            exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, Integer.toString(exif_sector_table[sector]));
            exifInterface.saveAttributes();
        } catch (IOException e) {
            Log.e("CameraActivity: applyExifOrientation", e);
        }
    }

    private void takePreviewSnapshot() {
        if (cameraPreviewView == null || isRecordingVideo || isTakingPreviewSnapshot) {
            return;
        }
        isTakingPreviewSnapshot = true;
        playCaptureStartAnimation(CameraMediaType.PHOTO);
        Log.d("CameraActivity: takePreviewSnapshot");
        clearTempMediaFile();
        mediaFile = generateTempMediaFile(Media.MEDIA_TYPE_IMAGE);

        isPreviewFlashEnabled = camera.getCameraInfo().hasFlashUnit() && isFlashOn;
        if (isPreviewFlashEnabled) {
            final ListenableFuture<Void> torchEnabledFuture = camera.getCameraControl().enableTorch(true);
            Futures.addCallback(
                    torchEnabledFuture,
                    new FutureCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            saveAndHandlePreviewSnapshot();
                        }

                        @Override
                        public void onFailure(@NonNull Throwable throwable) {
                            Log.e("CameraActivity: takePreviewSnapshot error " + throwable);
                            showErrorMessage(getResources().getString(R.string.camera_error_photo), false);
                            cleanAfterPreviewSnapshot();
                        }
                    },
                    ContextCompat.getMainExecutor(this)
            );
        } else {
            saveAndHandlePreviewSnapshot();
        }
    }

    private void saveAndHandlePreviewSnapshot() {
        final Bitmap bitmap = cameraPreviewView.getBitmap();
        if (bitmap == null) {
            Log.e("CameraActivity: saveAndHandlePreviewSnapshot could not create bitmap from preview");
            showErrorMessage(getResources().getString(R.string.camera_error_photo), false);
            cleanAfterPreviewSnapshot();
        } else {
            bgWorkers.execute(() -> {
                try (final FileOutputStream fileOutputStream = new FileOutputStream(mediaFile)) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, Constants.JPEG_QUALITY, fileOutputStream);
                    fileOutputStream.close();
                    applyExifOrientation(mediaFile, orientationAngle);
                    final Uri uri = Uri.fromFile(mediaFile);
                    Log.d("CameraActivity: saveAndHandlePreviewSnapshot image created " + uri);
                    handleMediaUri(uri);
                } catch (IOException exception) {
                    Log.e("CameraActivity: saveAndHandlePreviewSnapshot error " + exception);
                    showErrorMessage(getResources().getString(R.string.camera_error_photo), false);
                } finally {
                    cleanAfterPreviewSnapshot();
                }
            });
        }
    }

    private void cleanAfterPreviewSnapshot() {
        if (isPreviewFlashEnabled) {
            final ListenableFuture<Void> torchDisabledFuture = camera.getCameraControl().enableTorch(false);
            Futures.addCallback(
                    torchDisabledFuture,
                    new FutureCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            isTakingPreviewSnapshot = false;
                            playCaptureStopAnimation(CameraMediaType.PHOTO);
                        }

                        @Override
                        public void onFailure(@NonNull Throwable throwable) {
                            Log.e("CameraActivity: cleanAfterPreviewSnapshot error " + throwable);
                            isTakingPreviewSnapshot = false;
                            playCaptureStopAnimation(CameraMediaType.PHOTO);
                        }
                    },
                    ContextCompat.getMainExecutor(this)
            );
        } else {
            isTakingPreviewSnapshot = false;
            playCaptureStopAnimation(CameraMediaType.PHOTO);
        }
    }

    private void takeImageCapturePhoto() {
        if (imageCapture == null || isRecordingVideo || isCapturingPhoto) {
            return;
        }
        isCapturingPhoto = true;
        playCaptureStartAnimation(CameraMediaType.PHOTO);
        Log.d("CameraActivity: takeImageCapturePhoto");
        clearTempMediaFile();
        mediaFile = generateTempMediaFile(Media.MEDIA_TYPE_IMAGE);

        final ImageCapture.Metadata metadata = new ImageCapture.Metadata();
        metadata.setReversedHorizontal(!isUsingBackCamera);
        final ImageCapture.OutputFileOptions outputFileOptions =
                new ImageCapture.OutputFileOptions.Builder(mediaFile).setMetadata(metadata).build();
        imageCapture.takePicture(outputFileOptions, cameraExecutor, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                final Uri uri = Objects.requireNonNull(outputFileResults.getSavedUri());
                Log.d("CameraActivity: takePhoto onImageSaved " + uri);
                handleMediaUri(uri);
                isCapturingPhoto = false;
                playCaptureStopAnimation(CameraMediaType.PHOTO);
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e("CameraActivity: takePhoto error " + exception);
                showErrorMessage(getResources().getString(R.string.camera_error_photo), false);
                isCapturingPhoto = false;
                playCaptureStopAnimation(CameraMediaType.PHOTO);
            }
        });
    }

    void takePhoto() {
        if (isLimitedLevelSupported) {
            takeImageCapturePhoto();
        } else {
            takePreviewSnapshot();
        }
    }

    @SuppressLint("RestrictedApi")
    void startRecordingVideo() {
        Log.d("CameraActivity: startRecordingVideo");
        if (videoCapture == null || isRecordingVideo || !isRecordingVideoAllowed()) {
            return;
        }
        Log.d("CameraActivity: startRecordingVideo entered");
        isRecordingVideo = true;
        playCaptureStartAnimation(CameraMediaType.VIDEO);
        hideOptionButtons();
        clearTempMediaFile();
        mediaFile = generateTempMediaFile(Media.MEDIA_TYPE_VIDEO);
        startRecordingTimer();
        mainHandler.postAtTime(this::stopRecordingVideo, messageToken, SystemClock.uptimeMillis() + maxVideoDurationSeconds * 1000);
        mainHandler.postAtTime(this::startTimeLimitTimer, messageToken, SystemClock.uptimeMillis() + (maxVideoDurationSeconds - VIDEO_WARNING_DURATION_SEC) * 1000);

        final VideoCapture.OutputFileOptions outputFileOptions = new VideoCapture.OutputFileOptions.Builder(mediaFile).build();
        ThreadUtils.runWithoutStrictModeRestrictions(() -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                videoCapture.startRecording(outputFileOptions, cameraExecutor, new VideoCapture.OnVideoSavedCallback() {
                    @Override
                    public void onVideoSaved(@NonNull VideoCapture.OutputFileResults outputFileResults) {
                        final Uri uri = Objects.requireNonNull(outputFileResults.getSavedUri());
                        Log.d("CameraActivity: startRecordingVideo onVideoSaved " + uri);
                        mainHandler.post(() -> stopRecordingVideo());
                        startComposerForUri(uri);
                    }

                    @Override
                    public void onError(int videoCaptureError, @NonNull String message, @Nullable @org.jetbrains.annotations.Nullable Throwable cause) {
                        Log.e("CameraActivity: startRecordingVideo error " + cause);
                        mainHandler.post(() -> stopRecordingVideo());
                        showErrorMessage(getResources().getString(R.string.camera_error_video), false);
                    }
                });
            } else {
                Log.d("CameraActivity: startRecordingVideo RECORD_AUDIO permission lost");
                stopRecordingVideo();
                requestCameraAndAudioPermission();
            }
        });
    }

    @SuppressLint("RestrictedApi")
    @MainThread
    void stopRecordingVideo() {
        Log.d("CameraActivity: stopRecordingVideo");
        if (videoCapture == null || !isRecordingVideo) {
            return;
        }
        Log.d("CameraActivity: stopRecordingVideo entered");
        mainHandler.removeCallbacksAndMessages(messageToken);
        stopRecordingTimer();
        stopTimeLimitTimer();
        playCaptureStopAnimation(CameraMediaType.VIDEO);
        restoreOptionButtons();
        videoCapture.stopRecording();
        isRecordingVideo = false;
    }

    private void startRecordingTimer() {
            videoTimer.setVisibility(View.VISIBLE);
            videoTimer.setBase(SystemClock.elapsedRealtime());
            videoTimer.start();
    }

    private void stopRecordingTimer() {
        if (isRecordingVideo) {
            videoTimer.setVisibility(View.GONE);
            videoTimer.stop();
        }
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

    private File generateTempMediaFile(@Media.MediaType int mediaType) {
        File tempFile = FileStore.getInstance().getCameraFile(RandomId.create() + "." + Media.getFileExt(mediaType));
        Log.d("CameraActivity: generateTempFile " + Uri.fromFile(tempFile));
        return  tempFile;
    }

    private void clearTempMediaFile() {
        if (mediaFile != null) {
            Log.d("CameraActivity: clearMediaFile " + Uri.fromFile(mediaFile));
            bgWorkers.execute(mediaFile::delete);
            mediaFile = null;
        }
    }

    private void handleMediaUri(@NonNull Uri uri) {
        switch (purpose) {
            case PURPOSE_COMPOSE:
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
        startActivityForResult(intent, REQUEST_CODE_SEND_MOMENT);
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
        startActivity(intent);
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
        }
    }

    private void setMediaTypeMode(@NonNull CameraMediaType mediaTypeMode) {
        Log.d("CameraActivity: setMediaTypeMode " + mediaTypeMode);
        this.mediaTypeMode = mediaTypeMode;
        final MediaTypeAdapter adapter = (MediaTypeAdapter) mediaTypePager.getAdapter();
        if (adapter != null) {
            for (int i = 0; i < adapter.getItemCount(); ++i) {
                final CameraMediaType itemMediaType = adapter.getItemMediaType(i);
                RadioButton radioButton = (RadioButton) mediaTypePager.findViewWithTag(itemMediaType);
                if (radioButton != null) {
                    radioButton.setChecked(itemMediaType == mediaTypeMode);
                }
            }
        }
    }

    @Override
    public void onMomentNuxDismissed() {
        momentNuxContentContainer.setVisibility(View.VISIBLE);
    }

    class MediaTypeViewHolder extends RecyclerView.ViewHolder {
        public MediaTypeViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    class MediaTypeAdapter extends RecyclerView.Adapter<MediaTypeViewHolder> {
        final private CameraMediaType[] mediaTypes = { CameraMediaType.PHOTO, CameraMediaType.VIDEO };

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
            final String buttonText;
            if (mediaTypes[position] == CameraMediaType.PHOTO) {
                buttonText = mediaTypeOptionPhoto;
            } else {
                buttonText = mediaTypeOptionVideo;
            }
            final RadioButton button = (RadioButton) holder.itemView.findViewById(R.id.button);
            button.setText(buttonText.toUpperCase(getResources().getConfiguration().locale));
            button.setTag(mediaTypes[position]);
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

        public int indexOfMediaType(@NonNull CameraMediaType mediaType) {
            for (int i = 0; i < mediaTypes.length; ++i) {
                if (mediaType == mediaTypes[i]) {
                    return i;
                }
            }
            return -1;
        }

        @Nullable
        public CameraMediaType getItemMediaType(int index) {
            if (0 <= index && index < mediaTypes.length) {
                return mediaTypes[index];
            }
            return null;
        }
    }
}

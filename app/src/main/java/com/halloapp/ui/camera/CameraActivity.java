package com.halloapp.ui.camera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import androidx.cardview.widget.CardView;
import androidx.exifinterface.media.ExifInterface;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import com.halloapp.Constants;
import com.halloapp.FileStore;
import com.halloapp.R;
import com.halloapp.content.Media;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.media.MediaUtils;
import com.halloapp.props.ServerProps;
import com.halloapp.ui.ContentComposerActivity;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.SystemUiVisibility;
import com.halloapp.ui.avatar.AvatarPreviewActivity;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.GlobalUI;
import com.halloapp.util.logs.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.util.RandomId;
import com.halloapp.util.ThreadUtils;
import com.halloapp.widget.SnackbarHelper;
import com.otaliastudios.cameraview.CameraException;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraOptions;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.VideoResult;
import com.otaliastudios.cameraview.controls.Facing;
import com.otaliastudios.cameraview.controls.Flash;
import com.otaliastudios.cameraview.controls.Mode;
import com.otaliastudios.cameraview.gesture.Gesture;
import com.otaliastudios.cameraview.gesture.GestureAction;
import com.otaliastudios.cameraview.size.AspectRatio;
import com.otaliastudios.cameraview.size.Size;
import com.otaliastudios.cameraview.size.SizeSelector;
import com.otaliastudios.cameraview.size.SizeSelectors;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class CameraActivity extends HalloActivity implements EasyPermissions.PermissionCallbacks {
    public static final String EXTRA_CHAT_ID = "chat_id";
    public static final String EXTRA_GROUP_ID = "group_id";
    public static final String EXTRA_REPLY_POST_ID = "reply_post_id";
    public static final String EXTRA_REPLY_POST_MEDIA_INDEX = "reply_post_media_index";
    public static final String EXTRA_PURPOSE = "purpose";

    private static final int REQUEST_CODE_ASK_CAMERA_AND_AUDIO_PERMISSION = 1;
    private static final int REQUEST_CODE_SET_AVATAR = 2;

    public static final int PURPOSE_COMPOSE = 1;
    public static final int PURPOSE_AVATAR = 2;

    private static final int PENDING_OPERATION_NONE = 0;
    private static final int PENDING_OPERATION_PHOTO = 1;
    private static final int PENDING_OPERATION_VIDEO = 2;

    private static final int PREFERRED_RATIO_X = 3;
    private static final int PREFERRED_RATIO_Y = 4;

    private static final int[] EXIF_ORIENTATION_FLIP_MAP = new int[] {
            ExifInterface.ORIENTATION_UNDEFINED,
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL,
            ExifInterface.ORIENTATION_NORMAL,
            ExifInterface.ORIENTATION_FLIP_VERTICAL,
            ExifInterface.ORIENTATION_ROTATE_180,
            ExifInterface.ORIENTATION_ROTATE_90,
            ExifInterface.ORIENTATION_TRANSPOSE,
            ExifInterface.ORIENTATION_ROTATE_270,
            ExifInterface.ORIENTATION_TRANSVERSE,
    };

    private BgWorkers bgWorkers;

    private CardView cameraCardView;
    private CameraView cameraView;

    private File mediaFile;

    private Chronometer videoTimer;
    private ImageButton captureButton;
    private ImageButton flipCameraButton;
    private ImageButton toggleFlashButton;

    private Drawable flashOnDrawable;
    private Drawable flashOffDrawable;
    private Drawable captureButtonDrawable;
    private AnimatedVectorDrawableCompat recordStartDrawable;
    private AnimatedVectorDrawableCompat recordStopDrawable;

    private int purpose = PURPOSE_COMPOSE;
    private int orientation = 0;
    private boolean isFlashOn = false;
    private int pendingOperation = 0;
    private boolean isTakingPhoto = false;
    private boolean isRecordingVideo = false;

    private int maxVideoDurationSeconds;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        purpose = getIntent().getIntExtra(EXTRA_PURPOSE, PURPOSE_COMPOSE);

        if (Build.VERSION.SDK_INT >= 28) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        getWindow().getDecorView().setSystemUiVisibility(SystemUiVisibility.getDefaultSystemUiVisibility(this));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        ThreadUtils.runWithoutStrictModeRestrictions(() -> setContentView(R.layout.activity_camera));

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        bgWorkers = BgWorkers.getInstance();

        flashOnDrawable = getDrawable(R.drawable.ic_flash_on);
        flashOffDrawable = getDrawable(R.drawable.ic_flash_off);
        captureButtonDrawable = getDrawable(R.drawable.ic_camera_capture);
        recordStartDrawable = AnimatedVectorDrawableCompat.create(this, R.drawable.record_video_start_animation);
        recordStopDrawable = AnimatedVectorDrawableCompat.create(this, R.drawable.record_video_stop_animation);

        cameraCardView = findViewById(R.id.cameraCard);
        videoTimer = findViewById(R.id.video_timer);
        captureButton = findViewById(R.id.capture);
        captureButton.setOnLongClickListener(v -> {
            Log.d("CameraActivity: capture button onLongClick");
            if (cameraIsAvailable() && isRecordingVideoAllowed() && pendingOperation == PENDING_OPERATION_NONE) {
                if (cameraView.getMode() != Mode.VIDEO) {
                    Log.d("CameraActivity: PENDING_OPERATION_VIDEO");
                    pendingOperation = PENDING_OPERATION_VIDEO;
                    cameraView.setMode(Mode.VIDEO);
                } else {
                    takeVideo();
                }
            }
            return false;
        });
        captureButton.setOnClickListener(v -> {
            Log.d("CameraActivity: capture button onClick");
            if (cameraView != null) {
                if (isRecordingVideo) {
                    cameraView.stopVideo();
                } else if (cameraIsAvailable() && pendingOperation == PENDING_OPERATION_NONE) {
                    if (cameraView.getMode() != Mode.PICTURE) {
                        Log.d("CameraActivity: PENDING_OPERATION_PHOTO");
                        pendingOperation = PENDING_OPERATION_PHOTO;
                        cameraView.setMode(Mode.PICTURE);
                    } else {
                        takePicture();
                    }
                }
            }
        });
        flipCameraButton = findViewById(R.id.flip_camera);
        flipCameraButton.setOnClickListener(v -> flipCamera());
        toggleFlashButton = findViewById(R.id.toggle_flash);
        toggleFlashButton.setOnClickListener(v -> toggleFlash());
        toggleFlashButton.setImageDrawable(isFlashOn ? flashOnDrawable : flashOffDrawable);

        if (getIntent().getParcelableExtra(EXTRA_CHAT_ID) != null) {
            maxVideoDurationSeconds = ServerProps.getInstance().getMaxChatVideoDuration();
        } else {
            maxVideoDurationSeconds = ServerProps.getInstance().getMaxFeedVideoDuration();
        }

        if (!hasCameraAndAudioPermission()) {
            requestCameraAndAudioPermission();
        } else {
            setupCamera();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupCamera() {
        if (cameraView != null) {
            return;
        }

        final float cameraViewRadius = getResources().getDimension(R.dimen.content_composer_media_border);
        cameraCardView.setVisibility(View.VISIBLE);

        cameraView = findViewById(R.id.cameraView);
        cameraView.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), cameraViewRadius);
            }
        });
        cameraView.setClipToOutline(true);
        cameraView.setFlash(isFlashOn ? Flash.ON : Flash.OFF);

        final float eps = 1e-8f;
        final float preferredRatio = ((float) PREFERRED_RATIO_X) / PREFERRED_RATIO_Y;
        cameraView.setPreviewStreamSize(availableSizes -> {
            float diff = Float.MAX_VALUE / 2;
            for (Size size : availableSizes) {
                float ratio = (float) size.getWidth() / size.getHeight();
                diff = Math.min(diff, Math.abs(ratio - preferredRatio));
            }
            List<Size> selectedSizes = new ArrayList<>();
            for (Size size : availableSizes) {
                float ratio = (float) size.getWidth() / size.getHeight();
                if (Math.abs(ratio - preferredRatio) < diff + eps) {
                    selectedSizes.add(size);
                }
            }
            return selectedSizes;
        });

        final SizeSelector preferredPictureSize = SizeSelectors.and(
                SizeSelectors.maxWidth(Constants.MAX_IMAGE_DIMENSION),
                SizeSelectors.maxHeight(Constants.MAX_IMAGE_DIMENSION),
                SizeSelectors.aspectRatio(AspectRatio.of(PREFERRED_RATIO_X, PREFERRED_RATIO_Y), eps),
                SizeSelectors.biggest());
        final SizeSelector fallbackPictureSize = SizeSelectors.and(
                SizeSelectors.maxWidth(Constants.MAX_IMAGE_DIMENSION),
                SizeSelectors.maxHeight(Constants.MAX_IMAGE_DIMENSION),
                SizeSelectors.biggest());
        cameraView.setPictureSize(SizeSelectors.or(preferredPictureSize, fallbackPictureSize));

        final SizeSelector preferredVideoSize = SizeSelectors.and(
                SizeSelectors.minWidth(Constants.VIDEO_RESOLUTION_H264),
                SizeSelectors.minHeight(Constants.VIDEO_RESOLUTION_H264),
                SizeSelectors.aspectRatio(AspectRatio.of(PREFERRED_RATIO_X, PREFERRED_RATIO_Y), eps),
                SizeSelectors.smallest());
        final SizeSelector fallbackVideoSize = SizeSelectors.and(
                SizeSelectors.minWidth(Constants.VIDEO_RESOLUTION_H264),
                SizeSelectors.minHeight(Constants.VIDEO_RESOLUTION_H264),
                SizeSelectors.smallest());
        cameraView.setVideoSize(SizeSelectors.or(preferredVideoSize, fallbackVideoSize));

        cameraView.addCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(@NonNull PictureResult result) {
                super.onPictureTaken(result);
                clearMediaFile();
                mediaFile = generateTempCameraFile(Media.MEDIA_TYPE_IMAGE);
                result.toFile(mediaFile, file -> {
                    if (result.getFacing() == Facing.FRONT) {
                        flipImageHorizontally(file, () -> {
                            handleMediaUri(Uri.fromFile(file));
                            isTakingPhoto = false;
                        });
                    } else {
                        handleMediaUri(Uri.fromFile(file));
                        isTakingPhoto = false;
                    }
                });
            }

            @Override
            public void onVideoTaken(@NonNull VideoResult result) {
                super.onVideoTaken(result);
                handleMediaUri(Uri.fromFile(result.getFile()));
                if (cameraView.getMode() == Mode.VIDEO) {
                    cameraView.setMode(Mode.PICTURE);
                }
                isRecordingVideo = false;
            }

            @Override
            public void onVideoRecordingStart() {
                super.onVideoRecordingStart();
                startRecordingTimer();
            }

            @Override
            public void onVideoRecordingEnd() {
                super.onVideoRecordingEnd();
                stopRecordingTimer();
                playRecordStopAnimation();
            }

            @Override
            public void onCameraError(@NonNull CameraException exception) {
                super.onCameraError(exception);
                Log.e("CameraActivity: onCameraError", exception);

                int messageId = R.string.camera_error_generic;
                switch (exception.getReason()) {
                    case CameraException.REASON_PICTURE_FAILED:
                        messageId = R.string.camera_error_photo;
                        isTakingPhoto = false;
                        break;
                    case CameraException.REASON_VIDEO_FAILED:
                        messageId = R.string.camera_error_video;
                        stopRecordingTimer();
                        playRecordStopAnimation();
                        if (cameraView.getMode() == Mode.VIDEO) {
                            cameraView.setMode(Mode.PICTURE);
                        }
                        isRecordingVideo = false;
                        break;
                }
                showErrorMessage(getResources().getString(messageId), exception.isUnrecoverable());
            }

            @Override
            public void onCameraOpened(@NonNull CameraOptions options) {
                super.onCameraOpened(options);
                Log.d("CameraActivity: onCameraOpened");
                Flash flashMode = isFlashOn ? Flash.ON : Flash.OFF;
                if (cameraView.getFlash() != flashMode) {
                    cameraView.setFlash(flashMode);
                }
                if (pendingOperation != PENDING_OPERATION_NONE) {
                    if (cameraIsAvailable()) {
                        if (pendingOperation == PENDING_OPERATION_PHOTO && cameraView.getMode() == Mode.PICTURE) {
                            takePicture();
                        } else if (pendingOperation == PENDING_OPERATION_VIDEO && cameraView.getMode() == Mode.VIDEO) {
                            takeVideo();
                        }
                    }
                    pendingOperation = PENDING_OPERATION_NONE;
                }
            }

            @Override
            public void onOrientationChanged(int orientation) {
                super.onOrientationChanged(orientation);
                updateOrientation(orientation);
            }
        });

        cameraView.mapGesture(Gesture.PINCH, GestureAction.ZOOM);
        cameraView.mapGesture(Gesture.TAP, GestureAction.AUTO_FOCUS);

        cameraView.setLifecycleOwner(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearMediaFile();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.camera_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.back) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        final MenuItem backMenuItem = menu.findItem(R.id.back);
        backMenuItem.setVisible(orientation % 180 == 90);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
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
            if (hasCameraAndAudioPermission()) {
                setupCamera();
            }
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Log.d("CameraActivity: onPermissionDenied");
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            if (requestCode == REQUEST_CODE_ASK_CAMERA_AND_AUDIO_PERMISSION) {
                Log.d("CameraActivity: onPermissionsDenied permanent camera and audio");
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

    private void flipImageHorizontally(@NonNull File file, @NonNull Runnable callback) {
        bgWorkers.execute(() -> {
            GlobalUI globalUI = GlobalUI.getInstance();
            try {
                ExifInterface exifInterface = new ExifInterface(file);
                int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                if (orientation != ExifInterface.ORIENTATION_UNDEFINED && orientation < EXIF_ORIENTATION_FLIP_MAP.length) {
                    exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, Integer.toString(EXIF_ORIENTATION_FLIP_MAP[orientation]));
                    exifInterface.saveAttributes();
                }
            } catch (IOException e) {
                Log.e("CameraActivity: flipImageHorizontally", e);
            } finally {
                globalUI.postDelayed(callback, 0);
            }
        });
    }

    private void updateOrientation(int orientation) {
        this.orientation = orientation;
        toggleFlashButton.setRotation(360 - orientation);
        flipCameraButton.setRotation(360 - orientation);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(orientation == 0 ? getResources().getString(R.string.camera_post) : "");
            actionBar.setDisplayHomeAsUpEnabled(orientation % 180 == 0);
            invalidateOptionsMenu();
        }
    }

    private void playRecordStartAnimation() {
        captureButton.setImageDrawable(recordStartDrawable);
        recordStartDrawable.start();
    }

    private void playRecordStopAnimation() {
        if (captureButton.getDrawable() == recordStartDrawable) {
            captureButton.setImageDrawable(recordStopDrawable);
            recordStopDrawable.registerAnimationCallback(new Animatable2Compat.AnimationCallback() {
                @Override
                public void onAnimationEnd(Drawable drawable) {
                    super.onAnimationEnd(drawable);
                    captureButton.setImageDrawable(captureButtonDrawable);
                    recordStopDrawable.unregisterAnimationCallback(this);
                }
            });
            recordStopDrawable.start();
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

    private void flipCamera() {
        Log.d("CameraActivity: flipCamera");
        if (cameraIsAvailable()) {
            cameraView.toggleFacing();
        }
    }

    private void toggleFlash() {
        Log.d("CameraActivity: toggleFlash");
        if (cameraIsAvailable()) {
            isFlashOn = !isFlashOn;
            toggleFlashButton.setImageDrawable(isFlashOn ? flashOnDrawable : flashOffDrawable);
            Flash flashMode = isFlashOn ? Flash.ON : Flash.OFF;
            if (cameraView.getFlash() != flashMode) {
                cameraView.setFlash(flashMode);
            }
        }
    }

    private boolean isRecordingVideoAllowed() {
        return purpose == PURPOSE_COMPOSE;
    }

    private boolean cameraIsAvailable() {
        return cameraView != null && !isTakingPhoto && !cameraView.isTakingPicture() && !isRecordingVideo && !cameraView.isTakingVideo();
    }

    private void takePicture() {
        Log.d("CameraActivity: takePicture");
        isTakingPhoto = true;
        cameraView.takePicture();
    }

    private void takeVideo() {
        Log.d("CameraActivity: takeVideo");
        isRecordingVideo = true;
        playRecordStartAnimation();
        clearMediaFile();
        mediaFile = generateTempCameraFile(Media.MEDIA_TYPE_VIDEO);
        cameraView.takeVideo(mediaFile, maxVideoDurationSeconds * 1000);
    }

    private void startRecordingTimer() {
        videoTimer.setVisibility(View.VISIBLE);
        videoTimer.setBase(SystemClock.elapsedRealtime());
        videoTimer.start();
    }

    private void stopRecordingTimer() {
        videoTimer.setVisibility(View.GONE);
        videoTimer.stop();
    }

    private File generateTempCameraFile(@Media.MediaType int mediaType) {
        File tempFile = FileStore.getInstance().getCameraFile(RandomId.create() + "." + Media.getFileExt(mediaType));
        Log.d("CameraActivity: generateTempFile " + Uri.fromFile(tempFile));
        return  tempFile;
    }

    private void clearMediaFile() {
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

            case PURPOSE_AVATAR:
                startAvatarPreviewForUri(uri);
                break;
        }
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
        final Intent intent = new Intent(this, AvatarPreviewActivity.class);
        intent.setData(uri);
        startActivityForResult(intent, REQUEST_CODE_SET_AVATAR);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SET_AVATAR && resultCode == RESULT_OK) {
            setResult(RESULT_OK, data);
            finish();
        }
    }
}

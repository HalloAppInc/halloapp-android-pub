package com.halloapp.ui.camera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraCharacteristics;
import android.net.Uri;
import android.os.Build;
import android.util.Rational;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.ViewTreeObserver;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
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
import androidx.core.content.ContextCompat;
import androidx.exifinterface.media.ExifInterface;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.halloapp.AndroidHallOfShame;
import com.halloapp.Constants;
import com.halloapp.FileStore;
import com.halloapp.content.Media;
import com.halloapp.util.OrientationListener;
import com.halloapp.util.RandomId;
import com.halloapp.util.ThreadUtils;
import com.halloapp.util.logs.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HalloCamera {

    public interface Listener {
        void onCaptureSuccess(File file, @Media.MediaType int type);
        void onCaptureFailure(@Media.MediaType int type);
        void onStateUpdated(HalloCamera camera);
        void onCameraInitFailure();
        void onCameraNotFound();
        void onCameraPermissionsMissing();
    }

    private static final int FOCUS_AUTO_CANCEL_DURATION_SEC = 2;

    private final ExecutorService cameraExecutor;
    private LifecycleOwner lifecycleOwner;
    private Context context;
    private final Listener listener;
    private ProcessCameraProvider provider;
    private Camera camera;
    private ImageCapture imageCapture;
    private VideoCapture videoCapture;

    private boolean isSquareAspectRatio;
    private boolean isFlashSupported;
    private boolean hasBackCamera;
    private boolean hasFrontCamera;
    private int rotation;

    private boolean isLimitedLevelSupported = true;
    private boolean isUsingBackCamera;
    private boolean isFlashOn = false;
    private boolean isRecordingVideo = false;
    private boolean isTakingPreviewSnapshot = false;
    private boolean isPreviewFlashEnabled = false;
    private boolean isCapturingPhoto = false;

    private PreviewView previewView;

    public HalloCamera(@NonNull LifecycleOwner lifecycleOwner, @NonNull PreviewView view, boolean isSquareAspectRatio, boolean isUsingBackCamera, int rotation, @NonNull Listener listener) {
        Log.d("HalloCamera: init");

        this.lifecycleOwner = lifecycleOwner;
        this.previewView = view;
        this.context = view.getContext();
        this.isSquareAspectRatio = isSquareAspectRatio;
        this.isUsingBackCamera = isUsingBackCamera;
        this.rotation = rotation;
        this.listener = listener;

        cameraExecutor = Executors.newSingleThreadExecutor();

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);
        cameraProviderFuture.addListener(() -> {
            try {
                provider = cameraProviderFuture.get();
            } catch (ExecutionException | InterruptedException e) {
                Log.e("HalloCamera: ProcessCameraProvider init failed", e);
                listener.onCameraInitFailure();
            }

            bindCameraUseCases();
        }, ContextCompat.getMainExecutor(context));

        this.lifecycleOwner.getLifecycle().addObserver(new LifecycleEventObserver() {
            @Override
            public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                if (event.getTargetState() == Lifecycle.State.DESTROYED) {
                    cameraExecutor.shutdown();

                    HalloCamera.this.lifecycleOwner.getLifecycle().removeObserver(this);
                    HalloCamera.this.lifecycleOwner = null;
                    HalloCamera.this.context = null;
                    HalloCamera.this.previewView = null;
                }
            }
        });
    }

    @MainThread
    public void bindCameraUseCases() {
        Log.d("HalloCamera: bind");

        if (provider == null) {
            return;
        }

        provider.unbindAll();

        final CameraSelector backCameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        final CameraSelector frontCameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build();
        hasBackCamera = hasCamera(provider, backCameraSelector);
        hasFrontCamera = hasCamera(provider, frontCameraSelector);

        if (!hasBackCamera && !hasFrontCamera) {
            Log.e("HalloCamera: no front or back camera");

            if (listener != null) {
                listener.onCameraNotFound();
            }

            return;
        } else if (!hasBackCamera) {
            isUsingBackCamera = false;
        } else if (!hasFrontCamera) {
            isUsingBackCamera = true;
        }

        isLimitedLevelSupported = true;
        if (hasFrontCamera) {
            isLimitedLevelSupported = isLimitedLevelSupported && isCameraHardwareLevelLimitedOrBetter(provider, frontCameraSelector);
        }

        if (hasBackCamera) {
            isLimitedLevelSupported = isLimitedLevelSupported && isCameraHardwareLevelLimitedOrBetter(provider, backCameraSelector);
        }

        Log.d("HalloCamera: isLegacyLevelSupported = " + isLimitedLevelSupported);

        UseCaseGroup.Builder useCaseGroupBuilder = new UseCaseGroup.Builder();
        if (isLimitedLevelSupported) {
            imageCapture = setupImageCapture();
            useCaseGroupBuilder.addUseCase(imageCapture);
        }
        videoCapture = setupVideoCapture();
        useCaseGroupBuilder.addUseCase(videoCapture);

        if (isSquareAspectRatio) {
            ViewPort viewPort = new ViewPort.Builder(new Rational(1, 1), Surface.ROTATION_0).build();
            useCaseGroupBuilder.setViewPort(viewPort);
        }

        final Preview preview = new Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3).build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        useCaseGroupBuilder.addUseCase(preview);

        final CameraSelector cameraSelector = isUsingBackCamera ? backCameraSelector : frontCameraSelector;
        camera = provider.bindToLifecycle(lifecycleOwner, cameraSelector, useCaseGroupBuilder.build());

        isFlashSupported = camera.getCameraInfo().hasFlashUnit();
        final boolean isFlashEnabled = isFlashSupported && isFlashOn;

        if (imageCapture != null) {
            imageCapture.setFlashMode(isFlashEnabled ? ImageCapture.FLASH_MODE_ON : ImageCapture.FLASH_MODE_OFF);
        }

        previewView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void onGlobalLayout() {
                if (previewView.getMeasuredWidth() > 0 && previewView.getMeasuredHeight() > 0) {
                    previewView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    setFocusMode((float) previewView.getWidth() / 2,(float) previewView.getHeight() / 2, true);

                    final ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                        @Override
                        public boolean onScale(ScaleGestureDetector detector) {
                            zoomCamera(detector.getScaleFactor());
                            return true;
                        }
                    });
                    scaleGestureDetector.setQuickScaleEnabled(false);
                    previewView.setOnTouchListener((view, motionEvent) -> {
                        scaleGestureDetector.onTouchEvent(motionEvent);
                        if (!scaleGestureDetector.isInProgress() && motionEvent.getAction() == MotionEvent.ACTION_UP) {
                            setFocusMode(motionEvent.getX(), motionEvent.getY(), false);
                        }
                        return true;
                    });
                }
            }
        });

        listener.onStateUpdated(this);
    }

    boolean hasCamera(@NonNull ProcessCameraProvider cameraProvider, @NonNull CameraSelector cameraSelector) {
        try {
            return cameraProvider.hasCamera(cameraSelector);
        } catch (CameraInfoUnavailableException e) {
            return false;
        }
    }

    @MainThread
    private ImageCapture setupImageCapture() {
        return new ImageCapture.Builder()
                .setTargetRotation(rotation)
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build();
    }

    @SuppressLint("RestrictedApi")
    @MainThread
    private VideoCapture setupVideoCapture() {
        return new VideoCapture.Builder()
                .setTargetRotation(rotation)
                .setTargetAspectRatio(AndroidHallOfShame.deviceDoesNotSupport4To3Encoding() ? AspectRatio.RATIO_16_9 : AspectRatio.RATIO_4_3)
                .build();
    }

    @MainThread
    private void setFocusMode(float x, float y, boolean isAutoFocus) {
        if (camera == null) {
            return;
        }

        final MeteringPointFactory meteringPointFactory =
                new SurfaceOrientedMeteringPointFactory(previewView.getWidth(), previewView.getHeight());
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

    @MainThread
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

    @SuppressLint("RestrictedApi")
    public void onRotationChanged(int rotation) {
        this.rotation = rotation;

        if (imageCapture != null) {
            imageCapture.setTargetRotation(rotation);
        }

        if (videoCapture != null) {
            videoCapture.setTargetRotation(rotation);
        }
    }

    @OptIn(markerClass = ExperimentalCamera2Interop.class)
    private Integer getCameraHardwareLevel(@NonNull CameraInfo cameraInfo) {
        return Camera2CameraInfo.from(cameraInfo).getCameraCharacteristic(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
    }

    @MainThread
    private boolean isCameraHardwareLevelLimitedOrBetter(@NonNull ProcessCameraProvider cameraProvider, @NonNull CameraSelector cameraSelector) {
        List<CameraInfo> filteredCameraInfos = cameraSelector.filter(cameraProvider.getAvailableCameraInfos());
        if (!filteredCameraInfos.isEmpty()) {
            final Integer deviceLevel = getCameraHardwareLevel(filteredCameraInfos.get(0));
            if (deviceLevel != null) {
                if (deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED||
                        deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL) {
                    return true;
                }
                if (Build.VERSION.SDK_INT >= 28) {
                    if (deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL) {
                        return false;
                    }
                }
                if (Build.VERSION.SDK_INT >= 24) {
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
            Log.e("HalloCamera: applyExifOrientation", e);
        }
    }

    @MainThread
    public void flip() {
        Log.d("HalloCamera: flip");
        if (hasBackCamera && hasFrontCamera && !isRecordingVideo) {
            isUsingBackCamera = !isUsingBackCamera;
            bindCameraUseCases();
        }
    }

    @MainThread
    public void toggleFlash() {
        Log.d("HalloCamera: toggleFlash");
        if (isFlashSupported && !isRecordingVideo) {
            isFlashOn = !isFlashOn;

            if (imageCapture != null) {
                final boolean isFlashEnabled = camera.getCameraInfo().hasFlashUnit() && isFlashOn;
                imageCapture.setFlashMode(isFlashEnabled ? ImageCapture.FLASH_MODE_ON : ImageCapture.FLASH_MODE_OFF);
            }
        }
    }

    @MainThread
    public void takePhoto() {
        if (isLimitedLevelSupported) {
            takeImageCapturePhoto();
        } else {
            takePreviewSnapshot();
        }
    }

    @MainThread
    private void takeImageCapturePhoto() {
        if (imageCapture == null || isRecordingVideo || isCapturingPhoto) {
            return;
        }
        isCapturingPhoto = true;

        Log.d("HalloCamera: takeImageCapturePhoto");

        File file = generateTempMediaFile(Media.MEDIA_TYPE_IMAGE);
        ImageCapture.Metadata metadata = new ImageCapture.Metadata();
        metadata.setReversedHorizontal(!isUsingBackCamera);
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).setMetadata(metadata).build();

        imageCapture.takePicture(outputFileOptions, cameraExecutor, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                Log.d("HalloCamera: takeImageCapturePhoto onImageSaved " + Uri.fromFile(file));
                listener.onCaptureSuccess(file, Media.MEDIA_TYPE_IMAGE);
                isCapturingPhoto = false;
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e("HalloCamera: takeImageCapturePhoto error " + exception);
                listener.onCaptureFailure(Media.MEDIA_TYPE_IMAGE);
                isCapturingPhoto = false;
            }
        });
    }

    @MainThread
    private void takePreviewSnapshot() {
        if (previewView == null || isRecordingVideo || isTakingPreviewSnapshot) {
            return;
        }
        isTakingPreviewSnapshot = true;

        Log.d("HalloCamera: takePreviewSnapshot");

        File file = generateTempMediaFile(Media.MEDIA_TYPE_IMAGE);
        isPreviewFlashEnabled = camera.getCameraInfo().hasFlashUnit() && isFlashOn;

        if (isPreviewFlashEnabled) {
            final ListenableFuture<Void> torchEnabledFuture = camera.getCameraControl().enableTorch(true);
            Futures.addCallback(
                    torchEnabledFuture,
                    new FutureCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            saveAndHandlePreviewSnapshot(file);
                        }

                        @Override
                        public void onFailure(@NonNull Throwable throwable) {
                            Log.e("HalloCamera: takePreviewSnapshot error " + throwable);
                            listener.onCaptureFailure(Media.MEDIA_TYPE_IMAGE);
                            cleanAfterPreviewSnapshot();
                        }
                    },
                    ContextCompat.getMainExecutor(context)
            );
        } else {
            saveAndHandlePreviewSnapshot(file);
        }
    }

    @MainThread
    private void saveAndHandlePreviewSnapshot(File file) {
        final Bitmap bitmap = previewView.getBitmap();

        if (bitmap == null) {
            Log.e("HalloCamera: saveAndHandlePreviewSnapshot could not create bitmap from preview");
            listener.onCaptureFailure(Media.MEDIA_TYPE_IMAGE);
            cleanAfterPreviewSnapshot();
        } else {
            cameraExecutor.execute(() -> {
                try (final FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, Constants.JPEG_QUALITY, fileOutputStream);
                    fileOutputStream.close();
                    applyExifOrientation(file, OrientationListener.getRotationAngle(rotation));
                    Log.d("HalloCamera: saveAndHandlePreviewSnapshot image created " + Uri.fromFile(file));

                    listener.onCaptureSuccess(file, Media.MEDIA_TYPE_IMAGE);
                } catch (IOException exception) {
                    Log.e("HalloCamera: saveAndHandlePreviewSnapshot error " + exception);
                    listener.onCaptureFailure(Media.MEDIA_TYPE_IMAGE);
                } finally {
                    ContextCompat.getMainExecutor(context).execute(this::cleanAfterPreviewSnapshot);
                }
            });
        }
    }

    @MainThread
    private void cleanAfterPreviewSnapshot() {
        if (isPreviewFlashEnabled) {
            final ListenableFuture<Void> torchDisabledFuture = camera.getCameraControl().enableTorch(false);
            Futures.addCallback(
                    torchDisabledFuture,
                    new FutureCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            isTakingPreviewSnapshot = false;
                        }

                        @Override
                        public void onFailure(@NonNull Throwable throwable) {
                            Log.e("HalloCamera: cleanAfterPreviewSnapshot error " + throwable);
                            isTakingPreviewSnapshot = false;
                        }
                    },
                    ContextCompat.getMainExecutor(context)
            );
        } else {
            isTakingPreviewSnapshot = false;
        }
    }

    @SuppressLint("RestrictedApi")
    @MainThread
    public void startRecordingVideo() {
        if (videoCapture == null || isRecordingVideo) {
            return;
        }
        isRecordingVideo = true;

        Log.d("HalloCamera: startRecordingVideo");

        File file = generateTempMediaFile(Media.MEDIA_TYPE_VIDEO);

        final VideoCapture.OutputFileOptions outputFileOptions = new VideoCapture.OutputFileOptions.Builder(file).build();
        ThreadUtils.runWithoutStrictModeRestrictions(() -> {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                videoCapture.startRecording(outputFileOptions, cameraExecutor, new VideoCapture.OnVideoSavedCallback() {
                    @Override
                    public void onVideoSaved(@NonNull VideoCapture.OutputFileResults outputFileResults) {
                        Log.d("HalloCamera: startRecordingVideo onVideoSaved " + Uri.fromFile(file));

                        listener.onCaptureSuccess(file, Media.MEDIA_TYPE_VIDEO);
                        isRecordingVideo = false;
                    }

                    @Override
                    public void onError(int videoCaptureError, @NonNull String message, @Nullable @org.jetbrains.annotations.Nullable Throwable cause) {
                        Log.e("HalloCamera: startRecordingVideo error " + cause);
                        listener.onCaptureFailure(Media.MEDIA_TYPE_VIDEO);
                        isRecordingVideo = false;
                    }
                });
            } else {
                Log.d("HalloCamera: startRecordingVideo RECORD_AUDIO permission lost");
                listener.onCameraPermissionsMissing();
                isRecordingVideo = false;
            }
        });
    }

    @SuppressLint("RestrictedApi")
    @MainThread
    public void stopRecordingVideo() {
        videoCapture.stopRecording();
        isRecordingVideo = false;
    }

    private File generateTempMediaFile(@Media.MediaType int type) {
        File tempFile = FileStore.getInstance().getCameraFile(RandomId.create() + "." + Media.getFileExt(type));
        Log.d("HalloCamera: generateTempMediaFile " + Uri.fromFile(tempFile));
        return  tempFile;
    }

    public boolean isCapturingPhoto() {
        return isCapturingPhoto || isTakingPreviewSnapshot;
    }

    public boolean isRecordingVideo() {
        return isRecordingVideo;
    }

    public boolean hasBackCamera() {
        return hasBackCamera;
    }

    public boolean hasFrontCamera() {
        return hasFrontCamera;
    }

    public boolean isFlashSupported() {
        return isFlashSupported;
    }

    public boolean isFlashOn() {
        return isFlashOn;
    }

    public boolean isUsingBackCamera() {
        return isUsingBackCamera;
    }

    public void setSquareAspectRatio(boolean enable) {
        isSquareAspectRatio = enable;
    }
}

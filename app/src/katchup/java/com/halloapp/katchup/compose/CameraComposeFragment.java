package com.halloapp.katchup.compose;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Outline;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.daasuu.mp4compose.composer.Mp4Composer;
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
import com.halloapp.FileStore;
import com.halloapp.R;
import com.halloapp.content.Media;
import com.halloapp.katchup.SelfiePostComposerActivity;
import com.halloapp.katchup.media.PromptOverlayComposer;
import com.halloapp.media.ExoUtils;
import com.halloapp.ui.camera.HalloCamera;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.ContentPlayerView;
import com.halloapp.widget.SnackbarHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CameraComposeFragment extends ComposeFragment {

    public static CameraComposeFragment newInstance(String prompt) {
        Bundle args = new Bundle();
        args.putString(EXTRA_PROMPT, prompt);

        CameraComposeFragment fragment = new CameraComposeFragment();
        fragment.setArguments(args);

        return fragment;
    }

    private static final String EXTRA_PROMPT = "prompt";

    private HalloCamera camera;

    private PreviewView cameraPreviewView;
    private TextView cameraPromptText;
    private View cameraPromptContainer;

    private View controlsContainer;
    private View mediaPreviewContainer;
    private View cameraPreviewContainer;

    private ImageView mediaPreviewView;

    private ImageButton flipCameraButton;
    private ImageButton toggleFlashButton;

    private View captureButton;
    private ImageView captureButtonInner;

    private SelfiePostComposerActivity host;

    private SelfieComposerViewModel viewModel;

    private File captureFile;
    private @Media.MediaType int captureType;
    private File createdImage;

    private Chronometer videoRecordTimer;
    private View videoTimerContainer;

    private ContentPlayerView videoPlayerView;
    private TextView previewPromptText;

    private SimpleExoPlayer videoPlayer;

    private String prompt;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.katchup_fragment_camera_compose, container, false);

        cameraPreviewContainer = root.findViewById(R.id.camera_container);
        cameraPreviewView = root.findViewById(R.id.cameraPreview);
        cameraPromptText = root.findViewById(R.id.camera_prompt_text);
        cameraPromptContainer = root.findViewById(R.id.camera_prompt_container);

        mediaPreviewContainer = root.findViewById(R.id.preview_container);
        mediaPreviewView = root.findViewById(R.id.media_preview);
        videoPlayerView = root.findViewById(R.id.video_player);
        previewPromptText = root.findViewById(R.id.preview_prompt_text);

        controlsContainer = root.findViewById(R.id.controls_container);
        flipCameraButton = root.findViewById(R.id.flip_camera);
        toggleFlashButton = root.findViewById(R.id.toggle_flash);
        captureButton = root.findViewById(R.id.capture);
        captureButtonInner = root.findViewById(R.id.capture_inner);

        videoRecordTimer = root.findViewById(R.id.video_timer);
        videoTimerContainer = root.findViewById(R.id.video_timer_container);

        flipCameraButton.setOnClickListener(v -> camera.flip());
        toggleFlashButton.setOnClickListener(v -> {
            camera.toggleFlash();
            updateFlashButton();
        });
        final GestureDetector gestureDetector = new GestureDetector(requireContext(), new GestureDetector.SimpleOnGestureListener() {
            public void onLongPress(MotionEvent e) {
                camera.startRecordingVideo();
                updateCaptureButton();
                updateRecordingTimer(true);
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                Log.d("CameraComposeFragment: capture button onClick");
                if(!camera.isCapturingPhoto()) {
                    camera.takePhoto();
                }
                return true;
            }
        });
        captureButton.setOnTouchListener((v, event) -> {
            if (gestureDetector.onTouchEvent(event)) {
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (camera.isRecordingVideo()) {
                    camera.stopRecordingVideo();
                    updateRecordingTimer(false);
                }
                updateCaptureButton();
            }
            return true;
        });
        final float cameraViewRadius = getResources().getDimension(R.dimen.camera_preview_border_radius);
        ViewOutlineProvider roundedOutlineProvider = new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), cameraViewRadius);
            }
        };
        cameraPreviewView.setClipToOutline(true);
        cameraPreviewView.setOutlineProvider(roundedOutlineProvider);

        mediaPreviewView.setClipToOutline(true);
        mediaPreviewView.setOutlineProvider(roundedOutlineProvider);

        viewModel = new ViewModelProvider(requireActivity()).get(SelfieComposerViewModel.class);
        viewModel.getComposerState().observe(getViewLifecycleOwner(), state -> {
            switch (state) {
                case SelfieComposerViewModel.ComposeState.COMPOSING_CONTENT:
                    showCaptureView();
                    break;
                case SelfieComposerViewModel.ComposeState.COMPOSING_SELFIE:
                case SelfieComposerViewModel.ComposeState.TRANSITIONING:
                case SelfieComposerViewModel.ComposeState.READY_TO_SEND:
                    showPreviewView();
                    break;
            }
        });

        Bundle args = getArguments();
        if (args != null) {
            prompt = args.getString(EXTRA_PROMPT, null);
        }
        previewPromptText.setText(prompt);
        cameraPromptText.setText(prompt);

        initializeCamera();

        return root;
    }

    private void updateRecordingTimer(boolean recording) {
        if (recording) {
            videoTimerContainer.setVisibility(View.VISIBLE);
            videoRecordTimer.setBase(SystemClock.elapsedRealtime());
            videoRecordTimer.start();
        } else {
            videoTimerContainer.setVisibility(View.GONE);
            videoRecordTimer.stop();
        }
    }

    private void updateCaptureButton() {
        if (camera != null && camera.isRecordingVideo()) {
            captureButtonInner.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.recording_button)));
        } else {
            captureButtonInner.setImageTintList(ColorStateList.valueOf(Color.WHITE));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (camera != null && viewModel.getComposerState().getValue() == SelfieComposerViewModel.ComposeState.COMPOSING_CONTENT) {
            camera.bindCameraUseCases();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (videoPlayer != null) {
            videoPlayer.stop(true);
            videoPlayer = null;
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof SelfiePostComposerActivity) {
            host = (SelfiePostComposerActivity) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        host = null;
    }

    private void initializeCamera() {
        if (camera != null) {
            return;
        }
        camera = new HalloCamera(this, cameraPreviewView, false, true, Surface.ROTATION_0, new HalloCamera.DefaultListener() {
            @Override
            public void onCaptureSuccess(File file, int type) {
                if (type != Media.MEDIA_TYPE_VIDEO) {
                    cameraPreviewView.post(() -> {
                        captureFile = file;
                        captureType = type;
                        viewModel.onComposedMedia(Uri.fromFile(file), type);
                    });
                } else {
                    cameraPromptText.setDrawingCacheEnabled(true);
                    final Bitmap b = Bitmap.createBitmap(cameraPromptText.getDrawingCache());
                    cameraPromptText.setDrawingCacheEnabled(false);

                    float bottomMarginPercent = cameraPromptContainer.getY() / cameraPreviewView.getHeight();

                    File outFile = FileStore.getInstance().getTmpFile("camera-compose");
                    Mp4Composer composer = new PromptOverlayComposer(file, outFile, b, bottomMarginPercent);
                    composer.listener(new Mp4Composer.Listener() {
                        @Override
                        public void onProgress(double progress) {
                        }

                        @Override
                        public void onCurrentWrittenVideoTime(long timeUs) {
                        }

                        @Override
                        public void onCompleted() {
                            cameraPreviewView.post(() -> {
                                captureFile = outFile;
                                captureType = type;
                                viewModel.onComposedMedia(Uri.fromFile(outFile), type);
                            });
                        }

                        @Override
                        public void onCanceled() {
                        }

                        @Override
                        public void onFailed(Exception exception) {
                            Log.e("CameraComposeFragment camera prompt compose failed", exception);
                            SnackbarHelper.showWarning(requireActivity(), R.string.camera_error_video);
                        }
                    });
                    composer.start();
                }
            }
            @Override
            public void onStateUpdated(HalloCamera camera) {
                cameraPreviewView.post(() -> {
                    flipCameraButton.setVisibility(!camera.hasBackCamera() || !camera.hasFrontCamera() ? View.INVISIBLE : View.VISIBLE);
                    updateFlashButton();
                });
            }
        });
    }

    private void showCaptureView() {
        controlsContainer.setVisibility(View.VISIBLE);
        cameraPreviewContainer.setVisibility(View.VISIBLE);
        camera.bindCameraUseCases();
        mediaPreviewContainer.setVisibility(View.GONE);
        videoPlayerView.setPlayer(null);
        if (videoPlayer != null) {
            videoPlayer.stop(true);
            videoPlayer = null;
        }
    }

    private void showPreviewView() {
        previewPromptText.setVisibility(captureType == Media.MEDIA_TYPE_IMAGE ? View.VISIBLE : View.GONE);
        controlsContainer.setVisibility(View.GONE);
        cameraPreviewContainer.setVisibility(View.GONE);
        mediaPreviewContainer.setVisibility(View.VISIBLE);
        if (captureFile != null) {
            host.getMediaThumbnailLoader().load(mediaPreviewView, Media.createFromFile(captureType, captureFile));
            if (captureType == Media.MEDIA_TYPE_VIDEO) {
                videoPlayerView.setVisibility(View.VISIBLE);
                bindVideo();
            } else {
                videoPlayerView.setPlayer(null);
                if (videoPlayer != null) {
                    videoPlayer.stop(true);
                }
                videoPlayerView.setVisibility(View.GONE);
            }
        } else {
            host.getMediaThumbnailLoader().cancel(mediaPreviewView);
            mediaPreviewView.setImageBitmap(null);
        }
        if (captureType == Media.MEDIA_TYPE_IMAGE) {
            mediaPreviewContainer.post(() -> {
                mediaPreviewContainer.setDrawingCacheEnabled(true);
                final Bitmap b = Bitmap.createBitmap(mediaPreviewContainer.getDrawingCache());
                mediaPreviewContainer.setDrawingCacheEnabled(false);
                if (createdImage == null) {
                    createdImage = FileStore.getInstance().getTmpFile(RandomId.create() + "." + Media.getFileExt(Media.MEDIA_TYPE_IMAGE));
                }
                BgWorkers.getInstance().execute(() -> {
                    if (createdImage == null) {
                        return;
                    }
                    if (!createdImage.delete()) {
                        Log.e("CameraComposeFragment/savePreview failed to delete file");
                    }
                    try (FileOutputStream out = new FileOutputStream(createdImage)) {
                        if (!b.compress(Bitmap.CompressFormat.JPEG, Constants.JPEG_QUALITY, out)) {
                            Log.e("CameraComposeFragment/savePreview failed to compress");
                        }
                    } catch (IOException e) {
                        Log.e("CameraComposeFragment/savePreview failed", e);
                    }
                });
            });
        }
    }

    private void updateFlashButton() {
        toggleFlashButton.setVisibility(camera.isFlashSupported() ? View.VISIBLE : View.INVISIBLE);
        toggleFlashButton.setImageResource(camera.isFlashOn() ? R.drawable.ic_flash_on : R.drawable.ic_flash_off);
    }

    private void bindVideo() {
        if (captureFile != null) {
            final DataSource.Factory dataSourceFactory;
            final MediaItem exoMediaItem;
            dataSourceFactory = ExoUtils.getDefaultDataSourceFactory(videoPlayerView.getContext());
            exoMediaItem = ExoUtils.getUriMediaItem(Uri.fromFile(captureFile));

            videoPlayerView.setPauseHiddenPlayerOnScroll(true);
            videoPlayerView.setControllerAutoShow(true);
            final MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(exoMediaItem);

            if (videoPlayer != null) {
                videoPlayer.stop(true);
            }
            videoPlayer = new SimpleExoPlayer.Builder(videoPlayerView.getContext()).build();

            videoPlayerView.setPlayer(videoPlayer);
            videoPlayerView.setUseController(false);
            videoPlayerView.setVisibility(View.VISIBLE);

            videoPlayer.addListener(new Player.EventListener() {
                @Override
                public void onPlaybackStateChanged(int state) {

                }

                @Override
                public void onIsPlayingChanged(boolean isPlaying) {
                    videoPlayerView.setKeepScreenOn(isPlaying);
                }
            });

            videoPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);
            videoPlayer.setMediaSource(mediaSource);
            videoPlayer.setPlayWhenReady(true);
            videoPlayer.prepare();

            PlayerControlView controlView = videoPlayerView.findViewById(R.id.exo_controller);
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

    @Override
    public Media getComposedMedia() {
        return Media.createFromFile(captureType, captureType == Media.MEDIA_TYPE_IMAGE ? createdImage : captureFile);
    }

    @Override
    public View getPreview() {
        return mediaPreviewContainer;
    }
}

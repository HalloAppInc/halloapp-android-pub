package com.halloapp.katchup.compose;

import android.content.Context;
import android.graphics.Outline;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.view.PreviewView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.R;
import com.halloapp.content.Media;
import com.halloapp.katchup.SelfiePostComposerActivity;
import com.halloapp.ui.camera.HalloCamera;
import com.halloapp.util.logs.Log;

import java.io.File;

public class CameraComposeFragment extends Fragment {

    private HalloCamera camera;

    private PreviewView cameraPreviewView;

    private View controlsContainer;
    private View mediaPreviewContainer;
    private View cameraPreviewContainer;

    private ImageView mediaPreviewView;

    private ImageButton flipCameraButton;
    private ImageButton toggleFlashButton;

    private View captureButton;

    private SelfiePostComposerActivity host;

    private SelfieComposerViewModel viewModel;

    private File captureFile;
    private @Media.MediaType int captureType;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.ketchup_fragment_camera_compose, container, false);

        cameraPreviewContainer = root.findViewById(R.id.camera_container);
        cameraPreviewView = root.findViewById(R.id.cameraPreview);

        mediaPreviewContainer = root.findViewById(R.id.preview_container);
        mediaPreviewView = root.findViewById(R.id.media_preview);

        controlsContainer = root.findViewById(R.id.controls_container);
        flipCameraButton = root.findViewById(R.id.flip_camera);
        toggleFlashButton = root.findViewById(R.id.toggle_flash);
        captureButton = root.findViewById(R.id.capture);

        flipCameraButton.setOnClickListener(v -> camera.flip());
        toggleFlashButton.setOnClickListener(v -> {
            camera.toggleFlash();
            updateFlashButton();
        });
        captureButton.setOnClickListener(v -> {
            Log.d("CameraComposeFragment: capture button onClick");
            if(!camera.isCapturingPhoto()) {
                camera.takePhoto();
            }
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
                case SelfieComposerViewModel.ComposeState.READY_TO_SEND:
                    showPreviewView();
                    break;
            }
        });

        initializeCamera();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (camera != null && viewModel.getComposerState().getValue() == SelfieComposerViewModel.ComposeState.COMPOSING_CONTENT) {
            camera.bindCameraUseCases();
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
                cameraPreviewView.post(() -> {
                    captureFile = file;
                    captureType = type;
                    showPreviewView();
                    viewModel.onComposedMedia(Uri.fromFile(file), type);
                });
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
    }

    private void showPreviewView() {
        controlsContainer.setVisibility(View.GONE);
        cameraPreviewContainer.setVisibility(View.GONE);
        mediaPreviewContainer.setVisibility(View.VISIBLE);
        if (captureFile != null) {
            host.getMediaThumbnailLoader().load(mediaPreviewView, Media.createFromFile(captureType, captureFile));
        } else {
            host.getMediaThumbnailLoader().cancel(mediaPreviewView);
            mediaPreviewView.setImageBitmap(null);
        }
    }


    private void updateFlashButton() {
        toggleFlashButton.setVisibility(camera.isFlashSupported() ? View.VISIBLE : View.INVISIBLE);
        toggleFlashButton.setImageResource(camera.isFlashOn() ? R.drawable.ic_flash_on : R.drawable.ic_flash_off);
    }
}

package com.halloapp.katchup;

import android.content.Context;
import android.content.Intent;
import android.graphics.Outline;
import android.net.Uri;
import android.os.Bundle;
import android.view.Surface;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.view.PreviewView;

import com.halloapp.R;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.camera.HalloCamera;

import java.io.File;

public class ProfilePictureCameraActivity extends HalloActivity {

    public static Intent open(@NonNull Context context) {
        return new Intent(context, ProfilePictureCameraActivity.class);
    }

    private HalloCamera camera;
    private PreviewView previewView;
    private ImageView toggleFlashBtn;
    private View flipCameraBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile_picture_camera);

        View prev = findViewById(R.id.prev);
        prev.setOnClickListener(v -> finish());

        View captureBtn = findViewById(R.id.capture);
        captureBtn.setOnClickListener(v -> {
            if (!camera.isCapturingPhoto()) {
                camera.takePhoto();
            }
        });

        toggleFlashBtn = findViewById(R.id.toggle_flash);
        toggleFlashBtn.setOnClickListener(v -> {
            if (!camera.isCapturingPhoto() && camera.isFlashSupported()) {
                camera.toggleFlash();
                updateFlashButton();
            }
        });

        flipCameraBtn = findViewById(R.id.flip_camera);
        flipCameraBtn.setOnClickListener(v -> {
            if (!camera.isCapturingPhoto() && camera.hasBackCamera() && camera.hasFrontCamera()) {
                camera.flip();
            }
        });

        previewView = findViewById(R.id.camera_preview);

        final float cameraViewRadius = getResources().getDimension(R.dimen.camera_preview_border_radius);
        ViewOutlineProvider roundedOutlineProvider = new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), cameraViewRadius);
            }
        };
        previewView.setClipToOutline(true);
        previewView.setOutlineProvider(roundedOutlineProvider);

        setupCamera();
    }

    private void updateFlashButton() {
        toggleFlashBtn.setVisibility(camera.isFlashSupported() ? View.VISIBLE : View.INVISIBLE);
        toggleFlashBtn.setImageResource(camera.isFlashOn() ? R.drawable.ic_flash_on : R.drawable.ic_flash_off);
    }

    private void setupCamera() {
        if (camera != null) {
            return;
        }

        camera = new HalloCamera(this, previewView, false, true, Surface.ROTATION_0, new HalloCamera.DefaultListener() {
            @Override
            public void onCaptureSuccess(File file, int type) {
                runOnUiThread(() -> {
                    Intent intent = new Intent();
                    intent.setData(Uri.fromFile(file));
                    setResult(RESULT_OK, intent);
                    finish();
                });
            }
            @Override
            public void onStateUpdated(HalloCamera camera) {
                runOnUiThread(() -> {
                    flipCameraBtn.setVisibility(!camera.hasBackCamera() || !camera.hasFrontCamera() ? View.INVISIBLE : View.VISIBLE);
                    updateFlashButton();
                });
            }
        });
    }
}

package com.halloapp.calling.calling;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.webrtc.CapturerObserver;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoCapturer;

import java.lang.ref.WeakReference;

public class HAVideoCapturer {
    @Nullable
    public final VideoCapturer videoCapturer;
    @NonNull
    public final WeakReference<Activity> activityRef;

    public final boolean frontFacing;

    public HAVideoCapturer(@Nullable VideoCapturer videoCapturer, @NonNull Activity activity, boolean frontFacing) {
        this.videoCapturer = videoCapturer;
        this.activityRef = new WeakReference<>(activity);
        this.frontFacing = frontFacing;
    }

    public void stopCapturer() throws InterruptedException {
        if (videoCapturer != null) {
            videoCapturer.stopCapture();
        }
    }

    public void dispose() {
        if (videoCapturer != null) {
            videoCapturer.dispose();
        }
    }

    public boolean initialize(SurfaceTextureHelper surfaceTextureHelper, CapturerObserver capturerObserver) {
        final Activity activity = activityRef.get();
        if (videoCapturer != null && activity != null) {
            videoCapturer.initialize(surfaceTextureHelper, activity, capturerObserver);
            return true;
        }
        return false;
    }

    public void startCapturer(int width, int height, int fps) {
        if (videoCapturer != null) {
            videoCapturer.startCapture(width, height, fps);
        }
    }
}

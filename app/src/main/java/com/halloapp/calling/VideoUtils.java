package com.halloapp.calling;

import android.app.Activity;

import androidx.annotation.Nullable;

import com.halloapp.util.logs.Log;

import org.webrtc.Camera2Enumerator;
import org.webrtc.VideoCapturer;

public class VideoUtils {

    public static VideoCapturer createVideoCapturer(Activity activity) {
        return createVideoCapturer(new Camera2Enumerator(activity));
    }

    @Nullable
    public static VideoCapturer createVideoCapturer(Camera2Enumerator camera2Enumerator) {
        final String[] deviceNames = camera2Enumerator.getDeviceNames();
        Log.d("VideoUtils: Looking for front facing cameras.");
        for (String deviceName : deviceNames) {
            if (camera2Enumerator.isFrontFacing(deviceName)) {
                Log.d("VideoUtils: Creating front facing camera capturer.");
                // TODO(nikola): read more about the eventHandler for createCapturer
                VideoCapturer videoCapturer = camera2Enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        Log.d("VideoUtils: Looking for other cameras.");
        for (String deviceName : deviceNames) {
            if (!camera2Enumerator.isFrontFacing(deviceName)) {
                Log.d("VideoUtils: Creating other camera capturer.");
                VideoCapturer videoCapturer = camera2Enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }
}

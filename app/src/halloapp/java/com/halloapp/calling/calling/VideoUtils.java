package com.halloapp.calling.calling;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.util.logs.Log;

import org.webrtc.Camera2Enumerator;
import org.webrtc.VideoCapturer;

public class VideoUtils {

    public static HAVideoCapturer createVideoCapturer(Activity activity) {
        return createVideoCapturer(activity, true);
    }
    @Nullable
    public static HAVideoCapturer createVideoCapturer(@NonNull Activity activity, boolean frontFacing) {
        Camera2Enumerator camera2Enumerator = new Camera2Enumerator(activity);
        final String[] deviceNames = camera2Enumerator.getDeviceNames();
        Log.d("VideoUtils: Looking for front facing cameras.");
        for (String deviceName : deviceNames) {
            if (camera2Enumerator.isFrontFacing(deviceName) == frontFacing) {
                Log.d("VideoUtils: Creating front facing camera capturer.");
                // TODO(nikola): read more about the eventHandler for createCapturer
                VideoCapturer videoCapturer = camera2Enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return new HAVideoCapturer(videoCapturer, activity, frontFacing);
                }
            }
        }

        Log.d("VideoUtils: Looking for other cameras.");
        for (String deviceName : deviceNames) {
            if (camera2Enumerator.isFrontFacing(deviceName) != frontFacing) {
                Log.d("VideoUtils: Creating other camera capturer.");
                VideoCapturer videoCapturer = camera2Enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return new HAVideoCapturer(videoCapturer, activity, !frontFacing);
                }
            }
        }

        return null;
    }
}

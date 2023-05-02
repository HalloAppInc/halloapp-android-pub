package com.halloapp.util;

import android.app.PictureInPictureParams;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.halloapp.Constants;

public class PictureInPictureUtils {

    @RequiresApi(api = 26)
    public static PictureInPictureParams buildVideoCallParams() {
        PictureInPictureParams.Builder builder = new PictureInPictureParams.Builder();
        builder.setAspectRatio(Constants.PIP_ASPECT_RATIO);
        if (Build.VERSION.SDK_INT >= 31) {
            builder.setAutoEnterEnabled(true);
            builder.setSeamlessResizeEnabled(true);
        }
        return builder.build();
    }
}

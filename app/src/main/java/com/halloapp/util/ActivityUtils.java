package com.halloapp.util;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Build;

import androidx.annotation.NonNull;

public class ActivityUtils {

    public static boolean supportsWideColor(@NonNull Activity activity) {
        if (Build.VERSION.SDK_INT >= 27) {
            return activity.getWindow().isWideColorGamut();
        } else if (Build.VERSION.SDK_INT >= 26) {
            return activity.getWindow().getColorMode() == ActivityInfo.COLOR_MODE_WIDE_COLOR_GAMUT;
        }
        return false;
    }
}

package com.halloapp.util;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.annotation.NonNull;

public class VibrationUtils {

    private static final int QUICK_BUZZ_DURATION_MS = 50;
    private static final int MEDIUM_BUZZ_DURATION_MS = 300;

    public static void quickVibration(@NonNull Context context) {
        vibrate(context, QUICK_BUZZ_DURATION_MS);
    }

    public static void mediumVibration(@NonNull Context context) {
        vibrate(context, MEDIUM_BUZZ_DURATION_MS);
    }

    private static void vibrate(@NonNull Context context, int duration) {
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            v.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            v.vibrate(duration);
        }
    }

}

package com.halloapp.util;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.annotation.NonNull;

public class VibrationUtils {

    private static final int QUICK_BUZZ_DURATION_MS = 50;

    public static void quickVibration(@NonNull Context context) {
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            v.vibrate(VibrationEffect.createOneShot(QUICK_BUZZ_DURATION_MS, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            v.vibrate(QUICK_BUZZ_DURATION_MS);
        }
    }

}

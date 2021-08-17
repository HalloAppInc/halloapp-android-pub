package com.halloapp.util;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

public class GlobalUI {

    private static GlobalUI instance;

    public static GlobalUI getInstance() {
        if (instance == null) {
            synchronized (GlobalUI.class) {
                if (instance == null) {
                    instance = new GlobalUI();
                }
            }
        }
        return instance;
    }

    private final Handler mainHandler;

    private GlobalUI() {
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public void postDelayed(@NonNull Runnable runnable, long delay) {
        mainHandler.postDelayed(runnable, delay);
    }
}

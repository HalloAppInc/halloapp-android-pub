package com.halloapp.ui;

import android.view.View;

import androidx.annotation.NonNull;

public abstract class DebouncedClickListener implements View.OnClickListener {
    private static final int MIN_TIME_BETWEEN_CLICKS_MS = 250;

    private long lastClickTime;

    @Override
    public void onClick(View view) {
        long now = System.currentTimeMillis();
        if (now - lastClickTime > MIN_TIME_BETWEEN_CLICKS_MS) {
            lastClickTime = now;
            onOneClick(view);
        }
    }

    public abstract void onOneClick(@NonNull View view);
}

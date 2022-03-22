package com.halloapp.widget;

import android.text.style.ClickableSpan;
import android.view.View;

import androidx.annotation.NonNull;

public abstract class DebouncedSpan extends ClickableSpan {
    private static final int MIN_TIME_BETWEEN_CLICKS_MS = 250;

    private long lastClickTime;

    @Override
    public void onClick(@NonNull View view) {
        long now = System.currentTimeMillis();
        if (now - lastClickTime > MIN_TIME_BETWEEN_CLICKS_MS) {
            lastClickTime = now;
            onOneClick(view);
        }
    }

    public abstract void onOneClick(@NonNull View view);
}

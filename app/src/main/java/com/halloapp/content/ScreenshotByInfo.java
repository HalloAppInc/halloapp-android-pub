package com.halloapp.content;

import androidx.annotation.NonNull;

import com.halloapp.id.UserId;

public class ScreenshotByInfo {
    public final UserId userId;
    public final long timestamp;

    public ScreenshotByInfo(@NonNull UserId userId, long timestamp) {
        this.userId = userId;
        this.timestamp = timestamp;
    }
}

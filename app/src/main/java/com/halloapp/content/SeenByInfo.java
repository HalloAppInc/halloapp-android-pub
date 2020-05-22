package com.halloapp.content;

import androidx.annotation.NonNull;

import com.halloapp.contacts.UserId;

public class SeenByInfo {

    public final UserId userId;
    public final long timestamp;

    public SeenByInfo(@NonNull UserId userId, long timestamp) {
        this.userId = userId;
        this.timestamp = timestamp;
    }
}

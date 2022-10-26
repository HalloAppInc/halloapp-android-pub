package com.halloapp.content;

import androidx.annotation.NonNull;

import com.halloapp.id.UserId;

public class MessageDeliveryState {
    public final String contentId;
    public final UserId userId;
    public final @Message.State int state;
    public final long timestamp;

    public MessageDeliveryState(@NonNull UserId userId, @NonNull String contentId, @Message.State int state, long timestamp) {
        this.contentId = contentId;
        this.userId = userId;
        this.state = state;
        this.timestamp = timestamp;
    }
}

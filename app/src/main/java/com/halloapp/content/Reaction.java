package com.halloapp.content;

import androidx.annotation.NonNull;

import com.halloapp.id.UserId;
import com.halloapp.proto.clients.ChatContainer;
import com.halloapp.proto.clients.ChatContext;

public class Reaction {

    public String contentId;
    public UserId senderUserId;
    public String reactionType;
    public long timestamp;

    public Reaction(@NonNull String contentId, @NonNull UserId senderUserId, @NonNull String reactionType, long timestamp) {
        this.contentId = contentId;
        this.senderUserId = senderUserId;
        this.reactionType = reactionType;
        this.timestamp = timestamp;
    }

    public UserId getSenderUserId() { return senderUserId;}
    public String getReactionType() {
        return reactionType;
    }
}

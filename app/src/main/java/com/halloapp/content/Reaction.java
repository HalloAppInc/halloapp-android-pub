package com.halloapp.content;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.contacts.Contact;
import com.halloapp.id.UserId;

import java.util.Objects;

public class Reaction {
    public static final String TYPE_KATCHUP_LIKE = "\u2764\uFE0F"; // Red heart

    public String reactionId;
    public String contentId;
    public UserId senderUserId;
    public String reactionType;
    public long timestamp;
    public boolean seen;

    @Nullable
    public Contact senderContact;
    public boolean isFollowingSender;
    public boolean isFollowerSender;

    public Reaction(@NonNull String reactionId, @NonNull String contentId, @NonNull UserId senderUserId, @NonNull String reactionType, long timestamp) {
        this.reactionId = reactionId;
        this.contentId = contentId;
        this.senderUserId = senderUserId;
        this.reactionType = reactionType;
        this.timestamp = timestamp;
    }

    public UserId getSenderUserId() { return senderUserId;}
    public String getReactionType() {
        return reactionType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reaction reaction = (Reaction) o;
        return timestamp == reaction.timestamp && reactionId.equals(reaction.reactionId) && contentId.equals(reaction.contentId) && senderUserId.equals(reaction.senderUserId) && reactionType.equals(reaction.reactionType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reactionId);
    }
}

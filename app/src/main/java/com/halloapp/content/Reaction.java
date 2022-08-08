package com.halloapp.content;

import androidx.annotation.NonNull;

import com.halloapp.id.ChatId;
import com.halloapp.id.UserId;

public class Reaction {

    public ContentItem contentItem;
    public UserId senderUserId;
    public String reactionType;
    public long timestamp;

    public Reaction(@NonNull ContentItem contentItem, UserId senderUserId, String reactionType, long timestamp) {
        this.contentItem = contentItem;
        this.senderUserId = senderUserId;
        this.reactionType = reactionType;
        this.timestamp = timestamp;
    }

    public static Reaction readFromDb(
            String contentId,
            UserId senderUserId,
            String reactionType,
            long timestamp) {

        Message message = ContentDb.getInstance().getMessage(contentId);
        return new Reaction(message, senderUserId, reactionType, timestamp);
    }

    public ContentItem getContentItem() { return contentItem; }
    public long getRowId() { return contentItem.rowId; }
    public UserId getSenderUserId() { return senderUserId;}
    public String getReactionType() {
        return reactionType;
    }
    public long getTimestamp() {
        return timestamp;
    }

}

package com.halloapp.content;

import androidx.annotation.Nullable;

import com.halloapp.id.ChatId;
import com.halloapp.id.UserId;

public class ReactionMessage extends Message {

    private Reaction reaction;

    public ReactionMessage(
            long rowId,
            ChatId chatId,
            UserId senderUserId,
            String messageId,
            long timestamp,
            int usage,
            int state, String text, String replyPostId, int replyPostMediaIndex, String replyMessageId, int replyMessageMediaIndex, UserId replyMessageSenderId, int rerequestCount) {
        super(rowId, chatId, senderUserId, messageId, timestamp, Message.TYPE_REACTION, usage, state, text, replyPostId, replyPostMediaIndex, replyMessageId, replyMessageMediaIndex, replyMessageSenderId, rerequestCount);
    }

    public void setReaction(Reaction reaction) {
        this.reaction = reaction;
    }

    public Reaction getReaction() {
        return reaction;
    }
}

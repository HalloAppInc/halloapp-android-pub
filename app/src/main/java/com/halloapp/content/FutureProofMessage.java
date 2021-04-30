package com.halloapp.content;

import androidx.annotation.Nullable;

import com.halloapp.id.ChatId;
import com.halloapp.id.UserId;

public class FutureProofMessage extends Message {

    private byte[] protoBytes;

    public FutureProofMessage(
            long rowId,
            ChatId chatId,
            UserId senderUserId,
            String messageId,
            long timestamp,
            int usage,
            int state, String text, String replyPostId, int replyPostMediaIndex, String replyMessageId, int replyMessageMediaIndex, UserId replyMessageSenderId, int rerequestCount) {
        super(rowId, chatId, senderUserId, messageId, timestamp, Message.TYPE_FUTURE_PROOF, usage, state, text, replyPostId, replyPostMediaIndex, replyMessageId, replyMessageMediaIndex, replyMessageSenderId, rerequestCount);
    }

    public void setProtoBytes(@Nullable byte[] protoBytes) {
        this.protoBytes = protoBytes;
    }

    public byte[] getProtoBytes() {
        return protoBytes;
    }
}

package com.halloapp.content;

import com.halloapp.id.ChatId;
import com.halloapp.id.UserId;

public class DocumentMessage extends Message {
    public DocumentMessage(long rowId, ChatId chatId, UserId senderUserId, String messageId, long timestamp, int usage, int state, String text, String replyPostId, int replyPostMediaIndex, String replyMessageId, int replyMessageMediaIndex, UserId replyMessageSenderId, int rerequestCount) {
        super(rowId, chatId, senderUserId, messageId, timestamp, Message.TYPE_DOCUMENT, usage, state, text, replyPostId, replyPostMediaIndex, replyMessageId, replyMessageMediaIndex, replyMessageSenderId, rerequestCount);
    }
}

package com.halloapp.content;

import com.halloapp.id.ChatId;
import com.halloapp.id.UserId;

public class ContactMessage extends Message {

    public ContactMessage(long rowId, ChatId chatId, UserId senderUserId, String messageId, long timestamp, int usage, int state, String text, String replyPostId, int replyPostMediaIndex, String replyMessageId, int replyMessageMediaIndex, UserId replyMessageSenderId, int rerequestCount) {
        super(rowId, chatId, senderUserId, messageId, timestamp, Message.TYPE_CONTACT, usage, state, text, replyPostId, replyPostMediaIndex, replyMessageId, replyMessageMediaIndex, replyMessageSenderId, rerequestCount);
    }
}
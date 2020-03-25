package com.halloapp.content;

import androidx.annotation.NonNull;

import com.halloapp.BuildConfig;
import com.halloapp.contacts.UserId;
import com.halloapp.xmpp.Connection;

import java.util.Objects;

public class Message extends ContentItem {

    public final String chatId;

    public Message(
            long rowId,
            String chatId,
            UserId senderUserId,
            String messageId,
            long timestamp,
            boolean transferred,
            @Post.SeenState int seen,
            String text) {
        super(rowId, senderUserId, messageId, timestamp, transferred, seen, text);
        this.chatId = chatId;
    }

    @Override
    public void addToStorage(@NonNull ContentDb contentDb) {
        contentDb.addMessage(this);
    }

    @Override
    public void send(@NonNull Connection connection) {
        connection.sendMessage(this);
    }

    @Override
    public void setMediaTransferred(@NonNull Media media, @NonNull ContentDb contentDb) {
        contentDb.setMediaTransferred(this, media);
    }

    @Override
    public @NonNull String toString() {
        return "Message {timestamp:" + timestamp + " sender:" + senderUserId + ", id:" + id + (BuildConfig.DEBUG ? ", text:" + text : "") + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (getClass() != o.getClass()) {
            return false;
        }
        final Message message = (Message) o;
        return rowId == message.rowId &&
                Objects.equals(senderUserId, message.senderUserId) &&
                Objects.equals(id, message.id) &&
                timestamp == message.timestamp &&
                Objects.equals(text, message.text) &&
                transferred == message.transferred &&
                seen == message.seen;
    }
}

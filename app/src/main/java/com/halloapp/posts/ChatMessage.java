package com.halloapp.posts;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.halloapp.BuildConfig;
import com.halloapp.contacts.UserId;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChatMessage {
    public long rowId; // could be 0 when message not inserted yet
    public final String chatId;
    public final UserId senderUserId;
    public final String messageId;
    public final long timestamp;

    public final boolean transferred;
    public int seen;

    public final String text;
    public final List<Media> media = new ArrayList<>();

    public ChatMessage(
            long rowId,
            String chatId,
            UserId senderUserId,
            String messageId,
            long timestamp,
            boolean transferred,
            @Post.SeenState int seen,
            String text) {
        this.rowId = rowId;
        this.chatId = chatId;
        this.senderUserId = senderUserId;
        this.messageId = messageId;
        this.timestamp = timestamp;
        this.transferred = transferred;
        this.seen = seen;
        this.text = text;
    }

    @Override
    public @NonNull
    String toString() {
        return "Message {timestamp:" + timestamp + " sender:" + senderUserId + ", id:" + messageId + (BuildConfig.DEBUG ? ", text:" + text : "") + "}";
    }

    public boolean isOutgoing() {
        return senderUserId.isMe();
    }

    public boolean isIncoming() {
        return !isOutgoing();
    }

    public boolean isRetracted() {
        return TextUtils.isEmpty(text) && media.isEmpty();
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
        final ChatMessage message = (ChatMessage) o;
        return rowId == message.rowId &&
                Objects.equals(senderUserId, message.senderUserId) &&
                Objects.equals(messageId, message.messageId) &&
                timestamp == message.timestamp &&
                Objects.equals(text, message.text) &&
                transferred == message.transferred &&
                seen == message.seen;
    }

}

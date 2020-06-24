package com.halloapp.content;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import com.halloapp.BuildConfig;
import com.halloapp.contacts.UserId;
import com.halloapp.crypto.EncryptedSessionManager;
import com.halloapp.xmpp.Connection;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

public class Message extends ContentItem {

    public final String chatId;
    public final @State int state;
    public final int rerequestCount;

    public final String replyPostId;
    public final int replyPostMediaIndex;

    @SuppressLint("UniqueConstants")
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({STATE_INITIAL, STATE_INCOMING_RECEIVED, STATE_OUTGOING_SENT, STATE_OUTGOING_DELIVERED, STATE_OUTGOING_SEEN})
    public @interface State {}
    public static final int STATE_INITIAL = 0;
    public static final int STATE_INCOMING_RECEIVED = 1;
    public static final int STATE_OUTGOING_SENT = 1;
    public static final int STATE_OUTGOING_DELIVERED = 2;
    public static final int STATE_OUTGOING_SEEN = 3;

    public Message(
            long rowId,
            String chatId,
            UserId senderUserId,
            String messageId,
            long timestamp,
            @State int state,
            String text,
            String replyPostId,
            int replyPostMediaIndex,
            int rerequestCount) {
        super(rowId, senderUserId, messageId, timestamp, text);
        this.chatId = chatId;
        this.state = state;
        this.replyPostId = TextUtils.isEmpty(replyPostId) ? null : replyPostId;
        this.replyPostMediaIndex = replyPostMediaIndex;
        this.rerequestCount = rerequestCount;
    }

    @Override
    public void addToStorage(@NonNull ContentDb contentDb) {
        contentDb.addMessage(this);
    }

    @Override
    public void send(@NonNull Connection connection) {
        EncryptedSessionManager.getInstance().sendMessage(this);
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
                state == message.state &&
                media.equals(message.media);
    }
}

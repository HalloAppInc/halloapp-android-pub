package com.halloapp.content;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import com.halloapp.BuildConfig;
import com.halloapp.crypto.EncryptedSessionManager;
import com.halloapp.id.ChatId;
import com.halloapp.id.UserId;
import com.halloapp.xmpp.Connection;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

public class Message extends ContentItem {

    public final ChatId chatId;

    public final @Type int type;
    public final @Usage int usage;
    public final @State int state;

    public final int rerequestCount;

    public final String replyPostId;
    public final int replyPostMediaIndex;

    @SuppressLint("UniqueConstants")
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TYPE_CHAT, TYPE_SYSTEM})
    public @interface Type {}
    public static final int TYPE_CHAT = 0;
    public static final int TYPE_SYSTEM = 1;

    @SuppressLint("UniqueConstants")
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({USAGE_CHAT, USAGE_BLOCK, USAGE_UNBLOCK, USAGE_CREATE_GROUP, USAGE_ADD_MEMBERS, USAGE_REMOVE_MEMBER, USAGE_MEMBER_LEFT, USAGE_PROMOTE, USAGE_DEMOTE, USAGE_AUTO_PROMOTE, USAGE_NAME_CHANGE, USAGE_AVATAR_CHANGE, USAGE_GROUP_DELETED})
    public @interface Usage {}
    public static final int USAGE_CHAT = 0;
    public static final int USAGE_BLOCK = 1;
    public static final int USAGE_UNBLOCK = 2;
    public static final int USAGE_CREATE_GROUP = 3;
    public static final int USAGE_ADD_MEMBERS = 4;
    public static final int USAGE_REMOVE_MEMBER = 5;
    public static final int USAGE_MEMBER_LEFT = 6;
    public static final int USAGE_PROMOTE = 7;
    public static final int USAGE_DEMOTE = 8;
    public static final int USAGE_AUTO_PROMOTE = 9;
    public static final int USAGE_NAME_CHANGE = 10;
    public static final int USAGE_AVATAR_CHANGE = 11;
    public static final int USAGE_GROUP_DELETED = 12;

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
            ChatId chatId,
            UserId senderUserId,
            String messageId,
            long timestamp,
            @Type int messageType,
            @Usage int usage,
            @State int state,
            String text,
            String replyPostId,
            int replyPostMediaIndex,
            int rerequestCount) {
        super(rowId, senderUserId, messageId, timestamp, text);
        this.chatId = chatId;
        this.type = messageType;
        this.usage = usage;
        this.state = state;
        this.replyPostId = TextUtils.isEmpty(replyPostId) ? null : replyPostId;
        this.replyPostMediaIndex = replyPostMediaIndex;
        this.rerequestCount = rerequestCount;
    }

    public boolean isLocalMessage() {
        return type == Message.TYPE_SYSTEM;
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
    public void setPatchUrl(long rowId, @NonNull String url, @NonNull ContentDb contentDb) {
        contentDb.setPatchUrl(this, rowId, url);
    }

    @Override
    public String getPatchUrl(long rowId, @NonNull ContentDb contentDb) {
        return contentDb.getPatchUrl(this, rowId);
    }

    @Override
    public @Media.TransferredState int getMediaTransferred(long rowId, @NonNull ContentDb contentDb) {
        return contentDb.getMediaTransferred(this, rowId);
    }

    @Override
    public byte[] getMediaEncKey(long rowId, @NonNull ContentDb contentDb) {
        return contentDb.getMediaEncKey(this, rowId);
    }

    @Override
    public void setUploadProgress(long rowId, long offset, @NonNull ContentDb contentDb) {
        contentDb.setUploadProgress(this, rowId, offset);
    }

    @Override
    public long getUploadProgress(long rowId, @NonNull ContentDb contentDb) {
        return contentDb.getUploadProgress(this, rowId);
    }

    @Override
    public void setRetryCount(long rowId, int count, @NonNull ContentDb contentDb) {
        contentDb.setRetryCount(this, rowId, count);
    }

    @Override
    public int getRetryCount(long rowId, @NonNull ContentDb contentDb) {
        return contentDb.getRetryCount(this, rowId);
    }

    @Override
    public @NonNull String toString() {
        return "Message {timestamp:" + timestamp + " sender:" + senderUserId + ", state: " + state + ", id:" + id + (BuildConfig.DEBUG ? ", text:" + text : "") + "}";
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

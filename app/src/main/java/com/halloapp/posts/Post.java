package com.halloapp.posts;

import android.text.TextUtils;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

public class Post {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({POST_TYPE_UNKNOWN, POST_TYPE_TEXT, POST_TYPE_IMAGE})
    public @interface PostType {}
    public static final int POST_TYPE_UNKNOWN = 0;
    public static final int POST_TYPE_TEXT = 1;
    public static final int POST_TYPE_IMAGE = 2;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({POST_STATE_UNDEFINED, POST_STATE_SENDING, POST_STATE_SENT, POST_STATE_RECEIVING, POST_STATE_RECEIVED})
    public @interface PostState {}
    public static final int POST_STATE_UNDEFINED = 0;
    public static final int POST_STATE_SENDING = 1;
    public static final int POST_STATE_SENT = 2; // after got ack from server
    public static final int POST_STATE_RECEIVING = 3;
    public static final int POST_STATE_RECEIVED = 4; // after sent delivery receipt to the server

    public final long rowId; // could be 0 when post not inserted yet
    public final String chatJid;
    public final String senderJid;
    public final String postId;
    public final String groupId; // same for messages sent in a group
    public final long replyRowId;
    public final long timestamp;

    public @PostState int state;

    public final @PostType int type;
    public final String text;
    public String mediaFile;

    public Post(
            long rowId,
            String chatJid,
            String senderJid,
            String postId,
            String groupId,
            long replyRowId,
            long timestamp,
            @PostState int state,
            @PostType int type,
            String text,
            String mediaFile) {
        this.rowId = rowId;
        this.chatJid = chatJid;
        this.senderJid = senderJid;
        this.postId = postId;
        this.groupId = groupId;
        this.replyRowId = replyRowId;
        this.timestamp = timestamp;
        this.state = state;
        this.type = type;
        this.text = text;
        this.mediaFile = mediaFile;
    }

    public String keyString() {
        return "{" + chatJid + ", " + senderJid + ", " + postId + "}";
    }

    public boolean isOutgoing() {
        return TextUtils.isEmpty(senderJid);
    }

    public boolean isIncoming() {
        return !isOutgoing();
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
        final Post post = (Post) o;
        return rowId == post.rowId &&
                Objects.equals(chatJid, post.chatJid) &&
                Objects.equals(senderJid, post.senderJid) &&
                Objects.equals(postId, post.postId);
    }
}

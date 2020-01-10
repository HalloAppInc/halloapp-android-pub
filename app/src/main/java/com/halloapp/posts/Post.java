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
    @IntDef({POST_STATE_UNDEFINED,
            POST_STATE_OUTGOING_PREPARING, POST_STATE_OUTGOING_UPLOADING, POST_STATE_OUTGOING_SENDING, POST_STATE_OUTGOING_SENT, POST_STATE_OUTGOING_DELIVERED,
            POST_STATE_INCOMING_PREPARING, POST_STATE_INCOMING_DOWNLOADING, POST_STATE_INCOMING_RECEIVED})
    public @interface PostState {}
    public static final int POST_STATE_UNDEFINED = 0;
    // outgoing states:
    public static final int POST_STATE_OUTGOING_PREPARING = 0x10; // initial state for outgoing posts
    public static final int POST_STATE_OUTGOING_UPLOADING = 0x11; // for media messages
    public static final int POST_STATE_OUTGOING_SENDING = 0x12; // after media uploaded
    public static final int POST_STATE_OUTGOING_SENT = 0x13; // after got ack from server
    public static final int POST_STATE_OUTGOING_DELIVERED = 0x14; // after got delivery receipt
    // incoming states:
    public static final int POST_STATE_INCOMING_PREPARING = 0x20;
    public static final int POST_STATE_INCOMING_DOWNLOADING = 0x21;
    public static final int POST_STATE_INCOMING_RECEIVED = 0x22;

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
    public String url;
    public String file;
    public int width;
    public int height;

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
            String url,
            String file,
            int width,
            int height) {
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
        this.url = url;
        this.file = file;
        this.width = width;
        this.height = height;
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
                Objects.equals(postId, post.postId) &&
                Objects.equals(groupId, post.groupId) &&
                replyRowId == post.replyRowId &&
                timestamp == post.timestamp &&
                type == post.type &&
                Objects.equals(text, post.text) &&
                Objects.equals(url, post.url) &&
                Objects.equals(file, post.file) &&
                width == post.width &&
                height == post.height &&
                state == post.state;
    }
}

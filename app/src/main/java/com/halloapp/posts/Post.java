package com.halloapp.posts;

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
    @IntDef({POST_STATUS_UNDEFINED, POST_STATUS_SENDING, POST_STATUS_SENT, POST_STATUS_RECEIVING, POST_STATUS_RECEIVED})
    public @interface PostStatus {}
    public static final int POST_STATUS_UNDEFINED = 0;
    public static final int POST_STATUS_SENDING = 1;
    public static final int POST_STATUS_SENT = 2; // after got ack from server
    public static final int POST_STATUS_RECEIVING = 3;
    public static final int POST_STATUS_RECEIVED = 4; // after sent delivery receipt to the server

    public long rowId; // could be 0 when post not inserted yet
    public String chatJid;
    public String senderJid;
    public String postId;
    public long timestamp;
    public String groupId; // same for messages sent in a group

    public @PostStatus int status;

    public @PostType int type;
    public long quotedRowId;
    public String text;
    public String mediaFile;

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

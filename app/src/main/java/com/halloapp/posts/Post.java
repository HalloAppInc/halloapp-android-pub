package com.halloapp.posts;

import android.text.TextUtils;

import androidx.annotation.IntDef;

import com.halloapp.contacts.UserId;

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

    public final long rowId; // could be 0 when post not inserted yet
    public final String chatId;
    public final UserId senderUserId;
    public final String postId;
    public final String groupId; // same for messages sent in a group
    public final long parentRowId;
    public final long timestamp;

    public boolean transferred;

    public final @PostType int type;
    public final String text;
    public String url;
    public String file;
    public int width;
    public int height;

    public Post(
            long rowId,
            String chatId,
            UserId senderUserId,
            String postId,
            String groupId,
            long parentRowId,
            long timestamp,
            boolean transferred,
            @PostType int type,
            String text,
            String url,
            String file,
            int width,
            int height) {
        this.rowId = rowId;
        this.chatId = chatId;
        this.senderUserId = senderUserId;
        this.postId = postId;
        this.groupId = groupId;
        this.parentRowId = parentRowId;
        this.timestamp = timestamp;
        this.transferred = transferred;
        this.type = type;
        this.text = text;
        this.url = url;
        this.file = file;
        this.width = width;
        this.height = height;
    }

    public String keyString() {
        return "{" + chatId + ", " + senderUserId + ", " + postId + "}";
    }

    public boolean isOutgoing() {
        return senderUserId.isMe();
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
                Objects.equals(chatId, post.chatId) &&
                Objects.equals(senderUserId, post.senderUserId) &&
                Objects.equals(postId, post.postId) &&
                Objects.equals(groupId, post.groupId) &&
                parentRowId == post.parentRowId &&
                timestamp == post.timestamp &&
                type == post.type &&
                Objects.equals(text, post.text) &&
                Objects.equals(url, post.url) &&
                Objects.equals(file, post.file) &&
                width == post.width &&
                height == post.height &&
                transferred == post.transferred;
    }
}

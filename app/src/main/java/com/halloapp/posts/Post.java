package com.halloapp.posts;

import androidx.annotation.NonNull;

import com.halloapp.BuildConfig;
import com.halloapp.contacts.UserId;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Post {

    public long rowId; // could be 0 when post not inserted yet
    public final UserId senderUserId;
    public final String postId;
    public final long timestamp;

    public boolean transferred;
    public boolean seen;

    public final String text;
    public final List<Media> media = new ArrayList<>();

    public int commentCount;
    public int unseenCommentCount;

    public Post(
            long rowId,
            UserId senderUserId,
            String postId,
            long timestamp,
            boolean transferred,
            String text) {
        this.rowId = rowId;
        this.senderUserId = senderUserId;
        this.postId = postId;
        this.timestamp = timestamp;
        this.transferred = transferred;
        this.text = text;
    }

    @Override
    public @NonNull String toString() {
        return "{timestamp:" + timestamp + " sender:" + senderUserId + ", post:" + postId + (BuildConfig.DEBUG ? ", text:" + text : "") + "}";
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
                Objects.equals(senderUserId, post.senderUserId) &&
                Objects.equals(postId, post.postId) &&
                timestamp == post.timestamp &&
                Objects.equals(text, post.text) &&
                transferred == post.transferred &&
                commentCount == post.commentCount &&
                unseenCommentCount == post.unseenCommentCount &&
                seen == post.seen;
    }
}

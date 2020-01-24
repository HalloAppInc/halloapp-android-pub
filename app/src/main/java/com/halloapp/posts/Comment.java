package com.halloapp.posts;

import androidx.annotation.NonNull;

import com.halloapp.BuildConfig;
import com.halloapp.contacts.UserId;

import java.util.Objects;

public class Comment {

    public long rowId;
    public final UserId postSenderUserId;
    public final String postId;
    public final UserId commentSenderUserId;
    public final String commentId;
    public final String parentCommentId;
    public final long timestamp;

    public boolean transferred;
    public boolean seen;

    public final String text;

    public Comment(
            long rowId,
            UserId postSenderUserId,
            String postId,
            UserId commentSenderUserId,
            String commentId,
            String parentCommentId,
            long timestamp,
            boolean transferred,
            String text) {
        this.rowId = rowId;
        this.postSenderUserId = postSenderUserId;
        this.postId = postId;
        this.commentSenderUserId = commentSenderUserId;
        this.commentId = commentId;
        this.parentCommentId = parentCommentId;
        this.timestamp = timestamp;
        this.transferred = transferred;
        this.text = text;
    }

    @Override
    public @NonNull String toString() {
        return "{timestamp:" + timestamp + " postSender:" + postSenderUserId + ", post:" + postId + ", commentSender:" + commentSenderUserId + ", parentCommentId:" + parentCommentId + ", commentId:" + commentId + (BuildConfig.DEBUG ? ", text:" + text : "") + "}";
    }

    public boolean isOutgoing() {
        return commentSenderUserId.isMe();
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
        final Comment comment = (Comment) o;
        return rowId == comment.rowId &&
                Objects.equals(postSenderUserId, comment.postSenderUserId) &&
                Objects.equals(postId, comment.postId) &&
                Objects.equals(commentSenderUserId, comment.commentSenderUserId) &&
                Objects.equals(commentId, comment.commentId) &&
                Objects.equals(parentCommentId, comment.parentCommentId) &&
                timestamp == comment.timestamp &&
                Objects.equals(text, comment.text) &&
                transferred == comment.transferred &&
                seen == comment.seen;
    }

}

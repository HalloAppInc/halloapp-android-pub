package com.halloapp.content;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.BuildConfig;
import com.halloapp.Constants;
import com.halloapp.id.UserId;
import com.halloapp.ui.mentions.TextContent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Comment implements TextContent {

    public long rowId;
    public final String postId;
    public final UserId commentSenderUserId;
    public final String commentId;
    public final String parentCommentId;
    public final long timestamp;

    private Post parentPost;

    public final boolean transferred;
    public boolean seen;

    public final String text;

    public final List<Mention> mentions = new ArrayList<>();

    public Comment(
            long rowId,
            String postId,
            UserId commentSenderUserId,
            String commentId,
            String parentCommentId,
            long timestamp,
            boolean transferred,
            boolean seen,
            String text) {
        this.rowId = rowId;
        this.postId = postId;
        this.commentSenderUserId = commentSenderUserId;
        this.commentId = commentId;
        this.parentCommentId = parentCommentId;
        this.timestamp = timestamp;
        this.transferred = transferred;
        this.seen = commentSenderUserId.isMe() || seen;
        this.text = text;
    }

    @Override
    public @NonNull String toString() {
        return "{timestamp:" + timestamp + ", post:" + postId + ", commentSender:" + commentSenderUserId + ", parentCommentId:" + parentCommentId + ", commentId:" + commentId + (BuildConfig.DEBUG ? ", text:" + text : "") + "}";
    }

    public boolean isOutgoing() {
        return commentSenderUserId.isMe();
    }

    public boolean isIncoming() {
        return !isOutgoing();
    }

    public boolean isRetracted() {
        return TextUtils.isEmpty(text);
    }

    public boolean canBeRetracted() {
        return isOutgoing() && (timestamp + Constants.RETRACT_COMMENT_ALLOWED_TIME > System.currentTimeMillis());
    }

    public void setParentPost(@Nullable Post parentPost) {
        this.parentPost = parentPost;
    }

    @Nullable
    public Post getParentPost() {
        return parentPost;
    }

    @Nullable
    public UserId getPostSenderUserId() {
        if (parentPost == null) {
            return null;
        }
        return parentPost.senderUserId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rowId);
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
                Objects.equals(postId, comment.postId) &&
                Objects.equals(commentSenderUserId, comment.commentSenderUserId) &&
                Objects.equals(commentId, comment.commentId) &&
                Objects.equals(parentCommentId, comment.parentCommentId) &&
                timestamp == comment.timestamp &&
                Objects.equals(text, comment.text) &&
                transferred == comment.transferred &&
                seen == comment.seen;
    }

    @Override
    public List<Mention> getMentions() {
        return mentions;
    }

    @Override
    public String getText() {
        return text;
    }
}

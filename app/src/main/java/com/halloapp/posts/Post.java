package com.halloapp.posts;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import com.halloapp.BuildConfig;
import com.halloapp.contacts.UserId;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Post {

    public long rowId; // could be 0 when post not inserted yet
    public final UserId senderUserId;
    public final String postId;
    public final long timestamp;

    public final boolean transferred;
    public int seen;

    public final String text;
    public final List<Media> media = new ArrayList<>();

    public int commentCount;
    public int unseenCommentCount;
    public int seenByCount;
    public Comment firstComment;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({POST_SEEN_NO, POST_SEEN_YES_PENDING, POST_SEEN_YES})
    public @interface SeenState {}
    public static final int POST_SEEN_NO = 0;
    public static final int POST_SEEN_YES_PENDING = 1;
    public static final int POST_SEEN_YES = 2;

    public Post(
            long rowId,
            UserId senderUserId,
            String postId,
            long timestamp,
            boolean transferred,
            @SeenState int seen,
            String text) {
        this.rowId = rowId;
        this.senderUserId = senderUserId;
        this.postId = postId;
        this.timestamp = timestamp;
        this.transferred = transferred;
        this.seen = seen;
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
                seenByCount == post.seenByCount &&
                seen == post.seen;
    }
}

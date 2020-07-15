package com.halloapp.content;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import com.halloapp.BuildConfig;
import com.halloapp.contacts.UserId;
import com.halloapp.xmpp.Connection;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.Objects;

public class Post extends ContentItem {

    public final @TransferredState int transferred;
    public @SeenState int seen;

    @Override
    public List<Mention> getMentions() {
        return mentions;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SEEN_NO, SEEN_YES_PENDING, SEEN_YES})
    public @interface SeenState {}
    public static final int SEEN_NO = 0;
    public static final int SEEN_YES_PENDING = 1;
    public static final int SEEN_YES = 2;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TRANSFERRED_NO, TRANSFERRED_YES})
    public @interface TransferredState {}
    public static final int TRANSFERRED_NO = 0;
    public static final int TRANSFERRED_YES = 1;

    public int commentCount;
    public int unseenCommentCount;
    public int seenByCount;
    public Comment firstComment;

    public Post(
            long rowId,
            UserId senderUserId,
            String postId,
            long timestamp,
            @TransferredState int transferred,
            @SeenState int seen,
            String text) {
        super(rowId, senderUserId, postId, timestamp, text);
        this.transferred = transferred;
        this.seen = seen;
    }

    @Override
    public void addToStorage(@NonNull ContentDb contentDb) {
        contentDb.addPost(this);
    }

    @Override
    public void send(@NonNull Connection connection) {
        connection.sendPost(this);
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
    public @NonNull String toString() {
        return "Post {timestamp:" + timestamp + " sender:" + senderUserId + ", id:" + id + (BuildConfig.DEBUG ? ", text:" + text : "") + "}";
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
                Objects.equals(id, post.id) &&
                timestamp == post.timestamp &&
                Objects.equals(text, post.text) &&
                transferred == post.transferred &&
                commentCount == post.commentCount &&
                unseenCommentCount == post.unseenCommentCount &&
                seenByCount == post.seenByCount &&
                seen == post.seen &&
                media.equals(post.media);
    }
}

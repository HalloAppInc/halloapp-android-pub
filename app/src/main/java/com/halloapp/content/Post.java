package com.halloapp.content;

import androidx.annotation.NonNull;

import com.halloapp.BuildConfig;
import com.halloapp.contacts.UserId;
import com.halloapp.xmpp.Connection;

import java.util.Objects;

public class Post extends ContentItem {

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
        super(rowId, senderUserId, postId, timestamp, transferred, seen, text);
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
                seen == post.seen;
    }
}

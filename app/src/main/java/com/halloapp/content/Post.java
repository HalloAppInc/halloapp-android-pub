package com.halloapp.content;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.BuildConfig;
import com.halloapp.id.UserId;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.privacy.PrivacyList;

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

    private @PrivacyList.Type String audienceType;
    private List<UserId> audienceList;
    private List<UserId> excludeList;

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

    public void setAudience(@PrivacyList.Type String audienceType, @NonNull List<UserId> users) {
        this.audienceList = users;
        this.audienceType = audienceType;
    }

    public void setExcludeList(@Nullable List<UserId> excludeList) {
        this.excludeList = excludeList;
    }

    @Nullable
    public List<UserId> getExcludeList() {
        return excludeList;
    }

    public @PrivacyList.Type String getAudienceType() {
        return audienceType;
    }

    @Nullable
    public List<UserId> getAudienceList() {
        return audienceList;
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
    public byte[] getMediaEncKey(long rowId, @NonNull ContentDb contentDb) {
        return contentDb.getMediaEncKey(this, rowId);
    }

    @Override
    public void setUploadProgress(long rowId, long offset, @NonNull ContentDb contentDb) {
        contentDb.setUploadProgress(this, rowId, offset);
    }

    @Override
    public long getUploadProgress(long rowId, @NonNull ContentDb contentDb) {
        return contentDb.getUploadProgress(this, rowId);
    }

    @Override
    public void setRetryCount(long rowId, int count, @NonNull ContentDb contentDb) {
        contentDb.setRetryCount(this, rowId, count);
    }

    @Override
    public int getRetryCount(long rowId, @NonNull ContentDb contentDb) {
        return contentDb.getRetryCount(this, rowId);
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

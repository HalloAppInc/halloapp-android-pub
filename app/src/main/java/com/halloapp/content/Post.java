package com.halloapp.content;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.BuildConfig;
import com.halloapp.id.GroupId;
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

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TYPE_USER, TYPE_SYSTEM})
    public @interface Type {}
    public static final int TYPE_USER = 0;
    public static final int TYPE_SYSTEM = 1;

    public int commentCount;
    public int unseenCommentCount;
    public int seenByCount;
    public Comment firstComment;

    private @PrivacyList.Type String audienceType;
    private List<UserId> audienceList;
    private List<UserId> excludeList;

    public @Type int type;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({USAGE_POST, USAGE_CREATE_GROUP, USAGE_ADD_MEMBERS, USAGE_REMOVE_MEMBER, USAGE_MEMBER_LEFT, USAGE_PROMOTE, USAGE_DEMOTE, USAGE_AUTO_PROMOTE, USAGE_NAME_CHANGE, USAGE_AVATAR_CHANGE, USAGE_GROUP_DELETED})
    public @interface Usage {}
    public static final int USAGE_POST = 0;
    public static final int USAGE_CREATE_GROUP = 1;
    public static final int USAGE_ADD_MEMBERS = 2;
    public static final int USAGE_REMOVE_MEMBER = 3;
    public static final int USAGE_MEMBER_LEFT = 4;
    public static final int USAGE_PROMOTE = 5;
    public static final int USAGE_DEMOTE = 6;
    public static final int USAGE_AUTO_PROMOTE = 7;
    public static final int USAGE_NAME_CHANGE = 8;
    public static final int USAGE_AVATAR_CHANGE = 9;
    public static final int USAGE_GROUP_DELETED = 10;

    public @Usage int usage;

    public GroupId parentGroup;

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

    public Post(
            long rowId,
            UserId senderUserId,
            String postId,
            long timestamp,
            @TransferredState int transferred,
            @SeenState int seen,
            @Type int type,
            String text) {
        super(rowId, senderUserId, postId, timestamp, text);
        this.type = type;
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

    @Override
    public boolean shouldSend() {
        return isOutgoing() && type == TYPE_USER && transferred == TRANSFERRED_NO;
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
    public boolean isRetracted() {
        return type != TYPE_SYSTEM && super.isRetracted();
    }

    @Override
    public @NonNull String toString() {
        return "Post {timestamp:" + timestamp + " sender:" + senderUserId + ", id:" + id + (BuildConfig.DEBUG ? ", text:" + text : "") + "}";
    }

    public void setParentGroup(@NonNull GroupId group) {
        this.parentGroup = group;
    }

    @Nullable
    public GroupId getParentGroup() {
        return parentGroup;
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
                media.equals(post.media) &&
                type == post.type;
    }
}

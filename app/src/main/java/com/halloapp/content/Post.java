package com.halloapp.content;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.BuildConfig;
import com.halloapp.Constants;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.privacy.PrivacyList;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.Objects;

public class Post extends ContentItem {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SEEN_NO, SEEN_YES_PENDING, SEEN_YES, SEEN_NO_HIDDEN})
    public @interface SeenState {}
    public static final int SEEN_NO = 0;
    public static final int SEEN_YES_PENDING = 1;
    public static final int SEEN_YES = 2;
    public static final int SEEN_NO_HIDDEN = 3;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TRANSFERRED_NO, TRANSFERRED_YES, TRANSFERRED_DECRYPT_FAILED})
    public @interface TransferredState {}
    public static final int TRANSFERRED_NO = 0;
    public static final int TRANSFERRED_YES = 1;
    public static final int TRANSFERRED_DECRYPT_FAILED = 2;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TYPE_USER, TYPE_SYSTEM, TYPE_FUTURE_PROOF, TYPE_RETRACTED, TYPE_ZERO_ZONE, TYPE_VOICE_NOTE, TYPE_MOMENT, TYPE_MOMENT_ENTRY, TYPE_RETRACTED_MOMENT, TYPE_MOMENT_PSA, TYPE_KATCHUP})
    public @interface Type {}
    public static final int TYPE_USER = 0;
    public static final int TYPE_SYSTEM = 1;
    public static final int TYPE_FUTURE_PROOF = 2;
    public static final int TYPE_RETRACTED = 3;
    public static final int TYPE_ZERO_ZONE = 4;
    public static final int TYPE_VOICE_NOTE = 5;
    public static final int TYPE_MOMENT = 6;
    public static final int TYPE_MOMENT_ENTRY = 7;
    public static final int TYPE_RETRACTED_MOMENT = 8;
    public static final int TYPE_MOMENT_PSA = 9;
    public static final int TYPE_KATCHUP = 10;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({USAGE_POST, USAGE_CREATE_GROUP, USAGE_ADD_MEMBERS, USAGE_REMOVE_MEMBER, USAGE_MEMBER_LEFT, USAGE_PROMOTE, USAGE_DEMOTE, USAGE_AUTO_PROMOTE, USAGE_NAME_CHANGE, USAGE_AVATAR_CHANGE, USAGE_GROUP_DELETED, USAGE_MEMBER_JOINED, USAGE_GROUP_THEME_CHANGED, USAGE_GROUP_DESCRIPTION_CHANGED, USAGE_GROUP_EXPIRY_CHANGED})
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
    public static final int USAGE_MEMBER_JOINED = 11;
    public static final int USAGE_GROUP_THEME_CHANGED = 12;
    public static final int USAGE_GROUP_DESCRIPTION_CHANGED = 13;
    public static final int USAGE_GROUP_EXPIRY_CHANGED = 14;

    public static final int POST_EXPIRATION_NEVER = 0;

    public static Post build(
            long rowId,
            UserId senderUserId,
            String postId,
            long timestamp,
            @TransferredState int transferred,
            @SeenState int seen,
            @Type int type,
            String text) {
        switch (type) {
            case Post.TYPE_VOICE_NOTE:
                return new VoiceNotePost(rowId, senderUserId, postId, timestamp, transferred, seen);
            case Post.TYPE_MOMENT:
            case Post.TYPE_RETRACTED_MOMENT:
                return new MomentPost(rowId, senderUserId, postId, timestamp, transferred, seen, type, text);
            case Post.TYPE_KATCHUP:
                return new KatchupPost(rowId, senderUserId, postId, timestamp, transferred, seen, text);
        }

        return new Post(rowId, senderUserId, postId, timestamp, transferred, seen, type, text);
    }

    public @TransferredState int transferred;
    public @SeenState int seen;
    public @Type int type;
    public @Usage int usage;

    public int commentCount;
    public int unseenCommentCount;
    public int seenByCount;
    public int rerequestCount;
    public Comment firstComment;
    public byte[] protoHash;
    public boolean subscribed;
    public long updateTime;
    public byte[] commentKey;
    public int reactionCount;
    public boolean reactedByMe;

    public long expirationTime;
    public boolean expirationMismatch;

    public String psaTag;

    // stats not read from DB
    public String failureReason;
    public String clientVersion;
    public String senderVersion;
    public String senderPlatform;
    public boolean fromHistory;

    private @PrivacyList.Type String audienceType;
    private List<UserId> audienceList;
    private List<UserId> excludeList;

    public GroupId parentGroup;

    public boolean isArchived = false;
    public long archiveDate;
    public boolean showShareFooter = false;

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
        this.expirationTime = timestamp + Constants.POSTS_EXPIRATION;
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
        this.expirationTime = timestamp + Constants.POSTS_EXPIRATION;
    }

    @Override
    public List<Mention> getMentions() {
        return mentions;
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

    public boolean isUnexpired() {
        return !isExpired();
    }

    public boolean isExpired() {
        return expirationTime != Post.POST_EXPIRATION_NEVER && expirationTime < System.currentTimeMillis();
    }

    @Override
    public boolean shouldSend() {
        return isOutgoing() && type == TYPE_USER && transferred == TRANSFERRED_NO;
    }

    public boolean isTombstone() {
        return transferred == Post.TRANSFERRED_DECRYPT_FAILED;
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
        return contentDb.getMediaEncKey(rowId);
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
        return (type == TYPE_USER && transferred != TRANSFERRED_DECRYPT_FAILED && super.isRetracted()) || type == TYPE_RETRACTED || type == TYPE_RETRACTED_MOMENT;
    }

    @Override
    public @NonNull String toString() {
        return "Post {timestamp:" + timestamp + " sender:" + senderUserId + ", id:" + id + (BuildConfig.DEBUG ? ", text:" + text : "") + "}";
    }

    public void setParentGroup(@NonNull GroupId group) {
        this.parentGroup = group;
    }

    public void setCommentKey(@NonNull byte[] commentKey) {
        this.commentKey = commentKey;
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
                type == post.type &&
                reactionCount == post.reactionCount &&
                reactedByMe == post.reactedByMe;
    }

    public boolean shouldUpdateGroupTimestamp() {
        if (usage == USAGE_POST) {
            return true;
        }
        return false;
    }

    public boolean shouldSendSeenReceipt() {
        switch (seen) {
            case SEEN_NO:
            case SEEN_NO_HIDDEN:
                return isIncoming();
        }
        return false;
    }

    public boolean isSeen() {
        return seen == Post.SEEN_YES || seen == Post.SEEN_YES_PENDING;
    }
}

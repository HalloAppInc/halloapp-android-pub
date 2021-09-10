package com.halloapp.content;

import android.text.TextUtils;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.BuildConfig;
import com.halloapp.Constants;
import com.halloapp.contacts.Contact;
import com.halloapp.id.UserId;
import com.halloapp.xmpp.Connection;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Comment extends ContentItem {

    public final String postId;
    public final String parentCommentId;

    private Post parentPost;

    public final @TransferredState int transferred;
    public boolean seen;
    public int rerequestCount;

    // stats not read from DB
    public String failureReason;
    public String clientVersion;

    @Nullable
    public Contact senderContact;

    public Comment parentComment;

    public final List<Mention> mentions = new ArrayList<>();

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TRANSFERRED_NO, TRANSFERRED_YES, TRANSFERRED_DECRYPT_FAILED})
    public @interface TransferredState {}
    public static final int TRANSFERRED_NO = 0;
    public static final int TRANSFERRED_YES = 1;
    public static final int TRANSFERRED_DECRYPT_FAILED = 2;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TYPE_USER, TYPE_FUTURE_PROOF, TYPE_VOICE_NOTE, TYPE_RETRACTED})
    public @interface Type {}
    public static final int TYPE_USER = 0;
    public static final int TYPE_FUTURE_PROOF = 1;
    public static final int TYPE_VOICE_NOTE = 2;
    public static final int TYPE_RETRACTED = 3;

    public @Type int type;

    public Comment(
            long rowId,
            String postId,
            UserId senderUserId,
            String commentId,
            String parentCommentId,
            long timestamp,
            @TransferredState int transferred,
            boolean seen,
            String text) {
        super(rowId, senderUserId, commentId, timestamp, text);
        this.postId = postId;
        this.parentCommentId = TextUtils.isEmpty(parentCommentId) ? null : parentCommentId;
        this.transferred = transferred;
        this.seen = senderUserId.isMe() || seen;
        this.type = TYPE_USER;
    }

    @Override
    public @NonNull String toString() {
        return "{timestamp:" + timestamp + ", post:" + postId + ", commentSender:" + senderUserId + ", parentCommentId:" + parentCommentId + ", commentId:" + id + (BuildConfig.DEBUG ? ", text:" + text : "") + "}";
    }

    public boolean shouldSend() {
        return transferred == Comment.TRANSFERRED_NO;
    }

    @Override
    public void addToStorage(@NonNull ContentDb contentDb) {
        contentDb.addComment(this);
    }

    @Override
    public void send(@NonNull Connection connection) {
        connection.sendComment(this);
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
    public int getMediaTransferred(long rowId, @NonNull ContentDb contentDb) {
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

    public boolean isOutgoing() {
        return senderUserId.isMe();
    }

    public boolean isIncoming() {
        return !isOutgoing();
    }

    public boolean isRetracted() {
        return (type == TYPE_USER && super.isRetracted()) || type == TYPE_RETRACTED;
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
                Objects.equals(senderUserId, comment.senderUserId) &&
                Objects.equals(id, comment.id) &&
                Objects.equals(parentCommentId, comment.parentCommentId) &&
                timestamp == comment.timestamp &&
                Objects.equals(text, comment.text) &&
                transferred == comment.transferred &&
                seen == comment.seen &&
                media.equals(comment.media);
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

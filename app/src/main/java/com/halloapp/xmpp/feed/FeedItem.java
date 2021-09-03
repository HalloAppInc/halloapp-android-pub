package com.halloapp.xmpp.feed;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.proto.server.SenderStateBundle;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

public class FeedItem {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Type.POST, Type.COMMENT})
    public @interface Type {
        int POST = 0;
        int COMMENT = 1;
    }

    public final @Type int type;
    public final @NonNull String id;
    public final String parentPostId;
    public String parentCommentId;
    public Long timestamp;

    public final @Nullable byte[] payload;
    public final @Nullable byte[] encPayload;

    public final @Nullable List<SenderStateBundle> senderStateBundles;
    public final @Nullable byte[] audienceHash;

    public FeedItem(@Type int type, @NonNull String postId, @Nullable byte[] payload) {
        this(type, postId, payload, null, null, null);
    }

    public FeedItem(@Type int type, @NonNull String postId, @Nullable byte[] payload, @Nullable byte[] encPayload, @Nullable List<SenderStateBundle> senderStateBundles, @Nullable byte[] audienceHash) {
        this.id = postId;
        this.type = type;
        this.payload = payload;
        this.encPayload = encPayload;
        this.parentPostId = null;
        this.parentCommentId = null;
        this.senderStateBundles = senderStateBundles;
        this.audienceHash = audienceHash;
    }

    public FeedItem(@Type int type, @NonNull String commentId, @NonNull String parentPostId, @Nullable byte[] payload, @Nullable byte[] encPayload, @Nullable List<SenderStateBundle> senderStateBundles, @Nullable byte[] audienceHash) {
        this.id = commentId;
        this.type = type;
        this.payload = payload;
        this.encPayload = encPayload;
        this.parentPostId = parentPostId;
        this.parentCommentId = null;
        this.senderStateBundles = senderStateBundles;
        this.audienceHash = audienceHash;
    }

    public FeedItem(@Type int type, @NonNull String id, @NonNull String parentPostId, @Nullable byte[] payload) {
        this(type, id, parentPostId, payload, null);
    }

    public FeedItem(@Type int type, @NonNull String id, @NonNull String parentPostId, @Nullable byte[] payload, @Nullable byte[] encPayload) {
        this.id = id;
        this.type = type;
        this.payload = payload;
        this.encPayload = encPayload;
        this.parentPostId = parentPostId;
        this.senderStateBundles = null;
        this.audienceHash = null;
    }
}

package com.halloapp.xmpp.feed;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.proto.server.SenderStateBundle;
import com.halloapp.xmpp.MediaCounts;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

public class FeedItem {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Type.POST, Type.COMMENT, Type.COMMENT_REACTION})
    public @interface Type {
        int POST = 0;
        int COMMENT = 1;
        int COMMENT_REACTION = 2;
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
    public final MediaCounts mediaCounts;

    public FeedItem(@Type int type, @NonNull String postId, @Nullable byte[] payload, @Nullable MediaCounts mediaCounts) {
        this(type, postId, payload, null, null, null, mediaCounts);
    }

    public FeedItem(@Type int type, @NonNull String postId, @Nullable byte[] payload, @Nullable byte[] encPayload, @Nullable List<SenderStateBundle> senderStateBundles, @Nullable byte[] audienceHash, @Nullable MediaCounts mediaCounts) {
        this.id = postId;
        this.type = type;
        this.payload = payload;
        this.encPayload = encPayload;
        this.parentPostId = null;
        this.parentCommentId = null;
        this.senderStateBundles = senderStateBundles;
        this.audienceHash = audienceHash;
        this.mediaCounts = mediaCounts;
    }

    public FeedItem(@Type int type, @NonNull String commentId, @NonNull String parentPostId, @Nullable byte[] payload, @Nullable byte[] encPayload, @Nullable List<SenderStateBundle> senderStateBundles, @Nullable byte[] audienceHash, @Nullable MediaCounts mediaCounts) {
        this.id = commentId;
        this.type = type;
        this.payload = payload;
        this.encPayload = encPayload;
        this.parentPostId = parentPostId;
        this.parentCommentId = null;
        this.senderStateBundles = senderStateBundles;
        this.audienceHash = audienceHash;
        this.mediaCounts = mediaCounts;
    }

    public FeedItem(@Type int type, @NonNull String id, @NonNull String parentPostId, @Nullable byte[] payload, @Nullable MediaCounts mediaCounts) {
        this(type, id, parentPostId, payload, null, mediaCounts);
    }

    public FeedItem(@Type int type, @NonNull String id, @NonNull String parentPostId, @Nullable byte[] payload, @Nullable byte[] encPayload, @Nullable MediaCounts mediaCounts) {
        this.id = id;
        this.type = type;
        this.payload = payload;
        this.encPayload = encPayload;
        this.parentPostId = parentPostId;
        this.senderStateBundles = null;
        this.audienceHash = null;
        this.mediaCounts = mediaCounts;
    }
}

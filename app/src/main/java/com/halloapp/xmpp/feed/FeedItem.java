package com.halloapp.xmpp.feed;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

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

    public FeedItem(@Type int type, @NonNull String postId, @Nullable byte[] payload) {
        this.id = postId;
        this.type = type;
        this.payload = payload;
        this.parentPostId = null;
        this.parentCommentId = null;
    }

    public FeedItem(@Type int type, @NonNull String id, @NonNull String parentPostId, @Nullable byte[] payload) {
        this.id = id;
        this.type = type;
        this.payload = payload;
        this.parentPostId = parentPostId;
    }
}

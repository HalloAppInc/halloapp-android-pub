package com.halloapp.xmpp.feed;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.protobuf.ByteString;
import com.halloapp.id.UserId;
import com.halloapp.proto.server.Audience;
import com.halloapp.proto.server.Comment;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.Post;
import com.halloapp.xmpp.HalloIq;
import com.halloapp.xmpp.privacy.PrivacyList;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class FeedUpdateIq extends HalloIq {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Action.PUBLISH, Action.RETRACT, Action.SHARE})
    public @interface Action {
        int PUBLISH = 0;
        int RETRACT = 1;
        int SHARE = 2;
    }

    private final @Action int action;
    private final @Nullable FeedItem feedItem;

    private @Nullable @PrivacyList.Type String audienceType;
    private @Nullable List<UserId> audienceList;

    private @NonNull final List<SharePosts> sharePosts = new ArrayList<>();

    public FeedUpdateIq(@Action int action, @NonNull FeedItem feedItem) {
        this.action = action;
        this.feedItem = feedItem;
    }

    public FeedUpdateIq(@NonNull Collection<SharePosts> posts) {
        this.action = Action.SHARE;
        this.feedItem = null;
        sharePosts.addAll(posts);
    }

    public void setPostAudience(@PrivacyList.Type String audienceType, List<UserId> audienceList) {
        this.audienceType = audienceType;
        this.audienceList = audienceList;
    }

    private com.halloapp.proto.server.FeedItem.Action getProtoAction() {
        switch (action) {
            case Action.PUBLISH:
                return com.halloapp.proto.server.FeedItem.Action.PUBLISH;
            case Action.RETRACT:
                return com.halloapp.proto.server.FeedItem.Action.RETRACT;
            case Action.SHARE:
                return com.halloapp.proto.server.FeedItem.Action.SHARE;
        }
        return null;
    }

    @Override
    public Iq.Builder toProtoIq() {
        com.halloapp.proto.server.FeedItem.Builder builder = com.halloapp.proto.server.FeedItem.newBuilder();
        builder.setAction(getProtoAction());

        if (action == Action.SHARE && !sharePosts.isEmpty()) {
            for (SharePosts sharePost : sharePosts) {
                builder.addShareStanzas(sharePost.toProto());
            }
        } else if (feedItem != null && feedItem.type == FeedItem.Type.POST) {
            Post.Builder pb = Post.newBuilder();
            if (audienceType != null && audienceList != null) {
                List<Long> uids = new ArrayList<>();
                for (UserId userId : audienceList) {
                    uids.add(Long.parseLong(userId.rawId()));
                }
                pb.setAudience(Audience.newBuilder().setType(Audience.Type.valueOf(audienceType.toUpperCase(Locale.US))).addAllUids(uids).build());
            }
            pb.setId(feedItem.id);
            if (feedItem.payload != null) {
                pb.setPayload(ByteString.copyFrom(feedItem.payload));
            }
            if (feedItem.mediaCounts != null) {
                pb.setMediaCounters(feedItem.mediaCounts.toProto());
            }
            builder.setPost(pb);
        } else if (feedItem != null && feedItem.type == FeedItem.Type.COMMENT) {
            Comment.Builder cb = Comment.newBuilder();
            cb.setId(feedItem.id);
            cb.setPostId(feedItem.parentPostId);
            if (feedItem.parentCommentId != null) {
                cb.setParentCommentId(feedItem.parentCommentId);
            }
            if (feedItem.payload != null) {
                cb.setPayload(ByteString.copyFrom(feedItem.payload));
            }
            if (feedItem.mediaCounts != null) {
                cb.setMediaCounters(feedItem.mediaCounts.toProto());
            }
            builder.setComment(cb);
        }

        return Iq.newBuilder()
                .setType(Iq.Type.SET)
                .setFeedItem(builder);
    }
}

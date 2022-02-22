package com.halloapp.xmpp.feed;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import com.google.protobuf.ByteString;
import com.halloapp.Constants;
import com.halloapp.id.GroupId;
import com.halloapp.proto.clients.EncryptedPayload;
import com.halloapp.proto.server.Comment;
import com.halloapp.proto.server.GroupFeedItem;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.Post;
import com.halloapp.xmpp.HalloIq;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class GroupFeedUpdateIq extends HalloIq {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Action.PUBLISH, Action.RETRACT})
    public @interface Action {
        int PUBLISH = 0;
        int RETRACT = 1;
    }

    private final @NonNull GroupId groupId;
    private final @Action int action;
    private final @NonNull FeedItem feedItem;

    public GroupFeedUpdateIq(@NonNull GroupId groupId, @Action int action, @NonNull FeedItem feedItem) {
        this.groupId = groupId;
        this.action = action;
        this.feedItem = feedItem;
    }

    private GroupFeedItem.Action getProtoAction() {
        switch (action) {
            case Action.PUBLISH:
                return GroupFeedItem.Action.PUBLISH;
            case Action.RETRACT:
                return GroupFeedItem.Action.RETRACT;
        }
        return null;
    }

    @Override
    public Iq toProtoIq() {
        GroupFeedItem.Builder builder = GroupFeedItem.newBuilder();
        builder.setAction(getProtoAction());
        builder.setGid(groupId.rawId());
        builder.setSenderClientVersion(Constants.USER_AGENT);
        if (feedItem.senderStateBundles != null && feedItem.senderStateBundles.size() > 0) {
            builder.addAllSenderStateBundles(feedItem.senderStateBundles);
        }
        if (feedItem.audienceHash != null) {
            builder.setAudienceHash(ByteString.copyFrom(feedItem.audienceHash));
        }

        if (feedItem.type == FeedItem.Type.POST) {
            Post.Builder pb = Post.newBuilder();
            if (feedItem.payload != null) {
                pb.setPayload(ByteString.copyFrom(feedItem.payload));
            }
            if (feedItem.encPayload != null) {
                EncryptedPayload encryptedPayload = EncryptedPayload.newBuilder()
                        .setSenderStateEncryptedPayload(ByteString.copyFrom(feedItem.encPayload))
                        .build();
                pb.setEncPayload(ByteString.copyFrom(encryptedPayload.toByteArray()));
            }
            if (feedItem.mediaCounts != null) {
                pb.setMediaCounters(feedItem.mediaCounts.toProto());
            }
            pb.setId(feedItem.id);
            builder.setPost(pb);
        } else if (feedItem.type == FeedItem.Type.COMMENT) {
            Comment.Builder cb = Comment.newBuilder();
            if (feedItem.payload != null) {
                cb.setPayload(ByteString.copyFrom(feedItem.payload));
            }
            if (feedItem.encPayload != null) {
                EncryptedPayload encryptedPayload = EncryptedPayload.newBuilder()
                    .setSenderStateEncryptedPayload(ByteString.copyFrom(feedItem.encPayload))
                    .build();
                cb.setEncPayload(ByteString.copyFrom(encryptedPayload.toByteArray()));
            }
            if (feedItem.mediaCounts != null) {
                cb.setMediaCounters(feedItem.mediaCounts.toProto());
            }
            if (feedItem.parentCommentId != null) {
                cb.setParentCommentId(feedItem.parentCommentId);
            }
            cb.setId(feedItem.id);
            cb.setPostId(feedItem.parentPostId);
            builder.setComment(cb);
        }

        return Iq.newBuilder()
                .setId(feedItem.id)
                .setType(Iq.Type.SET)
                .setGroupFeedItem(builder)
                .build();
    }
}

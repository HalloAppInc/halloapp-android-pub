package com.halloapp.xmpp.feed;

import android.util.Base64;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import com.google.protobuf.ByteString;
import com.halloapp.id.GroupId;
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

        if (feedItem.type == FeedItem.Type.POST) {
            Post.Builder pb = Post.newBuilder();
            if (feedItem.payload != null) {
                pb.setPayload(ByteString.copyFrom(Base64.decode(feedItem.payload, Base64.NO_WRAP)));
            }
            pb.setId(feedItem.id);
            builder.setPost(pb);
        } else if (feedItem.type == FeedItem.Type.COMMENT) {
            Comment.Builder cb = Comment.newBuilder();
            if (feedItem.payload != null) {
                cb.setPayload(ByteString.copyFrom(Base64.decode(feedItem.payload, Base64.NO_WRAP)));
            }
            if (feedItem.parentCommentId != null) {
                cb.setParentCommentId(feedItem.parentCommentId);
            }
            if (feedItem.parentPostId != null) {
                cb.setPostId(feedItem.parentPostId);
            }
            cb.setId(feedItem.id);
            builder.setComment(cb);
        }

        return Iq.newBuilder()
                .setId(feedItem.id)
                .setType(Iq.Type.SET)
                .setGroupFeedItem(builder)
                .build();
    }
}

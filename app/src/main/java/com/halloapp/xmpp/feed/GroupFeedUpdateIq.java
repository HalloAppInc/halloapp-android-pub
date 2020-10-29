package com.halloapp.xmpp.feed;

import android.util.Base64;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.protobuf.ByteString;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.proto.server.Comment;
import com.halloapp.proto.server.GroupFeedItem;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.Post;
import com.halloapp.xmpp.HalloIq;
import com.halloapp.xmpp.privacy.PrivacyList;

import org.jivesoftware.smack.packet.IQ;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GroupFeedUpdateIq extends HalloIq {

    public static final String ELEMENT = "group_feed";
    public static final String NAMESPACE = "halloapp:group:feed";

    private static final String ATTRIBUTE_ACTION = "action";
    private static final String ATTRIBUTE_GID = "gid";

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Action.PUBLISH, Action.RETRACT})
    public @interface Action {
        int PUBLISH = 0;
        int RETRACT = 1;
    }

    private @NonNull GroupId groupId;
    private @Action int action;
    private @NonNull FeedItem feedItem;

    public GroupFeedUpdateIq(@NonNull GroupId groupId, @Action int action, @NonNull FeedItem feedItem) {
        super(ELEMENT, NAMESPACE);

        setType(Type.set);

        this.groupId = groupId;
        this.action = action;
        this.feedItem = feedItem;
    }

    private String getActionString() {
        switch (action) {
            case Action.PUBLISH:
                return "publish";
            case Action.RETRACT:
                return "retract";
        }
        return null;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.xmlnsAttribute(NAMESPACE);
        xml.attribute(ATTRIBUTE_ACTION, getActionString());
        xml.attribute(ATTRIBUTE_GID, groupId.rawId());
        xml.rightAngleBracket();
        feedItem.toNode(xml);
        return xml;
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

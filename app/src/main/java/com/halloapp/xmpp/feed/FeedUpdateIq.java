package com.halloapp.xmpp.feed;

import android.util.Base64;

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

import org.jivesoftware.smack.packet.IQ;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class FeedUpdateIq extends HalloIq {

    public static final String ELEMENT = "feed";
    public static final String NAMESPACE = "halloapp:feed";

    private static final String ATTRIBUTE_ACTION = "action";

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Action.PUBLISH, Action.RETRACT, Action.SHARE})
    public @interface Action {
        int PUBLISH = 0;
        int RETRACT = 1;
        int SHARE = 2;
    }

    private @Action int action;
    private @Nullable FeedItem feedItem;

    private @Nullable @PrivacyList.Type String audienceType;
    private @Nullable List<UserId> audienceList;

    private @NonNull final List<SharePosts> sharePosts = new ArrayList<>();

    public FeedUpdateIq(@Action int action, @NonNull FeedItem feedItem) {
        super(ELEMENT, NAMESPACE);

        setType(Type.set);

        this.action = action;
        this.feedItem = feedItem;
    }

    public FeedUpdateIq(@NonNull Collection<SharePosts> posts) {
        super(ELEMENT, NAMESPACE);
        this.action = Action.SHARE;

        setType(Type.set);

        sharePosts.addAll(posts);
    }

    private String getActionString() {
        switch (action) {
            case Action.PUBLISH:
                return "publish";
            case Action.RETRACT:
                return "retract";
            case Action.SHARE:
                return "share";
        }
        return null;
    }

    public void setPostAudience(@PrivacyList.Type String audienceType, List<UserId> audienceList) {
        this.audienceType = audienceType;
        this.audienceList = audienceList;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.xmlnsAttribute(NAMESPACE);
        xml.attribute(ATTRIBUTE_ACTION, getActionString());
        xml.rightAngleBracket();
        if (action == Action.SHARE && !sharePosts.isEmpty()) {
            for (SharePosts sharePost : sharePosts) {
                sharePost.toNode(xml);
            }
        } else {
            feedItem.toNode(xml);
            if (audienceType != null && audienceList != null) {
                xml.halfOpenElement("audience_list");
                xml.attribute("type", audienceType);
                xml.rightAngleBracket();
                for (UserId user : audienceList) {
                    xml.openElement("uid");
                    xml.append(user.rawId());
                    xml.closeElement("uid");
                }
                xml.closeElement("audience_list");
            }
        }
        return xml;
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
    public Iq toProtoIq() {
        com.halloapp.proto.server.FeedItem.Builder builder = com.halloapp.proto.server.FeedItem.newBuilder();
        builder.setAction(getProtoAction());

        if (action == Action.SHARE && !sharePosts.isEmpty()) {
            for (SharePosts sharePost : sharePosts) {
                builder.addShareStanzas(sharePost.toProto());
            }
        } else if (feedItem.type == FeedItem.Type.POST) {
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
                pb.setPayload(ByteString.copyFrom(Base64.decode(feedItem.payload, Base64.NO_WRAP)));
            }
            builder.setPost(pb.build());
        } else if (feedItem.type == FeedItem.Type.COMMENT) {
            Comment.Builder cb = Comment.newBuilder();
            cb.setId(feedItem.id);
            cb.setPostId(feedItem.parentPostId);
            if (feedItem.parentCommentId != null) {
                cb.setParentCommentId(feedItem.parentCommentId);
            }
            if (feedItem.payload != null) {
                cb.setPayload(ByteString.copyFrom(Base64.decode(feedItem.payload, Base64.NO_WRAP)));
            }
            builder.setComment(cb.build());
        }

        return Iq.newBuilder().setType(Iq.Type.SET).setId(getStanzaId()).setFeedItem(builder.build()).build();
    }
}

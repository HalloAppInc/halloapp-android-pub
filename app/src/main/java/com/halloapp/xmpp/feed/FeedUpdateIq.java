package com.halloapp.xmpp.feed;

import android.text.TextUtils;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.protobuf.ByteString;
import com.halloapp.Constants;
import com.halloapp.content.KatchupPost;
import com.halloapp.id.UserId;
import com.halloapp.props.ServerProps;
import com.halloapp.proto.clients.EncryptedPayload;
import com.halloapp.proto.server.Audience;
import com.halloapp.proto.server.Comment;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.MomentInfo;
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

    private Post.Tag tag;
    private UserId unlockMomentUserId;
    private String psaTag;

    private KatchupPost katchupPost;

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

    public void setKatchupPost(@NonNull KatchupPost katchupPost) {
        this.katchupPost = katchupPost;
    }

    public void setPsaTag(@Nullable String tag) {
        this.psaTag = tag;
    }

    public void setPostAudience(@PrivacyList.Type String audienceType, List<UserId> audienceList) {
        this.audienceType = audienceType;
        this.audienceList = audienceList;
    }

    public void setTag(@Nullable Post.Tag tag) {
        this.tag = tag;
    }

    public void setUnlockMomentUserId(@Nullable UserId userId) {
        this.unlockMomentUserId = userId;
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
        builder.setSenderClientVersion(Constants.USER_AGENT);
        builder.setAction(getProtoAction());

        if (action == Action.SHARE && !sharePosts.isEmpty()) {
            for (SharePosts sharePost : sharePosts) {
                builder.addShareStanzas(sharePost.toProto());
            }
        } else if (feedItem != null && feedItem.type == FeedItem.Type.POST) {
            Post.Builder pb = Post.newBuilder();
            if (katchupPost != null) {
                pb.setMomentInfo(MomentInfo.newBuilder()
                        .setNotificationId(katchupPost.notificationId)
                        .setNumTakes(katchupPost.numTakes)
                        .setNumSelfieTakes(katchupPost.numSelfieTakes)
                        .setNotificationTimestamp(katchupPost.notificationTimestamp / 1000)
                        .setTimeTaken(katchupPost.timeTaken)
                        .setContentType(katchupPost.contentType).build());
            }
            if (audienceType != null && audienceList != null) {
                List<Long> uids = new ArrayList<>();
                for (UserId userId : audienceList) {
                    uids.add(Long.parseLong(userId.rawId()));
                }
                pb.setAudience(Audience.newBuilder().setType(Audience.Type.valueOf(audienceType.toUpperCase(Locale.US))).addAllUids(uids).build());
            }
            if (tag != null) {
                pb.setTag(tag);
            }
            if (unlockMomentUserId != null) {
                pb.setMomentUnlockUid(unlockMomentUserId.rawIdLong());
            }
            if (!TextUtils.isEmpty(psaTag)) {
                pb.setPsaTag(psaTag);
            }
            pb.setId(feedItem.id);
            if (feedItem.payload != null && ServerProps.getInstance().getSendPlaintextHomeFeed()) {
                pb.setPayload(ByteString.copyFrom(feedItem.payload));
            }
            if (feedItem.senderStateBundles != null && feedItem.senderStateBundles.size() > 0) {
                builder.addAllSenderStateBundles(feedItem.senderStateBundles);
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
            builder.setPost(pb);
        } else if (feedItem != null && (feedItem.type == FeedItem.Type.COMMENT || feedItem.type == FeedItem.Type.COMMENT_REACTION || feedItem.type == FeedItem.Type.POST_REACTION)) {
            Comment.Builder cb = Comment.newBuilder();
            cb.setId(feedItem.id);
            cb.setPostId(feedItem.parentPostId);
            cb.setCommentType(feedItem.type == FeedItem.Type.COMMENT_REACTION ? Comment.CommentType.COMMENT_REACTION : feedItem.type == FeedItem.Type.POST_REACTION ? Comment.CommentType.POST_REACTION : Comment.CommentType.COMMENT);
            if (feedItem.parentCommentId != null) {
                cb.setParentCommentId(feedItem.parentCommentId);
            }
            if (feedItem.payload != null && ServerProps.getInstance().getSendPlaintextHomeFeed()) {
                cb.setPayload(ByteString.copyFrom(feedItem.payload));
            }
            if (feedItem.encPayload != null) {
                EncryptedPayload encryptedPayload = EncryptedPayload.newBuilder()
                        .setCommentKeyEncryptedPayload(ByteString.copyFrom(feedItem.encPayload))
                        .build();
                cb.setEncPayload(ByteString.copyFrom(encryptedPayload.toByteArray()));
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

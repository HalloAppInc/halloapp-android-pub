package com.halloapp.xmpp.groups;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.halloapp.Constants;
import com.halloapp.Me;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Post;
import com.halloapp.crypto.keys.PublicEdECKey;
import com.halloapp.crypto.signal.SignalSessionManager;
import com.halloapp.crypto.signal.SignalSessionSetupInfo;
import com.halloapp.groups.GroupInfo;
import com.halloapp.groups.MemberInfo;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.proto.clients.Background;
import com.halloapp.proto.clients.CommentIdContext;
import com.halloapp.proto.clients.Container;
import com.halloapp.proto.clients.ContentDetails;
import com.halloapp.proto.clients.GroupHistoryPayload;
import com.halloapp.proto.clients.MemberDetails;
import com.halloapp.proto.clients.PostIdContext;
import com.halloapp.proto.server.ExpiryInfo;
import com.halloapp.proto.server.GroupFeedHistory;
import com.halloapp.proto.server.GroupFeedItem;
import com.halloapp.proto.server.GroupFeedItems;
import com.halloapp.proto.server.GroupInviteLink;
import com.halloapp.proto.server.GroupMember;
import com.halloapp.proto.server.GroupStanza;
import com.halloapp.proto.server.HistoryResend;
import com.halloapp.proto.server.IdentityKey;
import com.halloapp.proto.server.Iq;
import com.halloapp.util.RandomId;
import com.halloapp.util.StringUtils;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.HalloIq;
import com.halloapp.xmpp.feed.FeedContentEncoder;
import com.halloapp.xmpp.util.IqResult;
import com.halloapp.xmpp.util.Observable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupsApi {

    private static GroupsApi instance;

    private final Me me;
    private final ContentDb contentDb;
    private final Connection connection;

    public static GroupsApi getInstance() {
        if (instance == null) {
            synchronized (GroupsApi.class) {
                if (instance == null) {
                    instance = new GroupsApi(Me.getInstance(), ContentDb.getInstance(), Connection.getInstance());
                }
            }
        }
        return instance;
    }

    private GroupsApi(Me me, ContentDb contentDb, Connection connection) {
        this.me = me;
        this.contentDb = contentDb;
        this.connection = connection;
    }

    public Observable<GroupInfo> createFeedGroup(@NonNull String name, @NonNull List<UserId> uids) {
        return createFeedGroup(name, uids, ExpiryInfo.newBuilder()
                .setExpiresInSeconds(Constants.DEFAULT_GROUP_EXPIRATION_TIME)
                .setExpiryType(ExpiryInfo.ExpiryType.EXPIRES_IN_SECONDS)
                .build());
    }

    public Observable<GroupInfo> createFeedGroup(@NonNull String name, @NonNull List<UserId> uids, @NonNull ExpiryInfo expiryInfo) {
        final CreateGroupIq requestIq = new CreateGroupIq(name, uids, expiryInfo);
        final Observable<GroupResponseIq> observable = connection.sendRequestIq(requestIq);
        return observable.map(response -> {
            List<MemberInfo> memberInfos = new ArrayList<>();
            for (MemberElement memberElement : response.memberElements) {
                memberInfos.add(new MemberInfo(-1, memberElement.uid, memberElement.type, memberElement.name));
            }
            return new GroupInfo(response.groupType, response.groupId, response.name, response.description, response.avatar, response.background, memberInfos, response.expiryInfo);
        });
    }

    public Observable<GroupInfo> createGroupChat(@NonNull String name, @NonNull List<UserId> uids) {
        final CreateGroupIq requestIq = new CreateGroupIq(name, uids);
        final Observable<GroupResponseIq> observable = connection.sendRequestIq(requestIq);
        return observable.map(response -> {
            List<MemberInfo> memberInfos = new ArrayList<>();
            for (MemberElement memberElement : response.memberElements) {
                memberInfos.add(new MemberInfo(-1, memberElement.uid, memberElement.type, memberElement.name));
            }
            return new GroupInfo(response.groupType, response.groupId, response.name, response.description, response.avatar, response.background, memberInfos, response.expiryInfo);
        });
    }

    public Observable<Boolean> addRemoveMembers(@NonNull GroupId groupId, @Nullable List<UserId> addUids, @Nullable List<UserId> removeUids, @Nullable HistoryResend historyResend) {
        final AddRemoveMembersIq requestIq = new AddRemoveMembersIq(groupId, addUids, removeUids, historyResend);
        final Observable<GroupResponseIq> observable = connection.sendRequestIq(requestIq);
        return observable.map(response -> {
            boolean success = true;
            List<MemberInfo> addedUsers = new ArrayList<>();
            for (MemberElement memberElement : response.memberElements) {
                if (!MemberElement.Result.OK.equals(memberElement.result)) {
                    if ("already_not_member".equals(memberElement.reason)) {
                        contentDb.addRemoveGroupMembers(groupId, null, null, new ArrayList<>(), Collections.singletonList(new MemberInfo(-1, memberElement.uid, memberElement.type, memberElement.name)), null);
                    } else {
                        success = false;
                    }
                } else if (MemberElement.Action.ADD.equals(memberElement.action)) {
                    addedUsers.add(new MemberInfo(-1, memberElement.uid, memberElement.type, memberElement.name));
                }
            }

            if (!addedUsers.isEmpty()) {
                contentDb.addRemoveGroupMembers(groupId, null, null, addedUsers, new ArrayList<>(), null);
            }

            return success;
        });
    }

    public Observable<Boolean> promoteDemoteAdmins(@NonNull GroupId groupId, @Nullable List<UserId> promoteUids, @Nullable List<UserId> demoteUids) {
        final PromoteDemoteAdminsIq requestIq = new PromoteDemoteAdminsIq(groupId, promoteUids, demoteUids);
        final Observable<GroupResponseIq> observable = connection.sendRequestIq(requestIq);
        return observable.map(response -> {
            for (MemberElement memberElement : response.memberElements) {
                if (!MemberElement.Result.OK.equals(memberElement.result)) {
                    return false;
                }
            }

            return true;
        });
    }

    public Observable<GroupInfo> getGroupInfo(@NonNull GroupId groupId) {
        final GetGroupInfoIq requestIq = new GetGroupInfoIq(groupId);
        final Observable<GroupResponseIq> observable = connection.sendRequestIq(requestIq);
        return observable.map(response -> {
            List<MemberInfo> memberInfos = new ArrayList<>();
            for (MemberElement memberElement : response.memberElements) {
                memberInfos.add(new MemberInfo(-1, memberElement.uid, memberElement.type, memberElement.name));
            }
            return new GroupInfo(response.groupType, response.groupId, response.name, response.description, response.avatar, response.background, memberInfos, response.expiryInfo);
        });
    }

    public Observable<String> getGroupInviteLink(@NonNull GroupId groupId) {
        final GetGroupInviteLinkIq requestIq = new GetGroupInviteLinkIq(groupId);

        return connection.sendIqRequest(requestIq).map(response -> {
            GroupInviteLink groupInviteLink = response.getGroupInviteLink();
            if (groupInviteLink == null) {
                contentDb.setGroupLink(groupId, null);
                return null;
            }
            contentDb.setGroupLink(groupId, groupInviteLink.getLink());
            return groupInviteLink.getLink();
        });
    }

    public Observable<Boolean> resetGroupInviteLink(@NonNull GroupId groupId) {
        final ResetGroupInviteLinkIq requestIq = new ResetGroupInviteLinkIq(groupId);

        return connection.sendIqRequest(requestIq).map(response -> {
            GroupInviteLink groupInviteLink = response.getGroupInviteLink();
            if (groupInviteLink == null) {
                return null;
            }
            boolean success = GroupInviteLink.Action.RESET.equals(groupInviteLink.getAction());
            if (success) {
                contentDb.setGroupLink(groupId, null);
            }
            return success;
        });
    }

    public Observable<GroupInfo> previewGroupInviteLink(@NonNull String code) {
        final PreviewGroupInviteLinkIq requestIq = new PreviewGroupInviteLinkIq(code);

        return connection.sendIqRequest(requestIq).map(response -> {
            GroupInviteLink groupInviteLink = response.getGroupInviteLink();
            if (groupInviteLink == null) {
                return null;
            }
            GroupStanza groupStanza = groupInviteLink.getGroup();
            List<GroupMember> groupMembers = groupStanza.getMembersList();
            List<MemberInfo> membersList = new ArrayList<>();
            for (GroupMember g : groupMembers) {
                membersList.add(MemberInfo.fromGroupMember(g));
            }
            Background b = null;
            try {
                b = Background.parseFrom(groupStanza.getBackgroundBytes());
            } catch (InvalidProtocolBufferException e) {
                Log.w("Failed to parse background", e);
            }
            return new GroupInfo(groupStanza.getGroupType(), GroupId.fromNullable(groupInviteLink.getGid()), groupStanza.getName(), null, groupStanza.getAvatarId(), b, membersList, groupStanza.hasExpiryInfo() ? groupStanza.getExpiryInfo() : groupStanza.getExpiryInfo());
        });
    }

    public Observable<Boolean> setGroupExpiry(@NonNull GroupId groupId, @NonNull ExpiryInfo expiryInfo) {
        GroupStanza setExpiryStanza = GroupStanza.newBuilder()
                .setGid(groupId.rawId())
                .setAction(GroupStanza.Action.CHANGE_EXPIRY)
                .setExpiryInfo(expiryInfo).build();
        return connection.sendIqRequest(Iq.newBuilder()
                .setType(Iq.Type.SET)
                .setGroupStanza(setExpiryStanza)).map(response -> response.getGroupStanza() != null);
    }

    public Observable<Boolean> setGroupBackground(@NonNull GroupId groupId, int theme) {
        final SetGroupBackgroundIq requestIq = new SetGroupBackgroundIq(theme, groupId);

        return connection.sendIqRequest(requestIq).map(response -> response.getGroupStanza() != null);
    }

    public Observable<Boolean> setGroupDescription(@NonNull GroupId groupId, @NonNull String description) {
        final SetGroupDescriptionIq requestIq = new SetGroupDescriptionIq(description, groupId);

        return connection.sendIqRequest(requestIq).map(response -> response.getGroupStanza() != null);
    }

    public Observable<IqResult<GroupInviteLink>> joinGroupViaInviteLink(@NonNull String code) {
        final JoinGroupInviteLinkIq requestIq = new JoinGroupInviteLinkIq(code);

        return connection.sendIqRequest(requestIq).map(response -> {
            GroupInviteLink groupInviteLink = response.getGroupInviteLink();
            if (groupInviteLink == null) {
                return new IqResult<>();
            }
            return new IqResult<>(groupInviteLink);
        });
    }

    public Observable<List<GroupInfo>> getGroupsList() {
        final GetGroupsListIq requestIq = new GetGroupsListIq();
        final Observable<GroupsListResponseIq> observable = connection.sendRequestIq(requestIq);
        return observable.map(response -> {
            return response.groupInfos;
        });
    }

    public Observable<String> setGroupName(@NonNull GroupId groupId, @NonNull String name) {
        final SetGroupInfoIq requestIq = new SetGroupInfoIq(groupId, name, null);
        final Observable<GroupResponseIq> observable = connection.sendRequestIq(requestIq);
        return observable.map(response -> {
            return response.name;
        });
    }

    public Observable<Boolean> leaveGroup(@NonNull GroupId groupId) {
        final LeaveGroupIq requestIq = new LeaveGroupIq(groupId);
        final Observable<HalloIq> observable = connection.sendRequestIq(requestIq);
        return observable.map(response -> {
            return true;
        });
    }

    public Observable<Map<UserId, PublicEdECKey>> getGroupKeys(@NonNull GroupId groupId) {
        final GetGroupKeysIq requestIq = new GetGroupKeysIq(groupId);
        final Observable<GroupResponseIq> observable = connection.sendRequestIq(requestIq);
        return observable.map(response -> {
            Map<UserId, PublicEdECKey> ret = new HashMap<>();
            for (MemberElement member : response.memberElements) {
                byte[] identityKeyBytes = null;
                try {
                    IdentityKey identityKeyProto = IdentityKey.parseFrom(member.identityKey);
                    identityKeyBytes = identityKeyProto.getPublicKey().toByteArray();
                } catch (InvalidProtocolBufferException e) {
                    Log.e("Got invalid identity key proto", e);
                }
                ret.put(member.uid, new PublicEdECKey(identityKeyBytes));
            }
            return ret;
        });
    }

    public void handleGroupHistoryPayload(@NonNull GroupHistoryPayload groupHistoryPayload, @NonNull GroupId groupId) {
        long myUid = Long.parseLong(me.getUser());
        for (MemberDetails memberDetails : groupHistoryPayload.getMemberDetailsList()) {
            if (myUid == memberDetails.getUid()) {
                Log.i("This history resend includes me as recipient; creating tombstones");
                addTombstones(groupHistoryPayload.getContentDetailsList(), groupId);
                return;
            }
        }

        GroupFeedItems.Builder groupFeedItems = GroupFeedItems.newBuilder();
        groupFeedItems.setGid(groupId.rawId());
        for (ContentDetails contentDetails : groupHistoryPayload.getContentDetailsList()) {
            ContentDb contentDb = ContentDb.getInstance();
            byte[] remoteHash = contentDetails.getContentHash().toByteArray();
            if (contentDetails.hasPostIdContext()) {
                PostIdContext postIdContext = contentDetails.getPostIdContext();
                String id = postIdContext.getFeedPostId();
                Post post = contentDb.getPost(id);
                if (post != null && post.senderUserId.isMe()) {
                    byte[] localHash = contentDb.getPostProtoHash(id);
                    if (!Arrays.equals(remoteHash, localHash)) {
                        Log.w("Skipping sharing post " + id + " because hashes do not match");
                    } else {
                        final byte[] payload;
                        final GroupFeedItem.Action action;
                        if (post.isRetracted()) {
                            payload = Container.newBuilder().build().toByteArray();
                            action = GroupFeedItem.Action.RETRACT;
                        } else {
                            Container.Builder container = Container.newBuilder();
                            FeedContentEncoder.encodePost(container, post);
                            payload = container.build().toByteArray();
                            action = GroupFeedItem.Action.SHARE;
                        }

                        com.halloapp.proto.server.Post.Builder postBuilder = com.halloapp.proto.server.Post.newBuilder();
                        postBuilder.setId(id);
                        postBuilder.setPublisherUid(myUid);
                        postBuilder.setPayload(ByteString.copyFrom(payload));
                        postBuilder.setTimestamp(post.timestamp / 1000);

                        groupFeedItems.addItems(GroupFeedItem.newBuilder().setAction(action).setPost(postBuilder).setExpiryTimestamp(post.expirationTime / 1000).setSenderClientVersion(Constants.USER_AGENT));
                    }
                }
            } else if (contentDetails.hasCommentIdContext()) {
                CommentIdContext commentIdContext = contentDetails.getCommentIdContext();
                String id = commentIdContext.getCommentId();
                Comment comment = contentDb.getComment(id);
                if (comment != null && comment.senderUserId.isMe()) {
                    byte[] localHash = contentDb.getCommentProtoHash(id);
                    if (!Arrays.equals(remoteHash, localHash)) {
                        Log.w("Skipping sharing comment " + id + " because hashes do not match (" + StringUtils.bytesToHexString(remoteHash) + " and " + StringUtils.bytesToHexString(localHash) + ")");
                    } else {
                        final byte[] payload;
                        final GroupFeedItem.Action action;
                        if (comment.isRetracted()) {
                            payload = Container.newBuilder().build().toByteArray();
                            action = GroupFeedItem.Action.RETRACT;
                        } else {
                            Container.Builder container = Container.newBuilder();
                            FeedContentEncoder.encodeComment(container, comment);
                            payload = container.build().toByteArray();
                            action = GroupFeedItem.Action.SHARE;
                        }

                        com.halloapp.proto.server.Comment.Builder commentBuilder = com.halloapp.proto.server.Comment.newBuilder();
                        commentBuilder.setId(id);
                        commentBuilder.setPostId(comment.postId);
                        commentBuilder.setPublisherUid(myUid);
                        commentBuilder.setPayload(ByteString.copyFrom(payload));
                        commentBuilder.setTimestamp(comment.timestamp / 1000);

                        groupFeedItems.addItems(GroupFeedItem.newBuilder().setAction(action).setComment(commentBuilder).setSenderClientVersion(Constants.USER_AGENT));
                    }
                }
            } else {
                Log.e("History resend content details have neither post nor comment");
            }
        }

        if (groupFeedItems.getItemsCount() > 0) {
            String id = RandomId.create();
            byte[] groupFeedItemsPayload = groupFeedItems.build().toByteArray();
            contentDb.setHistoryResendPayload(groupId, id, groupFeedItemsPayload);

            for (MemberDetails memberDetails : groupHistoryPayload.getMemberDetailsList()) {
                // TODO: Verify that identity key matches one provided
                UserId peerUserId = new UserId(Long.toString(memberDetails.getUid()));
                sendGroupHistoryResend(groupId, peerUserId, id, groupFeedItemsPayload);
            }
        }
    }

    private void addTombstones(List<ContentDetails> contentDetailsList, GroupId groupId) {
        for (ContentDetails contentDetails : contentDetailsList) {
            if (contentDetails.hasPostIdContext()) {
                PostIdContext postIdContext = contentDetails.getPostIdContext();
                UserId senderUserId = new UserId(Long.toString(postIdContext.getSenderUid()));
                Post post = new Post(0,
                        senderUserId,
                        postIdContext.getFeedPostId(),
                        postIdContext.getTimestamp() * 1000L,
                        Post.TRANSFERRED_DECRYPT_FAILED,
                        Post.SEEN_NO,
                        "");
                post.parentGroup = groupId;
                contentDb.addPost(post);
            } else if (contentDetails.hasCommentIdContext()) {
                CommentIdContext commentIdContext = contentDetails.getCommentIdContext();
                UserId senderUserId = new UserId(Long.toString(commentIdContext.getSenderUid()));
                String parentCommentId = "".equals(commentIdContext.getParentCommentId()) ? null : commentIdContext.getParentCommentId();
                Comment comment = new Comment(0,
                        commentIdContext.getFeedPostId(),
                        senderUserId,
                        commentIdContext.getCommentId(),
                        parentCommentId,
                        commentIdContext.getTimestamp() * 1000L,
                        Comment.TRANSFERRED_DECRYPT_FAILED,
                        false,
                        "");
                contentDb.addComment(comment);
            }
        }
    }

    public void sendGroupHistoryResend(GroupId groupId, UserId peerUserId, String id, byte[] groupFeedItemsPayload) {
        GroupFeedHistory.Builder builder = GroupFeedHistory.newBuilder();
        builder.setSenderClientVersion(Constants.USER_AGENT);
        builder.setId(id);
        builder.setGid(groupId.rawId());

        try {
            SignalSessionSetupInfo signalSessionSetupInfo = SignalSessionManager.getInstance().getSessionSetupInfo(peerUserId);
            byte[] encryptedPayload = SignalSessionManager.getInstance().encryptMessage(groupFeedItemsPayload, peerUserId);
            builder.setEncPayload(ByteString.copyFrom(encryptedPayload));
            if (signalSessionSetupInfo != null) {
                builder.setPublicKey(ByteString.copyFrom(signalSessionSetupInfo.identityKey.getKeyMaterial()));
                if (signalSessionSetupInfo.oneTimePreKeyId != null) {
                    builder.setOneTimePreKeyId(signalSessionSetupInfo.oneTimePreKeyId);
                }
            }

            connection.sendGroupHistory(builder.build(), peerUserId);
        } catch (Exception e) {
            Log.e("Failed to encrypt history resend to " + peerUserId, e);
        }
    }
}

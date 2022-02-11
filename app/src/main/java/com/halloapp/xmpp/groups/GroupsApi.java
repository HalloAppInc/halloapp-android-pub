package com.halloapp.xmpp.groups;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
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
import com.halloapp.proto.server.GroupFeedHistory;
import com.halloapp.proto.server.GroupFeedItem;
import com.halloapp.proto.server.GroupFeedItems;
import com.halloapp.proto.server.GroupInviteLink;
import com.halloapp.proto.server.GroupMember;
import com.halloapp.proto.server.GroupStanza;
import com.halloapp.proto.server.HistoryResend;
import com.halloapp.proto.server.IdentityKey;
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

    public Observable<GroupInfo> createGroup(@NonNull String name, @NonNull List<UserId> uids) {
        final CreateGroupIq requestIq = new CreateGroupIq(name, uids);
        final Observable<GroupResponseIq> observable = connection.sendRequestIq(requestIq);
        return observable.map(response -> {
            List<MemberInfo> memberInfos = new ArrayList<>();
            for (MemberElement memberElement : response.memberElements) {
                memberInfos.add(new MemberInfo(-1, memberElement.uid, memberElement.type, memberElement.name));
            }
            return new GroupInfo(response.groupId, response.name, response.description, response.avatar, response.background, memberInfos);
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
            return new GroupInfo(response.groupId, response.name, response.description, response.avatar, response.background, memberInfos);
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
            return new GroupInfo(GroupId.fromNullable(groupInviteLink.getGid()), groupStanza.getName(), null, groupStanza.getAvatarId(), b, membersList);
        });
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
                Log.i("This history resend includes me as recipient; dropping");
                return;
            }
        }

        GroupFeedItems.Builder groupFeedItems = GroupFeedItems.newBuilder();
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
                        Container.Builder container = Container.newBuilder();
                        FeedContentEncoder.encodePost(container, post);
                        byte[] payload = container.build().toByteArray();

                        com.halloapp.proto.server.Post.Builder postBuilder = com.halloapp.proto.server.Post.newBuilder();
                        postBuilder.setId(id);
                        postBuilder.setPublisherUid(myUid);
                        postBuilder.setPayload(ByteString.copyFrom(payload));
                        postBuilder.setTimestamp(post.timestamp);

                        groupFeedItems.addItems(GroupFeedItem.newBuilder().setPost(postBuilder));
                    }
                }
            } else if (contentDetails.hasCommentIdContext()) {
                CommentIdContext commentIdContext = contentDetails.getCommentIdContext();
                String id = commentIdContext.getCommentId();
                Comment comment = contentDb.getComment(id);
                if (comment != null) {
                    byte[] localHash = contentDb.getCommentProtoHash(id);
                    if (!Arrays.equals(remoteHash, localHash)) {
                        Log.w("Skipping sharing comment " + id + " because hashes do not match (" + StringUtils.bytesToHexString(remoteHash) + " and " + StringUtils.bytesToHexString(localHash) + ")");
                    } else {
                        Container.Builder container = Container.newBuilder();
                        FeedContentEncoder.encodeComment(container, comment);
                        byte[] payload = container.build().toByteArray();

                        com.halloapp.proto.server.Comment.Builder commentBuilder = com.halloapp.proto.server.Comment.newBuilder();
                        commentBuilder.setId(id);
                        commentBuilder.setPostId(comment.postId);
                        commentBuilder.setPublisherUid(myUid);
                        commentBuilder.setPayload(ByteString.copyFrom(payload));
                        commentBuilder.setTimestamp(comment.timestamp);

                        groupFeedItems.addItems(GroupFeedItem.newBuilder().setComment(commentBuilder));
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
                // TODO(jack): Verify that identity key matches one provided
                UserId peerUserId = new UserId(Long.toString(memberDetails.getUid()));
                sendGroupHistoryResend(groupId, peerUserId, id, groupFeedItemsPayload);
            }
        }
    }

    public void sendGroupHistoryResend(GroupId groupId, UserId peerUserId, String id, byte[] groupFeedItemsPayload) {
        GroupFeedHistory.Builder builder = GroupFeedHistory.newBuilder();
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

            connection.sendGroupHistory(builder.build(), id, peerUserId);
        } catch (Exception e) {
            Log.e("Failed to encrypt history resend to " + peerUserId, e);
        }
    }
}

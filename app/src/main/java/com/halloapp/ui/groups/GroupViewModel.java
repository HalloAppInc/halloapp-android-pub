package com.halloapp.ui.groups;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.halloapp.Constants;
import com.halloapp.Me;
import com.halloapp.Preferences;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Group;
import com.halloapp.crypto.CryptoException;
import com.halloapp.crypto.group.GroupFeedSessionManager;
import com.halloapp.crypto.group.GroupSetupInfo;
import com.halloapp.groups.MemberInfo;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.nux.ZeroZoneManager;
import com.halloapp.props.ServerProps;
import com.halloapp.proto.clients.GroupHistoryPayload;
import com.halloapp.proto.clients.MemberDetails;
import com.halloapp.proto.server.HistoryResend;
import com.halloapp.proto.server.IdentityKey;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.DelayedProgressLiveData;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.groups.GroupsApi;
import com.halloapp.xmpp.groups.MemberElement;
import com.halloapp.xmpp.util.ObservableErrorException;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GroupViewModel extends AndroidViewModel {

    private final Me me;
    private final ContentDb contentDb;
    private final GroupsApi groupsApi;
    private final ContactsDb contactsDb;
    private final Preferences preferences;

    private final GroupId groupId;

    private final ComputableLiveData<Group> groupLiveData;
    private final ComputableLiveData<List<GroupMember>> membersLiveData;

    private final MutableLiveData<String> groupInviteLink = new MutableLiveData<>();
    private final MutableLiveData<Boolean> userIsAdmin = new MutableLiveData<>();
    private final MutableLiveData<Boolean> chatIsActive = new MutableLiveData<>();

    private final ContentDb.Observer contentObserver = new ContentDb.DefaultObserver() {

        @Override
        public void onGroupMetadataChanged(@NonNull GroupId groupId) {
            if (groupId.equals(GroupViewModel.this.groupId)) {
                invalidateChat();
            }
        }

        @Override
        public void onGroupMembersChanged(@NonNull GroupId groupId) {
            if (groupId.equals(GroupViewModel.this.groupId)) {
                invalidateMembers();
            }
        }

        @Override
        public void onGroupAdminsChanged(@NonNull GroupId groupId) {
            if (groupId.equals(GroupViewModel.this.groupId)) {
                invalidateMembers();
            }
        }

        @Override
        public void onGroupBackgroundChanged(@NonNull GroupId groupId) {
            if (groupId.equals(GroupViewModel.this.groupId)) {
                invalidateChat();
            }
        }

        @Override
        public void onChatDeleted(@NonNull ChatId chatId) {
            // TODO(jack): handle chat deletion
        }

        private void invalidateChat() {
            groupLiveData.invalidate();
        }

        private void invalidateMembers() {
            membersLiveData.invalidate();
        }
    };

    public GroupViewModel(@NonNull Application application, @NonNull GroupId groupId) {
        super(application);

        this.groupId = groupId;

        me = Me.getInstance();
        contentDb = ContentDb.getInstance();
        contentDb.addObserver(contentObserver);
        groupsApi = GroupsApi.getInstance();
        contactsDb = ContactsDb.getInstance();
        preferences = Preferences.getInstance();

        groupLiveData = new ComputableLiveData<Group>() {
            @Override
            protected Group compute() {
                Group group = contentDb.getGroup(groupId);
                chatIsActive.postValue(group != null && group.isActive);

                if (group != null
                        && preferences.getZeroZoneState() >= ZeroZoneManager.ZeroZoneState.NEEDS_INITIALIZATION
                        && groupId.equals(preferences.getZeroZoneGroupId())) {
                    groupInviteLink.postValue(Constants.GROUP_INVITE_BASE_URL + group.inviteToken);
                }
                return group;
            }
        };
        groupLiveData.invalidate();

        membersLiveData = new ComputableLiveData<List<GroupMember>>() {
            @Override
            protected List<GroupMember> compute() {
                List<GroupMember> groupMembers = new ArrayList<>();
                List<MemberInfo> members = contentDb.getGroupMembers(groupId);
                for (MemberInfo member : members) {
                    if (member.userId.isMe() || member.userId.rawId().equals(me.getUser())) {
                        userIsAdmin.postValue(MemberElement.Type.ADMIN.equals(member.type));
                        break;
                    }
                }
                for (MemberInfo memberInfo : members) {
                    groupMembers.add(new GroupMember(memberInfo, contactsDb.getContact(memberInfo.userId)));
                }
                Collections.sort(groupMembers, (m1, m2) -> {
                    if (m1.memberInfo.userId.isMe()) {
                        return -1;
                    } else if (m2.memberInfo.userId.isMe()) {
                        return 1;
                    } else if (m1.memberInfo.isAdmin() && !m2.memberInfo.isAdmin()) {
                        return -1;
                    } else if (m2.memberInfo.isAdmin() && !m1.memberInfo.isAdmin()) {
                        return 1;
                    }
                    if (m1.contact.addressBookName != null && m2.contact.addressBookName == null) {
                        return -1;
                    } else if (m2.contact.addressBookName != null && m1.contact.addressBookName == null) {
                        return 1;
                    }
                    return m1.contact.getDisplayName().compareTo(m2.contact.getDisplayName());
                });
                return groupMembers;
            }
        };
        membersLiveData.invalidate();
    }

    public String getGroupInviteLink() {
        return groupInviteLink.getValue();
    }

    public LiveData<Group> getGroup() {
        return groupLiveData.getLiveData();
    }

    public LiveData<List<GroupMember>> getMembers() {
        return membersLiveData.getLiveData();
    }

    public LiveData<Boolean> getUserIsAdmin() {
        return userIsAdmin;
    }

    public LiveData<Boolean> getChatIsActive() {
        return chatIsActive;
    }

    public LiveData<Boolean> addMembers(List<UserId> userIds) {
        MutableLiveData<Boolean> result = new DelayedProgressLiveData<>();
        BgWorkers.getInstance().execute(() -> {
            HistoryResend historyResend = null;
            GroupHistoryPayload.Builder groupHistoryPayload = GroupHistoryPayload.newBuilder();
            try {
                if (Constants.HISTORY_RESEND_ENABLED) {
                    for (UserId userId : userIds) {
                        try {
                            long uid = Long.parseLong(userId.rawId());
                            byte[] encodedIk = Connection.getInstance().downloadKeys(userId).await().identityKey;
                            IdentityKey identityKeyProto = IdentityKey.parseFrom(encodedIk);
                            byte[] ik = identityKeyProto.getPublicKey().toByteArray();
                            groupHistoryPayload.addMemberDetails(MemberDetails.newBuilder().setUid(uid).setPublicIdentityKey(ByteString.copyFrom(ik)));
                        } catch (ObservableErrorException | InterruptedException e) {
                            Log.e("Failed to get identity key for " + userId + "; skipping", e);
                        } catch (InvalidProtocolBufferException e) {
                            Log.e("Received invalid identity key proto for " + userId + "; skipping", e);
                        }
                    }
                    groupHistoryPayload.addAllContentDetails(contentDb.getHistoryResendContent(groupId));
                    byte[] payload = groupHistoryPayload.build().toByteArray();
                    String id = RandomId.create();

                    contentDb.setHistoryResendPayload(groupId, id, payload);
                    // TODO(jack): Clean up stale payloads in daily worker after some time period

                    GroupSetupInfo groupSetupInfo = GroupFeedSessionManager.getInstance().ensureGroupSetUp(groupId);
                    byte[] encPayload = GroupFeedSessionManager.getInstance().encryptMessage(payload, groupId);
                    HistoryResend.Builder builder = HistoryResend.newBuilder()
                            .setGid(groupId.rawId())
                            .setId(id)
                            .setEncPayload(ByteString.copyFrom(encPayload));
                    if (ServerProps.getInstance().getSendPlaintextGroupFeed()) {
                        builder.setPayload(ByteString.copyFrom(payload)); // TODO(jack): Remove once plaintext sending is off
                    }
                    if (groupSetupInfo.senderStateBundles != null) {
                        builder.addAllSenderStateBundles(groupSetupInfo.senderStateBundles);
                    }
                    if (groupSetupInfo.audienceHash != null) {
                        builder.setAudienceHash(ByteString.copyFrom(groupSetupInfo.audienceHash));
                    }
                    historyResend = builder.build();
                }
            } catch (CryptoException | NoSuchAlgorithmException e) {
                Log.e("Failed to encrypt details for history resend", e);
            }
            groupsApi.addRemoveMembers(groupId, userIds, null, historyResend)
                    .onResponse(response -> {
                        result.postValue(response);

                        if (Constants.HISTORY_RESEND_ENABLED) {
                            groupsApi.handleGroupHistoryPayload(groupHistoryPayload.build(), groupId);
                        }
                    })
                    .onError(error -> {
                        Log.e("Add members failed", error);
                        result.postValue(false);
                    });
        });
        return result;
    }

    public LiveData<Boolean> removeMember(UserId userId) {
        MutableLiveData<Boolean> result = new DelayedProgressLiveData<>();
        groupsApi.addRemoveMembers(groupId, null, Collections.singletonList(userId), null)
                .onResponse(result::postValue)
                .onError(error -> {
                    Log.e("Remove member failed", error);
                    result.postValue(false);
                });
        return result;
    }

    public LiveData<Boolean> promoteAdmin(UserId userId) {
        MutableLiveData<Boolean> result = new DelayedProgressLiveData<>();
        groupsApi.promoteDemoteAdmins(groupId, Collections.singletonList(userId), null)
                .onResponse(result::postValue)
                .onError(error -> {
                    Log.e("Promote admin failed", error);
                    result.postValue(false);
                });
        return result;
    }

    public LiveData<Boolean> demoteAdmin(UserId userId) {
        MutableLiveData<Boolean> result = new DelayedProgressLiveData<>();
        groupsApi.promoteDemoteAdmins(groupId, null, Collections.singletonList(userId))
                .onResponse(result::postValue)
                .onError(error -> {
                    Log.e("Demote admin failed", error);
                    result.postValue(false);
                });
        return result;
    }

    public LiveData<Boolean> leaveGroup() {
        MutableLiveData<Boolean> result = new DelayedProgressLiveData<>();
        groupsApi.leaveGroup(groupId)
                .onResponse(result::postValue)
                .onError(error -> {
                    Log.e("Leave group failed", error);
                    result.postValue(false);
                });
        return result;
    }

    @Override
    protected void onCleared() {
        contentDb.removeObserver(contentObserver);
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final Application application;
        private final GroupId groupId;

        Factory(@NonNull Application application, @NonNull GroupId groupId) {
            this.application = application;
            this.groupId = groupId;
        }

        @Override
        public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(GroupViewModel.class)) {
                //noinspection unchecked
                return (T) new GroupViewModel(application, groupId);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }

    public static class GroupMember {
        public final Contact contact;
        public final MemberInfo memberInfo;

        GroupMember(@NonNull MemberInfo memberInfo, @NonNull Contact contact) {
            this.contact = contact;
            this.memberInfo = memberInfo;
        }
    }
}

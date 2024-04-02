package com.halloapp.ui.groups;

import android.annotation.SuppressLint;
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
import com.halloapp.content.Chat;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Group;
import com.halloapp.crypto.CryptoException;
import com.halloapp.crypto.group.GroupFeedSessionManager;
import com.halloapp.crypto.group.GroupSetupInfo;
import com.halloapp.crypto.keys.EncryptedKeyStore;
import com.halloapp.crypto.signal.SignalSessionManager;
import com.halloapp.crypto.signal.SignalSessionSetupInfo;
import com.halloapp.groups.MemberInfo;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.nux.ZeroZoneManager;
import com.halloapp.props.ServerProps;
import com.halloapp.proto.clients.EncryptedPayload;
import com.halloapp.proto.clients.GroupHistoryPayload;
import com.halloapp.proto.clients.MemberDetails;
import com.halloapp.proto.clients.SenderKey;
import com.halloapp.proto.clients.SenderState;
import com.halloapp.proto.server.ExpiryInfo;
import com.halloapp.proto.server.HistoryResend;
import com.halloapp.proto.server.IdentityKey;
import com.halloapp.proto.server.SenderStateBundle;
import com.halloapp.proto.server.SenderStateWithKeyInfo;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.DelayedProgressLiveData;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;
import com.halloapp.util.stats.GroupHistoryDecryptStats;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.groups.GroupsApi;
import com.halloapp.xmpp.groups.MemberElement;
import com.halloapp.xmpp.util.ObservableErrorException;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GroupChatInfoViewModel extends BaseGroupInfoViewModel {

    private final Me me;
    private final ContentDb contentDb;
    private final GroupsApi groupsApi;
    private final ContactsDb contactsDb;
    private final Preferences preferences;

    private final GroupId groupId;

    private final ComputableLiveData<Group> groupLiveData;
    private final ComputableLiveData<List<GroupMember>> membersLiveData;
    private final ComputableLiveData<GroupHistoryDecryptStats> historyStats;

    private final MutableLiveData<String> groupInviteLink = new MutableLiveData<>();
    private final MutableLiveData<Boolean> userIsAdmin = new MutableLiveData<>();
    private final MutableLiveData<Boolean> chatIsActive = new MutableLiveData<>();

    private final ContentDb.Observer contentObserver = new ContentDb.DefaultObserver() {

        @Override
        public void onGroupMetadataChanged(@NonNull GroupId groupId) {
            if (groupId.equals(GroupChatInfoViewModel.this.groupId)) {
                invalidateChat();
            }
        }

        @Override
        public void onGroupMembersChanged(@NonNull GroupId groupId) {
            if (groupId.equals(GroupChatInfoViewModel.this.groupId)) {
                invalidateMembers();
            }
        }

        @Override
        public void onGroupAdminsChanged(@NonNull GroupId groupId) {
            if (groupId.equals(GroupChatInfoViewModel.this.groupId)) {
                invalidateMembers();
            }
        }

        @Override
        public void onGroupBackgroundChanged(@NonNull GroupId groupId) {
            if (groupId.equals(GroupChatInfoViewModel.this.groupId)) {
                invalidateChat();
            }
        }

        @Override
        public void onChatDeleted(@NonNull ChatId chatId) {
            // TODO: handle chat deletion
        }

        private void invalidateChat() {
            groupLiveData.invalidate();
        }

        private void invalidateMembers() {
            membersLiveData.invalidate();
        }
    };

    public GroupChatInfoViewModel(@NonNull Application application, @NonNull GroupId groupId) {
        super(application, groupId);

        this.groupId = groupId;

        me = Me.getInstance();
        contentDb = ContentDb.getInstance();
        contentDb.addObserver(contentObserver);
        groupsApi = GroupsApi.getInstance();
        contactsDb = ContactsDb.getInstance();
        preferences = Preferences.getInstance();

        groupLiveData = new ComputableLiveData<Group>() {
            @SuppressLint("RestrictedApi")
            @Override
            protected Group compute() {
                Group group = contentDb.getGroupFeedOrChat(groupId);
                chatIsActive.postValue(group != null && group.isActive);

                if (group != null) {
                    groupInviteLink.postValue(Constants.GROUP_INVITE_BASE_URL + group.inviteToken);
                }
                return group;
            }
        };
        groupLiveData.invalidate();

        membersLiveData = new ComputableLiveData<List<GroupMember>>() {
            @SuppressLint("RestrictedApi")
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

        historyStats = new ComputableLiveData<GroupHistoryDecryptStats>() {
            @SuppressLint("RestrictedApi")
            @Override
            protected GroupHistoryDecryptStats compute() {
                return contentDb.getGroupHistoryDecryptStats(groupId);
            }
        };
        historyStats.invalidate();
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

    public LiveData<GroupHistoryDecryptStats> getHistoryStats() {
        return historyStats.getLiveData();
    }

    @Override
    public LiveData<Boolean> getUserIsAdmin() {
        return userIsAdmin;
    }

    @Override
    public LiveData<Boolean> getChatIsActive() {
        return chatIsActive;
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
            if (modelClass.isAssignableFrom(GroupChatInfoViewModel.class)) {
                //noinspection unchecked
                return (T) new GroupChatInfoViewModel(application, groupId);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}

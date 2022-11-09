package com.halloapp.ui.groups;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.Me;
import com.halloapp.Preferences;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.ContentDb;
import com.halloapp.groups.GroupInfo;
import com.halloapp.groups.MemberInfo;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.proto.clients.Background;
import com.halloapp.proto.server.GroupInviteLink;
import com.halloapp.proto.server.GroupMember;
import com.halloapp.proto.server.GroupStanza;
import com.halloapp.registration.CheckRegistration;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.xmpp.IqErrorException;
import com.halloapp.xmpp.groups.GroupsApi;
import com.halloapp.xmpp.groups.MemberElement;
import com.halloapp.xmpp.util.IqResult;

import java.util.ArrayList;
import java.util.List;


public class ViewGroupInviteLinkViewModel extends AndroidViewModel{

    private final Me me = Me.getInstance();
    private final ContentDb contentDb = ContentDb.getInstance();
    private final GroupsApi groupsApi = GroupsApi.getInstance();
    private final ContactsDb contactsDb = ContactsDb.getInstance();
    private final Preferences preferences = Preferences.getInstance();

    private final String linkCode;

    final ComputableLiveData<CheckRegistration.CheckResult> registrationStatus;
    private final MutableLiveData<InviteLinkResult> inviteLinkPreview;

    public static class InviteLinkResult {
        public final GroupInfo groupInfo;
        public final List<Contact> contactList;

        private InviteLinkResult(@Nullable GroupInfo groupInfo, @Nullable List<Contact> contacts) {
            this.groupInfo = groupInfo;
            this.contactList = contacts;
        }
    }

    public ViewGroupInviteLinkViewModel(@NonNull Application application, @NonNull String linkCode) {
        super(application);

        this.linkCode = linkCode;

        inviteLinkPreview = new MutableLiveData<>();

        registrationStatus = new ComputableLiveData<CheckRegistration.CheckResult>() {
            @Override
            protected CheckRegistration.CheckResult compute() {
                return CheckRegistration.checkRegistration(me, preferences);
            }
        };

        fetchInvitePreview();
    }

    public LiveData<InviteLinkResult> getInvitePreview() {
        return inviteLinkPreview;
    }

    public LiveData<IqResult<GroupInviteLink>> joinGroup() {
        MutableLiveData<IqResult<GroupInviteLink>> requestLiveData = new MutableLiveData<>();
        groupsApi.joinGroupViaInviteLink(linkCode).onResponse(result -> {
            if (result != null && result.isSuccess()) {
                GroupStanza groupStanza = result.getResult().getGroup();
                List<MemberInfo> members = new ArrayList<>();
                String meUser = me.getUser();
                for (GroupMember groupMember : groupStanza.getMembersList()) {
                    String rawUserId = Long.toString(groupMember.getUid());
                    UserId userId = meUser.equals(rawUserId) ? UserId.ME : new UserId(rawUserId);
                    members.add(new MemberInfo(-1, userId, groupMember.getType().equals(GroupMember.Type.ADMIN) ? MemberElement.Type.ADMIN : MemberElement.Type.MEMBER, groupMember.getName()));
                }
                contentDb.addFeedGroup(new GroupInfo(GroupStanza.GroupType.FEED, new GroupId(groupStanza.getGid()), groupStanza.getName(), null, groupStanza.getAvatarId(), Background.getDefaultInstance(), members, groupStanza.getExpiryInfo()), () -> {
                    requestLiveData.postValue(result);
                });
            } else {
                requestLiveData.postValue(result);
            }
        }).onError(e -> {
            if (e instanceof IqErrorException) {
                requestLiveData.postValue(new IqResult<>(((IqErrorException) e).getReason()));
            } else {
                requestLiveData.postValue(new IqResult<>());
            }
        });
        return requestLiveData;
    }

    private void fetchInvitePreview() {
        groupsApi.previewGroupInviteLink(linkCode).onResponse(info -> {
            List<Contact> contacts = null;
            if (info != null && info.members != null) {
                contacts = new ArrayList<>();
                for (MemberInfo member : info.members) {
                    Contact contact = contactsDb.getContact(member.userId);
                    contact.halloName = member.name;
                    contacts.add(contact);
                }
                Contact.sort(contacts);
            }
            inviteLinkPreview.postValue(new InviteLinkResult(info, contacts));
        }).onError(e -> {
            inviteLinkPreview.postValue(new InviteLinkResult(null, null));
        });
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final Application application;
        private final String linkCode;

        public Factory(@NonNull Application application, @NonNull String linkCode) {
            this.application = application;
            this.linkCode = linkCode;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(ViewGroupInviteLinkViewModel.class)) {
                //noinspection unchecked
                return (T) new ViewGroupInviteLinkViewModel(application, linkCode);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}

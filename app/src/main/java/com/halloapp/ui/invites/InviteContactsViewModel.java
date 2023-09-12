package com.halloapp.ui.invites;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.provider.Telephony;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.halloapp.Me;
import com.halloapp.Preferences;
import com.halloapp.contacts.AddressBookContacts;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.id.UserId;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.invites.InvitesApi;
import com.halloapp.xmpp.invites.InvitesResponseIq;

import java.text.Collator;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Set;

public class InviteContactsViewModel extends AndroidViewModel {

    private final BgWorkers bgWorkers;
    private final Connection connection;
    private final ContactsDb contactsDb;
    private final Preferences preferences;

    private final InvitesApi invitesApi;

    private final Me me = Me.getInstance();

    final MutableLiveData<InviteCountAndRefreshTime> inviteAndTimeData;

    final ComputableLiveData<List<Contact>> contactList;

    public static final int RESPONSE_RETRYABLE = -1;
    public static final long RESPONSE_RETRYABLE_LONG = -1;

    private final ContactsDb.Observer contactsObserver = new ContactsDb.BaseObserver() {

        @Override
        public void onContactsChanged() {
            refreshContacts();
        }
    };

    public InviteContactsViewModel(@NonNull Application application) {
        super(application);
        bgWorkers = BgWorkers.getInstance();
        connection = Connection.getInstance();
        contactsDb = ContactsDb.getInstance();
        preferences = Preferences.getInstance();


        invitesApi = new InvitesApi(connection);

        inviteAndTimeData = new MutableLiveData<>();

        contactList = new ComputableLiveData<List<Contact>>() {

            @SuppressLint("RestrictedApi")
            @Override
            protected List<Contact> compute() {
                List<Contact> contacts = Contact.sort(ContactsDb.getInstance().getUniqueContactsWithPhones());

                ListIterator<Contact> iterator = contacts.listIterator();
                String myUserId = me.getUser();
                while(iterator.hasNext()){
                    UserId userId = iterator.next().userId;
                    if (userId != null && userId.rawId().equals(myUserId)) {
                        iterator.remove();
                   }
                }
                Collator collator = Collator.getInstance(Locale.getDefault());
                Collections.sort(contacts, (o1, o2) -> {
                    if (o1.userId != null || o2.userId != null) {
                        if (o1.userId == null) {
                            return -1;
                        } else if (o2.userId == null) {
                            return 1;
                        }
                    } else if (o1.numPotentialFriends > 1 && o2.numPotentialFriends > 1) {
                        if (o1.numPotentialFriends != o2.numPotentialFriends) {
                            return (int) o2.numPotentialFriends - (int) o1.numPotentialFriends;
                        }
                    } else if (o1.numPotentialFriends > 1) {
                        return -1;
                    } else if (o2.numPotentialFriends > 1) {
                        return 1;
                    }
                    boolean alpha1 = Character.isAlphabetic(o1.getDisplayName().codePointAt(0));
                    boolean alpha2 = Character.isAlphabetic(o2.getDisplayName().codePointAt(0));
                    if (alpha1 == alpha2) {
                        return collator.compare(o1.getDisplayName(), o2.getDisplayName());
                    } else {
                        return alpha1 ? -1 : 1;
                    }
                });
                return contacts;
            }
        };

        contactsDb.addObserver(contactsObserver);
        fetchInviteAndTimeRefreshData();
    }


    private void fetchInviteAndTimeRefreshData() {
        bgWorkers.execute(() -> {
            invitesApi.getInviteAndTimeRefresh().onResponse(response -> {
                if (response == null) {
                    InviteCountAndRefreshTime retryable = new InviteCountAndRefreshTime();
                    retryable.setInviteRemaining(RESPONSE_RETRYABLE);
                    retryable.setTimeTillRefresh(RESPONSE_RETRYABLE_LONG);
                    inviteAndTimeData.postValue(retryable);
                } else {
                    preferences.setInvitesRemaining(response.getInvitesRemaining());
                    inviteAndTimeData.postValue(response);
                }
            }).onError(e -> {
                InviteCountAndRefreshTime retryable = new InviteCountAndRefreshTime();
                retryable.setInviteRemaining(RESPONSE_RETRYABLE);
                retryable.setTimeTillRefresh(RESPONSE_RETRYABLE_LONG);
                inviteAndTimeData.postValue(retryable);
            });
        });
    }

    public void refreshContacts() {
        contactList.invalidate();
    }

    public LiveData<InviteCountAndRefreshTime> getInviteCountAndRefreshTime() {
        return inviteAndTimeData;
    }

    public LiveData<List<Contact>> getContactList() {
        return contactList.getLiveData();
    }

    public LiveData<Integer> sendInvite(@NonNull Contact contact) {
        MutableLiveData<Integer> inviteResult = new MutableLiveData<>();
        bgWorkers.execute(() -> {
            invitesApi.sendInvite(Preconditions.checkNotNull(contact.normalizedPhone)).onResponse(result -> {
                inviteResult.postValue(result);
                if (result != null && InvitesResponseIq.Result.SUCCESS == result) {
                    contactsDb.markInvited(contact);
                    fetchInviteAndTimeRefreshData();
                    refreshContacts();
                }
            }).onError(e -> {
                inviteResult.postValue(InvitesResponseIq.Result.UNKNOWN);
                Log.e("inviteFriendsViewModel/sendInvite failed to send invite", e);
            });
        });
        return inviteResult;
    }

    @Override
    protected void onCleared() {
        contactsDb.removeObserver(contactsObserver);
    }
}

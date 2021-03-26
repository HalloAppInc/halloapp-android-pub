package com.halloapp.ui.invites;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.provider.Telephony;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.halloapp.Preferences;
import com.halloapp.contacts.AddressBookContacts;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
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
import java.util.Locale;
import java.util.Set;

public class InviteContactsViewModel extends AndroidViewModel {

    private BgWorkers bgWorkers;
    private Connection connection;
    private ContactsDb contactsDb;
    private Preferences preferences;

    private final InvitesApi invitesApi;

    MutableLiveData<Integer> inviteCountData;
    ComputableLiveData<List<Contact>> contactList;
    ComputableLiveData<Set<String>> waContacts;
    ComputableLiveData<InviteOptions> inviteOptions;

    public static final int RESPONSE_RETRYABLE = -1;

    private final ContactsDb.Observer contactsObserver = new ContactsDb.BaseObserver() {

        @Override
        public void onContactsChanged() {
            refreshContacts();
        }
    };

    public static class InviteOptions {
        public boolean hasWA;
        public String defaultSms;
    }

    public InviteContactsViewModel(@NonNull Application application) {
        super(application);
        connection = Connection.getInstance();
        contactsDb = ContactsDb.getInstance();
        preferences = Preferences.getInstance();


        invitesApi = new InvitesApi(connection);

        inviteCountData = new MutableLiveData<>();

        contactList = new ComputableLiveData<List<Contact>>() {

            @Override
            protected List<Contact> compute() {
                List<Contact> contacts = Contact.sort(ContactsDb.getInstance().getUniqueContactsWithPhones());
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

        inviteOptions = new ComputableLiveData<InviteOptions>() {
            @Override
            protected InviteOptions compute() {
                return loadInviteOptions();
            }
        };

        waContacts = new ComputableLiveData<Set<String>>() {
            @Override
            protected Set<String> compute() {
                return AddressBookContacts.fetchWANumbers(application);
            }
        };

        contactsDb.addObserver(contactsObserver);
        fetchInvites();
    }

    private InviteOptions loadInviteOptions() {
        String defaultSms = Telephony.Sms.getDefaultSmsPackage(getApplication());
        boolean hasWa = false;

        PackageManager packageManager = getApplication().getPackageManager();
        for (PackageInfo packageInfo : packageManager.getInstalledPackages(0)) {
            if (packageInfo.packageName.equals("com.whatsapp")) {
                hasWa = true;
                break;
            }
        }
        InviteOptions inviteOptions = new InviteOptions();
        inviteOptions.hasWA = hasWa;
        inviteOptions.defaultSms = defaultSms;

        return inviteOptions;
    }

    private void fetchInvites() {
        invitesApi.getAvailableInviteCount().onResponse(response -> {
            if (response == null) {
                inviteCountData.postValue(RESPONSE_RETRYABLE);
            } else {
                preferences.setInvitesRemaining(response);
                inviteCountData.postValue(response);
            }
        }).onError(e -> {
            inviteCountData.postValue(RESPONSE_RETRYABLE);
        });
    }

    public void refreshInvites() {
        fetchInvites();
    }

    public void refreshContacts() {
        contactList.invalidate();
        waContacts.invalidate();
    }

    public LiveData<Integer> getInviteCount() {
        return inviteCountData;
    }

    public LiveData<List<Contact>> getContactList() {
        return contactList.getLiveData();
    }

    public LiveData<Integer> sendInvite(@NonNull Contact contact) {
        MutableLiveData<Integer> inviteResult = new MutableLiveData<>();
        invitesApi.sendInvite(Preconditions.checkNotNull(contact.normalizedPhone)).onResponse(result -> {
            inviteResult.postValue(result);
            contactsDb.markInvited(contact);
            fetchInvites();
            refreshContacts();
        }).onError(e -> {
            inviteResult.postValue(InvitesResponseIq.Result.UNKNOWN);
            Log.e("inviteFriendsViewModel/sendInvite failed to send invite", e);
        });
        return inviteResult;
    }

    @Override
    protected void onCleared() {
        contactsDb.removeObserver(contactsObserver);
    }
}

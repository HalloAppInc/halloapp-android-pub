package com.halloapp.contacts;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.halloapp.Connection;
import com.halloapp.HalloApp;
import com.halloapp.protocol.ContactsSyncResponse;
import com.halloapp.util.Log;

import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Domainpart;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Contacts {

    private static Contacts instance;

    final List<Contact> contacts = Arrays.asList(
            new Contact(1, 1, "Duygu", "+1 (347) 752-1636", "13477521636", true),
            new Contact(2, 2, "Murali", "1 (470) 338-1473", "14703381473", true),
            new Contact(3, 3, "Michael", "14154121848", "14154121848", true),
            new Contact(4, 4, "Tony", "14088922686", "14088922686", true),
            new Contact(5, 5, "Tony", "14088922686", "14088922686", true),
            new Contact(6, 6, "Neeraj", "(650) 3363079", "16503363079", true),
            new Contact(7, 7, "Dmitri", "(650) 275-2675", "16502752675", true),
            new Contact(8, 8, "Dima", "(650) 281-3677", "16502813677", true),
            new Contact(9, 9, "Unknown", "(650) 555-3677", "16505553677", false)
    );

    public static Contacts getInstance() {
        if (instance == null) {
            synchronized(Contacts.class) {
                if (instance == null) {
                    instance = new Contacts();
                }
            }
        }
        return instance;
    }

    private Contacts() {
    }

    public String getName(String jidString) {
        Jid jid;
        try {
            jid = JidCreate.bareFrom(jidString);
        } catch (XmppStringprepException e) {
            return null;
        }
        for (Contact contact : contacts) {
            if (jid.equals(contact.jid)) {
                return contact.name;
            }
        }
        return null;
    }

    public Collection<Jid> getMemberJids() {
        final Collection<Jid> jids = new ArrayList<>();
        for (Contact contact : contacts) {
            if (contact.member) {
                jids.add(contact.jid);
            }
        }
        return jids;
    }

    public Collection<String> getPhones() {
        final Collection<String> phones = new HashSet<>();
        for (Contact contact : contacts) {
            phones.add(contact.phone);
        }
        return phones;
    }

    public void startContactSync(Context context) {
        WorkManager.getInstance(context)
                .enqueueUniqueWork("contact-sync", ExistingWorkPolicy.REPLACE, new OneTimeWorkRequest.Builder(ContactSyncWorker.class).build());
    }


    public static class ContactSyncWorker extends Worker {

        public ContactSyncWorker(
                @NonNull Context context,
                @NonNull WorkerParameters params) {
            super(context, params);
        }

        @Override
        public @NonNull Result doWork() {
            final ContactsDb contactsDb = ContactsDb.getInstance(getApplicationContext());
            final Collection<Contact> contacts = contactsDb.getAllContacts();
            final HashMap<String, List<Contact>> phones = new HashMap<>();
            for (Contact contact : contacts) {
                List<Contact> phoneContactList = phones.get(contact.phone);
                if (phoneContactList == null) {
                    phoneContactList = new ArrayList<>();
                    phones.put(contact.phone, phoneContactList);
                }
                phoneContactList.add(contact);
            }
            final Future<List<ContactsSyncResponse.Contact>> future = HalloApp.instance.connection.syncContacts(phones.keySet());
            try {
                final List<ContactsSyncResponse.Contact> contactSyncResults = future.get();
                if (contactSyncResults == null) {
                    Log.e("ContactSyncWorker: failed");
                    return Result.failure();
                }
                final Collection<Contact> updatedContacts = new ArrayList<>();
                for (ContactsSyncResponse.Contact contactsSyncResult : contactSyncResults) {
                    final List<Contact> phoneContacts = phones.get(contactsSyncResult.phone);
                    if (phoneContacts == null) {
                        Log.e("ContactSyncWorker: phone " + contactsSyncResult.phone + "returned from server doesn't match to local phones");
                        continue;
                    }
                    for (Contact contact : phoneContacts) {
                        boolean contactUpdated = false;
                        if (contact.member != ("member".equals(contactsSyncResult.role))) {
                            contact.member = !contact.member;
                            contactUpdated = true;
                        }
                        if (!Objects.equals(contact.jid == null ? null : contact.jid.getLocalpartOrNull(), contactsSyncResult.normalizedPhone)) {
                            if (contactsSyncResult.normalizedPhone == null) {
                                contact.jid = null;
                            } else {
                                contact.jid = JidCreate.entityBareFrom(Localpart.fromOrThrowUnchecked(contactsSyncResult.normalizedPhone), Domainpart.fromOrNull(Connection.XMPP_DOMAIN));
                            }
                            contactUpdated = true;
                        }
                        if (contactUpdated) {
                            updatedContacts.add(contact);
                        }
                    }
                }
                if (!updatedContacts.isEmpty()) {
                    contactsDb.updateContactsMembership(updatedContacts).get();
                }
                Log.d("ContactSyncWorker: " + updatedContacts.size() + " contacts updated");
            } catch (ExecutionException | InterruptedException e) {
                Log.e("ContactSyncWorker", e);
                return Result.failure();
            }

            return Result.success();
        }
    }
}

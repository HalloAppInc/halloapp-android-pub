package com.halloapp.contacts;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.work.ExistingWorkPolicy;
import androidx.work.ListenableWorker;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ContactsSync {

    public static final String ADDRESS_BOOK_SYNC_WORK_ID = "addess-book-sync";
    public static final String CONTACT_SYNC_WORK_ID = "contact-sync";
    public static final String PUBSUB_SYNC_WORK_ID = "pubsub-sync";

    private static ContactsSync instance;

    private final Context context;
    private boolean initialized;
    private UUID lastSyncRequestId;

    public static ContactsSync getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized(ContactsSync.class) {
                if (instance == null) {
                    instance = new ContactsSync(context);
                }
            }
        }
        return instance;
    }

    private ContactsSync(@NonNull Context context) {
        this.context = context.getApplicationContext();
    }

    public UUID getLastSyncRequestId() {
        return lastSyncRequestId;
    }

    @MainThread
    public void startAddressBookListener() {
        if (!initialized) {
            try {
                context.getContentResolver().registerContentObserver(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, true, new ContentObserver(null) {

                    public void onChange(boolean selfChange, Uri uri) {
                        Log.i("ContactsSync: changed " + uri);
                        startAddressBookSync();
                    }
                });
                initialized = true;
            } catch (SecurityException ex) {
                Log.w("ContactsSync.startAddressBookListener", ex);
            }
        }
    }

    @MainThread
    public void startAddressBookSync() {
        WorkManager.getInstance(context).enqueueUniqueWork(ADDRESS_BOOK_SYNC_WORK_ID, ExistingWorkPolicy.REPLACE, new OneTimeWorkRequest.Builder(AddressBookSyncWorker.class).build());
    }

    @MainThread
    public void startContactSync() {
        final OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(ContactSyncWorker.class).build();
        lastSyncRequestId = workRequest.getId();
        WorkManager.getInstance(context).enqueueUniqueWork(CONTACT_SYNC_WORK_ID, ExistingWorkPolicy.REPLACE, workRequest);
    }

    @MainThread
    public void startPubSubSync() {
        WorkManager.getInstance(context).enqueueUniqueWork(PUBSUB_SYNC_WORK_ID, ExistingWorkPolicy.REPLACE, new OneTimeWorkRequest.Builder(PubSubSyncWorker.class).build());
    }

    @WorkerThread
    private ListenableWorker.Result performContactSync() {

        Log.i("ContactsSync.performContactSync");
        if (HalloApp.instance.getLastSyncTime() <= 0) {
            // initial sync, need to sync address book first
            Log.i("ContactsSync.performContactSync: initial address book sync");
            try {
                ContactsDb.getInstance(context).syncAddressBook().get();
            } catch (ExecutionException | InterruptedException e) {
                Log.e("ContactsSync.performContactSync", e);
                return ListenableWorker.Result.failure();
            }
        }

        final ContactsDb contactsDb = ContactsDb.getInstance(context);
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
        Log.i("ContactsSync.performContactSync: " + phones.keySet().size() + " phones to sync");
        final Future<List<ContactsSyncResponse.Contact>> future = Connection.getInstance().syncContacts(phones.keySet());
        try {
            final List<ContactsSyncResponse.Contact> contactSyncResults = future.get();
            if (contactSyncResults == null) {
                Log.e("ContactsSync.performContactSync: failed");
                return ListenableWorker.Result.failure();
            }
            final Collection<Contact> updatedContacts = new ArrayList<>();
            for (ContactsSyncResponse.Contact contactsSyncResult : contactSyncResults) {
                final List<Contact> phoneContacts = phones.get(contactsSyncResult.phone);
                if (phoneContacts == null) {
                    Log.e("ContactsSync.performContactSync: phone " + contactsSyncResult.phone + "returned from server doesn't match to local phones");
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
            final Collection<Jid> memberJids = contactsDb.getMemberJids();
            Log.i("ContactsSync.performContactSync: " + memberJids.size() + " to pubsub");
            Connection.getInstance().syncPubSub(memberJids).get();

            HalloApp.instance.setLastSyncTime(System.currentTimeMillis());

            Log.d("ContactsSync.performContactSync: " + updatedContacts.size() + " contacts updated");
        } catch (ExecutionException | InterruptedException e) {
            Log.e("ContactsSync.performContactSync", e);
            return ListenableWorker.Result.failure();
        }

        return ListenableWorker.Result.success();
    }

    public static class ContactSyncWorker extends Worker {

        public ContactSyncWorker(
                @NonNull Context context,
                @NonNull WorkerParameters params) {
            super(context, params);
        }

        @Override
        public @NonNull Result doWork() {
            return ContactsSync.getInstance(getApplicationContext()).performContactSync();
        }
    }

    public static class AddressBookSyncWorker extends Worker {

        public AddressBookSyncWorker(
                @NonNull Context context,
                @NonNull WorkerParameters params) {
            super(context, params);
        }

        @Override
        public @NonNull Result doWork() {
            try {
                final ContactsDiff contactsDiff = ContactsDb.getInstance(getApplicationContext()).syncAddressBook().get();
                if (contactsDiff == null) {
                    Log.e("ContactsSync.AddressBookSyncWorker: diff is null");
                    return ListenableWorker.Result.failure();
                }
                if (!contactsDiff.isEmpty()) {
                    new Handler(Looper.getMainLooper()).post(() -> ContactsSync.getInstance(getApplicationContext()).startContactSync());
                }
                return ListenableWorker.Result.success();
            } catch (ExecutionException | InterruptedException e) {
                Log.e("ContactsSync.AddressBookSyncWorker", e);
                return ListenableWorker.Result.failure();
            }
        }
    }

    public static class PubSubSyncWorker extends Worker {

        public PubSubSyncWorker(
                @NonNull Context context,
                @NonNull WorkerParameters params) {
            super(context, params);
        }

        @Override
        public @NonNull Result doWork() {
            try {
                final Collection<Jid> memberJids = ContactsDb.getInstance(getApplicationContext()).getMemberJids();
                Log.i("PubSubSyncWorker: " + memberJids.size() + " to pubsub");
                Connection.getInstance().syncPubSub(memberJids).get();
                return ListenableWorker.Result.success();
            } catch (ExecutionException | InterruptedException e) {
                Log.e("PubSubSyncWorker", e);
                return ListenableWorker.Result.failure();
            }
        }
    }
}

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
import androidx.core.util.Pair;
import androidx.core.util.Preconditions;
import androidx.work.ExistingWorkPolicy;
import androidx.work.ListenableWorker;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.halloapp.Preferences;
import com.halloapp.posts.Comment;
import com.halloapp.posts.Post;
import com.halloapp.posts.PostsDb;
import com.halloapp.util.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.ContactsSyncResponseIq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class ContactsSync {

    public static final String ADDRESS_BOOK_SYNC_WORK_ID = "address-book-sync";
    public static final String CONTACT_SYNC_WORK_ID = "contact-sync";

    private static final int CONTACT_SYNC_BATCH_SIZE = 128;

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

    @WorkerThread
    private ListenableWorker.Result performContactSync() {

        Log.i("ContactsSync.performContactSync");
        if (Preferences.getInstance(context).getLastSyncTime() <= 0) {
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
        final List<String> phonesBatch = new ArrayList<>(CONTACT_SYNC_BATCH_SIZE);
        final List<ContactsSyncResponseIq.Contact> contactSyncResults = new ArrayList<>(phonesBatch.size());
        boolean firstBatch = true;
        for (String phone : phones.keySet()) {
            phonesBatch.add(phone);
            if (phonesBatch.size() >= CONTACT_SYNC_BATCH_SIZE) {
                Log.i("ContactsSync.performContactSync: batch " + phonesBatch.size() + " phones to sync");
                try {
                    final List<ContactsSyncResponseIq.Contact> contactSyncBatchResults = Connection.getInstance().syncContacts(phonesBatch, firstBatch).get();
                    firstBatch = false;
                    if (contactSyncBatchResults != null) {
                        contactSyncResults.addAll(contactSyncBatchResults);
                        phonesBatch.clear();
                    } else {
                        Log.e("ContactsSync.performContactSync: failed to sync batch");
                        return ListenableWorker.Result.failure();
                    }
                } catch (ExecutionException | InterruptedException e) {
                    Log.e("ContactsSync.performContactSync: failed to sync batch", e);
                    return ListenableWorker.Result.failure();
                }
            }
        }
        if (phonesBatch.size() > 0) {
            Log.i("ContactsSync.performContactSync: last batch " + phonesBatch.size() + " phones to sync");
            try {
                final List<ContactsSyncResponseIq.Contact> contactSyncBatchResults = Connection.getInstance().syncContacts(phonesBatch, firstBatch).get();
                if (contactSyncBatchResults != null) {
                    contactSyncResults.addAll(contactSyncBatchResults);
                    phonesBatch.clear();
                } else {
                    Log.e("ContactsSync.performContactSync: failed to sync last batch");
                    return ListenableWorker.Result.failure();
                }
            } catch (ExecutionException | InterruptedException e) {
                Log.e("ContactsSync.performContactSync: failed to sync last batch", e);
                return ListenableWorker.Result.failure();
            }
        }

        final Collection<Contact> updatedContacts = new ArrayList<>();
        for (ContactsSyncResponseIq.Contact contactsSyncResult : contactSyncResults) {
            final List<Contact> phoneContacts = phones.get(contactsSyncResult.phone);
            if (phoneContacts == null) {
                Log.e("ContactsSync.performContactSync: phone " + contactsSyncResult.phone + "returned from server doesn't match to local phones");
                continue;
            }
            for (Contact contact : phoneContacts) {
                boolean contactUpdated = false;
                if (contact.friend != ("friends".equals(contactsSyncResult.role))) {
                    contact.friend = !contact.friend;
                    contactUpdated = true;
                    Log.i("ContactsSync.performContactSync: update friendship for " + contact.name + " to " + contact.friend);
                }
                if (!Objects.equals(contact.userId == null ? null : contact.userId.rawId(), contactsSyncResult.normalizedPhone)) {
                    if (contactsSyncResult.normalizedPhone == null) {
                        contact.userId = null;
                    } else {
                        contact.userId = new UserId(contactsSyncResult.normalizedPhone);
                    }
                    Log.i("ContactsSync.performContactSync: update jid for " + contact.name + " to " + contact.userId);
                    contactUpdated = true;
                }
                if (contactUpdated) {
                    updatedContacts.add(contact);
                }
            }
        }

        if (!updatedContacts.isEmpty()) {
            try {
                contactsDb.updateContactsFriendship(updatedContacts).get();
            } catch (ExecutionException | InterruptedException e) {
                Log.e("ContactsSync.performContactSync: failed to update friendship", e);
                return ListenableWorker.Result.failure();
            }
        }

        Log.i("ContactsSync.performContactSync: " + updatedContacts.size() + " contacts updated");

        // TODO (ds): remove
        try {
            final Pair<Collection<Post>, Collection<Comment>> result = Connection.getInstance().getFeedHistory().get();
            if (result == null) {
                Log.e("ContactsSync.performContactSync: failed retrieve feed history");
                return ListenableWorker.Result.failure();
            }
            PostsDb.getInstance(context).addHistory(Preconditions.checkNotNull(result.first), Preconditions.checkNotNull(result.second));
        } catch (ExecutionException | InterruptedException e) {
            Log.e("ContactsSync.performContactSync: failed retrieve feed history", e);
            return ListenableWorker.Result.failure();
        }

        Preferences.getInstance(context).setLastSyncTime(System.currentTimeMillis());

        Log.i("ContactsSync.done: " + Preferences.getInstance(context).getLastSyncTime());

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
            final Result result = ContactsSync.getInstance(getApplicationContext()).performContactSync();
            if  (!Result.success().equals(result)) {
                Log.sendErrorReport("ContactsSync failed");
            }
            return result;
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
}

package com.halloapp.contacts;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Base64;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.ListenableWorker;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.halloapp.Preferences;
import com.halloapp.content.ContentDb;
import com.halloapp.id.UserId;
import com.halloapp.util.logs.Log;
import com.halloapp.util.RandomId;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.ContactInfo;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class ContactsSync {

    private static final String FULL_CONTACT_SYNC_WORK_ID = "contact-sync-full";
    private static final String INCREMENTAL_CONTACT_SYNC_WORK_ID = "contact-sync-incremental";

    private static final String WORKER_PARAM_FULL_SYNC = "full_sync";
    private static final String WORKER_PARAM_CONTACT_HASHES = "contact_hashes";

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

    public LiveData<List<WorkInfo>> getWorkInfoLiveData() {
        return WorkManager.getInstance(context).getWorkInfosForUniqueWorkLiveData(ContactsSync.FULL_CONTACT_SYNC_WORK_ID);
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
                        startContactsSync(false);
                    }
                });
                initialized = true;
            } catch (SecurityException ex) {
                Log.w("ContactsSync.startAddressBookListener", ex);
            }
        }
    }

    public void cancelContactsSync() {
        WorkManager.getInstance(context).cancelUniqueWork(INCREMENTAL_CONTACT_SYNC_WORK_ID);
        WorkManager.getInstance(context).cancelUniqueWork(FULL_CONTACT_SYNC_WORK_ID);
    }

    private void startContactsSyncInternal(boolean fullSync, String[] contactHashes) {
        final Data data = new Data.Builder().putBoolean(WORKER_PARAM_FULL_SYNC, fullSync).putStringArray(WORKER_PARAM_CONTACT_HASHES, contactHashes).build();
        final OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(ContactSyncWorker.class).setInputData(data).build();
        lastSyncRequestId = workRequest.getId();
        WorkManager.getInstance(context).enqueueUniqueWork(fullSync ? FULL_CONTACT_SYNC_WORK_ID : INCREMENTAL_CONTACT_SYNC_WORK_ID, ExistingWorkPolicy.REPLACE, workRequest);
    }

    public void startContactsSync(boolean fullSync) {
        startContactsSyncInternal(fullSync, new String[]{});
    }

    public void startContactSync(@NonNull List<String> contactHashes) {
        if (contactHashes.isEmpty()) {
            Log.w("startContactSync called with empty hash list");
            return;
        }

        String[] arr = new String[contactHashes.size()];
        for (int i=0; i<contactHashes.size(); i++) {
            arr[i] = contactHashes.get(i);
        }

        startContactsSyncInternal(false, arr);
    }

    @WorkerThread
    private ListenableWorker.Result performContactSync(boolean fullSync, @NonNull List<String> contactHashes) {
        Log.i("ContactsSync.performContactSync");
        final ContactsDb.AddressBookSyncResult syncResult;
        try {
            syncResult = ContactsDb.getInstance().syncAddressBook().get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e("ContactsSync.performContactSync", e);
            return ListenableWorker.Result.failure();
        }
        if (syncResult == null) {
            Log.e("ContactsSync.performContactSync: address book diff is null");
            return ListenableWorker.Result.failure();
        }

        final Preferences preferences = Preferences.getInstance();
        final ListenableWorker.Result result;
        if (fullSync ||
                preferences.getRequireFullContactsSync() ||
                preferences.getLastContactsSyncTime() <= 0 ||
                contactHashes.contains("")) {
            result = performFullContactSync();
        } else {
            result = performIncrementalContactSync(syncResult, getHashSyncContacts(contactHashes));
        }

        if (ListenableWorker.Result.failure().equals(result)) {
            preferences.setRequireFullContactsSync(true);
        } else {
            preferences.setRequireFullContactsSync(false);
            preferences.setLastContactsSyncTime(System.currentTimeMillis());
        }

        Log.i("ContactsSync.done: " + Preferences.getInstance().getLastContactsSyncTime());
        return result;
    }

    private List<Contact> getHashSyncContacts(@NonNull List<String> contactHashStrings) {
        List<Contact> contactsToSync = new ArrayList<>();
        List<byte[]> contactHashes = new ArrayList<>();
        for (String contactHashString : contactHashStrings) {
            contactHashes.add(Base64.decode(contactHashString, Base64.DEFAULT));
        }
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            Log.e("ContactsSync.performHashContactSync: failed to get digest for sha256", e);
            return contactsToSync;
        }
        List<Contact> allContacts = ContactsDb.getInstance().getAllContacts();
        for (Contact contact : allContacts) {
            String normalizedPhone = contact.normalizedPhone;
            if (normalizedPhone != null) {
                byte[] bytes = messageDigest.digest(normalizedPhone.getBytes());
                for (byte[] contactHash : contactHashes) {
                    boolean isMatch = true;
                    for (int i=0; i<contactHash.length; i++) {
                        if (bytes[i] != contactHash[i]) {
                            isMatch = false;
                            break;
                        }
                    }
                    if (isMatch) {
                        contactsToSync.add(contact);
                        break;
                    }
                }
            }
        }
        return contactsToSync;
    }

    @WorkerThread
    private ListenableWorker.Result performIncrementalContactSync(@NonNull ContactsDb.AddressBookSyncResult addressBookSyncResult, @NonNull List<Contact> hashSyncContacts) {
        if (!addressBookSyncResult.removed.isEmpty()) {
            try {
                Connection.getInstance().syncContacts(null, addressBookSyncResult.removed,
                        false, null, 0, true).get();
            } catch (ExecutionException | InterruptedException e) {
                Log.i("ContactsSync.performContactSync: failed to delete contacts", e);
                return ListenableWorker.Result.failure();
            }
        }
        final ArrayList<Contact> contacts = new ArrayList<>(hashSyncContacts);
        contacts.addAll(addressBookSyncResult.updated);
        contacts.addAll(addressBookSyncResult.added);
        if (contacts.isEmpty()) {
            return ListenableWorker.Result.success();
        }
        return updateContactsOnServer(contacts, false);
    }

    @WorkerThread
    private ListenableWorker.Result performFullContactSync() {
        return updateContactsOnServer(ContactsDb.getInstance().getAllContacts(), true);
    }

    @WorkerThread
    private ListenableWorker.Result updateContactsOnServer(@NonNull Collection<Contact> contacts, boolean fullSync) {
        final HashMap<String, List<Contact>> phones = new HashMap<>();
        for (Contact contact : contacts) {
            List<Contact> phoneContactList = phones.get(contact.addressBookPhone);
            if (phoneContactList == null) {
                phoneContactList = new ArrayList<>();
                phones.put(contact.addressBookPhone, phoneContactList);
            }
            phoneContactList.add(contact);
        }

        Log.i("ContactsSync.performContactSync: " + phones.keySet().size() + " phones to sync");
        final List<String> phonesBatch = new ArrayList<>(CONTACT_SYNC_BATCH_SIZE);
        final List<ContactInfo> contactSyncResults = new ArrayList<>(phonesBatch.size());
        final String syncId = fullSync ? RandomId.create() : null;
        int phonesSyncedCount = 0;
        int batchIndex = 0;
        for (String phone : phones.keySet()) {
            phonesBatch.add(phone);
            phonesSyncedCount++;
            final boolean lastBatch = phonesSyncedCount == phones.size();
            if (phonesBatch.size() >= CONTACT_SYNC_BATCH_SIZE || lastBatch) {
                Log.i("ContactsSync.performContactSync: batch " + phonesBatch.size() + " phones to sync");
                try {
                    final List<ContactInfo> contactSyncBatchResults = Connection.getInstance().syncContacts(phonesBatch, null, fullSync, syncId, batchIndex, lastBatch).get();
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

        final Collection<Contact> updatedContacts = new ArrayList<>();
        final Collection<UserId> newFriends = new HashSet<>();
        final long syncTime = System.currentTimeMillis();
        final boolean initialSync = Preferences.getInstance().getLastContactsSyncTime() == 0;
        for (ContactInfo contactsSyncResult : contactSyncResults) {
            final List<Contact> phoneContacts = phones.get(contactsSyncResult.phone);
            if (phoneContacts == null) {
                Log.e("ContactsSync.performContactSync: phone " + contactsSyncResult.phone + "returned from server doesn't match to local phones");
                continue;
            }
            for (Contact contact : phoneContacts) {
                boolean contactUpdated = false;
                boolean isNewFriend = false;
                if (contact.friend != ("friends".equals(contactsSyncResult.role))) {
                    contact.friend = !contact.friend;
                    contactUpdated = true;
                    if (contact.friend) {
                        isNewFriend = true;
                        if (!initialSync) {
                            contact.newConnection = true;
                            contact.connectionTime = syncTime;
                        }
                    }
                    Log.i("ContactsSync.performContactSync: update friendship for " + contact.addressBookName + " to " + contact.friend);
                }
                if (!Objects.equals(contact.userId == null ? null : contact.userId.rawId(), contactsSyncResult.userId)) {
                    if (contactsSyncResult.userId == null) {
                        contact.userId = null;
                    } else {
                        contact.userId = new UserId(contactsSyncResult.userId);
                    }
                    Log.i("ContactsSync.performContactSync: update user id for " + contact.addressBookName + " to " + contact.userId);
                    contactUpdated = true;
                }
                if (!Objects.equals(contact.normalizedPhone, contactsSyncResult.normalizedPhone)) {
                    contact.normalizedPhone = contactsSyncResult.normalizedPhone;
                    Log.i("ContactsSync.performContactSync: update normalized phone for " + contact.addressBookName + " to " + contact.normalizedPhone);
                    contactUpdated = true;
                }
                if (!Objects.equals(contact.avatarId, contactsSyncResult.avatarId)) {
                    contact.avatarId = contactsSyncResult.avatarId;
                    Log.i("ContactsSync.performContactSync: update avatar id for " + contact.addressBookName + " to " + contact.avatarId);
                    contactUpdated = true;
                }
                if (!Objects.equals(contact.halloName, contactsSyncResult.halloName)) {
                    contact.halloName = contactsSyncResult.halloName;
                    Log.i("ContactSync.performContactSync: update push name for " + contact.addressBookName + " to " + contact.halloName);
                    contactUpdated = true;
                }
                if (contactUpdated) {
                    updatedContacts.add(contact);
                }
                if (isNewFriend) {
                    newFriends.add(contact.userId);
                }
            }
        }

        if (!updatedContacts.isEmpty()) {
            try {
                ContactsDb.getInstance().updateContactsServerData(updatedContacts, newFriends).get();
                Map<UserId, String> nameMap = new HashMap<>();
                for (Contact contact : updatedContacts) {
                    nameMap.put(contact.userId, contact.halloName);
                }
                ContactsDb.getInstance().updateUserNames(nameMap);
                // TODO(ds): remove
                ContentDb.getInstance().migrateUserIds(updatedContacts);
            } catch (ExecutionException | InterruptedException e) {
                Log.e("ContactsSync.performContactSync: failed to update server data", e);
                return ListenableWorker.Result.failure();
            }
        }

        Log.i("ContactsSync.performContactSync: " + updatedContacts.size() + " contacts updated");

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
            boolean fullSync = getInputData().getBoolean(WORKER_PARAM_FULL_SYNC, true);
            String[] contactHashes = getInputData().getStringArray(WORKER_PARAM_CONTACT_HASHES);
            if (contactHashes == null) {
                contactHashes = new String[]{};
            }
            final Result result = ContactsSync.getInstance(getApplicationContext()).performContactSync(fullSync, Arrays.asList(contactHashes));
            if  (!Result.success().equals(result)) {
                Log.sendErrorReport("ContactsSync failed");
            }
            return result;
        }
    }
}

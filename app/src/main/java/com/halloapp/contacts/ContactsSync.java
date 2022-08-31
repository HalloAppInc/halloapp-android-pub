package com.halloapp.contacts;

import android.Manifest;
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

import com.halloapp.AppContext;
import com.halloapp.Preferences;
import com.halloapp.id.UserId;
import com.halloapp.nux.ZeroZoneManager;
import com.halloapp.props.ServerProps;
import com.halloapp.util.Preconditions;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.ContactInfo;
import com.halloapp.xmpp.util.ObservableErrorException;

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

import pub.devrel.easypermissions.EasyPermissions;

public class ContactsSync {

    private static final String CONTACT_SYNC_WORK_ID = "contact-sync";

    private static final String WORKER_PARAM_FULL_SYNC = "full_sync";
    private static final String WORKER_PARAM_CONTACT_HASHES = "contact_hashes";

    private static final int CONTACT_SYNC_BATCH_SIZE = 1024;
    private static final int ATTEMPTS_PER_BATCH = 8;

    private static ContactsSync instance;

    private final AppContext appContext;
    private final Preferences preferences;

    private final RawContactDatabase rawContactDatabase;

    private boolean initialized;
    private UUID lastFullSyncRequestId;

    public static ContactsSync getInstance() {
        if (instance == null) {
            synchronized(ContactsSync.class) {
                if (instance == null) {
                    instance = new ContactsSync(AppContext.getInstance(), Preferences.getInstance());
                }
            }
        }
        return instance;
    }

    private ContactsSync(@NonNull AppContext appContext, @NonNull Preferences preferences) {
        this.appContext = appContext;
        this.preferences = preferences;

        this.rawContactDatabase = new RawContactDatabase(appContext.get());
    }

    public LiveData<List<WorkInfo>> getWorkInfoLiveData() {
        return WorkManager.getInstance(appContext.get()).getWorkInfosForUniqueWorkLiveData(CONTACT_SYNC_WORK_ID);
    }

    public UUID getLastFullSyncRequestId() {
        return lastFullSyncRequestId;
    }

    @MainThread
    public void startAddressBookListener() {
        if (!initialized) {
            try {
                appContext.get().getContentResolver().registerContentObserver(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, true, new ContentObserver(null) {

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
        Context context = appContext.get();
        WorkManager.getInstance(context).cancelUniqueWork(CONTACT_SYNC_WORK_ID);
    }

    private void startContactsSyncInternal(boolean fullSync, String[] contactHashes) {
        final Data data = new Data.Builder()
                .putBoolean(WORKER_PARAM_FULL_SYNC, fullSync)
                .putStringArray(WORKER_PARAM_CONTACT_HASHES, contactHashes)
                .build();
        final OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(ContactSyncWorker.class).setInputData(data).build();
        if (fullSync) {
            lastFullSyncRequestId = workRequest.getId();
        }
        ExistingWorkPolicy existingWorkPolicy = ExistingWorkPolicy.KEEP;
        if (fullSync || contactHashes.length > 0) {
            existingWorkPolicy = ExistingWorkPolicy.APPEND_OR_REPLACE;
        }
        WorkManager.getInstance(appContext.get()).enqueueUniqueWork(CONTACT_SYNC_WORK_ID, existingWorkPolicy, workRequest);
    }

    public void forceFullContactsSync() {
        forceFullContactsSync(false);
    }

    public void forceFullContactsSync(boolean initialSync) {
        if (initialSync) {
            preferences.clearContactSyncBackoffTime();
        }
        preferences.applyRequireFullContactsSync(true);
        startContactsSyncInternal(true, new String[]{});
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

        rawContactDatabase.removeRawContacts(syncResult.removed);

        final ListenableWorker.Result result;

        boolean shouldPerformFullSync = fullSync ||
                preferences.getRequireFullContactsSync() ||
                preferences.getLastFullContactSyncTime() <= 0 ||
                contactHashes.contains("");
        if (shouldPerformFullSync) {
            result = performFullContactSync();
        } else {
            result = performIncrementalContactSync(syncResult, getHashSyncContacts(contactHashes));
        }

        if (ListenableWorker.Result.failure().equals(result)) {
            preferences.setRequireFullContactsSync(true);
        } else {
            preferences.setRequireFullContactsSync(false);
            if (shouldPerformFullSync) {
                preferences.setLastFullContactSyncTime(System.currentTimeMillis());
            }
        }

        Log.i("ContactsSync.done: " + preferences.getLastFullContactSyncTime());
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
                ContactSyncResult result = Connection.getInstance().syncContacts(null, addressBookSyncResult.removed,
                        false, null, 0, true).await();
                if (result == null) {
                    return ListenableWorker.Result.failure();
                }
                if (!result.success) {
                    setSyncBackoff(result.retryAfterSecs);
                    Log.e("ContactsSync.performContactSync: failed to delete contacts backoff=" + result.retryAfterSecs);
                    return ListenableWorker.Result.failure();
                }
            } catch (InterruptedException | ObservableErrorException e) {
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
        List<Contact> contacts = ContactsDb.getInstance().getAllContacts();
        rawContactDatabase.updateFullSync(contacts);
        return updateContactsOnServer(contacts, true);
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
                ContactSyncResult contactSyncBatchResults = null;
                int attempts = 0;
                int prevDelay = 1;
                int delaySeconds = 1;
                Log.i("ContactsSync.performContactSync: batch " + phonesBatch.size() + " phones to sync");
                while (contactSyncBatchResults == null && attempts < ATTEMPTS_PER_BATCH) {
                    if (attempts > 0) {
                        Log.i("ContactsSync.performContactSync waiting " + delaySeconds + " seconds before retrying");
                        try {
                            Thread.sleep(delaySeconds * 1000);
                        } catch (InterruptedException ex) {
                            Log.i("ContactsSync.performContactSync delay interrupted", ex);
                        }
                        int nextDelay = prevDelay + delaySeconds;
                        prevDelay = delaySeconds;
                        delaySeconds = nextDelay;
                    }
                    attempts++;
                    Log.i("ContactsSync.performContactSync: attempting sync of batch: " + batchIndex + " attempt: " + attempts);
                    try {
                        contactSyncBatchResults = Connection.getInstance().syncContacts(phonesBatch, null, fullSync, syncId, batchIndex, lastBatch).await();
                    } catch (InterruptedException | ObservableErrorException e) {
                        Log.e("ContactsSync.performContactSync: failed to sync batch", e);
                    }
                    if (contactSyncBatchResults != null && !contactSyncBatchResults.success) {
                        Log.e("ContactSync.performContactSync: hit an error during batch backoff secs=" + contactSyncBatchResults.retryAfterSecs);
                        setSyncBackoff(contactSyncBatchResults.retryAfterSecs);
                        return ListenableWorker.Result.failure();
                    }
                }
                if (contactSyncBatchResults == null) {
                    return ListenableWorker.Result.failure();
                }
                contactSyncResults.addAll(Preconditions.checkNotNull(contactSyncBatchResults.contactList));
                phonesBatch.clear();
                batchIndex++;
            }
        }

        final Collection<Contact> updatedContacts = new ArrayList<>();
        final Collection<UserId> newContacts = new HashSet<>();
        final long syncTime = System.currentTimeMillis();
        final boolean initialSync = preferences.getLastFullContactSyncTime() == 0;
        for (ContactInfo contactsSyncResult : contactSyncResults) {
            final List<Contact> phoneContacts = phones.get(contactsSyncResult.phone);
            if (phoneContacts == null) {
                Log.e("ContactsSync.performContactSync: phone " + contactsSyncResult.phone + "returned from server doesn't match to local phones");
                continue;
            }
            for (Contact contact : phoneContacts) {
                boolean contactUpdated = false;
                boolean isNewContact = false;
                if (!Objects.equals(contact.userId == null ? null : contact.userId.rawId(), contactsSyncResult.userId)) {
                    if (contactsSyncResult.userId == null) {
                        contact.userId = null;
                    } else {
                        contact.userId = new UserId(contactsSyncResult.userId);
                        isNewContact = true;
                        if (!initialSync) {
                            contact.newConnection = true;
                            contact.connectionTime = syncTime;
                        }
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
                if (contact.userId != null && !Objects.equals(contact.halloName, contactsSyncResult.halloName)) {
                    contact.halloName = contactsSyncResult.halloName;
                    Log.i("ContactSync.performContactSync: update push name for " + contact.addressBookName + " to " + contact.halloName);
                    contactUpdated = true;
                }
                if (contact.numPotentialFriends != contactsSyncResult.numPotentialFriends) {
                    contact.numPotentialFriends = contactsSyncResult.numPotentialFriends;
                    Log.i("ContactSync.performContactSync: update num potential friends " + contact.addressBookName + " to " + contact.numPotentialFriends);
                    contactUpdated = true;
                }
                if (contactUpdated) {
                    updatedContacts.add(contact);
                }
                if (isNewContact) {
                    newContacts.add(contact.userId);
                }
            }
        }

        if (!updatedContacts.isEmpty()) {
            rawContactDatabase.updateRawContacts(updatedContacts);
            try {
                ContactsDb.getInstance().updateContactsServerData(updatedContacts, newContacts).get();
                Map<UserId, String> nameMap = new HashMap<>();
                for (Contact contact : updatedContacts) {
                    nameMap.put(contact.userId, contact.halloName);
                }
                ContactsDb.getInstance().updateUserNames(nameMap);
            } catch (ExecutionException | InterruptedException e) {
                Log.e("ContactsSync.performContactSync: failed to update server data", e);
                return ListenableWorker.Result.failure();
            }
        }

        Log.i("ContactsSync.performContactSync: " + updatedContacts.size() + " contacts updated");

        return ListenableWorker.Result.success();
    }

    private void setSyncBackoff(long backoffTimeSecs) {
        long backoffUntil = System.currentTimeMillis() + (backoffTimeSecs * 1000L);
        preferences.setContactSyncBackoffTime(backoffUntil);
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
            boolean forceSync = Preferences.getInstance().getRequireFullContactsSync();
            String[] contactHashes = getInputData().getStringArray(WORKER_PARAM_CONTACT_HASHES);
            if (contactHashes == null) {
                contactHashes = new String[]{};
            }
            long backoffTime = Preferences.getInstance().getContactSyncBackoffTime();
            if (System.currentTimeMillis() < backoffTime) {
                Log.i("ContactsSyncWorker.doWork aborting backoff until " + backoffTime);
                return Result.success();
            }
            if (fullSync && !forceSync) {
                long syncIntervalMs = ServerProps.getInstance().getContactSyncIntervalSeconds() * 1000L;
                long lastFullSync = Preferences.getInstance().getLastFullContactSyncTime();
                if (System.currentTimeMillis() - lastFullSync < syncIntervalMs) {
                    Log.i("ContactsSyncWorker.doWork aborting full sync too soon");
                    return Result.success();
                }
            }
            final Result result = ContactsSync.getInstance().performContactSync(fullSync, Arrays.asList(contactHashes));
            if  (!Result.success().equals(result)) {
                Log.sendErrorReport("ContactsSync failed");
            } else {
                @ZeroZoneManager.ZeroZoneState int zeroZoneState = Preferences.getInstance().getZeroZoneState();
                boolean skipOnboarding = false;
                if (zeroZoneState != ZeroZoneManager.ZeroZoneState.NOT_IN_ZERO_ZONE) {
                    if (zeroZoneState == ZeroZoneManager.ZeroZoneState.WAITING_FOR_SYNC) {
                        Preferences.getInstance().setZeroZoneState(ZeroZoneManager.ZeroZoneState.NEEDS_INITIALIZATION);
                        ZeroZoneManager.initialize(getApplicationContext());
                    }
                    boolean hasContactPerms = EasyPermissions.hasPermissions(getApplicationContext(), Manifest.permission.READ_CONTACTS);
                    if (hasContactPerms) {
                        List<Contact> contacts = ContactsDb.getInstance().getUsers();
                        if (contacts != null && contacts.size() > 5) {
                            skipOnboarding = true;
                        }
                    } else {
                        skipOnboarding = true;
                    }
                } else {
                    skipOnboarding = true;
                }
                if (skipOnboarding) {
                    Preferences.getInstance().setCompletedFirstPostOnboarding(true);
                }
            }
            return result;
        }
    }
}

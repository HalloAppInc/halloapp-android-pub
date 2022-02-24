package com.halloapp.contacts;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.provider.BaseColumns;
import android.provider.ContactsContract;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.BuildConfig;
import com.halloapp.R;
import com.halloapp.util.StringUtils;
import com.halloapp.util.logs.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class RawContactDatabase {

    private static final String MESSAGE_MIMETYPE = "vnd.android.cursor.item/vnd.com.halloapp.message";
    private static final String CALL_MIMETYPE = "vnd.android.cursor.item/vnd.com.halloapp.call";

    private final Context context;

    public RawContactDatabase(Context context) {
        this.context = context;
    }

    private boolean hasPermissions() {
        return EasyPermissions.hasPermissions(context, Manifest.permission.WRITE_CONTACTS);
    }

    public void removeRawContacts(@NonNull Collection<String> normalizedNumbers) {
        if (!hasPermissions()) {
            Log.e("RawContactDatabase/removeRawContacts no permissions");
            return;
        }
        Account account = getOrCreateSystemAccount(context);
        if (account == null) {
            Log.e("RawContactDatabase/removeRawContacts no account");
            return;
        }
        HashMap<String, RawHaContact> rawContactMap = getRawContacts(account);
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        for (String normNum : normalizedNumbers) {
            RawHaContact rawHaContact = rawContactMap.get(normNum);
            if (rawHaContact == null) {
                continue;
            }
            removeRawContact(ops, account, rawHaContact.rawId);
        }
        applyOperations(ops);
    }

    public void updateFullSync(Collection<Contact> allContacts) {
        if (!hasPermissions()) {
            Log.e("RawContactDatabase/updateFullSync no permissions");
            return;
        }
        Account account = getOrCreateSystemAccount(context);
        if (account == null) {
            Log.e("RawContactDatabase/updateFullSync no account");
            return;
        }
        HashMap<String, RawHaContact> rawContactMap = getRawContacts(account);
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        final HashSet<String> normalizedNumberSet = new HashSet<>();
        for (Contact contact : allContacts) {
            RawHaContact rawContact = rawContactMap.get(contact.normalizedPhone);
            if (contact.userId == null) {
                if (rawContact != null) {
                    removeRawContact(ops, account, rawContact.rawId);
                }
            } else {
                normalizedNumberSet.add(contact.normalizedPhone);
                if (rawContact == null) {
                    addRawContact(ops, account, contact.normalizedPhone, contact.getDisplayName());
                }
            }
        }
        applyOperations(ops);
        ops.clear();

        for (String normalizedNumber : rawContactMap.keySet()) {
            if (!normalizedNumberSet.contains(normalizedNumber)) {
                RawHaContact rawContact = rawContactMap.get(normalizedNumber);
                if (rawContact != null) {
                    removeRawContact(ops, account, rawContact.rawId);
                }
            }
        }
        applyOperations(ops);
    }

    public void updateRawContacts(@NonNull Collection<Contact> updatedContacts) {
        if (!hasPermissions()) {
            Log.e("RawContactDatabase/updateRawContacts no permissions");
            return;
        }
        Account account = getOrCreateSystemAccount(context);
        if (account == null) {
            Log.e("RawContactDatabase/updateRawContacts no account");
            return;
        }

        HashMap<String, RawHaContact> rawContactMap = getRawContacts(account);

        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        for (Contact updatedContact : updatedContacts) {
            boolean rawContactExists = rawContactMap.containsKey(updatedContact.normalizedPhone);
            if (updatedContact.userId != null) {
                if (!rawContactExists) {
                    addRawContact(ops, account, updatedContact.normalizedPhone, updatedContact.getDisplayName());
                }
            } else {
                RawHaContact rawHaContact = rawContactMap.get(updatedContact.normalizedPhone);
                if (rawHaContact != null) {
                    removeRawContact(ops, account, rawHaContact.rawId);
                }
            }
        }

        applyOperations(ops);
    }

    private void removeRawContact(List<ContentProviderOperation> operations, Account account, long rowId) {
        operations.add(ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI.buildUpon()
                .appendQueryParameter(ContactsContract.RawContacts.ACCOUNT_NAME, account.name)
                .appendQueryParameter(ContactsContract.RawContacts.ACCOUNT_TYPE, account.type)
                .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true").build())
                .withYieldAllowed(true)
                .withSelection(BaseColumns._ID + "=?", new String[]{String.valueOf(rowId)})
                .build());
    }

    private void addRawContact(List<ContentProviderOperation> ops, @NonNull Account account, String normalizedNumber, String displayName) {
        Uri contactUri = ContactsContract.Data.CONTENT_URI.buildUpon()
                .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
                .build();

        String formattedNumber = StringUtils.formatPhoneNumber(normalizedNumber);
        int insertIndex = ops.size();
        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, account.name)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, account.type)
                .withValue(ContactsContract.RawContacts.SYNC1, normalizedNumber)
                .withValue(ContactsContract.RawContacts.SYNC4, String.valueOf(true))
                .build());

        ops.add(ContentProviderOperation.newInsert(contactUri)
                .withValueBackReference(ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID, insertIndex)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, displayName)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .build());

        ops.add(ContentProviderOperation.newInsert(contactUri)
                .withValueBackReference(ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID, insertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, normalizedNumber)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_OTHER)
                .build());

        // Add option for messaging
        ops.add(ContentProviderOperation.newInsert(contactUri)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, insertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, MESSAGE_MIMETYPE)
                .withValue(ContactsContract.Data.DATA1, normalizedNumber)
                .withValue(ContactsContract.Data.DATA2, context.getString(R.string.app_name))
                .withValue(ContactsContract.Data.DATA3, context.getString(R.string.address_book_action_message, formattedNumber))
                .withYieldAllowed(true)
                .build());

        // Add option for voice call
        ops.add(ContentProviderOperation.newInsert(contactUri)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, insertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, CALL_MIMETYPE)
                .withValue(ContactsContract.Data.DATA1, normalizedNumber)
                .withValue(ContactsContract.Data.DATA2, context.getString(R.string.app_name))
                .withValue(ContactsContract.Data.DATA3, context.getString(R.string.address_book_action_call, formattedNumber))
                .withYieldAllowed(true)
                .build());
    }

    private @NonNull HashMap<String, RawHaContact> getRawContacts(@NonNull Account account) {
        Uri rawContactsUri = ContactsContract.RawContacts.CONTENT_URI.buildUpon()
                .appendQueryParameter(ContactsContract.RawContacts.ACCOUNT_NAME, account.name)
                .appendQueryParameter(ContactsContract.RawContacts.ACCOUNT_TYPE, account.type).build();

        HashMap<String, RawHaContact> rawContactMap = new HashMap<>();
        Cursor cursor = null;
        try {
            String[] projection = new String[]{ ContactsContract.RawContacts._ID, ContactsContract.RawContacts.SYNC1 };

            cursor = context.getContentResolver().query(rawContactsUri, projection, null, null, null);
            while (cursor != null && cursor.moveToNext()) {
                long rawContactId = cursor.getLong(0);
                String normalizedPhone = cursor.getString(1);
                rawContactMap.put(normalizedPhone, new RawHaContact(rawContactId));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return rawContactMap;
    }

    public static void deleteRawContactsAccount(Context context) {
        AccountManager accountManager = AccountManager.get(context);
        Account[] accounts = accountManager.getAccountsByType(BuildConfig.APPLICATION_ID);
        if (accounts.length > 0) {
            Account account = accounts[0];
            if (Build.VERSION.SDK_INT >= 22) {
                accountManager.removeAccountExplicitly(account);
            } else {
                accountManager.removeAccount(account, null, new Handler(Looper.getMainLooper()));
            }
        }
    }

    @Nullable
    private static Account getOrCreateSystemAccount(Context context) {
        AccountManager accountManager = AccountManager.get(context);
        Account[] accounts = accountManager.getAccountsByType(BuildConfig.APPLICATION_ID);

        Account account;
        if (accounts.length == 0) {
            account = createAccount(context);
        } else {
            account = accounts[0];
        }
        if (account != null && !ContentResolver.getSyncAutomatically(account, ContactsContract.AUTHORITY)) {
            ContentResolver.setSyncAutomatically(account, ContactsContract.AUTHORITY, true);
        }
        return account;
    }

    @Nullable
    private static Account createAccount(Context context) {
        AccountManager accountManager = AccountManager.get(context);
        Account account = new Account(context.getString(R.string.app_name), BuildConfig.APPLICATION_ID);

        if (accountManager.addAccountExplicitly(account, null, null)) {
            ContentResolver.setIsSyncable(account, ContactsContract.AUTHORITY, 1);
            return account;
        } else {
            return null;
        }
    }

    private void applyOperations(ArrayList<ContentProviderOperation> ops) {
        if (ops.isEmpty()) {
            return;
        }
        ContentResolver cr = context.getContentResolver();
        try {
            cr.applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (OperationApplicationException | RemoteException e) {
            Log.e("RawContactDatabase/applyOperations failed to apply", e);
        }
    }
}

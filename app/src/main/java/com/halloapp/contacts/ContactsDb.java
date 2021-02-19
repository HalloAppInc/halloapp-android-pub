package com.halloapp.contacts;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.halloapp.AppContext;
import com.halloapp.Me;
import com.halloapp.R;
import com.halloapp.id.ChatId;
import com.halloapp.id.UserId;
import com.halloapp.util.logs.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ContactsDb {

    private static ContactsDb instance;

    private final AppContext appContext;
    private final DatabaseHelper databaseHelper;
    private final ExecutorService databaseWriteExecutor = Executors.newSingleThreadExecutor();
    private final Set<Observer> observers = new HashSet<>();

    public interface Observer {
        void onContactsChanged();
        void onContactsReset();
        void onNewFriends(@NonNull Collection<UserId> newFriends);
    }

    public static class BaseObserver implements Observer {

        @Override
        public void onContactsChanged() { }

        @Override
        public void onContactsReset() { }

        @Override
        public void onNewFriends(@NonNull Collection<UserId> newFriends) { }
    }

    public static ContactsDb getInstance() {
        if (instance == null) {
            synchronized(ContactsDb.class) {
                if (instance == null) {
                    instance = new ContactsDb(AppContext.getInstance());
                }
            }
        }
        return instance;
    }

    private ContactsDb(final @NonNull AppContext appContext) {
        this.appContext = appContext;
        this.databaseHelper = new DatabaseHelper(appContext.get().getApplicationContext());
    }

    public void addObserver(Observer observer) {
        synchronized (observers) {
            observers.add(observer);
        }
    }

    public void removeObserver(@NonNull Observer observer) {
        synchronized (observers) {
            observers.remove(observer);
        }
    }

    Future<AddressBookSyncResult> syncAddressBook() {
        return databaseWriteExecutor.submit(() -> {
            final Collection<AddressBookContacts.AddressBookContact> addressBookContacts = AddressBookContacts.getAddressBookContacts(appContext.get());
            if (addressBookContacts == null) {
                return null;
            }
            final AddressBookSyncResult result = new AddressBookSyncResult();
            final ContactsDiff diff = new ContactsDiff();
            final SQLiteDatabase db = databaseHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                final Collection<Contact> dbContacts = getAllContacts();
                diff.calculate(addressBookContacts, dbContacts);

                // update database

                for (AddressBookContacts.AddressBookContact addressBookContact : diff.added) {
                    final ContentValues values = new ContentValues();
                    values.put(ContactsTable.COLUMN_ADDRESS_BOOK_ID, addressBookContact.id);
                    values.put(ContactsTable.COLUMN_ADDRESS_BOOK_NAME, addressBookContact.name);
                    values.put(ContactsTable.COLUMN_ADDRESS_BOOK_PHONE, addressBookContact.phone);
                    long rowId = db.insert(ContactsTable.TABLE_NAME, null, values);
                    result.added.add(new Contact(rowId,
                            addressBookContact.id, addressBookContact.name, addressBookContact.phone,
                            null, null, null, false));
                }

                for (Contact updateContact : diff.updated) {
                    final ContentValues values = new ContentValues();
                    values.put(ContactsTable.COLUMN_ADDRESS_BOOK_NAME, updateContact.addressBookName);
                    values.put(ContactsTable.COLUMN_ADDRESS_BOOK_PHONE, updateContact.addressBookPhone);
                    values.put(ContactsTable.COLUMN_NORMALIZED_PHONE, updateContact.normalizedPhone);
                    values.put(ContactsTable.COLUMN_AVATAR_ID, updateContact.avatarId);
                    values.put(ContactsTable.COLUMN_USER_ID, updateContact.getRawUserId());
                    values.put(ContactsTable.COLUMN_FRIEND, updateContact.friend);
                    values.put(ContactsTable.COLUMN_NUM_POTENTIAL_FRIENDS, updateContact.numPotentialFriends);
                    db.updateWithOnConflict(ContactsTable.TABLE_NAME, values,
                            ContactsTable._ID + "=? ",
                            new String [] {Long.toString(updateContact.rowId)},
                            SQLiteDatabase.CONFLICT_ABORT);
                    result.updated.add(updateContact);
                }
                for (Long id : diff.removedRowIds) {
                    db.delete(ContactsTable.TABLE_NAME,
                            ContactsTable._ID + "=? ",
                            new String [] {Long.toString(id)});
                }

                result.removed.addAll(diff.removedNormalizedPhones);

                Log.i("ContactsDb.syncAddressBook: " + diff);

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            if (!diff.isEmpty()) {
                notifyContactsChanged();
            }
            return result;
        });
    }

    public Future<Void> updateAvatarId(ChatId chatId, String avatarId) {
        return databaseWriteExecutor.submit(() -> {
            final SQLiteDatabase db = databaseHelper.getWritableDatabase();
            db.beginTransaction();
            int updatedRows = 0;
            try {
                final ContentValues values = new ContentValues();
                values.put(ContactsTable.COLUMN_AVATAR_ID, avatarId);
                final int updatedContactRows = db.updateWithOnConflict(ContactsTable.TABLE_NAME, values,
                        ContactsTable.COLUMN_USER_ID + "=? ",
                        new String [] {chatId.rawId()},
                        SQLiteDatabase.CONFLICT_ABORT);
                Log.i("ContactsDb.updateAvatarId: " + updatedContactRows + " rows updated for " + chatId + " " + avatarId);
                updatedRows += updatedContactRows;
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            if (updatedRows > 0) {
                notifyContactsChanged();
            }
            return null;
        });
    }

    public Future<Void> updateContactsServerData(Collection<Contact> updatedContacts, Collection<UserId> newFriends) {
        return databaseWriteExecutor.submit(() -> {
            final SQLiteDatabase db = databaseHelper.getWritableDatabase();
            db.beginTransaction();
            int updatedRows = 0;
            try {
                for (Contact updateContact : updatedContacts) {
                    final ContentValues values = new ContentValues();
                    values.put(ContactsTable.COLUMN_USER_ID, updateContact.getRawUserId());
                    values.put(ContactsTable.COLUMN_NORMALIZED_PHONE, updateContact.normalizedPhone);
                    values.put(ContactsTable.COLUMN_AVATAR_ID, updateContact.avatarId);
                    values.put(ContactsTable.COLUMN_FRIEND, updateContact.friend);
                    values.put(ContactsTable.COLUMN_NEW_CONNECTION, updateContact.newConnection);
                    values.put(ContactsTable.COLUMN_CONNECTION_TIME, updateContact.connectionTime);
                    values.put(ContactsTable.COLUMN_NUM_POTENTIAL_FRIENDS, updateContact.numPotentialFriends);
                    final int updatedContactRows = db.updateWithOnConflict(ContactsTable.TABLE_NAME, values,
                            ContactsTable._ID + "=? ",
                            new String [] {Long.toString(updateContact.rowId)},
                            SQLiteDatabase.CONFLICT_ABORT);
                    Log.i("ContactsDb.updateContactsServerData: " + updatedContactRows + " rows updated for " + updateContact.getDisplayName() + " " + updateContact.normalizedPhone + " " + updateContact.userId + " " + updateContact.avatarId + " " + updateContact.friend);
                    updatedRows += updatedContactRows;
                }
                Log.i("ContactsDb.updateContactsServerData: " + updatedRows + " rows updated for " + updatedContacts.size() + " contacts");
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            if (updatedRows > 0) {
                notifyContactsChanged();
                if (!newFriends.isEmpty()) {
                    notifyNewFriends(newFriends);
                }
            }
            return null;
        });
    }

    public Future<Void> updateNormalizedPhoneData(@NonNull List<NormalizedPhoneData> normalizedPhoneDataList) {
        return databaseWriteExecutor.submit(() -> {
            final List<UserId> newFriends = new ArrayList<>();
            final SQLiteDatabase db = databaseHelper.getWritableDatabase();
            db.beginTransaction();
            int updatedRows = 0;
            try {
                final long syncTime = System.currentTimeMillis();
                for (NormalizedPhoneData normalizedPhoneData : normalizedPhoneDataList) {
                    Contact existing = readContact(normalizedPhoneData.userId);
                    boolean newFriend = normalizedPhoneData.friend && (existing == null || !existing.friend);
                    final ContentValues values = new ContentValues();
                    values.put(ContactsTable.COLUMN_FRIEND, normalizedPhoneData.friend);
                    if (newFriend) {
                        values.put(ContactsTable.COLUMN_CONNECTION_TIME, syncTime);
                        values.put(ContactsTable.COLUMN_NEW_CONNECTION, true);
                    }
                    values.put(ContactsTable.COLUMN_USER_ID, normalizedPhoneData.userId.rawId());
                    values.put(ContactsTable.COLUMN_AVATAR_ID, normalizedPhoneData.avatarId);
                    final int updatedContactRows = db.updateWithOnConflict(ContactsTable.TABLE_NAME, values,
                            ContactsTable.COLUMN_NORMALIZED_PHONE + "=? ",
                            new String [] {normalizedPhoneData.normalizedPhone},
                            SQLiteDatabase.CONFLICT_ABORT);
                    Log.i("ContactsDb.updateNormalizedPhoneData: " + updatedContactRows + " rows updated for " + normalizedPhoneData.normalizedPhone + " " + normalizedPhoneData.userId + " " + normalizedPhoneData.avatarId + " " + normalizedPhoneData.friend);
                    updatedRows += updatedContactRows;
                    if (newFriend) {
                        newFriends.add(normalizedPhoneData.userId);
                    }
                }
                Log.i("ContactsDb.updateNormalizedPhoneData: " + updatedRows + " rows updated for " + normalizedPhoneDataList.size() + " contacts");
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            if (updatedRows > 0) {
                notifyContactsChanged();
                notifyNewFriends(newFriends);
            }
            return null;
        });
    }

    public void markContactSeen(@NonNull UserId userId) {
        databaseWriteExecutor.submit(() -> {
            final SQLiteDatabase db = databaseHelper.getWritableDatabase();
            final ContentValues values = new ContentValues();
            values.put(ContactsTable.COLUMN_NEW_CONNECTION, false);
            int updatedRows = db.update(ContactsTable.TABLE_NAME, values, ContactsTable.COLUMN_USER_ID + "=?", new String[] {userId.rawId()});
            Log.i("ContactsDb.markContactSeen " + updatedRows + " rows updated");
            if (updatedRows > 0) {
                notifyContactsChanged();
            }
        });
    }

    public Future<Void> updateUserNames(@NonNull Map<UserId, String> names) {
        return databaseWriteExecutor.submit(() -> {
            boolean updated = false;
            final SQLiteDatabase db = databaseHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                for (Map.Entry<UserId, String> user : names.entrySet()) {
                    String currentName = null;
                    try (final Cursor cursor = db.query(NamesTable.TABLE_NAME,
                            new String[] { NamesTable.COLUMN_NAME },
                            NamesTable.COLUMN_USER_ID + "=?",
                            new String [] {user.getKey().rawId()}, null, null, null, "1")) {
                        if (cursor.moveToNext()) {
                            currentName = cursor.getString(0);
                        }
                    }
                    if (!Objects.equals(currentName, user.getValue())) {
                        final ContentValues values = new ContentValues();
                        values.put(NamesTable.COLUMN_NAME, user.getValue());
                        final int updatedRowsCount = db.updateWithOnConflict(NamesTable.TABLE_NAME, values,
                                NamesTable.COLUMN_USER_ID + "=? ",
                                new String[]{user.getKey().rawId()},
                                SQLiteDatabase.CONFLICT_ABORT);
                        if (updatedRowsCount == 0) {
                            values.put(NamesTable.COLUMN_USER_ID, user.getKey().rawId());
                            db.insert(NamesTable.TABLE_NAME, null, values);
                            Log.i("ContactsDb.updateUserNames: name " + user.getValue() + " added for " + user.getKey());
                        }
                        updated = true;
                    }
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            if (updated) {
                notifyContactsChanged();
            }
            return null;
        });
    }

    public Future<Void> updateContactAvatarInfo(@NonNull ContactAvatarInfo contact) {
        return databaseWriteExecutor.submit(() -> {
            final SQLiteDatabase db = databaseHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                final ContentValues values = new ContentValues();
                values.put(AvatarsTable.COLUMN_AVATAR_TIMESTAMP, contact.avatarCheckTimestamp);
                values.put(AvatarsTable.COLUMN_AVATAR_ID, contact.avatarId);
                final int updateRowsCount = db.updateWithOnConflict(AvatarsTable.TABLE_NAME, values,
                        AvatarsTable.COLUMN_CHAT_ID + "=? ",
                        new String [] {contact.chatId.rawId()},
                        SQLiteDatabase.CONFLICT_ABORT);
                if (updateRowsCount == 0) {
                    values.put(AvatarsTable.COLUMN_CHAT_ID, contact.chatId.rawId());
                    db.insert(AvatarsTable.TABLE_NAME, null, values);
                }
                Log.i("ContactsDb.updateContactAvatarInfo " + contact.chatId);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            return null;
        });
    }

    @WorkerThread
    public ContactAvatarInfo getContactAvatarInfo(@NonNull ChatId chatId) {
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.query(AvatarsTable.TABLE_NAME,
                new String[] {
                        AvatarsTable.COLUMN_CHAT_ID,
                        AvatarsTable.COLUMN_AVATAR_TIMESTAMP,
                        AvatarsTable.COLUMN_AVATAR_ID
                },
                AvatarsTable.COLUMN_CHAT_ID + "=?", new String [] {chatId.rawId()}, null, null, null, "1")) {
            if (cursor.moveToNext()) {
                return new ContactAvatarInfo(chatId, cursor.getLong(1), cursor.getString(2));
            }
        }
        return null;
    }

    @WorkerThread
    public @NonNull Contact getContact(@NonNull UserId userId) {
        Contact contact = readContact(userId);
        if (contact == null) {
            final String halloName = readName(userId);
            if (!TextUtils.isEmpty(halloName)) {
                contact = new Contact(userId, null, halloName);
            } else {
                contact = new Contact(userId, null, null);
                contact.fallbackName = appContext.get().getString(R.string.unknown_contact);
            }
        } else if (TextUtils.isEmpty(contact.addressBookName) && TextUtils.isEmpty(contact.addressBookPhone) && TextUtils.isEmpty(contact.halloName)) {
            contact.fallbackName = appContext.get().getString(R.string.unknown_contact);
        }
        return contact;
    }

    @WorkerThread
    public @Nullable Contact readContact(@NonNull UserId userId) {
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.query(ContactsTable.TABLE_NAME,
                new String[] { ContactsTable._ID,
                        ContactsTable.COLUMN_ADDRESS_BOOK_ID,
                        ContactsTable.COLUMN_ADDRESS_BOOK_NAME,
                        ContactsTable.COLUMN_ADDRESS_BOOK_PHONE,
                        ContactsTable.COLUMN_NORMALIZED_PHONE,
                        ContactsTable.COLUMN_AVATAR_ID,
                        ContactsTable.COLUMN_USER_ID,
                        ContactsTable.COLUMN_FRIEND,
                        ContactsTable.COLUMN_NUM_POTENTIAL_FRIENDS
                },
                ContactsTable.COLUMN_USER_ID + "=?",
                new String [] {userId.rawId()}, null, null, null, "1")) {
            if (cursor.moveToNext()) {
                Contact c = new Contact(
                        cursor.getLong(0),
                        cursor.getLong(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        userId,
                        cursor.getInt(7) == 1);
                c.numPotentialFriends = cursor.getLong(8);
                return c;
            }
        }
        return null;
    }

    @WorkerThread
    public @Nullable String readName(@NonNull UserId userId) {
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.query(NamesTable.TABLE_NAME,
                new String[] { NamesTable.COLUMN_NAME },
                NamesTable.COLUMN_USER_ID + "=?",
                new String [] {userId.rawId()}, null, null, null, "1")) {
            if (cursor.moveToNext()) {
                return cursor.getString(0);
            }
        }
        return null;
    }

    @WorkerThread
    List<Contact> getAllContacts() {
        final List<Contact> contacts = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.query(ContactsTable.TABLE_NAME,
                new String[] { ContactsTable._ID,
                        ContactsTable.COLUMN_ADDRESS_BOOK_ID,
                        ContactsTable.COLUMN_ADDRESS_BOOK_NAME,
                        ContactsTable.COLUMN_ADDRESS_BOOK_PHONE,
                        ContactsTable.COLUMN_NORMALIZED_PHONE,
                        ContactsTable.COLUMN_AVATAR_ID,
                        ContactsTable.COLUMN_USER_ID,
                        ContactsTable.COLUMN_FRIEND,
                        ContactsTable.COLUMN_NUM_POTENTIAL_FRIENDS
                },
                ContactsTable.COLUMN_ADDRESS_BOOK_ID + " IS NOT NULL", null, null, null, null, null)) {
            while (cursor.moveToNext()) {
                final String userIdStr = cursor.getString(6);
                final Contact contact = new Contact(
                        cursor.getLong(0),
                        cursor.getLong(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        userIdStr == null ? null : new UserId(userIdStr),
                        cursor.getInt(7) == 1);
                contact.numPotentialFriends = cursor.getLong(8);
                contacts.add(contact);
            }
        }
        Log.i("ContactsDb.getAllContacts: " + contacts.size());
        return contacts;
    }

    @WorkerThread
    public List<Contact> getFriends() {
        final List<Contact> contacts = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.query(ContactsTable.TABLE_NAME,
                new String[] { ContactsTable._ID,
                        ContactsTable.COLUMN_ADDRESS_BOOK_ID,
                        ContactsTable.COLUMN_ADDRESS_BOOK_NAME,
                        ContactsTable.COLUMN_ADDRESS_BOOK_PHONE,
                        ContactsTable.COLUMN_NORMALIZED_PHONE,
                        ContactsTable.COLUMN_AVATAR_ID,
                        ContactsTable.COLUMN_USER_ID,
                        ContactsTable.COLUMN_FRIEND,
                        ContactsTable.COLUMN_NEW_CONNECTION,
                        ContactsTable.COLUMN_CONNECTION_TIME,
                        ContactsTable.COLUMN_HIDE_CHAT
                },
                ContactsTable.COLUMN_FRIEND + "=1" + " AND " + ContactsTable.COLUMN_ADDRESS_BOOK_ID + " IS NOT NULL" + " AND " + ContactsTable.COLUMN_USER_ID + " IS NOT NULL",
                null, null, null, null)) {
            final Set<String> userIds = new HashSet<>();
            while (cursor.moveToNext()) {
                final String userIdStr = cursor.getString(6);
                if (userIdStr != null && userIds.add(userIdStr) && !userIdStr.equals(Me.getInstance().getUser())) {
                    final Contact contact = new Contact(
                            cursor.getLong(0),
                            cursor.getLong(1),
                            cursor.getString(2),
                            cursor.getString(3),
                            cursor.getString(4),
                            cursor.getString(5),
                            new UserId(userIdStr),
                            cursor.getInt(7) == 1);
                    contact.newConnection = cursor.getInt(8) == 1;
                    contact.connectionTime = cursor.getLong(9);
                    contact.hideChat = cursor.getInt(10) == 1;
                    contacts.add(contact);
                }
            }
        }
        Log.i("ContactsDb.getFriends: " + contacts.size());
        return contacts;
    }

    public void hideEmptyChat(UserId userId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ContactsTable.COLUMN_HIDE_CHAT, true);
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        db.update(ContactsTable.TABLE_NAME, contentValues, ContactsTable.COLUMN_USER_ID + "=?", new String[] {userId.rawId()});
    }

    @WorkerThread
    public List<Contact> getUniqueContactsWithPhones() {
        final List<Contact> contacts = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.query(ContactsTable.TABLE_NAME,
                new String[] { ContactsTable._ID,
                        ContactsTable.COLUMN_ADDRESS_BOOK_ID,
                        ContactsTable.COLUMN_ADDRESS_BOOK_NAME,
                        ContactsTable.COLUMN_ADDRESS_BOOK_PHONE,
                        ContactsTable.COLUMN_NORMALIZED_PHONE,
                        ContactsTable.COLUMN_AVATAR_ID,
                        ContactsTable.COLUMN_USER_ID,
                        ContactsTable.COLUMN_FRIEND,
                        ContactsTable.COLUMN_NUM_POTENTIAL_FRIENDS
                },
                ContactsTable.COLUMN_ADDRESS_BOOK_ID + " IS NOT NULL",
                null, null, null, null)) {
            final Set<String> addressIdSet = new HashSet<>();
            final Set<String> phoneNumberSet = new HashSet<>();
            while (cursor.moveToNext()) {
                final String addressBookIdStr = cursor.getString(1);
                final String phoneNumberStr = cursor.getString(4);
                if (addressBookIdStr != null && phoneNumberStr != null && addressIdSet.add(addressBookIdStr) && phoneNumberSet.add(phoneNumberStr)) {
                    final String userIdStr = cursor.getString(6);
                    final Contact contact = new Contact(
                            cursor.getLong(0),
                            cursor.getLong(1),
                            cursor.getString(2),
                            cursor.getString(3),
                            cursor.getString(4),
                            cursor.getString(5),
                            userIdStr == null ? null : new UserId(userIdStr),
                            cursor.getInt(7) == 1);
                    contact.numPotentialFriends = cursor.getLong(8);
                    contacts.add(contact);
                }
            }
        }
        Log.i("ContactsDb.getNonUsers: " + contacts.size());
        return contacts;
    }

    @WorkerThread
    public List<Contact> getUsers() {
        final List<Contact> contacts = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.query(ContactsTable.TABLE_NAME,
                new String[] { ContactsTable._ID,
                        ContactsTable.COLUMN_ADDRESS_BOOK_ID,
                        ContactsTable.COLUMN_ADDRESS_BOOK_NAME,
                        ContactsTable.COLUMN_ADDRESS_BOOK_PHONE,
                        ContactsTable.COLUMN_NORMALIZED_PHONE,
                        ContactsTable.COLUMN_AVATAR_ID,
                        ContactsTable.COLUMN_USER_ID,
                        ContactsTable.COLUMN_FRIEND
                },
                ContactsTable.COLUMN_USER_ID + " IS NOT NULL AND " + ContactsTable.COLUMN_ADDRESS_BOOK_ID + " IS NOT NULL",
                null, null, null, null)) {
            final Set<String> userIds = new HashSet<>();
            while (cursor.moveToNext()) {
                final String userIdStr = cursor.getString(6);
                if (userIdStr != null && userIds.add(userIdStr) && !userIdStr.equals(Me.getInstance().getUser())) {
                    final Contact contact = new Contact(
                            cursor.getLong(0),
                            cursor.getLong(1),
                            cursor.getString(2),
                            cursor.getString(3),
                            cursor.getString(4),
                            cursor.getString(5),
                            new UserId(userIdStr),
                            cursor.getInt(7) == 1);
                    contacts.add(contact);
                }
            }
        }
        Log.i("ContactsDb.getUsers: " + contacts.size());
        return contacts;
    }

    @WorkerThread
    public List<UserId> getFeedExclusionList() {
        final List<UserId> exclusionList = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.rawQuery(
                "SELECT " +
                        FeedExcludedTable.TABLE_NAME + "." + FeedExcludedTable.COLUMN_USER_ID + ", " +
                        ContactsTable.COLUMN_ADDRESS_BOOK_ID +
                     " FROM " + FeedExcludedTable.TABLE_NAME +
                     " LEFT JOIN " + ContactsTable.TABLE_NAME + " ON " +
                        FeedExcludedTable.TABLE_NAME + "." + FeedExcludedTable.COLUMN_USER_ID + "=" + ContactsTable.TABLE_NAME + "." + ContactsTable.COLUMN_USER_ID +
                     " WHERE " + ContactsTable.TABLE_NAME + "." + ContactsTable.COLUMN_ADDRESS_BOOK_ID + " IS NOT NULL", null)) {
            final Set<String> userIds = new HashSet<>();
            while (cursor.moveToNext()) {
                final String userIdStr = cursor.getString(0);
                if (userIdStr != null && userIds.add(userIdStr)) {
                    exclusionList.add(new UserId(userIdStr));
                }
            }
        }
        return exclusionList;
    }

    @WorkerThread
    public void setFeedExclusionList(@Nullable List<UserId> feedSelectionList) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        db.delete(FeedExcludedTable.TABLE_NAME, null, null);
        if (feedSelectionList != null) {
            for (UserId selectedUser : feedSelectionList) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(FeedExcludedTable.COLUMN_USER_ID, selectedUser.rawId());
                db.insert(FeedExcludedTable.TABLE_NAME, null, contentValues);
            }
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    @WorkerThread
    public void updateFeedExclusionList(@Nullable List<UserId> added, @Nullable List<UserId> removed) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        if (added != null) {
            for (UserId addedUser : added) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(FeedExcludedTable.COLUMN_USER_ID, addedUser.rawId());
                db.insert(FeedExcludedTable.TABLE_NAME, null, contentValues);
            }
        }
        if (removed != null) {
            for (UserId removedUser : removed) {
                db.delete(FeedExcludedTable.TABLE_NAME, FeedExcludedTable.COLUMN_USER_ID + "=?", new String[] {removedUser.rawId()});
            }
        }
    }

    @WorkerThread
    public List<UserId> getFeedShareList() {
        final List<UserId> shareList = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.rawQuery(
                "SELECT " +
                        FeedSelectedTable.TABLE_NAME + "." + FeedSelectedTable.COLUMN_USER_ID + ", " +
                        ContactsTable.COLUMN_ADDRESS_BOOK_ID +
                     " FROM " + FeedSelectedTable.TABLE_NAME +
                     " LEFT JOIN " + ContactsTable.TABLE_NAME + " ON " +
                        FeedSelectedTable.TABLE_NAME + "." + FeedSelectedTable.COLUMN_USER_ID + "=" + ContactsTable.TABLE_NAME + "." + ContactsTable.COLUMN_USER_ID +
                     " WHERE " + ContactsTable.TABLE_NAME + "." + ContactsTable.COLUMN_ADDRESS_BOOK_ID + " IS NOT NULL", null)) {
            final Set<String> userIds = new HashSet<>();
            while (cursor.moveToNext()) {
                final String userIdStr = cursor.getString(0);
                if (userIdStr != null && userIds.add(userIdStr)) {
                    shareList.add(new UserId(userIdStr));
                }
            }
        }
        return shareList;
    }

    @WorkerThread
    public void setFeedShareList(@Nullable List<UserId> feedShareList) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        db.delete(FeedSelectedTable.TABLE_NAME, null, null);
        if (feedShareList != null) {
            for (UserId selectedUser : feedShareList) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(FeedSelectedTable.COLUMN_USER_ID, selectedUser.rawId());
                db.insert(FeedSelectedTable.TABLE_NAME, null, contentValues);
            }
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    @WorkerThread
    public void updateFeedShareList(@Nullable List<UserId> added, @Nullable List<UserId> removed) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        if (added != null) {
            for (UserId addedUser : added) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(FeedSelectedTable.COLUMN_USER_ID, addedUser.rawId());
                db.insert(FeedSelectedTable.TABLE_NAME, null, contentValues);
            }
        }
        if (removed != null) {
            for (UserId removedUser : removed) {
                db.delete(FeedSelectedTable.TABLE_NAME, FeedSelectedTable.COLUMN_USER_ID + "=?", new String[] {removedUser.rawId()});
            }
        }
    }

    @WorkerThread
    public void addUserToBlockList(@NonNull UserId userId) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(BlocklistTable.COLUMN_USER_ID, userId.rawId());
        db.insert(BlocklistTable.TABLE_NAME, null, contentValues);
    }

    @WorkerThread
    public void removeUserFromBlockList(@NonNull UserId userId) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.delete(BlocklistTable.TABLE_NAME, BlocklistTable.COLUMN_USER_ID + "=?", new String[] {userId.rawId()});
    }

    @WorkerThread
    public void setBlockList(@Nullable List<UserId> blocklist) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        db.delete(BlocklistTable.TABLE_NAME, null, null);
        if (blocklist != null) {
            for (UserId blockUser : blocklist) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(BlocklistTable.COLUMN_USER_ID, blockUser.rawId());
                db.insert(BlocklistTable.TABLE_NAME, null, contentValues);
            }
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    @WorkerThread
    public List<UserId> getBlockList() {
        final List<UserId> blocklist = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.query(BlocklistTable.TABLE_NAME,
                new String[] { BlocklistTable._ID, BlocklistTable.COLUMN_USER_ID},
                null, null, null, null, null
                )) {
            final Set<String> userIds = new HashSet<>();
            while (cursor.moveToNext()) {
                final String userIdStr = cursor.getString(1);
                if (userIdStr != null && userIds.add(userIdStr)) {
                    blocklist.add(new UserId(userIdStr));
                }
            }
        }
        return blocklist;
    }

    private void notifyNewFriends(@NonNull Collection<UserId> newFriends) {
        synchronized (observers) {
            for (Observer observer : observers) {
                observer.onNewFriends(newFriends);
            }
        }
    }

    private void notifyContactsChanged() {
        synchronized (observers) {
            for (Observer observer : observers) {
                observer.onContactsChanged();
            }
        }
    }

    private void notifyContactsReset() {
        synchronized (observers) {
            for (Observer observer : observers) {
                observer.onContactsReset();
            }
        }
    }

    public void deleteDb() {
        databaseHelper.deleteDb();
    }

    // dont't store anything other than address book contacts in this table
    private static final class ContactsTable implements BaseColumns {

        private ContactsTable() { }

        static final String TABLE_NAME = "contacts";

        static final String INDEX_USER_ID = "contacts_user_id_index";

        static final String COLUMN_ADDRESS_BOOK_ID = "address_book_id";
        static final String COLUMN_ADDRESS_BOOK_NAME = "address_book_name";
        static final String COLUMN_ADDRESS_BOOK_PHONE = "address_book_phone";
        static final String COLUMN_NORMALIZED_PHONE = "normalized_phone";
        static final String COLUMN_AVATAR_ID = "avatar_id";
        static final String COLUMN_USER_ID = "user_id";
        static final String COLUMN_FRIEND = "friend";
        static final String COLUMN_NEW_CONNECTION = "new_connection";
        static final String COLUMN_CONNECTION_TIME = "connection_time";
        static final String COLUMN_NUM_POTENTIAL_FRIENDS = "num_potential_friends";
        static final String COLUMN_HIDE_CHAT = "hide_chat";
    }

    private static final class AvatarsTable implements BaseColumns {

        private AvatarsTable() { }

        static final String TABLE_NAME = "avatars";

        static final String INDEX_USER_ID = "avatars_user_id_index";

        static final String COLUMN_CHAT_ID = "user_id";
        static final String COLUMN_AVATAR_TIMESTAMP = "avatar_timestamp";
        static final String COLUMN_AVATAR_ID = "avatar_hash";
    }

    // table for user-defined names
    private static final class NamesTable implements BaseColumns {

        private NamesTable() { }

        static final String TABLE_NAME = "names";

        static final String INDEX_USER_ID = "names_user_id_index";

        static final String COLUMN_USER_ID = "user_id";
        static final String COLUMN_NAME = "name";
    }

    private static final class BlocklistTable implements BaseColumns {
        private BlocklistTable() { }

        static final String TABLE_NAME = "block_list";

        static final String INDEX_USER_ID = "block_list_user_id_index";

        static final String COLUMN_USER_ID = "user_id";
    }

    private static final class FeedExcludedTable implements BaseColumns {
        private FeedExcludedTable() { }

        static final String TABLE_NAME = "feed_exclude_list";

        static final String INDEX_USER_ID = "feed_excluded_user_id_index";

        static final String COLUMN_USER_ID = "user_id";
    }

    private static final class FeedSelectedTable implements BaseColumns {
        private FeedSelectedTable() { }

        static final String TABLE_NAME = "feed_selected_list";

        static final String INDEX_USER_ID = "feed_selected_user_id_index";

        static final String COLUMN_USER_ID = "user_id";
    }

    private class DatabaseHelper extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = "contacts.db";
        private static final int DATABASE_VERSION = 11;

        DatabaseHelper(final @NonNull Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            setWriteAheadLoggingEnabled(true);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + ContactsTable.TABLE_NAME);
            db.execSQL("CREATE TABLE " + ContactsTable.TABLE_NAME + " ("
                    + ContactsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + ContactsTable.COLUMN_ADDRESS_BOOK_ID + " INTEGER NOT NULL,"
                    + ContactsTable.COLUMN_ADDRESS_BOOK_NAME + " TEXT,"
                    + ContactsTable.COLUMN_ADDRESS_BOOK_PHONE + " TEXT,"
                    + ContactsTable.COLUMN_NORMALIZED_PHONE + " TEXT,"
                    + ContactsTable.COLUMN_AVATAR_ID + " TEXT,"
                    + ContactsTable.COLUMN_USER_ID + " TEXT,"
                    + ContactsTable.COLUMN_FRIEND + " INTEGER,"
                    + ContactsTable.COLUMN_NEW_CONNECTION + " INTEGER,"
                    + ContactsTable.COLUMN_CONNECTION_TIME + " INTEGER,"
                    + ContactsTable.COLUMN_NUM_POTENTIAL_FRIENDS + " INTEGER,"
                    + ContactsTable.COLUMN_HIDE_CHAT + " INTEGER"
                    + ");");

            db.execSQL("DROP INDEX IF EXISTS " + ContactsTable.INDEX_USER_ID);
            db.execSQL("CREATE INDEX " + ContactsTable.INDEX_USER_ID + " ON " + ContactsTable.TABLE_NAME + " ("
                    + ContactsTable.COLUMN_USER_ID
                    + ");");

            db.execSQL("DROP TABLE IF EXISTS " + AvatarsTable.TABLE_NAME);
            db.execSQL("CREATE TABLE " + AvatarsTable.TABLE_NAME + " ("
                    + AvatarsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + AvatarsTable.COLUMN_CHAT_ID + " TEXT NOT NULL,"
                    + AvatarsTable.COLUMN_AVATAR_TIMESTAMP + " INTEGER,"
                    + AvatarsTable.COLUMN_AVATAR_ID + " TEXT"
                    + ");");

            db.execSQL("DROP INDEX IF EXISTS " + AvatarsTable.INDEX_USER_ID);
            db.execSQL("CREATE UNIQUE INDEX " + AvatarsTable.INDEX_USER_ID + " ON " + AvatarsTable.TABLE_NAME + " ("
                    + AvatarsTable.COLUMN_CHAT_ID
                    + ");");

            db.execSQL("DROP TABLE IF EXISTS " + NamesTable.TABLE_NAME);
            db.execSQL("CREATE TABLE " + NamesTable.TABLE_NAME + " ("
                    + NamesTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + NamesTable.COLUMN_USER_ID + " TEXT NOT NULL,"
                    + NamesTable.COLUMN_NAME + " TEXT"
                    + ");");

            db.execSQL("DROP INDEX IF EXISTS " + NamesTable.INDEX_USER_ID);
            db.execSQL("CREATE UNIQUE INDEX " + NamesTable.INDEX_USER_ID + " ON " + NamesTable.TABLE_NAME + " ("
                    + NamesTable.COLUMN_USER_ID
                    + ");");

            db.execSQL("DROP TABLE IF EXISTS " + BlocklistTable.TABLE_NAME);
            db.execSQL("CREATE TABLE " + BlocklistTable.TABLE_NAME + " ("
                    + BlocklistTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + BlocklistTable.COLUMN_USER_ID + " TEXT NOT NULL"
                    + ");");

            db.execSQL("DROP INDEX IF EXISTS " + BlocklistTable.INDEX_USER_ID);
            db.execSQL("CREATE UNIQUE INDEX " + BlocklistTable.INDEX_USER_ID + " ON " + BlocklistTable.TABLE_NAME + " ("
                    + BlocklistTable.COLUMN_USER_ID
                    + ");");

            upgradeFromVersion7(db);

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            switch (oldVersion) {
                case 4: {
                    upgradeFromVersion4(db);
                    break;
                }
                case 5: {
                    upgradeFromVersion5(db);
                    // fallthrough
                }
                case 6: {
                    upgradeFromVersion6(db);
                    // fallthrough
                }
                case 7: {
                    upgradeFromVersion7(db);
                }
                case 8: {
                    upgradeFromVersion8(db);
                }
                case 9: {
                    upgradeFromVersion9(db);
                }
                case 10: {
                    upgradeFromVersion10(db);
                }
                break;
                default: {
                    onCreate(db);
                    notifyContactsReset();
                    break;
                }
            }
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onCreate(db);
            notifyContactsReset();
        }

        private void upgradeFromVersion4(SQLiteDatabase db) {
            final List<Contact> contacts = new ArrayList<>();
            final Map<UserId, String> names = new HashMap<>();
            final Map<UserId, ContactAvatarInfo> avatars = new HashMap<>();
            try (final Cursor cursor = db.query(ContactsTable.TABLE_NAME,
                    new String[]{ContactsTable._ID,
                            ContactsTable.COLUMN_ADDRESS_BOOK_ID,
                            ContactsTable.COLUMN_ADDRESS_BOOK_NAME,
                            ContactsTable.COLUMN_ADDRESS_BOOK_PHONE,
                            ContactsTable.COLUMN_NORMALIZED_PHONE,
                            ContactsTable.COLUMN_USER_ID,
                            ContactsTable.COLUMN_FRIEND,
                            "hallo_name",
                            "avatar_timestamp",
                            "avatar_hash"
                    },
                    null, null, null, null, null)) {
                final Set<String> userIds = new HashSet<>();
                while (cursor.moveToNext()) {
                    final String userIdStr = cursor.getString(5);
                    final UserId userId = userIdStr == null ? null : new UserId(userIdStr);
                    final Contact contact = new Contact(
                        cursor.getLong(0),
                        cursor.getLong(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        null,
                        userId,
                        cursor.getInt(6) == 1);
                    contacts.add(contact);
                    if (userId != null) {
                        final String name = cursor.getString(7);
                        if (!TextUtils.isEmpty(name)) {
                            names.put(userId, name);
                        }
                        final String avatarHash = cursor.getString(9);
                        if (!TextUtils.isEmpty(avatarHash)) {
                            avatars.put(userId, new ContactAvatarInfo(userId, cursor.getLong(8), avatarHash));
                        }
                    }
                }
            }

            onCreate(db);

            for (Contact contact : contacts) {
                final ContentValues values = new ContentValues();
                values.put(ContactsTable.COLUMN_ADDRESS_BOOK_ID, contact.addressBookId);
                values.put(ContactsTable.COLUMN_ADDRESS_BOOK_NAME, contact.addressBookName);
                values.put(ContactsTable.COLUMN_ADDRESS_BOOK_PHONE, contact.addressBookPhone);
                values.put(ContactsTable.COLUMN_NORMALIZED_PHONE, contact.normalizedPhone);
                values.put(ContactsTable.COLUMN_AVATAR_ID, contact.avatarId);
                values.put(ContactsTable.COLUMN_USER_ID, contact.getRawUserId());
                values.put(ContactsTable.COLUMN_FRIEND, contact.friend);
                db.insert(ContactsTable.TABLE_NAME, null, values);
            }

            for (Map.Entry<UserId, String> name : names.entrySet()) {
                final ContentValues values = new ContentValues();
                values.put(NamesTable.COLUMN_USER_ID, name.getKey().rawId());
                values.put(NamesTable.COLUMN_NAME, name.getValue());
                db.insert(NamesTable.TABLE_NAME, null, values);
            }

            for (ContactAvatarInfo avatar : avatars.values()) {
                final ContentValues values = new ContentValues();
                values.put(AvatarsTable.COLUMN_CHAT_ID, avatar.chatId.rawId());
                values.put(AvatarsTable.COLUMN_AVATAR_ID, avatar.avatarId);
                values.put(AvatarsTable.COLUMN_AVATAR_TIMESTAMP, avatar.avatarCheckTimestamp);
                db.insert(AvatarsTable.TABLE_NAME, null, values);
            }
        }

        private void upgradeFromVersion5(SQLiteDatabase db) {
            db.execSQL("ALTER TABLE " + ContactsTable.TABLE_NAME + " ADD COLUMN " + ContactsTable.COLUMN_AVATAR_ID + " TEXT");
        }

        private void upgradeFromVersion6(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + BlocklistTable.TABLE_NAME);
            db.execSQL("CREATE TABLE " + BlocklistTable.TABLE_NAME + " ("
                    + BlocklistTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + BlocklistTable.COLUMN_USER_ID + " TEXT NOT NULL"
                    + ");");

            db.execSQL("DROP INDEX IF EXISTS " + BlocklistTable.INDEX_USER_ID);
            db.execSQL("CREATE UNIQUE INDEX " + BlocklistTable.INDEX_USER_ID + " ON " + BlocklistTable.TABLE_NAME + " ("
                    + BlocklistTable.COLUMN_USER_ID
                    + ");");
        }

        private void upgradeFromVersion7(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + FeedExcludedTable.TABLE_NAME);
            db.execSQL("CREATE TABLE " + FeedExcludedTable.TABLE_NAME + " ("
                    + FeedExcludedTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + FeedExcludedTable.COLUMN_USER_ID + " TEXT NOT NULL"
                    + ");");

            db.execSQL("DROP INDEX IF EXISTS " + FeedExcludedTable.INDEX_USER_ID);
            db.execSQL("CREATE UNIQUE INDEX " + FeedExcludedTable.INDEX_USER_ID + " ON " + FeedExcludedTable.TABLE_NAME + " ("
                    + FeedExcludedTable.COLUMN_USER_ID
                    + ");");

            db.execSQL("DROP TABLE IF EXISTS " + FeedSelectedTable.TABLE_NAME);
            db.execSQL("CREATE TABLE " + FeedSelectedTable.TABLE_NAME + " ("
                    + FeedSelectedTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + FeedSelectedTable.COLUMN_USER_ID + " TEXT NOT NULL"
                    + ");");

            db.execSQL("DROP INDEX IF EXISTS " + FeedSelectedTable.INDEX_USER_ID);
            db.execSQL("CREATE UNIQUE INDEX " + FeedSelectedTable.INDEX_USER_ID + " ON " + FeedSelectedTable.TABLE_NAME + " ("
                    + FeedSelectedTable.COLUMN_USER_ID
                    + ");");
        }

        private void upgradeFromVersion8(SQLiteDatabase db) {
            db.execSQL("ALTER TABLE " + ContactsTable.TABLE_NAME + " ADD COLUMN " + ContactsTable.COLUMN_NEW_CONNECTION + " INTEGER");
            db.execSQL("ALTER TABLE " + ContactsTable.TABLE_NAME + " ADD COLUMN " + ContactsTable.COLUMN_CONNECTION_TIME + " INTEGER");
        }

        private void upgradeFromVersion9(SQLiteDatabase db) {
            db.execSQL("ALTER TABLE " + ContactsTable.TABLE_NAME + " ADD COLUMN " + ContactsTable.COLUMN_NUM_POTENTIAL_FRIENDS + " INTEGER");
        }

        private void upgradeFromVersion10(SQLiteDatabase db) {
            db.execSQL("ALTER TABLE " + ContactsTable.TABLE_NAME + " ADD COLUMN " + ContactsTable.COLUMN_HIDE_CHAT + " INTEGER");
        }

        private void deleteDb() {
            close();
            final File dbFile = appContext.get().getDatabasePath(getDatabaseName());
            if (!dbFile.delete()) {
                Log.e("ContactsDb: cannot delete " + dbFile.getAbsolutePath());
            }
            final File walFile = new File(dbFile.getAbsolutePath() + "-wal");
            if (walFile.exists() && !walFile.delete()) {
                Log.e("ContactsDb: cannot delete " + walFile.getAbsolutePath());
            }
            final File shmFile = new File(dbFile.getAbsolutePath() + "-shm");
            if (shmFile.exists() && !shmFile.delete()) {
                Log.e("ContactsDb: cannot delete " + shmFile.getAbsolutePath());
            }
        }
    }

    public static class NormalizedPhoneData {
        private final String normalizedPhone;
        private final UserId userId;
        private final boolean friend;
        private final String avatarId;

        public NormalizedPhoneData(@NonNull String normalizedPhone, @NonNull UserId userId, boolean friend, String avatarId) {
            this.normalizedPhone = normalizedPhone;
            this.userId = userId;
            this.friend = friend;
            this.avatarId = avatarId;
        }
    }

    static class AddressBookSyncResult {
        final Collection<Contact> added = new ArrayList<>();
        final Collection<Contact> updated = new ArrayList<>();
        final Collection<String> removed = new HashSet<>();

        public boolean isEmpty() {
            return added.isEmpty() && updated.isEmpty() && removed.isEmpty();
        }
    }

    public static class ContactAvatarInfo {
        public final ChatId chatId;
        public long avatarCheckTimestamp;
        public String avatarId;

        public ContactAvatarInfo(ChatId chatId, long avatarCheckTimestamp, String avatarId) {
            this.chatId = chatId;
            this.avatarCheckTimestamp = avatarCheckTimestamp;
            this.avatarId = avatarId;
        }
    }
}

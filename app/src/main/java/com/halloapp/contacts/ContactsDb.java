package com.halloapp.contacts;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.text.TextUtils;

import androidx.annotation.AnyThread;
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
        void onNewContacts(@NonNull Collection<UserId> newContacts);
        void onSuggestedContactDismissed(long addressBookId);
        void onRelationshipsChanged();
        void onRelationshipRemoved(@NonNull RelationshipInfo relationshipInfo);
        void onFriendshipsChanged(@NonNull FriendshipInfo friendshipInfo);
        void onFriendshipRemoved(@NonNull FriendshipInfo friendshipInfo);
    }

    public static class BaseObserver implements Observer {

        @Override
        public void onContactsChanged() { }

        @Override
        public void onContactsReset() { }

        @Override
        public void onNewContacts(@NonNull Collection<UserId> newContacts) { }

        @Override
        public void onSuggestedContactDismissed(long addressBookId) { }

        @Override
        public void onRelationshipsChanged() { }

        @Override
        public void onRelationshipRemoved(@NonNull RelationshipInfo relationshipInfo) { }

        @Override
        public void onFriendshipsChanged(@NonNull FriendshipInfo friendshipInfo) { }

        @Override
        public void onFriendshipRemoved(@NonNull FriendshipInfo friendshipInfo) { }
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
                            null, null, null));
                }

                for (Contact updateContact : diff.updated) {
                    final ContentValues values = new ContentValues();
                    values.put(ContactsTable.COLUMN_ADDRESS_BOOK_NAME, updateContact.addressBookName);
                    values.put(ContactsTable.COLUMN_ADDRESS_BOOK_PHONE, updateContact.addressBookPhone);
                    values.put(ContactsTable.COLUMN_NORMALIZED_PHONE, updateContact.normalizedPhone);
                    values.put(ContactsTable.COLUMN_AVATAR_ID, updateContact.avatarId);
                    values.put(ContactsTable.COLUMN_USER_ID, updateContact.getRawUserId());
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

    public Future<Void> updateContactsServerData(Collection<Contact> updatedContacts, Collection<UserId> newContacts) {
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
                    values.put(ContactsTable.COLUMN_NUM_POTENTIAL_FRIENDS, updateContact.numPotentialFriends);
                    final int updatedContactRows = db.updateWithOnConflict(ContactsTable.TABLE_NAME, values,
                            ContactsTable._ID + "=? ",
                            new String [] {Long.toString(updateContact.rowId)},
                            SQLiteDatabase.CONFLICT_ABORT);
                    Log.i("ContactsDb.updateContactsServerData: " + updatedContactRows + " rows updated for " + updateContact.getDisplayName() + " " + updateContact.normalizedPhone + " " + updateContact.userId + " " + updateContact.avatarId + " " + updateContact.connectionTime);
                    updatedRows += updatedContactRows;
                    if (updateContact.getRawUserId() != null) {
                        addChatPlaceholderForContact(db, updateContact.getRawUserId(), updateContact.connectionTime, updateContact.newConnection);
                    }
                }
                Log.i("ContactsDb.updateContactsServerData: " + updatedRows + " rows updated for " + updatedContacts.size() + " contacts");
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            if (updatedRows > 0) {
                notifyContactsChanged();
                if (!newContacts.isEmpty()) {
                    notifyNewContacts(newContacts);
                }
            }
            return null;
        });
    }

    public Future<Void> updateNormalizedPhoneData(@NonNull List<NormalizedPhoneData> normalizedPhoneDataList) {
        return databaseWriteExecutor.submit(() -> {
            final List<UserId> newContacts = new ArrayList<>();
            final SQLiteDatabase db = databaseHelper.getWritableDatabase();
            db.beginTransaction();
            int updatedRows = 0;
            try {
                final long syncTime = System.currentTimeMillis();
                for (NormalizedPhoneData normalizedPhoneData : normalizedPhoneDataList) {
                    Contact existing = readContact(normalizedPhoneData.userId);
                    boolean newContact = existing == null ||
                            !Objects.equals(normalizedPhoneData.userId.rawId(), existing.userId == null ? null : existing.userId.rawId());
                    final ContentValues values = new ContentValues();
                    if (newContact) {
                        addChatPlaceholderForContact(db, normalizedPhoneData.userId.rawId(), syncTime, true);
                    }
                    values.put(ContactsTable.COLUMN_USER_ID, normalizedPhoneData.userId.rawId());
                    values.put(ContactsTable.COLUMN_AVATAR_ID, normalizedPhoneData.avatarId);
                    final int updatedContactRows = db.updateWithOnConflict(ContactsTable.TABLE_NAME, values,
                            ContactsTable.COLUMN_NORMALIZED_PHONE + "=? ",
                            new String [] {normalizedPhoneData.normalizedPhone},
                            SQLiteDatabase.CONFLICT_ABORT);
                    Log.i("ContactsDb.updateNormalizedPhoneData: " + updatedContactRows + " rows updated for " + normalizedPhoneData.normalizedPhone + " " + normalizedPhoneData.userId + " " + normalizedPhoneData.avatarId);
                    updatedRows += updatedContactRows;
                    if (newContact) {
                        newContacts.add(normalizedPhoneData.userId);
                    }
                }
                Log.i("ContactsDb.updateNormalizedPhoneData: " + updatedRows + " rows updated for " + normalizedPhoneDataList.size() + " contacts");
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            if (updatedRows > 0) {
                notifyContactsChanged();
                notifyNewContacts(newContacts);
            }
            return null;
        });
    }

    @WorkerThread
    private void addChatPlaceholderForContact(@NonNull SQLiteDatabase db, @NonNull String rawUserId, long timestamp, boolean unseen) {
        final ContentValues values = new ContentValues();
        values.put(ChatsPlaceholderTable.COLUMN_USER_ID, rawUserId);
        values.put(ChatsPlaceholderTable.COLUMN_TIMESTAMP, timestamp);
        values.put(ChatsPlaceholderTable.COLUMN_UNSEEN, unseen);
        values.put(ChatsPlaceholderTable.COLUMN_HIDDEN, false);
        final long rowId = db.insertWithOnConflict(ChatsPlaceholderTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        Log.i("ContactsDb.addChatPlaceholderForContact added placeholder: " + rowId);
    }

    public void markContactSeen(@NonNull UserId userId) {
        databaseWriteExecutor.submit(() -> {
            final SQLiteDatabase db = databaseHelper.getWritableDatabase();
            final ContentValues values = new ContentValues();
            values.put(ChatsPlaceholderTable.COLUMN_UNSEEN, false);
            int updatedRows = db.update(ChatsPlaceholderTable.TABLE_NAME, values, ChatsPlaceholderTable.COLUMN_USER_ID + "=?", new String[] {userId.rawId()});
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

    public Future<Void> updateUserPhones(@NonNull Map<UserId, String> phones) {
        return databaseWriteExecutor.submit(() -> {
            boolean updated = false;
            final SQLiteDatabase db = databaseHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                for (Map.Entry<UserId, String> user : phones.entrySet()) {
                    String currentPhone = null;
                    try (final Cursor cursor = db.query(PhonesTable.TABLE_NAME,
                            new String[] { PhonesTable.COLUMN_PHONE },
                            PhonesTable.COLUMN_USER_ID + "=?",
                            new String [] {user.getKey().rawId()}, null, null, null, "1")) {
                        if (cursor.moveToNext()) {
                            currentPhone = cursor.getString(0);
                        }
                    }
                    if (!Objects.equals(currentPhone, user.getValue())) {
                        final ContentValues values = new ContentValues();
                        values.put(PhonesTable.COLUMN_PHONE, user.getValue());
                        final int updatedRowsCount = db.updateWithOnConflict(PhonesTable.TABLE_NAME, values,
                                PhonesTable.COLUMN_USER_ID + "=? ",
                                new String[]{user.getKey().rawId()},
                                SQLiteDatabase.CONFLICT_ABORT);
                        if (updatedRowsCount == 0) {
                            values.put(PhonesTable.COLUMN_USER_ID, user.getKey().rawId());
                            db.insert(PhonesTable.TABLE_NAME, null, values);
                            Log.i("ContactsDb.updateUserPhones: phone " + user.getValue() + " added for " + user.getKey());
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

    public Future<Void> updateUserUsernames(@NonNull Map<UserId, String> usernames) {
        return databaseWriteExecutor.submit(() -> {
            boolean updated = false;
            final SQLiteDatabase db = databaseHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                for (Map.Entry<UserId, String> user : usernames.entrySet()) {
                    String currentName = null;
                    try (final Cursor cursor = db.query(UsernamesTable.TABLE_NAME,
                            new String[] {UsernamesTable.COLUMN_USERNAME},
                            UsernamesTable.COLUMN_USER_ID + "=?",
                            new String[] {user.getKey().rawId()}, null, null, null, "1")) {
                        if (cursor.moveToNext()) {
                            currentName = cursor.getString(0);
                        }
                    }
                    if (!Objects.equals(currentName, user.getValue())) {
                        final ContentValues values = new ContentValues();
                        values.put(UsernamesTable.COLUMN_USERNAME, user.getValue());
                        final int updatedRowsCount = db.updateWithOnConflict(UsernamesTable.TABLE_NAME, values,
                                UsernamesTable.COLUMN_USER_ID + "=? ",
                                new String[] {user.getKey().rawId()},
                                SQLiteDatabase.CONFLICT_ABORT);
                        if (updatedRowsCount == 0) {
                            values.put(UsernamesTable.COLUMN_USER_ID, user.getKey().rawId());
                            db.insert(UsernamesTable.TABLE_NAME, null, values);
                            Log.i("ContactsDb.updateUserUsernames: username " + user.getValue() + " added for " + user.getKey());
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

    public Future<Void> updateGeotags(@NonNull Map<UserId, String> geotags) {
        return databaseWriteExecutor.submit(() -> {
            boolean updated = false;
            final SQLiteDatabase db = databaseHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                for (Map.Entry<UserId, String> user : geotags.entrySet()) {
                    String currentName = null;
                    try (final Cursor cursor = db.query(GeotagsTable.TABLE_NAME,
                            new String[] { GeotagsTable.COLUMN_GEOTAG },
                            GeotagsTable.COLUMN_USER_ID + "=?",
                            new String [] {user.getKey().rawId()}, null, null, null, "1")) {
                        if (cursor.moveToNext()) {
                            currentName = cursor.getString(0);
                        }
                    }
                    if (!Objects.equals(currentName, user.getValue())) {
                        final ContentValues values = new ContentValues();
                        values.put(GeotagsTable.COLUMN_GEOTAG, user.getValue());
                        final int updatedRowsCount = db.updateWithOnConflict(GeotagsTable.TABLE_NAME, values,
                                GeotagsTable.COLUMN_USER_ID + "=? ",
                                new String[]{user.getKey().rawId()},
                                SQLiteDatabase.CONFLICT_ABORT);
                        if (updatedRowsCount == 0) {
                            values.put(GeotagsTable.COLUMN_USER_ID, user.getKey().rawId());
                            db.insert(GeotagsTable.TABLE_NAME, null, values);
                            Log.i("ContactsDb.updateGeotags: geotag " + user.getValue() + " added for " + user.getKey());
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

    public Future<Void> updateUserAvatars(@NonNull Map<UserId, String> avatars) {
        return databaseWriteExecutor.submit(() -> {
            boolean updated = false;
            final SQLiteDatabase db = databaseHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                for (Map.Entry<UserId, String> avatar : avatars.entrySet()) {
                    ContactAvatarInfo contactAvatarInfo = getContactAvatarInfo(avatar.getKey());
                    if (contactAvatarInfo == null || !Objects.equals(contactAvatarInfo.avatarId, avatar.getValue())) {
                        contactAvatarInfo = new ContactAvatarInfo(avatar.getKey(), 0, avatar.getValue(), null, null);
                        updateContactAvatarInfo(contactAvatarInfo);
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
                values.put(AvatarsTable.COLUMN_REGULAR_CURRENT_ID, contact.regularCurrentId);
                values.put(AvatarsTable.COLUMN_LARGE_CURRENT_ID, contact.largeCurrentId);
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
                        AvatarsTable.COLUMN_AVATAR_ID,
                        AvatarsTable.COLUMN_REGULAR_CURRENT_ID,
                        AvatarsTable.COLUMN_LARGE_CURRENT_ID
                },
                AvatarsTable.COLUMN_CHAT_ID + "=?", new String [] {chatId.rawId()}, null, null, null, "1")) {
            if (cursor.moveToNext()) {
                return new ContactAvatarInfo(chatId, cursor.getLong(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
            }
        }
        return null;
    }

    @WorkerThread
    public @NonNull Contact getContact(@NonNull UserId userId) {
        Contact contact = readContact(userId);
        final String halloName = readName(userId);
        if (contact == null) {
            if (!TextUtils.isEmpty(halloName)) {
                contact = new Contact(userId, null, halloName);
            } else {
                contact = new Contact(userId, null, null);
                contact.fallbackName = appContext.get().getString(R.string.unknown_contact);
            }
        } else {
            contact.halloName = halloName;
            if (TextUtils.isEmpty(contact.addressBookName) && TextUtils.isEmpty(contact.addressBookPhone) && TextUtils.isEmpty(contact.halloName)) {
                contact.fallbackName = appContext.get().getString(R.string.unknown_contact);
            }
        }
        contact.username = readUsername(userId);
        contact.friendshipStatus = readFriendshipStatus(userId);
        return contact;
    }

    @WorkerThread
    public @Nullable UserId getUserForPhoneNumber(@NonNull String normalizedNumber) {
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.query(ContactsTable.TABLE_NAME,
                new String[] { ContactsTable._ID,
                        ContactsTable.COLUMN_NORMALIZED_PHONE,
                        ContactsTable.COLUMN_USER_ID,
                },
                ContactsTable.COLUMN_NORMALIZED_PHONE + "=?",
                new String [] {normalizedNumber}, null, null, null, "1")) {
            if (cursor.moveToNext()) {
                return (UserId) UserId.fromNullable(cursor.getString(2));
            }
        }
        return null;
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
                        userId);
                c.numPotentialFriends = cursor.getLong(7);
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
    public @Nullable String readPhone(@NonNull UserId userId) {
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.query(PhonesTable.TABLE_NAME,
                new String[] { PhonesTable.COLUMN_PHONE },
                PhonesTable.COLUMN_USER_ID + "=?",
                new String [] {userId.rawId()}, null, null, null, "1")) {
            if (cursor.moveToNext()) {
                return cursor.getString(0);
            }
        }
        return null;
    }

    @WorkerThread
    public @Nullable String readUsername(@NonNull UserId userId) {
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.query(UsernamesTable.TABLE_NAME,
                new String[]{UsernamesTable.COLUMN_USERNAME},
                UsernamesTable.COLUMN_USER_ID + "=?",
                new String[]{userId.rawId()}, null, null, null, "1")) {
            if (cursor.moveToNext()) {
                return cursor.getString(0);
            }
        }
        return null;
    }

    @WorkerThread
    public @FriendshipInfo.Type int readFriendshipStatus(@NonNull UserId userId) {
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.query(RelationshipTable.TABLE_NAME,
                new String[]{RelationshipTable.COLUMN_LIST_TYPE},
                RelationshipTable.COLUMN_USER_ID + "=?",
                new String[]{userId.rawId()}, null, null, null, "1")) {
            if (cursor.moveToNext()) {
                return cursor.getInt(0);
            }
        }
        return FriendshipInfo.Type.NONE_STATUS;
    }

    @WorkerThread
    public @Nullable String readGeotag(@NonNull UserId userId) {
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.query(GeotagsTable.TABLE_NAME,
                new String[] { GeotagsTable.COLUMN_GEOTAG },
                GeotagsTable.COLUMN_USER_ID + "=?",
                new String[] {userId.rawId()}, null, null, null, "1")) {
            if (cursor.moveToNext()) {
                return cursor.getString(0);
            }
        }
        return null;
    }

    @WorkerThread
    public List<Contact> getAllContacts() {
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
                        userIdStr == null ? null : new UserId(userIdStr));
                contacts.add(contact);
            }
        }
        Log.i("ContactsDb.getAllContacts: " + contacts.size());
        return contacts;
    }

    public void hideEmptyChat(UserId userId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ChatsPlaceholderTable.COLUMN_HIDDEN, true);
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        db.update(ChatsPlaceholderTable.TABLE_NAME, contentValues, ChatsPlaceholderTable.COLUMN_USER_ID + "=?", new String[] {userId.rawId()});
    }

    public void markInvited(Contact contact) {
        ContentValues values = new ContentValues();
        values.put(ContactsTable.COLUMN_INVITED, true);
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        db.updateWithOnConflict(ContactsTable.TABLE_NAME, values,
                ContactsTable._ID + "=? ",
                new String [] {Long.toString(contact.rowId)},
                SQLiteDatabase.CONFLICT_ABORT);
    }

    @AnyThread
    public void dismissSuggestedContact(Contact contact) {
        databaseWriteExecutor.execute(() -> {
            ContentValues values = new ContentValues();
            values.put(ContactsTable.COLUMN_DONT_SUGGEST, true);
            final SQLiteDatabase db = databaseHelper.getReadableDatabase();
            int rows = db.updateWithOnConflict(ContactsTable.TABLE_NAME, values,
                    ContactsTable._ID + "=? ",
                    new String [] {Long.toString(contact.rowId)},
                    SQLiteDatabase.CONFLICT_ABORT);
            if (rows > 0) {
                notifySuggestedContactDismissed(contact);
            }
        });
    }

    @WorkerThread
    public List<Contact> getSuggestedContactsForInvite() {
        return getSuggestedContactsForInvite(true);
    }

    // TODO(vasil): add support for name prefix search
    @WorkerThread
    public List<Contact> getSuggestedContactsForInvite(boolean shouldHavePotentialFriends) {
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
                        ContactsTable.COLUMN_NUM_POTENTIAL_FRIENDS,
                        ContactsTable.COLUMN_INVITED,
                        ContactsTable.COLUMN_DONT_SUGGEST
                },
                ContactsTable.COLUMN_USER_ID + " IS NULL AND " + ContactsTable.COLUMN_ADDRESS_BOOK_ID + " IS NOT NULL AND " +
                        (shouldHavePotentialFriends ? ContactsTable.COLUMN_NUM_POTENTIAL_FRIENDS + ">0 AND " : "") +
                        "(" + ContactsTable.COLUMN_DONT_SUGGEST + " IS NULL OR " + ContactsTable.COLUMN_DONT_SUGGEST + "!=1)",
                null, null, null, ContactsTable.COLUMN_NUM_POTENTIAL_FRIENDS + " DESC", "50")) {
            final Set<String> addressIdSet = new HashSet<>();
            final Set<String> phoneNumberSet = new HashSet<>();
            while (cursor.moveToNext()) {
                final String addressBookIdStr = cursor.getString(1);
                final String phoneNumberStr = cursor.getString(4);
                if (addressBookIdStr != null && !TextUtils.isEmpty(phoneNumberStr) && addressIdSet.add(addressBookIdStr) && phoneNumberSet.add(phoneNumberStr)) {
                    final String userIdStr = cursor.getString(6);
                    final Contact contact = new Contact(
                            cursor.getLong(0),
                            cursor.getLong(1),
                            cursor.getString(2),
                            cursor.getString(3),
                            cursor.getString(4),
                            cursor.getString(5),
                            userIdStr == null ? null : new UserId(userIdStr));
                    contact.numPotentialFriends = cursor.getLong(7);
                    contact.invited = cursor.getInt(8) == 1;
                    contact.dontSuggest = cursor.getInt(9) == 1;
                    contacts.add(contact);
                }
            }
        }
        Log.i("ContactsDb.getSuggestedContactsForInvite: " + contacts.size());
        return contacts;
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
                        ContactsTable.COLUMN_NUM_POTENTIAL_FRIENDS,
                        ContactsTable.COLUMN_INVITED,
                        ContactsTable.COLUMN_DONT_SUGGEST
                },
                ContactsTable.COLUMN_ADDRESS_BOOK_ID + " IS NOT NULL",
                null, null, null, null)) {
            final Set<String> addressIdSet = new HashSet<>();
            final Set<String> phoneNumberSet = new HashSet<>();
            while (cursor.moveToNext()) {
                final String addressBookIdStr = cursor.getString(1);
                final String phoneNumberStr = cursor.getString(4);
                if (addressBookIdStr != null && !TextUtils.isEmpty(phoneNumberStr) && addressIdSet.add(addressBookIdStr) && phoneNumberSet.add(phoneNumberStr)) {
                    final String userIdStr = cursor.getString(6);
                    final Contact contact = new Contact(
                            cursor.getLong(0),
                            cursor.getLong(1),
                            cursor.getString(2),
                            cursor.getString(3),
                            cursor.getString(4),
                            cursor.getString(5),
                            userIdStr == null ? null : new UserId(userIdStr));
                    contact.numPotentialFriends = cursor.getLong(7);
                    contact.invited = cursor.getInt(8) == 1;
                    contact.dontSuggest = cursor.getInt(9) == 1;
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
                        ContactsTable.COLUMN_USER_ID
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
                            new UserId(userIdStr));
                    contacts.add(contact);
                }
            }
        }
        Log.i("ContactsDb.getUsers: " + contacts.size());
        return contacts;
    }

    @WorkerThread
    public int getContactsCount() {
        final String queryString = "SELECT COUNT(DISTINCT " + ContactsTable.COLUMN_USER_ID + ")" +
                " FROM " + ContactsTable.TABLE_NAME +
                " WHERE " + ContactsTable.COLUMN_USER_ID + " IS NOT NULL AND " + ContactsTable.COLUMN_USER_ID + " != ? AND " + ContactsTable.COLUMN_ADDRESS_BOOK_ID + " IS NOT NULL";
        try (Cursor cursor = databaseHelper.getReadableDatabase().rawQuery(queryString, new String[] {Me.getInstance().getUser()})) {
            if (cursor.moveToNext()) {
                return cursor.getInt(0);
            }
        }
        return 0;
    }

    @WorkerThread
    public int getFriendsCount() {
        final String queryString = "SELECT COUNT(DISTINCT " + RelationshipTable.COLUMN_USER_ID + ")" +
                " FROM " + RelationshipTable.TABLE_NAME +
                " WHERE " + RelationshipTable.COLUMN_USER_ID + " IS NOT NULL AND " + RelationshipTable.COLUMN_USER_ID + " != ? AND " + RelationshipTable.COLUMN_LIST_TYPE + " IS ?";
        try (Cursor cursor = databaseHelper.getReadableDatabase().rawQuery(queryString, new String[] {Me.getInstance().getUser(), String.valueOf(FriendshipInfo.Type.FRIENDS)})) {
            if (cursor.moveToNext()) {
                return cursor.getInt(0);
            }
        }
        return 0;
    }

    @WorkerThread
    public List<UserId> getFeedExclusionListForServer() {
        return getFeedExclusionList(false);
    }

    @WorkerThread
    public List<UserId> getFeedExclusionList() {
        return getFeedExclusionList(true);
    }

    private List<UserId> getFeedExclusionList(boolean addressBookOnly) {
        final List<UserId> exclusionList = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.rawQuery(
                "SELECT " +
                        FeedExcludedTable.TABLE_NAME + "." + FeedExcludedTable.COLUMN_USER_ID + ", " +
                        ContactsTable.COLUMN_ADDRESS_BOOK_ID +
                        " FROM " + FeedExcludedTable.TABLE_NAME +
                        " LEFT JOIN " + ContactsTable.TABLE_NAME + " ON " +
                        FeedExcludedTable.TABLE_NAME + "." + FeedExcludedTable.COLUMN_USER_ID + "=" + ContactsTable.TABLE_NAME + "." + ContactsTable.COLUMN_USER_ID +
                        (addressBookOnly ? (" WHERE " + ContactsTable.TABLE_NAME + "." + ContactsTable.COLUMN_ADDRESS_BOOK_ID + " IS NOT NULL") : "")
                        , null)) {
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
    public List<UserId> getFeedShareListForServer() {
        return getFeedShareList(false);
    }

    @WorkerThread
    public List<UserId> getFeedShareList() {
        return getFeedShareList(true);
    }

    @WorkerThread
    private List<UserId> getFeedShareList(boolean addressBookOnly) {
        final List<UserId> shareList = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.rawQuery(
                "SELECT " +
                        FeedSelectedTable.TABLE_NAME + "." + FeedSelectedTable.COLUMN_USER_ID + ", " +
                        ContactsTable.COLUMN_ADDRESS_BOOK_ID +
                     " FROM " + FeedSelectedTable.TABLE_NAME +
                     " LEFT JOIN " + ContactsTable.TABLE_NAME + " ON " +
                        FeedSelectedTable.TABLE_NAME + "." + FeedSelectedTable.COLUMN_USER_ID + "=" + ContactsTable.TABLE_NAME + "." + ContactsTable.COLUMN_USER_ID +
                        (addressBookOnly ? (" WHERE " + ContactsTable.TABLE_NAME + "." + ContactsTable.COLUMN_ADDRESS_BOOK_ID + " IS NOT NULL") : ""), null)) {
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
    public void addUserToFavorites(@NonNull UserId userId) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(FeedSelectedTable.COLUMN_USER_ID, userId.rawId());
        db.insert(FeedSelectedTable.TABLE_NAME, null, contentValues);
    }

    @WorkerThread
    public void removeUserFromFavorites(@NonNull UserId userId) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.delete(FeedSelectedTable.TABLE_NAME, FeedSelectedTable.COLUMN_USER_ID + "=?", new String[] {userId.rawId()});
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

    @WorkerThread
    @NonNull
    public List<Contact> getPlaceholderChats() {
        final List<Contact> contacts = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();

        final String sql = "SELECT " +
                ContactsTable.TABLE_NAME + "." + ContactsTable._ID + "," +
                ContactsTable.TABLE_NAME + "." + ContactsTable.COLUMN_ADDRESS_BOOK_ID + "," +
                ContactsTable.TABLE_NAME + "." + ContactsTable.COLUMN_ADDRESS_BOOK_NAME + "," +
                ContactsTable.TABLE_NAME + "." + ContactsTable.COLUMN_ADDRESS_BOOK_PHONE + "," +
                ContactsTable.TABLE_NAME + "." + ContactsTable.COLUMN_NORMALIZED_PHONE + "," +
                ContactsTable.TABLE_NAME + "." + ContactsTable.COLUMN_AVATAR_ID + "," +
                ContactsTable.TABLE_NAME + "." + ContactsTable.COLUMN_USER_ID + "," +
                " p." + ChatsPlaceholderTable.COLUMN_UNSEEN + "," +
                " p." + ChatsPlaceholderTable.COLUMN_TIMESTAMP + "," +
                " p." + ChatsPlaceholderTable.COLUMN_HIDDEN +
                " FROM " + ContactsTable.TABLE_NAME + " JOIN " + ChatsPlaceholderTable.TABLE_NAME + " AS p ON "
                + ContactsTable.TABLE_NAME + "." + ContactsTable.COLUMN_USER_ID + "=p." + ChatsPlaceholderTable.COLUMN_USER_ID +
                " WHERE p." + ChatsPlaceholderTable.COLUMN_HIDDEN + "=0 AND "
                + ContactsTable.TABLE_NAME + "." + ContactsTable.COLUMN_USER_ID + " IS NOT NULL AND " + ContactsTable.TABLE_NAME + "." + ContactsTable.COLUMN_ADDRESS_BOOK_ID + " IS NOT NULL";

        try (final Cursor cursor = db.rawQuery(sql, null)) {
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
                            new UserId(userIdStr));
                    contact.newConnection = cursor.getInt(7) == 1;
                    contact.connectionTime = cursor.getLong(8);
                    contact.hideChat = cursor.getInt(9) == 1;
                    contacts.add(contact);
                }
            }
        }
        Log.i("ContactsDb.getUsers: " + contacts.size());
        return contacts;
    }

    @WorkerThread
    @NonNull
    public List<RelationshipInfo> getRelationships(@RelationshipInfo.Type int relationshipType) {
        final List<RelationshipInfo> relationships = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String sql = "SELECT " +
                RelationshipTable._ID + "," +
                RelationshipTable.COLUMN_USER_ID + "," +
                RelationshipTable.COLUMN_USERNAME + "," +
                RelationshipTable.COLUMN_NAME + "," +
                RelationshipTable.COLUMN_AVATAR_ID + "," +
                RelationshipTable.COLUMN_LIST_TYPE + "," +
                RelationshipTable.COLUMN_SEEN + "," +
                RelationshipTable.COLUMN_TIMESTAMP +
                " FROM " + RelationshipTable.TABLE_NAME +
                " WHERE " + RelationshipTable.COLUMN_LIST_TYPE + "=?";

        try (final Cursor cursor = db.rawQuery(sql, new String[] {Integer.toString(relationshipType)})) {
            while (cursor.moveToNext()) {
                RelationshipInfo relationshipInfo = new RelationshipInfo(
                        new UserId(cursor.getString(1)),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getInt(5),
                        cursor.getLong(7)
                );
                relationshipInfo.seen = cursor.getInt(6) == 1;
                relationships.add(relationshipInfo);
            }
        }

        return relationships;
    }

    @WorkerThread
    @Nullable
    public RelationshipInfo getRelationship(@NonNull UserId userId, @RelationshipInfo.Type int relationshipType) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String sql = "SELECT " +
                RelationshipTable._ID + "," +
                RelationshipTable.COLUMN_USER_ID + "," +
                RelationshipTable.COLUMN_USERNAME + "," +
                RelationshipTable.COLUMN_NAME + "," +
                RelationshipTable.COLUMN_AVATAR_ID + "," +
                RelationshipTable.COLUMN_LIST_TYPE + "," +
                RelationshipTable.COLUMN_SEEN + "," +
                RelationshipTable.COLUMN_TIMESTAMP +
                " FROM " + RelationshipTable.TABLE_NAME +
                " WHERE " + RelationshipTable.COLUMN_USER_ID + "=?" +
                " AND " + RelationshipTable.COLUMN_LIST_TYPE + "=?";

        try (final Cursor cursor = db.rawQuery(sql, new String[] {userId.rawId(), Long.toString(relationshipType)})) {
            if (cursor.moveToNext()) {
                RelationshipInfo relationshipInfo = new RelationshipInfo(
                        new UserId(cursor.getString(1)),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getInt(5),
                        cursor.getLong(7)
                );
                relationshipInfo.seen = cursor.getInt(6) == 1;
                return relationshipInfo;
            }
        }

        return null;
    }

    public List<RelationshipInfo> getFollowerHistory(int limit) {
        final List<RelationshipInfo> relationships = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String sql = "SELECT " +
                RelationshipTable._ID + "," +
                RelationshipTable.COLUMN_USER_ID + "," +
                RelationshipTable.COLUMN_USERNAME + "," +
                RelationshipTable.COLUMN_NAME + "," +
                RelationshipTable.COLUMN_AVATAR_ID + "," +
                RelationshipTable.COLUMN_LIST_TYPE + "," +
                RelationshipTable.COLUMN_SEEN + "," +
                RelationshipTable.COLUMN_TIMESTAMP +
                " FROM " + RelationshipTable.TABLE_NAME +
                " WHERE " + RelationshipTable.COLUMN_LIST_TYPE + "=" + RelationshipInfo.Type.FOLLOWER +
                " ORDER BY " + RelationshipTable.COLUMN_TIMESTAMP + " DESC " +
                " LIMIT " + limit;

        try (final Cursor cursor = db.rawQuery(sql, new String[] {})) {
            while (cursor.moveToNext()) {
                RelationshipInfo relationshipInfo = new RelationshipInfo(
                        new UserId(cursor.getString(1)),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getInt(5),
                        cursor.getLong(7)
                );
                relationshipInfo.seen = cursor.getInt(6) == 1;
                relationships.add(relationshipInfo);
            }
        }

        return relationships;
    }

    @WorkerThread
    public void addRelationship(@NonNull RelationshipInfo relationship) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(RelationshipTable.COLUMN_USER_ID, relationship.userId.rawId());
        values.put(RelationshipTable.COLUMN_USERNAME, relationship.username);
        values.put(RelationshipTable.COLUMN_NAME, relationship.name);
        values.put(RelationshipTable.COLUMN_AVATAR_ID, relationship.avatarId);
        values.put(RelationshipTable.COLUMN_LIST_TYPE, relationship.relationshipType);
        values.put(RelationshipTable.COLUMN_TIMESTAMP, relationship.timestamp);

        db.insert(RelationshipTable.TABLE_NAME, null, values);

        notifyRelationshipsChanged();
    }

    @WorkerThread
    public void removeRelationship(@NonNull RelationshipInfo relationship) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        db.delete(RelationshipTable.TABLE_NAME,
                RelationshipTable.COLUMN_USER_ID + "=? AND " + RelationshipTable.COLUMN_LIST_TYPE + "=?",
                new String[] {relationship.userId.rawId(), Integer.toString(relationship.relationshipType)});

        notifyRelationshipRemoved(relationship);
        notifyRelationshipsChanged();
    }

    @WorkerThread
    public void updateRelationship(@NonNull RelationshipInfo relationship) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(RelationshipTable.COLUMN_USERNAME, relationship.username);
        values.put(RelationshipTable.COLUMN_NAME, relationship.name);
        values.put(RelationshipTable.COLUMN_AVATAR_ID, relationship.avatarId);

        db.update(RelationshipTable.TABLE_NAME,
                values,
                RelationshipTable.COLUMN_USER_ID + "=? AND " + RelationshipTable.COLUMN_LIST_TYPE + "=?",
                new String[] {relationship.userId.rawId(), Integer.toString(relationship.relationshipType)});

        notifyRelationshipsChanged();
    }

    @WorkerThread
    public void markFollowersSeen() {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(RelationshipTable.COLUMN_SEEN, true);

        db.update(RelationshipTable.TABLE_NAME,
                values,
                RelationshipTable.COLUMN_LIST_TYPE + "=?",
                new String[] {Integer.toString(RelationshipInfo.Type.FOLLOWER)});
    }

    @WorkerThread
    public int getUnseenFollowerCount() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String sql = "SELECT COUNT(*) " +
                " FROM " + RelationshipTable.TABLE_NAME +
                " WHERE " + RelationshipTable.COLUMN_SEEN + "=?" +
                " AND " + RelationshipTable.COLUMN_LIST_TYPE + "=?";

        try (final Cursor cursor = db.rawQuery(sql, new String[] {"0", Long.toString(RelationshipInfo.Type.FOLLOWER)})) {
            if (cursor.moveToNext()) {
                return cursor.getInt(0);
            }
        }

        return 0;
    }

    public List<Contact> getFriends() {
        final List<Contact> friends = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.query(RelationshipTable.TABLE_NAME,
                new String[] {
                        RelationshipTable._ID,
                        RelationshipTable.COLUMN_USER_ID,
                        RelationshipTable.COLUMN_AVATAR_ID,
                        RelationshipTable.COLUMN_NAME,
                        RelationshipTable.COLUMN_LIST_TYPE
                },
                RelationshipTable.COLUMN_LIST_TYPE + "=? AND " + RelationshipTable.COLUMN_USER_ID + " IS NOT NULL", new String[]{String.valueOf(FriendshipInfo.Type.FRIENDS)}, null, null, null)) {
                while (cursor.moveToNext()) {
                    final String userIdStr = cursor.getString(1);
                    final Contact friend = new Contact(
                            cursor.getLong(0),
                            userIdStr == null ? null : new UserId(userIdStr),
                            cursor.getString(2),
                            cursor.getString(3),
                            cursor.getInt(4));
                    friends.add(friend);
                }
        }
        return friends;
    }

    @WorkerThread
    @NonNull
    public List<FriendshipInfo> getFriendships(@FriendshipInfo.Type int friendshipType) {
        final List<FriendshipInfo> friendships = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String sql = "SELECT " +
                RelationshipTable._ID + "," +
                RelationshipTable.COLUMN_USER_ID + "," +
                RelationshipTable.COLUMN_USERNAME + "," +
                RelationshipTable.COLUMN_NAME + "," +
                RelationshipTable.COLUMN_AVATAR_ID + "," +
                RelationshipTable.COLUMN_LIST_TYPE + "," +
                RelationshipTable.COLUMN_SEEN + "," +
                RelationshipTable.COLUMN_TIMESTAMP +
                " FROM " + RelationshipTable.TABLE_NAME +
                " WHERE " + RelationshipTable.COLUMN_LIST_TYPE + "=?";
        try (final Cursor cursor = db.rawQuery(sql, new String[] {Integer.toString(friendshipType)})) {
            while (cursor.moveToNext()) {
                FriendshipInfo friendshipInfo = new FriendshipInfo(
                        new UserId(cursor.getString(1)),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getInt(5),
                        cursor.getLong(7)
                );
                friendshipInfo.seen = cursor.getInt(6) == 1;
                friendships.add(friendshipInfo);
            }
        }
        return friendships;
    }

    @WorkerThread
    public void addFriendship(@NonNull FriendshipInfo friendship) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(RelationshipTable.COLUMN_USER_ID, friendship.userId.rawId());
        values.put(RelationshipTable.COLUMN_USERNAME, friendship.username);
        values.put(RelationshipTable.COLUMN_NAME, friendship.name);
        values.put(RelationshipTable.COLUMN_AVATAR_ID, friendship.avatarId);
        values.put(RelationshipTable.COLUMN_LIST_TYPE, friendship.friendshipStatus);
        values.put(RelationshipTable.COLUMN_TIMESTAMP, friendship.timestamp);

        boolean exists;
        try (Cursor cursor = db.rawQuery("SELECT * FROM " + RelationshipTable.TABLE_NAME + " WHERE " + RelationshipTable.COLUMN_USER_ID + "=?", new String[] {friendship.userId.rawId()})) {
            exists = cursor.getCount() > 0;
        }

        if (exists) {
            db.update(RelationshipTable.TABLE_NAME, values, RelationshipTable.COLUMN_USER_ID + "=?", new String[] {friendship.userId.rawId()});
        } else {
            db.insertWithOnConflict(RelationshipTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }
        notifyFriendshipsChanged(friendship);
    }

    @WorkerThread
    public void removeFriendship(@NonNull FriendshipInfo friendship) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.delete(RelationshipTable.TABLE_NAME,
                RelationshipTable.COLUMN_USER_ID + "=? ", new String[] {friendship.userId.rawId()});

        notifyFriendshipRemoved(friendship);
        notifyFriendshipsChanged(friendship);
    }

    @WorkerThread
    public void updateFriendship(@NonNull FriendshipInfo friendship) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(RelationshipTable.COLUMN_USERNAME, friendship.username);
        values.put(RelationshipTable.COLUMN_NAME, friendship.name);
        values.put(RelationshipTable.COLUMN_AVATAR_ID, friendship.avatarId);
        db.update(RelationshipTable.TABLE_NAME,
                values,
                RelationshipTable.COLUMN_USER_ID + "=? AND " + RelationshipTable.COLUMN_LIST_TYPE + "=?",
                new String[] {friendship.userId.rawId(), Integer.toString(friendship.friendshipStatus)});

        notifyFriendshipsChanged(friendship);
    }

    private void notifyNewContacts(@NonNull Collection<UserId> newContacts) {
        synchronized (observers) {
            for (Observer observer : observers) {
                observer.onNewContacts(newContacts);
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

    private void notifySuggestedContactDismissed(Contact contact) {
        synchronized (observers) {
            for (Observer observer : observers) {
                observer.onSuggestedContactDismissed(contact.addressBookId);
            }
        }
    }

    private void notifyRelationshipsChanged() {
        synchronized (observers) {
            for (Observer observer : observers) {
                observer.onRelationshipsChanged();
            }
        }
    }

    private void notifyRelationshipRemoved(@NonNull RelationshipInfo relationshipInfo) {
        synchronized (observers) {
            for (Observer observer : observers) {
                observer.onRelationshipRemoved(relationshipInfo);
            }
        }
    }

    private void notifyFriendshipsChanged(@NonNull FriendshipInfo friendshipInfo) {
        synchronized (observers) {
            for (Observer observer : observers) {
                observer.onFriendshipsChanged(friendshipInfo);
            }
        }
    }

    private void notifyFriendshipRemoved(@NonNull FriendshipInfo friendshipInfo) {
        synchronized (observers) {
            for (Observer observer : observers) {
                observer.onFriendshipRemoved(friendshipInfo);
            }
        }
    }

    public void deleteDb() {
        databaseHelper.deleteDb();
    }

    public void checkIndexes() {
        String[] indexNames = new String[] {
                AvatarsTable.INDEX_USER_ID,
                BlocklistTable.INDEX_USER_ID,
                ChatsPlaceholderTable.INDEX_USER_ID,
                ContactsTable.INDEX_USER_ID,
                FeedExcludedTable.INDEX_USER_ID,
                FeedSelectedTable.INDEX_USER_ID,
                NamesTable.INDEX_USER_ID,
                PhonesTable.INDEX_USER_ID,
                UsernamesTable.INDEX_USER_ID,
        };

        for (String name : indexNames) {
            Log.i("ContactsDb.checkIndexes checking for index " + name);
            if (!hasIndex(name)) {
                Log.sendErrorReport("ContactsDb.checkIndexes missing expected index " + name);
            }
        }
    }

    private boolean hasIndex(String name) {
        try (Cursor postIndexCountCursor = databaseHelper.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type=? AND name=?", new String[]{"index", name})) {
            if (postIndexCountCursor.moveToNext()) {
                if (postIndexCountCursor.getInt(0) <= 0) {
                    return false;
                }
            }
        }
        return true;
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
        static final String COLUMN_NUM_POTENTIAL_FRIENDS = "num_potential_friends";
        static final String COLUMN_INVITED = "invited";
        static final String COLUMN_DONT_SUGGEST = "dont_suggest";
    }

    private static final class AvatarsTable implements BaseColumns {

        private AvatarsTable() { }

        static final String TABLE_NAME = "avatars";

        static final String INDEX_USER_ID = "avatars_user_id_index";

        static final String COLUMN_CHAT_ID = "user_id";
        static final String COLUMN_AVATAR_TIMESTAMP = "avatar_timestamp";
        static final String COLUMN_AVATAR_ID = "avatar_hash";
        static final String COLUMN_REGULAR_CURRENT_ID = "regular_current_id";
        static final String COLUMN_LARGE_CURRENT_ID = "large_current_id";
    }

    // table for user-defined names
    private static final class NamesTable implements BaseColumns {

        private NamesTable() { }

        static final String TABLE_NAME = "names";

        static final String INDEX_USER_ID = "names_user_id_index";

        static final String COLUMN_USER_ID = "user_id";
        static final String COLUMN_NAME = "name";
    }

    // table for phone number from received 1-1 messages
    private static final class PhonesTable implements BaseColumns {
        private PhonesTable() {}

        static final String TABLE_NAME = "phones";

        static final String INDEX_USER_ID = "phones_user_id_index";

        static final String COLUMN_USER_ID = "user_id";
        static final String COLUMN_PHONE = "phone";
    }

    // table for katchup usernames
    private static final class UsernamesTable implements BaseColumns {
        private UsernamesTable() {}

        static final String TABLE_NAME = "usernames";

        static final String INDEX_USER_ID = "usernames_user_id_index";

        static final String COLUMN_USER_ID = "user_id";
        static final String COLUMN_USERNAME = "username";
    }

    private static final class GeotagsTable implements BaseColumns {
        private GeotagsTable() {}

        static final String TABLE_NAME = "geotags";

        static final String INDEX_USER_ID = "geotags_user_id_index";

        static final String COLUMN_USER_ID = "user_id";
        static final String COLUMN_GEOTAG = "geotag";
    }

    private static final class ChatsPlaceholderTable implements BaseColumns {
        private ChatsPlaceholderTable() {}

        static final String TABLE_NAME = "placeholder_chats";

        static final String INDEX_USER_ID = "chats_user_id_index";

        static final String COLUMN_USER_ID = "user_id";
        static final String COLUMN_TIMESTAMP = "timestamp";
        static final String COLUMN_UNSEEN = "unseen";
        static final String COLUMN_HIDDEN = "hidden";
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

    private static final class RelationshipTable implements BaseColumns {
        private RelationshipTable() {}

        static final String TABLE_NAME = "relationship_table";

        static final String INDEX_RELATIONSHIP_KEY = "relationship_key_index";

        static final String COLUMN_USER_ID = "user_id";
        static final String COLUMN_USERNAME = "username";
        static final String COLUMN_NAME = "name";
        static final String COLUMN_AVATAR_ID = "avatar_id";
        // For katchup: following, follower, incoming, outgoing, blocked
        // For halloapp: friends, incoming, outgoing, blocked 
        static final String COLUMN_LIST_TYPE = "list_type";
        static final String COLUMN_SEEN = "seen";
        static final String COLUMN_TIMESTAMP = "timestamp";
    }

    private class DatabaseHelper extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = "contacts.db";
        private static final int DATABASE_VERSION = 22;

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
                    + ContactsTable.COLUMN_NUM_POTENTIAL_FRIENDS + " INTEGER,"
                    + ContactsTable.COLUMN_INVITED + " INTEGER,"
                    + ContactsTable.COLUMN_DONT_SUGGEST + " INTEGER"
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
                    + AvatarsTable.COLUMN_AVATAR_ID + " TEXT,"
                    + AvatarsTable.COLUMN_REGULAR_CURRENT_ID + " TEXT,"
                    + AvatarsTable.COLUMN_LARGE_CURRENT_ID + " TEXT"
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

            db.execSQL("DROP TABLE IF EXISTS " + PhonesTable.TABLE_NAME);
            db.execSQL("CREATE TABLE " + PhonesTable.TABLE_NAME + " ("
                    + PhonesTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + PhonesTable.COLUMN_USER_ID + " TEXT NOT NULL,"
                    + PhonesTable.COLUMN_PHONE + " TEXT"
                    + ");");

            db.execSQL("DROP INDEX IF EXISTS " + PhonesTable.INDEX_USER_ID);
            db.execSQL("CREATE UNIQUE INDEX " + PhonesTable.INDEX_USER_ID + " ON " + PhonesTable.TABLE_NAME + " ("
                    + PhonesTable.COLUMN_USER_ID
                    + ");");

            db.execSQL("DROP TABLE IF EXISTS " + UsernamesTable.TABLE_NAME);
            db.execSQL("CREATE TABLE " + UsernamesTable.TABLE_NAME + " ("
                    + UsernamesTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + UsernamesTable.COLUMN_USER_ID + " TEXT NOT NULL,"
                    + UsernamesTable.COLUMN_USERNAME + " TEXT"
                    + ");");

            db.execSQL("DROP INDEX IF EXISTS " + UsernamesTable.INDEX_USER_ID);
            db.execSQL("CREATE UNIQUE INDEX " + UsernamesTable.INDEX_USER_ID + " ON " + UsernamesTable.TABLE_NAME + " ("
                    + UsernamesTable.COLUMN_USER_ID
                    + ");");

            db.execSQL("DROP TABLE IF EXISTS " + GeotagsTable.TABLE_NAME);
            db.execSQL("CREATE TABLE " + GeotagsTable.TABLE_NAME + " ("
                    + GeotagsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + GeotagsTable.COLUMN_USER_ID + " TEXT NOT NULL,"
                    + GeotagsTable.COLUMN_GEOTAG + " TEXT"
                    + ");");

            db.execSQL("DROP INDEX IF EXISTS " + GeotagsTable.INDEX_USER_ID);
            db.execSQL("CREATE UNIQUE INDEX " + GeotagsTable.INDEX_USER_ID + " ON " + GeotagsTable.TABLE_NAME + " ("
                    + GeotagsTable.COLUMN_USER_ID
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

            db.execSQL("DROP TABLE IF EXISTS " + ChatsPlaceholderTable.TABLE_NAME);
            db.execSQL("CREATE TABLE " + ChatsPlaceholderTable.TABLE_NAME + " ("
                    + ChatsPlaceholderTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + ChatsPlaceholderTable.COLUMN_USER_ID + " TEXT NOT NULL UNIQUE,"
                    + ChatsPlaceholderTable.COLUMN_TIMESTAMP + " INTEGER,"
                    + ChatsPlaceholderTable.COLUMN_UNSEEN + " INTEGER DEFAULT 0,"
                    + ChatsPlaceholderTable.COLUMN_HIDDEN + " INTEGER DEFAULT 0"
                    + ");");

            db.execSQL("DROP INDEX IF EXISTS " + ChatsPlaceholderTable.INDEX_USER_ID);
            db.execSQL("CREATE UNIQUE INDEX " + ChatsPlaceholderTable.INDEX_USER_ID + " ON " + ChatsPlaceholderTable.TABLE_NAME + "("
                    + ChatsPlaceholderTable.COLUMN_USER_ID + ");");

            db.execSQL("DROP TABLE IF EXISTS " + RelationshipTable.TABLE_NAME);
            db.execSQL("CREATE TABLE " + RelationshipTable.TABLE_NAME + " ("
                    + RelationshipTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + RelationshipTable.COLUMN_USER_ID + " TEXT NOT NULL,"
                    + RelationshipTable.COLUMN_USERNAME + " TEXT NOT NULL,"
                    + RelationshipTable.COLUMN_NAME + " TEXT,"
                    + RelationshipTable.COLUMN_AVATAR_ID + " TEXT,"
                    + RelationshipTable.COLUMN_LIST_TYPE + " INTEGER,"
                    + RelationshipTable.COLUMN_SEEN + " INTEGER DEFAULT 0,"
                    + RelationshipTable.COLUMN_TIMESTAMP + " INTEGER"
                    + ");");

            db.execSQL("DROP INDEX IF EXISTS " + RelationshipTable.INDEX_RELATIONSHIP_KEY);
            db.execSQL("CREATE UNIQUE INDEX " + RelationshipTable.INDEX_RELATIONSHIP_KEY + " ON " + RelationshipTable.TABLE_NAME + "("
                    + RelationshipTable.COLUMN_USER_ID + ","
                    + RelationshipTable.COLUMN_LIST_TYPE
                    + ");");

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
                case 11: {
                    upgradeFromVersion11(db);
                }
                case 12: {
                    upgradeFromVersion12(db);
                }
                case 13: {
                    upgradeFromVersion13(db);
                }
                case 14: {
                    upgradeFromVersion14(db);
                }
                case 15: {
                    upgradeFromVersion15(db);
                }
                case 16: {
                    upgradeFromVersion16(db);
                }
                case 17: {
                    upgradeFromVersion17(db);
                }
                case 18: {
                    upgradeFromVersion18(db);
                }
                case 19: {
                    upgradeFromVersion19(db);
                }
                case 20: {
                    upgradeFromVersion20(db);
                }
                case 21: {
                    upgradeFromVersion21(db);
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
                        userId);
                    contacts.add(contact);
                    if (userId != null) {
                        final String name = cursor.getString(7);
                        if (!TextUtils.isEmpty(name)) {
                            names.put(userId, name);
                        }
                        final String avatarHash = cursor.getString(9);
                        if (!TextUtils.isEmpty(avatarHash)) {
                            avatars.put(userId, new ContactAvatarInfo(userId, cursor.getLong(8), avatarHash, null, null));
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
            db.execSQL("ALTER TABLE " + ContactsTable.TABLE_NAME + " ADD COLUMN " + "new_connection" + " INTEGER");
            db.execSQL("ALTER TABLE " + ContactsTable.TABLE_NAME + " ADD COLUMN " + "connection_time" + " INTEGER");
        }

        private void upgradeFromVersion9(SQLiteDatabase db) {
            db.execSQL("ALTER TABLE " + ContactsTable.TABLE_NAME + " ADD COLUMN " + ContactsTable.COLUMN_NUM_POTENTIAL_FRIENDS + " INTEGER");
        }

        private void upgradeFromVersion10(SQLiteDatabase db) {
            db.execSQL("ALTER TABLE " + ContactsTable.TABLE_NAME + " ADD COLUMN " + "hide_chat" + " INTEGER");
        }

        private void upgradeFromVersion11(SQLiteDatabase db) {
            db.execSQL("ALTER TABLE " + ContactsTable.TABLE_NAME + " ADD COLUMN " + ContactsTable.COLUMN_INVITED + " INTEGER");
        }

        private void upgradeFromVersion12(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + PhonesTable.TABLE_NAME);
            db.execSQL("CREATE TABLE " + PhonesTable.TABLE_NAME + " ("
                    + PhonesTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + PhonesTable.COLUMN_USER_ID + " TEXT NOT NULL,"
                    + PhonesTable.COLUMN_PHONE + " TEXT"
                    + ");");

            db.execSQL("DROP INDEX IF EXISTS " + PhonesTable.INDEX_USER_ID);
            db.execSQL("CREATE UNIQUE INDEX " + PhonesTable.INDEX_USER_ID + " ON " + PhonesTable.TABLE_NAME + " ("
                    + PhonesTable.COLUMN_USER_ID
                    + ");");
        }

        private void upgradeFromVersion13(SQLiteDatabase db) {
            recreateTable(db, ContactsTable.TABLE_NAME, new String[] {
                    ContactsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT",
                    ContactsTable.COLUMN_ADDRESS_BOOK_ID + " INTEGER NOT NULL",
                    ContactsTable.COLUMN_ADDRESS_BOOK_NAME + " TEXT",
                    ContactsTable.COLUMN_ADDRESS_BOOK_PHONE + " TEXT",
                    ContactsTable.COLUMN_NORMALIZED_PHONE + " TEXT",
                    ContactsTable.COLUMN_AVATAR_ID + " TEXT",
                    ContactsTable.COLUMN_USER_ID + " TEXT",
                    "new_connection" + " INTEGER",
                    "connection_time" + " INTEGER",
                    ContactsTable.COLUMN_NUM_POTENTIAL_FRIENDS + " INTEGER",
                    "hide_chat" + " INTEGER",
                    ContactsTable.COLUMN_INVITED + " INTEGER",
            });

            db.execSQL("DROP INDEX IF EXISTS " + ContactsTable.INDEX_USER_ID);
            db.execSQL("CREATE INDEX " + ContactsTable.INDEX_USER_ID + " ON " + ContactsTable.TABLE_NAME + " ("
                    + ContactsTable.COLUMN_USER_ID
                    + ");");
        }

        private void upgradeFromVersion14(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + ChatsPlaceholderTable.TABLE_NAME);
            db.execSQL("CREATE TABLE " + ChatsPlaceholderTable.TABLE_NAME + " ("
                    + ChatsPlaceholderTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + ChatsPlaceholderTable.COLUMN_USER_ID + " TEXT NOT NULL UNIQUE,"
                    + ChatsPlaceholderTable.COLUMN_TIMESTAMP + " INTEGER,"
                    + ChatsPlaceholderTable.COLUMN_UNSEEN + " INTEGER DEFAULT 0,"
                    + ChatsPlaceholderTable.COLUMN_HIDDEN + " INTEGER DEFAULT 0"
                    + ");");

            db.execSQL("DROP INDEX IF EXISTS " + ChatsPlaceholderTable.INDEX_USER_ID);
            db.execSQL("CREATE UNIQUE INDEX " + ChatsPlaceholderTable.INDEX_USER_ID + " ON " + ChatsPlaceholderTable.TABLE_NAME + "("
                    + ChatsPlaceholderTable.COLUMN_USER_ID + ");");

            db.execSQL("INSERT OR IGNORE INTO " + ChatsPlaceholderTable.TABLE_NAME + " ("
                    + ChatsPlaceholderTable.COLUMN_USER_ID + ","
                    + ChatsPlaceholderTable.COLUMN_HIDDEN + ","
                    + ChatsPlaceholderTable.COLUMN_TIMESTAMP + ","
                    + ChatsPlaceholderTable.COLUMN_UNSEEN
                    + ") SELECT "
                    + ContactsTable.COLUMN_USER_ID + ",COALESCE(hide_chat,0),connection_time,new_connection FROM " + ContactsTable.TABLE_NAME + " WHERE " + ContactsTable.COLUMN_USER_ID + " IS NOT NULL AND " + ContactsTable.COLUMN_ADDRESS_BOOK_ID + " IS NOT NULL");

            recreateTable(db, ContactsTable.TABLE_NAME, new String[]{
                    ContactsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT",
                    ContactsTable.COLUMN_ADDRESS_BOOK_ID + " INTEGER NOT NULL",
                    ContactsTable.COLUMN_ADDRESS_BOOK_NAME + " TEXT",
                    ContactsTable.COLUMN_ADDRESS_BOOK_PHONE + " TEXT",
                    ContactsTable.COLUMN_NORMALIZED_PHONE + " TEXT",
                    ContactsTable.COLUMN_AVATAR_ID + " TEXT",
                    ContactsTable.COLUMN_USER_ID + " TEXT",
                    ContactsTable.COLUMN_NUM_POTENTIAL_FRIENDS + " INTEGER",
                    ContactsTable.COLUMN_INVITED + " INTEGER"
            });

            db.execSQL("DROP INDEX IF EXISTS " + ContactsTable.INDEX_USER_ID);
            db.execSQL("CREATE INDEX " + ContactsTable.INDEX_USER_ID + " ON " + ContactsTable.TABLE_NAME + " ("
                    + ContactsTable.COLUMN_USER_ID
                    + ");");
        }

        private void upgradeFromVersion15(SQLiteDatabase db) {
            db.execSQL("ALTER TABLE " + AvatarsTable.TABLE_NAME + " ADD COLUMN " + AvatarsTable.COLUMN_REGULAR_CURRENT_ID + " TEXT");
            db.execSQL("ALTER TABLE " + AvatarsTable.TABLE_NAME + " ADD COLUMN " + AvatarsTable.COLUMN_LARGE_CURRENT_ID + " TEXT");
        }

        private void upgradeFromVersion16(SQLiteDatabase db) {
            db.execSQL("ALTER TABLE " + ContactsTable.TABLE_NAME + " ADD COLUMN " + ContactsTable.COLUMN_DONT_SUGGEST + " INTEGER");
        }

        private void upgradeFromVersion17(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + RelationshipTable.TABLE_NAME);
            db.execSQL("CREATE TABLE " + RelationshipTable.TABLE_NAME + " ("
                    + RelationshipTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + RelationshipTable.COLUMN_USER_ID + " TEXT NOT NULL,"
                    + RelationshipTable.COLUMN_USERNAME + " TEXT NOT NULL,"
                    + RelationshipTable.COLUMN_NAME + " TEXT,"
                    + RelationshipTable.COLUMN_AVATAR_ID + " TEXT,"
                    + RelationshipTable.COLUMN_LIST_TYPE + " INTEGER"
                    + ");");

            db.execSQL("DROP INDEX IF EXISTS " + RelationshipTable.INDEX_RELATIONSHIP_KEY);
            db.execSQL("CREATE UNIQUE INDEX " + RelationshipTable.INDEX_RELATIONSHIP_KEY + " ON " + RelationshipTable.TABLE_NAME + "("
                    + RelationshipTable.COLUMN_USER_ID + ","
                    + RelationshipTable.COLUMN_LIST_TYPE
                    + ");");
        }

        private void upgradeFromVersion18(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + UsernamesTable.TABLE_NAME);
            db.execSQL("CREATE TABLE " + UsernamesTable.TABLE_NAME + " ("
                    + UsernamesTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + UsernamesTable.COLUMN_USER_ID + " TEXT NOT NULL,"
                    + UsernamesTable.COLUMN_USERNAME + " TEXT"
                    + ");");

            db.execSQL("DROP INDEX IF EXISTS " + UsernamesTable.INDEX_USER_ID);
            db.execSQL("CREATE UNIQUE INDEX " + UsernamesTable.INDEX_USER_ID + " ON " + UsernamesTable.TABLE_NAME + " ("
                    + UsernamesTable.COLUMN_USER_ID
                    + ");");
        }

        private void upgradeFromVersion19(SQLiteDatabase db) {
            db.execSQL("ALTER TABLE " + RelationshipTable.TABLE_NAME + " ADD COLUMN " + RelationshipTable.COLUMN_SEEN + " INTEGER DEFAULT 0");
            db.execSQL("UPDATE " + RelationshipTable.TABLE_NAME + " SET " + RelationshipTable.COLUMN_SEEN + "=1");
        }

        private void upgradeFromVersion20(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + GeotagsTable.TABLE_NAME);
            db.execSQL("CREATE TABLE " + GeotagsTable.TABLE_NAME + " ("
                    + GeotagsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + GeotagsTable.COLUMN_USER_ID + " TEXT NOT NULL,"
                    + GeotagsTable.COLUMN_GEOTAG + " TEXT"
                    + ");");

            db.execSQL("DROP INDEX IF EXISTS " + GeotagsTable.INDEX_USER_ID);
            db.execSQL("CREATE UNIQUE INDEX " + GeotagsTable.INDEX_USER_ID + " ON " + GeotagsTable.TABLE_NAME + " ("
                    + GeotagsTable.COLUMN_USER_ID
                    + ");");
        }

        private void upgradeFromVersion21(SQLiteDatabase db) {
            db.execSQL("ALTER TABLE " + RelationshipTable.TABLE_NAME + " ADD COLUMN " + RelationshipTable.COLUMN_TIMESTAMP + " INTEGER");
        }

        /**
         * Recreates a table with a new schema specified by columns.
         *
         * Be careful as triggers AND indices on the old table will be deleted, and WILL need to be recreated.
         */
        private void recreateTable(@NonNull SQLiteDatabase db, @NonNull String tableName, @NonNull String [] columns) {
            final StringBuilder schema = new StringBuilder();
            for (String column : columns) {
                if (schema.length() != 0) {
                    schema.append(',');
                }
                schema.append(column);
            }
            final StringBuilder selection = new StringBuilder();
            for (String column : columns) {
                if (selection.length() != 0) {
                    selection.append(',');
                }
                selection.append(column.substring(0, column.indexOf(' ')));
            }

            db.execSQL("DROP TABLE IF EXISTS tmp");
            db.execSQL("CREATE TABLE tmp (" + schema.toString() + ");");
            db.execSQL("INSERT INTO tmp SELECT " + selection.toString() + " FROM " + tableName);
            db.execSQL("DROP TABLE " + tableName);
            db.execSQL("ALTER TABLE tmp RENAME TO " + tableName);
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
        private final String avatarId;

        public NormalizedPhoneData(@NonNull String normalizedPhone, @NonNull UserId userId, String avatarId) {
            this.normalizedPhone = normalizedPhone;
            this.userId = userId;
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
        public String regularCurrentId;
        public String largeCurrentId;

        public ContactAvatarInfo(ChatId chatId, long avatarCheckTimestamp, String avatarId, String regularCurrentId, String largeCurrentId) {
            this.chatId = chatId;
            this.avatarCheckTimestamp = avatarCheckTimestamp;
            this.avatarId = avatarId;
            this.regularCurrentId = regularCurrentId;
            this.largeCurrentId = largeCurrentId;
        }
    }

}

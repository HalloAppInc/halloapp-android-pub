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

import com.halloapp.Me;
import com.halloapp.R;
import com.halloapp.util.Log;

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

    private final Context context;
    private final DatabaseHelper databaseHelper;
    private final ExecutorService databaseWriteExecutor = Executors.newSingleThreadExecutor();
    private final Set<Observer> observers = new HashSet<>();

    public interface Observer {
        void onContactsChanged();
        void onContactsReset();
    }

    public static ContactsDb getInstance(final @NonNull Context context) {
        if (instance == null) {
            synchronized(ContactsDb.class) {
                if (instance == null) {
                    instance = new ContactsDb(context);
                }
            }
        }
        return instance;
    }

    private ContactsDb(final @NonNull Context context) {
        this.context = context.getApplicationContext();
        this.databaseHelper = new DatabaseHelper(context.getApplicationContext());
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
            final Collection<AddressBookContacts.AddressBookContact> addressBookContacts = AddressBookContacts.getAddressBookContacts(context);
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
                            null, null, false));
                }

                for (Contact updateContact : diff.updated) {
                    final ContentValues values = new ContentValues();
                    values.put(ContactsTable.COLUMN_ADDRESS_BOOK_NAME, updateContact.addressBookName);
                    values.put(ContactsTable.COLUMN_ADDRESS_BOOK_PHONE, updateContact.addressBookPhone);
                    values.put(ContactsTable.COLUMN_NORMALIZED_PHONE, updateContact.normalizedPhone);
                    values.put(ContactsTable.COLUMN_USER_ID, updateContact.getRawUserId());
                    values.put(ContactsTable.COLUMN_FRIEND, updateContact.friend);
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

    public Future<Void> updateContactsServerData(Collection<Contact> updatedContacts) {
        return databaseWriteExecutor.submit(() -> {
            final SQLiteDatabase db = databaseHelper.getWritableDatabase();
            db.beginTransaction();
            int updatedRows = 0;
            try {
                for (Contact updateContact : updatedContacts) {
                    final ContentValues values = new ContentValues();
                    values.put(ContactsTable.COLUMN_USER_ID, updateContact.getRawUserId());
                    values.put(ContactsTable.COLUMN_NORMALIZED_PHONE, updateContact.normalizedPhone);
                    values.put(ContactsTable.COLUMN_FRIEND, updateContact.friend);
                    final int updatedContactRows = db.updateWithOnConflict(ContactsTable.TABLE_NAME, values,
                            ContactsTable._ID + "=? ",
                            new String [] {Long.toString(updateContact.rowId)},
                            SQLiteDatabase.CONFLICT_ABORT);
                    Log.i("ContactsDb.updateContactsServerData: " + updatedContactRows + " rows updated for " + updateContact.getDisplayName() + " " + updateContact.normalizedPhone + " " + updateContact.userId + " " + updateContact.friend);
                    updatedRows += updatedContactRows;
                }
                Log.i("ContactsDb.updateContactsServerData: " + updatedRows + " rows updated for " + updatedContacts.size() + " contacts");
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

    public Future<Void> updateNormalizedPhoneData(@NonNull List<NormalizedPhoneData> normalizedPhoneDataList) {
        return databaseWriteExecutor.submit(() -> {
            final SQLiteDatabase db = databaseHelper.getWritableDatabase();
            db.beginTransaction();
            int updatedRows = 0;
            try {
                for (NormalizedPhoneData normalizedPhoneData : normalizedPhoneDataList) {
                    final ContentValues values = new ContentValues();
                    values.put(ContactsTable.COLUMN_FRIEND, normalizedPhoneData.friend);
                    values.put(ContactsTable.COLUMN_USER_ID, normalizedPhoneData.userId.rawId());
                    final int updatedContactRows = db.updateWithOnConflict(ContactsTable.TABLE_NAME, values,
                            ContactsTable.COLUMN_NORMALIZED_PHONE + "=? ",
                            new String [] {normalizedPhoneData.normalizedPhone},
                            SQLiteDatabase.CONFLICT_ABORT);
                    Log.i("ContactsDb.updateNormalizedPhoneData: " + updatedContactRows + " rows updated for " + normalizedPhoneData.normalizedPhone + " " + normalizedPhoneData.userId + " " + normalizedPhoneData.friend);
                    updatedRows += updatedContactRows;
                }
                Log.i("ContactsDb.updateNormalizedPhoneData: " + updatedRows + " rows updated for " + normalizedPhoneDataList.size() + " contacts");
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
                values.put(AvatarsTable.COLUMN_AVATAR_HASH, contact.avatarHash);
                final int updateRowsCount = db.updateWithOnConflict(AvatarsTable.TABLE_NAME, values,
                        AvatarsTable.COLUMN_USER_ID + "=? ",
                        new String [] {contact.userId.rawId()},
                        SQLiteDatabase.CONFLICT_ABORT);
                if (updateRowsCount == 0) {
                    values.put(AvatarsTable.COLUMN_USER_ID, contact.userId.rawId());
                    db.insert(AvatarsTable.TABLE_NAME, null, values);
                }
                Log.i("ContactsDb.updateContactAvatarInfo");
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            return null;
        });
    }

    @WorkerThread
    public ContactAvatarInfo getContactAvatarInfo(@NonNull UserId userId) {
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.query(AvatarsTable.TABLE_NAME,
                new String[] {
                        AvatarsTable.COLUMN_USER_ID,
                        AvatarsTable.COLUMN_AVATAR_TIMESTAMP,
                        AvatarsTable.COLUMN_AVATAR_HASH
                },
                AvatarsTable.COLUMN_USER_ID + "=?", new String [] {userId.rawId()}, null, null, null, "1")) {
            if (cursor.moveToNext()) {
                return new ContactAvatarInfo(userId, cursor.getLong(1), cursor.getString(2));
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
                contact = new Contact(userId, context.getString(R.string.unknown_contact), null);
            }
        } else if (TextUtils.isEmpty(contact.addressBookName) && TextUtils.isEmpty(contact.addressBookPhone) && TextUtils.isEmpty(contact.halloName)) {
            contact.addressBookName = context.getString(R.string.unknown_contact);
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
                        ContactsTable.COLUMN_USER_ID,
                        ContactsTable.COLUMN_FRIEND
                },
                ContactsTable.COLUMN_USER_ID + "=?",
                new String [] {userId.rawId()}, null, null, null, "1")) {
            if (cursor.moveToNext()) {
                return new Contact(
                        cursor.getLong(0),
                        cursor.getLong(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        userId,
                        cursor.getInt(6) == 1);
            }
        }
        return null;
    }

    @WorkerThread
    public @Nullable String readName(@NonNull UserId userId) {
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.query(NamesTable.TABLE_NAME,
                new String[] { NamesTable.COLUMN_NAME },
                ContactsTable.COLUMN_USER_ID + "=?",
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
                        ContactsTable.COLUMN_USER_ID,
                        ContactsTable.COLUMN_FRIEND
                },
                ContactsTable.COLUMN_ADDRESS_BOOK_ID + " IS NOT NULL", null, null, null, null, null)) {
            while (cursor.moveToNext()) {
                final String userIdStr = cursor.getString(5);
                final Contact contact = new Contact(
                        cursor.getLong(0),
                        cursor.getLong(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        userIdStr == null ? null : new UserId(userIdStr),
                        cursor.getInt(6) == 1);
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
                        ContactsTable.COLUMN_USER_ID,
                        ContactsTable.COLUMN_FRIEND
                },
                ContactsTable.COLUMN_FRIEND + "=1" + " AND " + ContactsTable.COLUMN_ADDRESS_BOOK_ID + " IS NOT NULL",
                null, null, null, null)) {
            final Set<String> userIds = new HashSet<>();
            while (cursor.moveToNext()) {
                final String userIdStr = cursor.getString(5);
                if (userIdStr != null && userIds.add(userIdStr) && !userIdStr.equals(Me.getInstance(context).getUser())) {
                    final Contact contact = new Contact(
                            cursor.getLong(0),
                            cursor.getLong(1),
                            cursor.getString(2),
                            cursor.getString(3),
                            cursor.getString(4),
                            new UserId(userIdStr),
                            cursor.getInt(6) == 1);
                    contacts.add(contact);
                }
            }
        }
        Log.i("ContactsDb.getFriends: " + contacts.size());
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
                        ContactsTable.COLUMN_USER_ID,
                        ContactsTable.COLUMN_FRIEND
                },
                ContactsTable.COLUMN_USER_ID + " IS NOT NULL AND " + ContactsTable.COLUMN_ADDRESS_BOOK_ID + " IS NOT NULL",
                null, null, null, null)) {
            final Set<String> userIds = new HashSet<>();
            while (cursor.moveToNext()) {
                final String userIdStr = cursor.getString(5);
                if (userIdStr != null && userIds.add(userIdStr) && !userIdStr.equals(Me.getInstance(context).getUser())) {
                    final Contact contact = new Contact(
                            cursor.getLong(0),
                            cursor.getLong(1),
                            cursor.getString(2),
                            cursor.getString(3),
                            cursor.getString(4),
                            new UserId(userIdStr),
                            cursor.getInt(6) == 1);
                    contacts.add(contact);
                }
            }
        }
        Log.i("ContactsDb.getUsers: " + contacts.size());
        return contacts;
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
        static final String COLUMN_USER_ID = "user_id";
        static final String COLUMN_FRIEND = "friend";
    }

    private static final class AvatarsTable implements BaseColumns {

        private AvatarsTable() { }

        static final String TABLE_NAME = "avatars";

        static final String INDEX_USER_ID = "avatars_user_id_index";

        static final String COLUMN_USER_ID = "user_id";
        static final String COLUMN_AVATAR_TIMESTAMP = "avatar_timestamp";
        static final String COLUMN_AVATAR_HASH = "avatar_hash";
    }

    // table for user-defined names
    private static final class NamesTable implements BaseColumns {

        private NamesTable() { }

        static final String TABLE_NAME = "names";

        static final String INDEX_USER_ID = "names_user_id_index";

        static final String COLUMN_USER_ID = "user_id";
        static final String COLUMN_NAME = "name";
    }

    private class DatabaseHelper extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = "contacts.db";
        private static final int DATABASE_VERSION = 5;

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
                    + ContactsTable.COLUMN_USER_ID + " TEXT,"
                    + ContactsTable.COLUMN_FRIEND + " INTEGER"
                    + ");");

            db.execSQL("DROP INDEX IF EXISTS " + ContactsTable.INDEX_USER_ID);
            db.execSQL("CREATE INDEX " + ContactsTable.INDEX_USER_ID + " ON " + ContactsTable.TABLE_NAME + " ("
                    + ContactsTable.COLUMN_USER_ID
                    + ");");

            db.execSQL("DROP TABLE IF EXISTS " + AvatarsTable.TABLE_NAME);
            db.execSQL("CREATE TABLE " + AvatarsTable.TABLE_NAME + " ("
                    + AvatarsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + AvatarsTable.COLUMN_USER_ID + " TEXT NOT NULL,"
                    + AvatarsTable.COLUMN_AVATAR_TIMESTAMP + " INTEGER,"
                    + AvatarsTable.COLUMN_AVATAR_HASH + " TEXT"
                    + ");");

            db.execSQL("DROP INDEX IF EXISTS " + AvatarsTable.INDEX_USER_ID);
            db.execSQL("CREATE UNIQUE INDEX " + AvatarsTable.INDEX_USER_ID + " ON " + AvatarsTable.TABLE_NAME + " ("
                    + AvatarsTable.COLUMN_USER_ID
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
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //noinspection SwitchStatementWithTooFewBranches
            switch (oldVersion) {
                case 4: {
                    upgradeFromVersion4(db);
                    //
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
                values.put(AvatarsTable.COLUMN_USER_ID, avatar.userId.rawId());
                values.put(AvatarsTable.COLUMN_AVATAR_HASH, avatar.avatarHash);
                values.put(AvatarsTable.COLUMN_AVATAR_TIMESTAMP, avatar.avatarCheckTimestamp);
                db.insert(AvatarsTable.TABLE_NAME, null, values);
            }
        }

        private void deleteDb() {
            close();
            final File dbFile = context.getDatabasePath(getDatabaseName());
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

        public NormalizedPhoneData(@NonNull String normalizedPhone, @NonNull UserId userId, boolean friend) {
            this.normalizedPhone = normalizedPhone;
            this.userId = userId;
            this.friend = friend;
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
        public final UserId userId;
        public long avatarCheckTimestamp;
        public String avatarHash;

        public ContactAvatarInfo(UserId userId, long avatarCheckTimestamp, String avatarHash) {
            this.userId = userId;
            this.avatarCheckTimestamp = avatarCheckTimestamp;
            this.avatarHash = avatarHash;
        }
    }
}

package com.halloapp.contacts;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.halloapp.Me;
import com.halloapp.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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

    Future<ContactsDiff> syncAddressBook() {
        return databaseWriteExecutor.submit(() -> {
            final Collection<AddressBookContacts.AddressBookContact> addressBookContacts = AddressBookContacts.getAddressBookContacts(context);
            if (addressBookContacts == null) {
                return null;
            }
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
                    values.put(ContactsTable.COLUMN_NAME, addressBookContact.name);
                    values.put(ContactsTable.COLUMN_PHONE, addressBookContact.phone);
                    db.insert(ContactsTable.TABLE_NAME, null, values);
                }

                for (Contact updateContact : diff.updated) {
                    final ContentValues values = new ContentValues();
                    values.put(ContactsTable.COLUMN_NAME, updateContact.name);
                    values.put(ContactsTable.COLUMN_PHONE, updateContact.phone);
                    values.put(ContactsTable.COLUMN_USER_ID, updateContact.getRawUserId());
                    values.put(ContactsTable.COLUMN_FRIEND, updateContact.friend);
                    db.updateWithOnConflict(ContactsTable.TABLE_NAME, values,
                            ContactsTable._ID + "=? ",
                            new String [] {Long.toString(updateContact.id)},
                            SQLiteDatabase.CONFLICT_ABORT);
                }
                for (Long id : diff.removed) {
                    db.delete(ContactsTable.TABLE_NAME,
                            ContactsTable._ID + "=? ",
                            new String [] {Long.toString(id)});
                }

                Log.i("ContactsDb.syncAddressBook: " + diff);

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            if (!diff.isEmpty()) {
                notifyContactsChanged();
            }
            return diff;
        });
    }

    Future<Void> updateContactsFriendship(Collection<Contact> updatedContacts) {
        return databaseWriteExecutor.submit(() -> {
            final SQLiteDatabase db = databaseHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                for (Contact updateContact : updatedContacts) {
                    final ContentValues values = new ContentValues();
                    values.put(ContactsTable.COLUMN_USER_ID, updateContact.getRawUserId());
                    values.put(ContactsTable.COLUMN_FRIEND, updateContact.friend);
                    db.updateWithOnConflict(ContactsTable.TABLE_NAME, values,
                            ContactsTable._ID + "=? ",
                            new String [] {Long.toString(updateContact.id)},
                            SQLiteDatabase.CONFLICT_ABORT);
                }
                Log.i("ContactsDb.updateContactsFriendship: " + updatedContacts.size() + " updated");
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            if (!updatedContacts.isEmpty()) {
                notifyContactsChanged();
            }
            return null;
        });
    }

    @WorkerThread
    public Contact getContact(@NonNull UserId userId) {
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.query(ContactsTable.TABLE_NAME,
                new String[] { ContactsTable._ID,
                        ContactsTable.COLUMN_ADDRESS_BOOK_ID,
                        ContactsTable.COLUMN_NAME,
                        ContactsTable.COLUMN_PHONE,
                        ContactsTable.COLUMN_USER_ID,
                        ContactsTable.COLUMN_FRIEND
                },
                ContactsTable.COLUMN_USER_ID + "=?", new String [] {userId.rawId()}, null, null, null, "1")) {
            if (cursor.moveToNext()) {
                return new Contact(
                        cursor.getLong(0),
                        cursor.getLong(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        userId,
                        cursor.getInt(5) == 1);
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
                        ContactsTable.COLUMN_NAME,
                        ContactsTable.COLUMN_PHONE,
                        ContactsTable.COLUMN_USER_ID,
                        ContactsTable.COLUMN_FRIEND
                },
                null, null, null, null, null, null)) {

            while (cursor.moveToNext()) {
                final String userIdStr = cursor.getString(4);
                final Contact contact = new Contact(
                        cursor.getLong(0),
                        cursor.getLong(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        userIdStr == null ? null : new UserId(userIdStr),
                        cursor.getInt(5) == 1);
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
                        ContactsTable.COLUMN_NAME,
                        ContactsTable.COLUMN_PHONE,
                        ContactsTable.COLUMN_USER_ID,
                        ContactsTable.COLUMN_FRIEND
                },
                ContactsTable.COLUMN_FRIEND + "=1",
                null, null, null, null)) {

            final Set<String> userIds = new HashSet<>();
            while (cursor.moveToNext()) {
                final String userIdStr = cursor.getString(4);
                if (userIdStr != null && userIds.add(userIdStr) && !userIdStr.equals(Me.getInstance(context).getUser())) {
                    final Contact contact = new Contact(
                            cursor.getLong(0),
                            cursor.getLong(1),
                            cursor.getString(2),
                            cursor.getString(3),
                            new UserId(userIdStr),
                            cursor.getInt(5) == 1);
                    contacts.add(contact);
                }
            }
        }
        Log.i("ContactsDb.getFriends: " + contacts.size());
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

    private static final class ContactsTable implements BaseColumns {

        private ContactsTable() { }

        static final String TABLE_NAME = "contacts";

        static final String INDEX_JID = "jid";

        static final String COLUMN_ADDRESS_BOOK_ID = "address_book_id";
        static final String COLUMN_USER_ID = "user_id";
        static final String COLUMN_NAME = "name";
        static final String COLUMN_PHONE = "phone";
        static final String COLUMN_FRIEND = "member"; // TODO (ds): rename
    }

    private class DatabaseHelper extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = "contacts.db";
        private static final int DATABASE_VERSION = 2;

        DatabaseHelper(final @NonNull Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            setWriteAheadLoggingEnabled(true);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + ContactsTable.TABLE_NAME);
            db.execSQL("CREATE TABLE " + ContactsTable.TABLE_NAME + " ("
                    + ContactsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + ContactsTable.COLUMN_ADDRESS_BOOK_ID + " INTEGER,"
                    + ContactsTable.COLUMN_USER_ID + " TEXT,"
                    + ContactsTable.COLUMN_NAME + " TEXT NOT NULL,"
                    + ContactsTable.COLUMN_PHONE + " TEXT,"
                    + ContactsTable.COLUMN_FRIEND + " INTEGER"
                    + ");");

            db.execSQL("DROP INDEX IF EXISTS " + ContactsTable.INDEX_JID);
            db.execSQL("CREATE INDEX " + ContactsTable.INDEX_JID + " ON " + ContactsTable.TABLE_NAME + " ("
                    + ContactsTable.COLUMN_USER_ID
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //noinspection SwitchStatementWithTooFewBranches
            switch (oldVersion) {
                // TODO (ds): handle upgrades
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
}

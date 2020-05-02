package com.halloapp.content;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;

import com.halloapp.content.tables.ChatsTable;
import com.halloapp.content.tables.CommentsTable;
import com.halloapp.content.tables.MediaTable;
import com.halloapp.content.tables.MessagesTable;
import com.halloapp.content.tables.OutgoingSeenReceiptsTable;
import com.halloapp.content.tables.PostsTable;
import com.halloapp.content.tables.RepliesTable;
import com.halloapp.content.tables.SeenTable;
import com.halloapp.util.Log;

import java.io.File;

class ContentDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "content.db";
    private static final int DATABASE_VERSION = 14;

    private final Context context;
    private final ContentDbObservers observers;

    ContentDbHelper(@NonNull Context context, @NonNull ContentDbObservers observers) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        setWriteAheadLoggingEnabled(true);
        this.context = context.getApplicationContext();
        this.observers = observers;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + PostsTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + PostsTable.TABLE_NAME + " ("
                + PostsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + PostsTable.COLUMN_SENDER_USER_ID + " TEXT NOT NULL,"
                + PostsTable.COLUMN_POST_ID + " TEXT NOT NULL,"
                + PostsTable.COLUMN_TIMESTAMP + " INTEGER,"
                + PostsTable.COLUMN_TRANSFERRED + " INTEGER,"
                + PostsTable.COLUMN_SEEN + " INTEGER,"
                + PostsTable.COLUMN_TEXT + " TEXT"
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + PostsTable.INDEX_POST_KEY);
        db.execSQL("CREATE UNIQUE INDEX " + PostsTable.INDEX_POST_KEY + " ON " + PostsTable.TABLE_NAME + "("
                + PostsTable.COLUMN_SENDER_USER_ID + ", "
                + PostsTable.COLUMN_POST_ID
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + PostsTable.INDEX_TIMESTAMP);
        db.execSQL("CREATE INDEX " + PostsTable.INDEX_TIMESTAMP + " ON " + PostsTable.TABLE_NAME + "("
                + PostsTable.COLUMN_TIMESTAMP
                + ");");

        db.execSQL("DROP TABLE IF EXISTS " + MessagesTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + MessagesTable.TABLE_NAME + " ("
                + MessagesTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MessagesTable.COLUMN_CHAT_ID + " TEXT NOT NULL,"
                + MessagesTable.COLUMN_SENDER_USER_ID + " TEXT NOT NULL,"
                + MessagesTable.COLUMN_MESSAGE_ID + " TEXT NOT NULL,"
                + MessagesTable.COLUMN_TIMESTAMP + " INTEGER,"
                + MessagesTable.COLUMN_STATE + " INTEGER,"
                + MessagesTable.COLUMN_TEXT + " TEXT"
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + MessagesTable.INDEX_MESSAGE_KEY);
        db.execSQL("CREATE UNIQUE INDEX " + MessagesTable.INDEX_MESSAGE_KEY + " ON " + MessagesTable.TABLE_NAME + "("
                + MessagesTable.COLUMN_CHAT_ID + ", "
                + MessagesTable.COLUMN_SENDER_USER_ID + ", "
                + MessagesTable.COLUMN_MESSAGE_ID
                + ");");

        db.execSQL("DROP TABLE IF EXISTS " + ChatsTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + ChatsTable.TABLE_NAME + " ("
                + ChatsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + ChatsTable.COLUMN_CHAT_ID + " TEXT NOT NULL UNIQUE,"
                + ChatsTable.COLUMN_TIMESTAMP + " INTEGER,"
                + ChatsTable.COLUMN_NEW_MESSAGE_COUNT + " INTEGER,"
                + ChatsTable.COLUMN_LAST_MESSAGE_ROW_ID + " INTEGER DEFAULT -1,"
                + ChatsTable.COLUMN_FIRST_UNSEEN_MESSAGE_ROW_ID + " INTEGER DEFAULT -1"
                + ");");

        db.execSQL("DROP TABLE IF EXISTS " + RepliesTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + RepliesTable.TABLE_NAME + " ("
                + RepliesTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + RepliesTable.COLUMN_MESSAGE_ROW_ID + " INTEGER,"
                + RepliesTable.COLUMN_POST_ID + " TEXT NOT NULL,"
                + RepliesTable.COLUMN_POST_MEDIA_INDEX + " INTEGER,"
                + RepliesTable.COLUMN_TEXT + " TEXT,"
                + RepliesTable.COLUMN_MEDIA_TYPE + " INTEGER,"
                + RepliesTable.COLUMN_MEDIA_PREVIEW_FILE + " TEXT"
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + RepliesTable.INDEX_MESSAGE_KEY);
        db.execSQL("CREATE UNIQUE INDEX " + RepliesTable.INDEX_MESSAGE_KEY + " ON " + RepliesTable.TABLE_NAME + "("
                + RepliesTable.COLUMN_MESSAGE_ROW_ID
                + ");");

        db.execSQL("DROP TABLE IF EXISTS " + MediaTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + MediaTable.TABLE_NAME + " ("
                + MediaTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MediaTable.COLUMN_PARENT_TABLE + " TEXT NOT NULL,"
                + MediaTable.COLUMN_PARENT_ROW_ID + " INTEGER,"
                + MediaTable.COLUMN_TYPE + " INTEGER,"
                + MediaTable.COLUMN_TRANSFERRED + " INTEGER,"
                + MediaTable.COLUMN_URL + " TEXT,"
                + MediaTable.COLUMN_FILE + " FILE,"
                + MediaTable.COLUMN_ENC_KEY + " BLOB,"
                + MediaTable.COLUMN_SHA256_HASH + " BLOB,"
                + MediaTable.COLUMN_WIDTH + " INTEGER,"
                + MediaTable.COLUMN_HEIGHT + " INTEGER"
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + MediaTable.INDEX_MEDIA_KEY);
        db.execSQL("CREATE INDEX " + MediaTable.INDEX_MEDIA_KEY + " ON " + MediaTable.TABLE_NAME + "("
                + MediaTable.COLUMN_PARENT_TABLE + ", "
                + MediaTable.COLUMN_PARENT_ROW_ID
                + ");");

        db.execSQL("DROP TABLE IF EXISTS " + CommentsTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + CommentsTable.TABLE_NAME + " ("
                + CommentsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + CommentsTable.COLUMN_POST_SENDER_USER_ID + " TEXT NOT NULL,"
                + CommentsTable.COLUMN_POST_ID + " TEXT NOT NULL,"
                + CommentsTable.COLUMN_COMMENT_SENDER_USER_ID + " TEXT NOT NULL,"
                + CommentsTable.COLUMN_COMMENT_ID + " TEXT NOT NULL,"
                + CommentsTable.COLUMN_PARENT_ID + " INTEGER,"
                + CommentsTable.COLUMN_TIMESTAMP + " INTEGER,"
                + CommentsTable.COLUMN_TRANSFERRED + " INTEGER,"
                + CommentsTable.COLUMN_SEEN + " INTEGER,"
                + CommentsTable.COLUMN_TEXT + " TEXT"
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + CommentsTable.INDEX_COMMENT_KEY);
        db.execSQL("CREATE UNIQUE INDEX " + CommentsTable.INDEX_COMMENT_KEY + " ON " + CommentsTable.TABLE_NAME + "("
                + CommentsTable.COLUMN_COMMENT_SENDER_USER_ID + ", "
                + CommentsTable.COLUMN_COMMENT_ID
                + ");");

        db.execSQL("DROP TABLE IF EXISTS " + SeenTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + SeenTable.TABLE_NAME + " ("
                + SeenTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SeenTable.COLUMN_POST_ID + " TEXT NOT NULL,"
                + SeenTable.COLUMN_SEEN_BY_USER_ID + " TEXT NOT NULL,"
                + SeenTable.COLUMN_TIMESTAMP
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + SeenTable.INDEX_SEEN_KEY);
        db.execSQL("CREATE UNIQUE INDEX " + SeenTable.INDEX_SEEN_KEY + " ON " + SeenTable.TABLE_NAME + "("
                + SeenTable.COLUMN_POST_ID + ", "
                + SeenTable.COLUMN_SEEN_BY_USER_ID
                + ");");

        db.execSQL("DROP TABLE IF EXISTS " + OutgoingSeenReceiptsTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + OutgoingSeenReceiptsTable.TABLE_NAME + " ("
                + OutgoingSeenReceiptsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + OutgoingSeenReceiptsTable.COLUMN_CHAT_ID + " TEXT NOT NULL,"
                + OutgoingSeenReceiptsTable.COLUMN_SENDER_USER_ID + " TEXT NOT NULL,"
                + OutgoingSeenReceiptsTable.COLUMN_CONTENT_ITEM_ID + " TEXT NOT NULL"
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + OutgoingSeenReceiptsTable.INDEX_OUTGOING_RECEIPT_KEY);
        db.execSQL("CREATE UNIQUE INDEX " + OutgoingSeenReceiptsTable.INDEX_OUTGOING_RECEIPT_KEY + " ON " + OutgoingSeenReceiptsTable.TABLE_NAME + "("
                + OutgoingSeenReceiptsTable.COLUMN_CHAT_ID + ", "
                + OutgoingSeenReceiptsTable.COLUMN_SENDER_USER_ID + ", "
                + OutgoingSeenReceiptsTable.COLUMN_CONTENT_ITEM_ID
                + ");");

        db.execSQL("DROP TRIGGER IF EXISTS " + PostsTable.TRIGGER_DELETE);
        //noinspection SyntaxError
        db.execSQL("CREATE TRIGGER " + PostsTable.TRIGGER_DELETE + " AFTER DELETE ON " + PostsTable.TABLE_NAME + " "
                + "BEGIN "
                +   " DELETE FROM " + MediaTable.TABLE_NAME + " WHERE " + MediaTable.COLUMN_PARENT_ROW_ID + "=OLD." + PostsTable._ID + " AND " + MediaTable.COLUMN_PARENT_TABLE + "='" + PostsTable.TABLE_NAME + "'; "
                +   " DELETE FROM " + CommentsTable.TABLE_NAME + " WHERE " + CommentsTable.COLUMN_POST_ID + "=OLD." + PostsTable.COLUMN_POST_ID + " AND " + CommentsTable.COLUMN_POST_SENDER_USER_ID + "=OLD." + PostsTable.COLUMN_SENDER_USER_ID + "; "
                +   " DELETE FROM " + SeenTable.TABLE_NAME + " WHERE " + SeenTable.COLUMN_POST_ID + "=OLD." + PostsTable.COLUMN_POST_ID + " AND ''=OLD." + PostsTable.COLUMN_SENDER_USER_ID + "; "
                + "END;");

        db.execSQL("DROP TRIGGER IF EXISTS " + MessagesTable.TRIGGER_DELETE);
        db.execSQL("CREATE TRIGGER " + MessagesTable.TRIGGER_DELETE + " AFTER DELETE ON " + MessagesTable.TABLE_NAME + " "
                + "BEGIN "
                +   " DELETE FROM " + MediaTable.TABLE_NAME + " WHERE " + MediaTable.COLUMN_PARENT_ROW_ID + "=OLD." + MessagesTable._ID + " AND " + MediaTable.COLUMN_PARENT_TABLE + "='" + MessagesTable.TABLE_NAME + "'; "
                + "END;");

        observers.notifyDbCreated();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("ContentDb: upgrade from " + oldVersion + " to " + newVersion);
        switch (oldVersion) {
            case 10: {
                upgradeFromVersion10(db);
                // fall through
            }
            case 11: {
                upgradeFromVersion11(db);
                // fall through
            }
            case 12: {
                upgradeFromVersion12(db);
                // fall through
            }

            break;
            default: {
                onCreate(db);
                break;
            }
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("ContentDb: downgrade from " + oldVersion + " to " + newVersion);
        onCreate(db);
    }

    private void upgradeFromVersion10(@NonNull SQLiteDatabase db) {
        final ContentValues messageValues = new ContentValues();
        messageValues.put("seen", 2);
        db.update(MessagesTable.TABLE_NAME, messageValues, null, null);
    }

    private void upgradeFromVersion11(@NonNull SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + OutgoingSeenReceiptsTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + OutgoingSeenReceiptsTable.TABLE_NAME + " ("
                + OutgoingSeenReceiptsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + OutgoingSeenReceiptsTable.COLUMN_CHAT_ID + " TEXT NOT NULL,"
                + OutgoingSeenReceiptsTable.COLUMN_SENDER_USER_ID + " TEXT NOT NULL,"
                + OutgoingSeenReceiptsTable.COLUMN_CONTENT_ITEM_ID + " TEXT NOT NULL"
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + OutgoingSeenReceiptsTable.INDEX_OUTGOING_RECEIPT_KEY);
        db.execSQL("CREATE UNIQUE INDEX " + OutgoingSeenReceiptsTable.INDEX_OUTGOING_RECEIPT_KEY + " ON " + OutgoingSeenReceiptsTable.TABLE_NAME + "("
                + OutgoingSeenReceiptsTable.COLUMN_CHAT_ID + ", "
                + OutgoingSeenReceiptsTable.COLUMN_SENDER_USER_ID + ", "
                + OutgoingSeenReceiptsTable.COLUMN_CONTENT_ITEM_ID
                + ");");

        removeColumns(db, MessagesTable.TABLE_NAME, new String [] {
                MessagesTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT",
                MessagesTable.COLUMN_CHAT_ID + " TEXT NOT NULL",
                MessagesTable.COLUMN_SENDER_USER_ID + " TEXT NOT NULL",
                MessagesTable.COLUMN_MESSAGE_ID + " TEXT NOT NULL",
                MessagesTable.COLUMN_TIMESTAMP + " INTEGER",
                MessagesTable.COLUMN_TEXT + " TEXT"
        });

        db.execSQL("ALTER TABLE " + MessagesTable.TABLE_NAME + " ADD COLUMN " + MessagesTable.COLUMN_STATE + " INTEGER");
        ContentValues values = new ContentValues();
        values.put(MessagesTable.COLUMN_STATE, Message.STATE_OUTGOING_SEEN);
        db.update(MessagesTable.TABLE_NAME, values, MessagesTable.COLUMN_SENDER_USER_ID + "=''", null);
        values.put(MessagesTable.COLUMN_STATE, Message.STATE_INCOMING_RECEIVED);
        db.update(MessagesTable.TABLE_NAME, values, MessagesTable.COLUMN_SENDER_USER_ID + "<>''", null);

        final ContentValues postValues = new ContentValues();
        postValues.put(PostsTable.COLUMN_TRANSFERRED, Post.TRANSFERRED_YES);
        db.update(PostsTable.TABLE_NAME, postValues, PostsTable.COLUMN_TRANSFERRED + "=?", new String [] {"1"});
    }

    private void upgradeFromVersion12(@NonNull SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + RepliesTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + RepliesTable.TABLE_NAME + " ("
                + RepliesTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + RepliesTable.COLUMN_MESSAGE_ROW_ID + " INTEGER,"
                + RepliesTable.COLUMN_POST_ID + " TEXT NOT NULL,"
                + RepliesTable.COLUMN_POST_MEDIA_INDEX + " INTEGER,"
                + RepliesTable.COLUMN_TEXT + " TEXT,"
                + RepliesTable.COLUMN_MEDIA_TYPE + " INTEGER,"
                + RepliesTable.COLUMN_MEDIA_PREVIEW_FILE + " TEXT"
                + ");");

        removeColumns(db, MessagesTable.TABLE_NAME, new String [] {
                MessagesTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT",
                MessagesTable.COLUMN_CHAT_ID + " TEXT NOT NULL",
                MessagesTable.COLUMN_SENDER_USER_ID + " TEXT NOT NULL",
                MessagesTable.COLUMN_MESSAGE_ID + " TEXT NOT NULL",
                MessagesTable.COLUMN_TIMESTAMP + " INTEGER",
                MessagesTable.COLUMN_STATE + " INTEGER",
                MessagesTable.COLUMN_TEXT + " TEXT"
        });
    }

    private void removeColumns(@NonNull SQLiteDatabase db, @NonNull String tableName, @NonNull String [] columns) {
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

    void deleteDb() {
        close();
        final File dbFile = context.getDatabasePath(getDatabaseName());
        if (!dbFile.delete()) {
            Log.e("ContentDb: cannot delete " + dbFile.getAbsolutePath());
        }
        final File walFile = new File(dbFile.getAbsolutePath() + "-wal");
        if (walFile.exists() && !walFile.delete()) {
            Log.e("ContentDb: cannot delete " + walFile.getAbsolutePath());
        }
        final File shmFile = new File(dbFile.getAbsolutePath() + "-shm");
        if (shmFile.exists() && !shmFile.delete()) {
            Log.e("ContentDb: cannot delete " + shmFile.getAbsolutePath());
        }
    }
}

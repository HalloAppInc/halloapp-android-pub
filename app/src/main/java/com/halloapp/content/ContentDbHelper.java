package com.halloapp.content;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;

import com.halloapp.content.tables.CommentsTable;
import com.halloapp.content.tables.MediaTable;
import com.halloapp.content.tables.MessagesTable;
import com.halloapp.content.tables.PostsTable;
import com.halloapp.content.tables.SeenTable;
import com.halloapp.util.Log;

import java.io.File;

class ContentDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "content.db";
    private static final int DATABASE_VERSION = 9;

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

        db.execSQL("DROP TABLE IF EXISTS " + MessagesTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + MessagesTable.TABLE_NAME + " ("
                + MessagesTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MessagesTable.COLUMN_CHAT_ID + " TEXT NOT NULL,"
                + MessagesTable.COLUMN_SENDER_USER_ID + " TEXT NOT NULL,"
                + MessagesTable.COLUMN_MESSAGE_ID + " TEXT NOT NULL,"
                + MessagesTable.COLUMN_REPLY_TO_ROW_ID + " INTEGER,"
                + MessagesTable.COLUMN_TIMESTAMP + " INTEGER,"
                + MessagesTable.COLUMN_TRANSFERRED + " INTEGER,"
                + MessagesTable.COLUMN_SEEN + " INTEGER,"
                + MessagesTable.COLUMN_TEXT + " TEXT"
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

        db.execSQL("DROP TABLE IF EXISTS " + SeenTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + SeenTable.TABLE_NAME + " ("
                + SeenTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SeenTable.COLUMN_POST_ID + " TEXT NOT NULL,"
                + SeenTable.COLUMN_SEEN_BY_USER_ID + " TEXT NOT NULL,"
                + SeenTable.COLUMN_TIMESTAMP
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

        db.execSQL("DROP INDEX IF EXISTS " + MessagesTable.INDEX_MESSAGE_KEY);
        db.execSQL("CREATE UNIQUE INDEX " + MessagesTable.INDEX_MESSAGE_KEY + " ON " + MessagesTable.TABLE_NAME + "("
                + MessagesTable.COLUMN_CHAT_ID + ", "
                + MessagesTable.COLUMN_SENDER_USER_ID + ", "
                + MessagesTable.COLUMN_MESSAGE_ID
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + PostsTable.INDEX_TIMESTAMP);
        db.execSQL("CREATE INDEX " + PostsTable.INDEX_TIMESTAMP + " ON " + PostsTable.TABLE_NAME + "("
                + PostsTable.COLUMN_TIMESTAMP
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + CommentsTable.INDEX_COMMENT_KEY);
        db.execSQL("CREATE UNIQUE INDEX " + CommentsTable.INDEX_COMMENT_KEY + " ON " + CommentsTable.TABLE_NAME + "("
                + CommentsTable.COLUMN_COMMENT_SENDER_USER_ID + ", "
                + CommentsTable.COLUMN_COMMENT_ID
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + SeenTable.INDEX_SEEN_KEY);
        db.execSQL("CREATE UNIQUE INDEX " + SeenTable.INDEX_SEEN_KEY + " ON " + SeenTable.TABLE_NAME + "("
                + SeenTable.COLUMN_POST_ID + ", "
                + SeenTable.COLUMN_SEEN_BY_USER_ID
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
        switch (oldVersion) {
            case 7: {
                upgradeFromVersion7(db);
                // fall through
            }
            case 8: {
                upgradeFromVersion8(db);
                break;
            }
            default: {
                onCreate(db);
                break;
            }
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }

    public SQLiteDatabase getWritableDatabase() {
        synchronized (this) {
            renameDb();
            return super.getWritableDatabase();
        }
    }

    public SQLiteDatabase getReadableDatabase() {
        synchronized (this) {
            renameDb();
            return super.getReadableDatabase();
        }
    }

    // TODO (ds): remove
    private void renameDb() {
        final File dbFileOld = context.getDatabasePath("posts.db");
        if (!dbFileOld.exists()) {
            return;
        }
        final File dbFileNew = context.getDatabasePath(getDatabaseName());
        if (!dbFileOld.renameTo(dbFileNew)) {
            Log.e("ContentDb: cannot rename " + dbFileOld.getAbsolutePath());
        }
        final File walFileOld = new File(dbFileOld.getAbsolutePath() + "-wal");
        final File walFileNew = new File(dbFileNew.getAbsolutePath() + "-wal");
        if (!walFileOld.renameTo(walFileNew)) {
            Log.e("ContentDb: cannot rename " + walFileOld.getAbsolutePath());
        }
        final File shmFileOld = new File(dbFileOld.getAbsolutePath() + "-shm");
        final File shmFileNew = new File(dbFileNew.getAbsolutePath() + "-shm");
        if (!shmFileOld.renameTo(shmFileNew)) {
            Log.e("ContentDb: cannot rename " + shmFileOld.getAbsolutePath());
        }
    }

    private void upgradeFromVersion7(SQLiteDatabase db) {
        Log.i("ContentDb.upgradeFromVersion7 started");
        db.beginTransaction();

        db.execSQL("CREATE TABLE tmp ("
                + MediaTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "post_row_id" + " INTEGER,"
                + MediaTable.COLUMN_TYPE + " INTEGER,"
                + MediaTable.COLUMN_TRANSFERRED + " INTEGER,"
                + MediaTable.COLUMN_URL + " TEXT,"
                + MediaTable.COLUMN_FILE + " FILE,"
                + MediaTable.COLUMN_ENC_KEY + " BLOB,"
                + MediaTable.COLUMN_SHA256_HASH + " BLOB,"
                + MediaTable.COLUMN_WIDTH + " INTEGER,"
                + MediaTable.COLUMN_HEIGHT + " INTEGER"
                + ");");
        db.execSQL("INSERT INTO tmp SELECT "
                + MediaTable._ID + " ,"
                + "post_row_id" + " ,"
                + MediaTable.COLUMN_TYPE + " ,"
                + MediaTable.COLUMN_TRANSFERRED + " ,"
                + MediaTable.COLUMN_URL + " ,"
                + MediaTable.COLUMN_FILE + " ,"
                + MediaTable.COLUMN_ENC_KEY + " ,"
                + MediaTable.COLUMN_SHA256_HASH + " ,"
                + MediaTable.COLUMN_WIDTH + " ,"
                + MediaTable.COLUMN_HEIGHT + " "
                + "FROM " + MediaTable.TABLE_NAME);
        db.execSQL("DROP TABLE " + MediaTable.TABLE_NAME);
        db.execSQL("ALTER TABLE tmp RENAME TO " + MediaTable.TABLE_NAME);

        db.setTransactionSuccessful();
        db.endTransaction();
        Log.i("ContentDb.upgradeFromVersion7 finished");
    }

    private void upgradeFromVersion8(SQLiteDatabase db) {

        // create messages table
        db.execSQL("DROP TABLE IF EXISTS " + MessagesTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + MessagesTable.TABLE_NAME + " ("
                + MessagesTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MessagesTable.COLUMN_CHAT_ID + " TEXT NOT NULL,"
                + MessagesTable.COLUMN_SENDER_USER_ID + " TEXT NOT NULL,"
                + MessagesTable.COLUMN_MESSAGE_ID + " TEXT NOT NULL,"
                + MessagesTable.COLUMN_REPLY_TO_ROW_ID + " INTEGER,"
                + MessagesTable.COLUMN_TIMESTAMP + " INTEGER,"
                + MessagesTable.COLUMN_TRANSFERRED + " INTEGER,"
                + MessagesTable.COLUMN_SEEN + " INTEGER,"
                + MessagesTable.COLUMN_TEXT + " TEXT"
                + ");");

        db.execSQL("CREATE UNIQUE INDEX " + MessagesTable.INDEX_MESSAGE_KEY + " ON " + MessagesTable.TABLE_NAME + "("
                + MessagesTable.COLUMN_CHAT_ID + ", "
                + MessagesTable.COLUMN_SENDER_USER_ID + ", "
                + MessagesTable.COLUMN_MESSAGE_ID
                + ");");

        db.execSQL("CREATE TRIGGER " + MessagesTable.TRIGGER_DELETE + " AFTER DELETE ON " + MessagesTable.TABLE_NAME + " "
                + "BEGIN "
                +   " DELETE FROM " + MediaTable.TABLE_NAME + " WHERE " + MediaTable.COLUMN_PARENT_ROW_ID + "=OLD." + MessagesTable._ID + " AND " + MediaTable.COLUMN_PARENT_TABLE + "='" + MessagesTable.TABLE_NAME + "'; "
                + "END;");

        // refactor media table
        db.execSQL("CREATE TABLE tmp ("
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
        db.execSQL("INSERT INTO tmp SELECT "
                + MediaTable._ID + " ,"
                + "'" + PostsTable.TABLE_NAME + "' ,"
                + "post_row_id" + " ,"
                + MediaTable.COLUMN_TYPE + " ,"
                + MediaTable.COLUMN_TRANSFERRED + " ,"
                + MediaTable.COLUMN_URL + " ,"
                + MediaTable.COLUMN_FILE + " ,"
                + MediaTable.COLUMN_ENC_KEY + " ,"
                + MediaTable.COLUMN_SHA256_HASH + " ,"
                + MediaTable.COLUMN_WIDTH + " ,"
                + MediaTable.COLUMN_HEIGHT + " "
                + "FROM " + MediaTable.TABLE_NAME);
        db.execSQL("DROP TABLE " + MediaTable.TABLE_NAME);
        db.execSQL("ALTER TABLE tmp RENAME TO " + MediaTable.TABLE_NAME);

        // update posts trigger
        db.execSQL("DROP TRIGGER IF EXISTS " + PostsTable.TRIGGER_DELETE);
        //noinspection SyntaxError
        db.execSQL("CREATE TRIGGER " + PostsTable.TRIGGER_DELETE + " AFTER DELETE ON " + PostsTable.TABLE_NAME + " "
                + "BEGIN "
                +   " DELETE FROM " + MediaTable.TABLE_NAME + " WHERE " + MediaTable.COLUMN_PARENT_ROW_ID + "=OLD." + PostsTable._ID + " AND " + MediaTable.COLUMN_PARENT_TABLE + "='" + PostsTable.TABLE_NAME + "'; "
                +   " DELETE FROM " + CommentsTable.TABLE_NAME + " WHERE " + CommentsTable.COLUMN_POST_ID + "=OLD." + PostsTable.COLUMN_POST_ID + " AND " + CommentsTable.COLUMN_POST_SENDER_USER_ID + "=OLD." + PostsTable.COLUMN_SENDER_USER_ID + "; "
                +   " DELETE FROM " + SeenTable.TABLE_NAME + " WHERE " + SeenTable.COLUMN_POST_ID + "=OLD." + PostsTable.COLUMN_POST_ID + " AND ''=OLD." + PostsTable.COLUMN_SENDER_USER_ID + "; "
                + "END;");
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

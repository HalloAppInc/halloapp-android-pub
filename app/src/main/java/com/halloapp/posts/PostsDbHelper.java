package com.halloapp.posts;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;

import com.halloapp.posts.tables.CommentsTable;
import com.halloapp.posts.tables.MediaTable;
import com.halloapp.posts.tables.PostsTable;
import com.halloapp.posts.tables.SeenTable;
import com.halloapp.util.Log;

import java.io.File;

class PostsDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "posts.db";
    private static final int DATABASE_VERSION = 7;

    private final Context context;
    private final PostsDbObservers observers;

    PostsDbHelper(@NonNull Context context, @NonNull PostsDbObservers observers) {
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

        db.execSQL("DROP TABLE IF EXISTS " + MediaTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + MediaTable.TABLE_NAME + " ("
                + MediaTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MediaTable.COLUMN_MEDIA_ID + " TEXT NOT NULL,"
                + MediaTable.COLUMN_POST_ROW_ID + " INTEGER,"
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
                + SeenTable.COLUMN_POST_ID + " TEXT NOT NULL REFERENCES " + PostsTable.TABLE_NAME + "(" + PostsTable.COLUMN_POST_ID + ") ON DELETE CASCADE,"
                + SeenTable.COLUMN_SEEN_BY_USER_ID + " TEXT NOT NULL,"
                + SeenTable.COLUMN_TIMESTAMP
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + PostsTable.INDEX_POST_KEY);
        db.execSQL("CREATE UNIQUE INDEX " + PostsTable.INDEX_POST_KEY + " ON " + PostsTable.TABLE_NAME + "("
                + PostsTable.COLUMN_SENDER_USER_ID + ", "
                + PostsTable.COLUMN_POST_ID
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + CommentsTable.INDEX_COMMENT_KEY);
        db.execSQL("CREATE UNIQUE INDEX " + CommentsTable.INDEX_COMMENT_KEY + " ON " + CommentsTable.TABLE_NAME + "("
                + CommentsTable.COLUMN_COMMENT_SENDER_USER_ID + ", "
                + CommentsTable.COLUMN_COMMENT_ID
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + PostsTable.INDEX_TIMESTAMP);
        db.execSQL("CREATE INDEX " + PostsTable.INDEX_TIMESTAMP + " ON " + PostsTable.TABLE_NAME + "("
                + PostsTable.COLUMN_TIMESTAMP
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + SeenTable.INDEX_SEEN_KEY);
        db.execSQL("CREATE UNIQUE INDEX " + SeenTable.INDEX_SEEN_KEY + " ON " + SeenTable.TABLE_NAME + "("
                + SeenTable.COLUMN_POST_ID + ", "
                + SeenTable.COLUMN_SEEN_BY_USER_ID
                + ");");

        db.execSQL("DROP TRIGGER IF EXISTS " + PostsTable.TRIGGER_DELETE);
        db.execSQL("CREATE TRIGGER " + PostsTable.TRIGGER_DELETE + " AFTER DELETE ON " + PostsTable.TABLE_NAME + " "
                + "BEGIN "
                +   " DELETE FROM " + CommentsTable.TABLE_NAME + " WHERE " + CommentsTable.COLUMN_POST_ID + "=OLD." + PostsTable.COLUMN_POST_ID + "; "
                + "END;");

        observers.notifyDbCreated();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (oldVersion) {
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

    void deleteDb() {
        close();
        final File dbFile = context.getDatabasePath(getDatabaseName());
        if (!dbFile.delete()) {
            Log.e("PostsDb: cannot delete " + dbFile.getAbsolutePath());
        }
        final File walFile = new File(dbFile.getAbsolutePath() + "-wal");
        if (walFile.exists() && !walFile.delete()) {
            Log.e("PostsDb: cannot delete " + walFile.getAbsolutePath());
        }
        final File shmFile = new File(dbFile.getAbsolutePath() + "-shm");
        if (shmFile.exists() && !shmFile.delete()) {
            Log.e("PostsDb: cannot delete " + shmFile.getAbsolutePath());
        }
    }
}

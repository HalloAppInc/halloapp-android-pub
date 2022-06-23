package com.halloapp.drafts.db;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import androidx.annotation.NonNull;

import com.halloapp.AppContext;

import com.halloapp.id.ChatId;
import com.halloapp.util.logs.Log;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DraftsDb {

    private final AppContext appContext;
    private final DatabaseHelper databaseHelper;
    private final ExecutorService databaseWriteExecutor = Executors.newSingleThreadExecutor();

    public DraftsDb(AppContext appContext) {
        this.appContext = appContext;
        this.databaseHelper = new DatabaseHelper(appContext.get().getApplicationContext());
    }

    public Future<Void> insertChatDraftRecord(ChatId chatId, String chatText) {
        return databaseWriteExecutor.submit(() -> {
            final SQLiteDatabase db = databaseHelper.getWritableDatabase();
            final ContentValues values = new ContentValues();
            values.put(ChatDraftsTable.COLUMN_CHAT_ID, chatId.rawId());
            values.put(ChatDraftsTable.COLUMN_CHAT_DRAFT_TEXT, chatText);
            final long result = db.insertWithOnConflict(ChatDraftsTable.TABLE_NAME, null, values,
                    SQLiteDatabase.CONFLICT_REPLACE);
            Log.i("DraftsDb.insertChatDraftRecord: Successful inserted record for " + chatId);

            return null;
        });
    }

    public Future<Void> insertCommentDraftRecord(String postId, String commentText) {
        return databaseWriteExecutor.submit(() -> {
            final SQLiteDatabase db = databaseHelper.getWritableDatabase();
            final ContentValues values = new ContentValues();
            values.put(CommentDraftsTable.COLUMN_COMMENT_ID, postId);
            values.put(CommentDraftsTable.COLUMN_COMMENT_DRAFT_TEXT, commentText);
            final long insertedCommentDraftRows = db.insertWithOnConflict(CommentDraftsTable.TABLE_NAME, null, values,
                    SQLiteDatabase.CONFLICT_REPLACE);
            Log.i("DraftsDb.insertCommentDraftRecord: Successful inserted record for " + postId);

            return null;
        });
    }

    public Future<Void> deleteChatDraftRecord(ChatId chatId) {
        return databaseWriteExecutor.submit(() -> {
            final SQLiteDatabase db = databaseHelper.getWritableDatabase();
            final long result = db.delete(ChatDraftsTable.TABLE_NAME, ChatDraftsTable.COLUMN_CHAT_ID + "=?",
                    new String[]{chatId.rawId()});
            Log.i("DraftsDb.deleteChatDraftRecord: Successful deleted record for " + chatId.rawId());

            return null;
        });
    }

    public Future<Void> deleteCommentDraftRecord(String postId) {
        return databaseWriteExecutor.submit(() -> {
            final SQLiteDatabase db = databaseHelper.getWritableDatabase();
            final long result = db.delete(CommentDraftsTable.TABLE_NAME, CommentDraftsTable.COLUMN_COMMENT_ID + "=?",
                    new String[]{postId});
            Log.i("DraftsDb.deleteCommentDraftRecord: Successful deleted record for " + postId);

            return null;
        });
    }

    public Map<ChatId, String> getAllChatDraftText() {
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Map<ChatId, String> map = new HashMap<>();
        if (db.isOpen()) {
            final Cursor cursor = db.query(ChatDraftsTable.TABLE_NAME,
                    new String[]{ChatDraftsTable.COLUMN_CHAT_ID, ChatDraftsTable.COLUMN_CHAT_DRAFT_TEXT}, null, null, null, null, null);
            while (cursor.moveToNext()){
                @SuppressLint("Range") String chatId = cursor.getString(cursor.getColumnIndex(ChatDraftsTable.COLUMN_CHAT_ID));
                @SuppressLint("Range") String chatDraftText = cursor.getString(cursor.getColumnIndex(ChatDraftsTable.COLUMN_CHAT_DRAFT_TEXT));
                if (chatId != null) {
                    map.put(ChatId.fromNullable(chatId), chatDraftText);
                } else {
                    Log.e("DraftsDb/getAllChatDraftText failed to query chat draft data with null as chatId");
                }
            }
            cursor.close();
            db.close();
        }

        return map;
    }

    public Map<String, String> getAllCommentDraftText() {
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Map<String, String> map = new HashMap<>();
        if (db.isOpen()) {
            final Cursor cursor = db.query(CommentDraftsTable.TABLE_NAME, new String[]{CommentDraftsTable.COLUMN_COMMENT_ID, CommentDraftsTable.COLUMN_COMMENT_DRAFT_TEXT}, null, null, null, null, null);
            while (cursor.moveToNext()){
                @SuppressLint("Range") String postId = cursor.getString(cursor.getColumnIndex(CommentDraftsTable.COLUMN_COMMENT_ID));
                @SuppressLint("Range") String commentDraftText = cursor.getString(cursor.getColumnIndex(CommentDraftsTable.COLUMN_COMMENT_DRAFT_TEXT));
                map.put(postId, commentDraftText);
            }
            cursor.close();
            db.close();
        }
        return map;
    }

    public void deleteDb() {
        databaseHelper.deleteDb();
    }

    public void checkIndexes() {
        String[] indexNames = new String[] {
                ChatDraftsTable.INDEX_CHAT_ID,
                CommentDraftsTable.INDEX_COMMENT_ID
        };

        for (String name : indexNames) {
            Log.i("DraftsDb.checkIndexes checking for index " + name);
            if (!hasIndex(name)) {
                Log.sendErrorReport("DraftsDb.checkIndexes missing expected index " + name);
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

    private static final class ChatDraftsTable implements BaseColumns {

        private ChatDraftsTable() { }

        static final String TABLE_NAME = "chat_drafts";

        static final String INDEX_CHAT_ID = "chats_id_index";

        static final String COLUMN_CHAT_ID = "chats_id";
        static final String COLUMN_CHAT_DRAFT_TEXT = "chats_draft_text";
    }

    private static final class CommentDraftsTable implements BaseColumns {

        private CommentDraftsTable() { }

        static final String TABLE_NAME = "comment_drafts";

        static final String INDEX_COMMENT_ID= "comments_id_index";

        static final String COLUMN_COMMENT_ID = "comments_id";
        static final String COLUMN_COMMENT_DRAFT_TEXT = "comments_draft_text";
    }

    private class DatabaseHelper extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = "drafts.db";
        private static final int DATABASE_VERSION = 1;

        DatabaseHelper(final @NonNull Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            setWriteAheadLoggingEnabled(true);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + ChatDraftsTable.TABLE_NAME);
            db.execSQL("CREATE TABLE " + ChatDraftsTable.TABLE_NAME + " ("
                    + ChatDraftsTable.COLUMN_CHAT_ID + " TEXT PRIMARY KEY,"
                    + ChatDraftsTable.COLUMN_CHAT_DRAFT_TEXT + " TEXT"
                    + ");");

            db.execSQL("DROP INDEX IF EXISTS " + ChatDraftsTable.INDEX_CHAT_ID);
            db.execSQL("CREATE INDEX " + ChatDraftsTable.INDEX_CHAT_ID + " ON " + ChatDraftsTable.TABLE_NAME + " ("
                    + ChatDraftsTable.COLUMN_CHAT_ID
                    + ");");

            db.execSQL("DROP TABLE IF EXISTS " + CommentDraftsTable.TABLE_NAME);
            db.execSQL("CREATE TABLE " + CommentDraftsTable.TABLE_NAME + " ("
                    + CommentDraftsTable.COLUMN_COMMENT_ID + " TEXT PRIMARY KEY,"
                    + CommentDraftsTable.COLUMN_COMMENT_DRAFT_TEXT + " TEXT"
                    + ");");

            db.execSQL("DROP INDEX IF EXISTS " + CommentDraftsTable.INDEX_COMMENT_ID);
            db.execSQL("CREATE INDEX " + CommentDraftsTable.INDEX_COMMENT_ID+ " ON " + CommentDraftsTable.TABLE_NAME + " ("
                    + CommentDraftsTable.COLUMN_COMMENT_ID
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }

        private void deleteDb() {
            close();
            final File dbFile = appContext.get().getDatabasePath(getDatabaseName());
            if (!dbFile.delete()) {
                Log.e("DraftsDb.deleteDb: cannot delete " + dbFile.getAbsolutePath());
            }
            final File walFile = new File(dbFile.getAbsolutePath() + "-wal");
            if (walFile.exists() && !walFile.delete()) {
                Log.e("DraftsDb.deleteDb: cannot delete " + walFile.getAbsolutePath());
            }
            final File shmFile = new File(dbFile.getAbsolutePath() + "-shm");
            if (shmFile.exists() && !shmFile.delete()) {
                Log.e("DraftsDb.deleteDb: cannot delete " + shmFile.getAbsolutePath());
            }
        }
    }

}

package com.halloapp.posts;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PostsDb {

    private static PostsDb instance;

    private Executor databaseWriteExecutor = Executors.newSingleThreadExecutor();

    private final DatabaseHelper databaseHelper;
    private final Set<Observer> observers = new HashSet<>();

    public interface Observer {
        void onPostAdded(@NonNull Post post);
        void onPostDeleted(@NonNull Post post);
    }

    public static PostsDb getInstance(final @NonNull Context context) {
        if (instance == null) {
            synchronized(PostsDb.class) {
                if (instance == null) {
                    instance = new PostsDb(context);
                }
            }
        }
        return instance;
    }

    private PostsDb(final @NonNull Context context) {
        databaseHelper = new DatabaseHelper(context.getApplicationContext());
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

    private void notifyPostAdded(@NonNull Post post) {
        synchronized (observers) {
            for (Observer observer : observers) {
                observer.onPostAdded(post);
            }
        }
    }

    private void notifyPostDeleted(@NonNull Post post) {
        synchronized (observers) {
            for (Observer observer : observers) {
                observer.onPostDeleted(post);
            }
        }
    }

    public void addPost(@NonNull Post post) {
        databaseWriteExecutor.execute(() -> {
            final ContentValues values = new ContentValues();
            values.put(PostsTable.COLUMN_POST_TEXT, post.text);
            final SQLiteDatabase db = databaseHelper.getReadableDatabase();
            db.replaceOrThrow(PostsTable.TABLE_NAME, null, values);
            notifyPostAdded(post);
        });
    }

    public List<Post> getPosts(final @Nullable Long id, final int count, final boolean after) {
        final List<Post> posts = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.query(PostsTable.TABLE_NAME,
                new String[] { PostsTable._ID,
                        PostsTable.COLUMN_CHAT_JID,
                        PostsTable.COLUMN_SENDER_JID,
                        PostsTable.COLUMN_POST_ID,
                        PostsTable.COLUMN_POST_GROUP_ID,
                        PostsTable.COLUMN_POST_TIMESTAMP,
                        PostsTable.COLUMN_POST_STATUS,
                        PostsTable.COLUMN_POST_TYPE,
                        PostsTable.COLUMN_POST_QUOTE_ID,
                        PostsTable.COLUMN_POST_TEXT,
                        PostsTable.COLUMN_POST_MEDIA },
                id == null ? null : PostsTable._ID + (after ? " < " : " > ") + id, null,
                null, null,
                PostsTable._ID + " DESC",
                Integer.toString(count))) {

            while (cursor.moveToNext()) {
                final Post post = new Post();
                post.rowId = cursor.getLong(0);
                post.chatJid = cursor.getString(1);
                post.senderJid = cursor.getString(2);
                post.postId = cursor.getString(3);
                post.groupId = cursor.getString(4);
                post.timestamp = cursor.getLong(5);
                post.status = cursor.getInt(6);
                post.type = cursor.getInt(7);
                post.quotedRowId = cursor.getLong(8);
                post.text = cursor.getString(9);
                post.mediaFile = cursor.getString(10);
                posts.add(post);
            }
        }
        return posts;
    }

    private static final class PostsTable implements BaseColumns {

        private PostsTable() { }

        static final String TABLE_NAME = "posts";

        static final String COLUMN_CHAT_JID = "chat_jid";
        static final String COLUMN_SENDER_JID = "sender_jid";
        static final String COLUMN_POST_ID = "post_id";
        static final String COLUMN_POST_GROUP_ID = "group_id";
        static final String COLUMN_POST_TIMESTAMP = "timestamp";
        static final String COLUMN_POST_STATUS = "status";
        static final String COLUMN_POST_TYPE = "type";
        static final String COLUMN_POST_QUOTE_ID = "quote_id";
        static final String COLUMN_POST_TEXT = "text";
        static final String COLUMN_POST_MEDIA = "media";
    }

    private class DatabaseHelper extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = "posts.db";
        private static final int DATABASE_VERSION = 1;

        DatabaseHelper(final @NonNull Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            setWriteAheadLoggingEnabled(true);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + PostsTable.TABLE_NAME);
            db.execSQL("CREATE TABLE " + PostsTable.TABLE_NAME + " ("
                    + PostsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + PostsTable.COLUMN_CHAT_JID + " TEXT,"
                    + PostsTable.COLUMN_SENDER_JID + " TEXT,"
                    + PostsTable.COLUMN_POST_ID + " TEXT,"
                    + PostsTable.COLUMN_POST_GROUP_ID + " TEXT,"
                    + PostsTable.COLUMN_POST_TIMESTAMP + " INTEGER,"
                    + PostsTable.COLUMN_POST_STATUS + " INTEGER,"
                    + PostsTable.COLUMN_POST_TYPE + " INTEGER,"
                    + PostsTable.COLUMN_POST_QUOTE_ID + " INTEGER,"
                    + PostsTable.COLUMN_POST_TEXT + " TEXT,"
                    + PostsTable.COLUMN_POST_MEDIA + " TEXT"
                    + ");");

            // testing-only
            for (int i = 0; i < 77; i++) {
                final ContentValues values = new ContentValues();
                values.put(PostsTable.COLUMN_POST_TEXT, "This is post #" + i + ". I'll make " + (77 - i) + " more posts today and " + (i*2) + " posts tomorrow.");
                db.replaceOrThrow(PostsTable.TABLE_NAME, null, values);
            }
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
    }
}

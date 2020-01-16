package com.halloapp.posts;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.halloapp.contacts.UserId;
import com.halloapp.media.MediaStore;
import com.halloapp.media.MediaUtils;
import com.halloapp.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PostsDb {

    private static PostsDb instance;

    private final Executor databaseWriteExecutor = Executors.newSingleThreadExecutor();

    private final DatabaseHelper databaseHelper;
    private final MediaStore mediaStore;
    private final Set<Observer> observers = new HashSet<>();

    public interface Observer {
        void onPostAdded(@NonNull Post post);
        void onPostDuplicate(@NonNull Post post);
        void onPostDeleted(@NonNull Post post);
        void onPostUpdated(@NonNull String chatId, @NonNull UserId senderUserId, @NonNull String postId);
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
        mediaStore = MediaStore.getInstance(context);
    }

    public void addObserver(@NonNull Observer observer) {
        synchronized (observers) {
            observers.add(observer);
        }
    }

    public void removeObserver(@NonNull Observer observer) {
        synchronized (observers) {
            observers.remove(observer);
        }
    }

    public void addPost(@NonNull Post post) {
        databaseWriteExecutor.execute(() -> {
            final ContentValues values = new ContentValues();
            values.put(PostsTable.COLUMN_CHAT_ID, post.chatId);
            values.put(PostsTable.COLUMN_SENDER_USER_ID, post.senderUserId.rawId());
            values.put(PostsTable.COLUMN_POST_ID, post.postId);
            if (post.groupId != null) {
                values.put(PostsTable.COLUMN_POST_GROUP_ID, post.groupId);
            }
            values.put(PostsTable.COLUMN_POST_PARENT_ID, post.parentRowId);
            values.put(PostsTable.COLUMN_POST_TIMESTAMP, post.timestamp);
            values.put(PostsTable.COLUMN_POST_TRANSFERRED, post.transferred);
            values.put(PostsTable.COLUMN_POST_TYPE, post.type);
            if (post.text != null) {
                values.put(PostsTable.COLUMN_POST_TEXT, post.text);
            }
            if (post.url != null) {
                values.put(PostsTable.COLUMN_POST_URL, post.url);
            }
            if (post.file != null) {
                values.put(PostsTable.COLUMN_POST_FILE, post.file);
                if (post.width == 0 || post.height == 0) {
                    final Size dimensions = MediaUtils.getDimensions(mediaStore.getMediaFile(post.file));
                    post.width = dimensions.getWidth();
                    post.height = dimensions.getHeight();
                }
            }
            if (post.width > 0 && post.height > 0) {
                values.put(PostsTable.COLUMN_POST_WIDTH, post.width);
                values.put(PostsTable.COLUMN_POST_HEIGHT, post.height);
            }
            final SQLiteDatabase db = databaseHelper.getReadableDatabase();
            try {
                db.insertWithOnConflict(PostsTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_ABORT);
                notifyPostAdded(post);
                Log.i("PostsDb.addPost: added " + post.keyString());
            } catch (SQLiteConstraintException ex) {
                Log.w("PostsDb.addPost: duplicate " + post.keyString());
                notifyPostDuplicate(post);
            }
        });
    }

    public void deletePost(@NonNull Post post) {
        databaseWriteExecutor.execute(() -> {
            final SQLiteDatabase db = databaseHelper.getReadableDatabase();
            db.delete(PostsTable.TABLE_NAME, PostsTable._ID + "=?", new String[] {Long.toString(post.rowId)});
            notifyPostDeleted(post);
        });
    }

    public void setPostTransferred(@NonNull String chatId, @NonNull UserId senderUserId, @NonNull String postId) {
        databaseWriteExecutor.execute(() -> {
            Log.i("PostsDb.setPostTransferred: chatId=" + chatId + " senderUserId=" + senderUserId + " postId=" + postId);
            final ContentValues values = new ContentValues();
            values.put(PostsTable.COLUMN_POST_TRANSFERRED, true);
            final SQLiteDatabase db = databaseHelper.getReadableDatabase();
            try {
                db.updateWithOnConflict(PostsTable.TABLE_NAME, values,
                        PostsTable.COLUMN_CHAT_ID + "=? AND " + PostsTable.COLUMN_SENDER_USER_ID + "=? AND " + PostsTable.COLUMN_POST_ID + "=?",
                        new String [] {chatId, senderUserId.rawId(), postId},
                        SQLiteDatabase.CONFLICT_ABORT);
                notifyPostUpdated(chatId, senderUserId, postId);
            } catch (SQLException ex) {
                Log.e("PostsDb.setPostState: failed");
                throw ex;
            }
        });
    }

    public void setPostFile(@NonNull String chatId, @NonNull UserId senderUserId, @NonNull String postId, @NonNull String file) {
        databaseWriteExecutor.execute(() -> {
            Log.i("PostsDb.setPostFile: chatId=" + chatId + " senderUserId=" + senderUserId + " postId=" + postId + " file=" + file);
            final ContentValues values = new ContentValues();
            values.put(PostsTable.COLUMN_POST_FILE, file);
            final Size dimensions = MediaUtils.getDimensions(mediaStore.getMediaFile(file));
            if (dimensions.getWidth() > 0 && dimensions.getHeight() > 0) {
                values.put(PostsTable.COLUMN_POST_WIDTH, dimensions.getWidth());
                values.put(PostsTable.COLUMN_POST_HEIGHT, dimensions.getHeight());
            }
            values.put(PostsTable.COLUMN_POST_TRANSFERRED, true);
            final SQLiteDatabase db = databaseHelper.getReadableDatabase();
            try {
                db.updateWithOnConflict(PostsTable.TABLE_NAME, values,
                        PostsTable.COLUMN_CHAT_ID + "=? AND " + PostsTable.COLUMN_SENDER_USER_ID + "=? AND " + PostsTable.COLUMN_POST_ID + "=?",
                        new String [] {chatId, senderUserId.rawId(), postId},
                        SQLiteDatabase.CONFLICT_ABORT);
                notifyPostUpdated(chatId, senderUserId, postId);
            } catch (SQLException ex) {
                Log.e("PostsDb.setPostFile: failed", ex);
                throw ex;
            }
        });
    }

    public void setPostUrl(@NonNull String chatId, @NonNull UserId senderUserId, @NonNull String postId, @NonNull String url) {
        databaseWriteExecutor.execute(() -> {
            Log.i("PostsDb.setPostUrl: chatId=" + chatId + " senderUserId=" + senderUserId + " postId=" + postId + " url=" + url);
            final ContentValues values = new ContentValues();
            values.put(PostsTable.COLUMN_POST_URL, url);
            final SQLiteDatabase db = databaseHelper.getReadableDatabase();
            try {
                db.updateWithOnConflict(PostsTable.TABLE_NAME, values,
                        PostsTable.COLUMN_CHAT_ID + "=? AND " + PostsTable.COLUMN_SENDER_USER_ID + "=? AND " + PostsTable.COLUMN_POST_ID + "=?",
                        new String [] {chatId, senderUserId.rawId(), postId},
                        SQLiteDatabase.CONFLICT_ABORT);
                notifyPostUpdated(chatId, senderUserId, postId);
            } catch (SQLException ex) {
                Log.e("PostsDb.setPostState: failed", ex);
                throw ex;
            }
        });
    }

    @WorkerThread
    public List<Post> getPosts(@Nullable Long id, int count, boolean after) {
        final List<Post> posts = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.query(PostsTable.TABLE_NAME,
                new String[] { PostsTable._ID,
                        PostsTable.COLUMN_CHAT_ID,
                        PostsTable.COLUMN_SENDER_USER_ID,
                        PostsTable.COLUMN_POST_ID,
                        PostsTable.COLUMN_POST_GROUP_ID,
                        PostsTable.COLUMN_POST_PARENT_ID,
                        PostsTable.COLUMN_POST_TIMESTAMP,
                        PostsTable.COLUMN_POST_TRANSFERRED,
                        PostsTable.COLUMN_POST_TYPE,
                        PostsTable.COLUMN_POST_TEXT,
                        PostsTable.COLUMN_POST_URL,
                        PostsTable.COLUMN_POST_FILE,
                        PostsTable.COLUMN_POST_WIDTH,
                        PostsTable.COLUMN_POST_HEIGHT
                },
                id == null ? null : PostsTable._ID + (after ? " < " : " > ") + id, null,
                null, null,
                PostsTable._ID + " DESC",
                Integer.toString(count))) {

            while (cursor.moveToNext()) {
                final Post post = new Post(
                        cursor.getLong(0),
                        cursor.getString(1),
                        new UserId(cursor.getString(2)),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getLong(5),
                        cursor.getLong(6),
                        cursor.getInt(7) == 1,
                        cursor.getInt(8),
                        cursor.getString(9),
                        cursor.getString(10),
                        cursor.getString(11),
                        cursor.getInt(12),
                        cursor.getInt(13));
                posts.add(post);
            }
        }
        Log.i("PostsDb.getPosts: start=" + id + " count=" + count + " after=" + after + " posts.size=" + posts.size() + (posts.isEmpty() ? "" : (" got posts from " + posts.get(0).rowId + " to " + posts.get(posts.size()-1).rowId)));
        return posts;
    }

    private void notifyPostAdded(@NonNull Post post) {
        synchronized (observers) {
            for (Observer observer : observers) {
                observer.onPostAdded(post);
            }
        }
    }

    private void notifyPostDuplicate(@NonNull Post post) {
        synchronized (observers) {
            for (Observer observer : observers) {
                observer.onPostDuplicate(post);
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

    private void notifyPostUpdated(@NonNull String chatId, @NonNull UserId senderUserId, @NonNull String postId) {
        synchronized (observers) {
            for (Observer observer : observers) {
                observer.onPostUpdated(chatId, senderUserId, postId);
            }
        }
    }

    private static final class PostsTable implements BaseColumns {

        private PostsTable() { }

        static final String TABLE_NAME = "posts";

        static final String INDEX_POST_KEY = "post_key";

        static final String COLUMN_CHAT_ID = "chat_id";
        static final String COLUMN_SENDER_USER_ID = "sender_user_id";
        static final String COLUMN_POST_ID = "post_id";
        static final String COLUMN_POST_GROUP_ID = "group_id";
        static final String COLUMN_POST_PARENT_ID = "parent_id";
        static final String COLUMN_POST_TIMESTAMP = "timestamp";
        static final String COLUMN_POST_TRANSFERRED = "transferred";
        static final String COLUMN_POST_TYPE = "type";
        static final String COLUMN_POST_TEXT = "text";
        static final String COLUMN_POST_URL = "url";
        static final String COLUMN_POST_FILE = "file";
        static final String COLUMN_POST_WIDTH = "width";
        static final String COLUMN_POST_HEIGHT = "height";
    }

    private class DatabaseHelper extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = "posts.db";
        private static final int DATABASE_VERSION = 2;

        DatabaseHelper(final @NonNull Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            setWriteAheadLoggingEnabled(true);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + PostsTable.TABLE_NAME);
            db.execSQL("CREATE TABLE " + PostsTable.TABLE_NAME + " ("
                    + PostsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + PostsTable.COLUMN_CHAT_ID + " TEXT NOT NULL,"
                    + PostsTable.COLUMN_SENDER_USER_ID + " TEXT NOT NULL,"
                    + PostsTable.COLUMN_POST_ID + " TEXT NOT NULL,"
                    + PostsTable.COLUMN_POST_GROUP_ID + " TEXT,"
                    + PostsTable.COLUMN_POST_PARENT_ID + " INTEGER,"
                    + PostsTable.COLUMN_POST_TIMESTAMP + " INTEGER,"
                    + PostsTable.COLUMN_POST_TRANSFERRED + " INTEGER,"
                    + PostsTable.COLUMN_POST_TYPE + " INTEGER,"
                    + PostsTable.COLUMN_POST_TEXT + " TEXT,"
                    + PostsTable.COLUMN_POST_URL + " TEXT,"
                    + PostsTable.COLUMN_POST_FILE + " TEXT,"
                    + PostsTable.COLUMN_POST_WIDTH + " INTEGER,"
                    + PostsTable.COLUMN_POST_HEIGHT + " INTEGER"
                    + ");");

            db.execSQL("DROP INDEX IF EXISTS " + PostsTable.INDEX_POST_KEY);
            db.execSQL("CREATE UNIQUE INDEX " + PostsTable.INDEX_POST_KEY + " ON " + PostsTable.TABLE_NAME + "("
                    + PostsTable.COLUMN_CHAT_ID + ", "
                    + PostsTable.COLUMN_SENDER_USER_ID + ", "
                    + PostsTable.COLUMN_POST_ID
                    + ");");
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

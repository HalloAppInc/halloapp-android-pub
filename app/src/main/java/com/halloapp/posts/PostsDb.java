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
import androidx.core.util.Preconditions;

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
        void onPostUpdated(@NonNull UserId senderUserId, @NonNull String postId);
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
            values.put(PostsTable.COLUMN_SENDER_USER_ID, post.senderUserId.rawId());
            values.put(PostsTable.COLUMN_POST_ID, post.postId);
            values.put(PostsTable.COLUMN_TIMESTAMP, post.timestamp);
            values.put(PostsTable.COLUMN_TRANSFERRED, post.transferred);
            if (post.text != null) {
                values.put(PostsTable.COLUMN_TEXT, post.text);
            }
            final SQLiteDatabase db = databaseHelper.getReadableDatabase();
            db.beginTransaction();
            try {
                try {
                    post.rowId = db.insertWithOnConflict(PostsTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_ABORT);
                    for (Media mediaItem : post.media) {
                        final ContentValues mediaItemValues = new ContentValues();
                        mediaItemValues.put(MediaTable.COLUMN_MEDIA_ID, mediaItem.id);
                        mediaItemValues.put(MediaTable.COLUMN_POST_ROW_ID, post.rowId);
                        if (mediaItem.url != null) {
                            mediaItemValues.put(MediaTable.COLUMN_URL, mediaItem.url);
                        }
                        if (mediaItem.file != null) {
                            mediaItemValues.put(MediaTable.COLUMN_FILE, mediaItem.file);
                            if (mediaItem.width == 0 || mediaItem.height == 0) {
                                final Size dimensions = MediaUtils.getDimensions(mediaStore.getMediaFile(mediaItem.file));
                                mediaItem.width = dimensions.getWidth();
                                mediaItem.height = dimensions.getHeight();
                            }
                        }
                        if (mediaItem.width > 0 && mediaItem.height > 0) {
                            mediaItemValues.put(MediaTable.COLUMN_WIDTH, mediaItem.width);
                            mediaItemValues.put(MediaTable.COLUMN_HEIGHT, mediaItem.height);
                        }
                        db.insertWithOnConflict(MediaTable.TABLE_NAME, null, mediaItemValues, SQLiteDatabase.CONFLICT_IGNORE);
                    }
                    notifyPostAdded(post);
                    Log.i("PostsDb.addPost: added " + post.keyString());
                } catch (SQLiteConstraintException ex) {
                    Log.w("PostsDb.addPost: duplicate " + post.keyString());
                    notifyPostDuplicate(post);
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
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

    public void setPostTransferred(@NonNull UserId senderUserId, @NonNull String postId) {
        databaseWriteExecutor.execute(() -> {
            Log.i("PostsDb.setPostTransferred: senderUserId=" + senderUserId + " postId=" + postId);
            final ContentValues values = new ContentValues();
            values.put(PostsTable.COLUMN_TRANSFERRED, true);
            final SQLiteDatabase db = databaseHelper.getReadableDatabase();
            try {
                db.updateWithOnConflict(PostsTable.TABLE_NAME, values,
                        PostsTable.COLUMN_SENDER_USER_ID + "=? AND " + PostsTable.COLUMN_POST_ID + "=?",
                        new String [] {senderUserId.rawId(), postId},
                        SQLiteDatabase.CONFLICT_ABORT);
                notifyPostUpdated(senderUserId, postId);
            } catch (SQLException ex) {
                Log.e("PostsDb.setPostTransferred: failed");
                throw ex;
            }
        });
    }

    public void setMediaTransferred(@NonNull Post post, @NonNull Media media) {
        databaseWriteExecutor.execute(() -> {
            Log.i("PostsDb.setMediaTransferred: post=" + post + " media=" + media);
            final ContentValues values = new ContentValues();
            values.put(MediaTable.COLUMN_FILE, media.file);
            values.put(MediaTable.COLUMN_URL, media.url);
            if (media.width == 0 || media.height == 0) {
                final Size dimensions = MediaUtils.getDimensions(mediaStore.getMediaFile(media.file));
                if (dimensions.getWidth() > 0 && dimensions.getHeight() > 0) {
                    values.put(MediaTable.COLUMN_WIDTH, dimensions.getWidth());
                    values.put(MediaTable.COLUMN_HEIGHT, dimensions.getHeight());
                }
            }
            values.put(MediaTable.COLUMN_TRANSFERRED, true);
            final SQLiteDatabase db = databaseHelper.getReadableDatabase();
            try {
                db.updateWithOnConflict(MediaTable.TABLE_NAME, values,
                        MediaTable.COLUMN_MEDIA_ID + "=?",
                        new String [] {media.id},
                        SQLiteDatabase.CONFLICT_ABORT);
                notifyPostUpdated(post.senderUserId, post.postId);
            } catch (SQLException ex) {
                Log.e("PostsDb.setMediaTransferred: failed", ex);
                throw ex;
            }
            if (post.isIncoming()) {
                boolean transferred = true;
                for (Media mediaItem : post.media) {
                    if (!mediaItem.transferred) {
                        transferred = false;
                        break;
                    }
                }
                if (transferred) {
                    setPostTransferred(post.senderUserId, post.postId);
                }
            }
        });
    }

    @WorkerThread
    public List<Post> getPosts(@Nullable Long id, int count, boolean after) {
        final List<Post> posts = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String where = id == null ? null : PostsTable.TABLE_NAME + "." + PostsTable._ID + (after ? " < " : " > ") + id;
        String sql =
            "SELECT " +
                PostsTable.TABLE_NAME + "." + PostsTable._ID + "," +
                PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_SENDER_USER_ID + "," +
                PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_POST_ID + "," +
                PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TIMESTAMP + "," +
                PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TRANSFERRED + "," +
                PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TEXT + "," +
                "m." + MediaTable.COLUMN_MEDIA_ID + "," +
                "m." + MediaTable.COLUMN_TYPE + "," +
                "m." + MediaTable.COLUMN_URL + "," +
                "m." + MediaTable.COLUMN_FILE + "," +
                "m." + MediaTable.COLUMN_WIDTH + "," +
                "m." + MediaTable.COLUMN_HEIGHT + "," +
                "m." + MediaTable.COLUMN_TRANSFERRED + " " +
             "FROM " +
                PostsTable.TABLE_NAME + " LEFT JOIN " +
                "(SELECT " +
                    MediaTable.COLUMN_POST_ROW_ID + "," +
                    MediaTable.COLUMN_MEDIA_ID + "," +
                    MediaTable.COLUMN_TYPE + "," +
                    MediaTable.COLUMN_URL + "," +
                    MediaTable.COLUMN_FILE + "," +
                    MediaTable.COLUMN_WIDTH + "," +
                    MediaTable.COLUMN_HEIGHT + "," +
                    MediaTable.COLUMN_TRANSFERRED + " " +
                "FROM " + MediaTable.TABLE_NAME + ") AS m ON " + PostsTable.TABLE_NAME + "." + PostsTable._ID + "=m." + MediaTable.COLUMN_POST_ROW_ID + " " +
            (where == null ? "" : "WHERE " + where + " ") +
            "ORDER BY " + PostsTable.TABLE_NAME + "." + PostsTable._ID + " DESC " +
            "LIMIT " + count;

        try (final Cursor cursor = db.rawQuery(sql, null)) {

            long lastRowId = -1;
            Post post = null;
            while (cursor.moveToNext()) {
                long rowId = cursor.getLong(0);
                if (lastRowId != rowId) {
                    lastRowId = rowId;
                    if (post != null) {
                        posts.add(post);
                    }
                    post = new Post(
                            rowId,
                            new UserId(cursor.getString(1)),
                            cursor.getString(2),
                            cursor.getLong(3),
                            cursor.getInt(4) == 1,
                            cursor.getString(5));
                }
                final String mediaId = cursor.getString(6);
                if (mediaId != null) {
                    Preconditions.checkNotNull(post).media.add(new Media(
                            mediaId,
                            cursor.getInt(7),
                            cursor.getString(8),
                            cursor.getString(9),
                            cursor.getInt(10),
                            cursor.getInt(11),
                            cursor.getInt(12) == 1));
                }
            }
            if (post != null && cursor.getCount() < count) {
                posts.add(post);
            }
        }
        Log.i("PostsDb.getPosts: start=" + id + " count=" + count + " after=" + after + " posts.size=" + posts.size() + (posts.isEmpty() ? "" : (" got posts from " + posts.get(0).rowId + " to " + posts.get(posts.size()-1).rowId)));
        return posts;
    }

    public List<Post> getPendingPosts() {


        final List<Post> posts = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String sql =
                "SELECT " +
                        PostsTable.TABLE_NAME + "." + PostsTable._ID + "," +
                        PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_SENDER_USER_ID + "," +
                        PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_POST_ID + "," +
                        PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TIMESTAMP + "," +
                        PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TRANSFERRED + "," +
                        PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TEXT + "," +
                        "m." + MediaTable.COLUMN_MEDIA_ID + "," +
                        "m." + MediaTable.COLUMN_TYPE + "," +
                        "m." + MediaTable.COLUMN_URL + "," +
                        "m." + MediaTable.COLUMN_FILE + "," +
                        "m." + MediaTable.COLUMN_WIDTH + "," +
                        "m." + MediaTable.COLUMN_HEIGHT + "," +
                        "m." + MediaTable.COLUMN_TRANSFERRED + " " +
                        "FROM " +
                        PostsTable.TABLE_NAME + " LEFT JOIN " +
                        "(SELECT " +
                        MediaTable.COLUMN_POST_ROW_ID + "," +
                        MediaTable.COLUMN_MEDIA_ID + "," +
                        MediaTable.COLUMN_TYPE + "," +
                        MediaTable.COLUMN_URL + "," +
                        MediaTable.COLUMN_FILE + "," +
                        MediaTable.COLUMN_WIDTH + "," +
                        MediaTable.COLUMN_HEIGHT + "," +
                        MediaTable.COLUMN_TRANSFERRED + " " +
                        "FROM " + MediaTable.TABLE_NAME + ") AS m ON " + PostsTable.TABLE_NAME + "." + PostsTable._ID + "=m." + MediaTable.COLUMN_POST_ROW_ID + " " +
                        "WHERE " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TRANSFERRED + "=0 " +
                        "ORDER BY " + PostsTable.TABLE_NAME + "." + PostsTable._ID + " DESC ";

        try (final Cursor cursor = db.rawQuery(sql, null)) {

            long lastRowId = -1;
            Post post = null;
            while (cursor.moveToNext()) {
                long rowId = cursor.getLong(0);
                if (lastRowId != rowId) {
                    lastRowId = rowId;
                    if (post != null) {
                        posts.add(post);
                    }
                    post = new Post(
                            rowId,
                            new UserId(cursor.getString(1)),
                            cursor.getString(2),
                            cursor.getLong(3),
                            cursor.getInt(4) == 1,
                            cursor.getString(5));
                }
                final String mediaId = cursor.getString(6);
                if (mediaId != null) {
                    Preconditions.checkNotNull(post).media.add(new Media(
                            mediaId,
                            cursor.getInt(7),
                            cursor.getString(8),
                            cursor.getString(9),
                            cursor.getInt(10),
                            cursor.getInt(11),
                            cursor.getInt(12) == 1));
                }
            }
            if (post != null) {
                posts.add(post);
            }
        }
        Log.i("PostsDb.getPendingPosts: posts.size=" + posts.size());
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

    private void notifyPostUpdated(@NonNull UserId senderUserId, @NonNull String postId) {
        synchronized (observers) {
            for (Observer observer : observers) {
                observer.onPostUpdated(senderUserId, postId);
            }
        }
    }

    private static final class PostsTable implements BaseColumns {

        private PostsTable() { }

        static final String TABLE_NAME = "posts";

        static final String INDEX_POST_KEY = "post_key";

        static final String COLUMN_SENDER_USER_ID = "sender_user_id";
        static final String COLUMN_POST_ID = "post_id";
        static final String COLUMN_TIMESTAMP = "timestamp";
        static final String COLUMN_TRANSFERRED = "transferred";
        static final String COLUMN_TEXT = "text";
    }

    private static final class MediaTable implements BaseColumns {

        private MediaTable() { }

        static final String TABLE_NAME = "media";

        static final String COLUMN_MEDIA_ID = "id";
        static final String COLUMN_POST_ROW_ID = "post_row_id";
        static final String COLUMN_TYPE = "type";
        static final String COLUMN_TRANSFERRED = "transferred";
        static final String COLUMN_URL = "url";
        static final String COLUMN_FILE = "file";
        static final String COLUMN_WIDTH = "width";
        static final String COLUMN_HEIGHT = "height";
    }

    private class DatabaseHelper extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = "posts.db";
        private static final int DATABASE_VERSION = 3;

        DatabaseHelper(final @NonNull Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            setWriteAheadLoggingEnabled(true);
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
                    + MediaTable.COLUMN_WIDTH + " INTEGER,"
                    + MediaTable.COLUMN_HEIGHT + " INTEGER"
                    + ");");

            db.execSQL("DROP INDEX IF EXISTS " + PostsTable.INDEX_POST_KEY);
            db.execSQL("CREATE UNIQUE INDEX " + PostsTable.INDEX_POST_KEY + " ON " + PostsTable.TABLE_NAME + "("
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

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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PostsDb {

    private static PostsDb instance;

    private final Executor databaseWriteExecutor = Executors.newSingleThreadExecutor();

    private final Context context;
    private final DatabaseHelper databaseHelper;
    private final MediaStore mediaStore;
    private final Set<Observer> observers = new HashSet<>();

    public interface Observer {
        void onPostAdded(@NonNull Post post);
        void onPostDuplicate(@NonNull Post post);
        void onPostDeleted(@NonNull Post post);
        void onPostUpdated(@NonNull UserId senderUserId, @NonNull String postId);
        void onCommentAdded(@NonNull Comment comment);
        void onCommentDuplicate(@NonNull Comment comment);
        void onCommentUpdated(@NonNull UserId postSenderUserId, @NonNull String postId, @NonNull UserId commentSenderUserId, @NonNull String commentId);
        void onCommentsSeen(@NonNull UserId postSenderUserId, @NonNull String postId);
        void onHistoryAdded(@NonNull Collection<Post> historyPosts, @NonNull Collection<Comment> historyComments);
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
        this.context = context.getApplicationContext();
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
            final SQLiteDatabase db = databaseHelper.getReadableDatabase();
            db.beginTransaction();
            try {
                insertPost(post);
                notifyPostAdded(post);
                Log.i("PostsDb.addPost: added " + post);
                db.setTransactionSuccessful();
            } catch (SQLiteConstraintException ex) {
                Log.w("PostsDb.addPost: duplicate " + post);
                notifyPostDuplicate(post);
            } finally {
                db.endTransaction();
            }
        });
    }

    @WorkerThread
    private void insertPost(@NonNull Post post) {
        final ContentValues values = new ContentValues();
        values.put(PostsTable.COLUMN_SENDER_USER_ID, post.senderUserId.rawId());
        values.put(PostsTable.COLUMN_POST_ID, post.postId);
        values.put(PostsTable.COLUMN_TIMESTAMP, post.timestamp);
        values.put(PostsTable.COLUMN_TRANSFERRED, post.transferred);
        if (post.text != null) {
            values.put(PostsTable.COLUMN_TEXT, post.text);
        }
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        post.rowId = db.insertWithOnConflict(PostsTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_ABORT);
        for (Media mediaItem : post.media) {
            final ContentValues mediaItemValues = new ContentValues();
            mediaItemValues.put(MediaTable.COLUMN_MEDIA_ID, mediaItem.id);
            mediaItemValues.put(MediaTable.COLUMN_POST_ROW_ID, post.rowId);
            if (mediaItem.url != null) {
                mediaItemValues.put(MediaTable.COLUMN_URL, mediaItem.url);
            }
            if (mediaItem.file != null) {
                mediaItemValues.put(MediaTable.COLUMN_FILE, mediaItem.file.getName());
                if (mediaItem.width == 0 || mediaItem.height == 0) {
                    final Size dimensions = MediaUtils.getDimensions(mediaItem.file);
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
            values.put(MediaTable.COLUMN_FILE, media.file.getName());
            values.put(MediaTable.COLUMN_URL, media.url);
            if (media.width == 0 || media.height == 0) {
                final Size dimensions = MediaUtils.getDimensions(media.file);
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

    public void addComment(@NonNull Comment comment) {
        databaseWriteExecutor.execute(() -> {
            try {
                insertComment(comment);
                notifyCommentAdded(comment);
                Log.i("PostsDb.addComment: added " + comment);
            } catch (SQLiteConstraintException ex) {
                Log.w("PostsDb.addComment: duplicate " + ex.getMessage() + " " + comment);
                notifyCommentDuplicate(comment);
            }
        });
    }

    @WorkerThread
    private void insertComment(@NonNull Comment comment) {
        final ContentValues values = new ContentValues();
        values.put(CommentsTable.COLUMN_POST_SENDER_USER_ID, comment.postSenderUserId.rawId());
        values.put(CommentsTable.COLUMN_POST_ID, comment.postId);
        values.put(CommentsTable.COLUMN_COMMENT_SENDER_USER_ID, comment.commentSenderUserId.rawId());
        values.put(CommentsTable.COLUMN_COMMENT_ID, comment.commentId);
        values.put(CommentsTable.COLUMN_PARENT_ID, comment.parentCommentId);
        values.put(CommentsTable.COLUMN_TIMESTAMP, comment.timestamp);
        values.put(CommentsTable.COLUMN_TRANSFERRED, comment.transferred);
        values.put(CommentsTable.COLUMN_SEEN, comment.seen);
        values.put(CommentsTable.COLUMN_TEXT, comment.text);
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        comment.rowId = db.insertWithOnConflict(CommentsTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_ABORT);
    }

    public void setCommentTransferred(@NonNull UserId postSenderUserId, @NonNull String postId, @NonNull UserId commentSenderUserId, @NonNull String commentId) {
        databaseWriteExecutor.execute(() -> {
            Log.i("PostsDb.setCommentTransferred: senderUserId=" + commentSenderUserId + " commentId=" + commentId);
            final ContentValues values = new ContentValues();
            values.put(CommentsTable.COLUMN_TRANSFERRED, true);
            final SQLiteDatabase db = databaseHelper.getReadableDatabase();
            try {
                db.updateWithOnConflict(CommentsTable.TABLE_NAME, values,
                        CommentsTable.COLUMN_COMMENT_SENDER_USER_ID + "=? AND " + CommentsTable.COLUMN_COMMENT_ID + "=?",
                        new String [] {commentSenderUserId.rawId(), commentId},
                        SQLiteDatabase.CONFLICT_ABORT);
                notifyCommentUpdated(postSenderUserId, postId, commentSenderUserId, commentId);
            } catch (SQLException ex) {
                Log.e("PostsDb.setCommentTransferred: failed");
                throw ex;
            }
        });
    }

    public void setCommentsSeen(@NonNull UserId postSenderUserId, @NonNull String postId) {
        setCommentsSeen(postSenderUserId, postId, true);
    }

    public void setCommentsSeen(@NonNull UserId postSenderUserId, @NonNull String postId, boolean seen) {
        databaseWriteExecutor.execute(() -> {
            Log.i("PostsDb.setCommentsSeen: postSenderUserId=" + postSenderUserId+ " postId=" + postId);
            final ContentValues values = new ContentValues();
            values.put(CommentsTable.COLUMN_SEEN, seen);
            final SQLiteDatabase db = databaseHelper.getReadableDatabase();
            try {
                int updatedCount = db.updateWithOnConflict(CommentsTable.TABLE_NAME, values,
                        CommentsTable.COLUMN_POST_SENDER_USER_ID + "=? AND " + CommentsTable.COLUMN_POST_ID + "=?",
                        new String [] {postSenderUserId.rawId(), postId},
                        SQLiteDatabase.CONFLICT_ABORT);
                notifyCommentsSeen(postSenderUserId, postId);
            } catch (SQLException ex) {
                Log.e("PostsDb.setCommentsSeen: failed");
                throw ex;
            }
        });
    }

    public void addHistory(@NonNull Collection<Post> historyPosts, @NonNull Collection<Comment> historyComments) {
        databaseWriteExecutor.execute(() -> {
            final SQLiteDatabase db = databaseHelper.getReadableDatabase();
            db.beginTransaction();
            try {
                final Collection<Post> addedHistoryPosts = new ArrayList<>();
                final Collection<Comment> addedHistoryComments = new ArrayList<>();
                for (Post post : historyPosts) {
                    try {
                        insertPost(post);
                        addedHistoryPosts.add(post);
                        Log.i("PostsDb.addHistory: post added " + post);
                    } catch (SQLiteConstraintException ex) {
                        Log.i("PostsDb.addHistory: post duplicate " + post);
                    }
                }
                for (Comment comment : historyComments) {
                    try {
                        insertComment(comment);
                        addedHistoryComments.add(comment);
                        Log.i("PostsDb.addHistory: comment added " + comment);
                    } catch (SQLiteConstraintException ex) {
                        Log.i("PostsDb.addHistory: post duplicate " + comment);
                    }
                }
                if (!addedHistoryPosts.isEmpty() || !addedHistoryComments.isEmpty()) {
                    notifyHistoryAdded(addedHistoryPosts, addedHistoryComments);
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        });
    }

    @WorkerThread
    public List<Post> getPosts(@Nullable Long timestamp, int count, boolean after, boolean outgoingOnly) {
        final List<Post> posts = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String where = timestamp == null ? null : PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TIMESTAMP + (after ? " < " : " > ") + timestamp;
        if (outgoingOnly) {
            if (where == null) {
                where = "";
            } else {
                where += " AND ";
            }
            where += PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_SENDER_USER_ID + "=''";
        }
        String sql =
            "SELECT " +
                PostsTable.TABLE_NAME + "." + PostsTable._ID + "," +
                PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_SENDER_USER_ID + "," +
                PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_POST_ID + "," +
                PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TIMESTAMP + "," +
                PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TRANSFERRED + "," +
                PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TEXT + "," +
                "m." + MediaTable._ID + "," +
                "m." + MediaTable.COLUMN_MEDIA_ID + "," +
                "m." + MediaTable.COLUMN_TYPE + "," +
                "m." + MediaTable.COLUMN_URL + "," +
                "m." + MediaTable.COLUMN_FILE + "," +
                "m." + MediaTable.COLUMN_WIDTH + "," +
                "m." + MediaTable.COLUMN_HEIGHT + "," +
                "m." + MediaTable.COLUMN_TRANSFERRED + ", " +
                "c.comment_count" + ", " +
                "c.seen_comment_count" + " " +
            "FROM " + PostsTable.TABLE_NAME + " " +
            "LEFT JOIN (" +
                "SELECT " +
                    MediaTable._ID + "," +
                    MediaTable.COLUMN_POST_ROW_ID + "," +
                    MediaTable.COLUMN_MEDIA_ID + "," +
                    MediaTable.COLUMN_TYPE + "," +
                    MediaTable.COLUMN_URL + "," +
                    MediaTable.COLUMN_FILE + "," +
                    MediaTable.COLUMN_WIDTH + "," +
                    MediaTable.COLUMN_HEIGHT + "," +
                    MediaTable.COLUMN_TRANSFERRED + " FROM " + MediaTable.TABLE_NAME + " ORDER BY " + MediaTable._ID + " ASC) " +
                "AS m ON " + PostsTable.TABLE_NAME + "." + PostsTable._ID + "=m." + MediaTable.COLUMN_POST_ROW_ID + " " +
            "LEFT JOIN (" +
                "SELECT " +
                    CommentsTable.COLUMN_POST_SENDER_USER_ID + "," +
                    CommentsTable.COLUMN_POST_ID + "," +
                    "count(*) AS comment_count" + ", " +
                    "sum(" + CommentsTable.COLUMN_SEEN + ") AS seen_comment_count" + " " +
                    "FROM " + CommentsTable.TABLE_NAME + " GROUP BY " + CommentsTable.COLUMN_POST_SENDER_USER_ID+ ", " + CommentsTable.COLUMN_POST_ID + ") " +
                "AS c ON " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_SENDER_USER_ID + "=c." + CommentsTable.COLUMN_POST_SENDER_USER_ID + " AND " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_POST_ID + "=c." + CommentsTable.COLUMN_POST_ID + " " +
            (where == null ? "" : "WHERE " + where + " ") +
            "ORDER BY " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TIMESTAMP + " DESC " +
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
                    post.commentCount = cursor.getInt(14);
                    post.unseenCommentCount = post.commentCount - cursor.getInt(15);
                }
                final String mediaId = cursor.getString(7);
                if (mediaId != null) {
                    Preconditions.checkNotNull(post).media.add(new Media(
                            cursor.getLong(6),
                            mediaId,
                            cursor.getInt(8),
                            cursor.getString(9),
                            mediaStore.getMediaFile(cursor.getString(10)),
                            cursor.getInt(11),
                            cursor.getInt(12),
                            cursor.getInt(13) == 1));
                }
            }
            if (post != null && cursor.getCount() < count) {
                posts.add(post);
            }
        }
        Log.i("PostsDb.getPosts: start=" + timestamp + " count=" + count + " after=" + after + " posts.size=" + posts.size() + (posts.isEmpty() ? "" : (" got posts from " + posts.get(0).rowId + " to " + posts.get(posts.size()-1).rowId)));
        return posts;
    }

    @WorkerThread
    public @Nullable Post getPost(@NonNull UserId userId, @NonNull String postId) {
        Post post = null;
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        final String sql =
                "SELECT " +
                    PostsTable.TABLE_NAME + "." + PostsTable._ID + "," +
                    PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_SENDER_USER_ID + "," +
                    PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_POST_ID + "," +
                    PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TIMESTAMP + "," +
                    PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TRANSFERRED + "," +
                    PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TEXT + "," +
                    "m." + MediaTable._ID + "," +
                    "m." + MediaTable.COLUMN_MEDIA_ID + "," +
                    "m." + MediaTable.COLUMN_TYPE + "," +
                    "m." + MediaTable.COLUMN_URL + "," +
                    "m." + MediaTable.COLUMN_FILE + "," +
                    "m." + MediaTable.COLUMN_WIDTH + "," +
                    "m." + MediaTable.COLUMN_HEIGHT + "," +
                    "m." + MediaTable.COLUMN_TRANSFERRED + " " +
                "FROM " + PostsTable.TABLE_NAME + " " +
                "LEFT JOIN (" +
                    "SELECT " +
                        MediaTable._ID + "," +
                        MediaTable.COLUMN_POST_ROW_ID + "," +
                        MediaTable.COLUMN_MEDIA_ID + "," +
                        MediaTable.COLUMN_TYPE + "," +
                        MediaTable.COLUMN_URL + "," +
                        MediaTable.COLUMN_FILE + "," +
                        MediaTable.COLUMN_WIDTH + "," +
                        MediaTable.COLUMN_HEIGHT + "," +
                        MediaTable.COLUMN_TRANSFERRED + " FROM " + MediaTable.TABLE_NAME + ") " +
                    "AS m ON " + PostsTable.TABLE_NAME + "." + PostsTable._ID + "=m." + MediaTable.COLUMN_POST_ROW_ID + " " +
                "WHERE " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_SENDER_USER_ID + "=? AND " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_POST_ID + "=?";

        try (final Cursor cursor = db.rawQuery(sql, new String [] {userId.rawId(), postId})) {

            while (cursor.moveToNext()) {
                long rowId = cursor.getLong(0);
                if (post == null) {
                    post = new Post(
                            rowId,
                            new UserId(cursor.getString(1)),
                            cursor.getString(2),
                            cursor.getLong(3),
                            cursor.getInt(4) == 1,
                            cursor.getString(5));
                }
                final String mediaId = cursor.getString(7);
                if (mediaId != null) {
                    Preconditions.checkNotNull(post).media.add(new Media(
                            cursor.getLong(6),
                            mediaId,
                            cursor.getInt(8),
                            cursor.getString(9),
                            mediaStore.getMediaFile(cursor.getString(10)),
                            cursor.getInt(11),
                            cursor.getInt(12),
                            cursor.getInt(13) == 1));
                }
            }
        }
        Log.i("PostsDb.getPost: post=" + post);
        return post;
    }

    @WorkerThread
    public List<Comment> getComments(@NonNull UserId postSenderUserId, @NonNull String postId, int start, int count) {
        final String sql =
                "WITH RECURSIVE " +
                    "comments_tree(level, _id, timestamp, parent_id, comment_sender_user_id, comment_id, transferred, text) AS ( " +
                        "SELECT 0, _id, timestamp, parent_id, comment_sender_user_id, comment_id, transferred, text FROM comments WHERE post_sender_user_id=? AND post_id=? AND parent_id IS NULL " +
                        "UNION ALL " +
                        "SELECT comments_tree.level+1, comments._id, comments.timestamp, comments.parent_id, comments.comment_sender_user_id, comments.comment_id, comments.transferred, comments.text " +
                            "FROM comments, comments_tree WHERE comments.parent_id=comments_tree.comment_id ORDER BY 1 DESC) " +
                "SELECT * FROM comments_tree LIMIT " + count + " OFFSET " + start;
        final List<Comment> comments = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.rawQuery(sql, new String [] {postSenderUserId.rawId(), postId})) {
            while (cursor.moveToNext()) {
                final Comment comment = new Comment(
                        cursor.getLong(1),
                        postSenderUserId,
                        postId,
                        new UserId(cursor.getString(4)),
                        cursor.getString(5),
                        cursor.getString(3),
                        cursor.getLong(2),
                        cursor.getInt(6) == 1,
                        cursor.getString(7));
                comments.add(comment);
            }
        }
        Log.i("PostsDb.getComments: start=" + start + " count=" + count + " comments.size=" + comments.size());
        return comments;
    }

    /*
    * returns "important" comments only
    * */
    @WorkerThread
    public List<Comment> getIncomingCommentsHistory(int limit) {
        final List<Comment> comments = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();

        final String sql =
                "SELECT " +
                    CommentsTable._ID + ", " +
                    CommentsTable.COLUMN_POST_SENDER_USER_ID + ", " +
                    CommentsTable.COLUMN_POST_ID + ", " +
                    CommentsTable.COLUMN_COMMENT_SENDER_USER_ID + ", " +
                    CommentsTable.COLUMN_COMMENT_ID + ", " +
                    CommentsTable.COLUMN_PARENT_ID + ", " +
                    CommentsTable.COLUMN_TIMESTAMP + ", " +
                    CommentsTable.COLUMN_TRANSFERRED + ", " +
                    CommentsTable.COLUMN_TEXT + ", " +
                    CommentsTable.COLUMN_SEEN + " " +
                "FROM " + CommentsTable.TABLE_NAME + " " +
                "WHERE comments.comment_sender_user_id<>'' " +
                    "AND EXISTS(SELECT post_id FROM posts WHERE posts.post_id=comments.post_id)" +
                    "AND (" +
                        "comments.post_sender_user_id='' " +
                        "OR " +
                        "EXISTS(SELECT post_id FROM comments AS c WHERE comments.post_id==c.post_id AND c.comment_sender_user_id='') " +
                    ")" +
                "ORDER BY " + CommentsTable.TABLE_NAME + "." + CommentsTable.COLUMN_TIMESTAMP + " DESC " +
                "LIMIT " + limit;
        try (final Cursor cursor = db.rawQuery(sql, null)) {
            while (cursor.moveToNext()) {
                final Comment comment = new Comment(
                        cursor.getLong(0),
                        new UserId(cursor.getString(1)),
                        cursor.getString(2),
                        new UserId(cursor.getString(3)),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getLong(6),
                        cursor.getInt(7) == 1,
                        cursor.getString(8));
                comment.seen = cursor.getInt(9) == 1;
                comments.add(comment);
            }
        }
        Log.i("PostsDb.getIncomingCommentsHistory: comments.size=" + comments.size());
        return comments;
    }

    @WorkerThread
    public List<Post> getPendingPosts() {

        final List<Post> posts = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        final String sql =
                "SELECT " +
                    PostsTable.TABLE_NAME + "." + PostsTable._ID + "," +
                    PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_SENDER_USER_ID + "," +
                    PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_POST_ID + "," +
                    PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TIMESTAMP + "," +
                    PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TRANSFERRED + "," +
                    PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TEXT + "," +
                    "m." + MediaTable._ID + "," +
                    "m." + MediaTable.COLUMN_MEDIA_ID + "," +
                    "m." + MediaTable.COLUMN_TYPE + "," +
                    "m." + MediaTable.COLUMN_URL + "," +
                    "m." + MediaTable.COLUMN_FILE + "," +
                    "m." + MediaTable.COLUMN_WIDTH + "," +
                    "m." + MediaTable.COLUMN_HEIGHT + "," +
                    "m." + MediaTable.COLUMN_TRANSFERRED + " " +
                "FROM " + PostsTable.TABLE_NAME + " " +
                "LEFT JOIN (" +
                    "SELECT " +
                        MediaTable._ID + "," +
                        MediaTable.COLUMN_POST_ROW_ID + "," +
                        MediaTable.COLUMN_MEDIA_ID + "," +
                        MediaTable.COLUMN_TYPE + "," +
                        MediaTable.COLUMN_URL + "," +
                        MediaTable.COLUMN_FILE + "," +
                        MediaTable.COLUMN_WIDTH + "," +
                        MediaTable.COLUMN_HEIGHT + "," +
                        MediaTable.COLUMN_TRANSFERRED + " FROM " + MediaTable.TABLE_NAME + ") " +
                    "AS m ON " + PostsTable.TABLE_NAME + "." + PostsTable._ID + "=m." + MediaTable.COLUMN_POST_ROW_ID + " " +
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
                final String mediaId = cursor.getString(7);
                if (mediaId != null) {
                    Preconditions.checkNotNull(post).media.add(new Media(
                            cursor.getLong(6),
                            mediaId,
                            cursor.getInt(8),
                            cursor.getString(9),
                            mediaStore.getMediaFile(cursor.getString(10)),
                            cursor.getInt(11),
                            cursor.getInt(12),
                            cursor.getInt(13) == 1));
                }
            }
            if (post != null) {
                posts.add(post);
            }
        }
        Log.i("PostsDb.getPendingPosts: posts.size=" + posts.size());
        return posts;
    }

    @WorkerThread
    public List<Comment> getPendingComments() {
        final List<Comment> comments = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.query(CommentsTable.TABLE_NAME,
                new String [] {
                        CommentsTable._ID,
                        CommentsTable.COLUMN_POST_SENDER_USER_ID,
                        CommentsTable.COLUMN_POST_ID,
                        CommentsTable.COLUMN_COMMENT_SENDER_USER_ID,
                        CommentsTable.COLUMN_COMMENT_ID,
                        CommentsTable.COLUMN_PARENT_ID,
                        CommentsTable.COLUMN_TIMESTAMP,
                        CommentsTable.COLUMN_TRANSFERRED,
                        CommentsTable.COLUMN_TEXT},
                CommentsTable.COLUMN_COMMENT_SENDER_USER_ID + "='' AND " + CommentsTable.COLUMN_TRANSFERRED + "=0",
                null, null, null, null)) {
            while (cursor.moveToNext()) {
                final Comment comment = new Comment(
                        cursor.getLong(0),
                        new UserId(cursor.getString(1)),
                        cursor.getString(2),
                        new UserId(cursor.getString(3)),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getLong(6),
                        cursor.getInt(7) == 1,
                        cursor.getString(8));
                comments.add(comment);
            }
        }
        Log.i("PostsDb.getPendingComments: comments.size=" + comments.size());
        return comments;
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

    private void notifyCommentAdded(@NonNull Comment comment) {
        synchronized (observers) {
            for (Observer observer : observers) {
                observer.onCommentAdded(comment);
            }
        }
    }

    private void notifyCommentDuplicate(@NonNull Comment comment) {
        synchronized (observers) {
            for (Observer observer : observers) {
                observer.onCommentDuplicate(comment);
            }
        }
    }

    private void notifyCommentUpdated(@NonNull UserId postSenderUserId, @NonNull String postId, @NonNull UserId commentSenderUserId, @NonNull String commentId) {
        synchronized (observers) {
            for (Observer observer : observers) {
                observer.onCommentUpdated(postSenderUserId, postId, commentSenderUserId, commentId);
            }
        }
    }

    private void notifyCommentsSeen(@NonNull UserId postSenderUserId, @NonNull String postId) {
        synchronized (observers) {
            for (Observer observer : observers) {
                observer.onCommentsSeen(postSenderUserId, postId);
            }
        }
    }


    private void notifyHistoryAdded(@NonNull Collection<Post> historyPosts, @NonNull Collection<Comment> historyComments) {
        synchronized (observers) {
            for (Observer observer : observers) {
                observer.onHistoryAdded(historyPosts, historyComments);
            }
        }
    }

    public void deleteDb() {
        databaseHelper.deleteDb();
    }

    private static final class PostsTable implements BaseColumns {

        private PostsTable() { }

        static final String TABLE_NAME = "posts";

        static final String INDEX_POST_KEY = "post_key";
        static final String INDEX_TIMESTAMP = "timestamp";

        static final String COLUMN_SENDER_USER_ID = "sender_user_id";
        static final String COLUMN_POST_ID = "post_id";
        static final String COLUMN_TIMESTAMP = "timestamp";
        static final String COLUMN_TRANSFERRED = "transferred";
        static final String COLUMN_SEEN = "seen";
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

    private static final class CommentsTable implements BaseColumns {

        private CommentsTable() { }

        static final String TABLE_NAME = "comments";

        static final String INDEX_COMMENT_KEY = "comment_key";

        static final String COLUMN_POST_SENDER_USER_ID = "post_sender_user_id";
        static final String COLUMN_POST_ID = "post_id";
        static final String COLUMN_COMMENT_SENDER_USER_ID = "comment_sender_user_id";
        static final String COLUMN_COMMENT_ID = "comment_id";
        static final String COLUMN_PARENT_ID = "parent_id";
        static final String COLUMN_TIMESTAMP = "timestamp";
        static final String COLUMN_TRANSFERRED = "transferred";
        static final String COLUMN_SEEN = "seen";
        static final String COLUMN_TEXT = "text";
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
            db.execSQL("CREATE UNIQUE INDEX " + PostsTable.INDEX_TIMESTAMP + " ON " + PostsTable.TABLE_NAME + "("
                    + PostsTable.COLUMN_TIMESTAMP
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

        private void deleteDb() {
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
}

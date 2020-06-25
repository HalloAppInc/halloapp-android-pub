package com.halloapp.content;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.core.util.Pair;

import com.halloapp.Constants;
import com.halloapp.FileStore;
import com.halloapp.contacts.UserId;
import com.halloapp.content.tables.CommentsTable;
import com.halloapp.content.tables.MediaTable;
import com.halloapp.content.tables.MentionsTable;
import com.halloapp.content.tables.PostsTable;
import com.halloapp.content.tables.SeenTable;
import com.halloapp.media.MediaUtils;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class PostsDb {

    private final MentionsDb mentionsDb;
    private final ContentDbHelper databaseHelper;
    private final FileStore fileStore;

    PostsDb(MentionsDb mentionsDb, ContentDbHelper databaseHelper, FileStore fileStore) {
        this.mentionsDb = mentionsDb;
        this.databaseHelper = databaseHelper;
        this.fileStore = fileStore;
    }

    @WorkerThread
    void addPost(@NonNull Post post) {
        Log.i("ContentDb.addPost " + post);
        if (post.timestamp < getPostExpirationTime()) {
            throw new SQLiteConstraintException("attempting to add expired post");
        }
        final ContentValues values = new ContentValues();
        values.put(PostsTable.COLUMN_SENDER_USER_ID, post.senderUserId.rawId());
        values.put(PostsTable.COLUMN_POST_ID, post.id);
        values.put(PostsTable.COLUMN_TIMESTAMP, post.timestamp);
        values.put(PostsTable.COLUMN_TRANSFERRED, post.transferred);
        values.put(PostsTable.COLUMN_SEEN, post.seen);
        if (post.text != null) {
            values.put(PostsTable.COLUMN_TEXT, post.text);
        }
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        post.rowId = db.insertWithOnConflict(PostsTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_ABORT);
        for (Media mediaItem : post.media) {
            final ContentValues mediaItemValues = new ContentValues();
            mediaItemValues.put(MediaTable.COLUMN_PARENT_TABLE, PostsTable.TABLE_NAME);
            mediaItemValues.put(MediaTable.COLUMN_PARENT_ROW_ID, post.rowId);
            mediaItemValues.put(MediaTable.COLUMN_TYPE, mediaItem.type);
            if (mediaItem.url != null) {
                mediaItemValues.put(MediaTable.COLUMN_URL, mediaItem.url);
            }
            if (mediaItem.file != null) {
                mediaItemValues.put(MediaTable.COLUMN_FILE, mediaItem.file.getName());
                if (mediaItem.width == 0 || mediaItem.height == 0) {
                    final Size dimensions = MediaUtils.getDimensions(mediaItem.file, mediaItem.type);
                    if (dimensions != null) {
                        mediaItem.width = dimensions.getWidth();
                        mediaItem.height = dimensions.getHeight();
                    }
                }
            }
            if (mediaItem.width > 0 && mediaItem.height > 0) {
                mediaItemValues.put(MediaTable.COLUMN_WIDTH, mediaItem.width);
                mediaItemValues.put(MediaTable.COLUMN_HEIGHT, mediaItem.height);
            }
            if (mediaItem.encKey != null) {
                mediaItemValues.put(MediaTable.COLUMN_ENC_KEY, mediaItem.encKey);
            }
            if (mediaItem.sha256hash != null) {
                mediaItemValues.put(MediaTable.COLUMN_SHA256_HASH, mediaItem.sha256hash);
            }
            mediaItem.rowId = db.insertWithOnConflict(MediaTable.TABLE_NAME, null, mediaItemValues, SQLiteDatabase.CONFLICT_IGNORE);
        }
        mentionsDb.addMentions(post);
        Log.i("ContentDb.addPost: added " + post);
    }

    @WorkerThread
    void retractPost(@NonNull Post post) {
        Log.i("ContentDb.retractPost: postId=" + post.id);
        final ContentValues values = new ContentValues();
        values.put(PostsTable.COLUMN_TRANSFERRED, !post.senderUserId.isMe());
        values.put(PostsTable.COLUMN_TEXT, (String)null);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            if (post.rowId == 0) {
                final Post updatePost = getPost(post.senderUserId, post.id);
                if (updatePost != null) {
                    post = updatePost;
                }
            }
            final int updatedCount = db.updateWithOnConflict(PostsTable.TABLE_NAME, values,
                    PostsTable.COLUMN_SENDER_USER_ID + "=? AND " + PostsTable.COLUMN_POST_ID + "=?",
                    new String[]{post.senderUserId.rawId(), post.id},
                    SQLiteDatabase.CONFLICT_ABORT);
            if (updatedCount == 0) {
                Log.i("ContentDb.retractPost: nothing to retract, will insert new post");
                values.put(PostsTable.COLUMN_SENDER_USER_ID, post.senderUserId.rawId());
                values.put(PostsTable.COLUMN_POST_ID, post.id);
                values.put(PostsTable.COLUMN_TIMESTAMP, post.timestamp);
                values.put(PostsTable.COLUMN_SEEN, false);
                post.rowId = db.insertWithOnConflict(PostsTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_ABORT);
            } else {
                db.delete(CommentsTable.TABLE_NAME,
                        CommentsTable.COLUMN_POST_SENDER_USER_ID + "=? AND " + CommentsTable.COLUMN_POST_ID + "=?",
                        new String[]{post.senderUserId.rawId(), post.id});
                db.delete(MediaTable.TABLE_NAME,
                        MediaTable.COLUMN_PARENT_ROW_ID + "=? AND " + MediaTable.COLUMN_PARENT_TABLE + "='" + PostsTable.TABLE_NAME + "'",
                        new String[]{Long.toString(post.rowId)});
                db.delete(MentionsTable.TABLE_NAME,
                        MentionsTable.COLUMN_PARENT_ROW_ID + "=? AND " + MentionsTable.COLUMN_PARENT_TABLE + "='" + PostsTable.TABLE_NAME + "'",
                        new String[]{Long.toString(post.rowId)});
                for (Media media : post.media) {
                    if (media.file != null) {
                        if (!media.file.delete()) {
                            Log.e("ContentDb.retractPost: failed to delete " + media.file.getAbsolutePath());
                        }
                    }
                }
            }
            db.setTransactionSuccessful();
            Log.i("ContentDb.retractPost: retracted postId=" + post.id);
        } catch (SQLException ex) {
            Log.e("ContentDb.retractPost: failed");
            throw ex;
        } finally {
            db.endTransaction();
        }
    }

    @WorkerThread
    void setIncomingPostSeen(@NonNull UserId senderUserId, @NonNull String postId) {
        Log.i("ContentDb.setIncomingPostSeen: senderUserId=" + senderUserId + " postId=" + postId);
        final ContentValues values = new ContentValues();
        values.put(PostsTable.COLUMN_SEEN, Post.SEEN_YES_PENDING);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        try {
            db.updateWithOnConflict(PostsTable.TABLE_NAME, values,
                    PostsTable.COLUMN_SENDER_USER_ID + "=? AND " + PostsTable.COLUMN_POST_ID + "=?",
                    new String [] {senderUserId.rawId(), postId},
                    SQLiteDatabase.CONFLICT_ABORT);
        } catch (SQLException ex) {
            Log.e("ContentDb.setIncomingPostSeen: failed");
            throw ex;
        }
    }

    // for debug only
    @WorkerThread
    void setIncomingPostsSeen(@Post.SeenState int seen) {
        Log.i("ContentDb.setIncomingPostsSeen: seen=" + seen);
        final ContentValues values = new ContentValues();
        values.put(PostsTable.COLUMN_SEEN, seen);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        try {
            db.updateWithOnConflict(PostsTable.TABLE_NAME, values,
                    PostsTable.COLUMN_SENDER_USER_ID + "!=''",
                    null,
                    SQLiteDatabase.CONFLICT_ABORT);
        } catch (SQLException ex) {
            Log.e("ContentDb.setIncomingPostsSeen: failed");
            throw ex;
        }
    }

    @WorkerThread
    void setPostSeenReceiptSent(@NonNull UserId senderUserId, @NonNull String postId) {
        Log.i("ContentDb.setPostSeenReceiptSent: senderUserId=" + senderUserId + " postId=" + postId);
        final ContentValues values = new ContentValues();
        values.put(PostsTable.COLUMN_SEEN, Post.SEEN_YES);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        try {
            db.updateWithOnConflict(PostsTable.TABLE_NAME, values,
                    PostsTable.COLUMN_SENDER_USER_ID + "=? AND " + PostsTable.COLUMN_POST_ID + "=?",
                    new String [] {senderUserId.rawId(), postId},
                    SQLiteDatabase.CONFLICT_ABORT);
        } catch (SQLException ex) {
            Log.e("ContentDb.setPostSeenReceiptSent: failed");
            throw ex;
        }
    }

    @WorkerThread
    void setOutgoingPostSeen(@NonNull UserId seenByUserId, @NonNull String postId, long timestamp) {
        Log.i("ContentDb.setOutgoingPostSeen: seenByUserId=" + seenByUserId + " postId=" + postId + " timestamp=" + timestamp);
        final ContentValues values = new ContentValues();
        values.put(SeenTable.COLUMN_SEEN_BY_USER_ID, seenByUserId.rawId());
        values.put(SeenTable.COLUMN_POST_ID, postId);
        values.put(SeenTable.COLUMN_TIMESTAMP, timestamp);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        try {
            db.insertWithOnConflict(SeenTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_ABORT);
        } catch (SQLiteConstraintException ex) {
            Log.i("ContentDb.setOutgoingPostSeen: seen duplicate", ex);
        } catch (SQLException ex) {
            Log.e("ContentDb.setOutgoingPostSeen: failed");
            throw ex;
        }
    }

    @WorkerThread
    void setPostTransferred(@NonNull UserId senderUserId, @NonNull String postId) {
        Log.i("ContentDb.setPostTransferred: senderUserId=" + senderUserId + " postId=" + postId);
        final ContentValues values = new ContentValues();
        values.put(PostsTable.COLUMN_TRANSFERRED, true);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        try {
            db.updateWithOnConflict(PostsTable.TABLE_NAME, values,
                    PostsTable.COLUMN_SENDER_USER_ID + "=? AND " + PostsTable.COLUMN_POST_ID + "=?",
                    new String [] {senderUserId.rawId(), postId},
                    SQLiteDatabase.CONFLICT_ABORT);
        } catch (SQLException ex) {
            Log.e("ContentDb.setPostTransferred: failed");
            throw ex;
        }
    }

    @WorkerThread
    void setMediaTransferred(@NonNull Post post, @NonNull Media media) {
        Log.i("ContentDb.setMediaTransferred: post=" + post + " media=" + media);
        final ContentValues values = new ContentValues();
        values.put(MediaTable.COLUMN_FILE, media.file == null ? null : media.file.getName());
        values.put(MediaTable.COLUMN_URL, media.url);
        if (media.encKey != null) {
            values.put(MediaTable.COLUMN_ENC_KEY, media.encKey);
        }
        if (media.sha256hash != null) {
            values.put(MediaTable.COLUMN_SHA256_HASH, media.sha256hash);
        }
        if (media.file != null && (media.width == 0 || media.height == 0)) {
            final Size dimensions = MediaUtils.getDimensions(media.file, media.type);
            if (dimensions != null && dimensions.getWidth() > 0 && dimensions.getHeight() > 0) {
                values.put(MediaTable.COLUMN_WIDTH, dimensions.getWidth());
                values.put(MediaTable.COLUMN_HEIGHT, dimensions.getHeight());
            }
        }
        values.put(MediaTable.COLUMN_TRANSFERRED, media.transferred);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        try {
            db.updateWithOnConflict(MediaTable.TABLE_NAME, values,
                    MediaTable._ID + "=?",
                    new String [] {Long.toString(media.rowId)},
                    SQLiteDatabase.CONFLICT_ABORT);
        } catch (SQLException ex) {
            Log.e("ContentDb.setMediaTransferred: failed", ex);
            throw ex;
        }
    }

    @WorkerThread
    void addComment(@NonNull Comment comment) {
        Log.i("ContentDb.addComment " + comment);
        if (comment.timestamp < getPostExpirationTime()) {
            throw new SQLiteConstraintException("attempting to add expired comment");
        }
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
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        comment.rowId = db.insertWithOnConflict(CommentsTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_ABORT);
        mentionsDb.addMentions(comment);
        Log.i("ContentDb.addComment: added " + comment);
    }

    @WorkerThread
    void retractComment(@NonNull Comment comment) {
        Log.i("ContentDb.retractComment: senderUserId=" + comment.commentSenderUserId + " commentId=" + comment.commentId);
        final ContentValues values = new ContentValues();
        values.put(CommentsTable.COLUMN_TRANSFERRED, !comment.commentSenderUserId.isMe());
        values.put(CommentsTable.COLUMN_TEXT, (String)null);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final int updatedCount = db.updateWithOnConflict(CommentsTable.TABLE_NAME, values,
                    CommentsTable.COLUMN_COMMENT_SENDER_USER_ID + "=? AND " + CommentsTable.COLUMN_COMMENT_ID + "=?",
                    new String [] {comment.commentSenderUserId.rawId(), comment.commentId},
                    SQLiteDatabase.CONFLICT_ABORT);
            if (updatedCount == 0) {
                values.put(CommentsTable.COLUMN_POST_SENDER_USER_ID, comment.postSenderUserId.rawId());
                values.put(CommentsTable.COLUMN_POST_ID, comment.postId);
                values.put(CommentsTable.COLUMN_COMMENT_SENDER_USER_ID, comment.commentSenderUserId.rawId());
                values.put(CommentsTable.COLUMN_COMMENT_ID, comment.commentId);
                values.put(CommentsTable.COLUMN_PARENT_ID, comment.parentCommentId);
                values.put(CommentsTable.COLUMN_TIMESTAMP, comment.timestamp);
                values.put(CommentsTable.COLUMN_SEEN, comment.seen);
                comment.rowId = db.insertWithOnConflict(CommentsTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_ABORT);
            }
            db.setTransactionSuccessful();
        } catch (SQLException ex) {
            Log.e("ContentDb.retractComment: failed");
            throw ex;
        } finally {
            db.endTransaction();
        }
    }

    @WorkerThread
    void setCommentTransferred(@NonNull UserId postSenderUserId, @NonNull String postId, @NonNull UserId commentSenderUserId, @NonNull String commentId) {
        Log.i("ContentDb.setCommentTransferred: senderUserId=" + commentSenderUserId + " commentId=" + commentId);
        final ContentValues values = new ContentValues();
        values.put(CommentsTable.COLUMN_TRANSFERRED, true);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        try {
            db.updateWithOnConflict(CommentsTable.TABLE_NAME, values,
                    CommentsTable.COLUMN_POST_SENDER_USER_ID + "=? AND " +
                            CommentsTable.COLUMN_POST_ID + "=? AND " +
                            CommentsTable.COLUMN_COMMENT_SENDER_USER_ID + "=? AND " +
                            CommentsTable.COLUMN_COMMENT_ID + "=?",
                    new String [] {postSenderUserId.rawId(), postId, commentSenderUserId.rawId(), commentId},
                    SQLiteDatabase.CONFLICT_ABORT);
        } catch (SQLException ex) {
            Log.e("ContentDb.setCommentTransferred: failed");
            throw ex;
        }
    }

    // for debug only
    @WorkerThread
    boolean setCommentsSeen(boolean seen) {
        Log.i("ContentDb.setCommentsSeen: seen=" + seen);
        final ContentValues values = new ContentValues();
        values.put(CommentsTable.COLUMN_SEEN, seen);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        try {
            final int updatedCount = db.updateWithOnConflict(CommentsTable.TABLE_NAME, values,
                    CommentsTable.COLUMN_SEEN + "=?",
                    new String [] {seen ? "0" : "1"},
                    SQLiteDatabase.CONFLICT_ABORT);
            return updatedCount > 0;
        } catch (SQLException ex) {
            Log.e("ContentDb.setCommentsSeen: failed");
            throw ex;
        }
    }

    @WorkerThread
    boolean setCommentsSeen(@NonNull UserId postSenderUserId, @NonNull String postId, boolean seen) {
        Log.i("ContentDb.setCommentsSeen: postSenderUserId=" + postSenderUserId+ " postId=" + postId);
        final ContentValues values = new ContentValues();
        values.put(CommentsTable.COLUMN_SEEN, seen);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        try {
            final int updatedCount = db.updateWithOnConflict(CommentsTable.TABLE_NAME, values,
                    CommentsTable.COLUMN_POST_SENDER_USER_ID + "=? AND " +
                            CommentsTable.COLUMN_POST_ID + "=? AND " +
                            CommentsTable.COLUMN_SEEN + "=" + (seen ? 0 : 1),
                    new String [] {postSenderUserId.rawId(), postId},
                    SQLiteDatabase.CONFLICT_ABORT);
            return updatedCount > 0;
        } catch (SQLException ex) {
            Log.e("ContentDb.setCommentsSeen: failed");
            throw ex;
        }
    }

    @WorkerThread
    @NonNull List<Post> getPosts(@Nullable Long timestamp, int count, boolean after, boolean outgoingOnly, boolean unseenOnly) {
        final List<Post> posts = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String where;
        if (timestamp == null) {
            where = PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TIMESTAMP + ">" + getPostExpirationTime();
        } else {
            if (after) {
                where = PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TIMESTAMP + ">" + getPostExpirationTime() + " AND " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TIMESTAMP + "<" + timestamp;
            } else {
                where = PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TIMESTAMP + ">" + Math.max(getPostExpirationTime(), timestamp);
            }
        }

        if (outgoingOnly) {
            where += " AND " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_SENDER_USER_ID + "=''";
        }
        if (unseenOnly) {
            where += " AND " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_SEEN + "=0";
        }
        String sql =
            "SELECT " +
                PostsTable.TABLE_NAME + "." + PostsTable._ID + "," +
                PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_SENDER_USER_ID + "," +
                PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_POST_ID + "," +
                PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TIMESTAMP + "," +
                PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TRANSFERRED + "," +
                PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_SEEN + "," +
                PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TEXT + "," +
                "m." + MediaTable._ID + "," +
                "m." + MediaTable.COLUMN_TYPE + "," +
                "m." + MediaTable.COLUMN_URL + "," +
                "m." + MediaTable.COLUMN_FILE + "," +
                "m." + MediaTable.COLUMN_WIDTH + "," +
                "m." + MediaTable.COLUMN_HEIGHT + "," +
                "m." + MediaTable.COLUMN_TRANSFERRED + ", " +
                "c.comment_count" + ", " +
                "c.seen_comment_count" + ", " +
                "fc.first_comment_row_id" + ", " +
                "fc.first_comment_id" + ", " +
                "fc.first_comment_user_id" + ", " +
                "fc.first_comment_text" + ", " +
                "fc.first_comment_timestamp" + ", " +
                "s.seen_by_count" + " " +

            "FROM " + PostsTable.TABLE_NAME + " " +
            "LEFT JOIN (" +
                "SELECT " +
                    MediaTable._ID + "," +
                    MediaTable.COLUMN_PARENT_TABLE + "," +
                    MediaTable.COLUMN_PARENT_ROW_ID + "," +
                    MediaTable.COLUMN_TYPE + "," +
                    MediaTable.COLUMN_URL + "," +
                    MediaTable.COLUMN_FILE + "," +
                    MediaTable.COLUMN_WIDTH + "," +
                    MediaTable.COLUMN_HEIGHT + "," +
                    MediaTable.COLUMN_TRANSFERRED + " FROM " + MediaTable.TABLE_NAME + " ORDER BY " + MediaTable._ID + " ASC) " +
                "AS m ON " + PostsTable.TABLE_NAME + "." + PostsTable._ID + "=m." + MediaTable.COLUMN_PARENT_ROW_ID + " AND '" + PostsTable.TABLE_NAME + "'=m." + MediaTable.COLUMN_PARENT_TABLE + " " +
            "LEFT JOIN (" +
                "SELECT " +
                    CommentsTable.COLUMN_POST_SENDER_USER_ID + "," +
                    CommentsTable.COLUMN_POST_ID + "," +
                    "min(timestamp) as min_timestamp" + "," +
                    "count(*) AS comment_count" + ", " +
                    "sum(" + CommentsTable.COLUMN_SEEN + ") AS seen_comment_count" + " " +
                    "FROM " + CommentsTable.TABLE_NAME + " WHERE " + CommentsTable.COLUMN_TEXT + " IS NOT NULL" + " GROUP BY " + CommentsTable.COLUMN_POST_SENDER_USER_ID+ ", " + CommentsTable.COLUMN_POST_ID + ") " +
                "AS c ON " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_SENDER_USER_ID + "=c." + CommentsTable.COLUMN_POST_SENDER_USER_ID + " AND " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_POST_ID + "=c." + CommentsTable.COLUMN_POST_ID + " " +
            "LEFT JOIN (" +
                "SELECT " +
                    CommentsTable._ID + " AS first_comment_row_id," +
                    CommentsTable.COLUMN_POST_SENDER_USER_ID + "," +
                    CommentsTable.COLUMN_POST_ID + "," +
                    CommentsTable.COLUMN_TIMESTAMP + " AS first_comment_timestamp," +
                    CommentsTable.COLUMN_COMMENT_ID + " AS first_comment_id, " +
                    CommentsTable.COLUMN_COMMENT_SENDER_USER_ID + " AS first_comment_user_id, " +
                    CommentsTable.COLUMN_TEXT + " AS first_comment_text " +
                    "FROM " + CommentsTable.TABLE_NAME + " ) " +
                "AS fc ON " + "fc.first_comment_timestamp=c.min_timestamp AND " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_SENDER_USER_ID + "=fc." + CommentsTable.COLUMN_POST_SENDER_USER_ID + " AND " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_POST_ID + "=fc." + CommentsTable.COLUMN_POST_ID + " " +
            "LEFT JOIN (" +
                "SELECT " +
                    SeenTable.COLUMN_POST_ID + "," +
                    "count(*) AS seen_by_count " +
                    "FROM " + SeenTable.TABLE_NAME + " GROUP BY " + SeenTable.COLUMN_POST_ID + ") " +
                "AS s ON " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_SENDER_USER_ID + "=''" + " AND " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_POST_ID + "=s." + SeenTable.COLUMN_POST_ID + " " +
            "WHERE " + where + " " +
            "ORDER BY " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TIMESTAMP + (after ? " DESC " : " ASC ") +
            "LIMIT " + count;

        try (final Cursor cursor = db.rawQuery(sql, null)) {

            long lastRowId = -1;
            Post post = null;
            while (cursor.moveToNext()) {
                long rowId = cursor.getLong(0);
                if (lastRowId != rowId) {
                    lastRowId = rowId;
                    if (post != null) {
                        mentionsDb.fillMentions(post);
                        posts.add(post);
                    }
                    post = new Post(
                            rowId,
                            new UserId(cursor.getString(1)),
                            cursor.getString(2),
                            cursor.getLong(3),
                            cursor.getInt(4),
                            cursor.getInt(5),
                            cursor.getString(6));
                    post.commentCount = cursor.getInt(14);
                    post.unseenCommentCount = post.commentCount - cursor.getInt(15);
                    final String firstCommentId = cursor.getString(17);
                    if (firstCommentId != null) {
                        post.firstComment = new Comment(cursor.getLong(16),
                                post.senderUserId,
                                post.id,
                                new UserId(cursor.getString(18)),
                                firstCommentId,
                                null,
                                cursor.getLong(20),
                                true,
                                true,
                                cursor.getString(19));
                        mentionsDb.fillMentions(post.firstComment);
                    }
                    post.seenByCount = cursor.getInt(21);
                }
                if (!cursor.isNull(7)) {
                    Preconditions.checkNotNull(post).media.add(new Media(
                            cursor.getLong(7),
                            cursor.getInt(8),
                            cursor.getString(9),
                            fileStore.getMediaFile(cursor.getString(10)),
                            null,
                            null,
                            cursor.getInt(11),
                            cursor.getInt(12),
                            cursor.getInt(13)));
                }
            }
            if (post != null && cursor.getCount() < count) {
                mentionsDb.fillMentions(post);
                posts.add(post);
            }
        }
        if (!after) {
            Collections.reverse(posts);
        }
        Log.i("ContentDb.getPosts: start=" + timestamp + " count=" + count + " after=" + after + " posts.size=" + posts.size() + (posts.isEmpty() ? "" : (" got posts from " + posts.get(0).timestamp + " to " + posts.get(posts.size()-1).timestamp)));

        return posts;
    }

    @WorkerThread
    @Nullable Post getPost(@NonNull UserId userId, @NonNull String postId) {
        Post post = null;
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        final String sql =
                "SELECT " +
                    PostsTable.TABLE_NAME + "." + PostsTable._ID + "," +
                    PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_SENDER_USER_ID + "," +
                    PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_POST_ID + "," +
                    PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TIMESTAMP + "," +
                    PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TRANSFERRED + "," +
                    PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_SEEN + "," +
                    PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TEXT + "," +
                    "m." + MediaTable._ID + "," +
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
                        MediaTable.COLUMN_PARENT_TABLE + "," +
                        MediaTable.COLUMN_PARENT_ROW_ID + "," +
                        MediaTable.COLUMN_TYPE + "," +
                        MediaTable.COLUMN_URL + "," +
                        MediaTable.COLUMN_FILE + "," +
                        MediaTable.COLUMN_WIDTH + "," +
                        MediaTable.COLUMN_HEIGHT + "," +
                        MediaTable.COLUMN_TRANSFERRED + " FROM " + MediaTable.TABLE_NAME + " ORDER BY " + MediaTable._ID + " ASC) " +
                    "AS m ON " + PostsTable.TABLE_NAME + "." + PostsTable._ID + "=m." + MediaTable.COLUMN_PARENT_ROW_ID + " AND '" + PostsTable.TABLE_NAME + "'=m." + MediaTable.COLUMN_PARENT_TABLE + " " +
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
                            cursor.getInt(4),
                            cursor.getInt(5),
                            cursor.getString(6));
                    mentionsDb.fillMentions(post);
                }
                if (!cursor.isNull(7)) {
                    Preconditions.checkNotNull(post).media.add(new Media(
                            cursor.getLong(7),
                            cursor.getInt(8),
                            cursor.getString(9),
                            fileStore.getMediaFile(cursor.getString(10)),
                            null,
                            null,
                            cursor.getInt(11),
                            cursor.getInt(12),
                            cursor.getInt(13)));
                }
            }
        }
        Log.i("ContentDb.getPost: post=" + post);
        return post;
    }

    @WorkerThread
    long getLastSeenCommentRowId(@NonNull UserId postSenderUserId, @NonNull String postId) {
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        final String sql =
                "SELECT MAX(" + CommentsTable._ID + ") FROM " + CommentsTable.TABLE_NAME + " " +
                "WHERE " + CommentsTable.COLUMN_POST_SENDER_USER_ID + "=? AND " + CommentsTable.COLUMN_POST_ID + "=? AND " + CommentsTable.COLUMN_SEEN + "=1";
        try (final Cursor cursor = db.rawQuery(sql, new String [] {postSenderUserId.rawId(), postId})) {
            if (cursor.moveToNext()) {
                return cursor.isNull(0) ? -1 : cursor.getLong(0);
            } else {
                return -1;
            }
        }
    }

    @WorkerThread
    @NonNull List<Comment> getComments(@NonNull UserId postSenderUserId, @NonNull String postId, int start, int count) {
        final String sql =
                "WITH RECURSIVE " +
                    "comments_tree(level, _id, timestamp, parent_id, comment_sender_user_id, comment_id, transferred, seen, text) AS ( " +
                        "SELECT 0, _id, timestamp, parent_id, comment_sender_user_id, comment_id, transferred, seen, text FROM comments WHERE post_sender_user_id=? AND post_id=? AND parent_id IS NULL AND timestamp > " + getPostExpirationTime() + " " +
                        "UNION ALL " +
                        "SELECT comments_tree.level+1, comments._id, comments.timestamp, comments.parent_id, comments.comment_sender_user_id, comments.comment_id, comments.transferred, comments.seen, comments.text " +
                            "FROM comments, comments_tree WHERE comments.parent_id=comments_tree.comment_id ORDER BY 1 DESC, 2) " +
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
                        cursor.getInt(7) == 1,
                        cursor.getString(8));
                mentionsDb.fillMentions(comment);
                comments.add(comment);
            }
        }
        Log.i("ContentDb.getComments: start=" + start + " count=" + count + " comments.size=" + comments.size());
        return comments;
    }

    @WorkerThread
    @NonNull List<Post> getMentionedPosts(@NonNull UserId mentionedUserId, int limit) {
        List<Post> mentionedPosts = new ArrayList<>();
        final String sql =
                "SELECT " +
                        MentionsTable.TABLE_NAME + "." + MentionsTable.COLUMN_PARENT_TABLE + "," +
                        MentionsTable.TABLE_NAME + "." + MentionsTable.COLUMN_PARENT_ROW_ID + "," +
                        MentionsTable.TABLE_NAME + "." + MentionsTable.COLUMN_MENTION_USER_ID + "," +
                        "p." + PostsTable._ID + "," +
                        "p." + PostsTable.COLUMN_SENDER_USER_ID + "," +
                        "p." + PostsTable.COLUMN_POST_ID + "," +
                        "p." + PostsTable.COLUMN_TIMESTAMP + "," +
                        "p." + PostsTable.COLUMN_TRANSFERRED + "," +
                        "p." + PostsTable.COLUMN_SEEN + "," +
                        "p." + PostsTable.COLUMN_TEXT + "," +
                        "m." + MediaTable._ID + "," +
                        "m." + MediaTable.COLUMN_TYPE + "," +
                        "m." + MediaTable.COLUMN_URL + "," +
                        "m." + MediaTable.COLUMN_FILE + "," +
                        "m." + MediaTable.COLUMN_WIDTH + "," +
                        "m." + MediaTable.COLUMN_HEIGHT + "," +
                        "m." + MediaTable.COLUMN_TRANSFERRED + " " +
                        "FROM " + MentionsTable.TABLE_NAME + " " +
                        "LEFT JOIN " + PostsTable.TABLE_NAME + " " +
                        "AS p ON " + MentionsTable.TABLE_NAME + "." + MentionsTable.COLUMN_PARENT_ROW_ID + "=p." + PostsTable._ID + " " +
                        "LEFT JOIN (" +
                        "SELECT " +
                        MediaTable._ID + "," +
                        MediaTable.COLUMN_PARENT_TABLE + "," +
                        MediaTable.COLUMN_PARENT_ROW_ID + "," +
                        MediaTable.COLUMN_TYPE + "," +
                        MediaTable.COLUMN_URL + "," +
                        MediaTable.COLUMN_FILE + "," +
                        MediaTable.COLUMN_WIDTH + "," +
                        MediaTable.COLUMN_HEIGHT + "," +
                        MediaTable.COLUMN_TRANSFERRED + " FROM " + MediaTable.TABLE_NAME + " ORDER BY " + MediaTable._ID + " ASC) " +
                        "AS m ON p." + PostsTable._ID + "=m." + MediaTable.COLUMN_PARENT_ROW_ID + " AND '" + PostsTable.TABLE_NAME + "'=m." + MediaTable.COLUMN_PARENT_TABLE + " " +
                        "WHERE " + MentionsTable.TABLE_NAME + "." + MentionsTable.COLUMN_MENTION_USER_ID + "=? AND " + MentionsTable.TABLE_NAME + "." + MentionsTable.COLUMN_PARENT_TABLE + "=? LIMIT " + limit;
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.rawQuery(sql, new String[]{mentionedUserId.rawId(), PostsTable.TABLE_NAME})) {
            Post post = null;
            while (cursor.moveToNext()) {
                long rowId = cursor.getLong(3);
                if (post == null || rowId != post.rowId) {
                    post = new Post(
                            rowId,
                            new UserId(cursor.getString(4)),
                            cursor.getString(5),
                            cursor.getLong(6),
                            cursor.getInt(7),
                            cursor.getInt(8),
                            cursor.getString(9));
                    mentionsDb.fillMentions(post);
                    mentionedPosts.add(post);
                }
                if (!cursor.isNull(7)) {
                    Preconditions.checkNotNull(post).media.add(new Media(
                            cursor.getLong(10),
                            cursor.getInt(11),
                            cursor.getString(12),
                            fileStore.getMediaFile(cursor.getString(13)),
                            null,
                            null,
                            cursor.getInt(14),
                            cursor.getInt(15),
                            cursor.getInt(16)));
                }
            }
        }
        return mentionedPosts;
    }

    @WorkerThread
    @NonNull List<Comment> getMentionedComments(@NonNull UserId mentionedUserId, int limit) {
        List<Comment> mentionedComments = new ArrayList<>();
        final String sql =
                "SELECT " +
                        "c." + CommentsTable._ID + ", " +
                        "c." + CommentsTable.COLUMN_POST_SENDER_USER_ID + ", " +
                        "c." + CommentsTable.COLUMN_POST_ID + ", " +
                        "c." + CommentsTable.COLUMN_COMMENT_SENDER_USER_ID + ", " +
                        "c." + CommentsTable.COLUMN_COMMENT_ID + ", " +
                        "c." + CommentsTable.COLUMN_PARENT_ID + ", " +
                        "c." + CommentsTable.COLUMN_TIMESTAMP + ", " +
                        "c." + CommentsTable.COLUMN_TRANSFERRED + ", " +
                        "c." + CommentsTable.COLUMN_TEXT + ", " +
                        "c." + CommentsTable.COLUMN_SEEN + ", " +
                        MentionsTable.TABLE_NAME + "." + MentionsTable.COLUMN_PARENT_TABLE + "," +
                        MentionsTable.TABLE_NAME + "." + MentionsTable.COLUMN_PARENT_ROW_ID + "," +
                        MentionsTable.TABLE_NAME + "." + MentionsTable.COLUMN_MENTION_USER_ID + " " +
                        "FROM " + MentionsTable.TABLE_NAME + " " +
                        "LEFT JOIN " + CommentsTable.TABLE_NAME + " " +
                        "AS c ON " + MentionsTable.TABLE_NAME + "." + MentionsTable.COLUMN_PARENT_ROW_ID + "=c." + CommentsTable._ID + " " +
                        "WHERE " + MentionsTable.TABLE_NAME + "." + MentionsTable.COLUMN_MENTION_USER_ID + "=? AND " + MentionsTable.TABLE_NAME + "." + MentionsTable.COLUMN_PARENT_TABLE + "=? LIMIT " + limit;
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.rawQuery(sql, new String[]{mentionedUserId.rawId(), CommentsTable.TABLE_NAME})) {
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
                        cursor.getInt(9) == 1,
                        cursor.getString(8));
                mentionsDb.fillMentions(comment);
                mentionedComments.add(comment);
            }
        }
        return mentionedComments;
    }

    /*
    * returns "important" comments only
    * */
    @WorkerThread
    @NonNull List<Comment> getIncomingCommentsHistory(int limit) {
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
                    "AND comments.timestamp>" + getPostExpirationTime() + " " +
                    "AND comments.text IS NOT NULL " +
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
                        cursor.getInt(9) == 1,
                        cursor.getString(8));
                mentionsDb.fillMentions(comment);
                comments.add(comment);
            }
        }
        Log.i("ContentDb.getIncomingCommentsHistory: comments.size=" + comments.size());
        return comments;
    }

    @WorkerThread
    @NonNull List<Comment> getUnseenCommentsOnMyPosts(long timestamp, int count) {
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
                        CommentsTable.COLUMN_SEEN,
                        CommentsTable.COLUMN_TEXT},
                CommentsTable.COLUMN_POST_SENDER_USER_ID + "='' AND " + CommentsTable.COLUMN_SEEN + "=0 AND " + CommentsTable.COLUMN_TIMESTAMP + ">" + Math.max(timestamp, getPostExpirationTime()),
                null, null, null, CommentsTable.COLUMN_TIMESTAMP + " ASC LIMIT " + count)) {
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
                        cursor.getInt(8) == 1,
                        cursor.getString(9));
                comments.add(comment);
            }
        }
        Log.i("ContentDb.getUnseenCommentsOnMyPosts: comments.size=" + comments.size());
        return comments;
    }

    @WorkerThread
    @NonNull List<UserId> getPostSeenByUsers(@NonNull String postId) {
        final List<UserId> users = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.query(SeenTable.TABLE_NAME,
                new String [] {SeenTable.COLUMN_SEEN_BY_USER_ID},
                SeenTable.COLUMN_POST_ID + "=?",
                new String [] {postId}, null, null, SeenTable._ID + " DESC")) {
            while (cursor.moveToNext()) {
                users.add(new UserId(cursor.getString(0)));
            }
        }
        Log.i("ContentDb.getSeenBy: users.size=" + users.size());
        return users;
    }

    @WorkerThread
    @NonNull List<SeenByInfo> getPostSeenByInfos(@NonNull String postId) {
        final List<SeenByInfo> seenByInfos = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.query(SeenTable.TABLE_NAME,
                new String [] {SeenTable.COLUMN_SEEN_BY_USER_ID, SeenTable.COLUMN_TIMESTAMP},
                SeenTable.COLUMN_POST_ID + "=?",
                new String [] {postId}, null, null, SeenTable._ID + " DESC")) {
            while (cursor.moveToNext()) {
                seenByInfos.add(new SeenByInfo(new UserId(cursor.getString(0)), cursor.getLong(1)));
            }
        }
        Log.i("ContentDb.getSeenBy: seenByInfos.size=" + seenByInfos.size());
        return seenByInfos;
    }

    @WorkerThread
    @NonNull List<Post> getPendingPosts() {
        final List<Post> posts = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        final String sql =
                "SELECT " +
                    PostsTable.TABLE_NAME + "." + PostsTable._ID + "," +
                    PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_SENDER_USER_ID + "," +
                    PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_POST_ID + "," +
                    PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TIMESTAMP + "," +
                    PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TRANSFERRED + "," +
                    PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_SEEN + "," +
                    PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TEXT + "," +
                    "m." + MediaTable._ID + "," +
                    "m." + MediaTable.COLUMN_TYPE + "," +
                    "m." + MediaTable.COLUMN_URL + "," +
                    "m." + MediaTable.COLUMN_FILE + "," +
                    "m." + MediaTable.COLUMN_ENC_KEY + "," +
                    "m." + MediaTable.COLUMN_SHA256_HASH + "," +
                    "m." + MediaTable.COLUMN_WIDTH + "," +
                    "m." + MediaTable.COLUMN_HEIGHT + "," +
                    "m." + MediaTable.COLUMN_TRANSFERRED + " " +
                "FROM " + PostsTable.TABLE_NAME + " " +
                "LEFT JOIN (" +
                    "SELECT " +
                        MediaTable._ID + "," +
                        MediaTable.COLUMN_PARENT_TABLE + "," +
                        MediaTable.COLUMN_PARENT_ROW_ID + "," +
                        MediaTable.COLUMN_TYPE + "," +
                        MediaTable.COLUMN_URL + "," +
                        MediaTable.COLUMN_FILE + "," +
                        MediaTable.COLUMN_ENC_KEY + "," +
                        MediaTable.COLUMN_SHA256_HASH + "," +
                        MediaTable.COLUMN_WIDTH + "," +
                        MediaTable.COLUMN_HEIGHT + "," +
                        MediaTable.COLUMN_TRANSFERRED + " FROM " + MediaTable.TABLE_NAME + " ORDER BY " + MediaTable._ID + " ASC) " +
                    "AS m ON " + PostsTable.TABLE_NAME + "." + PostsTable._ID + "=m." + MediaTable.COLUMN_PARENT_ROW_ID + " AND '" + PostsTable.TABLE_NAME + "'=m." + MediaTable.COLUMN_PARENT_TABLE + " " +
                "WHERE " +
                    PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TRANSFERRED + "=0 AND " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TIMESTAMP + ">" + getPostExpirationTime() + " " +
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
                            cursor.getInt(4),
                            cursor.getInt(5),
                            cursor.getString(6));
                    mentionsDb.fillMentions(post);
                }
                if (!cursor.isNull(7)) {
                    Preconditions.checkNotNull(post).media.add(new Media(
                            cursor.getLong(7),
                            cursor.getInt(8),
                            cursor.getString(9),
                            fileStore.getMediaFile(cursor.getString(10)),
                            cursor.getBlob(11),
                            cursor.getBlob(12),
                            cursor.getInt(13),
                            cursor.getInt(14),
                            cursor.getInt(15)));
                }
            }
            if (post != null) {
                posts.add(post);
            }
        }
        Log.i("ContentDb.getPendingPosts: posts.size=" + posts.size());
        return posts;
    }

    @WorkerThread
    @NonNull List<Comment> getPendingComments() {
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
                        CommentsTable.COLUMN_SEEN,
                        CommentsTable.COLUMN_TEXT},
                CommentsTable.COLUMN_COMMENT_SENDER_USER_ID + "='' AND " + CommentsTable.COLUMN_TRANSFERRED + "=0 AND " + CommentsTable.COLUMN_TIMESTAMP + ">" + getPostExpirationTime(),
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
                        cursor.getInt(8) == 1,
                        cursor.getString(9));
                comments.add(comment);
            }
        }
        Log.i("ContentDb.getPendingComments: comments.size=" + comments.size());
        return comments;
    }

    @WorkerThread
    @NonNull List<SeenReceipt> getPendingPostSeenReceipts() {
        final List<SeenReceipt> receipts = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.query(PostsTable.TABLE_NAME,
                new String [] {
                        PostsTable.COLUMN_SENDER_USER_ID,
                        PostsTable.COLUMN_POST_ID},
                PostsTable.COLUMN_SEEN + "=" + Post.SEEN_YES_PENDING + " AND " + PostsTable.COLUMN_SENDER_USER_ID + "<>''",
                null, null, null, null)) {
            while (cursor.moveToNext()) {
                final SeenReceipt receipt = new SeenReceipt(
                        null,
                        new UserId(cursor.getString(0)),
                        cursor.getString(1));
                receipts.add(receipt);
            }
        }
        Log.i("ContentDb.getPendingPostSeenReceipts: receipts.size=" + receipts.size());
        return receipts;
    }

    @WorkerThread
    boolean cleanup() {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();

        final String sql =
            "SELECT " +
                "m." + MediaTable.COLUMN_FILE + " " +
            "FROM " + PostsTable.TABLE_NAME + " " +
            "INNER JOIN (" +
                "SELECT " +
                    MediaTable.COLUMN_PARENT_TABLE + "," +
                    MediaTable.COLUMN_PARENT_ROW_ID + "," +
                    MediaTable.COLUMN_FILE + "," +
                    MediaTable.COLUMN_TRANSFERRED + " FROM " + MediaTable.TABLE_NAME + ")" +
                "AS m ON " + PostsTable.TABLE_NAME + "." + PostsTable._ID + "=m." + MediaTable.COLUMN_PARENT_ROW_ID + " AND '" + PostsTable.TABLE_NAME + "'=m." + MediaTable.COLUMN_PARENT_TABLE + " " +
            "WHERE " + PostsTable.COLUMN_TIMESTAMP + "<" + getPostExpirationTime();
        int deletedFiles = 0;
        try (final Cursor cursor = db.rawQuery(sql, null)) {
            while (cursor.moveToNext()) {
                final File mediaFile = fileStore.getMediaFile(cursor.getString(0));
                if (mediaFile != null) {
                    if (mediaFile.delete()) {
                        deletedFiles++;
                    } else {
                        Log.i("ContentDb.cleanup: failed to delete " + mediaFile.getAbsolutePath());
                    }
                }
            }
        }
        Log.i("ContentDb.cleanup: " + deletedFiles + " media files deleted");

        final int deletedPostsCount = db.delete(PostsTable.TABLE_NAME,
                PostsTable.COLUMN_TIMESTAMP + "<" + getPostExpirationTime(),
                null);
        Log.i("ContentDb.cleanup: " + deletedPostsCount + " posts deleted");

        // comments are deleted using trigger, but in case there are orphaned comments delete them here
        final int deletedCommentsCount = db.delete(CommentsTable.TABLE_NAME,
                CommentsTable.COLUMN_TIMESTAMP + "<" + getPostExpirationTime(),
                null);
        Log.i("ContentDb.cleanup: " + deletedCommentsCount + " comments deleted");

        // seen receipts are deleted using trigger, but in case there are orphaned receipts delete them here
        final int deletedSeenCount = db.delete(SeenTable.TABLE_NAME,
                SeenTable.COLUMN_TIMESTAMP + "<" + getPostExpirationTime(),
                null);
        Log.i("ContentDb.cleanup: " + deletedSeenCount + " seen receipts deleted");

        return (deletedPostsCount > 0 || deletedCommentsCount > 0 || deletedSeenCount > 0);
    }

    private static long getPostExpirationTime() {
        return System.currentTimeMillis() - Constants.POSTS_EXPIRATION;
    }
}

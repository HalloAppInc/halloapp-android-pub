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

import com.google.protobuf.InvalidProtocolBufferException;
import com.halloapp.Constants;
import com.halloapp.FileStore;
import com.halloapp.content.tables.ArchiveTable;
import com.halloapp.content.tables.AudienceTable;
import com.halloapp.content.tables.CommentsTable;
import com.halloapp.content.tables.MediaTable;
import com.halloapp.content.tables.MentionsTable;
import com.halloapp.content.tables.PostsTable;
import com.halloapp.content.tables.SeenTable;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.media.MediaUtils;
import com.halloapp.props.ServerProps;
import com.halloapp.proto.clients.CommentContainer;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.feed.FeedContentParser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

class PostsDb {

    private final MediaDb mediaDb;
    private final MentionsDb mentionsDb;
    private final FutureProofDb futureProofDb;
    private final ContentDbHelper databaseHelper;
    private final FileStore fileStore;
    private final ServerProps serverProps;

    PostsDb(
            MediaDb mediaDb,
            MentionsDb mentionsDb,
            FutureProofDb futureProofDb,
            ContentDbHelper databaseHelper,
            FileStore fileStore,
            ServerProps serverProps) {
        this.mediaDb = mediaDb;
        this.mentionsDb = mentionsDb;
        this.futureProofDb = futureProofDb;
        this.databaseHelper = databaseHelper;
        this.fileStore = fileStore;
        this.serverProps = serverProps;
    }

    @WorkerThread
    void addPostToArchive(@NonNull Post post) {
        Log.i("ContentDb.addPostToArchive " + post);
        if (post.isRetracted() || post.type != Post.TYPE_USER) {
            return;
        }
        final ContentValues values = new ContentValues();
        values.put(ArchiveTable.COLUMN_POST_ID, post.id);
        values.put(ArchiveTable.COLUMN_TIMESTAMP, post.timestamp);
        if (post.getParentGroup() != null) {
            values.put(ArchiveTable.COLUMN_GROUP_ID, post.getParentGroup().rawId());
        }
        if (post.text != null) {
            values.put(ArchiveTable.COLUMN_TEXT, post.text);
        }
        values.put(ArchiveTable.COLUMN_ARCHIVE_TIMESTAMP, System.currentTimeMillis());

        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        post.rowId = db.insertWithOnConflict(ArchiveTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_ABORT);

        post.isArchived = true;
        if (!post.mentions.isEmpty()) {
            mentionsDb.addMentions(post);
        }
        mediaDb.addArchiveMedia(post);

        Log.i("ContentDb.addPostToArchive: moved " + post);
    }

    @WorkerThread
    void removePostFromArchive(@NonNull Post post) {
        Log.i("ContentDb.removePostFromArchive " + post);
        for (Media media : post.media) {
            if (media.file != null) {
                if (!media.file.delete()) {
                    Log.e("ContentDb.removePostFromArchive: failed to delete " + media.file.getAbsolutePath());
                }
            }
        }
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        final int removedCount = db.delete(ArchiveTable.TABLE_NAME, ArchiveTable.COLUMN_POST_ID + "=?", new String[]{post.id});
        if (removedCount > 0) {
            Log.i("ContentDb.removePostFromArchive: removed " + post);
        } else {
            Log.w("ContentDb.removePostFromArchive: failed to remove post: " + post);
        }
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
        values.put(PostsTable.COLUMN_AUDIENCE_TYPE, post.getAudienceType());
        values.put(PostsTable.COLUMN_TYPE, post.type);
        values.put(PostsTable.COLUMN_USAGE, post.usage);
        if (post.getParentGroup() != null) {
            values.put(PostsTable.COLUMN_GROUP_ID, post.getParentGroup().rawId());
        }
        if (post.text != null) {
            values.put(PostsTable.COLUMN_TEXT, post.text);
        }
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        post.rowId = db.insertWithOnConflict(PostsTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_ABORT);
        Log.i("ContentDb.addPost got rowid " + post.rowId + " for " + post);

        mediaDb.addMedia(post);

        final List<UserId> audienceList = post.getAudienceList();
        if (audienceList != null) {
            for (UserId userId : audienceList) {
                final ContentValues audienceUser = new ContentValues();
                audienceUser.put(AudienceTable.COLUMN_POST_ID, post.id);
                audienceUser.put(AudienceTable.COLUMN_USER_ID, userId.rawId());
                db.insertWithOnConflict(AudienceTable.TABLE_NAME, null, audienceUser, SQLiteDatabase.CONFLICT_IGNORE);
            }
        }
        final List<UserId> excludeList = post.getExcludeList();
        if (excludeList != null) {
            for (UserId userId : excludeList) {
                final ContentValues excludedUserValues = new ContentValues();
                excludedUserValues.put(AudienceTable.COLUMN_POST_ID, post.id);
                excludedUserValues.put(AudienceTable.COLUMN_USER_ID, userId.rawId());
                excludedUserValues.put(AudienceTable.COLUMN_EXCLUDED, true);
                db.insertWithOnConflict(AudienceTable.TABLE_NAME, null, excludedUserValues, SQLiteDatabase.CONFLICT_IGNORE);
            }
        }
        mentionsDb.addMentions(post);
        if (post instanceof FutureProofPost) {
            futureProofDb.saveFutureProof((FutureProofPost) post);
        }
        Log.i("ContentDb.addPost: added " + post);
    }

    @WorkerThread
    void retractPost(@NonNull Post post) {
        Log.i("ContentDb.retractPost: postId=" + post.id);
        final ContentValues values = new ContentValues();
        values.put(PostsTable.COLUMN_TRANSFERRED, !post.senderUserId.isMe());
        values.put(PostsTable.COLUMN_TEXT, (String)null);
        values.put(PostsTable.COLUMN_TYPE, Post.TYPE_RETRACTED);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            if (post.rowId == 0) {
                final Post updatePost = getPost(post.id);
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
                values.put(PostsTable.COLUMN_TYPE, Post.TYPE_RETRACTED);
                post.rowId = db.insertWithOnConflict(PostsTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_ABORT);
            } else {
                db.delete(CommentsTable.TABLE_NAME, CommentsTable.COLUMN_POST_ID + "=?",
                        new String[]{post.id});
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
    void updatePostAudience(@NonNull Map<UserId, Collection<Post>> shareMap) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (UserId user : shareMap.keySet()) {
                Collection<Post> posts = shareMap.get(user);
                if (posts == null) {
                    continue;
                }
                for (Post post : posts) {
                    final ContentValues audienceUser = new ContentValues();
                    audienceUser.put(AudienceTable.COLUMN_POST_ID, post.id);
                    audienceUser.put(AudienceTable.COLUMN_USER_ID, user.rawId());
                    db.insertWithOnConflict(AudienceTable.TABLE_NAME, null, audienceUser, SQLiteDatabase.CONFLICT_IGNORE);
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e ) {
            Log.e("PostsDb/updatePostAudience failed to update audience for post", e);
        } finally {
            db.endTransaction();
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
        values.put(MediaTable.COLUMN_ENC_FILE, media.encFile == null ? null : media.encFile.getName());
        values.put(MediaTable.COLUMN_URL, media.url);
        if (media.encKey != null) {
            values.put(MediaTable.COLUMN_ENC_KEY, media.encKey);
        }
        if (media.encSha256hash != null) {
            values.put(MediaTable.COLUMN_SHA256_HASH, media.encSha256hash);
        }
        if (media.decSha256hash != null) {
            values.put(MediaTable.COLUMN_DEC_SHA256_HASH, media.decSha256hash);
        }
        if (media.file != null && (media.width == 0 || media.height == 0)) {
            final Size dimensions = MediaUtils.getDimensions(media.file, media.type);
            if (dimensions != null && dimensions.getWidth() > 0 && dimensions.getHeight() > 0) {
                values.put(MediaTable.COLUMN_WIDTH, dimensions.getWidth());
                values.put(MediaTable.COLUMN_HEIGHT, dimensions.getHeight());
            }
        }
        values.put(MediaTable.COLUMN_BLOB_VERSION, media.blobVersion);
        if (media.blobVersion == Media.BLOB_VERSION_CHUNKED) {
            values.put(MediaTable.COLUMN_CHUNK_SIZE, media.chunkSize);
            values.put(MediaTable.COLUMN_BLOB_SIZE, media.blobSize);
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
    void setMediaTransferred(@NonNull Comment comment, @NonNull Media media) {
        Log.i("ContentDb.setMediaTransferred: comment=" + comment + " media=" + media);
        final ContentValues values = new ContentValues();
        values.put(MediaTable.COLUMN_FILE, media.file == null ? null : media.file.getName());
        values.put(MediaTable.COLUMN_ENC_FILE, media.encFile == null ? null : media.encFile.getName());
        values.put(MediaTable.COLUMN_URL, media.url);
        if (media.encKey != null) {
            values.put(MediaTable.COLUMN_ENC_KEY, media.encKey);
        }
        if (media.encSha256hash != null) {
            values.put(MediaTable.COLUMN_SHA256_HASH, media.encSha256hash);
        }
        if (media.file != null && (media.width == 0 || media.height == 0)) {
            final Size dimensions = MediaUtils.getDimensions(media.file, media.type);
            if (dimensions != null && dimensions.getWidth() > 0 && dimensions.getHeight() > 0) {
                values.put(MediaTable.COLUMN_WIDTH, dimensions.getWidth());
                values.put(MediaTable.COLUMN_HEIGHT, dimensions.getHeight());
            }
        }
        values.put(MediaTable.COLUMN_BLOB_VERSION, media.blobVersion);
        if (media.blobVersion == Media.BLOB_VERSION_CHUNKED) {
            values.put(MediaTable.COLUMN_CHUNK_SIZE, media.chunkSize);
            values.put(MediaTable.COLUMN_BLOB_SIZE, media.blobSize);
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
    public void setPatchUrl(long rowId, @NonNull String url) {
        final ContentValues values = new ContentValues();
        values.put(MediaTable.COLUMN_PATCH_URL, url);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        try {
            db.updateWithOnConflict(MediaTable.TABLE_NAME,
                    values,
                    MediaTable._ID + "=?",
                    new String[]{Long.toString(rowId)},
                    SQLiteDatabase.CONFLICT_ABORT);
            Log.d("Resumable Uploader ContentDb.setPatchUrl = " + url);
        } catch (SQLiteConstraintException ex) {
            Log.i("Resumable Uploader PostDb setPatchUrl: seen duplicate", ex);
        } catch (SQLException ex) {
            Log.e("Resumable Uploader PostDb setPatchUrl: failed " + ex);
            throw ex;
        }
    }

    @WorkerThread
    public String getPatchUrl(long rowId) {
        final String sql =
                "SELECT " + MediaTable.TABLE_NAME + "." + MediaTable.COLUMN_PATCH_URL + " "
                        + "FROM " + MediaTable.TABLE_NAME + " "
                        + "WHERE " + MediaTable.TABLE_NAME + "." + MediaTable._ID + "=? LIMIT " + 1;

        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.rawQuery(sql, new String[]{Long.toString(rowId)})) {
            if (cursor.moveToNext()) {
                return cursor.getString(0);
            }
        }
        return null;
    }

    @WorkerThread
    public @Media.TransferredState int getMediaTransferred(long rowId) {
        final String sql =
                "SELECT " + MediaTable.TABLE_NAME + "." + MediaTable.COLUMN_TRANSFERRED + " "
                        + "FROM " + MediaTable.TABLE_NAME + " "
                        + "WHERE " + MediaTable.TABLE_NAME + "." + MediaTable._ID + "=? LIMIT " + 1;

        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.rawQuery(sql, new String[]{Long.toString(rowId)})) {
            if (cursor.moveToNext()) {
                return cursor.getInt(0);
            }
        }
        return Media.TRANSFERRED_UNKNOWN;
    }

    @WorkerThread
    public byte[] getMediaEncKey(long rowId) {
        final String sql =
                "SELECT " + MediaTable.TABLE_NAME + "." + MediaTable.COLUMN_ENC_KEY + " "
                        + "FROM " + MediaTable.TABLE_NAME + " "
                        + "WHERE " + MediaTable.TABLE_NAME + "." + MediaTable._ID + "=? LIMIT " + 1;

        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.rawQuery(sql, new String[]{Long.toString(rowId)})) {
            if (cursor.moveToNext()) {
                return cursor.getBlob(0);
            }
        }
        Log.d("Resumable Uploader PostsDb.getMediaEncKey failed to get encKey");
        return null;
    }

    @WorkerThread
    public void setUploadProgress(long rowId, long offset) {
        final ContentValues values = new ContentValues();
        values.put(MediaTable.COLUMN_UPLOAD_PROGRESS, offset);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        try {
            db.updateWithOnConflict(MediaTable.TABLE_NAME,
                    values,
                    MediaTable._ID + "=?",
                    new String[]{Long.toString(rowId)},
                    SQLiteDatabase.CONFLICT_ABORT);
            Log.d("Resumable Uploader PostsDb.setUploadProgress = " + offset);
        } catch (SQLiteConstraintException ex) {
            Log.i("Resumable Uploader PostDb setPatchUrl: seen duplicate ", ex);
        } catch (SQLException ex) {
            Log.e("Resumable Uploader PostDb setPatchUrl: failed " + ex);
            throw ex;
        }
    }

    @WorkerThread
    public long getUploadProgress(long rowId) {
        final String sql =
                "SELECT " + MediaTable.TABLE_NAME + "." + MediaTable.COLUMN_UPLOAD_PROGRESS + " "
                        + "FROM " + MediaTable.TABLE_NAME + " "
                        + "WHERE " + MediaTable.TABLE_NAME + "." + MediaTable._ID + "=? LIMIT " + 1;

        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.rawQuery(sql, new String[]{Long.toString(rowId)})) {
            if (cursor.moveToNext()) {
                return cursor.getLong(0);
            }
        }
        return 0;
    }

    @WorkerThread
    public void setRetryCount(long rowId, int count) {
        final ContentValues values = new ContentValues();
        values.put(MediaTable.COLUMN_RETRY_COUNT, count);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        try {
            db.updateWithOnConflict(MediaTable.TABLE_NAME,
                    values,
                    MediaTable._ID + "=?",
                    new String[]{Long.toString(rowId)},
                    SQLiteDatabase.CONFLICT_ABORT);
            Log.d("Resumable Uploader ContentDb.setRetryCount = " + count);
        } catch (SQLiteConstraintException ex) {
            Log.i("Resumable Uploader PostDb setPatchUrl: seen duplicate", ex);
        } catch (SQLException ex) {
            Log.e("Resumable Uploader PostDb setPatchUrl: failed " + ex);
            throw ex;
        }
    }

    @WorkerThread
    public int getRetryCount(long rowId) {
        final String sql =
                "SELECT " + MediaTable.TABLE_NAME + "." + MediaTable.COLUMN_RETRY_COUNT + " "
                        + "FROM " + MediaTable.TABLE_NAME + " "
                        + "WHERE " + MediaTable.TABLE_NAME + "." + MediaTable._ID + "=? LIMIT " + 1;

        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.rawQuery(sql, new String[]{Long.toString(rowId)})) {
            if (cursor.moveToNext()) {
                return cursor.getInt(0);
            }
        }
        return 0;
    }

    @WorkerThread
    void addComment(@NonNull Comment comment) {
        Log.i("ContentDb.addComment " + comment);
        if (comment.timestamp < getPostExpirationTime()) {
            throw new SQLiteConstraintException("attempting to add expired comment");
        }
        final ContentValues values = new ContentValues();
        values.put(CommentsTable.COLUMN_POST_ID, comment.postId);
        values.put(CommentsTable.COLUMN_COMMENT_SENDER_USER_ID, comment.senderUserId.rawId());
        values.put(CommentsTable.COLUMN_COMMENT_ID, comment.id);
        values.put(CommentsTable.COLUMN_PARENT_ID, comment.parentCommentId);
        values.put(CommentsTable.COLUMN_TIMESTAMP, comment.timestamp);
        values.put(CommentsTable.COLUMN_TRANSFERRED, comment.transferred);
        values.put(CommentsTable.COLUMN_SEEN, comment.seen);
        values.put(CommentsTable.COLUMN_TEXT, comment.text);
        values.put(CommentsTable.COLUMN_TYPE, comment.type);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        comment.rowId = db.insertWithOnConflict(CommentsTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_ABORT);
        mediaDb.addMedia(comment);
        mentionsDb.addMentions(comment);
        if (comment instanceof FutureProofComment) {
            futureProofDb.saveFutureProof((FutureProofComment) comment);
        }
        Log.i("ContentDb.addComment: added " + comment);
    }

    @WorkerThread
    void retractComment(@NonNull Comment comment) {
        Log.i("ContentDb.retractComment: senderUserId=" + comment.senderUserId + " commentId=" + comment.id);
        final ContentValues values = new ContentValues();
        values.put(CommentsTable.COLUMN_TRANSFERRED, !comment.senderUserId.isMe());
        values.put(CommentsTable.COLUMN_TEXT, (String)null);
        values.put(CommentsTable.COLUMN_TYPE, Comment.TYPE_RETRACTED);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            if (comment.rowId == 0) {
                final Comment dbComment = getComment(comment.id);
                if (dbComment != null) {
                    comment = dbComment;
                }
            }
            final int updatedCount = db.updateWithOnConflict(CommentsTable.TABLE_NAME, values,
                    CommentsTable.COLUMN_COMMENT_SENDER_USER_ID + "=? AND " + CommentsTable.COLUMN_COMMENT_ID + "=?",
                    new String [] {comment.senderUserId.rawId(), comment.id},
                    SQLiteDatabase.CONFLICT_ABORT);
            if (updatedCount == 0) {
                values.put(CommentsTable.COLUMN_POST_ID, comment.postId);
                values.put(CommentsTable.COLUMN_COMMENT_SENDER_USER_ID, comment.senderUserId.rawId());
                values.put(CommentsTable.COLUMN_COMMENT_ID, comment.id);
                values.put(CommentsTable.COLUMN_PARENT_ID, comment.parentCommentId);
                values.put(CommentsTable.COLUMN_TIMESTAMP, comment.timestamp);
                values.put(CommentsTable.COLUMN_SEEN, comment.seen);
                comment.rowId = db.insertWithOnConflict(CommentsTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_ABORT);
            } else {
                db.delete(MediaTable.TABLE_NAME,
                        MediaTable.COLUMN_PARENT_ROW_ID + "=? AND " + MediaTable.COLUMN_PARENT_TABLE + "='" + CommentsTable.TABLE_NAME + "'",
                        new String[]{Long.toString(comment.rowId)});
                db.delete(MentionsTable.TABLE_NAME,
                        MentionsTable.COLUMN_PARENT_ROW_ID + "=? AND " + MentionsTable.COLUMN_PARENT_TABLE + "='" + CommentsTable.TABLE_NAME + "'",
                        new String[]{Long.toString(comment.rowId)});
                for (Media media : comment.media) {
                    if (media.file != null) {
                        if (!media.file.delete()) {
                            Log.e("ContentDb.retractComment: failed to delete " + media.file.getAbsolutePath());
                        }
                    }
                }
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
    void setCommentTransferred(@NonNull String postId, @NonNull UserId commentSenderUserId, @NonNull String commentId) {
        Log.i("ContentDb.setCommentTransferred: senderUserId=" + commentSenderUserId + " commentId=" + commentId);
        final ContentValues values = new ContentValues();
        values.put(CommentsTable.COLUMN_TRANSFERRED, true);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        try {
            db.updateWithOnConflict(CommentsTable.TABLE_NAME, values,
                            CommentsTable.COLUMN_POST_ID + "=? AND " +
                            CommentsTable.COLUMN_COMMENT_SENDER_USER_ID + "=? AND " +
                            CommentsTable.COLUMN_COMMENT_ID + "=?",
                    new String [] {postId, commentSenderUserId.rawId(), commentId},
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
    boolean setCommentsSeen(@NonNull String postId, boolean seen) {
        Log.i("ContentDb.setCommentsSeen: postId=" + postId);
        final ContentValues values = new ContentValues();
        values.put(CommentsTable.COLUMN_SEEN, seen);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        try {
            final int updatedCount = db.updateWithOnConflict(CommentsTable.TABLE_NAME, values,
                            CommentsTable.COLUMN_POST_ID + "=? AND " +
                            CommentsTable.COLUMN_SEEN + "=" + (seen ? 0 : 1),
                    new String [] {postId},
                    SQLiteDatabase.CONFLICT_ABORT);
            return updatedCount > 0;
        } catch (SQLException ex) {
            Log.e("ContentDb.setCommentsSeen: failed");
            throw ex;
        }
    }

    @WorkerThread
    boolean setCommentSeen(@NonNull String postId, @NonNull String commentId, boolean seen) {
        Log.i("ContentDb.setCommentSeen: commentId=" + commentId);
        final ContentValues values = new ContentValues();
        values.put(CommentsTable.COLUMN_SEEN, seen);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        try {
            final int updatedCount = db.updateWithOnConflict(CommentsTable.TABLE_NAME, values,
                    CommentsTable.COLUMN_POST_ID + "=? AND " +
                            CommentsTable.COLUMN_COMMENT_ID + "=? AND " +
                            CommentsTable.COLUMN_SEEN + "=" + (seen ? 0 : 1),
                    new String [] {postId, commentId},
                    SQLiteDatabase.CONFLICT_ABORT);
            return updatedCount > 0;
        } catch (SQLException ex) {
            Log.e("ContentDb.setCommentSeen: failed");
            throw ex;
        }
    }

    @WorkerThread
    int getUnreadGroups() {
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        final String query = "SELECT DISTINCT " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_GROUP_ID
                + " FROM " + PostsTable.TABLE_NAME
                + " WHERE " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_GROUP_ID + " IS NOT NULL"
                + " AND " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_SEEN + "=0"
                + " AND " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_SENDER_USER_ID + "!=?";
        try (final Cursor cursor = db.rawQuery(query, new String[]{UserId.ME.rawId()})) {
            return cursor.getCount();
        }
    }

    @WorkerThread
    @NonNull List<Post> getPosts(@Nullable Long timestamp, @Nullable Integer count, boolean after, @Nullable UserId senderUserId, @Nullable GroupId groupId, boolean unseenOnly) {
        final List<Post> posts = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String where;
        String[] selectionArgs = null;
        if (timestamp == null) {
            where = PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TIMESTAMP + ">" + getPostExpirationTime();
        } else {
            if (after) {
                where = PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TIMESTAMP + ">" + getPostExpirationTime() + " AND " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TIMESTAMP + "<" + timestamp;
            } else {
                where = PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TIMESTAMP + ">" + Math.max(getPostExpirationTime(), timestamp);
            }
        }
        List<String> args = new ArrayList<>();
        if (senderUserId != null) {
            where += " AND " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_SENDER_USER_ID + "=?";
            args.add(senderUserId.rawId());
        }
        if (unseenOnly) {
            where += " AND " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_SEEN + "=0 AND " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_SENDER_USER_ID + " != ''";
        }
        if (groupId != null) {
            where += " AND " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_GROUP_ID + "=?";
            args.add(groupId.rawId());
        } else {
            where += " AND " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TYPE + "=" + Post.TYPE_USER;
            where += " AND (" + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TEXT + "!='' OR m." + MediaTable._ID + " IS NOT NULL OR " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_GROUP_ID + " IS NULL)";
        }
        if (!args.isEmpty()) {
            selectionArgs = args.toArray(new String[0]);
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
                PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_AUDIENCE_TYPE + "," +
                PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_GROUP_ID + "," +
                PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TYPE + "," +
                PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_USAGE + "," +
                "m." + MediaTable._ID + "," +
                "m." + MediaTable.COLUMN_TYPE + "," +
                "m." + MediaTable.COLUMN_URL + "," +
                "m." + MediaTable.COLUMN_FILE + "," +
                "m." + MediaTable.COLUMN_ENC_FILE + "," +
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
                    MediaTable.COLUMN_ENC_FILE + "," +
                    MediaTable.COLUMN_WIDTH + "," +
                    MediaTable.COLUMN_HEIGHT + "," +
                    MediaTable.COLUMN_TRANSFERRED + " FROM " + MediaTable.TABLE_NAME + " ORDER BY " + MediaTable._ID + " ASC) " +
                "AS m ON " + PostsTable.TABLE_NAME + "." + PostsTable._ID + "=m." + MediaTable.COLUMN_PARENT_ROW_ID + " AND '" + PostsTable.TABLE_NAME + "'=m." + MediaTable.COLUMN_PARENT_TABLE + " " +
            "LEFT JOIN (" +
                "SELECT " +
                    CommentsTable.COLUMN_POST_ID + "," +
                    "min(timestamp) as min_timestamp" + "," +
                    "count(*) AS comment_count" + ", " +
                    "sum(" + CommentsTable.COLUMN_SEEN + ") AS seen_comment_count" + " " +
                    "FROM " + CommentsTable.TABLE_NAME + " WHERE " + CommentsTable.COLUMN_TEXT + " IS NOT NULL " +
                    "OR (SELECT COUNT(*) FROM " + MediaTable.TABLE_NAME + " WHERE " + MediaTable.COLUMN_PARENT_TABLE + " = '" + CommentsTable.TABLE_NAME + "' AND " + MediaTable.COLUMN_PARENT_ROW_ID + " = " + CommentsTable.TABLE_NAME + "." + CommentsTable._ID + ") > 0" + " " +
                    "GROUP BY " + CommentsTable.COLUMN_POST_ID + ") " +
                "AS c ON " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_POST_ID + "=c." + CommentsTable.COLUMN_POST_ID + " " +
            "LEFT JOIN (" +
                "SELECT " +
                    CommentsTable._ID + " AS first_comment_row_id," +
                    CommentsTable.COLUMN_POST_ID + "," +
                    CommentsTable.COLUMN_TIMESTAMP + " AS first_comment_timestamp," +
                    CommentsTable.COLUMN_COMMENT_ID + " AS first_comment_id, " +
                    CommentsTable.COLUMN_COMMENT_SENDER_USER_ID + " AS first_comment_user_id, " +
                    CommentsTable.COLUMN_TEXT + " AS first_comment_text " +
                    "FROM " + CommentsTable.TABLE_NAME + " ) " +
                "AS fc ON " + "fc.first_comment_timestamp=c.min_timestamp AND " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_POST_ID + "=fc." + CommentsTable.COLUMN_POST_ID + " " +
            "LEFT JOIN (" +
                "SELECT " +
                    SeenTable.COLUMN_POST_ID + "," +
                    "count(*) AS seen_by_count " +
                    "FROM " + SeenTable.TABLE_NAME + " GROUP BY " + SeenTable.COLUMN_POST_ID + ") " +
                "AS s ON " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_SENDER_USER_ID + "=''" + " AND " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_POST_ID + "=s." + SeenTable.COLUMN_POST_ID + " " +
            "WHERE " + where + " " +
            "ORDER BY " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TIMESTAMP + (after ? " DESC " : " ASC ") +
                    (count == null ? "" : ("LIMIT " + count));

        try (final Cursor cursor = db.rawQuery(sql, selectionArgs)) {

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
                            cursor.getInt(9),
                            cursor.getString(6));
                    List<UserId> audienceList = new ArrayList<>();
                    List<UserId> excludeList = new ArrayList<>();
                    getPostAudienceInfo(post.id, audienceList, excludeList);
                    post.setAudience(cursor.getString(7), audienceList);
                    post.setExcludeList(excludeList);
                    post.usage = cursor.getInt(10);
                    post.commentCount = cursor.getInt(19);
                    post.unseenCommentCount = post.commentCount - cursor.getInt(20);
                    GroupId parentGroupId = GroupId.fromNullable(cursor.getString(8));
                    if (parentGroupId != null) {
                        post.setParentGroup(parentGroupId);
                    }
                    final String firstCommentId = cursor.getString(22);
                    if (firstCommentId != null) {
                        post.firstComment = new Comment(cursor.getLong(21),
                                post.id,
                                new UserId(cursor.getString(23)),
                                firstCommentId,
                                null,
                                cursor.getLong(25),
                                true,
                                true,
                                cursor.getString(24));
                        post.firstComment.setParentPost(post);
                        mentionsDb.fillMentions(post.firstComment);
                    }
                    post.seenByCount = cursor.getInt(26);
                }
                if (!cursor.isNull(11)) {
                    Media media = new Media(
                            cursor.getLong(11),
                            cursor.getInt(12),
                            cursor.getString(13),
                            fileStore.getMediaFile(cursor.getString(14)),
                            null,
                            null,
                            null,
                            cursor.getInt(16),
                            cursor.getInt(17),
                            cursor.getInt(18),
                            Media.BLOB_VERSION_UNKNOWN,
                            0,
                            0);
                    media.encFile = fileStore.getTmpFile(cursor.getString(15));
                    Preconditions.checkNotNull(post).media.add(media);
                }
            }
            if (post != null && (count == null || cursor.getCount() < count)) {
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
    @Nullable Post getPost(@NonNull String postId) {
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
                    PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_AUDIENCE_TYPE + "," +
                    PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_GROUP_ID + "," +
                    PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TYPE + "," +
                    "m." + MediaTable._ID + "," +
                    "m." + MediaTable.COLUMN_TYPE + "," +
                    "m." + MediaTable.COLUMN_URL + "," +
                    "m." + MediaTable.COLUMN_FILE + "," +
                    "m." + MediaTable.COLUMN_ENC_FILE + "," +
                    "m." + MediaTable.COLUMN_WIDTH + "," +
                    "m." + MediaTable.COLUMN_HEIGHT + "," +
                    "m." + MediaTable.COLUMN_TRANSFERRED + "," +
                    "s.seen_by_count " +
                "FROM " + PostsTable.TABLE_NAME + " " +
                "LEFT JOIN (" +
                    "SELECT " +
                        MediaTable._ID + "," +
                        MediaTable.COLUMN_PARENT_TABLE + "," +
                        MediaTable.COLUMN_PARENT_ROW_ID + "," +
                        MediaTable.COLUMN_TYPE + "," +
                        MediaTable.COLUMN_URL + "," +
                        MediaTable.COLUMN_FILE + "," +
                        MediaTable.COLUMN_ENC_FILE + "," +
                        MediaTable.COLUMN_WIDTH + "," +
                        MediaTable.COLUMN_HEIGHT + "," +
                        MediaTable.COLUMN_TRANSFERRED + " FROM " + MediaTable.TABLE_NAME + " ORDER BY " + MediaTable._ID + " ASC) " +
                    "AS m ON " + PostsTable.TABLE_NAME + "." + PostsTable._ID + "=m." + MediaTable.COLUMN_PARENT_ROW_ID + " AND '" + PostsTable.TABLE_NAME + "'=m." + MediaTable.COLUMN_PARENT_TABLE + " " +
                "LEFT JOIN (" +
                    "SELECT " +
                    SeenTable.COLUMN_POST_ID + "," +
                    "count(*) AS seen_by_count " +
                    "FROM " + SeenTable.TABLE_NAME + " GROUP BY " + SeenTable.COLUMN_POST_ID + ") " +
                    "AS s ON " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_SENDER_USER_ID + "=''" + " AND " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_POST_ID + "=s." + SeenTable.COLUMN_POST_ID + " " +
                "WHERE " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_POST_ID + "=?";

        try (final Cursor cursor = db.rawQuery(sql, new String [] {postId})) {

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
                            cursor.getInt(9),
                            cursor.getString(6));
                    List<UserId> audienceList = new ArrayList<>();
                    List<UserId> excludeList = new ArrayList<>();
                    getPostAudienceInfo(post.id, audienceList, excludeList);
                    post.setAudience(cursor.getString(7), audienceList);
                    post.setExcludeList(excludeList);
                    GroupId parentGroupId = GroupId.fromNullable(cursor.getString(8));
                    if (parentGroupId != null) {
                        post.setParentGroup(parentGroupId);
                    }
                    mentionsDb.fillMentions(post);
                    post.seenByCount = cursor.getInt(17);
                }
                if (!cursor.isNull(10)) {
                    Media media = new Media(
                            cursor.getLong(10),
                            cursor.getInt(11),
                            cursor.getString(12),
                            fileStore.getMediaFile(cursor.getString(13)),
                            null,
                            null,
                            null,
                            cursor.getInt(15),
                            cursor.getInt(16),
                            cursor.getInt(17),
                            Media.BLOB_VERSION_UNKNOWN,
                            0,
                            0);
                    media.encFile = fileStore.getTmpFile(cursor.getString(14));
                    Preconditions.checkNotNull(post).media.add(media);
                }
            }
        }
        Log.i("ContentDb.getPost: post=" + post);
        return post;
    }

    @WorkerThread
    @Nullable Post getArchivePost(@NonNull String postId) {
        Post post = null;
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        final String sql =
                "SELECT " +
                        ArchiveTable.TABLE_NAME + "." + ArchiveTable._ID + "," +
                        ArchiveTable.TABLE_NAME + "." + ArchiveTable.COLUMN_POST_ID + "," +
                        ArchiveTable.TABLE_NAME + "." + ArchiveTable.COLUMN_TIMESTAMP + "," +
                        ArchiveTable.TABLE_NAME + "." + ArchiveTable.COLUMN_TEXT + "," +
                        ArchiveTable.TABLE_NAME + "." + ArchiveTable.COLUMN_GROUP_ID + "," +
                        ArchiveTable.TABLE_NAME + "." + ArchiveTable.COLUMN_ARCHIVE_TIMESTAMP + "," +
                        "m." + MediaTable._ID + "," +
                        "m." + MediaTable.COLUMN_TYPE + "," +
                        "m." + MediaTable.COLUMN_URL + "," +
                        "m." + MediaTable.COLUMN_FILE + "," +
                        "m." + MediaTable.COLUMN_ENC_FILE + "," +
                        "m." + MediaTable.COLUMN_WIDTH + "," +
                        "m." + MediaTable.COLUMN_HEIGHT + "," +
                        "m." + MediaTable.COLUMN_TRANSFERRED + " " +
                        "FROM " + ArchiveTable.TABLE_NAME + " " +
                        "LEFT JOIN (" +
                        "SELECT " +
                        MediaTable._ID + "," +
                        MediaTable.COLUMN_PARENT_TABLE + "," +
                        MediaTable.COLUMN_PARENT_ROW_ID + "," +
                        MediaTable.COLUMN_TYPE + "," +
                        MediaTable.COLUMN_URL + "," +
                        MediaTable.COLUMN_FILE + "," +
                        MediaTable.COLUMN_ENC_FILE + "," +
                        MediaTable.COLUMN_WIDTH + "," +
                        MediaTable.COLUMN_HEIGHT + "," +
                        MediaTable.COLUMN_TRANSFERRED + " FROM " + MediaTable.TABLE_NAME + " ORDER BY " + MediaTable._ID + " ASC) " +
                        "AS m ON " + ArchiveTable.TABLE_NAME + "." + ArchiveTable._ID + "=m." + MediaTable.COLUMN_PARENT_ROW_ID + " AND '" + ArchiveTable.TABLE_NAME + "'=m." + MediaTable.COLUMN_PARENT_TABLE + " " +
                        "WHERE " + ArchiveTable.TABLE_NAME + "." + ArchiveTable.COLUMN_POST_ID + "=?";

        try (final Cursor cursor = db.rawQuery(sql, new String[] {postId})) {

            while (cursor.moveToNext()) {
                long rowId = cursor.getLong(0);
                if (post == null) {
                    post = new Post(
                            rowId,
                            UserId.ME,
                            cursor.getString(1),
                            cursor.getLong(2),
                            Post.TRANSFERRED_YES,
                            Post.SEEN_NO,
                            Post.TYPE_USER,
                            cursor.getString(3));

                    GroupId parentGroupId = GroupId.fromNullable(cursor.getString(4));
                    if (parentGroupId != null) {
                        post.setParentGroup(parentGroupId);
                    }
                    post.isArchived = true;
                    post.archiveDate = cursor.getLong(5);
                    mentionsDb.fillMentions(post);
                }
                if (!cursor.isNull(6)) {
                    Media media = new Media(
                            cursor.getLong(6),
                            cursor.getInt(7),
                            cursor.getString(8),
                            fileStore.getMediaFile(cursor.getString(9)),
                            null,
                            null,
                            null,
                            cursor.getInt(11),
                            cursor.getInt(12),
                            cursor.getInt(13),
                            Media.BLOB_VERSION_UNKNOWN,
                            0,
                            0);
                    media.encFile = fileStore.getTmpFile(cursor.getString(10));
                    Preconditions.checkNotNull(post).media.add(media);
                }
            }
        }
        Log.i("ContentDb.getArchivePost: post=" + post);
        return post;
    }

    @WorkerThread
    long getLastSeenCommentRowId(@NonNull String postId) {
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        final String sql =
                "SELECT MAX(" + CommentsTable._ID + ") FROM " + CommentsTable.TABLE_NAME + " " +
                "WHERE " + CommentsTable.COLUMN_POST_ID + "=? AND " + CommentsTable.COLUMN_SEEN + "=1";
        try (final Cursor cursor = db.rawQuery(sql, new String [] {postId})) {
            if (cursor.moveToNext()) {
                return cursor.isNull(0) ? -1 : cursor.getLong(0);
            } else {
                return -1;
            }
        }
    }

    @WorkerThread
    @NonNull List<Comment> getCommentsFlat(@NonNull String postId, int start, int count) {
        final String sqls = "SELECT 0, _id, timestamp, parent_id, comment_sender_user_id, comment_id, transferred, seen, text, type FROM comments WHERE post_id=? AND timestamp > " + getPostExpirationTime() + " ORDER BY timestamp ASC LIMIT " + count + " OFFSET " + start ;
        final List<Comment> comments = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Post parentPost = getPost(postId);
        try (final Cursor cursor = db.rawQuery(sqls, new String [] {postId})) {
            while (cursor.moveToNext()) {
                final Comment comment = new Comment(
                        cursor.getLong(1),
                        postId,
                        new UserId(cursor.getString(4)),
                        cursor.getString(5),
                        cursor.getString(3),
                        cursor.getLong(2),
                        cursor.getInt(6) == 1,
                        cursor.getInt(7) == 1,
                        cursor.getString(8));
                comment.type = cursor.getInt(9);
                fillMedia(comment);
                mentionsDb.fillMentions(comment);
                comment.setParentPost(parentPost);
                comments.add(comment);
            }
        }
        Log.i("ContentDb.getComments: start=" + start + " count=" + count + " comments.size=" + comments.size());
        return comments;
    }

    @WorkerThread
    @NonNull List<Comment> getComments(@NonNull String postId, int start, int count) {
        final String sql =
                "WITH RECURSIVE " +
                    "comments_tree(level, _id, timestamp, parent_id, comment_sender_user_id, comment_id, transferred, seen, text, type) AS ( " +
                        "SELECT 0, _id, timestamp, parent_id, comment_sender_user_id, comment_id, transferred, seen, text, type FROM comments WHERE post_id=? AND parent_id IS NULL AND timestamp > " + getPostExpirationTime() + " " +
                        "UNION ALL " +
                        "SELECT comments_tree.level+1, comments._id, comments.timestamp, comments.parent_id, comments.comment_sender_user_id, comments.comment_id, comments.transferred, comments.seen, comments.text, comments.type " +
                            "FROM comments, comments_tree WHERE comments.parent_id=comments_tree.comment_id ORDER BY 1 DESC, 2) " +
                "SELECT * FROM comments_tree LIMIT " + count + " OFFSET " + start;
        final List<Comment> comments = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Post parentPost = getPost(postId);
        try (final Cursor cursor = db.rawQuery(sql, new String [] {postId})) {
            while (cursor.moveToNext()) {
                final Comment comment = new Comment(
                        cursor.getLong(1),
                        postId,
                        new UserId(cursor.getString(4)),
                        cursor.getString(5),
                        cursor.getString(3),
                        cursor.getLong(2),
                        cursor.getInt(6) == 1,
                        cursor.getInt(7) == 1,
                        cursor.getString(8));
                comment.type = cursor.getInt(9);
                fillMedia(comment);
                mentionsDb.fillMentions(comment);
                comment.setParentPost(parentPost);
                comments.add(comment);
            }
        }
        Log.i("ContentDb.getComments: start=" + start + " count=" + count + " comments.size=" + comments.size());
        return comments;
    }

    @WorkerThread
    int getCommentCount(@NonNull String postId) {
        final String sql = "SELECT 0 FROM " + CommentsTable.TABLE_NAME +" WHERE " + CommentsTable.COLUMN_POST_ID + "=?";
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        int count;
        try (final Cursor cursor = db.rawQuery(sql, new String [] {postId})) {
            count = cursor.getCount();
        }
        Log.i("ContentDb.getCommentCount: count=" + count);
        return count;
    }

    // TODO(jack): Switch to this style for loading media everywhere and move to MediaDb (Terlici team adding)
    @WorkerThread
    private void fillMedia(@NonNull Comment comment) {
        comment.media.clear();
        comment.media.addAll(readMedia(CommentsTable.TABLE_NAME, comment.rowId));
    }

    @WorkerThread
    private List<Media> readMedia(@NonNull String parentTable, long parentRowId) {
        String sql =
                "SELECT " +
                    MediaTable._ID + "," +
                    MediaTable.COLUMN_TYPE + "," +
                    MediaTable.COLUMN_URL + "," +
                    MediaTable.COLUMN_FILE + "," +
                    MediaTable.COLUMN_ENC_FILE + "," +
                    MediaTable.COLUMN_WIDTH + "," +
                    MediaTable.COLUMN_HEIGHT + "," +
                    MediaTable.COLUMN_TRANSFERRED + "," +
                    MediaTable.COLUMN_ENC_KEY + "," +
                    MediaTable.COLUMN_SHA256_HASH + "," +
                    MediaTable.COLUMN_DEC_SHA256_HASH + "," +
                    MediaTable.COLUMN_BLOB_VERSION + "," +
                    MediaTable.COLUMN_CHUNK_SIZE + "," +
                    MediaTable.COLUMN_BLOB_SIZE + " " +
                "FROM " + MediaTable.TABLE_NAME + " " +
                "WHERE " + MediaTable.TABLE_NAME + "." + MediaTable.COLUMN_PARENT_ROW_ID + " = ? AND " + MediaTable.TABLE_NAME + "." + MediaTable.COLUMN_PARENT_TABLE + " = ?" +
                "ORDER BY " + MediaTable._ID + " ASC";
        List<Media> ret = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.rawQuery(sql, new String[] {Long.toString(parentRowId), parentTable})) {
            while (cursor.moveToNext()) {
                Media media = new Media(
                        cursor.getLong(0),
                        cursor.getInt(1),
                        cursor.getString(2),
                        fileStore.getMediaFile(cursor.getString(3)),
                        cursor.getBlob(8),
                        cursor.getBlob(9),
                        cursor.getBlob(10),
                        cursor.getInt(5),
                        cursor.getInt(6),
                        cursor.getInt(7),
                        cursor.getInt(11),
                        cursor.getInt(12),
                        cursor.getLong(13));
                media.encFile = fileStore.getTmpFile(cursor.getString(4));
                ret.add(media);
            }
        }
        return ret;
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
                        "m." + MediaTable.COLUMN_ENC_FILE + "," +
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
                        MediaTable.COLUMN_ENC_FILE + "," +
                        MediaTable.COLUMN_WIDTH + "," +
                        MediaTable.COLUMN_HEIGHT + "," +
                        MediaTable.COLUMN_TRANSFERRED + " FROM " + MediaTable.TABLE_NAME + " ORDER BY " + MediaTable._ID + " ASC) " +
                        "AS m ON p." + PostsTable._ID + "=m." + MediaTable.COLUMN_PARENT_ROW_ID + " AND '" + PostsTable.TABLE_NAME + "'=m." + MediaTable.COLUMN_PARENT_TABLE + " " +
                        "WHERE " + MentionsTable.TABLE_NAME + "." + MentionsTable.COLUMN_MENTION_USER_ID + "=? AND " + MentionsTable.TABLE_NAME + "." + MentionsTable.COLUMN_PARENT_TABLE + "=? " +
                        (limit < 0 ? "" : "LIMIT " + limit);
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
                    Media media = new Media(
                            cursor.getLong(10),
                            cursor.getInt(11),
                            cursor.getString(12),
                            fileStore.getMediaFile(cursor.getString(13)),
                            null,
                            null,
                            null,
                            cursor.getInt(15),
                            cursor.getInt(16),
                            cursor.getInt(17),
                            Media.BLOB_VERSION_UNKNOWN,
                            0,
                            0);
                    media.encFile = fileStore.getTmpFile(cursor.getString(14));
                    Preconditions.checkNotNull(post).media.add(media);
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
                        "INNER JOIN " + CommentsTable.TABLE_NAME + " " +
                        "AS c ON " + MentionsTable.TABLE_NAME + "." + MentionsTable.COLUMN_PARENT_ROW_ID + "=c." + CommentsTable._ID + " " +
                        "WHERE " + MentionsTable.TABLE_NAME + "." + MentionsTable.COLUMN_MENTION_USER_ID + "=? AND " + MentionsTable.TABLE_NAME + "." + MentionsTable.COLUMN_PARENT_TABLE + "=? " +
                        (limit < 0 ? "" : "LIMIT " + limit);
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        final HashMap<String, Post> postCache = new HashMap<>();
        final HashSet<String> checkedIds = new HashSet<>();
        try (final Cursor cursor = db.rawQuery(sql, new String[]{mentionedUserId.rawId(), CommentsTable.TABLE_NAME})) {
            while (cursor.moveToNext()) {
                final Comment comment = new Comment(
                        cursor.getLong(0),
                        cursor.getString(1),
                        new UserId(cursor.getString(2)),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getLong(5),
                        cursor.getInt(6) == 1,
                        cursor.getInt(8) == 1,
                        cursor.getString(7));
                if (checkedIds.contains(comment.postId)) {
                    comment.setParentPost(postCache.get(comment.postId));
                } else {
                    Post parentPost = getPost(comment.postId);
                    checkedIds.add(comment.postId);
                    if (parentPost != null) {
                        postCache.put(comment.postId, parentPost);
                        comment.setParentPost(parentPost);
                    }
                }
                mentionsDb.fillMentions(comment);
                mentionedComments.add(comment);
            }
        }
        return mentionedComments;
    }

    @WorkerThread
    @Nullable Comment getComment(@NonNull String commentId) {
        final String sql =
                "SELECT " +
                        CommentsTable._ID + ", " +
                        CommentsTable.COLUMN_POST_ID + ", " +
                        CommentsTable.COLUMN_COMMENT_SENDER_USER_ID + ", " +
                        CommentsTable.COLUMN_COMMENT_ID + ", " +
                        CommentsTable.COLUMN_PARENT_ID + ", " +
                        CommentsTable.COLUMN_TIMESTAMP + ", " +
                        CommentsTable.COLUMN_TRANSFERRED + ", " +
                        CommentsTable.COLUMN_TEXT + ", " +
                        CommentsTable.COLUMN_SEEN + " " +
                        "FROM " + CommentsTable.TABLE_NAME + " " +
                        "WHERE comments.comment_id=? " +
                        "AND comments.timestamp>" + getPostExpirationTime() + " " +
                        "LIMIT " + 1;
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.rawQuery(sql, new String[] {commentId})) {
            if (cursor.moveToNext()) {
                final Comment comment = new Comment(
                        cursor.getLong(0),
                        cursor.getString(1),
                        new UserId(cursor.getString(2)),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getLong(5),
                        cursor.getInt(6) == 1,
                        cursor.getInt(8) == 1,
                        cursor.getString(7));
                Post parentPost = getPost(comment.postId);
                comment.setParentPost(parentPost);
                fillMedia(comment);
                mentionsDb.fillMentions(comment);
                return comment;
            }
        }
        return null;
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
                        "EXISTS(SELECT post_id FROM " + PostsTable.TABLE_NAME + " WHERE posts.post_id=comments.post_id AND posts.sender_user_id='')" +
                        "OR " +
                        "EXISTS(SELECT post_id FROM comments AS c WHERE comments.post_id==c.post_id AND c.comment_sender_user_id='') " +
                    ")" +
                "ORDER BY " + CommentsTable.TABLE_NAME + "." + CommentsTable.COLUMN_TIMESTAMP + " DESC " +
                (limit < 0 ? "" : "LIMIT " + limit);
        try (final Cursor cursor = db.rawQuery(sql, null)) {
            while (cursor.moveToNext()) {

                final Comment comment = new Comment(
                        cursor.getLong(0),
                        cursor.getString(1),
                        new UserId(cursor.getString(2)),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getLong(5),
                        cursor.getInt(6) == 1,
                        cursor.getInt(8) == 1,
                        cursor.getString(7));
                mentionsDb.fillMentions(comment);
                comment.setParentPost(getPost(comment.postId));
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
        final HashMap<String, Post> postCache = new HashMap<>();
        final HashSet<String> checkedIds = new HashSet<>();
        try (final Cursor cursor = db.query(CommentsTable.TABLE_NAME,
                new String [] {
                        CommentsTable._ID,
                        CommentsTable.COLUMN_POST_ID,
                        CommentsTable.COLUMN_COMMENT_SENDER_USER_ID,
                        CommentsTable.COLUMN_COMMENT_ID,
                        CommentsTable.COLUMN_PARENT_ID,
                        CommentsTable.COLUMN_TIMESTAMP,
                        CommentsTable.COLUMN_TRANSFERRED,
                        CommentsTable.COLUMN_SEEN,
                        CommentsTable.COLUMN_TEXT},
                CommentsTable.COLUMN_SEEN + "=0 AND " + CommentsTable.COLUMN_TIMESTAMP + ">" + Math.max(timestamp, getPostExpirationTime()) + " AND " +
                        CommentsTable.COLUMN_POST_ID + " IN (SELECT " + PostsTable.COLUMN_POST_ID + " FROM " + PostsTable.TABLE_NAME + " WHERE " + PostsTable.COLUMN_SENDER_USER_ID + "='')",
                null, null, null, CommentsTable.COLUMN_TIMESTAMP + " ASC LIMIT " + count)) {
            while (cursor.moveToNext()) {
                final Comment comment = new Comment(
                        cursor.getLong(0),
                        cursor.getString(1),
                        new UserId(cursor.getString(2)),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getLong(5),
                        cursor.getInt(6) == 1,
                        cursor.getInt(7) == 1,
                        cursor.getString(8));
                if (!checkedIds.contains(comment.postId)) {
                    Post parentPost = getPost(comment.postId);
                    if (parentPost != null) {
                        postCache.put(comment.postId, parentPost);
                        comment.setParentPost(parentPost);
                    }
                    checkedIds.add(comment.postId);
                } else {
                    comment.setParentPost(postCache.get(comment.postId));
                }
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
    void getPostAudienceInfo(@NonNull String postId, @NonNull List<UserId> audienceList, @NonNull List<UserId> excludeList) {
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.query(AudienceTable.TABLE_NAME,
                new String [] {AudienceTable.COLUMN_USER_ID, AudienceTable.COLUMN_EXCLUDED},
                AudienceTable.COLUMN_POST_ID + "=?",
                new String [] {postId}, null, null, AudienceTable._ID + " DESC")) {
            while (cursor.moveToNext()) {
                boolean excluded = cursor.getInt(1) == 1;
                UserId userId = new UserId(cursor.getString(0));
                if (excluded) {
                    excludeList.add(userId);
                } else {
                    audienceList.add(userId);
                }
            }
        }
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
        Log.i("ContentDb.getSeenBy: seenByInfos.size=" + seenByInfos.size() + " for post " + postId);
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
                    PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_AUDIENCE_TYPE + "," +
                    PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_GROUP_ID + "," +
                    PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TYPE + "," +
                    PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_USAGE + "," +
                    "m." + MediaTable._ID + "," +
                    "m." + MediaTable.COLUMN_TYPE + "," +
                    "m." + MediaTable.COLUMN_URL + "," +
                    "m." + MediaTable.COLUMN_FILE + "," +
                    "m." + MediaTable.COLUMN_ENC_FILE + "," +
                    "m." + MediaTable.COLUMN_ENC_KEY + "," +
                    "m." + MediaTable.COLUMN_SHA256_HASH + "," +
                    "m." + MediaTable.COLUMN_DEC_SHA256_HASH + "," +
                    "m." + MediaTable.COLUMN_WIDTH + "," +
                    "m." + MediaTable.COLUMN_HEIGHT + "," +
                    "m." + MediaTable.COLUMN_TRANSFERRED + "," +
                    "m." + MediaTable.COLUMN_BLOB_VERSION + ", " +
                    "m." + MediaTable.COLUMN_CHUNK_SIZE + "," +
                    "m." + MediaTable.COLUMN_BLOB_SIZE + " " +
                "FROM " + PostsTable.TABLE_NAME + " " +
                "LEFT JOIN (" +
                    "SELECT " +
                        MediaTable._ID + "," +
                        MediaTable.COLUMN_PARENT_TABLE + "," +
                        MediaTable.COLUMN_PARENT_ROW_ID + "," +
                        MediaTable.COLUMN_TYPE + "," +
                        MediaTable.COLUMN_URL + "," +
                        MediaTable.COLUMN_FILE + "," +
                        MediaTable.COLUMN_ENC_FILE + "," +
                        MediaTable.COLUMN_ENC_KEY + "," +
                        MediaTable.COLUMN_SHA256_HASH + "," +
                        MediaTable.COLUMN_DEC_SHA256_HASH + "," +
                        MediaTable.COLUMN_WIDTH + "," +
                        MediaTable.COLUMN_HEIGHT + "," +
                        MediaTable.COLUMN_TRANSFERRED + "," +
                        MediaTable.COLUMN_BLOB_VERSION + "," +
                        MediaTable.COLUMN_CHUNK_SIZE + "," +
                        MediaTable.COLUMN_BLOB_SIZE + " FROM " + MediaTable.TABLE_NAME + " ORDER BY " + MediaTable._ID + " ASC) " +
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
                            cursor.getInt(9),
                            cursor.getString(6));
                    mentionsDb.fillMentions(post);
                    List<UserId> audienceList = new ArrayList<>();
                    List<UserId> excludeList = new ArrayList<>();
                    getPostAudienceInfo(post.id, audienceList, excludeList);
                    post.setAudience(cursor.getString(7), audienceList);
                    post.setExcludeList(excludeList);
                    GroupId groupId = GroupId.fromNullable(cursor.getString(8));

                    if (groupId != null) {
                        post.setParentGroup(groupId);
                    }
                }
                if (!cursor.isNull(11)) {
                    Media media = new Media(
                            cursor.getLong(11),
                            cursor.getInt(12),
                            cursor.getString(13),
                            fileStore.getMediaFile(cursor.getString(14)),
                            cursor.getBlob(16),
                            cursor.getBlob(17),
                            cursor.getBlob(18),
                            cursor.getInt(19),
                            cursor.getInt(20),
                            cursor.getInt(21),
                            cursor.getInt(22),
                            cursor.getInt(23),
                            cursor.getLong(24));
                    media.encFile = fileStore.getTmpFile(cursor.getString(15));
                    Preconditions.checkNotNull(post).media.add(media);
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
    void deleteGroup(@NonNull GroupId groupId) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        final String sql =
                "SELECT " +
                        "m." + MediaTable.COLUMN_FILE + "," +
                        "m." + MediaTable.COLUMN_ENC_FILE + " " +
                        "FROM " + PostsTable.TABLE_NAME + " " +
                        "INNER JOIN (" +
                        "SELECT " +
                        MediaTable.COLUMN_PARENT_TABLE + "," +
                        MediaTable.COLUMN_PARENT_ROW_ID + "," +
                        MediaTable.COLUMN_FILE + "," +
                        MediaTable.COLUMN_ENC_FILE + "," +
                        MediaTable.COLUMN_TRANSFERRED + " FROM " + MediaTable.TABLE_NAME + ")" +
                        "AS m ON " + PostsTable.TABLE_NAME + "." + PostsTable._ID + "=m." + MediaTable.COLUMN_PARENT_ROW_ID + " AND '" + PostsTable.TABLE_NAME + "'=m." + MediaTable.COLUMN_PARENT_TABLE + " " +
                        "WHERE " + PostsTable.COLUMN_GROUP_ID + "=?";
        int deletedFiles = 0;
        try (final Cursor cursor = db.rawQuery(sql, new String[]{ groupId.rawId() })) {
            while (cursor.moveToNext()) {
                final File mediaFile = fileStore.getMediaFile(cursor.getString(0));
                if (mediaFile != null) {
                    if (mediaFile.delete()) {
                        deletedFiles++;
                    } else {
                        Log.i("PostsDb/deleteGroup: failed to delete " + mediaFile.getAbsolutePath());
                    }
                }
                final File encFile = fileStore.getTmpFile(cursor.getString(1));
                if (encFile != null) {
                    if (encFile.delete()) {
                        deletedFiles++;
                    } else {
                        Log.i("PostsDb/deleteGroup: failed to delete " + encFile.getAbsolutePath());
                    }
                }
            }
        }
        Log.i("PostsDb/deleteGroup: " + deletedFiles + " media files deleted");

        final int deletedPosts = db.delete(PostsTable.TABLE_NAME, PostsTable.COLUMN_GROUP_ID + "=?", new String[] {groupId.rawId()});
        Log.i("PostsDb/deleteGroup: " + deletedPosts + " posts deleted for group id " + groupId.rawId());
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    @WorkerThread
    @NonNull List<Comment> getPendingComments() {
        final List<Comment> comments = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        final HashSet<String> checkedPosts = new HashSet<>();
        final HashMap<String, Post> posts = new HashMap<>();
        try (final Cursor cursor = db.query(CommentsTable.TABLE_NAME,
                new String [] {
                        CommentsTable._ID,
                        CommentsTable.COLUMN_POST_ID,
                        CommentsTable.COLUMN_COMMENT_SENDER_USER_ID,
                        CommentsTable.COLUMN_COMMENT_ID,
                        CommentsTable.COLUMN_PARENT_ID,
                        CommentsTable.COLUMN_TIMESTAMP,
                        CommentsTable.COLUMN_TRANSFERRED,
                        CommentsTable.COLUMN_SEEN,
                        CommentsTable.COLUMN_TEXT,
                        CommentsTable.COLUMN_TYPE},
                CommentsTable.COLUMN_COMMENT_SENDER_USER_ID + "='' AND " + CommentsTable.COLUMN_TRANSFERRED + "=0 AND " + CommentsTable.COLUMN_TIMESTAMP + ">" + getPostExpirationTime(),
                null, null, null, null)) {
            while (cursor.moveToNext()) {
                final Comment comment = new Comment(
                        cursor.getLong(0),
                        cursor.getString(1),
                        new UserId(cursor.getString(2)),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getLong(5),
                        cursor.getInt(6) == 1,
                        cursor.getInt(7) == 1,
                        cursor.getString(8));
                if (!checkedPosts.contains(comment.postId)) {
                    Post parentPost = getPost(comment.postId);
                    checkedPosts.add(comment.postId);
                    if (parentPost != null) {
                        posts.put(comment.postId, parentPost);
                    }
                }
                comment.type = cursor.getInt(9);
                mentionsDb.fillMentions(comment);
                fillMedia(comment);
                comment.setParentPost(posts.get(comment.postId));
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

    @NonNull
    List<Post> getShareablePosts() {
        List<Post> ret = new ArrayList<>();
        List<Post> posts = getPosts(System.currentTimeMillis() - Constants.SHARE_OLD_POST_LIMIT, null, false, UserId.ME, null, false);
        for (Post post : posts) {
            if (!post.isRetracted()) {
                ret.add(post);
            }
        }
        return ret;
    }

    @NonNull
    List<Post> getArchivedPosts(Long timestamp, Integer pageSize, boolean after) {
        final List<Post> posts = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String where;
        if (timestamp != null) {
            where = "WHERE " + (after ?  ArchiveTable.TABLE_NAME + "." + ArchiveTable.COLUMN_TIMESTAMP + ">" + timestamp : ArchiveTable.TABLE_NAME + "." + ArchiveTable.COLUMN_TIMESTAMP + "<" + timestamp) + " ";
        } else {
            if (after) {
                where = "WHERE " + ArchiveTable.TABLE_NAME + "." + ArchiveTable.COLUMN_TIMESTAMP + ">" + 0L + " ";
            } else {
                where = "WHERE " + ArchiveTable.TABLE_NAME + "." + ArchiveTable.COLUMN_TIMESTAMP + "<" + Long.MAX_VALUE + " ";
            }
        }

        String sql =
                "SELECT " +
                        ArchiveTable.TABLE_NAME + "." + ArchiveTable._ID + "," +
                        ArchiveTable.TABLE_NAME + "." + ArchiveTable.COLUMN_POST_ID + "," +
                        ArchiveTable.TABLE_NAME + "." + ArchiveTable.COLUMN_TIMESTAMP + "," +
                        ArchiveTable.TABLE_NAME + "." + ArchiveTable.COLUMN_TEXT + "," +
                        ArchiveTable.TABLE_NAME + "." + ArchiveTable.COLUMN_GROUP_ID + "," +
                        ArchiveTable.TABLE_NAME + "." + ArchiveTable.COLUMN_ARCHIVE_TIMESTAMP + "," +
                        "m." + MediaTable._ID + "," +
                        "m." + MediaTable.COLUMN_TYPE + "," +
                        "m." + MediaTable.COLUMN_URL + "," +
                        "m." + MediaTable.COLUMN_FILE + "," +
                        "m." + MediaTable.COLUMN_ENC_FILE + "," +
                        "m." + MediaTable.COLUMN_WIDTH + "," +
                        "m." + MediaTable.COLUMN_HEIGHT + "," +
                        "m." + MediaTable.COLUMN_TRANSFERRED + " " +
                        "FROM " + ArchiveTable.TABLE_NAME + " " +
                        "LEFT JOIN (" +
                        "SELECT " +
                            MediaTable._ID + "," +
                            MediaTable.COLUMN_PARENT_TABLE + "," +
                            MediaTable.COLUMN_PARENT_ROW_ID + "," +
                            MediaTable.COLUMN_TYPE + "," +
                            MediaTable.COLUMN_URL + "," +
                            MediaTable.COLUMN_FILE + "," +
                            MediaTable.COLUMN_ENC_FILE + "," +
                            MediaTable.COLUMN_WIDTH + "," +
                            MediaTable.COLUMN_HEIGHT + "," +
                            MediaTable.COLUMN_TRANSFERRED + " FROM " + MediaTable.TABLE_NAME + " ORDER BY " + MediaTable._ID + " ASC) " +
                        "AS m ON " + ArchiveTable.TABLE_NAME + "." + ArchiveTable._ID + "=m." + MediaTable.COLUMN_PARENT_ROW_ID + " AND '" + ArchiveTable.TABLE_NAME + "'=m." + MediaTable.COLUMN_PARENT_TABLE + " " +
                        where + "ORDER BY " + ArchiveTable.TABLE_NAME + "." + ArchiveTable.COLUMN_TIMESTAMP + (after ? " ASC " : " DESC ") +
                        (pageSize == null ? "" : ("LIMIT " + pageSize));

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
                            UserId.ME,
                            cursor.getString(1),
                            cursor.getLong(2),
                            Post.TRANSFERRED_NO,
                            Post.SEEN_NO,
                            Post.TYPE_USER,
                            cursor.getString(3));

                    GroupId parentGroupId = GroupId.fromNullable(cursor.getString(4));
                    if (parentGroupId != null) {
                        post.setParentGroup(parentGroupId);
                    }
                    post.isArchived = true;
                    post.archiveDate = cursor.getLong(5);
                    mentionsDb.fillMentions(post);
                }
                if (!cursor.isNull(6)) {
                    Media media = new Media(
                            cursor.getLong(6),
                            cursor.getInt(7),
                            cursor.getString(8),
                            fileStore.getMediaFile(cursor.getString(9)),
                            null,
                            null,
                            null,
                            cursor.getInt(11),
                            cursor.getInt(12),
                            cursor.getInt(13),
                            Media.BLOB_VERSION_UNKNOWN,
                            0,
                            0);
                    media.encFile = fileStore.getTmpFile(cursor.getString(10));
                    Preconditions.checkNotNull(post).media.add(media);
                }
            }
            if (post != null && (pageSize == null || cursor.getCount() < pageSize)) {
                posts.add(post);
            }
        }
        Log.i("ContentDb.getArchivedPosts: timestamp=" + timestamp + " count=" + pageSize + " posts.size=" + posts.size() + (posts.isEmpty() ? "" : (" got posts from " + posts.get(0).timestamp + " to " + posts.get(posts.size()-1).timestamp)));
        return posts;
    }

    @WorkerThread
    public void processFutureProofContent(FeedContentParser parser, ContentDbObservers observers) {
        processFutureProofComments(parser, observers);
    }

    private void processFutureProofComments(FeedContentParser parser, ContentDbObservers observers) {
        List<FutureProofComment> futureProofComments = getFutureProofComments();
        for (FutureProofComment futureProofComment : futureProofComments) {
            try {
                CommentContainer commentContainer = CommentContainer.parseFrom(futureProofComment.getProtoBytes());
                Comment comment = parser.parseComment(futureProofComment.id, futureProofComment.parentCommentId, futureProofComment.senderUserId, futureProofComment.timestamp, commentContainer);
                if (comment instanceof FutureProofComment) {
                    continue;
                }
                replaceFutureProofComment(futureProofComment, comment);
                observers.notifyCommentAdded(comment);
            } catch (InvalidProtocolBufferException e) {
                Log.e("PostsDb.processFutureProofComments failed to parse proto", e);
            }
        }
    }

    private void replaceFutureProofComment(@NonNull FutureProofComment original, @NonNull Comment newComment) {
        final ContentValues values = new ContentValues();
        values.put(CommentsTable.COLUMN_POST_ID, newComment.postId);
        values.put(CommentsTable.COLUMN_COMMENT_SENDER_USER_ID, newComment.senderUserId.rawId());
        values.put(CommentsTable.COLUMN_COMMENT_ID, newComment.id);
        values.put(CommentsTable.COLUMN_PARENT_ID, newComment.parentCommentId);
        values.put(CommentsTable.COLUMN_TIMESTAMP, newComment.timestamp);
        values.put(CommentsTable.COLUMN_TRANSFERRED, newComment.transferred);
        values.put(CommentsTable.COLUMN_SEEN, newComment.seen);
        values.put(CommentsTable.COLUMN_TEXT, newComment.text);
        values.put(CommentsTable.COLUMN_TYPE, newComment.type);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();

        db.updateWithOnConflict(CommentsTable.TABLE_NAME, values, CommentsTable._ID + "=?", new String[]{Long.toString(original.rowId)}, SQLiteDatabase.CONFLICT_ABORT);
        newComment.rowId = original.rowId;
        mediaDb.addMedia(newComment);
        mentionsDb.addMentions(newComment);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    @WorkerThread
    @NonNull List<FutureProofComment> getFutureProofComments() {
        final List<FutureProofComment> comments = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        final HashSet<String> checkedPosts = new HashSet<>();
        final HashMap<String, Post> posts = new HashMap<>();
        try (final Cursor cursor = db.query(CommentsTable.TABLE_NAME,
                new String [] {
                        CommentsTable._ID,
                        CommentsTable.COLUMN_POST_ID,
                        CommentsTable.COLUMN_COMMENT_SENDER_USER_ID,
                        CommentsTable.COLUMN_COMMENT_ID,
                        CommentsTable.COLUMN_PARENT_ID,
                        CommentsTable.COLUMN_TIMESTAMP,
                        CommentsTable.COLUMN_TRANSFERRED,
                        CommentsTable.COLUMN_SEEN,
                        CommentsTable.COLUMN_TEXT,
                        CommentsTable.COLUMN_TYPE},
                CommentsTable.COLUMN_TYPE + "=? AND " + CommentsTable.COLUMN_TIMESTAMP + ">" + getPostExpirationTime(),
                new String[]{Integer.toString(Comment.TYPE_FUTURE_PROOF)}, null, null, null)) {
            while (cursor.moveToNext()) {
                final FutureProofComment comment = new FutureProofComment(
                        cursor.getLong(0),
                        cursor.getString(1),
                        new UserId(cursor.getString(2)),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getLong(5),
                        cursor.getInt(6) == 1,
                        cursor.getInt(7) == 1,
                        cursor.getString(8));
                if (!checkedPosts.contains(comment.postId)) {
                    Post parentPost = getPost(comment.postId);
                    checkedPosts.add(comment.postId);
                    if (parentPost != null) {
                        posts.put(comment.postId, parentPost);
                    }
                }
                mentionsDb.fillMentions(comment);
                futureProofDb.fillFutureProof(comment);
                comment.setParentPost(posts.get(comment.postId));
                comments.add(comment);
            }
        }
        Log.i("ContentDb.getPendingComments: comments.size=" + comments.size());
        return comments;
    }

    @WorkerThread
    boolean cleanup() {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();

        final String sql =
            "SELECT " +
                "m." + MediaTable.COLUMN_FILE + "," +
                "m." + MediaTable.COLUMN_ENC_FILE + " " +
            "FROM " + PostsTable.TABLE_NAME + " " +
            "INNER JOIN (" +
                "SELECT " +
                    MediaTable.COLUMN_PARENT_TABLE + "," +
                    MediaTable.COLUMN_PARENT_ROW_ID + "," +
                    MediaTable.COLUMN_FILE + "," +
                    MediaTable.COLUMN_ENC_FILE + "," +
                    MediaTable.COLUMN_TRANSFERRED + " FROM " + MediaTable.TABLE_NAME + ")" +
                "AS m ON " + PostsTable.TABLE_NAME + "." + PostsTable._ID + "=m." + MediaTable.COLUMN_PARENT_ROW_ID + " AND '" + PostsTable.TABLE_NAME + "'=m." + MediaTable.COLUMN_PARENT_TABLE + " " +
            "WHERE " + PostsTable.COLUMN_TIMESTAMP + "<" + getPostExpirationTime() + " AND " + PostsTable.COLUMN_SENDER_USER_ID + "!= ''";
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
                final File encFile = fileStore.getTmpFile(cursor.getString(1));
                if (encFile != null) {
                    if (encFile.delete()) {
                        deletedFiles++;
                    } else {
                        Log.i("ContentDb.cleanup: failed to delete " + encFile.getAbsolutePath());
                    }
                }
            }
        }
        Log.i("ContentDb.cleanup: " + deletedFiles + " media files deleted");

        String archiveSql = "SELECT " + PostsTable.COLUMN_POST_ID +
                " FROM " + PostsTable.TABLE_NAME +
                " WHERE " + PostsTable.COLUMN_TIMESTAMP + "<" + getPostExpirationTime() + " AND " + PostsTable.COLUMN_SENDER_USER_ID + " = ''  AND " + PostsTable.COLUMN_TYPE + "=" + Post.TYPE_USER;
        int archivedPostsCount = 0;
        try (final Cursor cursor = db.rawQuery(archiveSql, null)) {
            while (cursor.moveToNext()) {
                Post post = getPost(cursor.getString(0));
                addPostToArchive(post);
                archivedPostsCount += db.delete(PostsTable.TABLE_NAME, PostsTable.COLUMN_POST_ID + "=?", new String[]{post.id});
            }
        }
        Log.i("ContentDb.cleanup: " + archivedPostsCount + " posts archived");

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

        return (deletedPostsCount > 0 || deletedCommentsCount > 0 || deletedSeenCount > 0 || archivedPostsCount > 0);
    }

    //Note that this class archives ALL current posts (even those which aren't expired), for testing purposes only from debug menu
    @WorkerThread
    void archivePosts() {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        try (Cursor cursor = db.query(PostsTable.TABLE_NAME, new String[]{PostsTable.COLUMN_POST_ID},
                PostsTable.COLUMN_SENDER_USER_ID + " =? AND " + PostsTable.COLUMN_TYPE + "=" + Post.TYPE_USER,
                new String[]{UserId.ME.rawId()}, null, null, null, null)) {
            while (cursor.moveToNext()) {
                Post post = getPost(cursor.getString(0));
                addPostToArchive(post);
            }
        }
    }

    @WorkerThread
    void deleteArchive() {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.delete(ArchiveTable.TABLE_NAME,null,null);
    }

    private static long getPostExpirationTime() {
        return System.currentTimeMillis() - Constants.POSTS_EXPIRATION;
    }
}

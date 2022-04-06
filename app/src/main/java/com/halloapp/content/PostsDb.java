package com.halloapp.content;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.text.format.DateUtils;
import android.util.Pair;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.halloapp.Constants;
import com.halloapp.FileStore;
import com.halloapp.content.tables.ArchiveTable;
import com.halloapp.content.tables.AudienceTable;
import com.halloapp.content.tables.CommentsTable;
import com.halloapp.content.tables.HistoryRerequestTable;
import com.halloapp.content.tables.HistoryResendPayloadTable;
import com.halloapp.content.tables.MediaTable;
import com.halloapp.content.tables.MentionsTable;
import com.halloapp.content.tables.MessagesTable;
import com.halloapp.content.tables.PostsTable;
import com.halloapp.content.tables.RerequestsTable;
import com.halloapp.content.tables.SeenTable;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.media.MediaUtils;
import com.halloapp.props.ServerProps;
import com.halloapp.proto.clients.CommentContainer;
import com.halloapp.proto.clients.CommentIdContext;
import com.halloapp.proto.clients.ContentDetails;
import com.halloapp.proto.clients.PostContainer;
import com.halloapp.proto.clients.PostIdContext;
import com.halloapp.proto.clients.Video;
import com.halloapp.util.Preconditions;
import com.halloapp.util.StringUtils;
import com.halloapp.util.logs.Log;
import com.halloapp.util.stats.DecryptStats;
import com.halloapp.util.stats.GroupDecryptStats;
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
    private final UrlPreviewsDb urlPreviewsDb;
    private final ContentDbHelper databaseHelper;
    private final FileStore fileStore;
    private final ServerProps serverProps;

    PostsDb(
            MediaDb mediaDb,
            MentionsDb mentionsDb,
            FutureProofDb futureProofDb,
            UrlPreviewsDb urlPreviewsDb,
            ContentDbHelper databaseHelper,
            FileStore fileStore,
            ServerProps serverProps) {
        this.mediaDb = mediaDb;
        this.mentionsDb = mentionsDb;
        this.futureProofDb = futureProofDb;
        this.urlPreviewsDb = urlPreviewsDb;
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
        urlPreviewsDb.addUrlPreview(post);

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
    void subscribeToPost(@NonNull Post post) {
        Log.i("ContentDb.subscribeToPost: postId=" + post.id);
        final ContentValues values = new ContentValues();
        values.put(PostsTable.COLUMN_SUBSCRIBED, true);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        try {
            db.updateWithOnConflict(PostsTable.TABLE_NAME, values,
                    PostsTable.COLUMN_SENDER_USER_ID + "=? AND " + PostsTable.COLUMN_POST_ID + "=?",
                    new String [] {post.senderUserId.rawId(), post.id},
                    SQLiteDatabase.CONFLICT_ABORT);
        } catch (SQLException ex) {
            Log.e("ContentDb.subscribeToPost: failed");
            throw ex;
        }
    }

    @WorkerThread
    void addPost(@NonNull Post post) {
        Log.i("ContentDb.addPost " + post);
        if (post.timestamp < getPostExpirationTime()) {
            throw new SQLiteConstraintException("attempting to add expired post");
        }
        long now = System.currentTimeMillis();
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            Long tombstoneRowId = null;
            String tombstoneSql = "SELECT " + PostsTable._ID + " FROM " + PostsTable.TABLE_NAME + " WHERE " + PostsTable.COLUMN_POST_ID + "=? AND " + PostsTable.COLUMN_TRANSFERRED + "=" + Post.TRANSFERRED_DECRYPT_FAILED;
            try (Cursor cursor = db.rawQuery(tombstoneSql, new String[]{post.id})) {
                if (cursor.moveToNext()) {
                    tombstoneRowId = cursor.getLong(0);
                }
            }

            final ContentValues values = new ContentValues();
            values.put(PostsTable.COLUMN_RESULT_UPDATE_TIME, now);
            values.put(PostsTable.COLUMN_FAILURE_REASON, post.failureReason);
            if (post.getParentGroup() != null) {
                values.put(PostsTable.COLUMN_GROUP_ID, post.getParentGroup().rawId());
            }
            if (post.text != null) {
                values.put(PostsTable.COLUMN_TEXT, post.text);
            }

            // TODO(jack): turning off plaintext sending requires changing much of this portion to wait for successful decrypt
            // i.e. without the plaintext media can't be added, and therefore with a tombstone media row ids can't be loaded
            if (tombstoneRowId != null) {
                db.update(PostsTable.TABLE_NAME, values, PostsTable._ID + "=?", new String[]{tombstoneRowId.toString()});
                post.rowId = tombstoneRowId;
                mediaDb.getMediaRowIds(post);
            } else {
                values.put(PostsTable.COLUMN_CLIENT_VERSION, post.clientVersion);
                values.put(PostsTable.COLUMN_SENDER_VERSION, post.senderVersion);
                values.put(PostsTable.COLUMN_SENDER_PLATFORM, post.senderPlatform);
                values.put(PostsTable.COLUMN_SENDER_USER_ID, post.senderUserId.rawId());
                values.put(PostsTable.COLUMN_POST_ID, post.id);
                values.put(PostsTable.COLUMN_TIMESTAMP, post.timestamp);
                values.put(PostsTable.COLUMN_TRANSFERRED, post.transferred);
                values.put(PostsTable.COLUMN_AUDIENCE_TYPE, post.getAudienceType());
                values.put(PostsTable.COLUMN_SEEN, post.seen);
                values.put(PostsTable.COLUMN_TYPE, post.type);
                values.put(PostsTable.COLUMN_USAGE, post.usage);
                values.put(PostsTable.COLUMN_RECEIVE_TIME, now);
                values.put(PostsTable.COLUMN_PROTO_HASH, post.protoHash);
                values.put(PostsTable.COLUMN_SUBSCRIBED, post.subscribed);
                post.rowId = db.insertWithOnConflict(PostsTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_ABORT);
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
                urlPreviewsDb.addUrlPreview(post);
                if (post instanceof FutureProofPost) {
                    futureProofDb.saveFutureProof((FutureProofPost) post);
                }
            }
            Log.i("ContentDb.addPost got rowid " + post.rowId + " for " + post);

            db.setTransactionSuccessful();
            Log.i("ContentDb.addPost: added " + post);
        } finally {
            db.endTransaction();
        }
    }

    @WorkerThread
    void deleteZeroZonePost(@NonNull Post post) {
        if (post.type != Post.TYPE_ZERO_ZONE) {
            return;
        }
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.delete(PostsTable.TABLE_NAME, PostsTable.COLUMN_POST_ID + "=? AND " + PostsTable.COLUMN_TYPE + "=" + Post.TYPE_ZERO_ZONE, new String[] {post.id});
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
                if (post.getParentGroup() != null) {
                    values.put(PostsTable.COLUMN_GROUP_ID, post.getParentGroup().rawId());
                }
                post.rowId = db.insertWithOnConflict(PostsTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_ABORT);
            } else {
                List<String> fileNames = new ArrayList<>();
                String sql = "SELECT " + MediaTable.COLUMN_FILE
                        + " FROM " + MediaTable.TABLE_NAME
                        + " WHERE " + MediaTable.COLUMN_PARENT_TABLE + "='" + CommentsTable.TABLE_NAME + "'"
                        + " AND " + MediaTable.COLUMN_PARENT_ROW_ID + " IN (SELECT " + CommentsTable._ID + " FROM " + CommentsTable.TABLE_NAME + " WHERE " + CommentsTable.COLUMN_POST_ID + "=?)";
                try (Cursor cursor = db.rawQuery(sql, new String[] {post.id})) {
                    while (cursor.moveToNext()) {
                        String name = cursor.getString(0);
                        if (name != null) {
                            fileNames.add(name);
                        }
                    }
                }
                db.delete(CommentsTable.TABLE_NAME,
                        CommentsTable.COLUMN_POST_ID + "=?",
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
                for (String name : fileNames) {
                    File file = fileStore.getMediaFile(name);
                    if (!file.delete()) {
                        Log.e("ContentDb.retractPost: failed to delete " + file.getAbsolutePath());
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

    @WorkerThread
    void setPostUpdated(@NonNull String postId, long updateTime) {
        Log.i("ContentDb.setPostUpdated: " + " postId=" + postId + " time=" + updateTime);
        final ContentValues values = new ContentValues();
        values.put(PostsTable.COLUMN_LAST_UPDATE, updateTime);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        try {
            db.updateWithOnConflict(PostsTable.TABLE_NAME, values, PostsTable.COLUMN_POST_ID + "=?",
                    new String [] {postId},
                    SQLiteDatabase.CONFLICT_ABORT);
        } catch (SQLException ex) {
            Log.e("ContentDb.setPostUpdated: failed");
            throw ex;
        }
    }

    @WorkerThread
    void setGroupSeen(@NonNull GroupId groupId) {
        Log.i("ContentDb.setGroupSeen: groupId=" + groupId);
        final ContentValues values = new ContentValues();
        values.put(PostsTable.COLUMN_SEEN, Post.SEEN_NO_HIDDEN);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        try {
            db.updateWithOnConflict(PostsTable.TABLE_NAME, values,
                    PostsTable.COLUMN_GROUP_ID + "=? AND " + PostsTable.COLUMN_SEEN + "=? AND " + PostsTable.COLUMN_TIMESTAMP + "<?",
                    new String [] {groupId.rawId(), Integer.toString(Post.SEEN_NO), Long.toString(System.currentTimeMillis())},
                    SQLiteDatabase.CONFLICT_ABORT);
        } catch (SQLException ex) {
            Log.e("ContentDb.setIncomingPostSeen: failed");
            throw ex;
        }
    }


    @WorkerThread
    void setZeroZoneGroupPostSeen(@NonNull String postId) {
        Log.i("ContentDb.setZeroZoneGroupPostSeen: postId=" + postId);
        final ContentValues values = new ContentValues();
        values.put(PostsTable.COLUMN_SEEN, Post.SEEN_YES);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        try {
            db.updateWithOnConflict(PostsTable.TABLE_NAME, values,
                    PostsTable.COLUMN_SENDER_USER_ID + "=? AND " + PostsTable.COLUMN_POST_ID + "=?",
                    new String [] {"", postId},
                    SQLiteDatabase.CONFLICT_ABORT);
        } catch (SQLException ex) {
            Log.e("ContentDb.setZeroZoneGroupPostSeen: failed");
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
    void setPostProtoHash(@NonNull UserId senderUserId, @NonNull String postId, @Nullable byte[] protoHash) {
        Log.i("ContentDb.setPostProtoHash: senderUserId=" + senderUserId + " postId=" + postId + " protoHash=" + (protoHash == null ? null : StringUtils.bytesToHexString(protoHash)));
        final ContentValues values = new ContentValues();
        values.put(PostsTable.COLUMN_PROTO_HASH, protoHash);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        try {
            db.updateWithOnConflict(PostsTable.TABLE_NAME, values,
                    PostsTable.COLUMN_SENDER_USER_ID + "=? AND " + PostsTable.COLUMN_POST_ID + "=? AND " + PostsTable.COLUMN_PROTO_HASH + " IS NULL",
                    new String [] {senderUserId.rawId(), postId},
                    SQLiteDatabase.CONFLICT_ABORT);
        } catch (SQLException ex) {
            Log.e("ContentDb.setPostProtoHash: failed");
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
    public List<ContentDetails> getHistoryResendContent(@NonNull GroupId groupId) {
        List<ContentDetails> ret = new ArrayList<>();
        final String sql =
                "SELECT rowid, id, hash, post_id, parent_id, gid FROM "
                        + "(SELECT _id as rowid, post_id as id, proto_hash as hash, NULL as post_id, NULL as parent_id, group_id as gid from posts "
                        + " UNION SELECT " + CommentsTable.TABLE_NAME + "._id as rowid, " + CommentsTable.TABLE_NAME + ".comment_id as id, " + CommentsTable.TABLE_NAME + ".proto_hash as hash, " + CommentsTable.TABLE_NAME + ".post_id as post_id, parent_id as parent_id, group_id as gid from comments LEFT JOIN posts ON comments.post_id = posts.post_id) "
                        + "WHERE hash IS NOT NULL AND gid=? ORDER BY rowid DESC LIMIT 200";
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.rawQuery(sql, new String[]{groupId.rawId()})) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(1);
                String postId = cursor.getString(3);
                if (postId == null) { // is a post
                    ret.add(ContentDetails.newBuilder()
                            .setPostIdContext(PostIdContext.newBuilder().setFeedPostId(id))
                            .setContentHash(ByteString.copyFrom(cursor.getBlob(2)))
                            .build());
                } else { // is a comment
                    String parentId = cursor.getString(4);
                    CommentIdContext.Builder builder = CommentIdContext.newBuilder()
                            .setCommentId(id)
                            .setFeedPostId(postId);
                    if (parentId != null) {
                        builder.setParentCommentId(parentId);
                    }
                    ret.add(ContentDetails.newBuilder()
                            .setCommentIdContext(builder)
                            .setContentHash(ByteString.copyFrom(cursor.getBlob(2)))
                            .build());
                }
            }
        }
        return ret;
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

        long now = System.currentTimeMillis();
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            Long tombstoneRowId = null;
            String tombstoneSql = "SELECT " + CommentsTable._ID + " FROM " + CommentsTable.TABLE_NAME + " WHERE " + CommentsTable.COLUMN_COMMENT_ID + "=? AND " + CommentsTable.COLUMN_TRANSFERRED + "=" + Comment.TRANSFERRED_DECRYPT_FAILED;
            try (Cursor cursor = db.rawQuery(tombstoneSql, new String[]{comment.id})) {
                if (cursor.moveToNext()) {
                    tombstoneRowId = cursor.getLong(0);
                }
            }

            final ContentValues values = new ContentValues();
            values.put(CommentsTable.COLUMN_RESULT_UPDATE_TIME, now);
            values.put(CommentsTable.COLUMN_FAILURE_REASON, comment.failureReason);

            if (tombstoneRowId != null) {
                db.update(CommentsTable.TABLE_NAME, values, CommentsTable._ID + "=?", new String[]{tombstoneRowId.toString()});
                comment.rowId = tombstoneRowId;
            } else {
                values.put(CommentsTable.COLUMN_CLIENT_VERSION, comment.clientVersion);
                values.put(CommentsTable.COLUMN_SENDER_VERSION, comment.senderVersion);
                values.put(CommentsTable.COLUMN_SENDER_PLATFORM, comment.senderPlatform);
                values.put(CommentsTable.COLUMN_POST_ID, comment.postId);
                values.put(CommentsTable.COLUMN_COMMENT_SENDER_USER_ID, comment.senderUserId.rawId());
                values.put(CommentsTable.COLUMN_COMMENT_ID, comment.id);
                values.put(CommentsTable.COLUMN_PARENT_ID, comment.parentCommentId);
                values.put(CommentsTable.COLUMN_TIMESTAMP, comment.timestamp);
                values.put(CommentsTable.COLUMN_TRANSFERRED, comment.transferred);
                values.put(CommentsTable.COLUMN_SEEN, comment.seen);
                values.put(CommentsTable.COLUMN_TEXT, comment.text);
                values.put(CommentsTable.COLUMN_TYPE, comment.type);
                values.put(CommentsTable.COLUMN_RECEIVE_TIME, now);
                values.put(CommentsTable.COLUMN_PROTO_HASH, comment.protoHash);
                values.put(CommentsTable.COLUMN_SHOULD_NOTIFY, comment.shouldNotify);
                comment.rowId = db.insertWithOnConflict(CommentsTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_ABORT);

                mediaDb.addMedia(comment);
                mentionsDb.addMentions(comment);
                urlPreviewsDb.addUrlPreview(comment);
                setPostUpdated(comment.postId, comment.timestamp);
                if (comment instanceof FutureProofComment) {
                    futureProofDb.saveFutureProof((FutureProofComment) comment);
                }
            }

            db.setTransactionSuccessful();
            Log.i("ContentDb.addComment: added " + comment);
        } finally {
            db.endTransaction();
        }
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

    @WorkerThread
    void setCommentProtoHash(@NonNull String postId, @NonNull UserId commentSenderUserId, @NonNull String commentId, @Nullable byte[] protoHash) {
        Log.i("ContentDb.setCommentProtoHash: senderUserId=" + commentSenderUserId + " commentId=" + commentId + " protoHash=" + (protoHash == null ? null : StringUtils.bytesToHexString(protoHash)));
        final ContentValues values = new ContentValues();
        values.put(CommentsTable.COLUMN_PROTO_HASH, protoHash);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        try {
            db.updateWithOnConflict(CommentsTable.TABLE_NAME, values,
                            CommentsTable.COLUMN_POST_ID + "=? AND " +
                            CommentsTable.COLUMN_COMMENT_SENDER_USER_ID + "=? AND " +
                            CommentsTable.COLUMN_COMMENT_ID + "=? AND " +
                            CommentsTable.COLUMN_PROTO_HASH + " IS NULL",
                    new String [] {postId, commentSenderUserId.rawId(), commentId},
                    SQLiteDatabase.CONFLICT_ABORT);
        } catch (SQLException ex) {
            Log.e("ContentDb.setCommentProtoHash: failed");
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
    boolean setCommentPlayed(@NonNull String postId, @NonNull String commentId, boolean played) {
        Log.i("ContentDb.setCommentPlayed: commentId=" + commentId);
        final ContentValues values = new ContentValues();
        values.put(CommentsTable.COLUMN_PLAYED, played);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        try {
            final int updatedCount = db.updateWithOnConflict(CommentsTable.TABLE_NAME, values,
                    CommentsTable.COLUMN_POST_ID + "=? AND " +
                            CommentsTable.COLUMN_COMMENT_ID + "=? AND " +
                            CommentsTable.COLUMN_PLAYED + "=" + (played ? 0 : 1),
                    new String [] {postId, commentId},
                    SQLiteDatabase.CONFLICT_ABORT);
            return updatedCount > 0;
        } catch (SQLException ex) {
            Log.e("ContentDb.setCommentPlayed: failed");
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
                + " AND " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TYPE + "!=" + Post.TYPE_RETRACTED
                + " AND (" + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_SENDER_USER_ID + "!=?"
                + " OR " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TYPE + "=" + Post.TYPE_ZERO_ZONE + ")"
                + " AND " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TIMESTAMP + " > " + getPostExpirationTime();
        try (final Cursor cursor = db.rawQuery(query, new String[]{UserId.ME.rawId()})) {
            return cursor.getCount();
        }
    }

    @WorkerThread
    public List<GroupDecryptStats> getGroupPostDecryptStats(long lastRowId) {
        List<GroupDecryptStats> ret = new ArrayList<>();
        final String sql =
                "SELECT " + PostsTable.TABLE_NAME + "." + PostsTable._ID + ","
                        + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_POST_ID + ","
                        + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_GROUP_ID + ","
                        + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_REREQUEST_COUNT + ","
                        + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_FAILURE_REASON + ","
                        + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_CLIENT_VERSION + ","
                        + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_SENDER_VERSION + ","
                        + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_SENDER_PLATFORM + ","
                        + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_RECEIVE_TIME + ","
                        + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_RESULT_UPDATE_TIME
                        + " FROM " + PostsTable.TABLE_NAME
                        + " WHERE " + PostsTable.TABLE_NAME + "." + PostsTable._ID + " > ? AND " + PostsTable.COLUMN_GROUP_ID + " IS NOT NULL";

        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.rawQuery(sql, new String[]{Long.toString(lastRowId)})) {
            while (cursor.moveToNext()) {
                ret.add(new GroupDecryptStats(
                        cursor.getLong(0),
                        cursor.getString(1),
                        new GroupId(cursor.getString(2)),
                        false,
                        cursor.getInt(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getString(7),
                        cursor.getLong(8),
                        cursor.getLong(9)
                ));
            }
        }
        return ret;
    }

    @WorkerThread
    public GroupDecryptStats getGroupPostDecryptStats(String contentId) {
        final String sql =
                "SELECT " + PostsTable.TABLE_NAME + "." + PostsTable._ID + ","
                        + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_POST_ID + ","
                        + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_GROUP_ID + ","
                        + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_REREQUEST_COUNT + ","
                        + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_FAILURE_REASON + ","
                        + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_CLIENT_VERSION + ","
                        + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_SENDER_VERSION + ","
                        + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_SENDER_PLATFORM + ","
                        + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_RECEIVE_TIME + ","
                        + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_RESULT_UPDATE_TIME
                        + " FROM " + PostsTable.TABLE_NAME
                        + " WHERE " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_POST_ID + " = ? AND " + PostsTable.COLUMN_GROUP_ID + " IS NOT NULL";

        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.rawQuery(sql, new String[]{contentId})) {
            if (cursor.moveToNext()) {
                return new GroupDecryptStats(
                        cursor.getLong(0),
                        cursor.getString(1),
                        new GroupId(cursor.getString(2)),
                        false,
                        cursor.getInt(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getString(7),
                        cursor.getLong(8),
                        cursor.getLong(9)
                );
            }
        }
        return null;
    }

    @WorkerThread
    public List<GroupDecryptStats> getGroupCommentDecryptStats(long lastRowId) {
        List<GroupDecryptStats> ret = new ArrayList<>();
        final String sql =
                "SELECT " + CommentsTable.TABLE_NAME + "." + CommentsTable._ID + ","
                        + CommentsTable.TABLE_NAME + "." + CommentsTable.COLUMN_COMMENT_ID + ","
                        + "p." + PostsTable.COLUMN_GROUP_ID + ","
                        + CommentsTable.TABLE_NAME + "." + CommentsTable.COLUMN_REREQUEST_COUNT + ","
                        + CommentsTable.TABLE_NAME + "." + CommentsTable.COLUMN_FAILURE_REASON + ","
                        + CommentsTable.TABLE_NAME + "." + CommentsTable.COLUMN_CLIENT_VERSION + ","
                        + CommentsTable.TABLE_NAME + "." + CommentsTable.COLUMN_SENDER_VERSION + ","
                        + CommentsTable.TABLE_NAME + "." + CommentsTable.COLUMN_SENDER_PLATFORM + ","
                        + CommentsTable.TABLE_NAME + "." + CommentsTable.COLUMN_RECEIVE_TIME + ","
                        + CommentsTable.TABLE_NAME + "." + CommentsTable.COLUMN_RESULT_UPDATE_TIME
                        + " FROM " + CommentsTable.TABLE_NAME
                        + " LEFT JOIN (" +
                        "SELECT " +
                        PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_GROUP_ID + "," +
                        PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_POST_ID +
                        " FROM " + PostsTable.TABLE_NAME + ") " +
                        "AS p ON " + CommentsTable.TABLE_NAME + "." + CommentsTable.COLUMN_POST_ID + "=p." + PostsTable.COLUMN_POST_ID
                        + " WHERE " + CommentsTable.TABLE_NAME + "." + CommentsTable._ID + " > ? AND p." + PostsTable.COLUMN_GROUP_ID + " IS NOT NULL";

        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.rawQuery(sql, new String[]{Long.toString(lastRowId)})) {
            while (cursor.moveToNext()) {
                ret.add(new GroupDecryptStats(
                        cursor.getLong(0),
                        cursor.getString(1),
                        new GroupId(cursor.getString(2)),
                        true,
                        cursor.getInt(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getString(7),
                        cursor.getLong(8),
                        cursor.getLong(9)
                ));
            }
        }
        return ret;
    }

    @WorkerThread
    public GroupDecryptStats getGroupCommentDecryptStats(String commentId) {
        final String sql =
                "SELECT " + CommentsTable.TABLE_NAME + "." + CommentsTable._ID + ","
                        + CommentsTable.TABLE_NAME + "." + CommentsTable.COLUMN_COMMENT_ID + ","
                        + "p." + PostsTable.COLUMN_GROUP_ID + ","
                        + CommentsTable.TABLE_NAME + "." + CommentsTable.COLUMN_REREQUEST_COUNT + ","
                        + CommentsTable.TABLE_NAME + "." + CommentsTable.COLUMN_FAILURE_REASON + ","
                        + CommentsTable.TABLE_NAME + "." + CommentsTable.COLUMN_CLIENT_VERSION + ","
                        + CommentsTable.TABLE_NAME + "." + CommentsTable.COLUMN_SENDER_VERSION + ","
                        + CommentsTable.TABLE_NAME + "." + CommentsTable.COLUMN_SENDER_PLATFORM + ","
                        + CommentsTable.TABLE_NAME + "." + CommentsTable.COLUMN_RECEIVE_TIME + ","
                        + CommentsTable.TABLE_NAME + "." + CommentsTable.COLUMN_RESULT_UPDATE_TIME
                        + " FROM " + CommentsTable.TABLE_NAME
                        + " LEFT JOIN (" +
                        "SELECT " +
                        PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_GROUP_ID + "," +
                        PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_POST_ID +
                        " FROM " + PostsTable.TABLE_NAME + ") " +
                        "AS p ON " + CommentsTable.TABLE_NAME + "." + CommentsTable.COLUMN_POST_ID + "=p." + PostsTable.COLUMN_POST_ID
                        + " WHERE " + CommentsTable.TABLE_NAME + "." + CommentsTable.COLUMN_COMMENT_ID + " = ? AND p." + PostsTable.COLUMN_GROUP_ID + " IS NOT NULL";

        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.rawQuery(sql, new String[]{commentId})) {
            if (cursor.moveToNext()) {
                return new GroupDecryptStats(
                        cursor.getLong(0),
                        cursor.getString(1),
                        new GroupId(cursor.getString(2)),
                        true,
                        cursor.getInt(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getString(7),
                        cursor.getLong(8),
                        cursor.getLong(9)
                );
            }
        }
        return null;
    }

    @WorkerThread
    Pair<String, String> getExternalShareInfo(@NonNull String postId) {
        final String sql = "SELECT " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_EXTERNAL_SHARE_ID + "," + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_EXTERNAL_SHARE_KEY
                + " FROM " + PostsTable.TABLE_NAME + " WHERE " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_POST_ID + "=? AND " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_SENDER_USER_ID + "=?";

        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.rawQuery(sql, new String[]{postId, UserId.ME.rawId()})) {
            if (cursor.moveToNext()) {
                return new Pair<>(cursor.getString(0), cursor.getString(1));
            }
        }
        return null;
    }

    @WorkerThread
    void setExternalShareInfo(@NonNull String postId, @NonNull String shareId, @NonNull String shareKey) {
        final ContentValues values = new ContentValues();
        values.put(PostsTable.COLUMN_EXTERNAL_SHARE_ID, shareId);
        values.put(PostsTable.COLUMN_EXTERNAL_SHARE_KEY, shareKey);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        try {
            db.updateWithOnConflict(PostsTable.TABLE_NAME, values,
                    PostsTable.COLUMN_SENDER_USER_ID + "=? AND " + PostsTable.COLUMN_POST_ID + "=?",
                    new String [] {UserId.ME.rawId(), postId},
                    SQLiteDatabase.CONFLICT_ABORT);
        } catch (SQLException ex) {
            Log.e("PostsDb.setExternalShareInfo: failed");
            throw ex;
        }
    }

    @WorkerThread
    @NonNull List<Post> getPosts(@Nullable Long timestamp, @Nullable Integer count, boolean after, @Nullable UserId senderUserId, @Nullable GroupId groupId, boolean unseenOnly, boolean orderByLastUpdated) {
        final List<Post> posts = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String where;
        String[] selectionArgs = null;
        String orderBy;
        if (timestamp == null) {
            where = PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TIMESTAMP + ">" + getPostExpirationTime();
        } else {
            if (orderByLastUpdated) {
                String lastUpdate = "COALESCE(" + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_LAST_UPDATE + "," + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TIMESTAMP + ")";
                if (after) {
                    where = PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TIMESTAMP + ">" + getPostExpirationTime() + " AND " + lastUpdate + "<" + timestamp;
                } else {
                    where = lastUpdate + ">" + Math.max(getPostExpirationTime(), timestamp);
                }
            } else {
                if (after) {
                    where = PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TIMESTAMP + ">" + getPostExpirationTime() + " AND " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TIMESTAMP + "<" + timestamp;
                } else {
                    where = PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TIMESTAMP + ">" + Math.max(getPostExpirationTime(), timestamp);
                }
            }
        }
        List<String> args = new ArrayList<>();
        if (senderUserId != null) {
            where += " AND " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_SENDER_USER_ID + "=?";
            args.add(senderUserId.rawId());
            if (senderUserId.isMe()) {
                where += " AND " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TYPE + "!=" + Post.TYPE_ZERO_ZONE;
            }
        }
        if (unseenOnly) {
            where += " AND " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_SEEN + "=0"
                    + " AND " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TYPE + "!=" + Post.TYPE_RETRACTED
                    + " AND (" + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_SENDER_USER_ID + " != ''" + " OR " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TYPE + "=" + Post.TYPE_ZERO_ZONE + ")";
        }
        if (groupId != null) {
            where += " AND " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_GROUP_ID + "=?";
            args.add(groupId.rawId());
        }
        if (groupId == null || orderByLastUpdated) {
            where += " AND ("
                    + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TYPE + "=" + Post.TYPE_USER + " OR "
                    + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TYPE + "=" + Post.TYPE_FUTURE_PROOF + " OR "
                    + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TYPE + "=" + Post.TYPE_VOICE_NOTE + " OR "
                    + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_GROUP_ID + " IS NULL)";
        }
        if (orderByLastUpdated) {
            orderBy = "COALESCE(" + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_LAST_UPDATE + "," + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TIMESTAMP + ")" + (after ? " DESC " : " ASC ");
        } else {
            orderBy = PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TIMESTAMP + (after ? " DESC " : " ASC ");
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
                PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_LAST_UPDATE + "," +
                "m." + MediaTable._ID + "," +
                "m." + MediaTable.COLUMN_TYPE + "," +
                "m." + MediaTable.COLUMN_URL + "," +
                "m." + MediaTable.COLUMN_FILE + "," +
                "m." + MediaTable.COLUMN_ENC_FILE + "," +
                "m." + MediaTable.COLUMN_WIDTH + "," +
                "m." + MediaTable.COLUMN_HEIGHT + "," +
                "m." + MediaTable.COLUMN_TRANSFERRED + ", " +
                "m." + MediaTable.COLUMN_BLOB_VERSION + "," +
                "m." + MediaTable.COLUMN_CHUNK_SIZE + "," +
                "m." + MediaTable.COLUMN_BLOB_SIZE + "," +
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
                    MediaTable.COLUMN_TRANSFERRED + "," +
                    MediaTable.COLUMN_BLOB_VERSION + "," +
                    MediaTable.COLUMN_CHUNK_SIZE + "," +
                    MediaTable.COLUMN_BLOB_SIZE + " FROM " + MediaTable.TABLE_NAME + " ORDER BY " + MediaTable._ID + " ASC) " +
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
            "ORDER BY " + orderBy + (count == null ? "" : ("LIMIT " + count));;

        try (final Cursor cursor = db.rawQuery(sql, selectionArgs)) {

            long lastRowId = -1;
            Post post = null;
            while (cursor.moveToNext()) {
                long rowId = cursor.getLong(0);
                if (lastRowId != rowId) {
                    lastRowId = rowId;
                    if (post != null) {
                        mentionsDb.fillMentions(post);
                        urlPreviewsDb.fillUrlPreview(post);
                        posts.add(post);
                    }
                    post = Post.build(
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
                    post.commentCount = cursor.getInt(23);
                    post.unseenCommentCount = post.commentCount - cursor.getInt(24);
                    post.updateTime = Math.max(post.timestamp, cursor.getLong(11));
                    GroupId parentGroupId = GroupId.fromNullable(cursor.getString(8));
                    if (parentGroupId != null) {
                        post.setParentGroup(parentGroupId);
                    }
                    final String firstCommentId = cursor.getString(26);
                    if (firstCommentId != null) {
                        post.firstComment = new Comment(cursor.getLong(25),
                                post.id,
                                new UserId(cursor.getString(27)),
                                firstCommentId,
                                null,
                                cursor.getLong(29),
                                Comment.TRANSFERRED_YES,
                                true,
                                cursor.getString(28));
                        post.firstComment.setParentPost(post);
                        mentionsDb.fillMentions(post.firstComment);
                    }
                    post.seenByCount = cursor.getInt(30);
                }
                if (!cursor.isNull(12)) {
                    Media media = new Media(
                            cursor.getLong(12),
                            cursor.getInt(13),
                            cursor.getString(14),
                            fileStore.getMediaFile(cursor.getString(15)),
                            null,
                            null,
                            null,
                            cursor.getInt(17),
                            cursor.getInt(18),
                            cursor.getInt(19),
                            cursor.getInt(20),
                            cursor.getInt(21),
                            cursor.getLong(22));
                    media.encFile = fileStore.getTmpFile(cursor.getString(16));
                    Preconditions.checkNotNull(post).media.add(media);
                }
            }
            if (post != null && (count == null || cursor.getCount() < count)) {
                mentionsDb.fillMentions(post);
                urlPreviewsDb.fillUrlPreview(post);
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
    boolean hasZeroZoneHomePost() {
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
                        PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_REREQUEST_COUNT + " " +
                        "FROM " + PostsTable.TABLE_NAME + " " +
                        "WHERE " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TYPE + "=?";
        try (final Cursor cursor = db.rawQuery(sql, new String [] {Integer.toString(Post.TYPE_ZERO_ZONE)})) {
            while (cursor.moveToNext()) {
                return true;
            }
        }
        return false;
    }

    @WorkerThread
    boolean hasZeroZoneGroupPost(@NonNull GroupId groupId) {
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
                        PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_REREQUEST_COUNT + " " +
                        "FROM " + PostsTable.TABLE_NAME + " " +
                        "WHERE " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_TYPE + "=? AND " + PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_GROUP_ID + "=?";
        try (final Cursor cursor = db.rawQuery(sql, new String [] {Integer.toString(Post.TYPE_ZERO_ZONE), groupId.rawId()})) {
            while (cursor.moveToNext()) {
                return true;
            }
        }
        return false;
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
                    PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_REREQUEST_COUNT + "," +
                    PostsTable.TABLE_NAME + "." + PostsTable.COLUMN_SUBSCRIBED + "," +
                    "m." + MediaTable._ID + "," +
                    "m." + MediaTable.COLUMN_TYPE + "," +
                    "m." + MediaTable.COLUMN_URL + "," +
                    "m." + MediaTable.COLUMN_FILE + "," +
                    "m." + MediaTable.COLUMN_ENC_FILE + "," +
                    "m." + MediaTable.COLUMN_ENC_KEY + "," +
                    "m." + MediaTable.COLUMN_WIDTH + "," +
                    "m." + MediaTable.COLUMN_HEIGHT + "," +
                    "m." + MediaTable.COLUMN_TRANSFERRED + "," +
                    "m." + MediaTable.COLUMN_SHA256_HASH + "," +
                    "m." + MediaTable.COLUMN_DEC_SHA256_HASH + "," +
                    "m." + MediaTable.COLUMN_BLOB_VERSION + "," +
                    "m." + MediaTable.COLUMN_CHUNK_SIZE + "," +
                    "m." + MediaTable.COLUMN_BLOB_SIZE + "," +
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
                        MediaTable.COLUMN_ENC_KEY + "," +
                        MediaTable.COLUMN_WIDTH + "," +
                        MediaTable.COLUMN_HEIGHT + "," +
                        MediaTable.COLUMN_TRANSFERRED + "," +
                        MediaTable.COLUMN_SHA256_HASH + "," +
                        MediaTable.COLUMN_DEC_SHA256_HASH + "," +
                        MediaTable.COLUMN_BLOB_VERSION + "," +
                        MediaTable.COLUMN_CHUNK_SIZE + "," +
                        MediaTable.COLUMN_BLOB_SIZE + " FROM " + MediaTable.TABLE_NAME + " ORDER BY " + MediaTable._ID + " ASC) " +
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
                    post = Post.build(
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
                    post.seenByCount = cursor.getInt(26);
                    post.rerequestCount = cursor.getInt(10);
                    post.subscribed = cursor.getInt(11) == 1;
                }
                if (!cursor.isNull(12)) {
                    Media media = new Media(
                            cursor.getLong(12),
                            cursor.getInt(13),
                            cursor.getString(14),
                            fileStore.getMediaFile(cursor.getString(15)),
                            cursor.getBlob(17),
                            cursor.getBlob(21),
                            cursor.getBlob(22),
                            cursor.getInt(18),
                            cursor.getInt(19),
                            cursor.getInt(20),
                            cursor.getInt(23),
                            cursor.getInt(24),
                            cursor.getLong(25));
                    media.encFile = fileStore.getTmpFile(cursor.getString(16));
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
                        "m." + MediaTable.COLUMN_TRANSFERRED+ "," +
                        "m." + MediaTable.COLUMN_BLOB_VERSION+ "," +
                        "m." + MediaTable.COLUMN_CHUNK_SIZE+ "," +
                        "m." + MediaTable.COLUMN_BLOB_SIZE + " " +
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
                        MediaTable.COLUMN_TRANSFERRED + "," +
                        MediaTable.COLUMN_BLOB_VERSION + "," +
                        MediaTable.COLUMN_CHUNK_SIZE + "," +
                        MediaTable.COLUMN_BLOB_SIZE + " FROM " + MediaTable.TABLE_NAME + " ORDER BY " + MediaTable._ID + " ASC) " +
                        "AS m ON " + ArchiveTable.TABLE_NAME + "." + ArchiveTable._ID + "=m." + MediaTable.COLUMN_PARENT_ROW_ID + " AND '" + ArchiveTable.TABLE_NAME + "'=m." + MediaTable.COLUMN_PARENT_TABLE + " " +
                        "WHERE " + ArchiveTable.TABLE_NAME + "." + ArchiveTable.COLUMN_POST_ID + "=?";

        try (final Cursor cursor = db.rawQuery(sql, new String[] {postId})) {

            while (cursor.moveToNext()) {
                long rowId = cursor.getLong(0);
                if (post == null) {
                    post = Post.build(
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
                            cursor.getInt(14),
                            cursor.getInt(15),
                            cursor.getLong(16));
                    media.encFile = fileStore.getTmpFile(cursor.getString(10));
                    Preconditions.checkNotNull(post).media.add(media);
                }
            }
        }
        Log.i("ContentDb.getArchivePost: post=" + post);
        return post;
    }

    @WorkerThread
    int getPostRerequestCount(@NonNull GroupId groupId, @NonNull UserId senderUserId, @NonNull String postId) {
        Log.i("PostsDb.getPostRerequestCount: groupId=" + groupId + "senderUserId=" + senderUserId + " postId=" + postId);

        String sql = "SELECT " + PostsTable.COLUMN_REREQUEST_COUNT + " "
                + "FROM " + PostsTable.TABLE_NAME + " "
                + "WHERE " + PostsTable.COLUMN_POST_ID + "=? AND " + PostsTable.COLUMN_SENDER_USER_ID + "=? AND " + PostsTable.COLUMN_GROUP_ID + "=?";

        int count = 0;
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(sql, new String[] {postId, senderUserId.rawId(), groupId.rawId()})) {
            if (cursor.moveToNext()) {
                return cursor.getInt(0);
            }
        } catch (SQLException ex) {
            Log.e("PostsDb.getPostRerequestCount: failed");
            throw ex;
        }
        return count;
    }

    @WorkerThread
    void setPostRerequestCount(@NonNull GroupId groupId, @NonNull UserId senderUserId, @NonNull String postId, int count) {
        Log.i("PostsDb.setPostRerequestCount: groupId=" + groupId + "senderUserId=" + senderUserId + " postId=" + postId + " count=" + count);
        final ContentValues values = new ContentValues();
        values.put(PostsTable.COLUMN_REREQUEST_COUNT, count);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        try {
            db.updateWithOnConflict(PostsTable.TABLE_NAME, values,
                    PostsTable.COLUMN_GROUP_ID + "=? AND " + PostsTable.COLUMN_SENDER_USER_ID + "=? AND " + PostsTable.COLUMN_POST_ID + "=?",
                    new String [] {groupId.rawId(), senderUserId.rawId(), postId},
                    SQLiteDatabase.CONFLICT_ABORT);
        } catch (SQLException ex) {
            Log.e("PostsDb.setPostRerequestCount: failed");
            throw ex;
        }
    }

    @WorkerThread
    int getCommentRerequestCount(@NonNull GroupId groupId, @NonNull UserId senderUserId, @NonNull String commentId) {
        Log.i("PostsDb.getCommentRerequestCount: groupId=" + groupId + "senderUserId=" + senderUserId + " commentId=" + commentId);

        String sql = "SELECT " + CommentsTable.COLUMN_REREQUEST_COUNT + " "
                + "FROM " + CommentsTable.TABLE_NAME + " "
                + "WHERE " + CommentsTable.COLUMN_COMMENT_ID + "=? AND " + CommentsTable.COLUMN_COMMENT_SENDER_USER_ID + "=?";

        int count = 0;
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(sql, new String[] {commentId, senderUserId.rawId()})) {
            if (cursor.moveToNext()) {
                return cursor.getInt(0);
            }
        } catch (SQLException ex) {
            Log.e("PostsDb.getCommentRerequestCount: failed");
            throw ex;
        }
        return count;
    }

    @WorkerThread
    void setCommentRerequestCount(@NonNull GroupId groupId, @NonNull UserId senderUserId, @NonNull String commentId, int count) {
        Log.i("PostsDb.setCommentRerequestCount: groupId=" + groupId + "senderUserId=" + senderUserId + " commentId=" + commentId + " count=" + count);
        final ContentValues values = new ContentValues();
        values.put(CommentsTable.COLUMN_REREQUEST_COUNT, count);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        try {
            db.updateWithOnConflict(CommentsTable.TABLE_NAME, values,
                    CommentsTable.COLUMN_COMMENT_SENDER_USER_ID + "=? AND " + CommentsTable.COLUMN_COMMENT_ID + "=?",
                    new String [] {senderUserId.rawId(), commentId},
                    SQLiteDatabase.CONFLICT_ABORT);
        } catch (SQLException ex) {
            Log.e("PostsDb.setCommentRerequestCount: failed");
            throw ex;
        }
    }

    @WorkerThread
    int getHistoryResendRerequestCount(@NonNull GroupId groupId, @NonNull UserId senderUserId, @NonNull String historyId) {
        Log.i("PostsDb.getHistoryResendRerequestCount: groupId=" + groupId + "senderUserId=" + senderUserId + " historyId=" + historyId);

        String sql = "SELECT " + HistoryRerequestTable.COLUMN_REREQUEST_COUNT + " "
                + "FROM " + HistoryRerequestTable.TABLE_NAME + " "
                + "WHERE " + HistoryRerequestTable.COLUMN_HISTORY_RESEND_ID + "=? AND " + HistoryRerequestTable.COLUMN_SENDER_USER_ID + "=?";

        int count = 0;
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(sql, new String[] {historyId, senderUserId.rawId()})) {
            if (cursor.moveToNext()) {
                return cursor.getInt(0);
            }
        } catch (SQLException ex) {
            Log.e("PostsDb.getHistoryResendRerequestCount: failed");
            throw ex;
        }
        return count;
    }

    @WorkerThread
    void setHistoryResendRerequestCount(@NonNull GroupId groupId, @NonNull UserId senderUserId, @NonNull String historyId, int count) {
        Log.i("PostsDb.setHistoryResendRerequestCount: groupId=" + groupId + "senderUserId=" + senderUserId + " historyId=" + historyId + " count=" + count);

        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        Long existingRowId = null;
        String sql = "SELECT " + HistoryRerequestTable._ID + " FROM " + HistoryRerequestTable.TABLE_NAME
                + " WHERE " + HistoryRerequestTable.COLUMN_HISTORY_RESEND_ID + "=?"
                + " AND " + HistoryRerequestTable.COLUMN_SENDER_USER_ID + "=?";
        try (Cursor cursor = db.rawQuery(sql, new String[]{historyId, senderUserId.rawId()})) {
            if (cursor.moveToNext()) {
                existingRowId = cursor.getLong(0);
            }
        }

        final ContentValues values = new ContentValues();
        values.put(HistoryRerequestTable.COLUMN_REREQUEST_COUNT, count);
        try {
            if (existingRowId == null) {
                values.put(HistoryRerequestTable.COLUMN_HISTORY_RESEND_ID, historyId);
                values.put(HistoryRerequestTable.COLUMN_REREQUEST_COUNT, count);
                values.put(HistoryRerequestTable.COLUMN_SENDER_USER_ID, senderUserId.rawId());
                values.put(HistoryRerequestTable.COLUMN_TIMESTAMP, System.currentTimeMillis());
                db.insertWithOnConflict(HistoryRerequestTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_ABORT);
            } else {
                db.updateWithOnConflict(HistoryRerequestTable.TABLE_NAME, values,
                        HistoryRerequestTable.COLUMN_SENDER_USER_ID + "=? AND " + HistoryRerequestTable.COLUMN_HISTORY_RESEND_ID + "=?",
                        new String[]{senderUserId.rawId(), historyId},
                        SQLiteDatabase.CONFLICT_ABORT);
            }
        } catch (SQLException ex) {
            Log.e("PostsDb.setHistoryResendRerequestCount: failed");
            throw ex;
        }
    }

    @WorkerThread
    void setOutboundRerequestCount(@NonNull UserId rerequesterUserId, @NonNull String contentId, @NonNull String parentTable, int count) {
        Log.i("PostsDb.setOutboundRerequestCount: userId=" + rerequesterUserId + " contentId=" + contentId + " parentTable=" + parentTable + " count=" + count);

        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        Long existingRowId = null;
        String sql = "SELECT " + RerequestsTable._ID + " FROM " + RerequestsTable.TABLE_NAME
                + " WHERE " + RerequestsTable.COLUMN_CONTENT_ID + "=?"
                + " AND " + RerequestsTable.COLUMN_REQUESTOR_USER_ID + "=?"
                + " AND " + RerequestsTable.COLUMN_PARENT_TABLE + "=?";
        try (Cursor cursor = db.rawQuery(sql, new String[]{contentId, rerequesterUserId.rawId(), parentTable})) {
            if (cursor.moveToNext()) {
                existingRowId = cursor.getLong(0);
            }
        }

        final ContentValues values = new ContentValues();
        values.put(RerequestsTable.COLUMNT_REREQUEST_COUNT, count);
        try {
            if (existingRowId == null) {
                values.put(RerequestsTable.COLUMN_CONTENT_ID, contentId);
                values.put(RerequestsTable.COLUMN_REQUESTOR_USER_ID, rerequesterUserId.rawId());
                values.put(RerequestsTable.COLUMN_PARENT_TABLE, parentTable);
                db.insertWithOnConflict(RerequestsTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_ABORT);
            } else {
                db.updateWithOnConflict(RerequestsTable.TABLE_NAME, values, RerequestsTable._ID + "=?", new String[]{Long.toString(existingRowId)}, SQLiteDatabase.CONFLICT_ABORT);
            }
        } catch (SQLException ex) {
            Log.e("PostsDb.setOutboundRerequestCount: failed");
            throw ex;
        }
    }

    @WorkerThread
    int getOutboundRerequestCount(@NonNull UserId rerequesterUserId, @NonNull String contentId, @NonNull String parentTable) {
        Log.i("PostsDb.getOutboundRerequestCount: userId=" + rerequesterUserId + " contentId=" + contentId + " parentTable=" + parentTable);
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        final String sql = "SELECT " + RerequestsTable.COLUMNT_REREQUEST_COUNT + " FROM " + RerequestsTable.TABLE_NAME
                + " WHERE " + RerequestsTable.COLUMN_REQUESTOR_USER_ID + "=?"
                + " AND " + RerequestsTable.COLUMN_CONTENT_ID + "=?"
                + " AND " + RerequestsTable.COLUMN_PARENT_TABLE + "=?";
        try (final Cursor cursor = db.rawQuery(sql, new String[]{rerequesterUserId.rawId(), contentId, parentTable})) {
            if (cursor.moveToNext()) {
                return cursor.getInt(0);
            } else {
                return 0;
            }
        }
    }

    @WorkerThread
    void setHistoryResendPayload(@NonNull GroupId groupId, @NonNull String historyResendId, @NonNull byte[] payload) {
        Log.i("PostsDb.setHistoryResendPayload: groupId=" + groupId + " historyResendId=" + historyResendId);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(HistoryResendPayloadTable.COLUMN_GROUP_ID, groupId.rawId());
        contentValues.put(HistoryResendPayloadTable.COLUMN_HISTORY_RESEND_ID, historyResendId);
        contentValues.put(HistoryResendPayloadTable.COLUMN_PAYLOAD, payload);
        contentValues.put(HistoryResendPayloadTable.COLUMN_TIMESTAMP, System.currentTimeMillis());
        try {
            db.insertWithOnConflict(HistoryResendPayloadTable.TABLE_NAME, null, contentValues, SQLiteDatabase.CONFLICT_ABORT);
        } catch (SQLException ex) {
            Log.e("PostsDb.setHistoryResendPayload: failed");
            throw ex;
        }
    }

    @WorkerThread
    byte[] getHistoryResendPayload(@NonNull GroupId groupId, @NonNull String historyResendId) {
        Log.i("PostsDb.getHistoryResendPayload: groupId=" + groupId + " historyResendId=" + historyResendId);
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (Cursor cursor = db.query(HistoryResendPayloadTable.TABLE_NAME,
                new String[] {HistoryResendPayloadTable._ID, HistoryResendPayloadTable.COLUMN_GROUP_ID, HistoryResendPayloadTable.COLUMN_HISTORY_RESEND_ID, HistoryResendPayloadTable.COLUMN_PAYLOAD},
                HistoryResendPayloadTable.COLUMN_GROUP_ID + "=? AND " + HistoryResendPayloadTable.COLUMN_HISTORY_RESEND_ID + "=?",
                new String[] {groupId.rawId(), historyResendId},
                null,
                null,
                null)) {
            if (cursor.moveToNext()) {
                return cursor.getBlob(3);
            }
        }
        return null;
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
    int getUnseenCommentCount(@NonNull String postId) {
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        final String sql =
                "SELECT COUNT(" + CommentsTable._ID + ") FROM " + CommentsTable.TABLE_NAME + " " +
                        "WHERE " + CommentsTable.COLUMN_POST_ID + "=? AND " + CommentsTable.COLUMN_SEEN + "=0";
        try (final Cursor cursor = db.rawQuery(sql, new String [] {postId})) {
            if (cursor.moveToNext()) {
                return cursor.getInt(0);
            } else {
                return 0;
            }
        }
    }

    @WorkerThread
    long getFirstUnseenCommentRowId(@NonNull String postId) {
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        final String sql =
                "SELECT MIN(" + CommentsTable._ID + ") FROM " + CommentsTable.TABLE_NAME + " " +
                        "WHERE " + CommentsTable.COLUMN_POST_ID + "=? AND " + CommentsTable.COLUMN_SEEN + "=0";
        try (final Cursor cursor = db.rawQuery(sql, new String [] {postId})) {
            if (cursor.moveToNext()) {
                return cursor.isNull(0) ? -1 : cursor.getLong(0);
            } else {
                return -1;
            }
        }
    }

    @WorkerThread
    @NonNull int getCommentsFlatCount(@NonNull String postId) {
        final String sqls = "SELECT COUNT(*) FROM comments WHERE post_id=? AND timestamp > " + getPostExpirationTime();
        int count = 0;
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.rawQuery(sqls, new String [] {postId})) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                count = cursor.getInt(0);
            }
        }
        return count;
    }

    @WorkerThread
    @NonNull int getCommentFlatIndex(@NonNull String postId, @NonNull String commentId) {
        Comment comment = getComment(commentId);
        if (comment == null) {
            return -1;
        }
        final String sqls = "SELECT COUNT(*) FROM comments WHERE post_id=? AND timestamp > " + getPostExpirationTime() + " AND timestamp < " + comment.timestamp;
        int count = -1;
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.rawQuery(sqls, new String [] {postId})) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                count = cursor.getInt(0);
            }
        }
        return count;
    }

    @WorkerThread
    @NonNull List<Comment> getCommentsFlat(@NonNull String postId, int start, int count) {
        final String sqls = "SELECT 0, _id, timestamp, parent_id, comment_sender_user_id, comment_id, transferred, seen, text, type, played FROM comments WHERE post_id=? AND timestamp > " + getPostExpirationTime() + " ORDER BY timestamp ASC LIMIT " + count + " OFFSET " + start ;
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
                        cursor.getInt(6),
                        cursor.getInt(7) == 1,
                        cursor.getString(8));
                comment.type = cursor.getInt(9);
                comment.played = cursor.getInt(10) == 1;
                fillMedia(comment);
                mentionsDb.fillMentions(comment);
                urlPreviewsDb.fillUrlPreview(comment);
                comment.setParentPost(parentPost);
                comments.add(comment);
            }
        }
        Log.i("ContentDb.getComments: start=" + start + " count=" + count + " comments.size=" + comments.size());
        return comments;
    }

    @WorkerThread
    @NonNull List<Comment> getComments(@NonNull String postId, @Nullable Integer start, @Nullable Integer count) {
        final String sql =
                "WITH RECURSIVE " +
                    "comments_tree(level, _id, timestamp, parent_id, comment_sender_user_id, comment_id, transferred, seen, text, type) AS ( " +
                        "SELECT 0, _id, timestamp, parent_id, comment_sender_user_id, comment_id, transferred, seen, text, type FROM comments WHERE post_id=? AND parent_id IS NULL AND timestamp > " + getPostExpirationTime() + " " +
                        "UNION ALL " +
                        "SELECT comments_tree.level+1, comments._id, comments.timestamp, comments.parent_id, comments.comment_sender_user_id, comments.comment_id, comments.transferred, comments.seen, comments.text, comments.type " +
                            "FROM comments, comments_tree WHERE comments.parent_id=comments_tree.comment_id ORDER BY 1 DESC, 2) " +
                "SELECT * FROM comments_tree " +
                        (count == null ? "" : ("LIMIT " + count)) + (start == null ? "" : " OFFSET " + start);
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
                        cursor.getInt(6),
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

    @WorkerThread
    byte[] getPostProtoHash(@NonNull String postId) {
        final String sql = "SELECT " + PostsTable.COLUMN_PROTO_HASH + " FROM " + PostsTable.TABLE_NAME + " WHERE " + PostsTable.COLUMN_POST_ID + " =?";
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.rawQuery(sql, new String [] {postId})) {
            if (cursor.moveToNext()) {
                return cursor.getBlob(0);
            }
        }
        return null;
    }

    @WorkerThread
    byte[] getCommentProtoHash(@NonNull String commentId) {
        final String sql = "SELECT " + CommentsTable.COLUMN_PROTO_HASH + " FROM " + CommentsTable.TABLE_NAME + " WHERE " + CommentsTable.COLUMN_COMMENT_ID + " =?";
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.rawQuery(sql, new String [] {commentId})) {
            if (cursor.moveToNext()) {
                return cursor.getBlob(0);
            }
        }
        return null;
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
                        "m." + MediaTable.COLUMN_TRANSFERRED + "," +
                        "m." + MediaTable.COLUMN_BLOB_VERSION + "," +
                        "m." + MediaTable.COLUMN_CHUNK_SIZE + "," +
                        "m." + MediaTable.COLUMN_BLOB_SIZE + " " +
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
                        MediaTable.COLUMN_TRANSFERRED + "," +
                        MediaTable.COLUMN_BLOB_VERSION + "," +
                        MediaTable.COLUMN_CHUNK_SIZE + "," +
                        MediaTable.COLUMN_BLOB_SIZE + " FROM " + MediaTable.TABLE_NAME + " ORDER BY " + MediaTable._ID + " ASC) " +
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
                            cursor.getInt(18),
                            cursor.getInt(19),
                            cursor.getLong(20));
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
                        cursor.getInt(6),
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
                        CommentsTable.COLUMN_SEEN + ", " +
                        CommentsTable.COLUMN_REREQUEST_COUNT + ", " +
                        CommentsTable.COLUMN_TYPE + " " +
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
                        cursor.getInt(6),
                        cursor.getInt(8) == 1,
                        cursor.getString(7));
                comment.rerequestCount = cursor.getInt(9);
                comment.type = cursor.getInt(10);
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
        final String subscribedPostSubquery =
                "(SELECT " + PostsTable.COLUMN_POST_ID + " FROM " + PostsTable.TABLE_NAME
                        + " WHERE " + PostsTable.COLUMN_SENDER_USER_ID + "=''"
                        + " OR " + PostsTable.COLUMN_SUBSCRIBED + "=1)";
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
                    "AND EXISTS(SELECT post_id FROM posts WHERE posts.post_id=comments.post_id)" + // post exists
                    "AND ((" +
                        "EXISTS(SELECT post_id FROM " + PostsTable.TABLE_NAME + " WHERE posts.post_id=comments.post_id AND posts.sender_user_id='')" + // my post
                        "OR " +
                        "EXISTS(SELECT post_id FROM comments AS c WHERE comments.post_id==c.post_id AND c.comment_sender_user_id='') " + // i commented on the post
                    ") OR (" + CommentsTable.COLUMN_SHOULD_NOTIFY + "=1)) " +
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
                        cursor.getInt(6),
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
    @NonNull List<Comment> getNotificationComments(long timestamp, int count) {
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
                CommentsTable.COLUMN_SEEN + "=0 AND " + CommentsTable.COLUMN_TIMESTAMP + ">" + Math.max(timestamp, getPostExpirationTime())
                        + " AND " + CommentsTable.COLUMN_SHOULD_NOTIFY + "=1",
                null, null, null, CommentsTable.COLUMN_TIMESTAMP + " ASC LIMIT " + count)) {
            while (cursor.moveToNext()) {
                final Comment comment = new Comment(
                        cursor.getLong(0),
                        cursor.getString(1),
                        new UserId(cursor.getString(2)),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getLong(5),
                        cursor.getInt(6),
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
        Log.i("ContentDb.getNotificationComments: comments.size=" + comments.size());
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
                    post = Post.build(
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
                CommentsTable.COLUMN_TRANSFERRED + "=0 AND " + CommentsTable.COLUMN_TIMESTAMP + ">" + getPostExpirationTime(),
                null, null, null, null)) {
            while (cursor.moveToNext()) {
                final Comment comment = new Comment(
                        cursor.getLong(0),
                        cursor.getString(1),
                        new UserId(cursor.getString(2)),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getLong(5),
                        cursor.getInt(6),
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
        List<Post> posts = getPosts(System.currentTimeMillis() - Constants.SHARE_OLD_POST_LIMIT, null, false, UserId.ME, null, false, false);
        for (Post post : posts) {
            if (!post.isRetracted()) {
                ret.add(post);
            }
        }
        return ret;
    }

    @NonNull
    List<Post> getAllPosts(@Nullable GroupId groupId) {
        List<Post> posts = getPosts(null, null, false, null, groupId, false, false);
        return posts;
    }

    @NonNull
    List<Post> getArchivedPosts(Long timestamp, @Nullable Integer pageSize, boolean after) {
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
                        "m." + MediaTable.COLUMN_TRANSFERRED + "," +
                        "m." + MediaTable.COLUMN_BLOB_VERSION + "," +
                        "m." + MediaTable.COLUMN_CHUNK_SIZE + "," +
                        "m." + MediaTable.COLUMN_BLOB_SIZE + " " +
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
                            MediaTable.COLUMN_TRANSFERRED + "," +
                            MediaTable.COLUMN_BLOB_VERSION + "," +
                            MediaTable.COLUMN_CHUNK_SIZE + "," +
                            MediaTable.COLUMN_BLOB_SIZE + " FROM " + MediaTable.TABLE_NAME + " ORDER BY " + MediaTable._ID + " ASC) " +
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
                    post = Post.build(
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
                            cursor.getInt(14),
                            cursor.getInt(15),
                            cursor.getLong(16));
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
        processFutureProofPosts(parser, observers);
    }

    private void processFutureProofComments(FeedContentParser parser, ContentDbObservers observers) {
        List<FutureProofComment> futureProofComments = getFutureProofComments();
        for (FutureProofComment futureProofComment : futureProofComments) {
            try {
                CommentContainer commentContainer = CommentContainer.parseFrom(futureProofComment.getProtoBytes());
                Comment comment = parser.parseComment(futureProofComment.id, futureProofComment.parentCommentId, futureProofComment.senderUserId, futureProofComment.timestamp, commentContainer, false);
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
        urlPreviewsDb.addUrlPreview(newComment);
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
                        cursor.getInt(6),
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
        Log.i("ContentDb.getFutureProofComments: comments.size=" + comments.size());
        return comments;
    }

    private void processFutureProofPosts(FeedContentParser parser, ContentDbObservers observers) {
        List<FutureProofPost> futureProofPosts = getFutureProofPosts();
        for (FutureProofPost futureProofPost : futureProofPosts) {
            try {
                PostContainer postContainer = PostContainer.parseFrom(futureProofPost.getProtoBytes());
                Post post = parser.parsePost(futureProofPost.id, futureProofPost.senderUserId, futureProofPost.timestamp, postContainer, false);
                if (post instanceof FutureProofPost) {
                    continue;
                }
                replaceFutureProofPost(futureProofPost, post);
                observers.notifyPostAdded(post);
            } catch (InvalidProtocolBufferException e) {
                Log.e("PostsDb.processFutureProofPosts failed to parse proto", e);
            }
        }
    }

    private void replaceFutureProofPost(@NonNull FutureProofPost original, @NonNull Post newPost) {
        final ContentValues values = new ContentValues();
        values.put(PostsTable.COLUMN_POST_ID, newPost.id);
        values.put(PostsTable.COLUMN_SENDER_USER_ID, newPost.senderUserId.rawId());
        values.put(PostsTable.COLUMN_TIMESTAMP, newPost.timestamp);
        values.put(PostsTable.COLUMN_TRANSFERRED, newPost.transferred);
        values.put(PostsTable.COLUMN_SEEN, newPost.seen);
        values.put(PostsTable.COLUMN_TEXT, newPost.text);
        values.put(PostsTable.COLUMN_TYPE, newPost.type);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();

        db.updateWithOnConflict(PostsTable.TABLE_NAME, values, PostsTable._ID + "=?", new String[]{Long.toString(original.rowId)}, SQLiteDatabase.CONFLICT_ABORT);
        newPost.rowId = original.rowId;
        mediaDb.addMedia(newPost);
        mentionsDb.addMentions(newPost);
        urlPreviewsDb.addUrlPreview(newPost);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    @WorkerThread
    @NonNull List<FutureProofPost> getFutureProofPosts() {
        final List<FutureProofPost> posts = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.query(PostsTable.TABLE_NAME,
                new String [] {
                        PostsTable._ID,
                        PostsTable.COLUMN_SENDER_USER_ID,
                        PostsTable.COLUMN_POST_ID,
                        PostsTable.COLUMN_TIMESTAMP,
                        PostsTable.COLUMN_TRANSFERRED,
                        PostsTable.COLUMN_SEEN,
                        PostsTable.COLUMN_TEXT,
                        PostsTable.COLUMN_TYPE},
                PostsTable.COLUMN_TYPE + "=? AND " + PostsTable.COLUMN_TIMESTAMP + ">" + getPostExpirationTime(),
                new String[]{Integer.toString(Post.TYPE_FUTURE_PROOF)}, null, null, null)) {
            while (cursor.moveToNext()) {
                final FutureProofPost post = new FutureProofPost(
                        cursor.getLong(0),
                        new UserId(cursor.getString(1)),
                        cursor.getString(2),
                        cursor.getLong(3),
                        cursor.getInt(4),
                        cursor.getInt(5),
                        cursor.getString(6));
                mentionsDb.fillMentions(post);
                futureProofDb.fillFutureProof(post);
                posts.add(post);
            }
        }
        Log.i("ContentDb.getFutureProofPosts: posts.size=" + posts.size());
        return posts;
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

        final int deletedHistoryPayloads = db.delete(HistoryResendPayloadTable.TABLE_NAME,
                HistoryResendPayloadTable.COLUMN_TIMESTAMP + "<" + (System.currentTimeMillis() - DateUtils.WEEK_IN_MILLIS),
                null);
        Log.i("ContentDb.cleanup: " + deletedHistoryPayloads + " history payloads deleted");

        return (deletedPostsCount > 0 || deletedCommentsCount > 0 || deletedSeenCount > 0 || archivedPostsCount > 0 || deletedHistoryPayloads > 0);
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

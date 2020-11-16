package com.halloapp.content;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.halloapp.AppContext;
import com.halloapp.FileStore;
import com.halloapp.content.tables.ChatsTable;
import com.halloapp.content.tables.GroupMembersTable;
import com.halloapp.content.tables.MediaTable;
import com.halloapp.content.tables.MessagesTable;
import com.halloapp.content.tables.OutgoingSeenReceiptsTable;
import com.halloapp.content.tables.RepliesTable;
import com.halloapp.groups.GroupInfo;
import com.halloapp.groups.GroupsSync;
import com.halloapp.groups.MemberInfo;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.media.MediaUtils;
import com.halloapp.util.logs.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.util.RandomId;
import com.halloapp.xmpp.groups.MemberElement;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class MessagesDb {

    private final MentionsDb mentionsDb;
    private final ContentDbHelper databaseHelper;
    private final FileStore fileStore;

    MessagesDb(MentionsDb mentionsDb, ContentDbHelper databaseHelper, FileStore fileStore) {
        this.mentionsDb = mentionsDb;
        this.databaseHelper = databaseHelper;
        this.fileStore = fileStore;
    }

    @WorkerThread
    boolean addMessage(@NonNull Message message, boolean unseen, @Nullable Post replyPost, @Nullable Message replyMessage) {
        Log.i("ContentDb.addMessage " + message + " " + unseen);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final ContentValues messageValues = new ContentValues();
            messageValues.put(MessagesTable.COLUMN_CHAT_ID, message.chatId.rawId());
            messageValues.put(MessagesTable.COLUMN_SENDER_USER_ID, message.senderUserId.rawId());
            messageValues.put(MessagesTable.COLUMN_MESSAGE_ID, message.id);
            messageValues.put(MessagesTable.COLUMN_TIMESTAMP, message.timestamp);
            messageValues.put(MessagesTable.COLUMN_TYPE, message.type);
            messageValues.put(MessagesTable.COLUMN_USAGE, message.usage);
            messageValues.put(MessagesTable.COLUMN_STATE, message.state);
            if (message.text != null) {
                messageValues.put(MessagesTable.COLUMN_TEXT, message.text);
            }
            message.rowId = db.insertWithOnConflict(MessagesTable.TABLE_NAME, null, messageValues, SQLiteDatabase.CONFLICT_ABORT);
            for (Media mediaItem : message.media) {
                final ContentValues mediaItemValues = new ContentValues();
                mediaItemValues.put(MediaTable.COLUMN_PARENT_TABLE, MessagesTable.TABLE_NAME);
                mediaItemValues.put(MediaTable.COLUMN_PARENT_ROW_ID, message.rowId);
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
                if (mediaItem.encFile != null) {
                    mediaItemValues.put(MediaTable.COLUMN_ENC_FILE, mediaItem.encFile.getName());
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
            mentionsDb.addMentions(message);

            if (message.replyPostId != null || message.replyMessageId != null) {
                final ContentValues replyValues = new ContentValues();
                replyValues.put(RepliesTable.COLUMN_MESSAGE_ROW_ID, message.rowId);

                ContentItem replyItem = null;
                int mediaIndex = -1;
                if (message.replyMessageId != null) {
                    replyItem = replyMessage;
                    mediaIndex = message.replyMessageMediaIndex;
                    replyValues.put(RepliesTable.COLUMN_POST_ID, ""); // TODO(jack)
                    replyValues.put(RepliesTable.COLUMN_REPLY_MESSAGE_ID, message.replyMessageId);
                    replyValues.put(RepliesTable.COLUMN_REPLY_MESSAGE_MEDIA_INDEX, message.replyMessageMediaIndex);
                    replyValues.put(RepliesTable.COLUMN_REPLY_MESSAGE_SENDER_ID, message.replyMessageSenderId.rawId());
                } else if (replyPost != null) {
                    replyItem = replyPost;
                    mediaIndex = message.replyPostMediaIndex;
                    replyValues.put(RepliesTable.COLUMN_POST_ID, message.replyPostId);
                    replyValues.put(RepliesTable.COLUMN_POST_MEDIA_INDEX, message.replyPostMediaIndex);
                    replyValues.put(RepliesTable.COLUMN_REPLY_MESSAGE_SENDER_ID, message.replyMessageSenderId.rawId());
                } else {
                    Log.e("Content item for reply is null");
                }

                if (replyItem != null) {
                    if (!TextUtils.isEmpty(replyItem.text)) {
                        replyValues.put(RepliesTable.COLUMN_TEXT, replyItem.text);
                    }
                    final Media replyMedia = (mediaIndex >= 0 && mediaIndex < replyItem.media.size()) ? replyItem.media.get(mediaIndex) : null;
                    if (replyMedia != null && replyMedia.file != null) {
                        replyValues.put(RepliesTable.COLUMN_MEDIA_TYPE, replyMedia.type);
                        final File replyThumbFile = fileStore.getMediaFile(RandomId.create() + "." + Media.getFileExt(replyMedia.type));
                        try {
                            MediaUtils.createThumb(replyMedia.file, replyThumbFile, replyMedia.type, 320);
                            replyValues.put(RepliesTable.COLUMN_MEDIA_PREVIEW_FILE, replyThumbFile.getName());
                        } catch (IOException e) {
                            Log.e("ContentDb.addMessage: cannot create reply preview", e);
                        }
                    }
                }
                long id = db.insertWithOnConflict(RepliesTable.TABLE_NAME, null, replyValues, SQLiteDatabase.CONFLICT_IGNORE);
                if (replyItem != null && replyItem.mentions.size() > 0) {
                    mentionsDb.addReplyPreviewMentions(id, replyItem.mentions);
                }
            }

            final int updatedRowsCount;
            try (SQLiteStatement statement = db.compileStatement("UPDATE " + ChatsTable.TABLE_NAME + " SET " +
                    ChatsTable.COLUMN_TIMESTAMP + "=" + message.timestamp + " " +
                    (unseen ? (", " + ChatsTable.COLUMN_NEW_MESSAGE_COUNT + "=" + ChatsTable.COLUMN_NEW_MESSAGE_COUNT + "+1 ") : "") +
                    (unseen ? (", " + ChatsTable.COLUMN_FIRST_UNSEEN_MESSAGE_ROW_ID + "=CASE WHEN " + ChatsTable.COLUMN_FIRST_UNSEEN_MESSAGE_ROW_ID + ">= 0 THEN " + ChatsTable.COLUMN_FIRST_UNSEEN_MESSAGE_ROW_ID + " ELSE " + message.rowId + " END ") : "") +
                    (message.type == Message.TYPE_CHAT ? (", " + ChatsTable.COLUMN_LAST_MESSAGE_ROW_ID + "=" + message.rowId + " ") : "") +
                    " WHERE " + ChatsTable.COLUMN_CHAT_ID + "='" + message.chatId.rawId() + "'")) {
                updatedRowsCount = statement.executeUpdateDelete();
            }
            if (updatedRowsCount == 0) {
                final ContentValues chatValues = new ContentValues();
                chatValues.put(ChatsTable.COLUMN_CHAT_ID, message.chatId.rawId());
                chatValues.put(ChatsTable.COLUMN_TIMESTAMP, message.timestamp);
                if (message.type == Message.TYPE_CHAT) {
                    chatValues.put(ChatsTable.COLUMN_LAST_MESSAGE_ROW_ID, message.rowId);
                }
                if (unseen) {
                    chatValues.put(ChatsTable.COLUMN_NEW_MESSAGE_COUNT, 1);
                    chatValues.put(ChatsTable.COLUMN_FIRST_UNSEEN_MESSAGE_ROW_ID, message.rowId);
                } else {
                    chatValues.put(ChatsTable.COLUMN_NEW_MESSAGE_COUNT, 0);
                    chatValues.put(ChatsTable.COLUMN_FIRST_UNSEEN_MESSAGE_ROW_ID, -1);
                }
                db.insertWithOnConflict(ChatsTable.TABLE_NAME, null, chatValues, SQLiteDatabase.CONFLICT_ABORT);
            }

            db.setTransactionSuccessful();
            Log.i("ContentDb.addMessage: added " + message);
        } catch (SQLiteConstraintException ex) {
            Log.w("ContentDb.addMessage: duplicate " + ex.getMessage() + " " + message);
            return false;
        } finally {
            db.endTransaction();
        }
        return true;
    }

    @WorkerThread
    boolean addGroupChat(@NonNull GroupInfo groupInfo) {
        Log.i("MessagesDb.addGroupChat " + groupInfo.groupId);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final ContentValues chatValues = new ContentValues();
            chatValues.put(ChatsTable.COLUMN_CHAT_ID, groupInfo.groupId.rawId());
            chatValues.put(ChatsTable.COLUMN_CHAT_NAME, groupInfo.name);
            chatValues.put(ChatsTable.COLUMN_IS_GROUP, 1);
            chatValues.put(ChatsTable.COLUMN_GROUP_DESCRIPTION, groupInfo.description);
            chatValues.put(ChatsTable.COLUMN_GROUP_AVATAR_ID, groupInfo.avatar);
            chatValues.put(ChatsTable.COLUMN_NEW_MESSAGE_COUNT, 0);

            boolean exists;
            try (Cursor cursor = db.rawQuery("SELECT * FROM " + ChatsTable.TABLE_NAME + " WHERE " + ChatsTable.COLUMN_CHAT_ID + "=?", new String[]{groupInfo.groupId.rawId()})) {
                exists = cursor.getCount() > 0;
            }

            if (exists) {
                db.update(ChatsTable.TABLE_NAME, chatValues, ChatsTable.COLUMN_CHAT_ID + "=?", new String[]{groupInfo.groupId.rawId()});
            } else {
                db.insertWithOnConflict(ChatsTable.TABLE_NAME, null, chatValues, SQLiteDatabase.CONFLICT_REPLACE);
            }

            if (groupInfo.members != null) {
                for (MemberInfo member : groupInfo.members) {
                    final ContentValues memberValues = new ContentValues();
                    memberValues.put(GroupMembersTable.COLUMN_GROUP_ID, groupInfo.groupId.rawId());
                    memberValues.put(GroupMembersTable.COLUMN_USER_ID, member.userId.rawId());
                    memberValues.put(GroupMembersTable.COLUMN_IS_ADMIN, MemberElement.Type.ADMIN.equals(member.type) ? 1 : 0);
                    db.insertWithOnConflict(GroupMembersTable.TABLE_NAME, null, memberValues, SQLiteDatabase.CONFLICT_ABORT);
                }
            }

            db.setTransactionSuccessful();
            Log.i("ContentDb.addGroupChat: added " + groupInfo.groupId);
        } catch (SQLiteConstraintException ex) {
            Log.w("ContentDb.addGroupChat: duplicate " + ex.getMessage() + " " + groupInfo.groupId);
            return false;
        } finally {
            db.endTransaction();
        }
        return true;
    }

    @WorkerThread
    boolean updateGroupChat(@NonNull GroupInfo groupInfo) {
        Log.i("MessagesDb.updateGroupChat " + groupInfo.groupId);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final ContentValues chatValues = new ContentValues();
            chatValues.put(ChatsTable.COLUMN_CHAT_NAME, groupInfo.name);
            chatValues.put(ChatsTable.COLUMN_GROUP_DESCRIPTION, groupInfo.description);
            chatValues.put(ChatsTable.COLUMN_GROUP_AVATAR_ID, groupInfo.avatar);
            db.update(ChatsTable.TABLE_NAME, chatValues, ChatsTable.COLUMN_CHAT_ID + "=?", new String[]{groupInfo.groupId.rawId()});

            db.setTransactionSuccessful();
            Log.i("ContentDb.updateGroupChat: updated " + groupInfo.groupId);
        } catch (SQLiteConstraintException ex) {
            Log.w("ContentDb.updateGroupChat: " + ex.getMessage() + " " + groupInfo.groupId);
            return false;
        } finally {
            db.endTransaction();
        }
        return true;
    }

    @WorkerThread
    boolean setGroupName(@NonNull GroupId groupId, @NonNull String name) {
        Log.i("MessagesDb.setGroupName " + groupId);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final ContentValues chatValues = new ContentValues();
            chatValues.put(ChatsTable.COLUMN_CHAT_NAME, name);
            db.update(ChatsTable.TABLE_NAME, chatValues, ChatsTable.COLUMN_CHAT_ID + "=?", new String[]{groupId.rawId()});

            db.setTransactionSuccessful();
            Log.i("ContentDb.setGroupName: success " + groupId);
        } catch (SQLiteConstraintException ex) {
            Log.w("ContentDb.setGroupName: " + ex.getMessage() + " " + groupId);
            return false;
        } finally {
            db.endTransaction();
        }
        return true;
    }

    @WorkerThread
    boolean setGroupInactive(@NonNull GroupId groupId) {
        Log.i("MessagesDb.setGroupInactive " + groupId);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final ContentValues chatValues = new ContentValues();
            chatValues.put(ChatsTable.COLUMN_IS_ACTIVE, 0);
            db.update(ChatsTable.TABLE_NAME, chatValues, ChatsTable.COLUMN_CHAT_ID + "=?", new String[]{groupId.rawId()});

            db.setTransactionSuccessful();
            Log.i("ContentDb.setGroupInactive: success " + groupId);
        } catch (SQLiteConstraintException ex) {
            Log.w("ContentDb.setGroupInactive: " + ex.getMessage() + " " + groupId);
            return false;
        } finally {
            db.endTransaction();
        }
        return true;
    }

    @WorkerThread
    boolean setGroupAvatar(@NonNull GroupId groupId, @NonNull String avatarId) {
        Log.i("MessagesDb.setGroupAvatar " + groupId);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final ContentValues chatValues = new ContentValues();
            chatValues.put(ChatsTable.COLUMN_GROUP_AVATAR_ID, avatarId);
            db.update(ChatsTable.TABLE_NAME, chatValues, ChatsTable.COLUMN_CHAT_ID + "=?", new String[]{groupId.rawId()});

            db.setTransactionSuccessful();
            Log.i("ContentDb.setGroupAvatar: success " + groupId);
        } catch (SQLiteConstraintException ex) {
            Log.w("ContentDb.setGroupAvatar: " + ex.getMessage() + " " + groupId);
            return false;
        } finally {
            db.endTransaction();
        }
        return true;
    }

    @WorkerThread
    boolean addRemoveGroupMembers(@NonNull GroupId groupId, @Nullable String groupName, @Nullable String avatarId, @NonNull List<MemberInfo> added, @NonNull List<MemberInfo> removed) {
        Log.i("MessagesDb.addRemoveGroupMembers " + groupId);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            boolean groupExists;
            try (Cursor cursor = db.rawQuery("SELECT * FROM " + ChatsTable.TABLE_NAME + " WHERE " + ChatsTable.COLUMN_CHAT_ID + "=?", new String[]{groupId.rawId()})) {
                groupExists = cursor.getCount() > 0;
            }
            if (!groupExists) {
                addGroupChat(new GroupInfo(groupId, groupName, null, avatarId, new ArrayList<>()));
                GroupsSync.getInstance(AppContext.getInstance().get()).startGroupsSync();
            }

            for (MemberInfo member : added) {
                final ContentValues memberValues = new ContentValues();
                memberValues.put(GroupMembersTable.COLUMN_GROUP_ID, groupId.rawId());
                memberValues.put(GroupMembersTable.COLUMN_USER_ID, member.userId.rawId());
                memberValues.put(GroupMembersTable.COLUMN_IS_ADMIN, MemberElement.Type.ADMIN.equals(member.type) ? 1 : 0);
                db.insertWithOnConflict(GroupMembersTable.TABLE_NAME, null, memberValues, SQLiteDatabase.CONFLICT_IGNORE);
            }

            for (MemberInfo member : removed) {
                db.delete(
                        GroupMembersTable.TABLE_NAME,
                        GroupMembersTable.COLUMN_GROUP_ID + "=? AND " + GroupMembersTable.COLUMN_USER_ID + "=?",
                        new String[]{groupId.rawId(), member.userId.rawId()}
                        );
            }

            db.setTransactionSuccessful();
            Log.i("ContentDb.addGroupMembers: added " + groupId);
        } catch (SQLiteConstraintException ex) {
            Log.w("ContentDb.addGroupMembers: duplicate " + ex.getMessage() + " " + groupId);
            return false;
        } finally {
            db.endTransaction();
        }
        return true;
    }

    @WorkerThread
    boolean promoteDemoteGroupAdmins(@NonNull GroupId groupId, @NonNull List<MemberInfo> promoted, @NonNull List<MemberInfo> demoted) {
        Log.i("MessagesDb.promoteDemoteGroupAdmins " + groupId);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (MemberInfo member : promoted) {
                final ContentValues memberValues = new ContentValues();
                memberValues.put(GroupMembersTable.COLUMN_IS_ADMIN, 1);

                db.update(GroupMembersTable.TABLE_NAME,
                        memberValues,
                        GroupMembersTable.COLUMN_GROUP_ID + "=? AND " + GroupMembersTable.COLUMN_USER_ID + "=?",
                        new String[]{groupId.rawId(), member.userId.rawId()});
            }

            for (MemberInfo member : demoted) {
                final ContentValues memberValues = new ContentValues();
                memberValues.put(GroupMembersTable.COLUMN_IS_ADMIN, 0);

                db.update(GroupMembersTable.TABLE_NAME,
                        memberValues,
                        GroupMembersTable.COLUMN_GROUP_ID + "=? AND " + GroupMembersTable.COLUMN_USER_ID + "=?",
                        new String[]{groupId.rawId(), member.userId.rawId()});
            }

            db.setTransactionSuccessful();
            Log.i("ContentDb.promoteDemoteGroupAdmins: done " + groupId);
        } catch (SQLiteConstraintException ex) {
            Log.w("ContentDb.promoteDemoteGroupAdmins: error " + ex.getMessage() + " " + groupId);
            return false;
        } finally {
            db.endTransaction();
        }
        return true;
    }

    @WorkerThread
    void setMediaTransferred(@NonNull Message message, @NonNull Media media) {
        Log.i("ContentDb.setMediaTransferred: message=" + message + " media=" + media);
        final ContentValues values = new ContentValues();
        values.put(MediaTable.COLUMN_FILE, media.file == null ? null : media.file.getName());
        values.put(MediaTable.COLUMN_ENC_FILE, media.encFile == null ? null : media.encFile.getName());
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
    void setMessageRerequestCount(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId, int count) {
        Log.i("MessagesDb.setMessageRerequestCount: chatId=" + chatId + "senderUserId=" + senderUserId + " messageId=" + messageId + " count=" + count);
        final ContentValues values = new ContentValues();
        values.put(MessagesTable.COLUMN_REREQUEST_COUNT, count);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        try {
            db.updateWithOnConflict(MessagesTable.TABLE_NAME, values,
                    MessagesTable.COLUMN_CHAT_ID + "=? AND " + MessagesTable.COLUMN_SENDER_USER_ID + "=? AND " + MessagesTable.COLUMN_MESSAGE_ID + "=?",
                    new String [] {chatId.rawId(), senderUserId.rawId(), messageId},
                    SQLiteDatabase.CONFLICT_ABORT);
        } catch (SQLException ex) {
            Log.e("MessagesDb.setMessageRerequestCount: failed");
            throw ex;
        }
    }

    @WorkerThread
    void setMessageTransferred(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
        Log.i("ContentDb.setMessageTransferred: chatId=" + chatId + " senderUserId=" + senderUserId + " messageId=" + messageId);
        final ContentValues values = new ContentValues();
        values.put(MessagesTable.COLUMN_STATE, senderUserId.isMe() ? Message.STATE_OUTGOING_SENT : Message.STATE_INCOMING_RECEIVED);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        try {
            db.updateWithOnConflict(MessagesTable.TABLE_NAME, values,
                    MessagesTable.COLUMN_CHAT_ID + "=? AND " + MessagesTable.COLUMN_SENDER_USER_ID + "=? AND " + MessagesTable.COLUMN_MESSAGE_ID + "=?",
                    new String [] {chatId.rawId(), senderUserId.rawId(), messageId},
                    SQLiteDatabase.CONFLICT_ABORT);
        } catch (SQLException ex) {
            Log.e("ContentDb.setMessageTransferred: failed");
            throw ex;
        }
    }

    @WorkerThread
    public void setPatchUrl(long rowId, @NonNull String url) {
        final ContentValues values = new ContentValues();
        values.put(MediaTable.COLUMN_PATCH_URL, url);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        try {
            db.updateWithOnConflict(MediaTable.TABLE_NAME, values,
                    MediaTable._ID + "=?",
                    new String[]{Long.toString(rowId)},
                    SQLiteDatabase.CONFLICT_ABORT);
            Log.d("Resumable Uploader ContentDb.setPatchUrl =" + url);
        } catch (SQLiteConstraintException ex) {
            Log.i("Resumable Uploader MessageDb setPatchUrl: seen duplicate", ex);
        } catch (SQLException ex) {
            Log.e("Resumable Uploader MessageDb setPatchUrl: failed " + ex);
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
        Log.d("Resumable Uploader MessageDb.getMediaEncKey failed to get encKey");
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
            Log.d("Resumable Uploader ContentDb.setUploadProgress = " + offset);
        } catch (SQLiteConstraintException ex) {
            Log.i("Resumable Uploader PostDb setPatchUrl: seen duplicate", ex);
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
    void setOutgoingMessageDelivered(@NonNull ChatId chatId, @NonNull UserId recipientUserId, @NonNull String messageId, long timestamp /*TODO (ds): use timestamp in receipts table*/) {
        Log.i("ContentDb.setOutgoingMessageDelivered: chatId=" + chatId + " recipientUserId=" + recipientUserId + " messageId=" + messageId);
        final ContentValues values = new ContentValues();
        values.put(MessagesTable.COLUMN_STATE, Message.STATE_OUTGOING_DELIVERED);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        try {
            db.updateWithOnConflict(MessagesTable.TABLE_NAME, values,
                    MessagesTable.COLUMN_CHAT_ID + "=? AND " + MessagesTable.COLUMN_SENDER_USER_ID + "='' AND " + MessagesTable.COLUMN_MESSAGE_ID + "=? AND " + MessagesTable.COLUMN_STATE + "<" + Message.STATE_OUTGOING_DELIVERED,
                    new String [] {chatId.rawId(), messageId},
                    SQLiteDatabase.CONFLICT_ABORT);
        } catch (SQLException ex) {
            Log.e("ContentDb.setOutgoingMessageDelivered: failed");
            throw ex;
        }
    }

    @WorkerThread
    void setOutgoingMessageSeen(@NonNull ChatId chatId, @NonNull UserId recipientUserId, @NonNull String messageId, long timestamp /*TODO (ds): use timestamp in receipts table*/) {
        Log.i("ContentDb.setOutgoingMessageSeen: chatId=" + chatId + " recipientUserId=" + recipientUserId + " messageId=" + messageId);
        final ContentValues values = new ContentValues();
        values.put(MessagesTable.COLUMN_STATE, Message.STATE_OUTGOING_SEEN);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        try {
            db.updateWithOnConflict(MessagesTable.TABLE_NAME, values,
                    MessagesTable.COLUMN_CHAT_ID + "=? AND " + MessagesTable.COLUMN_SENDER_USER_ID + "='' AND " + MessagesTable.COLUMN_MESSAGE_ID + "=?",
                    new String [] {chatId.rawId(), messageId},
                    SQLiteDatabase.CONFLICT_ABORT);
        } catch (SQLException ex) {
            Log.e("ContentDb.setOutgoingMessageSeen: failed");
            throw ex;
        }
    }

    @WorkerThread
    void retractMessage(@NonNull Message message) {
        Log.i("ContentDb.retractMessage: messageId=" + message.id);
        final ContentValues values = new ContentValues();
        values.put(MessagesTable.COLUMN_STATE, message.isOutgoing() ? Message.STATE_INITIAL : Message.STATE_INCOMING_RECEIVED);
        values.put(MessagesTable.COLUMN_TEXT, (String)null);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            int updatedCount = db.updateWithOnConflict(MessagesTable.TABLE_NAME, values,
                    MessagesTable.COLUMN_CHAT_ID + "=? AND " + MessagesTable.COLUMN_SENDER_USER_ID + "=? AND " + MessagesTable.COLUMN_MESSAGE_ID + "=?",
                    new String[]{message.chatId.rawId(), message.senderUserId.rawId(), message.id},
                    SQLiteDatabase.CONFLICT_ABORT);
            if (updatedCount == 0) {
                values.put(MessagesTable.COLUMN_CHAT_ID, message.chatId.rawId());
                values.put(MessagesTable.COLUMN_SENDER_USER_ID, message.senderUserId.rawId());
                values.put(MessagesTable.COLUMN_MESSAGE_ID, message.id);
                values.put(MessagesTable.COLUMN_TIMESTAMP, message.timestamp);
                message.rowId = db.insertWithOnConflict(MessagesTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_ABORT);
            } else {
                if (message.rowId == 0) {
                    final Message updateMessage = getMessage(message.chatId, message.senderUserId, message.id);
                    if (updateMessage != null) {
                        message = updateMessage;
                    }
                }
                db.delete(MediaTable.TABLE_NAME,
                        MediaTable.COLUMN_PARENT_ROW_ID + "=? AND " + MediaTable.COLUMN_PARENT_TABLE + "='" + MessagesTable.TABLE_NAME + "'",
                        new String[]{Long.toString(message.rowId)});

                for (Media media : message.media) {
                    if (media.file != null) {
                        if (!media.file.delete()) {
                            Log.e("ContentDb.retractMessage: failed to delete " + media.file.getAbsolutePath());
                        }
                    }
                }
            }
            db.setTransactionSuccessful();
            Log.i("ContentDb.retractMessage: retracted messageId=" + message.id);
        } catch (SQLException ex) {
            Log.e("ContentDb.retractMessage: failed");
            throw ex;
        } finally {
            db.endTransaction();
        }
    }

    @WorkerThread
    @NonNull List<Message> getUnseenMessages(int count) {
        final List<Message> messages = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        final String where =
                MessagesTable.TABLE_NAME + "." + MessagesTable._ID + ">=" + ChatsTable.TABLE_NAME + "." + ChatsTable.COLUMN_FIRST_UNSEEN_MESSAGE_ROW_ID + " AND " +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_CHAT_ID + "=" + ChatsTable.TABLE_NAME + "." + ChatsTable.COLUMN_CHAT_ID + " AND " +
                ChatsTable.TABLE_NAME + "." + ChatsTable.COLUMN_NEW_MESSAGE_COUNT + ">0 AND " +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_SENDER_USER_ID + "!=''";

        String sql =
            "SELECT " +
                MessagesTable.TABLE_NAME + "." + MessagesTable._ID + "," +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_CHAT_ID + ", " +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_SENDER_USER_ID + "," +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_MESSAGE_ID + "," +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_TIMESTAMP + "," +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_TYPE + "," +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_USAGE + "," +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_STATE + "," +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_TEXT + "," +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_REREQUEST_COUNT + "," +
                "m." + MediaTable._ID + "," +
                "m." + MediaTable.COLUMN_TYPE + "," +
                "m." + MediaTable.COLUMN_URL + "," +
                "m." + MediaTable.COLUMN_FILE + "," +
                "m." + MediaTable.COLUMN_ENC_FILE + "," +
                "m." + MediaTable.COLUMN_WIDTH + "," +
                "m." + MediaTable.COLUMN_HEIGHT + "," +
                "m." + MediaTable.COLUMN_TRANSFERRED + ", " +
                "r." + RepliesTable.COLUMN_POST_ID + ", " +
                "r." + RepliesTable.COLUMN_POST_MEDIA_INDEX + ", " +
                "r." + RepliesTable.COLUMN_REPLY_MESSAGE_ID + ", " +
                "r." + RepliesTable.COLUMN_REPLY_MESSAGE_MEDIA_INDEX + ", " +
                "r." + RepliesTable.COLUMN_REPLY_MESSAGE_SENDER_ID + " " +
            "FROM " + MessagesTable.TABLE_NAME + "," + ChatsTable.TABLE_NAME + " " +
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
                "AS m ON " + MessagesTable.TABLE_NAME + "." + MessagesTable._ID + "=m." + MediaTable.COLUMN_PARENT_ROW_ID + " AND '" + MessagesTable.TABLE_NAME + "'=m." + MediaTable.COLUMN_PARENT_TABLE + " " +
            "LEFT JOIN (" +
                "SELECT " +
                    RepliesTable.COLUMN_MESSAGE_ROW_ID + "," +
                    RepliesTable.COLUMN_REPLY_MESSAGE_ID + "," +
                    RepliesTable.COLUMN_REPLY_MESSAGE_MEDIA_INDEX + "," +
                    RepliesTable.COLUMN_REPLY_MESSAGE_SENDER_ID + "," +
                    RepliesTable.COLUMN_POST_ID + "," +
                    RepliesTable.COLUMN_POST_MEDIA_INDEX + " FROM " + RepliesTable.TABLE_NAME + ") " +
                "AS r ON " + MessagesTable.TABLE_NAME + "." + MessagesTable._ID + "=r." + RepliesTable.COLUMN_MESSAGE_ROW_ID + " " +
            "WHERE " + where + " " +
            "ORDER BY " + MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_TIMESTAMP + " ASC " +
            "LIMIT " + count;

        try (final Cursor cursor = db.rawQuery(sql, null)) {

            long lastRowId = -1;
            Message message = null;
            while (cursor.moveToNext()) {
                long rowId = cursor.getLong(0);
                if (lastRowId != rowId) {
                    lastRowId = rowId;
                    if (message != null) {
                        messages.add(message);
                    }
                    String rawReplySenderId = cursor.getString(22);
                    message = new Message(
                            rowId,
                            ChatId.fromNullable(cursor.getString(1)),
                            new UserId(cursor.getString(2)),
                            cursor.getString(3),
                            cursor.getLong(4),
                            cursor.getInt(5),
                            cursor.getInt(6),
                            cursor.getInt(7),
                            cursor.getString(8),
                            cursor.getString(18),
                            cursor.getInt(19),
                            cursor.getString(20),
                            cursor.getInt(21),
                            rawReplySenderId == null ? null : new UserId(rawReplySenderId),
                            cursor.getInt(9));
                    mentionsDb.fillMentions(message);
                }
                if (!cursor.isNull(10)) {
                    Media media = new Media(
                            cursor.getLong(10),
                            cursor.getInt(11),
                            cursor.getString(12),
                            fileStore.getMediaFile(cursor.getString(13)),
                            null,
                            null,
                            cursor.getInt(15),
                            cursor.getInt(16),
                            cursor.getInt(17));
                    media.encFile = fileStore.getTmpFile(cursor.getString(14));
                    Preconditions.checkNotNull(message).media.add(media);
                }
            }
            if (message != null && cursor.getCount() < count) {
                messages.add(message);
            }
        }
        Log.i("ContentDb.getUnseenMessages: count=" + count + " messages.size=" + messages.size() + (messages.isEmpty() ? "" : (" got messages from " + messages.get(0).timestamp + " to " + messages.get(messages.size()-1).timestamp)));

        return messages;

    }

    @WorkerThread
    @Nullable Message getMessage(long rowId) {
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        final String sql =
            "SELECT " +
                MessagesTable.TABLE_NAME + "." + MessagesTable._ID + "," +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_CHAT_ID + "," +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_SENDER_USER_ID + "," +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_MESSAGE_ID + "," +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_TIMESTAMP + "," +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_TYPE + "," +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_USAGE + "," +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_STATE + "," +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_TEXT + "," +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_REREQUEST_COUNT + "," +
                "m." + MediaTable._ID + "," +
                "m." + MediaTable.COLUMN_TYPE + "," +
                "m." + MediaTable.COLUMN_URL + "," +
                "m." + MediaTable.COLUMN_FILE + "," +
                "m." + MediaTable.COLUMN_ENC_FILE + "," +
                "m." + MediaTable.COLUMN_WIDTH + "," +
                "m." + MediaTable.COLUMN_HEIGHT + "," +
                "m." + MediaTable.COLUMN_TRANSFERRED + ", " +
                "r." + RepliesTable.COLUMN_POST_ID + ", " +
                "r." + RepliesTable.COLUMN_POST_MEDIA_INDEX + ", " +
                "r." + RepliesTable.COLUMN_REPLY_MESSAGE_ID + ", " +
                "r." + RepliesTable.COLUMN_REPLY_MESSAGE_MEDIA_INDEX + ", " +
                "r." + RepliesTable.COLUMN_REPLY_MESSAGE_SENDER_ID + " " +
            "FROM " + MessagesTable.TABLE_NAME + " " +
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
                "AS m ON " + MessagesTable.TABLE_NAME + "." + MessagesTable._ID + "=m." + MediaTable.COLUMN_PARENT_ROW_ID + " AND '" + MessagesTable.TABLE_NAME + "'=m." + MediaTable.COLUMN_PARENT_TABLE + " " +
            "LEFT JOIN (" +
                "SELECT " +
                    RepliesTable.COLUMN_MESSAGE_ROW_ID + "," +
                    RepliesTable.COLUMN_REPLY_MESSAGE_ID + "," +
                    RepliesTable.COLUMN_REPLY_MESSAGE_MEDIA_INDEX + "," +
                    RepliesTable.COLUMN_REPLY_MESSAGE_SENDER_ID + "," +
                    RepliesTable.COLUMN_POST_ID + "," +
                    RepliesTable.COLUMN_POST_MEDIA_INDEX + " FROM " + RepliesTable.TABLE_NAME + ") " +
                "AS r ON " + MessagesTable.TABLE_NAME + "." + MessagesTable._ID + "=r." + RepliesTable.COLUMN_MESSAGE_ROW_ID + " " +
            "WHERE " + MessagesTable.TABLE_NAME + "." + MessagesTable._ID + "=" + rowId;
        Message message = null;
        try (final Cursor cursor = db.rawQuery(sql, null)) {
            while (cursor.moveToNext()) {
                if (message == null) {
                    String rawReplySenderId = cursor.getString(22);
                    message = new Message(
                            cursor.getLong(0),
                            ChatId.fromNullable(cursor.getString(1)),
                            new UserId(cursor.getString(2)),
                            cursor.getString(3),
                            cursor.getLong(4),
                            cursor.getInt(5),
                            cursor.getInt(6),
                            cursor.getInt(7),
                            cursor.getString(8),
                            cursor.getString(18),
                            cursor.getInt(19),
                            cursor.getString(20),
                            cursor.getInt(21),
                            rawReplySenderId == null ? null : new UserId(rawReplySenderId),
                            cursor.getInt(9));
                    mentionsDb.fillMentions(message);
                }
                if (!cursor.isNull(10)) {
                    Media media = new Media(
                            cursor.getLong(10),
                            cursor.getInt(11),
                            cursor.getString(12),
                            fileStore.getMediaFile(cursor.getString(13)),
                            null,
                            null,
                            cursor.getInt(15),
                            cursor.getInt(16),
                            cursor.getInt(17));
                    media.encFile = fileStore.getTmpFile(cursor.getString(14));
                    Preconditions.checkNotNull(message).media.add(media);
                }
            }
        }
        return message;
    }

    @WorkerThread
    @Nullable Message getMessage(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        final String sql =
            "SELECT " +
                MessagesTable.TABLE_NAME + "." + MessagesTable._ID + "," +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_CHAT_ID + "," +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_SENDER_USER_ID + "," +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_MESSAGE_ID + "," +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_TIMESTAMP + "," +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_TYPE + "," +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_USAGE + "," +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_STATE + "," +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_TEXT + "," +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_REREQUEST_COUNT + "," +
                "m." + MediaTable._ID + "," +
                "m." + MediaTable.COLUMN_TYPE + "," +
                "m." + MediaTable.COLUMN_URL + "," +
                "m." + MediaTable.COLUMN_FILE + "," +
                "m." + MediaTable.COLUMN_ENC_FILE + "," +
                "m." + MediaTable.COLUMN_WIDTH + "," +
                "m." + MediaTable.COLUMN_HEIGHT + "," +
                "m." + MediaTable.COLUMN_TRANSFERRED + ", " +
                "m." + MediaTable.COLUMN_ENC_KEY + ", " +
                "m." + MediaTable.COLUMN_SHA256_HASH + "," +
                "r." + RepliesTable.COLUMN_POST_ID + ", " +
                "r." + RepliesTable.COLUMN_POST_MEDIA_INDEX + ", " +
                "r." + RepliesTable.COLUMN_REPLY_MESSAGE_ID + ", " +
                "r." + RepliesTable.COLUMN_REPLY_MESSAGE_MEDIA_INDEX + ", " +
                "r." + RepliesTable.COLUMN_REPLY_MESSAGE_SENDER_ID + " " +
            "FROM " + MessagesTable.TABLE_NAME + " " +
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
                    MediaTable.COLUMN_ENC_KEY + ", " +
                    MediaTable.COLUMN_SHA256_HASH + "," +
                    MediaTable.COLUMN_TRANSFERRED + " FROM " + MediaTable.TABLE_NAME + " ORDER BY " + MediaTable._ID + " ASC) " +
                "AS m ON " + MessagesTable.TABLE_NAME + "." + MessagesTable._ID + "=m." + MediaTable.COLUMN_PARENT_ROW_ID + " AND '" + MessagesTable.TABLE_NAME + "'=m." + MediaTable.COLUMN_PARENT_TABLE + " " +
            "LEFT JOIN (" +
                "SELECT " +
                    RepliesTable.COLUMN_MESSAGE_ROW_ID + "," +
                    RepliesTable.COLUMN_REPLY_MESSAGE_ID + "," +
                    RepliesTable.COLUMN_REPLY_MESSAGE_MEDIA_INDEX + "," +
                    RepliesTable.COLUMN_REPLY_MESSAGE_SENDER_ID + "," +
                    RepliesTable.COLUMN_POST_ID + "," +
                    RepliesTable.COLUMN_POST_MEDIA_INDEX + " FROM " + RepliesTable.TABLE_NAME + ") " +
                "AS r ON " + MessagesTable.TABLE_NAME + "." + MessagesTable._ID + "=r." + RepliesTable.COLUMN_MESSAGE_ROW_ID + " " +
            "WHERE " + MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_CHAT_ID + "=\"" + chatId.rawId() + "\" AND " +
                    MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_SENDER_USER_ID + "=\"" + senderUserId.rawId() + "\" AND " +
                    MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_MESSAGE_ID + "=\"" + messageId + "\"";
        Message message = null;
        try (final Cursor cursor = db.rawQuery(sql, null)) {
            while (cursor.moveToNext()) {
                if (message == null) {
                    String rawReplySenderId = cursor.getString(24);
                    message = new Message(
                            cursor.getLong(0),
                            ChatId.fromNullable(cursor.getString(1)),
                            new UserId(cursor.getString(2)),
                            cursor.getString(3),
                            cursor.getLong(4),
                            cursor.getInt(5),
                            cursor.getInt(6),
                            cursor.getInt(7),
                            cursor.getString(8),
                            cursor.getString(20),
                            cursor.getInt(21),
                            cursor.getString(22),
                            cursor.getInt(23),
                            rawReplySenderId == null ? null : new UserId(rawReplySenderId),
                            cursor.getInt(9));
                    mentionsDb.fillMentions(message);
                }
                if (!cursor.isNull(10)) {
                    Media media = new Media(
                            cursor.getLong(10),
                            cursor.getInt(11),
                            cursor.getString(12),
                            fileStore.getMediaFile(cursor.getString(13)),
                            cursor.getBlob(18),
                            cursor.getBlob(19),
                            cursor.getInt(15),
                            cursor.getInt(16),
                            cursor.getInt(17));
                    media.encFile = fileStore.getTmpFile(cursor.getString(14));
                    Preconditions.checkNotNull(message).media.add(media);
                }
            }
        }
        return message;
    }

    @WorkerThread
    @NonNull List<Message> getMessages(@NonNull ChatId chatId, @Nullable Long startRowId, int count, boolean after) {
        final List<Message> messages = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String where = MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_CHAT_ID + "=\"" + chatId.rawId() + "\"";
        if (startRowId != null) {
            where += " AND " + MessagesTable.TABLE_NAME + "." + MessagesTable._ID + (after ? "<" : ">") + startRowId;
        }

        final String sql =
            "SELECT " +
                MessagesTable.TABLE_NAME + "." + MessagesTable._ID + "," +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_CHAT_ID + "," +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_SENDER_USER_ID + "," +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_MESSAGE_ID + "," +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_TIMESTAMP + "," +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_TYPE + "," +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_USAGE + "," +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_STATE + "," +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_TEXT + "," +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_REREQUEST_COUNT + "," +
                "m." + MediaTable._ID + "," +
                "m." + MediaTable.COLUMN_TYPE + "," +
                "m." + MediaTable.COLUMN_URL + "," +
                "m." + MediaTable.COLUMN_FILE + "," +
                "m." + MediaTable.COLUMN_ENC_FILE + "," +
                "m." + MediaTable.COLUMN_WIDTH + "," +
                "m." + MediaTable.COLUMN_HEIGHT + "," +
                "m." + MediaTable.COLUMN_TRANSFERRED + ", " +
                "r." + RepliesTable.COLUMN_POST_ID + ", " +
                "r." + RepliesTable.COLUMN_POST_MEDIA_INDEX + ", " +
                "r." + RepliesTable.COLUMN_REPLY_MESSAGE_ID + ", " +
                "r." + RepliesTable.COLUMN_REPLY_MESSAGE_MEDIA_INDEX + ", " +
                "r." + RepliesTable.COLUMN_REPLY_MESSAGE_SENDER_ID + " " +
            "FROM " + MessagesTable.TABLE_NAME + " " +
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
                "AS m ON " + MessagesTable.TABLE_NAME + "." + MessagesTable._ID + "=m." + MediaTable.COLUMN_PARENT_ROW_ID + " AND '" + MessagesTable.TABLE_NAME + "'=m." + MediaTable.COLUMN_PARENT_TABLE + " " +
            "LEFT JOIN (" +
                "SELECT " +
                    RepliesTable.COLUMN_MESSAGE_ROW_ID + "," +
                    RepliesTable.COLUMN_POST_ID + "," +
                    RepliesTable.COLUMN_POST_MEDIA_INDEX + "," +
                    RepliesTable.COLUMN_REPLY_MESSAGE_ID + "," +
                    RepliesTable.COLUMN_REPLY_MESSAGE_SENDER_ID + "," +
                    RepliesTable.COLUMN_REPLY_MESSAGE_MEDIA_INDEX + " FROM " + RepliesTable.TABLE_NAME + ") " +
                "AS r ON " + MessagesTable.TABLE_NAME + "." + MessagesTable._ID + "=r." + RepliesTable.COLUMN_MESSAGE_ROW_ID + " " +
            "WHERE " + where + " " +
            "ORDER BY " + MessagesTable.TABLE_NAME + "." + MessagesTable._ID + (after ? " DESC " : " ASC ") +
            "LIMIT " + count;

        try (final Cursor cursor = db.rawQuery(sql, null)) {

            long lastRowId = -1;
            Message message = null;
            while (cursor.moveToNext()) {
                long rowId = cursor.getLong(0);
                if (lastRowId != rowId) {
                    lastRowId = rowId;
                    if (message != null) {
                        messages.add(message);
                    }
                    String rawReplySenderId = cursor.getString(22);
                    message = new Message(
                            rowId,
                            ChatId.fromNullable(cursor.getString(1)),
                            new UserId(cursor.getString(2)),
                            cursor.getString(3),
                            cursor.getLong(4),
                            cursor.getInt(5),
                            cursor.getInt(6),
                            cursor.getInt(7),
                            cursor.getString(8),
                            cursor.getString(18),
                            cursor.getInt(19),
                            cursor.getString(20),
                            cursor.getInt(21),
                            rawReplySenderId == null ? null : new UserId(rawReplySenderId),
                            cursor.getInt(9));
                    mentionsDb.fillMentions(message);
                }
                if (!cursor.isNull(10)) {
                    Media media = new Media(
                            cursor.getLong(10),
                            cursor.getInt(11),
                            cursor.getString(12),
                            fileStore.getMediaFile(cursor.getString(13)),
                            null,
                            null,
                            cursor.getInt(15),
                            cursor.getInt(16),
                            cursor.getInt(17));
                    media.encFile = fileStore.getTmpFile(cursor.getString(14));
                    Preconditions.checkNotNull(message).media.add(media);
                }
            }
            if (message != null && cursor.getCount() < count) {
                messages.add(message);
            }
        }
        if (!after) {
            Collections.reverse(messages);
        }
        Log.i("ContentDb.getMessages: start=" + startRowId + " count=" + count + " after=" + after + " messages.size=" + messages.size() + (messages.isEmpty() ? "" : (" got messages from " + messages.get(0).timestamp + " to " + messages.get(messages.size()-1).timestamp)));

        return messages;
    }

    @WorkerThread
    @NonNull List<Message> getPendingMessages() {
        final List<Message> messages = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        final String sql =
                "SELECT " +
                    MessagesTable.TABLE_NAME + "." + MessagesTable._ID + "," +
                    MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_CHAT_ID + ", " +
                    MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_SENDER_USER_ID + "," +
                    MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_MESSAGE_ID + "," +
                    MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_TIMESTAMP + "," +
                    MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_TYPE + "," +
                    MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_USAGE + "," +
                    MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_STATE + "," +
                    MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_TEXT + "," +
                    MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_REREQUEST_COUNT + "," +
                    "m." + MediaTable._ID + "," +
                    "m." + MediaTable.COLUMN_TYPE + "," +
                    "m." + MediaTable.COLUMN_URL + "," +
                    "m." + MediaTable.COLUMN_FILE + "," +
                    "m." + MediaTable.COLUMN_ENC_FILE + "," +
                    "m." + MediaTable.COLUMN_ENC_KEY + "," +
                    "m." + MediaTable.COLUMN_SHA256_HASH + "," +
                    "m." + MediaTable.COLUMN_WIDTH + "," +
                    "m." + MediaTable.COLUMN_HEIGHT + "," +
                    "m." + MediaTable.COLUMN_TRANSFERRED + ", " +
                    "r." + RepliesTable.COLUMN_POST_ID + ", " +
                    "r." + RepliesTable.COLUMN_POST_MEDIA_INDEX + ", " +
                    "r." + RepliesTable.COLUMN_REPLY_MESSAGE_ID + ", " +
                    "r." + RepliesTable.COLUMN_REPLY_MESSAGE_MEDIA_INDEX + ", " +
                    "r." + RepliesTable.COLUMN_REPLY_MESSAGE_SENDER_ID + " " +
                "FROM " + MessagesTable.TABLE_NAME + " " +
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
                        MediaTable.COLUMN_WIDTH + "," +
                        MediaTable.COLUMN_HEIGHT + "," +
                        MediaTable.COLUMN_TRANSFERRED + " FROM " + MediaTable.TABLE_NAME + " ORDER BY " + MediaTable._ID + " ASC) " +
                    "AS m ON " + MessagesTable.TABLE_NAME + "." + MessagesTable._ID + "=m." + MediaTable.COLUMN_PARENT_ROW_ID + " AND '" + MessagesTable.TABLE_NAME + "'=m." + MediaTable.COLUMN_PARENT_TABLE + " " +
                "LEFT JOIN (" +
                    "SELECT " +
                        RepliesTable.COLUMN_MESSAGE_ROW_ID + "," +
                        RepliesTable.COLUMN_REPLY_MESSAGE_ID + "," +
                        RepliesTable.COLUMN_REPLY_MESSAGE_MEDIA_INDEX + "," +
                        RepliesTable.COLUMN_REPLY_MESSAGE_SENDER_ID + "," +
                        RepliesTable.COLUMN_POST_ID + "," +
                        RepliesTable.COLUMN_POST_MEDIA_INDEX + " FROM " + RepliesTable.TABLE_NAME + ") " +
                    "AS r ON " + MessagesTable.TABLE_NAME + "." + MessagesTable._ID + "=r." + RepliesTable.COLUMN_MESSAGE_ROW_ID + " " +
                "WHERE " +
                    MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_STATE + "=0 AND " + MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_TIMESTAMP + ">" + getMessageRetryExpirationTime() + " " +
                "ORDER BY " + MessagesTable.TABLE_NAME + "." + MessagesTable._ID + " DESC ";

        try (final Cursor cursor = db.rawQuery(sql, null)) {

            long lastRowId = -1;
            Message message = null;
            while (cursor.moveToNext()) {
                long rowId = cursor.getLong(0);
                if (lastRowId != rowId) {
                    lastRowId = rowId;
                    if (message != null) {
                        messages.add(message);
                    }
                    String rawReplySenderId = cursor.getString(24);
                    message = new Message(
                            rowId,
                            ChatId.fromNullable(cursor.getString(1)),
                            new UserId(cursor.getString(2)),
                            cursor.getString(3),
                            cursor.getLong(4),
                            cursor.getInt(5),
                            cursor.getInt(6),
                            cursor.getInt(7),
                            cursor.getString(8),
                            cursor.getString(20),
                            cursor.getInt(21),
                            cursor.getString(22),
                            cursor.getInt(23),
                            rawReplySenderId == null ? null : new UserId(rawReplySenderId),
                            cursor.getInt(9));
                    mentionsDb.fillMentions(message);
                }
                if (!cursor.isNull(10)) {
                    Media media = new Media(
                            cursor.getLong(10),
                            cursor.getInt(11),
                            cursor.getString(12),
                            fileStore.getMediaFile(cursor.getString(13)),
                            cursor.getBlob(15),
                            cursor.getBlob(16),
                            cursor.getInt(17),
                            cursor.getInt(18),
                            cursor.getInt(19));
                    media.encFile = fileStore.getTmpFile(cursor.getString(14));
                    Preconditions.checkNotNull(message).media.add(media);
                }
            }
            if (message != null) {
                messages.add(message);
            }
        }
        Log.i("ContentDb.getPendingMessages: messages.size=" + messages.size());
        return messages;
    }

    @WorkerThread
    @Nullable ReplyPreview getReplyPreview(long messageRowId) {
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.query(RepliesTable.TABLE_NAME,
                new String [] {
                        RepliesTable._ID,
                        RepliesTable.COLUMN_TEXT,
                        RepliesTable.COLUMN_MEDIA_TYPE,
                        RepliesTable.COLUMN_MEDIA_PREVIEW_FILE},
                RepliesTable.COLUMN_MESSAGE_ROW_ID + "=?",
                new String [] {String.valueOf(messageRowId)}, null, null, null)) {
            if (cursor.moveToNext()) {
                ReplyPreview replyPreview = new ReplyPreview(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getInt(2),
                        fileStore.getMediaFile(cursor.getString(3)));
                mentionsDb.fillMentions(replyPreview);
                return replyPreview;
            }
        }
        return null;
    }

    private static long getMessageRetryExpirationTime() {
        return System.currentTimeMillis() - 24 * 60 * 60 * 1000L;
    }

    @WorkerThread
    void deleteChat(@NonNull ChatId chatId) {
        Log.i("ContentDb.deleteChat " + chatId);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();

        final String sql =
            "SELECT " +
                "m." + MediaTable.COLUMN_FILE + "," +
                "m." + MediaTable.COLUMN_ENC_FILE + " " +
            "FROM " + MessagesTable.TABLE_NAME + " " +
            "INNER JOIN (" +
                "SELECT " +
                    MediaTable.COLUMN_PARENT_TABLE + "," +
                    MediaTable.COLUMN_PARENT_ROW_ID + "," +
                    MediaTable.COLUMN_FILE + "," +
                    MediaTable.COLUMN_ENC_FILE + "," +
                    MediaTable.COLUMN_TRANSFERRED + " FROM " + MediaTable.TABLE_NAME + ")" +
                "AS m ON " + MessagesTable.TABLE_NAME + "." + MessagesTable._ID + "=m." + MediaTable.COLUMN_PARENT_ROW_ID + " AND '" + MessagesTable.TABLE_NAME + "'=m." + MediaTable.COLUMN_PARENT_TABLE + " " +
            "WHERE " + MessagesTable.COLUMN_CHAT_ID  + "='" + chatId.rawId() + "'";

        int filesDeleted = 0;
        try (final Cursor cursor = db.rawQuery(sql, null)) {
            while (cursor.moveToNext()) {
                final File mediaFile = fileStore.getMediaFile(cursor.getString(0));
                if (mediaFile != null) {
                    if (mediaFile.delete()) {
                        filesDeleted++;
                    } else {
                        Log.i("ContentDb.deleteChat: failed to delete " + mediaFile.getAbsolutePath());
                    }
                }
                final File encFile = fileStore.getTmpFile(cursor.getString(1));
                if (encFile != null) {
                    if (encFile.delete()) {
                        filesDeleted++;
                    } else {
                        Log.i("ContentDb.deleteChat: failed to delete " + encFile.getAbsolutePath());
                    }
                }
            }
        }
        final int messagesDeleted = db.delete(MessagesTable.TABLE_NAME, MessagesTable.COLUMN_CHAT_ID + "=?", new String []{chatId.rawId()});
        final int chatsDeleted = db.delete(ChatsTable.TABLE_NAME, ChatsTable.COLUMN_CHAT_ID + "=?", new String []{chatId.rawId()});
        db.setTransactionSuccessful();
        db.endTransaction();

        Log.i("ContentDb.deleteChat " + chatId + " filesDeleted=" + filesDeleted + " messagesDeleted=" + messagesDeleted + " chatsDeleted=" + chatsDeleted);
    }

    @WorkerThread
    @NonNull List<SeenReceipt> setChatSeen(@NonNull ChatId chatId) {
        Log.i("ContentDb.setChatSeen " + chatId);
        final List<SeenReceipt> seenReceipts = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final Chat chat = getChat(chatId);
            if (chat != null && chat.newMessageCount > 0) {
                // update chats table
                final ContentValues chatValues = new ContentValues();
                chatValues.put(ChatsTable.COLUMN_NEW_MESSAGE_COUNT, 0);
                chatValues.put(ChatsTable.COLUMN_FIRST_UNSEEN_MESSAGE_ROW_ID, -1);
                db.updateWithOnConflict(ChatsTable.TABLE_NAME, chatValues,
                        ChatsTable.COLUMN_CHAT_ID + "=?",
                        new String [] {chatId.rawId()},
                        SQLiteDatabase.CONFLICT_ABORT);

                try (final Cursor cursor = db.query(MessagesTable.TABLE_NAME,
                        new String [] {MessagesTable.COLUMN_MESSAGE_ID, MessagesTable.COLUMN_SENDER_USER_ID},
                        MessagesTable.COLUMN_CHAT_ID + "=? AND " + MessagesTable._ID + ">=? AND " + MessagesTable.COLUMN_SENDER_USER_ID + "<>''",
                        new String [] {chatId.rawId(), String.valueOf(chat.firstUnseenMessageRowId)}, null, null, null)) {
                    while (cursor.moveToNext()) {
                        final String messageId = cursor.getString(0);
                        final UserId senderUserId = new UserId(cursor.getString(1));
                        seenReceipts.add(new SeenReceipt(chatId, senderUserId, messageId));

                        final ContentValues values = new ContentValues();
                        values.put(OutgoingSeenReceiptsTable.COLUMN_CHAT_ID, chatId.rawId());
                        values.put(OutgoingSeenReceiptsTable.COLUMN_SENDER_USER_ID, senderUserId.rawId());
                        values.put(OutgoingSeenReceiptsTable.COLUMN_CONTENT_ITEM_ID, messageId);
                        db.insert(OutgoingSeenReceiptsTable.TABLE_NAME, null, values);
                    }
                    Log.i("ContentDb.setChatSeen: number of seen messages is " + seenReceipts.size());
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return seenReceipts;
    }

    @WorkerThread
    void setMessageSeenReceiptSent(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
        final int deleteCount = databaseHelper.getWritableDatabase().delete(OutgoingSeenReceiptsTable.TABLE_NAME,
                OutgoingSeenReceiptsTable.COLUMN_CHAT_ID + "=? AND " + OutgoingSeenReceiptsTable.COLUMN_SENDER_USER_ID + "=? AND " + OutgoingSeenReceiptsTable.COLUMN_CONTENT_ITEM_ID + "=?",
                new String [] {chatId.rawId(), senderUserId.rawId(), messageId});
        Log.i("ContentDb.setMessageSeenReceiptSent: delete " + deleteCount + " rows for " + chatId + " " + senderUserId + " " + messageId);
    }

    @WorkerThread
    @NonNull List<Chat> getChats() {
        final List<Chat> chats = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.query(ChatsTable.TABLE_NAME,
                new String [] {
                        ChatsTable._ID,
                        ChatsTable.COLUMN_CHAT_ID,
                        ChatsTable.COLUMN_TIMESTAMP,
                        ChatsTable.COLUMN_NEW_MESSAGE_COUNT,
                        ChatsTable.COLUMN_LAST_MESSAGE_ROW_ID,
                        ChatsTable.COLUMN_FIRST_UNSEEN_MESSAGE_ROW_ID,
                        ChatsTable.COLUMN_CHAT_NAME,
                        ChatsTable.COLUMN_IS_GROUP,
                        ChatsTable.COLUMN_GROUP_DESCRIPTION,
                        ChatsTable.COLUMN_GROUP_AVATAR_ID,
                        ChatsTable.COLUMN_IS_ACTIVE},
                null,
                null, null, null, ChatsTable.COLUMN_TIMESTAMP + " DESC")) {
            while (cursor.moveToNext()) {
                final Chat chat = new Chat(
                        cursor.getLong(0),
                        ChatId.fromNullable(cursor.getString(1)),
                        cursor.getLong(2),
                        cursor.getInt(3),
                        cursor.getLong(4),
                        cursor.getLong(5),
                        cursor.getString(6),
                        cursor.getInt(7) == 1,
                        cursor.getString(8),
                        cursor.getString(9),
                        cursor.getInt(10) == 1);
                chats.add(chat);
            }
        }
        return chats;
    }

    @WorkerThread
    @NonNull List<Chat> getGroups() {
        final List<Chat> chats = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.query(ChatsTable.TABLE_NAME,
                new String [] {
                        ChatsTable._ID,
                        ChatsTable.COLUMN_CHAT_ID,
                        ChatsTable.COLUMN_TIMESTAMP,
                        ChatsTable.COLUMN_NEW_MESSAGE_COUNT,
                        ChatsTable.COLUMN_LAST_MESSAGE_ROW_ID,
                        ChatsTable.COLUMN_FIRST_UNSEEN_MESSAGE_ROW_ID,
                        ChatsTable.COLUMN_CHAT_NAME,
                        ChatsTable.COLUMN_IS_GROUP,
                        ChatsTable.COLUMN_GROUP_DESCRIPTION,
                        ChatsTable.COLUMN_GROUP_AVATAR_ID,
                        ChatsTable.COLUMN_IS_ACTIVE},
                ChatsTable.COLUMN_IS_GROUP + "=?",
                new String[]{"1"}, null, null, ChatsTable.COLUMN_TIMESTAMP + " DESC")) {
            while (cursor.moveToNext()) {
                final Chat chat = new Chat(
                        cursor.getLong(0),
                        ChatId.fromNullable(cursor.getString(1)),
                        cursor.getLong(2),
                        cursor.getInt(3),
                        cursor.getLong(4),
                        cursor.getLong(5),
                        cursor.getString(6),
                        cursor.getInt(7) == 1,
                        cursor.getString(8),
                        cursor.getString(9),
                        cursor.getInt(10) == 1);
                chats.add(chat);
            }
        }
        return chats;
    }

    @WorkerThread
    @Nullable Chat getChat(@NonNull ChatId chatId) {
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.query(ChatsTable.TABLE_NAME,
                new String [] {
                        ChatsTable._ID,
                        ChatsTable.COLUMN_CHAT_ID,
                        ChatsTable.COLUMN_TIMESTAMP,
                        ChatsTable.COLUMN_NEW_MESSAGE_COUNT,
                        ChatsTable.COLUMN_LAST_MESSAGE_ROW_ID,
                        ChatsTable.COLUMN_FIRST_UNSEEN_MESSAGE_ROW_ID,
                        ChatsTable.COLUMN_CHAT_NAME,
                        ChatsTable.COLUMN_IS_GROUP,
                        ChatsTable.COLUMN_GROUP_DESCRIPTION,
                        ChatsTable.COLUMN_GROUP_AVATAR_ID,
                        ChatsTable.COLUMN_IS_ACTIVE},
                ChatsTable.COLUMN_CHAT_ID + "=?",
                new String [] {chatId.rawId()},
                null, null, null)) {
            if (cursor.moveToNext()) {
                return new Chat(
                        cursor.getLong(0),
                        ChatId.fromNullable(cursor.getString(1)),
                        cursor.getLong(2),
                        cursor.getInt(3),
                        cursor.getLong(4),
                        cursor.getLong(5),
                        cursor.getString(6),
                        cursor.getInt(7) == 1,
                        cursor.getString(8),
                        cursor.getString(9),
                        cursor.getInt(10) == 1);
            }
        }
        return null;
    }

    @WorkerThread
    @NonNull List<MemberInfo> getGroupMembers(GroupId groupId) {
        final List<MemberInfo> members = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.query(GroupMembersTable.TABLE_NAME,
                new String [] {
                        GroupMembersTable._ID,
                        GroupMembersTable.COLUMN_USER_ID,
                        GroupMembersTable.COLUMN_IS_ADMIN},
                GroupMembersTable.COLUMN_GROUP_ID + "=?",
                new String[]{groupId.rawId()}, null, null, null)) {
            while (cursor.moveToNext()) {
                final MemberInfo member = new MemberInfo(
                        cursor.getLong(0),
                        new UserId(cursor.getString(1)),
                        cursor.getInt(2) == 1 ? MemberElement.Type.ADMIN : MemberElement.Type.MEMBER,
                        null);
                members.add(member);
            }
        }
        return members;
    }

    @WorkerThread
    int getUnseenChatsCount() {
        return (int)DatabaseUtils.longForQuery(databaseHelper.getReadableDatabase(), "SELECT count(*) FROM " + ChatsTable.TABLE_NAME + " WHERE " + ChatsTable.COLUMN_NEW_MESSAGE_COUNT + ">0", null);
    }

    @WorkerThread
    List<SeenReceipt> getPendingMessageSeenReceipts() {
        final List<SeenReceipt> receipts = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.query(OutgoingSeenReceiptsTable.TABLE_NAME,
                new String [] {
                        OutgoingSeenReceiptsTable.COLUMN_CHAT_ID,
                        OutgoingSeenReceiptsTable.COLUMN_SENDER_USER_ID,
                        OutgoingSeenReceiptsTable.COLUMN_CONTENT_ITEM_ID},
                null, null, null, null, null)) {
            while (cursor.moveToNext()) {
                final SeenReceipt receipt = new SeenReceipt(
                        ChatId.fromNullable(cursor.getString(0)),
                        new UserId(cursor.getString(1)),
                        cursor.getString(2));
                receipts.add(receipt);
            }
        }
        Log.i("ContentDb.getPendingMessageSeenReceipts: receipts.size=" + receipts.size());
        return receipts;
    }
}

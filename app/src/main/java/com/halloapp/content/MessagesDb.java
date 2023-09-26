package com.halloapp.content;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDoneException;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.google.protobuf.InvalidProtocolBufferException;
import com.halloapp.FileStore;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.FriendshipInfo;
import com.halloapp.content.tables.ChatsTable;
import com.halloapp.content.tables.DeletedGroupNameTable;
import com.halloapp.content.tables.GroupMembersTable;
import com.halloapp.content.tables.GroupMessageSeenReceiptsTable;
import com.halloapp.content.tables.MediaTable;
import com.halloapp.content.tables.MessagesTable;
import com.halloapp.content.tables.OutgoingPlayedReceiptsTable;
import com.halloapp.content.tables.OutgoingSeenReceiptsTable;
import com.halloapp.content.tables.RepliesTable;
import com.halloapp.groups.GroupInfo;
import com.halloapp.groups.MemberInfo;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.media.MediaUtils;
import com.halloapp.props.ServerProps;
import com.halloapp.proto.clients.ChatContainer;
import com.halloapp.util.Preconditions;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;
import com.halloapp.util.stats.DecryptStats;
import com.halloapp.xmpp.groups.MemberElement;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

class MessagesDb {

    public static final int REPLY_THUMB_DIMENSION = 320;

    private final CallsDb callsDb;
    private final MediaDb mediaDb;
    private final FileStore fileStore;
    private final MentionsDb mentionsDb;
    private final ReactionsDb reactionsDb;
    private final ServerProps serverProps;
    private final FutureProofDb futureProofDb;
    private final UrlPreviewsDb urlPreviewsDb;
    private final ContentDbHelper databaseHelper;

    MessagesDb(
            CallsDb callsDb,
            MediaDb mediaDb,
            FileStore fileStore,
            MentionsDb mentionsDb,
            ReactionsDb reactionsDb,
            ServerProps serverProps,
            FutureProofDb futureProofDb,
            UrlPreviewsDb urlPreviewsDb,
            ContentDbHelper databaseHelper) {
        this.mediaDb = mediaDb;
        this.callsDb = callsDb;
        this.fileStore = fileStore;
        this.mentionsDb = mentionsDb;
        this.reactionsDb = reactionsDb;
        this.serverProps = serverProps;
        this.futureProofDb = futureProofDb;
        this.urlPreviewsDb = urlPreviewsDb;
        this.databaseHelper = databaseHelper;
    }

    @WorkerThread
    boolean addMessage(@NonNull Message message, boolean unseen, @Nullable Post replyPost, @Nullable Message replyMessage) {
        Log.i("ContentDb.addMessage " + message + " " + unseen);
        long now = System.currentTimeMillis();
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            Long tombstoneRowId = null;
            String tombstoneSenderId = null;
            String tombstoneSql = "SELECT " + MessagesTable._ID + "," + MessagesTable.COLUMN_SENDER_USER_ID + " FROM " + MessagesTable.TABLE_NAME + " WHERE " + MessagesTable.COLUMN_CHAT_ID + "=? AND " + MessagesTable.COLUMN_MESSAGE_ID + "=?";
            try (Cursor cursor = db.rawQuery(tombstoneSql, new String[]{message.chatId.rawId(), message.id})) {
                if (cursor.moveToNext()) {
                    tombstoneRowId = cursor.getLong(0);
                    tombstoneSenderId = cursor.getString(1);
                }
            }

            if (tombstoneSenderId != null && !tombstoneSenderId.equals(message.senderUserId.rawId())) {
                Log.e("ContentDb.addMessage tombstone with id " + message.id + " found but with different sender; dropping new message");
                return false;
            }

            final ContentValues messageValues = new ContentValues();
            messageValues.put(MessagesTable.COLUMN_CHAT_ID, message.chatId.rawId());
            messageValues.put(MessagesTable.COLUMN_SENDER_USER_ID, message.senderUserId.rawId());
            messageValues.put(MessagesTable.COLUMN_MESSAGE_ID, message.id);
            messageValues.put(MessagesTable.COLUMN_TIMESTAMP, message.timestamp);
            messageValues.put(MessagesTable.COLUMN_TYPE, message.type);
            messageValues.put(MessagesTable.COLUMN_USAGE, message.usage);
            messageValues.put(MessagesTable.COLUMN_STATE, message.state);
            messageValues.put(MessagesTable.COLUMN_RESULT_UPDATE_TIME, now);
            messageValues.put(MessagesTable.COLUMN_FAILURE_REASON, message.failureReason);
            messageValues.put(MessagesTable.COLUMN_REREQUEST_COUNT, message.rerequestCount);
            if (message.text != null) {
                messageValues.put(MessagesTable.COLUMN_TEXT, message.text);
            }

            if (tombstoneRowId != null) {
                db.update(MessagesTable.TABLE_NAME, messageValues, MessagesTable._ID + "=?", new String[]{tombstoneRowId.toString()});
                message.rowId = tombstoneRowId;
            } else {
                messageValues.put(MessagesTable.COLUMN_RECEIVE_TIME, now);
                messageValues.put(MessagesTable.COLUMN_CLIENT_VERSION, message.clientVersion);
                messageValues.put(MessagesTable.COLUMN_SENDER_VERSION, message.senderVersion);
                messageValues.put(MessagesTable.COLUMN_SENDER_PLATFORM, message.senderPlatform);
                message.rowId = db.insertWithOnConflict(MessagesTable.TABLE_NAME, null, messageValues, SQLiteDatabase.CONFLICT_ABORT);
            }

            if (!message.isTombstone()) {
                mediaDb.addMedia(message);
                mentionsDb.addMentions(message);
                urlPreviewsDb.addUrlPreview(message);

                if (message instanceof CallMessage) {
                    callsDb.addCall(message.id, ((CallMessage)message).callDuration);
                }

                if (message instanceof FutureProofMessage) {
                    futureProofDb.saveFutureProof((FutureProofMessage) message);
                }

                if (message.replyPostId != null || message.replyMessageId != null) {
                    final ContentValues replyValues = new ContentValues();
                    replyValues.put(RepliesTable.COLUMN_MESSAGE_ROW_ID, message.rowId);

                    ContentItem replyItem;
                    int mediaIndex;
                    if (message.replyMessageId != null) {
                        replyItem = replyMessage;
                        mediaIndex = message.replyMessageMediaIndex;
                        replyValues.put(RepliesTable.COLUMN_POST_ID, ""); // TODO(jack)
                        replyValues.put(RepliesTable.COLUMN_REPLY_MESSAGE_ID, message.replyMessageId);
                        replyValues.put(RepliesTable.COLUMN_REPLY_MESSAGE_MEDIA_INDEX, message.replyMessageMediaIndex);
                        replyValues.put(RepliesTable.COLUMN_REPLY_MESSAGE_SENDER_ID, message.replyMessageSenderId.rawId());
                    } else {
                        replyItem = replyPost;
                        mediaIndex = message.replyPostMediaIndex;
                        replyValues.put(RepliesTable.COLUMN_POST_ID, message.replyPostId);
                        replyValues.put(RepliesTable.COLUMN_POST_MEDIA_INDEX, message.replyPostMediaIndex);
                        if (replyPost != null) {
                            replyValues.put(RepliesTable.COLUMN_REPLY_MESSAGE_SENDER_ID, replyPost.senderUserId.rawId());
                            replyValues.put(RepliesTable.COLUMN_POST_TYPE, replyPost.type);
                        } else {
                            replyValues.put(RepliesTable.COLUMN_REPLY_MESSAGE_SENDER_ID, message.replyMessageSenderId == null ? null : message.replyMessageSenderId.rawId());
                        }
                    }

                    if (replyItem != null) {
                        if (!TextUtils.isEmpty(replyItem.text)) {
                            replyValues.put(RepliesTable.COLUMN_TEXT, replyItem.text);
                        }
                        List<Media> mediaList = replyItem.getMedia();
                        Media replyMedia = (mediaIndex >= 0 && mediaIndex < mediaList.size()) ? mediaList.get(mediaIndex) : null;

                        if (replyItem instanceof MomentPost) {
                            try {
                                final File replyThumbFile = ((MomentPost) replyItem).createThumb(REPLY_THUMB_DIMENSION);
                                replyValues.put(RepliesTable.COLUMN_MEDIA_PREVIEW_FILE, replyThumbFile.getName());
                            } catch (IOException e) {
                                Log.e("ContentDb.addMessage: cannot create moment reply preview", e);
                            }
                        } else if (replyMedia != null && replyMedia.file != null) {
                            replyValues.put(RepliesTable.COLUMN_MEDIA_TYPE, replyMedia.type);
                            if (replyMedia.type == Media.MEDIA_TYPE_AUDIO) {
                                long duration = MediaUtils.getAudioDuration(replyMedia.file);
                                if (duration != 0) {
                                    replyValues.put(RepliesTable.COLUMN_TEXT, duration);
                                }
                            } else {
                                final File replyThumbFile = fileStore.getMediaFile(RandomId.create() + "." + Media.getFileExt(replyMedia.type));
                                try {
                                    MediaUtils.createThumb(replyMedia.file, replyThumbFile, replyMedia.type, REPLY_THUMB_DIMENSION);
                                    replyValues.put(RepliesTable.COLUMN_MEDIA_PREVIEW_FILE, replyThumbFile.getName());
                                } catch (IOException e) {
                                    Log.e("ContentDb.addMessage: cannot create reply preview", e);
                                }
                            }
                        } else if (replyItem instanceof VoiceNotePost) {
                            Media audioCaption = replyItem.media.get(0);
                            if (audioCaption.file != null) {
                                long duration = MediaUtils.getAudioDuration(audioCaption.file);
                                if (duration != 0) {
                                    replyValues.put(RepliesTable.COLUMN_TEXT, duration);
                                }
                            }
                            replyValues.put(RepliesTable.COLUMN_MEDIA_TYPE, Media.MEDIA_TYPE_AUDIO);
                        }
                    }
                    long id = db.insertWithOnConflict(RepliesTable.TABLE_NAME, null, replyValues, SQLiteDatabase.CONFLICT_IGNORE);
                    if (replyItem != null && replyItem.mentions.size() > 0) {
                        mentionsDb.addReplyPreviewMentions(id, replyItem.mentions);
                    }
                }

                final int updatedRowsCount;
                if (!(message.chatId instanceof GroupId) && message.type == Message.TYPE_SYSTEM) {
                    updatedRowsCount = (int) DatabaseUtils.queryNumEntries(db, ChatsTable.TABLE_NAME, ChatsTable.COLUMN_CHAT_ID + "='" + message.chatId.rawId() + "'");
                } else {
                    try (SQLiteStatement statement = db.compileStatement("UPDATE " + ChatsTable.TABLE_NAME + " SET " +
                            ChatsTable.COLUMN_TIMESTAMP + "=" + message.timestamp + " " +
                            (unseen ? (", " + ChatsTable.COLUMN_NEW_MESSAGE_COUNT + "=" + "COALESCE(" + ChatsTable.COLUMN_NEW_MESSAGE_COUNT + ", 0)" + "+1 ") : "") +
                            (unseen ? (", " + ChatsTable.COLUMN_FIRST_UNSEEN_MESSAGE_ROW_ID + "=CASE WHEN " + ChatsTable.COLUMN_FIRST_UNSEEN_MESSAGE_ROW_ID + ">= 0 THEN " + ChatsTable.COLUMN_FIRST_UNSEEN_MESSAGE_ROW_ID + " ELSE " + message.rowId + " END ") : "") +
                            ", " + ChatsTable.COLUMN_LAST_MESSAGE_ROW_ID + "=" + message.rowId + " " +
                            " WHERE " + ChatsTable.COLUMN_CHAT_ID + "='" + message.chatId.rawId() + "'")) {
                        updatedRowsCount = statement.executeUpdateDelete();
                    }
                }
                if (updatedRowsCount == 0) {
                    final ContentValues chatValues = new ContentValues();
                    chatValues.put(ChatsTable.COLUMN_CHAT_ID, message.chatId.rawId());
                    if (message.type != Message.TYPE_SYSTEM || message.chatId instanceof GroupId) {
                        chatValues.put(ChatsTable.COLUMN_LAST_MESSAGE_ROW_ID, message.rowId);
                        chatValues.put(ChatsTable.COLUMN_TIMESTAMP, message.timestamp);
                        if (message.chatId instanceof UserId) {
                            Contact contact = ContactsDb.getInstance().getContact((UserId) message.chatId);
                            if (message.isOutgoing()) {
                                chatValues.put(ChatsTable.COLUMN_IS_ACTIVE, true);
                            } else if (contact.friendshipStatus == FriendshipInfo.Type.NONE_STATUS) {
                                chatValues.put(ChatsTable.COLUMN_IS_ACTIVE, false);
                            }
                        }
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
    boolean updateMessageDecrypt(@NonNull Message message) {
        Log.i("ContentDb.updateMessageDecrypt " + message);
        long now = System.currentTimeMillis();
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final ContentValues messageValues = new ContentValues();
            messageValues.put(MessagesTable.COLUMN_FAILURE_REASON, message.failureReason);
            messageValues.put(MessagesTable.COLUMN_RESULT_UPDATE_TIME, now);

            int count = db.update(MessagesTable.TABLE_NAME, messageValues,
                    MessagesTable.COLUMN_CHAT_ID + "=? AND " + MessagesTable.COLUMN_SENDER_USER_ID + "=? AND " + MessagesTable.COLUMN_MESSAGE_ID + "=? AND ? - " + MessagesTable.COLUMN_RECEIVE_TIME + " < ?",
                    new String [] {message.chatId.rawId(), message.senderUserId.rawId(), message.id, Long.toString(now), Long.toString(DateUtils.DAY_IN_MILLIS)});

            db.setTransactionSuccessful();

            Log.i("ContentDb.updateMessageDecrypt: updated " + count + " rows");
        } catch (SQLiteConstraintException ex) {
            Log.w("ContentDb.updateMessageDecrypt: " + ex.getMessage() + " " + message.id);
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
            Log.i("MessagesDb.updateGroupChat: updated " + groupInfo.groupId);
        } catch (SQLiteConstraintException ex) {
            Log.w("MessagesDb.updateGroupChat: " + ex.getMessage() + " " + groupInfo.groupId);
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
            chatValues.put(ChatsTable.COLUMN_GROUP_DESCRIPTION, groupInfo.description);
            chatValues.put(ChatsTable.COLUMN_GROUP_AVATAR_ID, groupInfo.avatar);
            chatValues.put(ChatsTable.COLUMN_NEW_MESSAGE_COUNT, 0);
            chatValues.put(ChatsTable.COLUMN_IS_GROUP, true);

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
            db.delete(DeletedGroupNameTable.TABLE_NAME, DeletedGroupNameTable.COLUMN_CHAT_ID + "=?", new String[]{groupInfo.groupId.rawId()});
            db.setTransactionSuccessful();
            Log.i("MessagesDb.addGroupChat: added " + groupInfo.groupId);
        } catch (SQLiteConstraintException ex) {
            Log.w("MessagesDb.addGroupChat: duplicate " + ex.getMessage() + " " + groupInfo.groupId);
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
            Log.i("MessagesDb.setGroupName: success " + groupId);
        } catch (SQLiteConstraintException ex) {
            Log.w("MessagesDb.setGroupName: " + ex.getMessage() + " " + groupId);
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
            Log.i("MessagesDb.setGroupAvatar: success " + groupId);
        } catch (SQLiteConstraintException ex) {
            Log.w("MessagesDb.setGroupAvatar: " + ex.getMessage() + " " + groupId);
            return false;
        } finally {
            db.endTransaction();
        }
        return true;
    }

    @WorkerThread
    boolean setGroupDescription(@NonNull GroupId groupId, @Nullable String description) {
        Log.i("MessagesDb.setGroupDescription " + groupId);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final ContentValues chatValues = new ContentValues();
            chatValues.put(ChatsTable.COLUMN_GROUP_DESCRIPTION, description);
            db.update(ChatsTable.TABLE_NAME, chatValues, ChatsTable.COLUMN_CHAT_ID + "=?", new String[]{groupId.rawId()});

            db.setTransactionSuccessful();
            Log.i("MessagesDb.setGroupDescription: success " + groupId);
        } catch (SQLiteConstraintException ex) {
            Log.w("MessagesDb.setGroupDescription: " + ex.getMessage() + " " + groupId);
            return false;
        } finally {
            db.endTransaction();
        }
        return true;
    }

    @WorkerThread
    boolean setGroupActive(@NonNull GroupId groupId, boolean active) {
        Log.i("MessagesDb.setGroupActive " + groupId + " active=" + active);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final ContentValues chatValues = new ContentValues();
            chatValues.put(ChatsTable.COLUMN_IS_ACTIVE, active);
            db.update(ChatsTable.TABLE_NAME, chatValues, ChatsTable.COLUMN_CHAT_ID + "=?", new String[]{groupId.rawId()});

            db.setTransactionSuccessful();
            Log.i("MessagesDb.setGroupAactive: success " + groupId);
        } catch (SQLiteConstraintException ex) {
            Log.w("MessagesDb.setGroupAactive: " + ex.getMessage() + " " + groupId);
            return false;
        } finally {
            db.endTransaction();
        }
        return true;
    }

    @WorkerThread
    boolean setUnknownContactAllowed(@NonNull UserId userId) {
        Log.i("MessagesDb.setUnknownContactAllowed " + userId);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final ContentValues chatValues = new ContentValues();
            chatValues.put(ChatsTable.COLUMN_IS_ACTIVE, 1);
            db.update(ChatsTable.TABLE_NAME, chatValues, ChatsTable.COLUMN_CHAT_ID + "=?", new String[]{userId.rawId()});

            db.setTransactionSuccessful();
            Log.i("ContentDb.setUnknownContactAllowed: success " + userId);
        } catch (SQLiteConstraintException ex) {
            Log.w("ContentDb.setUnknownContactAllowed: " + ex.getMessage() + " " + userId);
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
    void setMessageTransferred(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
        Log.i("ContentDb.setMessageTransferred: chatId=" + chatId + " senderUserId=" + senderUserId + " messageId=" + messageId);
        final ContentValues values = new ContentValues();
        values.put(MessagesTable.COLUMN_STATE, senderUserId.isMe() ? Message.STATE_OUTGOING_SENT : Message.STATE_INCOMING_RECEIVED);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        try {
            db.updateWithOnConflict(MessagesTable.TABLE_NAME, values,
                    MessagesTable.COLUMN_CHAT_ID + "=? AND " + MessagesTable.COLUMN_SENDER_USER_ID + "=? AND " + MessagesTable.COLUMN_MESSAGE_ID + "=? AND " + MessagesTable.COLUMN_STATE + "<" + Message.STATE_OUTGOING_DELIVERED,
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
    public List<MessageDeliveryState> getOutgoingMessageDeliveryStates(@NonNull String contentId) {
        List<MessageDeliveryState> deliveryStates = new ArrayList<>();
        final String sql =
                "SELECT "
                        + GroupMessageSeenReceiptsTable.COLUMN_USER_ID + ", "
                        + GroupMessageSeenReceiptsTable.COLUMN_STATE + ", "
                        + GroupMessageSeenReceiptsTable.COLUMN_TIMESTAMP + " "
                        + "FROM " + GroupMessageSeenReceiptsTable.TABLE_NAME + " "
                        + "WHERE " + GroupMessageSeenReceiptsTable.COLUMN_CONTENT_ID + "=?";

        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.rawQuery(sql, new String[]{contentId})) {
            while (cursor.moveToNext()) {
                deliveryStates.add(new MessageDeliveryState(
                        (UserId) ChatId.fromNullable(cursor.getString(0)),
                        contentId,
                        cursor.getInt(1),
                        cursor.getLong(2)));
            }
        }
        return deliveryStates;
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
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        if (chatId instanceof GroupId) {
            final ContentValues values = new ContentValues();
            String rawUserId = recipientUserId.rawId();
            values.put(GroupMessageSeenReceiptsTable.COLUMN_STATE, Message.STATE_OUTGOING_DELIVERED);
            values.put(GroupMessageSeenReceiptsTable.COLUMN_USER_ID, rawUserId);
            values.put(GroupMessageSeenReceiptsTable.COLUMN_TIMESTAMP, timestamp);
            values.put(GroupMessageSeenReceiptsTable.COLUMN_CONTENT_ID, messageId);
            try {
                int rows = db.updateWithOnConflict(GroupMessageSeenReceiptsTable.TABLE_NAME, values, GroupMessageSeenReceiptsTable.COLUMN_CONTENT_ID + "=? AND " + GroupMessageSeenReceiptsTable.COLUMN_USER_ID + "=?", new String[]{messageId, rawUserId}, SQLiteDatabase.CONFLICT_IGNORE);
                if (rows == 0) {
                    db.insertWithOnConflict(GroupMessageSeenReceiptsTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                }
            } catch (SQLException ex) {
                Log.e("ContentDb.setOutgoingMessageSeen: failed");
                throw ex;
            }
            Cursor cursor = db.query(GroupMessageSeenReceiptsTable.TABLE_NAME, new String[]{GroupMessageSeenReceiptsTable.COLUMN_USER_ID}, GroupMessageSeenReceiptsTable.COLUMN_CONTENT_ID + "=? AND " + GroupMessageSeenReceiptsTable.COLUMN_STATE + "<" + Message.STATE_OUTGOING_DELIVERED, new String[]{messageId}, null, null, null);
            int count = cursor.getCount();
            cursor.close();
            if (count > 0) {
                return;
            }
        }
        final ContentValues values = new ContentValues();
        values.put(MessagesTable.COLUMN_STATE, Message.STATE_OUTGOING_DELIVERED);
        try {
            db.updateWithOnConflict(MessagesTable.TABLE_NAME, values,
                    MessagesTable.COLUMN_CHAT_ID + "=? AND " + MessagesTable.COLUMN_SENDER_USER_ID + "='' AND " + MessagesTable.COLUMN_MESSAGE_ID + "=? AND " + MessagesTable.COLUMN_STATE + "<" + Message.STATE_OUTGOING_DELIVERED,
                    new String[]{chatId.rawId(), messageId},
                    SQLiteDatabase.CONFLICT_ABORT);
        } catch (SQLException ex) {
            Log.e("ContentDb.setOutgoingMessageDelivered: failed");
            throw ex;
        }
    }

    @WorkerThread
    void setGroupMessageSent(@NonNull GroupId groupId, @NonNull UserId recipientUserId, @NonNull String messageId, long timestamp) {
        Log.i("ContentDb.setGroupMessageSent: groupId=" + groupId + " recipientUserId=" + recipientUserId + " messageId=" + messageId);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        final ContentValues values = new ContentValues();
        values.put(GroupMessageSeenReceiptsTable.COLUMN_STATE, Message.STATE_OUTGOING_SENT);
        values.put(GroupMessageSeenReceiptsTable.COLUMN_USER_ID, recipientUserId.rawId());
        values.put(GroupMessageSeenReceiptsTable.COLUMN_TIMESTAMP, timestamp);
        values.put(GroupMessageSeenReceiptsTable.COLUMN_CONTENT_ID, messageId);
        try {
            int rows = db.updateWithOnConflict(GroupMessageSeenReceiptsTable.TABLE_NAME, values, GroupMessageSeenReceiptsTable.COLUMN_CONTENT_ID + "=?", new String[]{messageId}, SQLiteDatabase.CONFLICT_IGNORE);
            if (rows == 0) {
                db.insertWithOnConflict(GroupMessageSeenReceiptsTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
            }
        } catch (SQLException ex) {
            Log.e("ContentDb.setGroupMessageSent: failed");
            throw ex;
        }
    }

    @WorkerThread
    void setOutgoingMessageSeen(@NonNull ChatId chatId, @NonNull UserId recipientUserId, @NonNull String messageId, long timestamp) {
        Log.i("ContentDb.setOutgoingMessageSeen: chatId=" + chatId + " recipientUserId=" + recipientUserId + " messageId=" + messageId + " timestamp=" + timestamp);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        if (chatId instanceof GroupId) {
            final ContentValues receiptValues = new ContentValues();
            String rawUserId = recipientUserId.rawId();
            receiptValues.put(GroupMessageSeenReceiptsTable.COLUMN_STATE, Message.STATE_OUTGOING_SEEN);
            receiptValues.put(GroupMessageSeenReceiptsTable.COLUMN_USER_ID, rawUserId);
            receiptValues.put(GroupMessageSeenReceiptsTable.COLUMN_TIMESTAMP, timestamp);
            receiptValues.put(GroupMessageSeenReceiptsTable.COLUMN_CONTENT_ID, messageId);
            try {
                int rows = db.updateWithOnConflict(GroupMessageSeenReceiptsTable.TABLE_NAME, receiptValues, GroupMessageSeenReceiptsTable.COLUMN_CONTENT_ID + "=? AND " + GroupMessageSeenReceiptsTable.COLUMN_USER_ID + "=?", new String[]{messageId, rawUserId}, SQLiteDatabase.CONFLICT_IGNORE);
                if (rows == 0) {
                    db.insertWithOnConflict(GroupMessageSeenReceiptsTable.TABLE_NAME, null, receiptValues, SQLiteDatabase.CONFLICT_REPLACE);
                }
            } catch (SQLException ex) {
                Log.e("ContentDb.setOutgoingMessageSeen: failed");
                throw ex;
            }
            Cursor cursor = db.query(GroupMessageSeenReceiptsTable.TABLE_NAME, new String[]{GroupMessageSeenReceiptsTable.COLUMN_USER_ID}, GroupMessageSeenReceiptsTable.COLUMN_CONTENT_ID + "=? AND " + GroupMessageSeenReceiptsTable.COLUMN_STATE + "<" + Message.STATE_OUTGOING_SEEN, new String[]{messageId}, null, null, null);
            int count = cursor.getCount();
            cursor.close();
            if (count > 0) {
                return;
            }
        }
        final ContentValues values = new ContentValues();
        values.put(MessagesTable.COLUMN_STATE, Message.STATE_OUTGOING_SEEN);
        try {
            db.updateWithOnConflict(MessagesTable.TABLE_NAME, values,
                    MessagesTable.COLUMN_CHAT_ID + "=? AND " + MessagesTable.COLUMN_SENDER_USER_ID + "='' AND " + MessagesTable.COLUMN_MESSAGE_ID + "=? AND " + MessagesTable.COLUMN_STATE + " !=" + Message.STATE_OUTGOING_PLAYED,
                    new String[]{chatId.rawId(), messageId},
                    SQLiteDatabase.CONFLICT_ABORT);
        } catch (SQLException ex) {
            Log.e("ContentDb.setOutgoingMessageSeen: failed");
            throw ex;
        }
    }

    @WorkerThread
    void setOutgoingMessagePlayed(@NonNull ChatId chatId, @NonNull UserId recipientUserId, @NonNull String messageId, long timestamp /*TODO (ds): use timestamp in receipts table*/) {
        Log.i("ContentDb.setOutgoingMessagePlayed: chatId=" + chatId + " recipientUserId=" + recipientUserId + " messageId=" + messageId);
        final ContentValues values = new ContentValues();
        values.put(MessagesTable.COLUMN_STATE, Message.STATE_OUTGOING_PLAYED);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        try {
            db.updateWithOnConflict(MessagesTable.TABLE_NAME, values,
                    MessagesTable.COLUMN_CHAT_ID + "=? AND " + MessagesTable.COLUMN_SENDER_USER_ID + "='' AND " + MessagesTable.COLUMN_MESSAGE_ID + "=?",
                    new String [] {chatId.rawId(), messageId},
                    SQLiteDatabase.CONFLICT_ABORT);
        } catch (SQLException ex) {
            Log.e("ContentDb.setOutgoingMessagePlayed: failed");
            throw ex;
        }
    }

    @WorkerThread
    void deleteMessage(long rowId) {
        Log.i("MessagesDb.deleteMessage: messageId=" + rowId);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        final String sql =
                "SELECT " + "c." + ChatsTable.COLUMN_LAST_MESSAGE_ROW_ID + ", " +
                            "m." + MessagesTable._ID + ", " +
                            "m." + MessagesTable.COLUMN_TIMESTAMP + ", " +
                            "m." + MessagesTable.COLUMN_CHAT_ID + " " +
                "FROM " + MessagesTable.TABLE_NAME + " AS m, " + ChatsTable.TABLE_NAME + " AS c " +
                "WHERE " + "m." + MessagesTable.COLUMN_CHAT_ID + "=c." + ChatsTable.COLUMN_CHAT_ID + " " +
                        "AND " + "c." + ChatsTable.COLUMN_CHAT_ID + "=" +
                        "(SELECT " + "m2." + MessagesTable.COLUMN_CHAT_ID + " " +
                        "FROM " + MessagesTable.TABLE_NAME + " AS m2 " +
                        "WHERE " + "m2." + MessagesTable._ID + "=?) " +
                "ORDER BY " + "m." + MessagesTable._ID + " DESC " + " " +
                "LIMIT 1 " +
                "OFFSET 1";

        try (final Cursor cursor = db.rawQuery(sql, new String[]{Long.toString(rowId)})) {
            if (cursor.moveToNext()) {
                long lastRowId = cursor.getLong(0);
                if (rowId == lastRowId) {
                    long newLastRowId = cursor.getLong(1);
                    long timestamp = cursor.getLong(2);
                    String chatId = cursor.getString(3);
                    SQLiteStatement statement = db.compileStatement(
                            "UPDATE " + ChatsTable.TABLE_NAME +
                                " SET " + ChatsTable.COLUMN_TIMESTAMP + "=" + timestamp + " " +
                                ", " + ChatsTable.COLUMN_LAST_MESSAGE_ROW_ID + "=" + newLastRowId + " " +
                                " WHERE " + ChatsTable.COLUMN_CHAT_ID + "='" + chatId + "'");
                    statement.executeUpdateDelete();
                }
            }
        }
        db.delete(MessagesTable.TABLE_NAME, MessagesTable._ID + "=?", new String[]{Long.toString(rowId)});
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    @WorkerThread
    void retractMessage(@NonNull Message message) {
        Log.i("ContentDb.retractMessage: messageId=" + message.id);
        final ContentValues values = new ContentValues();
        values.put(MessagesTable.COLUMN_STATE, message.isOutgoing() ? Message.STATE_INITIAL : Message.STATE_INCOMING_RECEIVED);
        values.put(MessagesTable.COLUMN_TEXT, (String)null);
        values.put(MessagesTable.COLUMN_TYPE, Message.TYPE_RETRACTED);
        values.put(MessagesTable.COLUMN_FAILURE_REASON, (String)null);
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
                values.put(MessagesTable.COLUMN_TYPE, Message.TYPE_RETRACTED);
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

    interface MessageUpdateListener {
        void onMessageUpdated(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId);
    }

    @WorkerThread
    public void processFutureProofMessages(MessageUpdateListener listener) {
        List<FutureProofMessage> futureProofMessages = getFutureProofMessages();
        for (FutureProofMessage futureProofMessage : futureProofMessages) {
            try {
                ChatContainer chatContainer = ChatContainer.parseFrom(futureProofMessage.getProtoBytes());
                Message message = Message.parseFromProto(futureProofMessage.senderUserId, futureProofMessage.id, futureProofMessage.timestamp, chatContainer);
                if (message instanceof FutureProofMessage) {
                    continue;
                } else if (message instanceof ReactionMessage) {
                    Reaction reaction = ((ReactionMessage)message).getReaction();
                    reactionsDb.addReaction(reaction);
                    deleteMessage(futureProofMessage.rowId);
                    listener.onMessageUpdated(futureProofMessage.chatId, futureProofMessage.senderUserId, futureProofMessage.id);
                } else {
                    replaceFutureProofMessage(futureProofMessage, message);
                    listener.onMessageUpdated(message.chatId, message.senderUserId, message.id);
                }
            } catch (InvalidProtocolBufferException e) {
                Log.e("MessagesDb/processFutureProofMessages invalid proto", e);
            }
        }
    }

    @WorkerThread
    private void replaceFutureProofMessage(FutureProofMessage original, Message replacement) {
        long now = System.currentTimeMillis();
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final ContentValues messageValues = new ContentValues();
            messageValues.put(MessagesTable.COLUMN_CHAT_ID, replacement.chatId.rawId());
            messageValues.put(MessagesTable.COLUMN_SENDER_USER_ID, replacement.senderUserId.rawId());
            messageValues.put(MessagesTable.COLUMN_MESSAGE_ID, replacement.id);
            messageValues.put(MessagesTable.COLUMN_TIMESTAMP, replacement.timestamp);
            messageValues.put(MessagesTable.COLUMN_TYPE, replacement.type);
            messageValues.put(MessagesTable.COLUMN_USAGE, replacement.usage);
            messageValues.put(MessagesTable.COLUMN_STATE, replacement.state);
            messageValues.put(MessagesTable.COLUMN_RESULT_UPDATE_TIME, now);
            if (replacement.text != null) {
                messageValues.put(MessagesTable.COLUMN_TEXT, replacement.text);
            }

            db.update(MessagesTable.TABLE_NAME, messageValues, MessagesTable._ID + "=?", new String[]{Long.toString(original.rowId)});
            replacement.rowId = original.rowId;

            for (Media mediaItem : replacement.media) {
                final ContentValues mediaItemValues = new ContentValues();
                mediaItemValues.put(MediaTable.COLUMN_PARENT_TABLE, MessagesTable.TABLE_NAME);
                mediaItemValues.put(MediaTable.COLUMN_PARENT_ROW_ID, replacement.rowId);
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
                if (mediaItem.encSha256hash != null) {
                    mediaItemValues.put(MediaTable.COLUMN_SHA256_HASH, mediaItem.encSha256hash);
                }
                if (mediaItem.decSha256hash != null) {
                    mediaItemValues.put(MediaTable.COLUMN_DEC_SHA256_HASH, mediaItem.decSha256hash);
                }
                mediaItemValues.put(MediaTable.COLUMN_BLOB_VERSION, mediaItem.blobVersion);
                if (mediaItem.blobVersion == Media.BLOB_VERSION_CHUNKED) {
                    mediaItemValues.put(MediaTable.COLUMN_CHUNK_SIZE, mediaItem.chunkSize);
                    mediaItemValues.put(MediaTable.COLUMN_BLOB_SIZE, mediaItem.blobSize);
                }
                mediaItem.rowId = db.insertWithOnConflict(MediaTable.TABLE_NAME, null, mediaItemValues, SQLiteDatabase.CONFLICT_IGNORE);
            }
            mentionsDb.addMentions(replacement);

            futureProofDb.deleteFutureProof(db, original);

            db.setTransactionSuccessful();
            Log.i("ContentDb.replaceFutureProof: updated " + replacement);
        } catch (SQLiteConstraintException ex) {
            Log.w("ContentDb.addMessage: duplicate " + ex.getMessage() + " " + replacement);
        } finally {
            db.endTransaction();
        }
    }

    @WorkerThread
    public List<FutureProofMessage> getFutureProofMessages() {
        final List<FutureProofMessage> messages = new ArrayList<>();
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
                        "m." + MediaTable.COLUMN_DEC_SHA256_HASH + "," +
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
                        MediaTable.COLUMN_DEC_SHA256_HASH + "," +
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
                        "WHERE " + MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_TYPE + "=? " +
                        "ORDER BY " + MessagesTable.TABLE_NAME + "." + MessagesTable._ID + " DESC ";

        try (final Cursor cursor = db.rawQuery(sql, new String[]{Integer.toString(Message.TYPE_FUTURE_PROOF)})) {

            long lastRowId = -1;
            FutureProofMessage message = null;
            while (cursor.moveToNext()) {
                long rowId = cursor.getLong(0);
                if (lastRowId != rowId) {
                    lastRowId = rowId;
                    if (message != null) {
                        messages.add(message);
                    }
                    String rawReplySenderId = cursor.getString(25);
                    message = new FutureProofMessage(
                            rowId,
                            ChatId.fromNullable(cursor.getString(1)),
                            new UserId(cursor.getString(2)),
                            cursor.getString(3),
                            cursor.getLong(4),
                            cursor.getInt(6),
                            cursor.getInt(7),
                            cursor.getString(8),
                            cursor.getString(21),
                            cursor.getInt(22),
                            cursor.getString(23),
                            cursor.getInt(24),
                            rawReplySenderId == null ? null : new UserId(rawReplySenderId),
                            cursor.getInt(9));
                    futureProofDb.fillFutureProof(message);
                }
            }
            if (message != null) {
                messages.add(message);
            }
        }
        return messages;
    }

    @WorkerThread
    @NonNull List<CallMessage> getUnseenCallMessages(int count) {
        final List<CallMessage> messages = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        final String where =
                MessagesTable.TABLE_NAME + "." + MessagesTable._ID + ">=" + ChatsTable.TABLE_NAME + "." + ChatsTable.COLUMN_FIRST_UNSEEN_MESSAGE_ROW_ID + " AND " +
                        MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_CHAT_ID + "=" + ChatsTable.TABLE_NAME + "." + ChatsTable.COLUMN_CHAT_ID + " AND " +
                        ChatsTable.TABLE_NAME + "." + ChatsTable.COLUMN_NEW_MESSAGE_COUNT + ">0 AND " +
                        MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_TYPE + "=" + Message.TYPE_CALL + " AND " +
                        MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_SENDER_USER_ID + "!=''";

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
                        MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_REREQUEST_COUNT + " " +
                        "FROM " + MessagesTable.TABLE_NAME + "," + ChatsTable.TABLE_NAME + " " +
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
                    if (message instanceof CallMessage) {
                        messages.add((CallMessage) message);
                    }
                    message = Message.readFromDb(
                            rowId,
                            ChatId.fromNullable(cursor.getString(1)),
                            new UserId(cursor.getString(2)),
                            cursor.getString(3),
                            cursor.getLong(4),
                            cursor.getInt(5),
                            cursor.getInt(6),
                            cursor.getInt(7),
                            cursor.getString(8),
                            null,
                            -1,
                            null,
                            -1,
                            null,
                            cursor.getInt(9));
                    mentionsDb.fillMentions(message);
                    urlPreviewsDb.fillUrlPreview(message);
                    if (message instanceof CallMessage) {
                        callsDb.fillCallMessage((CallMessage) message);
                    }
                }
            }
            if (message instanceof CallMessage && cursor.getCount() < count) {
                messages.add((CallMessage) message);
            }
        }
        Log.i("ContentDb.getMissedCallMessages: count=" + count + " messages.size=" + messages.size() + (messages.isEmpty() ? "" : (" got messages from " + messages.get(0).timestamp + " to " + messages.get(messages.size()-1).timestamp)));

        return messages;
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
                "m." + MediaTable.COLUMN_BLOB_VERSION + ", " +
                "m." + MediaTable.COLUMN_CHUNK_SIZE + ", " +
                "m." + MediaTable.COLUMN_BLOB_SIZE + ", " +
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
                    MediaTable.COLUMN_TRANSFERRED + "," +
                    MediaTable.COLUMN_BLOB_VERSION + "," +
                    MediaTable.COLUMN_CHUNK_SIZE + "," +
                    MediaTable.COLUMN_BLOB_SIZE + " FROM " + MediaTable.TABLE_NAME + " ORDER BY " + MediaTable._ID + " ASC) " +
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
                    String rawReplySenderId = cursor.getString(25);
                    message = Message.readFromDb(
                            rowId,
                            ChatId.fromNullable(cursor.getString(1)),
                            new UserId(cursor.getString(2)),
                            cursor.getString(3),
                            cursor.getLong(4),
                            cursor.getInt(5),
                            cursor.getInt(6),
                            cursor.getInt(7),
                            cursor.getString(8),
                            cursor.getString(21),
                            cursor.getInt(22),
                            cursor.getString(23),
                            cursor.getInt(24),
                            rawReplySenderId == null ? null : new UserId(rawReplySenderId),
                            cursor.getInt(9));
                    mentionsDb.fillMentions(message);
                    urlPreviewsDb.fillUrlPreview(message);
                    if (message instanceof CallMessage) {
                        callsDb.fillCallMessage((CallMessage) message);
                    }
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
                            cursor.getInt(18),
                            cursor.getInt(19),
                            cursor.getLong(20));
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
    boolean hasMessage(UserId senderUserId, String id) {
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String sql = "SELECT COUNT(*) FROM " + MessagesTable.TABLE_NAME
                + " WHERE " + MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_MESSAGE_ID + "=?"
                + " AND " + MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_SENDER_USER_ID + "=?";
        try (final Cursor cursor = db.rawQuery(sql, new String[]{id, senderUserId.rawId()})) {
            if (cursor.moveToNext()) {
                return cursor.getInt(0) > 0;
            }
        }
        return false;
    }

    @WorkerThread
    @Nullable Message getMessage(String contentId) {
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
                        "m." + MediaTable.COLUMN_BLOB_VERSION + ", " +
                        "m." + MediaTable.COLUMN_CHUNK_SIZE + ", " +
                        "m." + MediaTable.COLUMN_BLOB_SIZE + ", " +
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
                        MediaTable.COLUMN_TRANSFERRED + "," +
                        MediaTable.COLUMN_BLOB_VERSION + "," +
                        MediaTable.COLUMN_CHUNK_SIZE + "," +
                        MediaTable.COLUMN_BLOB_SIZE + " FROM " + MediaTable.TABLE_NAME + " ORDER BY " + MediaTable._ID + " ASC) " +
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
                        "WHERE " + MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_MESSAGE_ID + "=?";
        Message message = null;
        try (final Cursor cursor = db.rawQuery(sql, new String[] {contentId})) {
            while (cursor.moveToNext()) {
                if (message == null) {
                    String rawReplySenderId = cursor.getString(25);
                    message = Message.readFromDb(
                            cursor.getLong(0),
                            ChatId.fromNullable(cursor.getString(1)),
                            new UserId(cursor.getString(2)),
                            cursor.getString(3),
                            cursor.getLong(4),
                            cursor.getInt(5),
                            cursor.getInt(6),
                            cursor.getInt(7),
                            cursor.getString(8),
                            cursor.getString(21),
                            cursor.getInt(22),
                            cursor.getString(23),
                            cursor.getInt(24),
                            rawReplySenderId == null ? null : new UserId(rawReplySenderId),
                            cursor.getInt(9));
                    mentionsDb.fillMentions(message);
                    urlPreviewsDb.fillUrlPreview(message);
                    if (message instanceof CallMessage) {
                        callsDb.fillCallMessage((CallMessage) message);
                    }
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
                            cursor.getInt(18),
                            cursor.getInt(19),
                            cursor.getLong(20));
                    media.encFile = fileStore.getTmpFile(cursor.getString(14));
                    Preconditions.checkNotNull(message).media.add(media);
                }
            }
        }
        return message;
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
                "m." + MediaTable.COLUMN_BLOB_VERSION + ", " +
                "m." + MediaTable.COLUMN_CHUNK_SIZE + ", " +
                "m." + MediaTable.COLUMN_BLOB_SIZE + ", " +
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
                    MediaTable.COLUMN_TRANSFERRED + "," +
                    MediaTable.COLUMN_BLOB_VERSION + "," +
                    MediaTable.COLUMN_CHUNK_SIZE + "," +
                    MediaTable.COLUMN_BLOB_SIZE + " FROM " + MediaTable.TABLE_NAME + " ORDER BY " + MediaTable._ID + " ASC) " +
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
                    String rawReplySenderId = cursor.getString(25);
                    message = Message.readFromDb(
                            cursor.getLong(0),
                            ChatId.fromNullable(cursor.getString(1)),
                            new UserId(cursor.getString(2)),
                            cursor.getString(3),
                            cursor.getLong(4),
                            cursor.getInt(5),
                            cursor.getInt(6),
                            cursor.getInt(7),
                            cursor.getString(8),
                            cursor.getString(21),
                            cursor.getInt(22),
                            cursor.getString(23),
                            cursor.getInt(24),
                            rawReplySenderId == null ? null : new UserId(rawReplySenderId),
                            cursor.getInt(9));
                    mentionsDb.fillMentions(message);
                    urlPreviewsDb.fillUrlPreview(message);
                    if (message instanceof CallMessage) {
                        callsDb.fillCallMessage((CallMessage) message);
                    }
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
                            cursor.getInt(18),
                            cursor.getInt(19),
                            cursor.getLong(20));
                    media.encFile = fileStore.getTmpFile(cursor.getString(14));
                    Preconditions.checkNotNull(message).media.add(media);
                }
            }
        }
        return message;
    }

    @WorkerThread
    @Nullable Message getMessage(@Nullable ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        final String chatQuery = (chatId == null) ? "" : (MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_CHAT_ID + "=\"" + chatId.rawId() + "\" AND ");
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
                "m." + MediaTable.COLUMN_DEC_SHA256_HASH + "," +
                "m." + MediaTable.COLUMN_BLOB_VERSION + ", " +
                "m." + MediaTable.COLUMN_CHUNK_SIZE + "," +
                "m." + MediaTable.COLUMN_BLOB_SIZE + "," +
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
                    MediaTable.COLUMN_DEC_SHA256_HASH + "," +
                    MediaTable.COLUMN_TRANSFERRED + "," +
                    MediaTable.COLUMN_BLOB_VERSION + "," +
                    MediaTable.COLUMN_CHUNK_SIZE + "," +
                    MediaTable.COLUMN_BLOB_SIZE + " FROM " + MediaTable.TABLE_NAME + " ORDER BY " + MediaTable._ID + " ASC) " +
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
            "WHERE " + chatQuery +
                    MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_SENDER_USER_ID + "=\"" + senderUserId.rawId() + "\" AND " +
                    MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_MESSAGE_ID + "=\"" + messageId + "\"";
        Message message = null;
        try (final Cursor cursor = db.rawQuery(sql, null)) {
            while (cursor.moveToNext()) {
                if (message == null) {
                    String rawReplySenderId = cursor.getString(28);
                    message = Message.readFromDb(
                            cursor.getLong(0),
                            ChatId.fromNullable(cursor.getString(1)),
                            new UserId(cursor.getString(2)),
                            cursor.getString(3),
                            cursor.getLong(4),
                            cursor.getInt(5),
                            cursor.getInt(6),
                            cursor.getInt(7),
                            cursor.getString(8),
                            cursor.getString(24),
                            cursor.getInt(25),
                            cursor.getString(26),
                            cursor.getInt(27),
                            rawReplySenderId == null ? null : new UserId(rawReplySenderId),
                            cursor.getInt(9));
                    mentionsDb.fillMentions(message);
                    urlPreviewsDb.fillUrlPreview(message);
                    if (message instanceof CallMessage) {
                        callsDb.fillCallMessage((CallMessage) message);
                    }
                }
                if (!cursor.isNull(10)) {
                    Media media = new Media(
                            cursor.getLong(10),
                            cursor.getInt(11),
                            cursor.getString(12),
                            fileStore.getMediaFile(cursor.getString(13)),
                            cursor.getBlob(18),
                            cursor.getBlob(19),
                            cursor.getBlob(20),
                            cursor.getInt(15),
                            cursor.getInt(16),
                            cursor.getInt(17),
                            cursor.getInt(21),
                            cursor.getInt(22),
                            cursor.getLong(23));
                    media.encFile = fileStore.getTmpFile(cursor.getString(14));
                    Preconditions.checkNotNull(message).media.add(media);
                }
            }
        }
        return message;
    }

    @WorkerThread
    int getMessageRerequestCount(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
        Log.i("MessagesDb.getMessageRerequestCount: chatId=" + chatId + "senderUserId=" + senderUserId + " messageId=" + messageId);

        String sql = "SELECT " + MessagesTable.COLUMN_REREQUEST_COUNT + " "
                + "FROM " + MessagesTable.TABLE_NAME + " "
                + "WHERE " + MessagesTable.COLUMN_CHAT_ID + "=? AND " + MessagesTable.COLUMN_SENDER_USER_ID + "=? AND " + MessagesTable.COLUMN_MESSAGE_ID + "=?";

        int count = 0;
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(sql, new String[]{chatId.rawId(), senderUserId.rawId(), messageId})) {
            if (cursor.moveToNext()) {
                return cursor.getInt(0);
            }
        } catch (SQLException ex) {
            Log.e("MessagesDb.getMessageRerequestCount: failed");
            throw ex;
        }
        return count;
    }

    @WorkerThread
    @Nullable Message getMessageForMedia(long mediaRowId) {
        String sql =
            "SELECT " + MediaTable.COLUMN_PARENT_ROW_ID + " " +
            "FROM " + MediaTable.TABLE_NAME + " " +
            "WHERE " + MediaTable._ID + "=" + mediaRowId + " AND " + MediaTable.COLUMN_PARENT_TABLE + "='" + MessagesTable.TABLE_NAME + "'";

        long messageRowId;
        try {
            messageRowId = DatabaseUtils.longForQuery(databaseHelper.getReadableDatabase(), sql, null);
        } catch (SQLiteDoneException e) {
            Log.w("ContentDb.getMessageForMedia: Unable to get message media with id " + mediaRowId + " due to " + e.getMessage());
            return null;
        }

        return getMessage(messageRowId);
    }

    @WorkerThread
    @NonNull List<Message> getMessages(@NonNull ChatId chatId, @Nullable Long startRowId, @Nullable Integer count, boolean after) {
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
                "m." + MediaTable.COLUMN_BLOB_VERSION + ", " +
                "m." + MediaTable.COLUMN_CHUNK_SIZE + ", " +
                "m." + MediaTable.COLUMN_BLOB_SIZE + ", " +
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
                    MediaTable.COLUMN_TRANSFERRED + "," +
                    MediaTable.COLUMN_BLOB_VERSION + "," +
                    MediaTable.COLUMN_CHUNK_SIZE + "," +
                    MediaTable.COLUMN_BLOB_SIZE + " FROM " + MediaTable.TABLE_NAME + " ORDER BY " + MediaTable._ID + " ASC) " +
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
            (count == null ? "" : ("LIMIT " + count));

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
                    String rawReplySenderId = cursor.getString(25);
                    message = Message.readFromDb(
                            rowId,
                            ChatId.fromNullable(cursor.getString(1)),
                            new UserId(cursor.getString(2)),
                            cursor.getString(3),
                            cursor.getLong(4),
                            cursor.getInt(5),
                            cursor.getInt(6),
                            cursor.getInt(7),
                            cursor.getString(8),
                            cursor.getString(21),
                            cursor.getInt(22),
                            cursor.getString(23),
                            cursor.getInt(24),
                            rawReplySenderId == null ? null : new UserId(rawReplySenderId),
                            cursor.getInt(9));
                    mentionsDb.fillMentions(message);
                    urlPreviewsDb.fillUrlPreview(message);

                    if (message instanceof CallMessage) {
                        callsDb.fillCallMessage((CallMessage) message);
                    }
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
                            cursor.getInt(18),
                            cursor.getInt(19),
                            cursor.getLong(20));
                    media.encFile = fileStore.getTmpFile(cursor.getString(14));
                    Preconditions.checkNotNull(message).media.add(media);
                }
            }
            if (message != null && (count == null || cursor.getCount() < count)) {
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
    void countMessageContactFrequencySinceTimestamp(long cutoffTimestamp, @NonNull Map<ChatId, Integer> contactFrequencyMap) {
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        final String sql =
            "SELECT " + MessagesTable.COLUMN_CHAT_ID + " " +
            "FROM " + MessagesTable.TABLE_NAME + " " +
            "WHERE " +
                MessagesTable.COLUMN_CHAT_ID + " IS NOT NULL AND " +
                MessagesTable.COLUMN_TIMESTAMP + ">" + cutoffTimestamp + " AND " +
                MessagesTable.COLUMN_SENDER_USER_ID + "=?";

        try (final Cursor cursor = db.rawQuery(sql, new String[] {UserId.ME.rawId()})) {
            while (cursor.moveToNext()) {
                ChatId chatId = ChatId.fromNullable(cursor.getString(0));
                if (chatId != null) {
                    Integer frequency = contactFrequencyMap.get(chatId);
                    frequency = frequency != null ? frequency + 1 : 1;
                    contactFrequencyMap.put(chatId, frequency);
                }
            }
        }
        Log.i("ContentDb.countMessageContactFrequency: cutoffTimestamp=" + cutoffTimestamp);
    }

    @WorkerThread
    @NonNull List<Media> getChatMedia(@NonNull ChatId chatId, @Nullable Long startRowId, int count, boolean after) {
        final ArrayList<Media> items = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String where = MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_CHAT_ID + "='" + chatId.rawId() + "' AND m." + MediaTable.COLUMN_TYPE + " != " + Media.MEDIA_TYPE_AUDIO;
        if (startRowId != null) {
            where += " AND m." + MediaTable._ID + (after ? ">" : "<") + startRowId;
        }

        final String sql =
            "SELECT " +
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
            "FROM " + MessagesTable.TABLE_NAME + " " +
            "INNER JOIN " + MediaTable.TABLE_NAME + " " +
                "AS m ON " + MessagesTable.TABLE_NAME + "." + MessagesTable._ID + "=m." + MediaTable.COLUMN_PARENT_ROW_ID + " AND '" + MessagesTable.TABLE_NAME + "'=m." + MediaTable.COLUMN_PARENT_TABLE + " " +
            "WHERE " + where + " " +
            "ORDER BY m." + MediaTable._ID + (after ? " ASC " : " DESC ") +
            "LIMIT " + count;

        try (final Cursor cursor = db.rawQuery(sql, null)) {
            while (cursor.moveToNext()) {
                Media media = new Media(
                    cursor.getLong(0),
                    cursor.getInt(1),
                    cursor.getString(2),
                    fileStore.getMediaFile(cursor.getString(3)),
                    null,
                    null,
                    null,
                    cursor.getInt(5),
                    cursor.getInt(6),
                    cursor.getInt(7),
                    cursor.getInt(8),
                    cursor.getInt(9),
                    cursor.getLong(10));
                media.encFile = fileStore.getTmpFile(cursor.getString(4));

                items.add(media);
            }
        }

        if (!after) {
            Collections.reverse(items);
        }

        Log.i("ContentDb.getChatMedia: start=" + startRowId + " count=" + count + " after=" + after + " media.size=" + items.size());

        return items;
    }

    @WorkerThread
    long getChatMediaPosition(ChatId chatId, long rowId) {
        String where = MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_CHAT_ID + "='" + chatId.rawId() + "' AND m."  + MediaTable.COLUMN_TYPE + " != " + Media.MEDIA_TYPE_AUDIO + " AND m." + MediaTable._ID + "<" + rowId;
        String sql =
            "SELECT COUNT(*) FROM " + MessagesTable.TABLE_NAME + " " +
            "INNER JOIN " + MediaTable.TABLE_NAME + " " +
            "AS m ON " + MessagesTable.TABLE_NAME + "." + MessagesTable._ID + "=m." + MediaTable.COLUMN_PARENT_ROW_ID + " AND '" + MessagesTable.TABLE_NAME + "'=m." + MediaTable.COLUMN_PARENT_TABLE + " " +
            "WHERE " + where + " ";
        return DatabaseUtils.longForQuery(databaseHelper.getReadableDatabase(), sql, null);
    }

    @WorkerThread
    long getChatMediaCount(ChatId chatId) {
        String where = MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_CHAT_ID + "='" + chatId.rawId() + "' AND m." + MediaTable.COLUMN_TYPE + " != " + Media.MEDIA_TYPE_AUDIO;
        String sql =
                "SELECT COUNT(*) FROM " + MessagesTable.TABLE_NAME + " " +
                        "INNER JOIN " + MediaTable.TABLE_NAME + " " +
                        "AS m ON " + MessagesTable.TABLE_NAME + "." + MessagesTable._ID + "=m." + MediaTable.COLUMN_PARENT_ROW_ID + " AND '" + MessagesTable.TABLE_NAME + "'=m." + MediaTable.COLUMN_PARENT_TABLE + " " +
                        "WHERE " + where + " ";
        return DatabaseUtils.longForQuery(databaseHelper.getReadableDatabase(), sql, null);
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
                    "m." + MediaTable.COLUMN_DEC_SHA256_HASH + "," +
                    "m." + MediaTable.COLUMN_WIDTH + "," +
                    "m." + MediaTable.COLUMN_HEIGHT + "," +
                    "m." + MediaTable.COLUMN_TRANSFERRED + ", " +
                    "m." + MediaTable.COLUMN_BLOB_VERSION + "," +
                    "m." + MediaTable.COLUMN_CHUNK_SIZE + "," +
                    "m." + MediaTable.COLUMN_BLOB_SIZE + "," +
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
                        MediaTable.COLUMN_DEC_SHA256_HASH + "," +
                        MediaTable.COLUMN_WIDTH + "," +
                        MediaTable.COLUMN_HEIGHT + "," +
                        MediaTable.COLUMN_TRANSFERRED + "," +
                        MediaTable.COLUMN_BLOB_VERSION + "," +
                        MediaTable.COLUMN_CHUNK_SIZE + "," +
                        MediaTable.COLUMN_BLOB_SIZE + " FROM " + MediaTable.TABLE_NAME + " ORDER BY " + MediaTable._ID + " ASC) " +
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
                    String rawReplySenderId = cursor.getString(28);
                    message = Message.readFromDb(
                            rowId,
                            ChatId.fromNullable(cursor.getString(1)),
                            new UserId(cursor.getString(2)),
                            cursor.getString(3),
                            cursor.getLong(4),
                            cursor.getInt(5),
                            cursor.getInt(6),
                            cursor.getInt(7),
                            cursor.getString(8),
                            cursor.getString(24),
                            cursor.getInt(25),
                            cursor.getString(26),
                            cursor.getInt(27),
                            rawReplySenderId == null ? null : new UserId(rawReplySenderId),
                            cursor.getInt(9));
                    mentionsDb.fillMentions(message);
                    urlPreviewsDb.fillUrlPreview(message);
                    if (message instanceof CallMessage) {
                        callsDb.fillCallMessage((CallMessage) message);
                    }
                }
                if (!cursor.isNull(10)) {
                    Media media = new Media(
                            cursor.getLong(10),
                            cursor.getInt(11),
                            cursor.getString(12),
                            fileStore.getMediaFile(cursor.getString(13)),
                            cursor.getBlob(15),
                            cursor.getBlob(16),
                            cursor.getBlob(17),
                            cursor.getInt(18),
                            cursor.getInt(19),
                            cursor.getInt(20),
                            cursor.getInt(21),
                            cursor.getInt(22),
                            cursor.getLong(23));
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
                        RepliesTable.COLUMN_MEDIA_PREVIEW_FILE,
                        RepliesTable.COLUMN_POST_TYPE},
                RepliesTable.COLUMN_MESSAGE_ROW_ID + "=?",
                new String [] {String.valueOf(messageRowId)}, null, null, null)) {
            if (cursor.moveToNext()) {
                Integer type = null;
                if (!cursor.isNull(4)) {
                    type = cursor.getInt(4);
                }
                ReplyPreview replyPreview = new ReplyPreview(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getInt(2),
                        fileStore.getMediaFile(cursor.getString(3)),
                        type);
                mentionsDb.fillMentions(replyPreview);
                return replyPreview;
            }
        }
        return null;
    }

    @WorkerThread
    List<File> getReplyMediaFiles() {
        List<File> ret = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.query(RepliesTable.TABLE_NAME,
                new String [] {RepliesTable.COLUMN_MEDIA_PREVIEW_FILE}, null, null, null, null, null)) {
            while (cursor.moveToNext()) {
                ret.add(fileStore.getMediaFile(cursor.getString(0)));
            }
        }
        return ret;
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

        final String nameSaveSql =
                "SELECT " + ChatsTable.COLUMN_CHAT_NAME +
                        " FROM " + ChatsTable.TABLE_NAME +
                        " WHERE " + ChatsTable.COLUMN_CHAT_ID + "='" + chatId.rawId() + "'";
        try (final Cursor cursor = db.rawQuery(nameSaveSql, null)) {
            while (cursor.moveToNext()) {
                ContentValues values = new ContentValues();
                values.put(DeletedGroupNameTable.COLUMN_CHAT_NAME, cursor.getString(0));
                values.put(DeletedGroupNameTable.COLUMN_CHAT_ID, chatId.rawId());
                db.insert(DeletedGroupNameTable.TABLE_NAME, null, values);
            }
        }
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
    boolean setMessagePlayed(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        Log.i("ContentDb.setMessagePlayed: chatId=" + chatId + " senderUserId=" + senderUserId + " messageId=" + messageId);
        final ContentValues msgValues = new ContentValues();
        msgValues.put(MessagesTable.COLUMN_STATE, Message.STATE_INCOMING_PLAYED);
        int rowsUpdated;
        try {
            rowsUpdated = db.updateWithOnConflict(MessagesTable.TABLE_NAME, msgValues,
                    MessagesTable.COLUMN_CHAT_ID + "=? AND " + MessagesTable.COLUMN_SENDER_USER_ID + "=? AND " + MessagesTable.COLUMN_MESSAGE_ID + "=? AND " + MessagesTable.COLUMN_STATE + "!=" + Message.STATE_INCOMING_PLAYED,
                    new String [] {chatId.rawId(), senderUserId.rawId(), messageId},
                    SQLiteDatabase.CONFLICT_ABORT);
        } catch (SQLException ex) {
            Log.e("ContentDb.setIncomingMessageSeen: failed");
            throw ex;
        }
        if (rowsUpdated == 0) {
            return false;
        }
        final ContentValues receiptValues = new ContentValues();
        receiptValues.put(OutgoingPlayedReceiptsTable.COLUMN_CHAT_ID, chatId.rawId());
        receiptValues.put(OutgoingPlayedReceiptsTable.COLUMN_SENDER_USER_ID, senderUserId.rawId());
        receiptValues.put(OutgoingPlayedReceiptsTable.COLUMN_CONTENT_ITEM_ID, messageId);
        Log.i("ContentDb.setMessagePlayed: " + chatId + " " + senderUserId + " " + messageId);
        return -1 != db.insert(OutgoingPlayedReceiptsTable.TABLE_NAME, null, receiptValues);
    }

    @WorkerThread
    void setMessagePlayedReceiptSent(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
        final int deleteCount = databaseHelper.getWritableDatabase().delete(OutgoingPlayedReceiptsTable.TABLE_NAME,
                OutgoingPlayedReceiptsTable.COLUMN_CHAT_ID + "=? AND " + OutgoingPlayedReceiptsTable.COLUMN_SENDER_USER_ID + "=? AND " + OutgoingPlayedReceiptsTable.COLUMN_CONTENT_ITEM_ID + "=?",
                new String [] {chatId.rawId(), senderUserId.rawId(), messageId});
        Log.i("ContentDb.setMessagePlayedReceiptSent: delete " + deleteCount + " rows for " + chatId + " " + senderUserId + " " + messageId);
    }

    @WorkerThread
    @NonNull List<Chat> getChats(boolean includeChats, boolean includeGroups) {
        final List<Chat> chats = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String selection;
        if (includeGroups && includeChats) {
            selection = "";
        } else if (includeGroups) {
            selection = ChatsTable.COLUMN_IS_GROUP + "=1";
        } else {
            selection = ChatsTable.COLUMN_IS_GROUP + "=0";
        }
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
                        ChatsTable.COLUMN_IS_ACTIVE,
                        ChatsTable.COLUMN_THEME,
                        ChatsTable.COLUMN_INVITE_LINK},
                selection, null, null, null, ChatsTable.COLUMN_TIMESTAMP + " DESC")) {
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
                        cursor.getInt(10) == 1,
                        cursor.getInt(11));
                chat.inviteToken = cursor.getString(12);
                if (!serverProps.getGroupChatsEnabled() && chat.isGroup) {
                    continue;
                }
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
                        ChatsTable.COLUMN_IS_ACTIVE,
                        ChatsTable.COLUMN_THEME,
                        ChatsTable.COLUMN_INVITE_LINK},
                ChatsTable.COLUMN_CHAT_ID + "=?",
                new String [] {chatId.rawId()},
                null, null, null)) {
            if (cursor.moveToNext()) {
                Chat chat = new Chat(
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
                        cursor.getInt(10) == 1,
                        cursor.getInt(11));
                chat.inviteToken = cursor.getString(12);
                return chat;
            }
        }
        return null;
    }

    @WorkerThread
    @Nullable String getDeletedChatName(@NonNull ChatId chatId) {
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.query(DeletedGroupNameTable.TABLE_NAME,
                new String [] {DeletedGroupNameTable.COLUMN_CHAT_NAME},
                DeletedGroupNameTable.COLUMN_CHAT_ID + "=?",
                new String [] {chatId.rawId()}, null, null, null)) {
            if (cursor.moveToNext()) {
                return cursor.getString(0);
            }
        }
        return null;
    }

    @WorkerThread
    int getUnseenChatsCount() {
        String query = "SELECT count(*) FROM " + ChatsTable.TABLE_NAME + " WHERE " + ChatsTable.COLUMN_NEW_MESSAGE_COUNT + ">0";
        if (!serverProps.getGroupChatsEnabled()) {
            query += " AND " + ChatsTable.COLUMN_IS_GROUP + "!=1";
        }
        return (int)DatabaseUtils.longForQuery(databaseHelper.getReadableDatabase(), query, null);
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

    @WorkerThread
    List<PlayedReceipt> getPendingMessagePlayedReceipts() {
        final List<PlayedReceipt> receipts = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.query(OutgoingPlayedReceiptsTable.TABLE_NAME,
                new String [] {
                        OutgoingPlayedReceiptsTable.COLUMN_CHAT_ID,
                        OutgoingPlayedReceiptsTable.COLUMN_SENDER_USER_ID,
                        OutgoingPlayedReceiptsTable.COLUMN_CONTENT_ITEM_ID},
                null, null, null, null, null)) {
            while (cursor.moveToNext()) {
                final PlayedReceipt receipt = new PlayedReceipt(
                        ChatId.fromNullable(cursor.getString(0)),
                        new UserId(cursor.getString(1)),
                        cursor.getString(2));
                receipts.add(receipt);
            }
        }
        Log.i("ContentDb.getPendingMessagePlayedReceipts: receipts.size=" + receipts.size());
        return receipts;
    }


    @WorkerThread
    public List<DecryptStats> getMessageDecryptStats(long lastRowId) {
        List<DecryptStats> ret = new ArrayList<>();
        final String sql =
                "SELECT " + MessagesTable.TABLE_NAME + "." + MessagesTable._ID + ","
                        + MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_MESSAGE_ID + ","
                        + MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_REREQUEST_COUNT + ","
                        + MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_FAILURE_REASON + ","
                        + MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_CLIENT_VERSION + ","
                        + MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_SENDER_VERSION + ","
                        + MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_SENDER_PLATFORM + ","
                        + MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_RECEIVE_TIME + ","
                        + MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_RESULT_UPDATE_TIME + ","
                        + MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_CHAT_ID
                        + " FROM " + MessagesTable.TABLE_NAME
                        + " WHERE " + MessagesTable.TABLE_NAME + "." + MessagesTable._ID + " > ?"
                        + " AND " + MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_SENDER_USER_ID + "<>''";

        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.rawQuery(sql, new String[]{Long.toString(lastRowId)})) {
            while (cursor.moveToNext()) {
                ret.add(new DecryptStats(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getInt(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getLong(7),
                        cursor.getLong(8),
                        false,
                        ChatId.fromNullable(cursor.getString(9))
                ));
            }
        }
        return ret;
    }

    @WorkerThread
    public DecryptStats getMessageDecryptStats(String messageId) {
        final String sql =
                "SELECT " + MessagesTable.TABLE_NAME + "." + MessagesTable._ID + ","
                        + MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_MESSAGE_ID + ","
                        + MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_REREQUEST_COUNT + ","
                        + MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_FAILURE_REASON + ","
                        + MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_CLIENT_VERSION + ","
                        + MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_SENDER_VERSION + ","
                        + MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_SENDER_PLATFORM + ","
                        + MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_RECEIVE_TIME + ","
                        + MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_RESULT_UPDATE_TIME + ","
                        + MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_CHAT_ID
                        + " FROM " + MessagesTable.TABLE_NAME
                        + " WHERE " + MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_MESSAGE_ID + "=?";

        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.rawQuery(sql, new String[]{messageId})) {
            if (cursor.moveToNext()) {
                return new DecryptStats(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getInt(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getLong(7),
                        cursor.getLong(8),
                        false,
                        ChatId.fromNullable(cursor.getString(9))
                );
            }
        }
        return null;
    }
}

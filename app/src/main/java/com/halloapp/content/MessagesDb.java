package com.halloapp.content;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.halloapp.FileStore;
import com.halloapp.contacts.UserId;
import com.halloapp.content.tables.ChatsTable;
import com.halloapp.content.tables.MediaTable;
import com.halloapp.content.tables.MessagesTable;
import com.halloapp.content.tables.OutgoingSeenReceiptsTable;
import com.halloapp.media.MediaUtils;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class MessagesDb {

    private final ContentDbHelper databaseHelper;
    private final FileStore fileStore;

    MessagesDb(ContentDbHelper databaseHelper, FileStore fileStore) {
        this.databaseHelper = databaseHelper;
        this.fileStore = fileStore;
    }

    @WorkerThread
    boolean addMessage(@NonNull Message message, boolean unseen) {
        Log.i("ContentDb.addMessage " + message + " " + unseen);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final ContentValues messageValues = new ContentValues();
            messageValues.put(MessagesTable.COLUMN_CHAT_ID, message.chatId);
            messageValues.put(MessagesTable.COLUMN_SENDER_USER_ID, message.senderUserId.rawId());
            messageValues.put(MessagesTable.COLUMN_MESSAGE_ID, message.id);
            messageValues.put(MessagesTable.COLUMN_TIMESTAMP, message.timestamp);
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
            final int updatedRowsCount;
            try (SQLiteStatement statement = db.compileStatement("UPDATE " + ChatsTable.TABLE_NAME + " SET " +
                    ChatsTable.COLUMN_TIMESTAMP + "=" + message.timestamp + ", " +
                    (unseen ? (ChatsTable.COLUMN_NEW_MESSAGE_COUNT + "=" + ChatsTable.COLUMN_NEW_MESSAGE_COUNT + "+1, ") : "") +
                    (unseen ? (ChatsTable.COLUMN_FIRST_UNSEEN_MESSAGE_ROW_ID + "=CASE WHEN " + ChatsTable.COLUMN_FIRST_UNSEEN_MESSAGE_ROW_ID + ">= 0 THEN " + ChatsTable.COLUMN_FIRST_UNSEEN_MESSAGE_ROW_ID + " ELSE " + message.rowId + " END, ") : "") +
                    ChatsTable.COLUMN_LAST_MESSAGE_ROW_ID + "=" + message.rowId + " " +
                    "WHERE " + ChatsTable.COLUMN_CHAT_ID + "=" + message.chatId)) {
                updatedRowsCount = statement.executeUpdateDelete();
            }
            if (updatedRowsCount == 0) {
                final ContentValues chatValues = new ContentValues();
                chatValues.put(ChatsTable.COLUMN_CHAT_ID, message.chatId);
                chatValues.put(ChatsTable.COLUMN_TIMESTAMP, message.timestamp);
                chatValues.put(ChatsTable.COLUMN_LAST_MESSAGE_ROW_ID, message.rowId);
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
    void setMediaTransferred(@NonNull Message message, @NonNull Media media) {
        Log.i("ContentDb.setMediaTransferred: message=" + message + " media=" + media);
        final ContentValues values = new ContentValues();
        values.put(MediaTable.COLUMN_FILE, media.file == null ? null : media.file.getName());
        values.put(MediaTable.COLUMN_URL, media.url);
        if (media.encKey != null) {
            values.put(MediaTable.COLUMN_ENC_KEY, media.encKey);
        }
        if (media.sha256hash != null) {
            values.put(MediaTable.COLUMN_SHA256_HASH, media.sha256hash);
        }
        if (media.width == 0 || media.height == 0) {
            final Size dimensions = MediaUtils.getDimensions(media.file, media.type);
            if (dimensions != null && dimensions.getWidth() > 0 && dimensions.getHeight() > 0) {
                values.put(MediaTable.COLUMN_WIDTH, dimensions.getWidth());
                values.put(MediaTable.COLUMN_HEIGHT, dimensions.getHeight());
            }
        }
        values.put(MediaTable.COLUMN_TRANSFERRED, true);
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
    void setMessageTransferred(@NonNull String chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
        Log.i("ContentDb.setMessageTransferred: chatId=" + chatId + "senderUserId=" + senderUserId + " messageId=" + messageId);
        final ContentValues values = new ContentValues();
        values.put(MessagesTable.COLUMN_STATE, senderUserId.isMe() ? Message.STATE_OUTGOING_SENT : Message.STATE_INCOMING_RECEIVED);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        try {
            db.updateWithOnConflict(MessagesTable.TABLE_NAME, values,
                    MessagesTable.COLUMN_CHAT_ID + "=? AND " + MessagesTable.COLUMN_SENDER_USER_ID + "=? AND " + MessagesTable.COLUMN_MESSAGE_ID + "=?",
                    new String [] {chatId, senderUserId.rawId(), messageId},
                    SQLiteDatabase.CONFLICT_ABORT);
        } catch (SQLException ex) {
            Log.e("ContentDb.setMessageTransferred: failed");
            throw ex;
        }
    }

    @WorkerThread
    void setOutgoingMessageDelivered(@NonNull String chatId, @NonNull UserId recipientUserId, @NonNull String messageId, long timestamp /*TODO (ds): use timestamp in receipts table*/) {
        Log.i("ContentDb.setOutgoingMessageDelivered: chatId=" + chatId + " recipientUserId=" + recipientUserId + " messageId=" + messageId);
        final ContentValues values = new ContentValues();
        values.put(MessagesTable.COLUMN_STATE, Message.STATE_OUTGOING_DELIVERED);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        try {
            db.updateWithOnConflict(MessagesTable.TABLE_NAME, values,
                    MessagesTable.COLUMN_CHAT_ID + "=? AND " + MessagesTable.COLUMN_SENDER_USER_ID + "='' AND " + MessagesTable.COLUMN_MESSAGE_ID + "=? AND " + MessagesTable.COLUMN_STATE + "<" + Message.STATE_OUTGOING_DELIVERED,
                    new String [] {chatId, messageId},
                    SQLiteDatabase.CONFLICT_ABORT);
        } catch (SQLException ex) {
            Log.e("ContentDb.setOutgoingMessageDelivered: failed");
            throw ex;
        }
    }

    @WorkerThread
    void setOutgoingMessageSeen(@NonNull String chatId, @NonNull UserId recipientUserId, @NonNull String messageId, long timestamp /*TODO (ds): use timestamp in receipts table*/) {
        Log.i("ContentDb.setOutgoingMessageSeen: chatId=" + chatId + " recipientUserId=" + recipientUserId + " messageId=" + messageId);
        final ContentValues values = new ContentValues();
        values.put(MessagesTable.COLUMN_STATE, Message.STATE_OUTGOING_SEEN);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        try {
            db.updateWithOnConflict(MessagesTable.TABLE_NAME, values,
                    MessagesTable.COLUMN_CHAT_ID + "=? AND " + MessagesTable.COLUMN_SENDER_USER_ID + "='' AND " + MessagesTable.COLUMN_MESSAGE_ID + "=?",
                    new String [] {chatId, messageId},
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
                    new String[]{message.chatId, message.senderUserId.rawId(), message.id},
                    SQLiteDatabase.CONFLICT_ABORT);
            if (updatedCount == 0) {
                values.put(MessagesTable.COLUMN_CHAT_ID, message.chatId);
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
    @NonNull List<Message> getUnseenMessages(long timestamp, int count) {
        final List<Message> messages = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        final String where =
                MessagesTable.TABLE_NAME + "." + MessagesTable._ID + ">=" + ChatsTable.TABLE_NAME + "." + ChatsTable.COLUMN_FIRST_UNSEEN_MESSAGE_ROW_ID + " AND " +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_CHAT_ID + "=" + ChatsTable.TABLE_NAME + "." + ChatsTable.COLUMN_CHAT_ID + " AND " +
                ChatsTable.TABLE_NAME + "." + ChatsTable.COLUMN_NEW_MESSAGE_COUNT + ">0 AND " +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_SENDER_USER_ID + "!='' AND " +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_TIMESTAMP + ">" + timestamp;

        String sql =
            "SELECT " +
                MessagesTable.TABLE_NAME + "." + MessagesTable._ID + "," +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_CHAT_ID + ", " +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_SENDER_USER_ID + "," +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_MESSAGE_ID + "," +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_TIMESTAMP + "," +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_STATE + "," +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_TEXT + "," +
                "m." + MediaTable._ID + "," +
                "m." + MediaTable.COLUMN_TYPE + "," +
                "m." + MediaTable.COLUMN_URL + "," +
                "m." + MediaTable.COLUMN_FILE + "," +
                "m." + MediaTable.COLUMN_WIDTH + "," +
                "m." + MediaTable.COLUMN_HEIGHT + "," +
                "m." + MediaTable.COLUMN_TRANSFERRED + " " +
            "FROM " + MessagesTable.TABLE_NAME + "," + ChatsTable.TABLE_NAME + " " +
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
                "AS m ON " + MessagesTable.TABLE_NAME + "." + MessagesTable._ID + "=m." + MediaTable.COLUMN_PARENT_ROW_ID + " AND '" + MessagesTable.TABLE_NAME + "'=m." + MediaTable.COLUMN_PARENT_TABLE + " " +
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
                    message = new Message(
                            rowId,
                            cursor.getString(1),
                            new UserId(cursor.getString(2)),
                            cursor.getString(3),
                            cursor.getLong(4),
                            cursor.getInt(5),
                            cursor.getString(6));
                }
                if (!cursor.isNull(7)) {
                    Preconditions.checkNotNull(message).media.add(new Media(
                            cursor.getLong(7),
                            cursor.getInt(8),
                            cursor.getString(9),
                            fileStore.getMediaFile(cursor.getString(10)),
                            null,
                            null,
                            cursor.getInt(11),
                            cursor.getInt(12),
                            cursor.getInt(13) == 1));
                }
            }
            if (message != null && cursor.getCount() < count) {
                messages.add(message);
            }
        }
        Log.i("ContentDb.getUnseenMessages: start=" + timestamp + " count=" + count + " messages.size=" + messages.size() + (messages.isEmpty() ? "" : (" got messages from " + messages.get(0).timestamp + " to " + messages.get(messages.size()-1).timestamp)));

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
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_STATE + "," +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_TEXT + "," +
                "m." + MediaTable._ID + "," +
                "m." + MediaTable.COLUMN_TYPE + "," +
                "m." + MediaTable.COLUMN_URL + "," +
                "m." + MediaTable.COLUMN_FILE + "," +
                "m." + MediaTable.COLUMN_WIDTH + "," +
                "m." + MediaTable.COLUMN_HEIGHT + "," +
                "m." + MediaTable.COLUMN_TRANSFERRED + " " +
            "FROM " + MessagesTable.TABLE_NAME + " " +
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
                "AS m ON " + MessagesTable.TABLE_NAME + "." + MessagesTable._ID + "=m." + MediaTable.COLUMN_PARENT_ROW_ID + " AND '" + MessagesTable.TABLE_NAME + "'=m." + MediaTable.COLUMN_PARENT_TABLE + " " +
            "WHERE " + MessagesTable.TABLE_NAME + "." + MessagesTable._ID + "=" + rowId;
        Message message = null;
        try (final Cursor cursor = db.rawQuery(sql, null)) {
            while (cursor.moveToNext()) {
                if (message == null) {
                    message = new Message(
                            cursor.getLong(0),
                            cursor.getString(1),
                            new UserId(cursor.getString(2)),
                            cursor.getString(3),
                            cursor.getLong(4),
                            cursor.getInt(5),
                            cursor.getString(6));
                }
                if (!cursor.isNull(7)) {
                    Preconditions.checkNotNull(message).media.add(new Media(
                            cursor.getLong(7),
                            cursor.getInt(8),
                            cursor.getString(9),
                            fileStore.getMediaFile(cursor.getString(10)),
                            null,
                            null,
                            cursor.getInt(11),
                            cursor.getInt(12),
                            cursor.getInt(13) == 1));
                }
            }
        }
        return message;
    }

    @WorkerThread
    @Nullable Message getMessage(@NonNull String chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        final String sql =
            "SELECT " +
                MessagesTable.TABLE_NAME + "." + MessagesTable._ID + "," +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_CHAT_ID + "," +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_SENDER_USER_ID + "," +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_MESSAGE_ID + "," +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_TIMESTAMP + "," +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_STATE + "," +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_TEXT + "," +
                "m." + MediaTable._ID + "," +
                "m." + MediaTable.COLUMN_TYPE + "," +
                "m." + MediaTable.COLUMN_URL + "," +
                "m." + MediaTable.COLUMN_FILE + "," +
                "m." + MediaTable.COLUMN_WIDTH + "," +
                "m." + MediaTable.COLUMN_HEIGHT + "," +
                "m." + MediaTable.COLUMN_TRANSFERRED + " " +
            "FROM " + MessagesTable.TABLE_NAME + " " +
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
                "AS m ON " + MessagesTable.TABLE_NAME + "." + MessagesTable._ID + "=m." + MediaTable.COLUMN_PARENT_ROW_ID + " AND '" + MessagesTable.TABLE_NAME + "'=m." + MediaTable.COLUMN_PARENT_TABLE + " " +
            "WHERE " + MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_CHAT_ID + "=" + chatId + " AND " +
                    MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_SENDER_USER_ID + "=" + senderUserId.rawId() + " AND " +
                    MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_MESSAGE_ID + "=" + messageId;
        Message message = null;
        try (final Cursor cursor = db.rawQuery(sql, null)) {
            while (cursor.moveToNext()) {
                if (message == null) {
                    message = new Message(
                            cursor.getLong(0),
                            cursor.getString(1),
                            new UserId(cursor.getString(2)),
                            cursor.getString(3),
                            cursor.getLong(4),
                            cursor.getInt(5),
                            cursor.getString(6));
                }
                if (!cursor.isNull(7)) {
                    Preconditions.checkNotNull(message).media.add(new Media(
                            cursor.getLong(7),
                            cursor.getInt(8),
                            cursor.getString(9),
                            fileStore.getMediaFile(cursor.getString(10)),
                            null,
                            null,
                            cursor.getInt(11),
                            cursor.getInt(12),
                            cursor.getInt(13) == 1));
                }
            }
        }
        return message;
    }

    @WorkerThread
    @NonNull List<Message> getMessages(@NonNull String chatId, @Nullable Long startRowId, int count, boolean after) {
        final List<Message> messages = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String where = MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_CHAT_ID + "=" + chatId;
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
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_STATE + "," +
                MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_TEXT + "," +
                "m." + MediaTable._ID + "," +
                "m." + MediaTable.COLUMN_TYPE + "," +
                "m." + MediaTable.COLUMN_URL + "," +
                "m." + MediaTable.COLUMN_FILE + "," +
                "m." + MediaTable.COLUMN_WIDTH + "," +
                "m." + MediaTable.COLUMN_HEIGHT + "," +
                "m." + MediaTable.COLUMN_TRANSFERRED + " " +
            "FROM " + MessagesTable.TABLE_NAME + " " +
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
                "AS m ON " + MessagesTable.TABLE_NAME + "." + MessagesTable._ID + "=m." + MediaTable.COLUMN_PARENT_ROW_ID + " AND '" + MessagesTable.TABLE_NAME + "'=m." + MediaTable.COLUMN_PARENT_TABLE + " " +
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
                    message = new Message(
                            rowId,
                            cursor.getString(1),
                            new UserId(cursor.getString(2)),
                            cursor.getString(3),
                            cursor.getLong(4),
                            cursor.getInt(5),
                            cursor.getString(6));
                }
                if (!cursor.isNull(7)) {
                    Preconditions.checkNotNull(message).media.add(new Media(
                            cursor.getLong(7),
                            cursor.getInt(8),
                            cursor.getString(9),
                            fileStore.getMediaFile(cursor.getString(10)),
                            null,
                            null,
                            cursor.getInt(11),
                            cursor.getInt(12),
                            cursor.getInt(13) == 1));
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
                    MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_STATE + "," +
                    MessagesTable.TABLE_NAME + "." + MessagesTable.COLUMN_TEXT + "," +
                    "m." + MediaTable._ID + "," +
                    "m." + MediaTable.COLUMN_TYPE + "," +
                    "m." + MediaTable.COLUMN_URL + "," +
                    "m." + MediaTable.COLUMN_FILE + "," +
                    "m." + MediaTable.COLUMN_ENC_KEY + "," +
                    "m." + MediaTable.COLUMN_SHA256_HASH + "," +
                    "m." + MediaTable.COLUMN_WIDTH + "," +
                    "m." + MediaTable.COLUMN_HEIGHT + "," +
                    "m." + MediaTable.COLUMN_TRANSFERRED + " " +
                "FROM " + MessagesTable.TABLE_NAME + " " +
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
                    "AS m ON " + MessagesTable.TABLE_NAME + "." + MessagesTable._ID + "=m." + MediaTable.COLUMN_PARENT_ROW_ID + " AND '" + MessagesTable.TABLE_NAME + "'=m." + MediaTable.COLUMN_PARENT_TABLE + " " +
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
                    message = new Message(
                            rowId,
                            cursor.getString(1),
                            new UserId(cursor.getString(2)),
                            cursor.getString(3),
                            cursor.getLong(4),
                            cursor.getInt(5),
                            cursor.getString(6));
                }
                if (!cursor.isNull(7)) {
                    Preconditions.checkNotNull(message).media.add(new Media(
                            cursor.getLong(7),
                            cursor.getInt(8),
                            cursor.getString(9),
                            fileStore.getMediaFile(cursor.getString(10)),
                            cursor.getBlob(11),
                            cursor.getBlob(12),
                            cursor.getInt(13),
                            cursor.getInt(14),
                            cursor.getInt(15) == 1));
                }
            }
            if (message != null) {
                messages.add(message);
            }
        }
        Log.i("ContentDb.getPendingMessages: messages.size=" + messages.size());
        return messages;
    }

    private static long getMessageRetryExpirationTime() {
        return System.currentTimeMillis() - 24 * 60 * 60 * 1000L;
    }

    @WorkerThread
    void deleteChat(@NonNull String chatId) {
        Log.i("ContentDb.deleteChat " + chatId);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();

        final String sql =
            "SELECT " +
                "m." + MediaTable.COLUMN_FILE + " " +
            "FROM " + MessagesTable.TABLE_NAME + " " +
            "INNER JOIN (" +
                "SELECT " +
                    MediaTable.COLUMN_PARENT_TABLE + "," +
                    MediaTable.COLUMN_PARENT_ROW_ID + "," +
                    MediaTable.COLUMN_FILE + "," +
                    MediaTable.COLUMN_TRANSFERRED + " FROM " + MediaTable.TABLE_NAME + ")" +
                "AS m ON " + MessagesTable.TABLE_NAME + "." + MessagesTable._ID + "=m." + MediaTable.COLUMN_PARENT_ROW_ID + " AND '" + MessagesTable.TABLE_NAME + "'=m." + MediaTable.COLUMN_PARENT_TABLE;

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
            }
        }
        final int messagesDeleted = db.delete(MessagesTable.TABLE_NAME, MessagesTable.COLUMN_CHAT_ID + "=?", new String []{chatId});
        final int chatsDeleted = db.delete(ChatsTable.TABLE_NAME, ChatsTable.COLUMN_CHAT_ID + "=?", new String []{chatId});
        db.setTransactionSuccessful();
        db.endTransaction();

        Log.i("ContentDb.deleteChat " + chatId + " filesDeleted=" + filesDeleted + " messagesDeleted=" + messagesDeleted + " chatsDeleted=" + chatsDeleted);
    }

    @WorkerThread
    @NonNull List<SeenReceipt> setChatSeen(@NonNull String chatId) {
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
                        new String [] {chatId},
                        SQLiteDatabase.CONFLICT_ABORT);

                try (final Cursor cursor = db.query(MessagesTable.TABLE_NAME,
                        new String [] {MessagesTable.COLUMN_MESSAGE_ID, MessagesTable.COLUMN_SENDER_USER_ID},
                        MessagesTable.COLUMN_CHAT_ID + "=? AND " + MessagesTable._ID + ">=? AND " + MessagesTable.COLUMN_SENDER_USER_ID + "<>''",
                        new String [] {chatId, String.valueOf(chat.firstUnseenMessageRowId)}, null, null, null)) {
                    while (cursor.moveToNext()) {
                        final String messageId = cursor.getString(0);
                        final UserId senderUserId = new UserId(cursor.getString(1));
                        seenReceipts.add(new SeenReceipt(chatId, senderUserId, messageId));

                        final ContentValues values = new ContentValues();
                        values.put(OutgoingSeenReceiptsTable.COLUMN_CHAT_ID, chatId);
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
    void setMessageSeenReceiptSent(@NonNull String chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
        final int deleteCount = databaseHelper.getWritableDatabase().delete(OutgoingSeenReceiptsTable.TABLE_NAME,
                OutgoingSeenReceiptsTable.COLUMN_CHAT_ID + "=? AND " + OutgoingSeenReceiptsTable.COLUMN_SENDER_USER_ID + "=? AND " + OutgoingSeenReceiptsTable.COLUMN_CONTENT_ITEM_ID + "=?",
                new String [] {chatId, senderUserId.rawId(), messageId});
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
                        ChatsTable.COLUMN_FIRST_UNSEEN_MESSAGE_ROW_ID},
                null,
                null, null, null, ChatsTable.COLUMN_TIMESTAMP + " DESC")) {
            while (cursor.moveToNext()) {
                final Chat chat = new Chat(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getLong(2),
                        cursor.getInt(3),
                        cursor.getLong(4),
                        cursor.getLong(5));
                chats.add(chat);
            }
        }
        return chats;
    }

    @WorkerThread
    @Nullable Chat getChat(@NonNull String chatId) {
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.query(ChatsTable.TABLE_NAME,
                new String [] {
                        ChatsTable._ID,
                        ChatsTable.COLUMN_CHAT_ID,
                        ChatsTable.COLUMN_TIMESTAMP,
                        ChatsTable.COLUMN_NEW_MESSAGE_COUNT,
                        ChatsTable.COLUMN_LAST_MESSAGE_ROW_ID,
                        ChatsTable.COLUMN_FIRST_UNSEEN_MESSAGE_ROW_ID},
                ChatsTable.COLUMN_CHAT_ID + "=?",
                new String [] {chatId},
                null, null, null)) {
            if (cursor.moveToNext()) {
                return new Chat(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getLong(2),
                        cursor.getInt(3),
                        cursor.getLong(4),
                        cursor.getLong(5));
            }
        }
        return null;
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
                        cursor.getString(0),
                        new UserId(cursor.getString(1)),
                        cursor.getString(2));
                receipts.add(receipt);
            }
        }
        Log.i("ContentDb.getPendingMessageSeenReceipts: receipts.size=" + receipts.size());
        return receipts;
    }
}

package com.halloapp.content;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;

import com.halloapp.content.tables.AudienceTable;
import com.halloapp.content.tables.ChatsTable;
import com.halloapp.content.tables.CommentsTable;
import com.halloapp.content.tables.GroupMembersTable;
import com.halloapp.content.tables.MediaTable;
import com.halloapp.content.tables.MentionsTable;
import com.halloapp.content.tables.MessagesTable;
import com.halloapp.content.tables.OutgoingSeenReceiptsTable;
import com.halloapp.content.tables.PostsTable;
import com.halloapp.content.tables.RepliesTable;
import com.halloapp.content.tables.SeenTable;
import com.halloapp.util.Log;

import java.io.File;

class ContentDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "content.db";
    private static final int DATABASE_VERSION = 31;

    private final Context context;
    private final ContentDbObservers observers;

    ContentDbHelper(@NonNull Context context, @NonNull ContentDbObservers observers) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        setWriteAheadLoggingEnabled(true);
        this.context = context.getApplicationContext();
        this.observers = observers;
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
                + PostsTable.COLUMN_TEXT + " TEXT,"
                + PostsTable.COLUMN_AUDIENCE_TYPE + " TEXT,"
                + PostsTable.COLUMN_GROUP_ID + " TEXT"
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + PostsTable.INDEX_POST_KEY);
        db.execSQL("CREATE UNIQUE INDEX " + PostsTable.INDEX_POST_KEY + " ON " + PostsTable.TABLE_NAME + "("
                + PostsTable.COLUMN_SENDER_USER_ID + ", "
                + PostsTable.COLUMN_POST_ID
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + PostsTable.INDEX_TIMESTAMP);
        db.execSQL("CREATE INDEX " + PostsTable.INDEX_TIMESTAMP + " ON " + PostsTable.TABLE_NAME + "("
                + PostsTable.COLUMN_TIMESTAMP
                + ");");

        db.execSQL("DROP TABLE IF EXISTS " + MessagesTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + MessagesTable.TABLE_NAME + " ("
                + MessagesTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MessagesTable.COLUMN_CHAT_ID + " TEXT NOT NULL,"
                + MessagesTable.COLUMN_SENDER_USER_ID + " TEXT NOT NULL,"
                + MessagesTable.COLUMN_MESSAGE_ID + " TEXT NOT NULL,"
                + MessagesTable.COLUMN_TIMESTAMP + " INTEGER,"
                + MessagesTable.COLUMN_TYPE + " INTEGER DEFAULT 0,"
                + MessagesTable.COLUMN_USAGE + " INTEGER DEFAULT 0,"
                + MessagesTable.COLUMN_STATE + " INTEGER,"
                + MessagesTable.COLUMN_TEXT + " TEXT,"
                + MessagesTable.COLUMN_REREQUEST_COUNT + " INTEGER"
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + MessagesTable.INDEX_MESSAGE_KEY);
        db.execSQL("CREATE UNIQUE INDEX " + MessagesTable.INDEX_MESSAGE_KEY + " ON " + MessagesTable.TABLE_NAME + "("
                + MessagesTable.COLUMN_CHAT_ID + ", "
                + MessagesTable.COLUMN_SENDER_USER_ID + ", "
                + MessagesTable.COLUMN_MESSAGE_ID
                + ");");

        db.execSQL("DROP TABLE IF EXISTS " + ChatsTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + ChatsTable.TABLE_NAME + " ("
                + ChatsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + ChatsTable.COLUMN_CHAT_ID + " TEXT NOT NULL UNIQUE,"
                + ChatsTable.COLUMN_TIMESTAMP + " INTEGER,"
                + ChatsTable.COLUMN_NEW_MESSAGE_COUNT + " INTEGER,"
                + ChatsTable.COLUMN_LAST_MESSAGE_ROW_ID + " INTEGER DEFAULT -1,"
                + ChatsTable.COLUMN_FIRST_UNSEEN_MESSAGE_ROW_ID + " INTEGER DEFAULT -1,"
                + ChatsTable.COLUMN_CHAT_NAME + " TEXT,"
                + ChatsTable.COLUMN_IS_GROUP + " INTEGER DEFAULT 0,"
                + ChatsTable.COLUMN_IS_ACTIVE + " INTEGER DEFAULT 1,"
                + ChatsTable.COLUMN_GROUP_DESCRIPTION + " TEXT,"
                + ChatsTable.COLUMN_GROUP_AVATAR_ID + " TEXT"
                + ");");

        db.execSQL("DROP TABLE IF EXISTS " + GroupMembersTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + GroupMembersTable.TABLE_NAME + " ("
                + GroupMembersTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + GroupMembersTable.COLUMN_GROUP_ID + " TEXT NOT NULL,"
                + GroupMembersTable.COLUMN_USER_ID + " TEXT NOT NULL,"
                + GroupMembersTable.COLUMN_IS_ADMIN + " INTEGER DEFAULT 0"
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + GroupMembersTable.INDEX_GROUP_USER);
        db.execSQL("CREATE UNIQUE INDEX " + GroupMembersTable.INDEX_GROUP_USER + " ON " + GroupMembersTable.TABLE_NAME + "("
                + GroupMembersTable.COLUMN_GROUP_ID + ", "
                + GroupMembersTable.COLUMN_USER_ID
                + ");");

        db.execSQL("DROP TABLE IF EXISTS " + RepliesTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + RepliesTable.TABLE_NAME + " ("
                + RepliesTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + RepliesTable.COLUMN_MESSAGE_ROW_ID + " INTEGER,"
                + RepliesTable.COLUMN_REPLY_MESSAGE_ID + " TEXT,"
                + RepliesTable.COLUMN_REPLY_MESSAGE_MEDIA_INDEX + " INTEGER DEFAULT 0,"
                + RepliesTable.COLUMN_REPLY_MESSAGE_SENDER_ID + " TEXT,"
                + RepliesTable.COLUMN_POST_ID + " TEXT NOT NULL,"
                + RepliesTable.COLUMN_POST_MEDIA_INDEX + " INTEGER,"
                + RepliesTable.COLUMN_TEXT + " TEXT,"
                + RepliesTable.COLUMN_MEDIA_TYPE + " INTEGER,"
                + RepliesTable.COLUMN_MEDIA_PREVIEW_FILE + " TEXT"
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + RepliesTable.INDEX_MESSAGE_KEY);
        db.execSQL("CREATE UNIQUE INDEX " + RepliesTable.INDEX_MESSAGE_KEY + " ON " + RepliesTable.TABLE_NAME + "("
                + RepliesTable.COLUMN_MESSAGE_ROW_ID
                + ");");

        db.execSQL("DROP TABLE IF EXISTS " + MediaTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + MediaTable.TABLE_NAME + " ("
                + MediaTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MediaTable.COLUMN_PARENT_TABLE + " TEXT NOT NULL,"
                + MediaTable.COLUMN_PARENT_ROW_ID + " INTEGER,"
                + MediaTable.COLUMN_TYPE + " INTEGER,"
                + MediaTable.COLUMN_TRANSFERRED + " INTEGER,"
                + MediaTable.COLUMN_URL + " TEXT,"
                + MediaTable.COLUMN_FILE + " FILE,"
                + MediaTable.COLUMN_PATCH_URL + " TEXT,"
                + MediaTable.COLUMN_ENC_KEY + " BLOB,"
                + MediaTable.COLUMN_SHA256_HASH + " BLOB,"
                + MediaTable.COLUMN_WIDTH + " INTEGER,"
                + MediaTable.COLUMN_HEIGHT + " INTEGER,"
                + MediaTable.COLUMN_UPLOAD_PROGRESS + " INTEGER DEFAULT 0,"
                + MediaTable.COLUMN_RETRY_COUNT + " INTEGER DEFAULT 0"
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + MediaTable.INDEX_MEDIA_KEY);
        db.execSQL("CREATE INDEX " + MediaTable.INDEX_MEDIA_KEY + " ON " + MediaTable.TABLE_NAME + "("
                + MediaTable.COLUMN_PARENT_TABLE + ", "
                + MediaTable.COLUMN_PARENT_ROW_ID
                + ");");

        db.execSQL("DROP TABLE IF EXISTS " + MentionsTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + MentionsTable.TABLE_NAME + " ("
                + MentionsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MentionsTable.COLUMN_PARENT_TABLE + " TEXT NOT NULL,"
                + MentionsTable.COLUMN_PARENT_ROW_ID + " INTEGER,"
                + MentionsTable.COLUMN_MENTION_INDEX + " INTEGER,"
                + MentionsTable.COLUMN_MENTION_NAME + " TEXT,"
                + MentionsTable.COLUMN_MENTION_USER_ID + " TEXT NOT NULL"
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + MentionsTable.INDEX_MENTION_KEY);
        db.execSQL("CREATE INDEX " + MentionsTable.INDEX_MENTION_KEY + " ON " + MentionsTable.TABLE_NAME + "("
                + MentionsTable.COLUMN_PARENT_TABLE + ", "
                + MentionsTable.COLUMN_PARENT_ROW_ID
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

        db.execSQL("DROP INDEX IF EXISTS " + CommentsTable.INDEX_COMMENT_KEY);
        db.execSQL("CREATE UNIQUE INDEX " + CommentsTable.INDEX_COMMENT_KEY + " ON " + CommentsTable.TABLE_NAME + "("
                + CommentsTable.COLUMN_COMMENT_SENDER_USER_ID + ", "
                + CommentsTable.COLUMN_COMMENT_ID
                + ");");

        db.execSQL("DROP TABLE IF EXISTS " + SeenTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + SeenTable.TABLE_NAME + " ("
                + SeenTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SeenTable.COLUMN_POST_ID + " TEXT NOT NULL,"
                + SeenTable.COLUMN_SEEN_BY_USER_ID + " TEXT NOT NULL,"
                + SeenTable.COLUMN_TIMESTAMP
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + SeenTable.INDEX_SEEN_KEY);
        db.execSQL("CREATE UNIQUE INDEX " + SeenTable.INDEX_SEEN_KEY + " ON " + SeenTable.TABLE_NAME + "("
                + SeenTable.COLUMN_POST_ID + ", "
                + SeenTable.COLUMN_SEEN_BY_USER_ID
                + ");");

        db.execSQL("DROP TABLE IF EXISTS " + AudienceTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + AudienceTable.TABLE_NAME + " ("
                + AudienceTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + AudienceTable.COLUMN_POST_ID + " TEXT NOT NULL,"
                + AudienceTable.COLUMN_USER_ID + " TEXT NOT NULL,"
                + AudienceTable.COLUMN_EXCLUDED + " INTEGER NOT NULL DEFAULT(0)"
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + AudienceTable.INDEX_AUDIENCE_KEY);
        db.execSQL("CREATE UNIQUE INDEX " + AudienceTable.INDEX_AUDIENCE_KEY + " ON " + AudienceTable.TABLE_NAME + "("
                + AudienceTable.COLUMN_POST_ID + ", "
                + AudienceTable.COLUMN_USER_ID + ", "
                + AudienceTable.COLUMN_EXCLUDED
                + ");");

        db.execSQL("DROP TABLE IF EXISTS " + OutgoingSeenReceiptsTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + OutgoingSeenReceiptsTable.TABLE_NAME + " ("
                + OutgoingSeenReceiptsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + OutgoingSeenReceiptsTable.COLUMN_CHAT_ID + " TEXT NOT NULL,"
                + OutgoingSeenReceiptsTable.COLUMN_SENDER_USER_ID + " TEXT NOT NULL,"
                + OutgoingSeenReceiptsTable.COLUMN_CONTENT_ITEM_ID + " TEXT NOT NULL"
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + OutgoingSeenReceiptsTable.INDEX_OUTGOING_RECEIPT_KEY);
        db.execSQL("CREATE UNIQUE INDEX " + OutgoingSeenReceiptsTable.INDEX_OUTGOING_RECEIPT_KEY + " ON " + OutgoingSeenReceiptsTable.TABLE_NAME + "("
                + OutgoingSeenReceiptsTable.COLUMN_CHAT_ID + ", "
                + OutgoingSeenReceiptsTable.COLUMN_SENDER_USER_ID + ", "
                + OutgoingSeenReceiptsTable.COLUMN_CONTENT_ITEM_ID
                + ");");

        db.execSQL("DROP TRIGGER IF EXISTS " + PostsTable.TRIGGER_DELETE);
        //noinspection SyntaxError
        db.execSQL("CREATE TRIGGER " + PostsTable.TRIGGER_DELETE + " AFTER DELETE ON " + PostsTable.TABLE_NAME + " "
                + "BEGIN "
                +   " DELETE FROM " + MediaTable.TABLE_NAME + " WHERE " + MediaTable.COLUMN_PARENT_ROW_ID + "=OLD." + PostsTable._ID + " AND " + MediaTable.COLUMN_PARENT_TABLE + "='" + PostsTable.TABLE_NAME + "'; "
                +   " DELETE FROM " + MentionsTable.TABLE_NAME + " WHERE " + MentionsTable.COLUMN_PARENT_ROW_ID + "=OLD." + PostsTable._ID + " AND " + MentionsTable.COLUMN_PARENT_TABLE + "='" + PostsTable.TABLE_NAME + "'; "
                +   " DELETE FROM " + CommentsTable.TABLE_NAME + " WHERE " + CommentsTable.COLUMN_POST_ID + "=OLD." + PostsTable.COLUMN_POST_ID + " AND " + CommentsTable.COLUMN_POST_SENDER_USER_ID + "=OLD." + PostsTable.COLUMN_SENDER_USER_ID + "; "
                +   " DELETE FROM " + SeenTable.TABLE_NAME + " WHERE " + SeenTable.COLUMN_POST_ID + "=OLD." + PostsTable.COLUMN_POST_ID + " AND ''=OLD." + PostsTable.COLUMN_SENDER_USER_ID + "; "
                +   " DELETE FROM " + AudienceTable.TABLE_NAME + " WHERE " + AudienceTable.COLUMN_POST_ID + "=OLD." + AudienceTable.COLUMN_POST_ID + "; "
                + "END;");

        db.execSQL("DROP TRIGGER IF EXISTS " + MessagesTable.TRIGGER_DELETE);
        db.execSQL("CREATE TRIGGER " + MessagesTable.TRIGGER_DELETE + " AFTER DELETE ON " + MessagesTable.TABLE_NAME + " "
                + "BEGIN "
                +   " DELETE FROM " + MediaTable.TABLE_NAME + " WHERE " + MediaTable.COLUMN_PARENT_ROW_ID + "=OLD." + MessagesTable._ID + " AND " + MediaTable.COLUMN_PARENT_TABLE + "='" + MessagesTable.TABLE_NAME + "'; "
                +   " DELETE FROM " + MentionsTable.TABLE_NAME + " WHERE " + MentionsTable.COLUMN_PARENT_ROW_ID + "=OLD." + MessagesTable._ID + " AND " + MentionsTable.COLUMN_PARENT_TABLE + "='" + MentionsTable.TABLE_NAME + "'; "
                + "END;");

        db.execSQL("DROP TRIGGER IF EXISTS " + CommentsTable.TRIGGER_DELETE);
        db.execSQL("CREATE TRIGGER " + CommentsTable.TRIGGER_DELETE + " AFTER DELETE ON " + CommentsTable.TABLE_NAME + " "
                + "BEGIN "
                +   "DELETE FROM " + MentionsTable.TABLE_NAME + " WHERE " + MentionsTable.COLUMN_PARENT_ROW_ID + "=OLD." + CommentsTable._ID + " AND " + MentionsTable.COLUMN_PARENT_TABLE + "='" + MentionsTable.TABLE_NAME + "'; "
                + "END;");

        db.execSQL("DROP TRIGGER IF EXISTS " + ChatsTable.TRIGGER_DELETE);
        db.execSQL("CREATE TRIGGER " + ChatsTable.TRIGGER_DELETE + " AFTER DELETE ON " + ChatsTable.TABLE_NAME + " "
                + "BEGIN "
                +   "DELETE FROM " + GroupMembersTable.TABLE_NAME + " WHERE " + GroupMembersTable.COLUMN_GROUP_ID + "=OLD." + ChatsTable.COLUMN_CHAT_ID + "; "
                + "END;");

        observers.notifyDbCreated();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("ContentDb: upgrade from " + oldVersion + " to " + newVersion);
        switch (oldVersion) {
            case 10: {
                upgradeFromVersion10(db);
                // fall through
            }
            case 11: {
                upgradeFromVersion11(db);
                // fall through
            }
            case 12: {
                upgradeFromVersion12(db);
                // fall through
            }
            case 14: {
                upgradeFromVersion14(db);
                // fall through
            }
            case 15: {
                upgradeFromVersion15(db);
                // fall through
            }
            case 16: {
                upgradeFromVersion16(db);
                // fall through
            }
            case 17: {
                upgradeFromVersion17(db);
            }
            case 18: {
                upgradeFromVersion18(db);
            }
            case 19: {
                upgradeFromVersion19(db);
            }
            case 20: {
                upgradeFromVersion20(db);
            }
            case 21: {
                upgradeFromVersion21(db);
            }
            case 22: {
                upgradeFromVersion22(db);
            }
            case 23: {
                upgradeFromVersion23(db);
            }
            case 24: {
                upgradeFromVersion24(db);
            }
            case 25: {
                upgradeFromVersion25(db);
            }
            case 26: {
                upgradeFromVersion26(db);
            }
            case 27: {
                upgradeFromVersion27(db);
            }
            case 28: {
                upgradeFromVersion28(db);
            }
            case 29: {
                upgradeFromVersion29(db);
            }
            case 30: {
                upgradeFromVersion30(db);
            }
            break;
            default: {
                onCreate(db);
                break;
            }
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("ContentDb: downgrade from " + oldVersion + " to " + newVersion);
        onCreate(db);
    }

    private void upgradeFromVersion10(@NonNull SQLiteDatabase db) {
        final ContentValues messageValues = new ContentValues();
        messageValues.put("seen", 2);
        db.update(MessagesTable.TABLE_NAME, messageValues, null, null);
    }

    private void upgradeFromVersion11(@NonNull SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + OutgoingSeenReceiptsTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + OutgoingSeenReceiptsTable.TABLE_NAME + " ("
                + OutgoingSeenReceiptsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + OutgoingSeenReceiptsTable.COLUMN_CHAT_ID + " TEXT NOT NULL,"
                + OutgoingSeenReceiptsTable.COLUMN_SENDER_USER_ID + " TEXT NOT NULL,"
                + OutgoingSeenReceiptsTable.COLUMN_CONTENT_ITEM_ID + " TEXT NOT NULL"
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + OutgoingSeenReceiptsTable.INDEX_OUTGOING_RECEIPT_KEY);
        db.execSQL("CREATE UNIQUE INDEX " + OutgoingSeenReceiptsTable.INDEX_OUTGOING_RECEIPT_KEY + " ON " + OutgoingSeenReceiptsTable.TABLE_NAME + "("
                + OutgoingSeenReceiptsTable.COLUMN_CHAT_ID + ", "
                + OutgoingSeenReceiptsTable.COLUMN_SENDER_USER_ID + ", "
                + OutgoingSeenReceiptsTable.COLUMN_CONTENT_ITEM_ID
                + ");");

        removeColumns(db, MessagesTable.TABLE_NAME, new String [] {
                MessagesTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT",
                MessagesTable.COLUMN_CHAT_ID + " TEXT NOT NULL",
                MessagesTable.COLUMN_SENDER_USER_ID + " TEXT NOT NULL",
                MessagesTable.COLUMN_MESSAGE_ID + " TEXT NOT NULL",
                MessagesTable.COLUMN_TIMESTAMP + " INTEGER",
                MessagesTable.COLUMN_TEXT + " TEXT"
        });

        db.execSQL("ALTER TABLE " + MessagesTable.TABLE_NAME + " ADD COLUMN " + MessagesTable.COLUMN_STATE + " INTEGER");
        ContentValues values = new ContentValues();
        values.put(MessagesTable.COLUMN_STATE, Message.STATE_OUTGOING_SEEN);
        db.update(MessagesTable.TABLE_NAME, values, MessagesTable.COLUMN_SENDER_USER_ID + "=''", null);
        values.put(MessagesTable.COLUMN_STATE, Message.STATE_INCOMING_RECEIVED);
        db.update(MessagesTable.TABLE_NAME, values, MessagesTable.COLUMN_SENDER_USER_ID + "<>''", null);

        final ContentValues postValues = new ContentValues();
        postValues.put(PostsTable.COLUMN_TRANSFERRED, Post.TRANSFERRED_YES);
        db.update(PostsTable.TABLE_NAME, postValues, PostsTable.COLUMN_TRANSFERRED + "=?", new String [] {"1"});
    }

    private void upgradeFromVersion12(@NonNull SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + RepliesTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + RepliesTable.TABLE_NAME + " ("
                + RepliesTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + RepliesTable.COLUMN_MESSAGE_ROW_ID + " INTEGER,"
                + RepliesTable.COLUMN_POST_ID + " TEXT NOT NULL,"
                + RepliesTable.COLUMN_POST_MEDIA_INDEX + " INTEGER,"
                + RepliesTable.COLUMN_TEXT + " TEXT,"
                + RepliesTable.COLUMN_MEDIA_TYPE + " INTEGER,"
                + RepliesTable.COLUMN_MEDIA_PREVIEW_FILE + " TEXT"
                + ");");

        removeColumns(db, MessagesTable.TABLE_NAME, new String [] {
                MessagesTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT",
                MessagesTable.COLUMN_CHAT_ID + " TEXT NOT NULL",
                MessagesTable.COLUMN_SENDER_USER_ID + " TEXT NOT NULL",
                MessagesTable.COLUMN_MESSAGE_ID + " TEXT NOT NULL",
                MessagesTable.COLUMN_TIMESTAMP + " INTEGER",
                MessagesTable.COLUMN_STATE + " INTEGER",
                MessagesTable.COLUMN_TEXT + " TEXT"
        });
    }

    private void upgradeFromVersion14(@NonNull SQLiteDatabase db) {
        // delete duplicate messages due to bug in upgradeFromVersion12
        db.execSQL("DELETE FROM messages WHERE _id NOT IN (SELECT MAX(_id) FROM messages GROUP BY chat_id, sender_user_id, message_id)");
        // recreate messages key
        db.execSQL("DROP INDEX IF EXISTS " + MessagesTable.INDEX_MESSAGE_KEY);
        db.execSQL("CREATE UNIQUE INDEX " + MessagesTable.INDEX_MESSAGE_KEY + " ON " + MessagesTable.TABLE_NAME + "("
                + MessagesTable.COLUMN_CHAT_ID + ", "
                + MessagesTable.COLUMN_SENDER_USER_ID + ", "
                + MessagesTable.COLUMN_MESSAGE_ID
                + ");");
        // recreate replies key
        db.execSQL("DROP INDEX IF EXISTS " + RepliesTable.INDEX_MESSAGE_KEY);
        db.execSQL("CREATE UNIQUE INDEX " + RepliesTable.INDEX_MESSAGE_KEY + " ON " + RepliesTable.TABLE_NAME + "("
                + RepliesTable.COLUMN_MESSAGE_ROW_ID
                + ");");
        // recreate messages trigger
        db.execSQL("DROP TRIGGER IF EXISTS " + MessagesTable.TRIGGER_DELETE);
        db.execSQL("CREATE TRIGGER " + MessagesTable.TRIGGER_DELETE + " AFTER DELETE ON " + MessagesTable.TABLE_NAME + " "
                + "BEGIN "
                +   " DELETE FROM " + MediaTable.TABLE_NAME + " WHERE " + MediaTable.COLUMN_PARENT_ROW_ID + "=OLD." + MessagesTable._ID + " AND " + MediaTable.COLUMN_PARENT_TABLE + "='" + MessagesTable.TABLE_NAME + "'; "
                + "END;");
    }

    private void upgradeFromVersion15(@NonNull SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + MentionsTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + MentionsTable.TABLE_NAME + " ("
                + MentionsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MentionsTable.COLUMN_PARENT_TABLE + " TEXT NOT NULL,"
                + MentionsTable.COLUMN_PARENT_ROW_ID + " INTEGER,"
                + MentionsTable.COLUMN_MENTION_INDEX + " INTEGER,"
                + MentionsTable.COLUMN_MENTION_NAME + " TEXT,"
                + MentionsTable.COLUMN_MENTION_USER_ID + " TEXT NOT NULL"
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + MentionsTable.INDEX_MENTION_KEY);
        db.execSQL("CREATE INDEX " + MentionsTable.INDEX_MENTION_KEY + " ON " + MentionsTable.TABLE_NAME + "("
                + MentionsTable.COLUMN_PARENT_TABLE + ", "
                + MentionsTable.COLUMN_PARENT_ROW_ID
                + ");");
    }

    private void upgradeFromVersion16(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + MessagesTable.TABLE_NAME + " ADD COLUMN " + MessagesTable.COLUMN_REREQUEST_COUNT + " INTEGER DEFAULT 0");
    }

    private void upgradeFromVersion17(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + ChatsTable.TABLE_NAME + " ADD COLUMN " + ChatsTable.COLUMN_CHAT_NAME + " TEXT");
    }

    private void upgradeFromVersion18(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + MediaTable.TABLE_NAME + " ADD COLUMN " + MediaTable.COLUMN_PATCH_URL + " TEXT");
    }

    private void upgradeFromVersion19(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + ChatsTable.TABLE_NAME + " ADD COLUMN " + ChatsTable.COLUMN_IS_GROUP + " INTEGER DEFAULT 0");
        db.execSQL("ALTER TABLE " + ChatsTable.TABLE_NAME + " ADD COLUMN " + ChatsTable.COLUMN_GROUP_DESCRIPTION + " TEXT");
        db.execSQL("ALTER TABLE " + ChatsTable.TABLE_NAME + " ADD COLUMN " + ChatsTable.COLUMN_GROUP_AVATAR_ID + " TEXT");
    }

    private void upgradeFromVersion20(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + MediaTable.TABLE_NAME + " ADD COLUMN " + MediaTable.COLUMN_UPLOAD_PROGRESS + " INTEGER DEFAULT 0");
        db.execSQL("ALTER TABLE " + MediaTable.TABLE_NAME + " ADD COLUMN " + MediaTable.COLUMN_RETRY_COUNT + " INTEGER DEFAULT 0");
    }

    private void upgradeFromVersion21(@NonNull SQLiteDatabase db) {
        // Recreate posts delete trigger
        db.execSQL("DROP TRIGGER IF EXISTS " + PostsTable.TRIGGER_DELETE);
        //noinspection SyntaxError
        db.execSQL("CREATE TRIGGER " + PostsTable.TRIGGER_DELETE + " AFTER DELETE ON " + PostsTable.TABLE_NAME + " "
                + "BEGIN "
                + " DELETE FROM " + MediaTable.TABLE_NAME + " WHERE " + MediaTable.COLUMN_PARENT_ROW_ID + "=OLD." + PostsTable._ID + " AND " + MediaTable.COLUMN_PARENT_TABLE + "='" + PostsTable.TABLE_NAME + "'; "
                + " DELETE FROM " + MentionsTable.TABLE_NAME + " WHERE " + MentionsTable.COLUMN_PARENT_ROW_ID + "=OLD." + PostsTable._ID + " AND " + MentionsTable.COLUMN_PARENT_TABLE + "='" + PostsTable.TABLE_NAME + "'; "
                + " DELETE FROM " + CommentsTable.TABLE_NAME + " WHERE " + CommentsTable.COLUMN_POST_ID + "=OLD." + PostsTable.COLUMN_POST_ID + " AND " + CommentsTable.COLUMN_POST_SENDER_USER_ID + "=OLD." + PostsTable.COLUMN_SENDER_USER_ID + "; "
                + " DELETE FROM " + SeenTable.TABLE_NAME + " WHERE " + SeenTable.COLUMN_POST_ID + "=OLD." + PostsTable.COLUMN_POST_ID + " AND ''=OLD." + PostsTable.COLUMN_SENDER_USER_ID + "; "
                + "END;");

        // Create comments delete trigger
        db.execSQL("DROP TRIGGER IF EXISTS " + CommentsTable.TRIGGER_DELETE);
        db.execSQL("CREATE TRIGGER " + CommentsTable.TRIGGER_DELETE + " AFTER DELETE ON " + CommentsTable.TABLE_NAME + " "
                + "BEGIN "
                + "DELETE FROM " + MentionsTable.TABLE_NAME + " WHERE " + MentionsTable.COLUMN_PARENT_ROW_ID + "=OLD." + CommentsTable._ID + " AND " + MentionsTable.COLUMN_PARENT_TABLE + "='" + MentionsTable.TABLE_NAME + "'; "
                + "END;");
    }

    private void upgradeFromVersion22(@NonNull SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + GroupMembersTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + GroupMembersTable.TABLE_NAME + " ("
                + GroupMembersTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + GroupMembersTable.COLUMN_GROUP_ID + " TEXT NOT NULL,"
                + GroupMembersTable.COLUMN_USER_ID + " TEXT NOT NULL,"
                + GroupMembersTable.COLUMN_IS_ADMIN + " INTEGER DEFAULT 0"
                + ");");
    }

    private void upgradeFromVersion23(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + MessagesTable.TABLE_NAME + " ADD COLUMN " + MessagesTable.COLUMN_TYPE + " INTEGER DEFAULT 0");
        db.execSQL("ALTER TABLE " + MessagesTable.TABLE_NAME + " ADD COLUMN " + MessagesTable.COLUMN_USAGE + " INTEGER DEFAULT 0");
    }

    private void upgradeFromVersion24(@NonNull SQLiteDatabase db) {
        db.execSQL("DROP INDEX IF EXISTS " + GroupMembersTable.INDEX_GROUP_USER);
        db.execSQL("CREATE UNIQUE INDEX " + GroupMembersTable.INDEX_GROUP_USER + " ON " + GroupMembersTable.TABLE_NAME + "("
                + GroupMembersTable.COLUMN_GROUP_ID + ", "
                + GroupMembersTable.COLUMN_USER_ID
                + ");");
    }

    private void upgradeFromVersion25(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + PostsTable.TABLE_NAME + " ADD COLUMN " + PostsTable.COLUMN_AUDIENCE_TYPE + " TEXT");
        db.execSQL("DROP TABLE IF EXISTS " + AudienceTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + AudienceTable.TABLE_NAME + " ("
                + AudienceTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + AudienceTable.COLUMN_POST_ID + " TEXT NOT NULL,"
                + AudienceTable.COLUMN_USER_ID + " TEXT NOT NULL"
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + AudienceTable.INDEX_AUDIENCE_KEY);
        db.execSQL("CREATE UNIQUE INDEX " + AudienceTable.INDEX_AUDIENCE_KEY + " ON " + AudienceTable.TABLE_NAME + "("
                + AudienceTable.COLUMN_POST_ID + ", "
                + AudienceTable.COLUMN_USER_ID
                + ");");

        db.execSQL("DROP TRIGGER IF EXISTS " + PostsTable.TRIGGER_DELETE);
        //noinspection SyntaxError
        db.execSQL("CREATE TRIGGER " + PostsTable.TRIGGER_DELETE + " AFTER DELETE ON " + PostsTable.TABLE_NAME + " "
                + "BEGIN "
                +   " DELETE FROM " + MediaTable.TABLE_NAME + " WHERE " + MediaTable.COLUMN_PARENT_ROW_ID + "=OLD." + PostsTable._ID + " AND " + MediaTable.COLUMN_PARENT_TABLE + "='" + PostsTable.TABLE_NAME + "'; "
                +   " DELETE FROM " + MentionsTable.TABLE_NAME + " WHERE " + MentionsTable.COLUMN_PARENT_ROW_ID + "=OLD." + PostsTable._ID + " AND " + MentionsTable.COLUMN_PARENT_TABLE + "='" + PostsTable.TABLE_NAME + "'; "
                +   " DELETE FROM " + CommentsTable.TABLE_NAME + " WHERE " + CommentsTable.COLUMN_POST_ID + "=OLD." + PostsTable.COLUMN_POST_ID + " AND " + CommentsTable.COLUMN_POST_SENDER_USER_ID + "=OLD." + PostsTable.COLUMN_SENDER_USER_ID + "; "
                +   " DELETE FROM " + SeenTable.TABLE_NAME + " WHERE " + SeenTable.COLUMN_POST_ID + "=OLD." + PostsTable.COLUMN_POST_ID + " AND ''=OLD." + PostsTable.COLUMN_SENDER_USER_ID + "; "
                +   " DELETE FROM " + AudienceTable.TABLE_NAME + " WHERE " + AudienceTable.COLUMN_POST_ID + "=OLD." + AudienceTable.COLUMN_POST_ID + "; "
                + "END;");
    }

    private void upgradeFromVersion26(@NonNull SQLiteDatabase db) {
        db.execSQL("DROP TRIGGER IF EXISTS " + ChatsTable.TRIGGER_DELETE);
        db.execSQL("CREATE TRIGGER " + ChatsTable.TRIGGER_DELETE + " AFTER DELETE ON " + ChatsTable.TABLE_NAME + " "
                + "BEGIN "
                +   "DELETE FROM " + GroupMembersTable.TABLE_NAME + " WHERE " + GroupMembersTable.COLUMN_GROUP_ID + "=OLD." + ChatsTable.COLUMN_CHAT_ID + "; "
                + "END;");
    }

    private void upgradeFromVersion27(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + ChatsTable.TABLE_NAME + " ADD COLUMN " + ChatsTable.COLUMN_IS_ACTIVE + " INTEGER DEFAULT 1");
    }

    private void upgradeFromVersion28(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + RepliesTable.TABLE_NAME + " ADD COLUMN " + RepliesTable.COLUMN_REPLY_MESSAGE_ID + " TEXT");
        db.execSQL("ALTER TABLE " + RepliesTable.TABLE_NAME + " ADD COLUMN " + RepliesTable.COLUMN_REPLY_MESSAGE_MEDIA_INDEX + " INTEGER DEFAULT 0");
        db.execSQL("ALTER TABLE " + RepliesTable.TABLE_NAME + " ADD COLUMN " + RepliesTable.COLUMN_REPLY_MESSAGE_SENDER_ID + " TEXT");
    }

    private void upgradeFromVersion29(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + AudienceTable.TABLE_NAME + " ADD COLUMN " + AudienceTable.COLUMN_EXCLUDED + " INTEGER NOT NULL DEFAULT(0)");
        db.execSQL("DROP INDEX IF EXISTS " + AudienceTable.INDEX_AUDIENCE_KEY);
        db.execSQL("CREATE UNIQUE INDEX " + AudienceTable.INDEX_AUDIENCE_KEY + " ON " + AudienceTable.TABLE_NAME + "("
                + AudienceTable.COLUMN_POST_ID + ", "
                + AudienceTable.COLUMN_USER_ID + ", "
                + AudienceTable.COLUMN_EXCLUDED
                + ");");
    }

    private void upgradeFromVersion30(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + PostsTable.TABLE_NAME + " ADD COLUMN " + PostsTable.COLUMN_GROUP_ID + " TEXT");
    }

    private void removeColumns(@NonNull SQLiteDatabase db, @NonNull String tableName, @NonNull String [] columns) {
        final StringBuilder schema = new StringBuilder();
        for (String column : columns) {
            if (schema.length() != 0) {
                schema.append(',');
            }
            schema.append(column);
        }
        final StringBuilder selection = new StringBuilder();
        for (String column : columns) {
            if (selection.length() != 0) {
                selection.append(',');
            }
            selection.append(column.substring(0, column.indexOf(' ')));
        }

        db.execSQL("DROP TABLE IF EXISTS tmp");
        db.execSQL("CREATE TABLE tmp (" + schema.toString() + ");");
        db.execSQL("INSERT INTO tmp SELECT " + selection.toString() + " FROM " + tableName);
        db.execSQL("DROP TABLE " + tableName);
        db.execSQL("ALTER TABLE tmp RENAME TO " + tableName);
    }

    void deleteDb() {
        close();
        final File dbFile = context.getDatabasePath(getDatabaseName());
        if (!dbFile.delete()) {
            Log.e("ContentDb: cannot delete " + dbFile.getAbsolutePath());
        }
        final File walFile = new File(dbFile.getAbsolutePath() + "-wal");
        if (walFile.exists() && !walFile.delete()) {
            Log.e("ContentDb: cannot delete " + walFile.getAbsolutePath());
        }
        final File shmFile = new File(dbFile.getAbsolutePath() + "-shm");
        if (shmFile.exists() && !shmFile.delete()) {
            Log.e("ContentDb: cannot delete " + shmFile.getAbsolutePath());
        }
    }
}

package com.halloapp.content;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import androidx.annotation.NonNull;

import com.halloapp.Constants;
import com.halloapp.content.tables.ArchiveTable;
import com.halloapp.content.tables.AudienceTable;
import com.halloapp.content.tables.CallsTable;
import com.halloapp.content.tables.ChatsTable;
import com.halloapp.content.tables.CommentsTable;
import com.halloapp.content.tables.DeletedGroupNameTable;
import com.halloapp.content.tables.FutureProofTable;
import com.halloapp.content.tables.GroupMembersTable;
import com.halloapp.content.tables.GroupMessageSeenReceiptsTable;
import com.halloapp.content.tables.GroupsTable;
import com.halloapp.content.tables.HistoryRerequestTable;
import com.halloapp.content.tables.HistoryResendPayloadTable;
import com.halloapp.content.tables.KatchupMomentsTable;
import com.halloapp.content.tables.MediaTable;
import com.halloapp.content.tables.MentionsTable;
import com.halloapp.content.tables.MessagesTable;
import com.halloapp.content.tables.MomentsTable;
import com.halloapp.content.tables.OutgoingPlayedReceiptsTable;
import com.halloapp.content.tables.OutgoingSeenReceiptsTable;
import com.halloapp.content.tables.PostsTable;
import com.halloapp.content.tables.ReactionsTable;
import com.halloapp.content.tables.RepliesTable;
import com.halloapp.content.tables.RerequestsTable;
import com.halloapp.content.tables.ScreenshotsTable;
import com.halloapp.content.tables.SeenTable;
import com.halloapp.content.tables.UrlPreviewsTable;
import com.halloapp.util.logs.Log;

import java.io.File;

class ContentDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "content.db";
    private static final int DATABASE_VERSION = 92;

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
                + PostsTable.COLUMN_GROUP_ID + " TEXT,"
                + PostsTable.COLUMN_TYPE + " INTEGER DEFAULT 0,"
                + PostsTable.COLUMN_USAGE + " INTEGER DEFAULT 0,"
                + PostsTable.COLUMN_REREQUEST_COUNT + " INTEGER DEFAULT 0,"
                + PostsTable.COLUMN_PROTO_HASH + " BLOB,"
                + PostsTable.COLUMN_FAILURE_REASON + " TEXT,"
                + PostsTable.COLUMN_CLIENT_VERSION + " TEXT,"
                + PostsTable.COLUMN_SENDER_PLATFORM + " TEXT,"
                + PostsTable.COLUMN_SENDER_VERSION + " TEXT,"
                + PostsTable.COLUMN_RECEIVE_TIME + " INTEGER,"
                + PostsTable.COLUMN_RESULT_UPDATE_TIME + " INTEGER,"
                + PostsTable.COLUMN_SUBSCRIBED + " INTEGER,"
                + PostsTable.COLUMN_LAST_UPDATE + " INTEGER,"
                + PostsTable.COLUMN_EXTERNAL_SHARE_ID + " TEXT,"
                + PostsTable.COLUMN_EXTERNAL_SHARE_KEY + " TEXT,"
                + PostsTable.COLUMN_PSA_TAG + " TEXT,"
                + PostsTable.COLUMN_COMMENT_KEY + " BLOB,"
                + PostsTable.COLUMN_EXPIRATION_TIME + " INTEGER,"
                + PostsTable.COLUMN_FROM_HISTORY + " INTEGER,"
                + PostsTable.COLUMN_SHOW_SHARE_FOOTER + " INTEGER DEFAULT 0,"
                + PostsTable.COLUMN_EXPIRATION_MISMATCH + " INTEGER DEFAULT 0"
                + ");");

        db.execSQL("DROP TABLE IF EXISTS " + MomentsTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + MomentsTable.TABLE_NAME + " ("
                + MomentsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MomentsTable.COLUMN_POST_ID + " TEXT NOT NULL,"
                + MomentsTable.COLUMN_UNLOCKED_USER_ID + " TEXT,"
                + MomentsTable.COLUMN_SCREENSHOTTED + " INTEGER,"
                + MomentsTable.COLUMN_SELFIE_MEDIA_INDEX + " INTEGER DEFAULT 0,"
                + MomentsTable.COLUMN_LOCATION + " TEXT"
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + MomentsTable.INDEX_POST_KEY);
        db.execSQL("CREATE INDEX " + MomentsTable.INDEX_POST_KEY + " ON " + MomentsTable.TABLE_NAME + "("
                + MomentsTable.COLUMN_POST_ID
                + ");");

        db.execSQL("DROP TABLE IF EXISTS " + KatchupMomentsTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + KatchupMomentsTable.TABLE_NAME + " ("
                + KatchupMomentsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KatchupMomentsTable.COLUMN_POST_ID + " TEXT NOT NULL,"
                + KatchupMomentsTable.COLUMN_LOCATION + " TEXT,"
                + KatchupMomentsTable.COLUMN_NOTIFICATION_TIMESTAMP + " INTEGER,"
                + KatchupMomentsTable.COLUMN_SELFIE_X + " REAL,"
                + KatchupMomentsTable.COLUMN_SELFIE_Y + " REAL,"
                + KatchupMomentsTable.COLUMN_NOTIFICATION_ID + " INTEGER,"
                + KatchupMomentsTable.COLUMN_NUM_TAKES + " INTEGER,"
                + KatchupMomentsTable.COLUMN_NUM_SELFIE_TAKES + " INTEGER,"
                + KatchupMomentsTable.COLUMN_TIME_TAKEN + " INTEGER,"
                + KatchupMomentsTable.COLUMN_CONTENT_TYPE + " INTEGER"
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + KatchupMomentsTable.INDEX_POST_KEY);
        db.execSQL("CREATE INDEX " + KatchupMomentsTable.INDEX_POST_KEY + " ON " + KatchupMomentsTable.TABLE_NAME + "("
                + KatchupMomentsTable.COLUMN_POST_ID
                + ");");

        db.execSQL("DROP TABLE IF EXISTS " + ArchiveTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + ArchiveTable.TABLE_NAME + " ("
                + ArchiveTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + ArchiveTable.COLUMN_POST_ID + " TEXT NOT NULL,"
                + ArchiveTable.COLUMN_TIMESTAMP + " INTEGER,"
                + ArchiveTable.COLUMN_TEXT + " TEXT,"
                + ArchiveTable.COLUMN_GROUP_ID + " TEXT,"
                + ArchiveTable.COLUMN_ARCHIVE_TIMESTAMP + " INTEGER"
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + ArchiveTable.INDEX_POST_KEY);
        db.execSQL("CREATE UNIQUE INDEX " + ArchiveTable.INDEX_POST_KEY + " ON " + ArchiveTable.TABLE_NAME + "("
                + ArchiveTable.COLUMN_POST_ID
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + ArchiveTable.INDEX_TIMESTAMP);
        db.execSQL("CREATE INDEX " + ArchiveTable.INDEX_TIMESTAMP + " ON " + ArchiveTable.TABLE_NAME + "("
                + ArchiveTable.COLUMN_TIMESTAMP
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
                + MessagesTable.COLUMN_REREQUEST_COUNT + " INTEGER,"
                + MessagesTable.COLUMN_FAILURE_REASON + " TEXT,"
                + MessagesTable.COLUMN_CLIENT_VERSION + " TEXT,"
                + MessagesTable.COLUMN_SENDER_PLATFORM + " TEXT,"
                + MessagesTable.COLUMN_SENDER_VERSION + " TEXT,"
                + MessagesTable.COLUMN_RECEIVE_TIME + " INTEGER,"
                + MessagesTable.COLUMN_RESULT_UPDATE_TIME + " INTEGER"
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
                + ChatsTable.COLUMN_GROUP_AVATAR_ID + " TEXT,"
                + ChatsTable.COLUMN_THEME + " INTEGER DEFAULT 0,"
                + ChatsTable.COLUMN_INVITE_LINK + " TEXT"
                + ");");

        db.execSQL("DROP TABLE IF EXISTS " + GroupsTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + GroupsTable.TABLE_NAME + " ("
                + GroupsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + GroupsTable.COLUMN_GROUP_ID + " TEXT NOT NULL UNIQUE,"
                + GroupsTable.COLUMN_TIMESTAMP + " INTEGER,"
                + GroupsTable.COLUMN_GROUP_NAME + " TEXT,"
                + GroupsTable.COLUMN_IS_ACTIVE + " INTEGER DEFAULT 1,"
                + GroupsTable.COLUMN_GROUP_DESCRIPTION + " TEXT,"
                + GroupsTable.COLUMN_GROUP_AVATAR_ID + " TEXT,"
                + GroupsTable.COLUMN_THEME + " INTEGER DEFAULT 0,"
                + GroupsTable.COLUMN_EXPIRATION_TYPE + " INTEGER DEFAULT 0,"
                + GroupsTable.COLUMN_EXPIRATION_TIME + " INTEGER DEFAULT " + Constants.DEFAULT_GROUP_EXPIRATION_TIME + ","
                + GroupsTable.COLUMN_INVITE_LINK + " TEXT,"
                + GroupsTable.COLUMN_ADDED_TIMESTAMP + " INTEGER DEFAULT 0"
                + ");");

        db.execSQL("DROP TABLE IF EXISTS " + DeletedGroupNameTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + DeletedGroupNameTable.TABLE_NAME + " ("
                + DeletedGroupNameTable.COLUMN_CHAT_ID + " TEXT NOT NULL UNIQUE,"
                + DeletedGroupNameTable.COLUMN_CHAT_NAME + " TEXT"
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
                + RepliesTable.COLUMN_MEDIA_PREVIEW_FILE + " TEXT,"
                + RepliesTable.COLUMN_POST_TYPE + " INTEGER"
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + RepliesTable.INDEX_MESSAGE_KEY);
        db.execSQL("CREATE UNIQUE INDEX " + RepliesTable.INDEX_MESSAGE_KEY + " ON " + RepliesTable.TABLE_NAME + "("
                + RepliesTable.COLUMN_MESSAGE_ROW_ID
                + ");");

        db.execSQL("DROP TABLE IF EXISTS " + GroupMessageSeenReceiptsTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + GroupMessageSeenReceiptsTable.TABLE_NAME + " ("
                + GroupMessageSeenReceiptsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + GroupMessageSeenReceiptsTable.COLUMN_CONTENT_ID + " TEXT NOT NULL,"
                + GroupMessageSeenReceiptsTable.COLUMN_USER_ID + " TEXT NOT NULL,"
                + GroupMessageSeenReceiptsTable.COLUMN_STATE + " INTEGER,"
                + GroupMessageSeenReceiptsTable.COLUMN_TIMESTAMP + " INTEGER"
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + GroupMessageSeenReceiptsTable.INDEX_GROUP_MESSAGE_SEEN_RECEIPT_KEY);
        db.execSQL("CREATE INDEX " + GroupMessageSeenReceiptsTable.INDEX_GROUP_MESSAGE_SEEN_RECEIPT_KEY + " ON " + GroupMessageSeenReceiptsTable.TABLE_NAME + "("
                + GroupMessageSeenReceiptsTable.COLUMN_CONTENT_ID + ", " + GroupMessageSeenReceiptsTable.COLUMN_USER_ID
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
                + MediaTable.COLUMN_ENC_FILE + " FILE,"
                + MediaTable.COLUMN_PATCH_URL + " TEXT,"
                + MediaTable.COLUMN_ENC_KEY + " BLOB,"
                + MediaTable.COLUMN_SHA256_HASH + " BLOB,"
                + MediaTable.COLUMN_WIDTH + " INTEGER,"
                + MediaTable.COLUMN_HEIGHT + " INTEGER,"
                + MediaTable.COLUMN_UPLOAD_PROGRESS + " INTEGER DEFAULT 0,"
                + MediaTable.COLUMN_RETRY_COUNT + " INTEGER DEFAULT 0,"
                + MediaTable.COLUMN_DEC_SHA256_HASH + " BLOB,"
                + MediaTable.COLUMN_BLOB_VERSION + " INTEGER DEFAULT 0,"
                + MediaTable.COLUMN_CHUNK_SIZE + " INTEGER DEFAULT 0,"
                + MediaTable.COLUMN_BLOB_SIZE + " INTEGER DEFAULT 0,"
                + MediaTable.COLUMN_CHUNK_SET + " BLOB,"
                + MediaTable.COLUMN_PERCENT_TRANSFERRED + " INTEGER DEFAULT 0,"
                + MediaTable.COLUMN_DOWNLOAD_SIZE + " INTEGER"
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + MediaTable.INDEX_MEDIA_KEY);
        db.execSQL("CREATE INDEX " + MediaTable.INDEX_MEDIA_KEY + " ON " + MediaTable.TABLE_NAME + "("
                + MediaTable.COLUMN_PARENT_TABLE + ", "
                + MediaTable.COLUMN_PARENT_ROW_ID
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + MediaTable.INDEX_DEC_HASH_KEY);
        db.execSQL("CREATE INDEX " + MediaTable.INDEX_DEC_HASH_KEY + " ON " + MediaTable.TABLE_NAME + "("
                + MediaTable.COLUMN_DEC_SHA256_HASH
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
                + CommentsTable.COLUMN_POST_ID + " TEXT NOT NULL,"
                + CommentsTable.COLUMN_COMMENT_SENDER_USER_ID + " TEXT NOT NULL,"
                + CommentsTable.COLUMN_COMMENT_ID + " TEXT NOT NULL,"
                + CommentsTable.COLUMN_PARENT_ID + " INTEGER,"
                + CommentsTable.COLUMN_TIMESTAMP + " INTEGER,"
                + CommentsTable.COLUMN_TRANSFERRED + " INTEGER,"
                + CommentsTable.COLUMN_SEEN + " INTEGER,"
                + CommentsTable.COLUMN_PLAYED + " INTEGER,"
                + CommentsTable.COLUMN_TEXT + " TEXT,"
                + CommentsTable.COLUMN_TYPE + " INTEGER,"
                + CommentsTable.COLUMN_REREQUEST_COUNT + " INTEGER DEFAULT 0,"
                + CommentsTable.COLUMN_PROTO_HASH + " BLOB,"
                + CommentsTable.COLUMN_FAILURE_REASON + " TEXT,"
                + CommentsTable.COLUMN_CLIENT_VERSION + " TEXT,"
                + CommentsTable.COLUMN_SENDER_PLATFORM + " TEXT,"
                + CommentsTable.COLUMN_SENDER_VERSION + " TEXT,"
                + CommentsTable.COLUMN_RECEIVE_TIME + " INTEGER,"
                + CommentsTable.COLUMN_RESULT_UPDATE_TIME + " INTEGER,"
                + CommentsTable.COLUMN_SHOULD_NOTIFY + " INTEGER,"
                + CommentsTable.COLUMN_FROM_HISTORY + " INTEGER"
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + CommentsTable.INDEX_COMMENT_KEY);
        db.execSQL("CREATE UNIQUE INDEX " + CommentsTable.INDEX_COMMENT_KEY + " ON " + CommentsTable.TABLE_NAME + "("
                + CommentsTable.COLUMN_COMMENT_SENDER_USER_ID + ", "
                + CommentsTable.COLUMN_COMMENT_ID
                + ");");

        db.execSQL("DROP TABLE IF EXISTS " + RerequestsTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + RerequestsTable.TABLE_NAME + " ("
                + RerequestsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + RerequestsTable.COLUMN_CONTENT_ID + " TEXT NOT NULL,"
                + RerequestsTable.COLUMN_REQUESTOR_USER_ID + " TEXT NOT NULL,"
                + RerequestsTable.COLUMN_PARENT_TABLE + " TEXT NOT NULL,"
                + RerequestsTable.COLUMNT_REREQUEST_COUNT + " INTEGER"
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + RerequestsTable.INDEX_REREQUEST_KEY);
        db.execSQL("CREATE UNIQUE INDEX " + RerequestsTable.INDEX_REREQUEST_KEY + " ON " + RerequestsTable.TABLE_NAME + "("
                + RerequestsTable.COLUMN_CONTENT_ID + ", "
                + RerequestsTable.COLUMN_REQUESTOR_USER_ID + ", "
                + RerequestsTable.COLUMN_PARENT_TABLE
                + ");");

        db.execSQL("DROP TABLE IF EXISTS " + HistoryResendPayloadTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + HistoryResendPayloadTable.TABLE_NAME + " ("
                + HistoryResendPayloadTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + HistoryResendPayloadTable.COLUMN_HISTORY_RESEND_ID + " TEXT NOT NULL,"
                + HistoryResendPayloadTable.COLUMN_GROUP_ID + " TEXT NOT NULL,"
                + HistoryResendPayloadTable.COLUMN_PAYLOAD + " BLOB,"
                + HistoryResendPayloadTable.COLUMN_TIMESTAMP + " INTEGER"
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + HistoryResendPayloadTable.INDEX_HISTORY_RESEND_ID);
        db.execSQL("CREATE UNIQUE INDEX " + HistoryResendPayloadTable.INDEX_HISTORY_RESEND_ID + " ON " + HistoryResendPayloadTable.TABLE_NAME + "("
                + HistoryResendPayloadTable.COLUMN_HISTORY_RESEND_ID
                + ")");

        db.execSQL("DROP TABLE IF EXISTS " + HistoryRerequestTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + HistoryRerequestTable.TABLE_NAME + "("
                + HistoryRerequestTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + HistoryRerequestTable.COLUMN_HISTORY_RESEND_ID + " TEXT NOT NULL,"
                + HistoryRerequestTable.COLUMN_SENDER_USER_ID + " TEXT NOT NULL,"
                + HistoryRerequestTable.COLUMN_REREQUEST_COUNT + " INTEGER,"
                + HistoryRerequestTable.COLUMN_TIMESTAMP + " INTEGER"
                + ")");

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

        db.execSQL("DROP TABLE IF EXISTS " + ReactionsTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + ReactionsTable.TABLE_NAME + " ("
                + ReactionsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + ReactionsTable.COLUMN_REACTION_ID + " TEXT,"
                + ReactionsTable.COLUMN_CONTENT_ID + " TEXT NOT NULL,"
                + ReactionsTable.COLUMN_SENDER_USER_ID + " TEXT NOT NULL,"
                + ReactionsTable.COLUMN_REACTION_TYPE + " TEXT NOT NULL,"
                + ReactionsTable.COLUMN_TIMESTAMP + " INTEGER,"
                + ReactionsTable.COLUMN_SENT + " INTEGER DEFAULT 0,"
                + ReactionsTable.COLUMN_SEEN + " INTEGER DEFAULT 0"
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + ReactionsTable.INDEX_REACTION_KEY);
        db.execSQL("CREATE UNIQUE INDEX " + ReactionsTable.INDEX_REACTION_KEY + " ON " + ReactionsTable.TABLE_NAME + "("  //TODO: @Jack perf
                + ReactionsTable.COLUMN_SENDER_USER_ID + ", "
                + ReactionsTable.COLUMN_CONTENT_ID
                + ");");

        db.execSQL("DROP TABLE IF EXISTS " + ScreenshotsTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + ScreenshotsTable.TABLE_NAME + " ("
                + ScreenshotsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + ScreenshotsTable.COLUMN_POST_ID + " TEXT NOT NULL,"
                + ScreenshotsTable.COLUMN_SEEN_BY_USER_ID + " TEXT NOT NULL,"
                + ScreenshotsTable.COLUMN_TIMESTAMP
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + ScreenshotsTable.INDEX_SEEN_KEY);
        db.execSQL("CREATE UNIQUE INDEX " + ScreenshotsTable.INDEX_SEEN_KEY + " ON " + ScreenshotsTable.TABLE_NAME + "("
                + ScreenshotsTable.COLUMN_POST_ID + ", "
                + ScreenshotsTable.COLUMN_SEEN_BY_USER_ID
                + ");");

        db.execSQL("DROP TABLE IF EXISTS " + UrlPreviewsTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + UrlPreviewsTable.TABLE_NAME + " ("
                + UrlPreviewsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + UrlPreviewsTable.COLUMN_PARENT_TABLE + " TEXT NOT NULL,"
                + UrlPreviewsTable.COLUMN_PARENT_ROW_ID + " INTEGER,"
                + UrlPreviewsTable.COLUMN_TITLE + " TEXT,"
                + UrlPreviewsTable.COLUMN_URL + " TEXT,"
                + UrlPreviewsTable.COLUMN_DESCRIPTION + " TEXT"
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + UrlPreviewsTable.INDEX_URL_PREVIEW_KEY);
        db.execSQL("CREATE INDEX " + UrlPreviewsTable.INDEX_URL_PREVIEW_KEY + " ON " + UrlPreviewsTable.TABLE_NAME + "("
                + UrlPreviewsTable.COLUMN_PARENT_TABLE + ", "
                + UrlPreviewsTable.COLUMN_PARENT_ROW_ID
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

        db.execSQL("DROP TABLE IF EXISTS " + OutgoingPlayedReceiptsTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + OutgoingPlayedReceiptsTable.TABLE_NAME + " ("
                + OutgoingPlayedReceiptsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + OutgoingPlayedReceiptsTable.COLUMN_CHAT_ID + " TEXT NOT NULL,"
                + OutgoingPlayedReceiptsTable.COLUMN_SENDER_USER_ID + " TEXT NOT NULL,"
                + OutgoingPlayedReceiptsTable.COLUMN_CONTENT_ITEM_ID + " TEXT NOT NULL"
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + OutgoingPlayedReceiptsTable.INDEX_OUTGOING_RECEIPT_KEY);
        db.execSQL("CREATE UNIQUE INDEX " + OutgoingPlayedReceiptsTable.INDEX_OUTGOING_RECEIPT_KEY + " ON " + OutgoingPlayedReceiptsTable.TABLE_NAME + "("
                + OutgoingPlayedReceiptsTable.COLUMN_CHAT_ID + ", "
                + OutgoingPlayedReceiptsTable.COLUMN_SENDER_USER_ID + ", "
                + OutgoingPlayedReceiptsTable.COLUMN_CONTENT_ITEM_ID
                + ");");

        db.execSQL("DROP TABLE IF EXISTS " + FutureProofTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + FutureProofTable.TABLE_NAME + " ("
                + FutureProofTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + FutureProofTable.COLUMN_PARENT_TABLE + " TEXT NOT NULL,"
                + FutureProofTable.COLUMN_PARENT_ROW_ID + " INTEGER,"
                + FutureProofTable.COLUMN_CONTENT_BYTES + " BLOB"
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + FutureProofTable.INDEX_FUTURE_PROOF_KEY);
        db.execSQL("CREATE INDEX " + FutureProofTable.INDEX_FUTURE_PROOF_KEY + " ON " + FutureProofTable.TABLE_NAME + "("
                + FutureProofTable.COLUMN_PARENT_TABLE + ", "
                + FutureProofTable.COLUMN_PARENT_ROW_ID
                + ");");

        db.execSQL("DROP TABLE IF EXISTS " + CallsTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + CallsTable.TABLE_NAME + " ("
                + CallsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + CallsTable.COLUMN_CALL_ID + " TEXT NOT NULL,"
                + CallsTable.COLUMN_CALL_DURATION + " INTEGER"
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + CallsTable.INDEX_CALL_KEY);
        db.execSQL("CREATE INDEX " + CallsTable.INDEX_CALL_KEY + " ON " + CallsTable.TABLE_NAME + "("
                + CallsTable.COLUMN_CALL_ID
                + ");");

        db.execSQL("DROP TRIGGER IF EXISTS " + PostsTable.TRIGGER_DELETE);
        //noinspection SyntaxError
        db.execSQL("CREATE TRIGGER " + PostsTable.TRIGGER_DELETE + " AFTER DELETE ON " + PostsTable.TABLE_NAME + " "
                + "BEGIN "
                +   " DELETE FROM " + MediaTable.TABLE_NAME + " WHERE " + MediaTable.COLUMN_PARENT_ROW_ID + "=OLD." + PostsTable._ID + " AND " + MediaTable.COLUMN_PARENT_TABLE + "='" + PostsTable.TABLE_NAME + "'; "
                +   " DELETE FROM " + MentionsTable.TABLE_NAME + " WHERE " + MentionsTable.COLUMN_PARENT_ROW_ID + "=OLD." + PostsTable._ID + " AND " + MentionsTable.COLUMN_PARENT_TABLE + "='" + PostsTable.TABLE_NAME + "'; "
                +   " DELETE FROM " + CommentsTable.TABLE_NAME + " WHERE " + CommentsTable.COLUMN_POST_ID + "=OLD." + PostsTable.COLUMN_POST_ID + "; "
                +   " DELETE FROM " + SeenTable.TABLE_NAME + " WHERE " + SeenTable.COLUMN_POST_ID + "=OLD." + PostsTable.COLUMN_POST_ID + " AND ''=OLD." + PostsTable.COLUMN_SENDER_USER_ID + "; "
                +   " DELETE FROM " + ScreenshotsTable.TABLE_NAME + " WHERE " + ScreenshotsTable.COLUMN_POST_ID + "=OLD." + PostsTable.COLUMN_POST_ID + " AND ''=OLD." + PostsTable.COLUMN_SENDER_USER_ID + "; "
                +   " DELETE FROM " + AudienceTable.TABLE_NAME + " WHERE " + AudienceTable.COLUMN_POST_ID + "=OLD." + AudienceTable.COLUMN_POST_ID + "; "
                +   " DELETE FROM " + FutureProofTable.TABLE_NAME + " WHERE " + FutureProofTable.COLUMN_PARENT_ROW_ID + "=OLD." + PostsTable._ID + " AND " + FutureProofTable.COLUMN_PARENT_TABLE + "='" + PostsTable.TABLE_NAME + "'; "
                +   " DELETE FROM " + UrlPreviewsTable.TABLE_NAME + " WHERE " + UrlPreviewsTable.COLUMN_PARENT_ROW_ID + "=OLD." + PostsTable._ID + " AND " + UrlPreviewsTable.COLUMN_PARENT_TABLE + "='" + PostsTable.TABLE_NAME + "'; "
                +   " DELETE FROM " + RerequestsTable.TABLE_NAME + " WHERE " + RerequestsTable.COLUMN_CONTENT_ID + "=OLD." + PostsTable.COLUMN_POST_ID + " AND " + RerequestsTable.COLUMN_PARENT_TABLE + "='" + PostsTable.TABLE_NAME + "';"
                +   " DELETE FROM " + MomentsTable.TABLE_NAME + " WHERE " + MomentsTable.COLUMN_POST_ID + "=OLD." + PostsTable.COLUMN_POST_ID + ";"
                + "END;");

        db.execSQL("DROP TRIGGER IF EXISTS " + MessagesTable.TRIGGER_DELETE);
        db.execSQL("CREATE TRIGGER " + MessagesTable.TRIGGER_DELETE + " AFTER DELETE ON " + MessagesTable.TABLE_NAME + " "
                + "BEGIN "
                +   " DELETE FROM " + MediaTable.TABLE_NAME + " WHERE " + MediaTable.COLUMN_PARENT_ROW_ID + "=OLD." + MessagesTable._ID + " AND " + MediaTable.COLUMN_PARENT_TABLE + "='" + MessagesTable.TABLE_NAME + "'; "
                +   " DELETE FROM " + MentionsTable.TABLE_NAME + " WHERE " + MentionsTable.COLUMN_PARENT_ROW_ID + "=OLD." + MessagesTable._ID + " AND " + MentionsTable.COLUMN_PARENT_TABLE + "='" + MentionsTable.TABLE_NAME + "'; "
                +   " DELETE FROM " + FutureProofTable.TABLE_NAME + " WHERE " + FutureProofTable.COLUMN_PARENT_ROW_ID + "=OLD." + MessagesTable._ID + " AND " + FutureProofTable.COLUMN_PARENT_TABLE + "='" + MessagesTable.TABLE_NAME + "'; "
                +   " DELETE FROM " + UrlPreviewsTable.TABLE_NAME + " WHERE " + UrlPreviewsTable.COLUMN_PARENT_ROW_ID + "=OLD." + MessagesTable._ID + " AND " + UrlPreviewsTable.COLUMN_PARENT_TABLE + "='" + MessagesTable.TABLE_NAME + "'; "
                +   " DELETE FROM " + CallsTable.TABLE_NAME + " WHERE " + CallsTable.COLUMN_CALL_ID + "=OLD." + MessagesTable.COLUMN_MESSAGE_ID + "; "
                +   " DELETE FROM " + GroupMessageSeenReceiptsTable.TABLE_NAME + " WHERE " + GroupMessageSeenReceiptsTable.COLUMN_CONTENT_ID + "=OLD." + MessagesTable.COLUMN_MESSAGE_ID + "; "
                + "END;");

        db.execSQL("DROP TRIGGER IF EXISTS " + CommentsTable.TRIGGER_DELETE);
        db.execSQL("CREATE TRIGGER " + CommentsTable.TRIGGER_DELETE + " AFTER DELETE ON " + CommentsTable.TABLE_NAME + " "
                + "BEGIN "
                +   "DELETE FROM " + MentionsTable.TABLE_NAME + " WHERE " + MentionsTable.COLUMN_PARENT_ROW_ID + "=OLD." + CommentsTable._ID + " AND " + MentionsTable.COLUMN_PARENT_TABLE + "='" + MentionsTable.TABLE_NAME + "'; "
                +   "DELETE FROM " + FutureProofTable.TABLE_NAME + " WHERE " + FutureProofTable.COLUMN_PARENT_ROW_ID + "=OLD." + CommentsTable._ID + " AND " + FutureProofTable.COLUMN_PARENT_TABLE + "='" + CommentsTable.TABLE_NAME + "'; "
                +   "DELETE FROM " + UrlPreviewsTable.TABLE_NAME + " WHERE " + UrlPreviewsTable.COLUMN_PARENT_ROW_ID + "=OLD." + CommentsTable._ID + " AND " + UrlPreviewsTable.COLUMN_PARENT_TABLE + "='" + CommentsTable.TABLE_NAME + "'; "
                +   "DELETE FROM " + RerequestsTable.TABLE_NAME + " WHERE " + RerequestsTable.COLUMN_CONTENT_ID + "=OLD." + CommentsTable.COLUMN_COMMENT_ID + " AND " + RerequestsTable.COLUMN_PARENT_TABLE + "='" + CommentsTable.TABLE_NAME + "'; "
                + "END;");

        db.execSQL("DROP TRIGGER IF EXISTS " + GroupsTable.TRIGGER_DELETE);
        db.execSQL("CREATE TRIGGER " + GroupsTable.TRIGGER_DELETE + " AFTER DELETE ON " + GroupsTable.TABLE_NAME + " "
                + "BEGIN "
                +   "DELETE FROM " + GroupMembersTable.TABLE_NAME + " WHERE " + GroupMembersTable.COLUMN_GROUP_ID + "=OLD." + GroupsTable.COLUMN_GROUP_ID + "; "
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
            case 31: {
                upgradeFromVersion31(db);
            }
            case 32: {
                upgradeFromVersion32(db);
            }
            case 33: {
                upgradeFromVersion33(db);
            }
            case 34: {
                upgradeFromVersion34(db);
            }
            case 35: {
                upgradeFromVersion35(db);
            }
            case 36: {
                upgradeFromVersion36(db);
            }
            case 37: {
                upgradeFromVersion37(db);
            }
            case 38: {
                upgradeFromVersion38(db);
            }
            case 39: {
                upgradeFromVersion39(db);
            }
            case 40: {
                upgradeFromVersion40(db);
            }
            case 41: {
                upgradeFromVersion41(db);
            }
            case 42: {
                upgradeFromVersion42(db);
            }
            case 43: {
                upgradeFromVersion43(db);
            }
            case 44: {
                upgradeFromVersion44(db);
            }
            case 45: {
                upgradeFromVersion45(db);
            }
            case 46: {
                upgradeFromVersion46(db);
            }
            case 47: {
                upgradeFromVersion47(db);
            }
            case 48: {
                upgradeFromVersion48(db);
            }
            case 49: {
                upgradeFromVersion49(db);
            }
            case 50: {
                upgradeFromVersion50(db);
            }
            case 51: {
                upgradeFromVersion51(db);
            }
            case 52: {
                upgradeFromVersion52(db);
            }
            case 53: {
                upgradeFromVersion53(db);
            }
            case 54: {
                upgradeFromVersion54(db);
            }
            case 55: {
                upgradeFromVersion55(db);
            }
            case 56: {
                upgradeFromVersion56(db);
            }
            case 57: {
                upgradeFromVersion57(db);
            }
            case 58: {
                upgradeFromVersion58(db);
            }
            case 59: {
                upgradeFromVersion59(db);
            }
            case 60: {
                upgradeFromVersion60(db);
            }
            case 61: {
                upgradeFromVersion61(db);
            }
            case 62: {
                upgradeFromVersion62(db);
            }
            case 63: {
                upgradeFromVersion63(db);
            }
            case 64: {
                upgradeFromVersion64(db);
            }
            case 65: {
                upgradeFromVersion65(db);
            }
            case 66: {
                upgradeFromVersion66(db);
            }
            case 67: {
                upgradeFromVersion67(db);
            }
            case 68: {
                upgradeFromVersion68(db);
            }
            case 69: {
                upgradeFromVersion69(db);
            }
            case 70: {
                upgradeFromVersion70(db);
            }
            case 71: {
                upgradeFromVersion71(db);
            }
            case 72: {
                upgradeFromVersion72(db);
            }
            case 73: {
                upgradeFromVersion73(db);
            }
            case 74: {
                upgradeFromVersion74(db);
            }
            case 75: {
                upgradeFromVersion75(db);
            }
            case 76: {
                upgradeFromVersion76(db);
            }
            case 77: {
                upgradeFromVersion77(db);
            }
            case 78: {
                upgradeFromVersion78(db);
            }
            case 79: {
                upgradeFromVersion79(db);
            }
            case 80: {
                upgradeFromVersion80(db);
            }
            case 81: {
                upgradeFromVersion81(db);
            }
            case 82: {
                upgradeFromVersion82(db);
            }
            case 83: {
                upgradeFromVersion83(db);
            }
            case 84: {
                upgradeFromVersion84(db);
            }
            case 85: {
                upgradeFromVersion85(db);
            }
            case 86: {
                upgradeFromVersion86(db);
            }
            case 87: {
                upgradeFromVersion87(db);
            }
            case 88: {
                upgradeFromVersion88(db);
            }
            case 89: {
                upgradeFromVersion89(db);
            }
            case 90: {
                upgradeFromVersion90(db);
            }
            case 91: {
                upgradeFromVersion91(db);
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
        throw new DowngradeAttemptedException("From " + oldVersion + " to " + newVersion);
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

        recreateTable(db, MessagesTable.TABLE_NAME, new String [] {
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

        recreateTable(db, MessagesTable.TABLE_NAME, new String [] {
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
                + " DELETE FROM " + CommentsTable.TABLE_NAME + " WHERE " + CommentsTable.COLUMN_POST_ID + "=OLD." + PostsTable.COLUMN_POST_ID + " AND post_sender_user_id=OLD." + PostsTable.COLUMN_SENDER_USER_ID + "; "
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
                +   " DELETE FROM " + CommentsTable.TABLE_NAME + " WHERE " + CommentsTable.COLUMN_POST_ID + "=OLD." + PostsTable.COLUMN_POST_ID + " AND post_sender_user_id=OLD." + PostsTable.COLUMN_SENDER_USER_ID + "; "
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

    private void upgradeFromVersion31(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + MediaTable.TABLE_NAME + " ADD COLUMN " + MediaTable.COLUMN_ENC_FILE + " FILE");
    }

    private void upgradeFromVersion32(@NonNull SQLiteDatabase db) {
        recreateTable(db, CommentsTable.TABLE_NAME, new String[]{
                CommentsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT",
                CommentsTable.COLUMN_POST_ID + " TEXT NOT NULL",
                CommentsTable.COLUMN_COMMENT_SENDER_USER_ID + " TEXT NOT NULL",
                CommentsTable.COLUMN_COMMENT_ID + " TEXT NOT NULL",
                CommentsTable.COLUMN_PARENT_ID + " INTEGER",
                CommentsTable.COLUMN_TIMESTAMP + " INTEGER",
                CommentsTable.COLUMN_TRANSFERRED + " INTEGER",
                CommentsTable.COLUMN_SEEN + " INTEGER",
                CommentsTable.COLUMN_TEXT + " TEXT"});
    }

    private void upgradeFromVersion33(@NonNull SQLiteDatabase db) {
        // We need to recreate the comment trigger as well
        db.execSQL("DROP TRIGGER IF EXISTS " + CommentsTable.TRIGGER_DELETE);
        db.execSQL("CREATE TRIGGER " + CommentsTable.TRIGGER_DELETE + " AFTER DELETE ON " + CommentsTable.TABLE_NAME + " "
                + "BEGIN "
                +   "DELETE FROM " + MentionsTable.TABLE_NAME + " WHERE " + MentionsTable.COLUMN_PARENT_ROW_ID + "=OLD." + CommentsTable._ID + " AND " + MentionsTable.COLUMN_PARENT_TABLE + "='" + MentionsTable.TABLE_NAME + "'; "
                + "END;");

        // Recreating posts trigger in case we still have the one from version 25
        db.execSQL("DROP TRIGGER IF EXISTS " + PostsTable.TRIGGER_DELETE);
        //noinspection SyntaxError
        db.execSQL("CREATE TRIGGER " + PostsTable.TRIGGER_DELETE + " AFTER DELETE ON " + PostsTable.TABLE_NAME + " "
                + "BEGIN "
                +   " DELETE FROM " + MediaTable.TABLE_NAME + " WHERE " + MediaTable.COLUMN_PARENT_ROW_ID + "=OLD." + PostsTable._ID + " AND " + MediaTable.COLUMN_PARENT_TABLE + "='" + PostsTable.TABLE_NAME + "'; "
                +   " DELETE FROM " + MentionsTable.TABLE_NAME + " WHERE " + MentionsTable.COLUMN_PARENT_ROW_ID + "=OLD." + PostsTable._ID + " AND " + MentionsTable.COLUMN_PARENT_TABLE + "='" + PostsTable.TABLE_NAME + "'; "
                +   " DELETE FROM " + CommentsTable.TABLE_NAME + " WHERE " + CommentsTable.COLUMN_POST_ID + "=OLD." + PostsTable.COLUMN_POST_ID + "; "
                +   " DELETE FROM " + SeenTable.TABLE_NAME + " WHERE " + SeenTable.COLUMN_POST_ID + "=OLD." + PostsTable.COLUMN_POST_ID + " AND ''=OLD." + PostsTable.COLUMN_SENDER_USER_ID + "; "
                +   " DELETE FROM " + AudienceTable.TABLE_NAME + " WHERE " + AudienceTable.COLUMN_POST_ID + "=OLD." + AudienceTable.COLUMN_POST_ID + "; "
                + "END;");
    }

    private void upgradeFromVersion34(@NonNull SQLiteDatabase db) {
        // Remove constraint violations in comments table before adding back the index
        db.execSQL("DELETE FROM " + CommentsTable.TABLE_NAME + " WHERE " + BaseColumns._ID + " NOT IN "
                + "(SELECT MIN(" + BaseColumns._ID + ") FROM " + CommentsTable.TABLE_NAME + " GROUP BY " + CommentsTable.COLUMN_COMMENT_ID + "," + CommentsTable.COLUMN_COMMENT_SENDER_USER_ID + ")");

        db.execSQL("DROP INDEX IF EXISTS " + CommentsTable.INDEX_COMMENT_KEY);
        db.execSQL("CREATE UNIQUE INDEX " + CommentsTable.INDEX_COMMENT_KEY + " ON " + CommentsTable.TABLE_NAME + "("
                + CommentsTable.COLUMN_COMMENT_SENDER_USER_ID + ", "
                + CommentsTable.COLUMN_COMMENT_ID
                + ");");
    }

    private void upgradeFromVersion35(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + PostsTable.TABLE_NAME + " ADD COLUMN " + PostsTable.COLUMN_TYPE + " INTEGER DEFAULT 0");
        db.execSQL("ALTER TABLE " + PostsTable.TABLE_NAME + " ADD COLUMN " + PostsTable.COLUMN_USAGE + " INTEGER DEFAULT 0");
    }

    private void upgradeFromVersion36(@NonNull SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + "silent_messages");
        db.execSQL("CREATE TABLE " + "silent_messages" + " ("
                + "_id" + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "sender_user_id" + " TEXT NOT NULL,"
                + "message_id" + " TEXT NOT NULL,"
                + "rerequest_count" + " INTEGER"
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + "silent_message_key");
        db.execSQL("CREATE UNIQUE INDEX " + "silent_message_key" + " ON " + "silent_messages" + "("
                + "sender_user_id" + ", "
                + "message_id"
                + ");");
    }

    private void upgradeFromVersion37(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + MessagesTable.TABLE_NAME + " ADD COLUMN " + MessagesTable.COLUMN_FAILURE_REASON + " TEXT");
        db.execSQL("ALTER TABLE " + MessagesTable.TABLE_NAME + " ADD COLUMN " + MessagesTable.COLUMN_CLIENT_VERSION + " TEXT");
        db.execSQL("ALTER TABLE " + MessagesTable.TABLE_NAME + " ADD COLUMN " + MessagesTable.COLUMN_SENDER_PLATFORM + " TEXT");
        db.execSQL("ALTER TABLE " + MessagesTable.TABLE_NAME + " ADD COLUMN " + MessagesTable.COLUMN_SENDER_VERSION + " TEXT");
        db.execSQL("ALTER TABLE " + MessagesTable.TABLE_NAME + " ADD COLUMN " + MessagesTable.COLUMN_RECEIVE_TIME + " INTEGER");
        db.execSQL("ALTER TABLE " + MessagesTable.TABLE_NAME + " ADD COLUMN " + MessagesTable.COLUMN_RESULT_UPDATE_TIME + " INTEGER");
    }

    private void upgradeFromVersion38(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + "silent_messages" + " ADD COLUMN " + "failure_reason" + " TEXT");
        db.execSQL("ALTER TABLE " + "silent_messages" + " ADD COLUMN " + "client_version" + " TEXT");
        db.execSQL("ALTER TABLE " + "silent_messages" + " ADD COLUMN " + "sender_platform" + " TEXT");
        db.execSQL("ALTER TABLE " + "silent_messages" + " ADD COLUMN " + "sender_version" + " TEXT");
        db.execSQL("ALTER TABLE " + "silent_messages" + " ADD COLUMN " + "receive_time" + " INTEGER");
        db.execSQL("ALTER TABLE " + "silent_messages" + " ADD COLUMN " + "result_update_time" + " INTEGER");
    }

    private void upgradeFromVersion39(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + ChatsTable.TABLE_NAME + " ADD COLUMN " + ChatsTable.COLUMN_THEME + " INTEGER DEFAULT 0");
    }

    private void upgradeFromVersion40(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + MediaTable.TABLE_NAME + " ADD COLUMN " + MediaTable.COLUMN_DEC_SHA256_HASH + " BLOB");

        db.execSQL("DROP INDEX IF EXISTS " + MediaTable.INDEX_DEC_HASH_KEY);
        db.execSQL("CREATE INDEX " + MediaTable.INDEX_DEC_HASH_KEY + " ON " + MediaTable.TABLE_NAME + "("
                + MediaTable.COLUMN_DEC_SHA256_HASH
                + ");");
    }

    private void upgradeFromVersion41(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + CommentsTable.TABLE_NAME + " ADD COLUMN " + CommentsTable.COLUMN_TYPE + " INTEGER DEFAULT 0");

        db.execSQL("DROP TABLE IF EXISTS " + FutureProofTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + FutureProofTable.TABLE_NAME + " ("
                + FutureProofTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + FutureProofTable.COLUMN_PARENT_TABLE + " TEXT NOT NULL,"
                + FutureProofTable.COLUMN_PARENT_ROW_ID + " INTEGER,"
                + FutureProofTable.COLUMN_CONTENT_BYTES + " BLOB"
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + FutureProofTable.INDEX_FUTURE_PROOF_KEY);
        db.execSQL("CREATE INDEX " + FutureProofTable.INDEX_FUTURE_PROOF_KEY + " ON " + FutureProofTable.TABLE_NAME + "("
                + FutureProofTable.COLUMN_PARENT_TABLE + ", "
                + FutureProofTable.COLUMN_PARENT_ROW_ID
                + ");");

        // Update triggers to also delete from future proof table
        db.execSQL("DROP TRIGGER IF EXISTS " + PostsTable.TRIGGER_DELETE);
        //noinspection SyntaxError
        db.execSQL("CREATE TRIGGER " + PostsTable.TRIGGER_DELETE + " AFTER DELETE ON " + PostsTable.TABLE_NAME + " "
                + "BEGIN "
                + " DELETE FROM " + MediaTable.TABLE_NAME + " WHERE " + MediaTable.COLUMN_PARENT_ROW_ID + "=OLD." + PostsTable._ID + " AND " + MediaTable.COLUMN_PARENT_TABLE + "='" + PostsTable.TABLE_NAME + "'; "
                + " DELETE FROM " + MentionsTable.TABLE_NAME + " WHERE " + MentionsTable.COLUMN_PARENT_ROW_ID + "=OLD." + PostsTable._ID + " AND " + MentionsTable.COLUMN_PARENT_TABLE + "='" + PostsTable.TABLE_NAME + "'; "
                + " DELETE FROM " + CommentsTable.TABLE_NAME + " WHERE " + CommentsTable.COLUMN_POST_ID + "=OLD." + PostsTable.COLUMN_POST_ID + "; "
                + " DELETE FROM " + SeenTable.TABLE_NAME + " WHERE " + SeenTable.COLUMN_POST_ID + "=OLD." + PostsTable.COLUMN_POST_ID + " AND ''=OLD." + PostsTable.COLUMN_SENDER_USER_ID + "; "
                + " DELETE FROM " + AudienceTable.TABLE_NAME + " WHERE " + AudienceTable.COLUMN_POST_ID + "=OLD." + AudienceTable.COLUMN_POST_ID + "; "
                + " DELETE FROM " + FutureProofTable.TABLE_NAME + " WHERE " + FutureProofTable.COLUMN_PARENT_ROW_ID + "=OLD." + PostsTable._ID + " AND " + FutureProofTable.COLUMN_PARENT_TABLE + "='" + PostsTable.TABLE_NAME + "'; "
                + "END;");

        db.execSQL("DROP TRIGGER IF EXISTS " + MessagesTable.TRIGGER_DELETE);
        db.execSQL("CREATE TRIGGER " + MessagesTable.TRIGGER_DELETE + " AFTER DELETE ON " + MessagesTable.TABLE_NAME + " "
                + "BEGIN "
                + " DELETE FROM " + MediaTable.TABLE_NAME + " WHERE " + MediaTable.COLUMN_PARENT_ROW_ID + "=OLD." + MessagesTable._ID + " AND " + MediaTable.COLUMN_PARENT_TABLE + "='" + MessagesTable.TABLE_NAME + "'; "
                + " DELETE FROM " + MentionsTable.TABLE_NAME + " WHERE " + MentionsTable.COLUMN_PARENT_ROW_ID + "=OLD." + MessagesTable._ID + " AND " + MentionsTable.COLUMN_PARENT_TABLE + "='" + MentionsTable.TABLE_NAME + "'; "
                + " DELETE FROM " + FutureProofTable.TABLE_NAME + " WHERE " + FutureProofTable.COLUMN_PARENT_ROW_ID + "=OLD." + MessagesTable._ID + " AND " + FutureProofTable.COLUMN_PARENT_TABLE + "='" + MessagesTable.TABLE_NAME + "'; "
                + "END;");

        db.execSQL("DROP TRIGGER IF EXISTS " + CommentsTable.TRIGGER_DELETE);
        db.execSQL("CREATE TRIGGER " + CommentsTable.TRIGGER_DELETE + " AFTER DELETE ON " + CommentsTable.TABLE_NAME + " "
                + "BEGIN "
                + "DELETE FROM " + MentionsTable.TABLE_NAME + " WHERE " + MentionsTable.COLUMN_PARENT_ROW_ID + "=OLD." + CommentsTable._ID + " AND " + MentionsTable.COLUMN_PARENT_TABLE + "='" + CommentsTable.TABLE_NAME + "'; "
                + "DELETE FROM " + FutureProofTable.TABLE_NAME + " WHERE " + FutureProofTable.COLUMN_PARENT_ROW_ID + "=OLD." + CommentsTable._ID + " AND " + FutureProofTable.COLUMN_PARENT_TABLE + "='" + CommentsTable.TABLE_NAME + "'; "
                + "END;");
    }

    private void upgradeFromVersion42(@NonNull SQLiteDatabase db) {
        db.execSQL("DROP INDEX IF EXISTS " + "silent_message_key");
        db.execSQL("DROP TABLE IF EXISTS " + "silent_messages");
    }

    private void upgradeFromVersion43(@NonNull SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + ArchiveTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + ArchiveTable.TABLE_NAME + " ("
                + ArchiveTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + ArchiveTable.COLUMN_POST_ID + " TEXT NOT NULL,"
                + ArchiveTable.COLUMN_TIMESTAMP + " INTEGER,"
                + ArchiveTable.COLUMN_TEXT + " TEXT,"
                + ArchiveTable.COLUMN_GROUP_ID + " TEXT,"
                + ArchiveTable.COLUMN_ARCHIVE_TIMESTAMP + " INTEGER"
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + ArchiveTable.INDEX_POST_KEY);
        db.execSQL("CREATE UNIQUE INDEX " + ArchiveTable.INDEX_POST_KEY + " ON " + ArchiveTable.TABLE_NAME + "("
                + ArchiveTable.COLUMN_POST_ID
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + ArchiveTable.INDEX_TIMESTAMP);
        db.execSQL("CREATE INDEX " + ArchiveTable.INDEX_TIMESTAMP + " ON " + ArchiveTable.TABLE_NAME + "("
                + ArchiveTable.COLUMN_TIMESTAMP
                + ");");
    }

    private void upgradeFromVersion44(@NonNull SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + DeletedGroupNameTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + DeletedGroupNameTable.TABLE_NAME + " ("
                + DeletedGroupNameTable.COLUMN_CHAT_ID + " TEXT NOT NULL UNIQUE,"
                + DeletedGroupNameTable.COLUMN_CHAT_NAME + " TEXT"
                + ")");
    }

    private void upgradeFromVersion45(@NonNull SQLiteDatabase db) {
        // ArchiveTable's indexes overwrote PostsTable's, allowing constraint violations; restore indexes

        db.execSQL("DELETE FROM " + PostsTable.TABLE_NAME + " WHERE " + PostsTable._ID + " NOT IN ("
                + "SELECT MIN(_id) FROM " + PostsTable.TABLE_NAME + " GROUP BY " + PostsTable.COLUMN_SENDER_USER_ID + "," + PostsTable.COLUMN_POST_ID
                + ");");
        db.execSQL("DELETE FROM " + ArchiveTable.TABLE_NAME + " WHERE " + ArchiveTable._ID + " NOT IN ("
                + "SELECT MIN(_id) FROM " + ArchiveTable.TABLE_NAME + " GROUP BY " + ArchiveTable.COLUMN_POST_ID
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

        db.execSQL("DROP INDEX IF EXISTS " + ArchiveTable.INDEX_POST_KEY);
        db.execSQL("CREATE UNIQUE INDEX " + ArchiveTable.INDEX_POST_KEY + " ON " + ArchiveTable.TABLE_NAME + "("
                + ArchiveTable.COLUMN_POST_ID
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + ArchiveTable.INDEX_TIMESTAMP);
        db.execSQL("CREATE INDEX " + ArchiveTable.INDEX_TIMESTAMP + " ON " + ArchiveTable.TABLE_NAME + "("
                + ArchiveTable.COLUMN_TIMESTAMP
                + ");");
    }

    private void upgradeFromVersion46(@NonNull SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + OutgoingPlayedReceiptsTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + OutgoingPlayedReceiptsTable.TABLE_NAME + " ("
                + OutgoingPlayedReceiptsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + OutgoingPlayedReceiptsTable.COLUMN_CHAT_ID + " TEXT NOT NULL,"
                + OutgoingPlayedReceiptsTable.COLUMN_SENDER_USER_ID + " TEXT NOT NULL,"
                + OutgoingPlayedReceiptsTable.COLUMN_CONTENT_ITEM_ID + " TEXT NOT NULL"
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + OutgoingPlayedReceiptsTable.INDEX_OUTGOING_RECEIPT_KEY);
        db.execSQL("CREATE UNIQUE INDEX " + OutgoingPlayedReceiptsTable.INDEX_OUTGOING_RECEIPT_KEY + " ON " + OutgoingPlayedReceiptsTable.TABLE_NAME + "("
                + OutgoingPlayedReceiptsTable.COLUMN_CHAT_ID + ", "
                + OutgoingPlayedReceiptsTable.COLUMN_SENDER_USER_ID + ", "
                + OutgoingPlayedReceiptsTable.COLUMN_CONTENT_ITEM_ID
                + ");");
    }

    private void upgradeFromVersion47(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + MediaTable.TABLE_NAME + " ADD COLUMN " + MediaTable.COLUMN_BLOB_VERSION + " INTEGER DEFAULT 0");
        db.execSQL("ALTER TABLE " + MediaTable.TABLE_NAME + " ADD COLUMN " + MediaTable.COLUMN_CHUNK_SIZE + " INTEGER DEFAULT 0");
        db.execSQL("ALTER TABLE " + MediaTable.TABLE_NAME + " ADD COLUMN " + MediaTable.COLUMN_BLOB_SIZE + " INTEGER DEFAULT 0");
    }

    private void upgradeFromVersion48(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + PostsTable.TABLE_NAME + " ADD COLUMN " + PostsTable.COLUMN_REREQUEST_COUNT + " INTEGER DEFAULT 0");
        db.execSQL("ALTER TABLE " + CommentsTable.TABLE_NAME + " ADD COLUMN " + CommentsTable.COLUMN_REREQUEST_COUNT + " INTEGER DEFAULT 0");
    }

    private void upgradeFromVersion49(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + PostsTable.TABLE_NAME + " ADD COLUMN " + PostsTable.COLUMN_FAILURE_REASON + " TEXT");
        db.execSQL("ALTER TABLE " + PostsTable.TABLE_NAME + " ADD COLUMN " + PostsTable.COLUMN_CLIENT_VERSION + " TEXT");
        db.execSQL("ALTER TABLE " + PostsTable.TABLE_NAME + " ADD COLUMN " + PostsTable.COLUMN_RECEIVE_TIME + " INTEGER");
        db.execSQL("ALTER TABLE " + PostsTable.TABLE_NAME + " ADD COLUMN " + PostsTable.COLUMN_RESULT_UPDATE_TIME + " INTEGER");

        db.execSQL("ALTER TABLE " + CommentsTable.TABLE_NAME + " ADD COLUMN " + CommentsTable.COLUMN_FAILURE_REASON + " TEXT");
        db.execSQL("ALTER TABLE " + CommentsTable.TABLE_NAME + " ADD COLUMN " + CommentsTable.COLUMN_CLIENT_VERSION + " TEXT");
        db.execSQL("ALTER TABLE " + CommentsTable.TABLE_NAME + " ADD COLUMN " + CommentsTable.COLUMN_RECEIVE_TIME + " INTEGER");
        db.execSQL("ALTER TABLE " + CommentsTable.TABLE_NAME + " ADD COLUMN " + CommentsTable.COLUMN_RESULT_UPDATE_TIME + " INTEGER");
    }

    private void upgradeFromVersion50(@NonNull SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + UrlPreviewsTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + UrlPreviewsTable.TABLE_NAME + " ("
                + UrlPreviewsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + UrlPreviewsTable.COLUMN_PARENT_TABLE + " TEXT NOT NULL,"
                + UrlPreviewsTable.COLUMN_PARENT_ROW_ID + " INTEGER,"
                + UrlPreviewsTable.COLUMN_TITLE + " TEXT,"
                + UrlPreviewsTable.COLUMN_URL + " TEXT"
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + UrlPreviewsTable.INDEX_URL_PREVIEW_KEY);
        db.execSQL("CREATE INDEX " + UrlPreviewsTable.INDEX_URL_PREVIEW_KEY + " ON " + UrlPreviewsTable.TABLE_NAME + "("
                + UrlPreviewsTable.COLUMN_PARENT_TABLE + ", "
                + UrlPreviewsTable.COLUMN_PARENT_ROW_ID
                + ");");


        db.execSQL("DROP TRIGGER IF EXISTS " + PostsTable.TRIGGER_DELETE);
        //noinspection SyntaxError
        db.execSQL("CREATE TRIGGER " + PostsTable.TRIGGER_DELETE + " AFTER DELETE ON " + PostsTable.TABLE_NAME + " "
                + "BEGIN "
                +   " DELETE FROM " + MediaTable.TABLE_NAME + " WHERE " + MediaTable.COLUMN_PARENT_ROW_ID + "=OLD." + PostsTable._ID + " AND " + MediaTable.COLUMN_PARENT_TABLE + "='" + PostsTable.TABLE_NAME + "'; "
                +   " DELETE FROM " + MentionsTable.TABLE_NAME + " WHERE " + MentionsTable.COLUMN_PARENT_ROW_ID + "=OLD." + PostsTable._ID + " AND " + MentionsTable.COLUMN_PARENT_TABLE + "='" + PostsTable.TABLE_NAME + "'; "
                +   " DELETE FROM " + CommentsTable.TABLE_NAME + " WHERE " + CommentsTable.COLUMN_POST_ID + "=OLD." + PostsTable.COLUMN_POST_ID + "; "
                +   " DELETE FROM " + SeenTable.TABLE_NAME + " WHERE " + SeenTable.COLUMN_POST_ID + "=OLD." + PostsTable.COLUMN_POST_ID + " AND ''=OLD." + PostsTable.COLUMN_SENDER_USER_ID + "; "
                +   " DELETE FROM " + AudienceTable.TABLE_NAME + " WHERE " + AudienceTable.COLUMN_POST_ID + "=OLD." + AudienceTable.COLUMN_POST_ID + "; "
                +   " DELETE FROM " + FutureProofTable.TABLE_NAME + " WHERE " + FutureProofTable.COLUMN_PARENT_ROW_ID + "=OLD." + PostsTable._ID + " AND " + FutureProofTable.COLUMN_PARENT_TABLE + "='" + PostsTable.TABLE_NAME + "'; "
                +   " DELETE FROM " + UrlPreviewsTable.TABLE_NAME + " WHERE " + UrlPreviewsTable.COLUMN_PARENT_ROW_ID + "=OLD." + PostsTable._ID + " AND " + UrlPreviewsTable.COLUMN_PARENT_TABLE + "='" + PostsTable.TABLE_NAME + "'; "
                + "END;");

        db.execSQL("DROP TRIGGER IF EXISTS " + MessagesTable.TRIGGER_DELETE);
        db.execSQL("CREATE TRIGGER " + MessagesTable.TRIGGER_DELETE + " AFTER DELETE ON " + MessagesTable.TABLE_NAME + " "
                + "BEGIN "
                +   " DELETE FROM " + MediaTable.TABLE_NAME + " WHERE " + MediaTable.COLUMN_PARENT_ROW_ID + "=OLD." + MessagesTable._ID + " AND " + MediaTable.COLUMN_PARENT_TABLE + "='" + MessagesTable.TABLE_NAME + "'; "
                +   " DELETE FROM " + MentionsTable.TABLE_NAME + " WHERE " + MentionsTable.COLUMN_PARENT_ROW_ID + "=OLD." + MessagesTable._ID + " AND " + MentionsTable.COLUMN_PARENT_TABLE + "='" + MentionsTable.TABLE_NAME + "'; "
                +   " DELETE FROM " + FutureProofTable.TABLE_NAME + " WHERE " + FutureProofTable.COLUMN_PARENT_ROW_ID + "=OLD." + MessagesTable._ID + " AND " + FutureProofTable.COLUMN_PARENT_TABLE + "='" + MessagesTable.TABLE_NAME + "'; "
                +   " DELETE FROM " + UrlPreviewsTable.TABLE_NAME + " WHERE " + UrlPreviewsTable.COLUMN_PARENT_ROW_ID + "=OLD." + MessagesTable._ID + " AND " + UrlPreviewsTable.COLUMN_PARENT_TABLE + "='" + MessagesTable.TABLE_NAME + "'; "
                + "END;");

        db.execSQL("DROP TRIGGER IF EXISTS " + CommentsTable.TRIGGER_DELETE);
        db.execSQL("CREATE TRIGGER " + CommentsTable.TRIGGER_DELETE + " AFTER DELETE ON " + CommentsTable.TABLE_NAME + " "
                + "BEGIN "
                +   "DELETE FROM " + MentionsTable.TABLE_NAME + " WHERE " + MentionsTable.COLUMN_PARENT_ROW_ID + "=OLD." + CommentsTable._ID + " AND " + MentionsTable.COLUMN_PARENT_TABLE + "='" + MentionsTable.TABLE_NAME + "'; "
                +   "DELETE FROM " + FutureProofTable.TABLE_NAME + " WHERE " + FutureProofTable.COLUMN_PARENT_ROW_ID + "=OLD." + CommentsTable._ID + " AND " + FutureProofTable.COLUMN_PARENT_TABLE + "='" + CommentsTable.TABLE_NAME + "'; "
                +   "DELETE FROM " + UrlPreviewsTable.TABLE_NAME + " WHERE " + UrlPreviewsTable.COLUMN_PARENT_ROW_ID + "=OLD." + CommentsTable._ID + " AND " + UrlPreviewsTable.COLUMN_PARENT_TABLE + "='" + CommentsTable.TABLE_NAME + "'; "
                + "END;");
    }

    private void upgradeFromVersion51(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + CommentsTable.TABLE_NAME + " ADD COLUMN " + CommentsTable.COLUMN_PLAYED + " INTEGER DEFAULT 0");
        db.execSQL("UPDATE " + CommentsTable.TABLE_NAME + " SET " + CommentsTable.COLUMN_PLAYED + "=" + CommentsTable.COLUMN_SEEN + ";");
    }

    private void upgradeFromVersion52(@NonNull SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + RerequestsTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + RerequestsTable.TABLE_NAME + " ("
                + RerequestsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + RerequestsTable.COLUMN_CONTENT_ID + " TEXT NOT NULL,"
                + RerequestsTable.COLUMN_REQUESTOR_USER_ID + " TEXT NOT NULL,"
                + RerequestsTable.COLUMN_PARENT_TABLE + " TEXT NOT NULL,"
                + RerequestsTable.COLUMNT_REREQUEST_COUNT + " INTEGER"
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + RerequestsTable.INDEX_REREQUEST_KEY);
        db.execSQL("CREATE UNIQUE INDEX " + RerequestsTable.INDEX_REREQUEST_KEY + " ON " + RerequestsTable.TABLE_NAME + "("
                + RerequestsTable.COLUMN_CONTENT_ID + ", "
                + RerequestsTable.COLUMN_REQUESTOR_USER_ID + ", "
                + RerequestsTable.COLUMN_PARENT_TABLE
                + ");");

        db.execSQL("DROP TRIGGER IF EXISTS " + PostsTable.TRIGGER_DELETE);
        //noinspection SyntaxError
        db.execSQL("CREATE TRIGGER " + PostsTable.TRIGGER_DELETE + " AFTER DELETE ON " + PostsTable.TABLE_NAME + " "
                + "BEGIN "
                +   " DELETE FROM " + MediaTable.TABLE_NAME + " WHERE " + MediaTable.COLUMN_PARENT_ROW_ID + "=OLD." + PostsTable._ID + " AND " + MediaTable.COLUMN_PARENT_TABLE + "='" + PostsTable.TABLE_NAME + "'; "
                +   " DELETE FROM " + MentionsTable.TABLE_NAME + " WHERE " + MentionsTable.COLUMN_PARENT_ROW_ID + "=OLD." + PostsTable._ID + " AND " + MentionsTable.COLUMN_PARENT_TABLE + "='" + PostsTable.TABLE_NAME + "'; "
                +   " DELETE FROM " + CommentsTable.TABLE_NAME + " WHERE " + CommentsTable.COLUMN_POST_ID + "=OLD." + PostsTable.COLUMN_POST_ID + "; "
                +   " DELETE FROM " + SeenTable.TABLE_NAME + " WHERE " + SeenTable.COLUMN_POST_ID + "=OLD." + PostsTable.COLUMN_POST_ID + " AND ''=OLD." + PostsTable.COLUMN_SENDER_USER_ID + "; "
                +   " DELETE FROM " + AudienceTable.TABLE_NAME + " WHERE " + AudienceTable.COLUMN_POST_ID + "=OLD." + AudienceTable.COLUMN_POST_ID + "; "
                +   " DELETE FROM " + FutureProofTable.TABLE_NAME + " WHERE " + FutureProofTable.COLUMN_PARENT_ROW_ID + "=OLD." + PostsTable._ID + " AND " + FutureProofTable.COLUMN_PARENT_TABLE + "='" + PostsTable.TABLE_NAME + "'; "
                +   " DELETE FROM " + UrlPreviewsTable.TABLE_NAME + " WHERE " + UrlPreviewsTable.COLUMN_PARENT_ROW_ID + "=OLD." + PostsTable._ID + " AND " + UrlPreviewsTable.COLUMN_PARENT_TABLE + "='" + PostsTable.TABLE_NAME + "'; "
                +   " DELETE FROM " + RerequestsTable.TABLE_NAME + " WHERE " + RerequestsTable.COLUMN_CONTENT_ID + "=OLD." + PostsTable.COLUMN_POST_ID + " AND " + RerequestsTable.COLUMN_PARENT_TABLE + "='" + PostsTable.TABLE_NAME + "';"
                + "END;");

        db.execSQL("DROP TRIGGER IF EXISTS " + CommentsTable.TRIGGER_DELETE);
        db.execSQL("CREATE TRIGGER " + CommentsTable.TRIGGER_DELETE + " AFTER DELETE ON " + CommentsTable.TABLE_NAME + " "
                + "BEGIN "
                +   "DELETE FROM " + MentionsTable.TABLE_NAME + " WHERE " + MentionsTable.COLUMN_PARENT_ROW_ID + "=OLD." + CommentsTable._ID + " AND " + MentionsTable.COLUMN_PARENT_TABLE + "='" + MentionsTable.TABLE_NAME + "'; "
                +   "DELETE FROM " + FutureProofTable.TABLE_NAME + " WHERE " + FutureProofTable.COLUMN_PARENT_ROW_ID + "=OLD." + CommentsTable._ID + " AND " + FutureProofTable.COLUMN_PARENT_TABLE + "='" + CommentsTable.TABLE_NAME + "'; "
                +   "DELETE FROM " + UrlPreviewsTable.TABLE_NAME + " WHERE " + UrlPreviewsTable.COLUMN_PARENT_ROW_ID + "=OLD." + CommentsTable._ID + " AND " + UrlPreviewsTable.COLUMN_PARENT_TABLE + "='" + CommentsTable.TABLE_NAME + "'; "
                +   "DELETE FROM " + RerequestsTable.TABLE_NAME + " WHERE " + RerequestsTable.COLUMN_CONTENT_ID + "=OLD." + CommentsTable.COLUMN_COMMENT_ID + " AND " + RerequestsTable.COLUMN_PARENT_TABLE + "='" + CommentsTable.TABLE_NAME + "'; "
                + "END;");
    }

    private void upgradeFromVersion53(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + ChatsTable.TABLE_NAME + " ADD COLUMN " + ChatsTable.COLUMN_INVITE_LINK + " TEXT");
    }

    private void upgradeFromVersion54(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + PostsTable.TABLE_NAME + " ADD COLUMN " + PostsTable.COLUMN_SENDER_PLATFORM + " TEXT");
        db.execSQL("ALTER TABLE " + PostsTable.TABLE_NAME + " ADD COLUMN " + PostsTable.COLUMN_SENDER_VERSION + " TEXT");

        db.execSQL("ALTER TABLE " + CommentsTable.TABLE_NAME + " ADD COLUMN " + CommentsTable.COLUMN_SENDER_PLATFORM + " TEXT");
        db.execSQL("ALTER TABLE " + CommentsTable.TABLE_NAME + " ADD COLUMN " + CommentsTable.COLUMN_SENDER_VERSION + " TEXT");
    }

    private void upgradeFromVersion55(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + MediaTable.TABLE_NAME + " ADD COLUMN " + MediaTable.COLUMN_CHUNK_SET + " BLOB");
    }

    // Fixes issue where chat only contains key change notifications
    private void upgradeFromVersion56(@NonNull SQLiteDatabase db) {
        // WARNING: Do not reuse. The below deletes messages when there is only a single chat.
        @SuppressLint("Recycle")
        Cursor cursor = db.query(MessagesTable.TABLE_NAME,
                new String[]{ChatsTable.COLUMN_CHAT_ID},
                "(SELECT COUNT(*)) = (SELECT COUNT(*) WHERE " + MessagesTable.COLUMN_USAGE + "=" + Message.USAGE_KEYS_CHANGED + ")",
                null,
                ChatsTable.COLUMN_CHAT_ID,
                null,
                null);
        StringBuilder idList = null;
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String s = cursor.getString(0);
                if (idList == null) {
                    idList = new StringBuilder(s);
                } else {
                    idList.append(",").append(s);
                }
            }
        }
        db.execSQL("DELETE FROM " + ChatsTable.TABLE_NAME + " WHERE " + ChatsTable.COLUMN_CHAT_ID + " IN (" + idList + ")");
        db.execSQL("DELETE FROM " + MessagesTable.TABLE_NAME + " WHERE " + MessagesTable.COLUMN_CHAT_ID + " IN (" + idList + ")");
    }

    private void upgradeFromVersion57(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + PostsTable.TABLE_NAME + " ADD COLUMN " + PostsTable.COLUMN_PROTO_HASH + " BLOB");
        db.execSQL("ALTER TABLE " + CommentsTable.TABLE_NAME + " ADD COLUMN " + CommentsTable.COLUMN_PROTO_HASH + " BLOB");
    }

    private void upgradeFromVersion58(@NonNull SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + CallsTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + CallsTable.TABLE_NAME + " ("
                + CallsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + CallsTable.COLUMN_CALL_ID + " TEXT NOT NULL,"
                + CallsTable.COLUMN_CALL_DURATION + " INTEGER"
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + CallsTable.INDEX_CALL_KEY);
        db.execSQL("CREATE INDEX " + CallsTable.INDEX_CALL_KEY + " ON " + CallsTable.TABLE_NAME + "("
                + CallsTable.COLUMN_CALL_ID
                + ");");

        db.execSQL("DROP TRIGGER IF EXISTS " + MessagesTable.TRIGGER_DELETE);
        db.execSQL("CREATE TRIGGER " + MessagesTable.TRIGGER_DELETE + " AFTER DELETE ON " + MessagesTable.TABLE_NAME + " "
                + "BEGIN "
                +   " DELETE FROM " + MediaTable.TABLE_NAME + " WHERE " + MediaTable.COLUMN_PARENT_ROW_ID + "=OLD." + MessagesTable._ID + " AND " + MediaTable.COLUMN_PARENT_TABLE + "='" + MessagesTable.TABLE_NAME + "'; "
                +   " DELETE FROM " + MentionsTable.TABLE_NAME + " WHERE " + MentionsTable.COLUMN_PARENT_ROW_ID + "=OLD." + MessagesTable._ID + " AND " + MentionsTable.COLUMN_PARENT_TABLE + "='" + MentionsTable.TABLE_NAME + "'; "
                +   " DELETE FROM " + FutureProofTable.TABLE_NAME + " WHERE " + FutureProofTable.COLUMN_PARENT_ROW_ID + "=OLD." + MessagesTable._ID + " AND " + FutureProofTable.COLUMN_PARENT_TABLE + "='" + MessagesTable.TABLE_NAME + "'; "
                +   " DELETE FROM " + UrlPreviewsTable.TABLE_NAME + " WHERE " + UrlPreviewsTable.COLUMN_PARENT_ROW_ID + "=OLD." + MessagesTable._ID + " AND " + UrlPreviewsTable.COLUMN_PARENT_TABLE + "='" + MessagesTable.TABLE_NAME + "'; "
                +   " DELETE FROM " + CallsTable.TABLE_NAME + " WHERE " + CallsTable.COLUMN_CALL_ID + "=OLD." + MessagesTable.COLUMN_MESSAGE_ID + "; "
                + "END;");

    }

    private void upgradeFromVersion59(@NonNull SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + HistoryResendPayloadTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + HistoryResendPayloadTable.TABLE_NAME + " ("
                + HistoryResendPayloadTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + HistoryResendPayloadTable.COLUMN_HISTORY_RESEND_ID + " TEXT NOT NULL,"
                + HistoryResendPayloadTable.COLUMN_GROUP_ID + " TEXT NOT NULL,"
                + HistoryResendPayloadTable.COLUMN_PAYLOAD + " BLOB,"
                + HistoryResendPayloadTable.COLUMN_TIMESTAMP + " INTEGER"
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + HistoryResendPayloadTable.INDEX_HISTORY_RESEND_ID);
        db.execSQL("CREATE UNIQUE INDEX " + HistoryResendPayloadTable.INDEX_HISTORY_RESEND_ID + " ON " + HistoryResendPayloadTable.TABLE_NAME + "("
                + HistoryResendPayloadTable.COLUMN_HISTORY_RESEND_ID
                + ")");
    }

    private void upgradeFromVersion60(@NonNull SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + HistoryRerequestTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + HistoryRerequestTable.TABLE_NAME + "("
                + HistoryRerequestTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + HistoryRerequestTable.COLUMN_HISTORY_RESEND_ID + " TEXT NOT NULL,"
                + HistoryRerequestTable.COLUMN_SENDER_USER_ID + " TEXT NOT NULL,"
                + HistoryRerequestTable.COLUMN_REREQUEST_COUNT + " INTEGER,"
                + HistoryRerequestTable.COLUMN_TIMESTAMP + " INTEGER"
                + ")");
    }

    private void upgradeFromVersion61(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + UrlPreviewsTable.TABLE_NAME + " ADD COLUMN " + UrlPreviewsTable.COLUMN_DESCRIPTION + " TEXT");
    }

    private void upgradeFromVersion62(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + CommentsTable.TABLE_NAME + " ADD COLUMN " + CommentsTable.COLUMN_SHOULD_NOTIFY + " INTEGER");
        db.execSQL("ALTER TABLE " + PostsTable.TABLE_NAME + " ADD COLUMN " + PostsTable.COLUMN_SUBSCRIBED + " INTEGER");
    }

    private void upgradeFromVersion63(@NonNull SQLiteDatabase db) {
        long now = System.currentTimeMillis();
        db.execSQL("UPDATE " + CommentsTable.TABLE_NAME
                + " SET " + CommentsTable.COLUMN_TIMESTAMP + " = " + CommentsTable.COLUMN_TIMESTAMP + "/1000"
                + " WHERE " + CommentsTable.COLUMN_TIMESTAMP + ">" + (now * 100));
        db.execSQL("UPDATE " + PostsTable.TABLE_NAME
                + " SET " + PostsTable.COLUMN_TIMESTAMP + " = " + PostsTable.COLUMN_TIMESTAMP + "/1000"
                + " WHERE " + PostsTable.COLUMN_TIMESTAMP + ">" + (now * 100));
    }

    private void upgradeFromVersion64(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + PostsTable.TABLE_NAME + " ADD COLUMN " + PostsTable.COLUMN_LAST_UPDATE + " INTEGER");
    }

    private void upgradeFromVersion65(@NonNull SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE " + PostsTable.TABLE_NAME + " ADD COLUMN " + PostsTable.COLUMN_EXTERNAL_SHARE_ID + " TEXT");
        } catch (SQLiteException e) {
            Log.e("ContentDb/upgradeFromVersion65 column share id exists", e);
        }
        try {
            db.execSQL("ALTER TABLE " + PostsTable.TABLE_NAME + " ADD COLUMN " + PostsTable.COLUMN_EXTERNAL_SHARE_KEY + " TEXT");
        } catch (SQLiteException e) {
            Log.e("ContentDb/upgradeFromVersion65 column share key exists", e);
        }
    }

    private void upgradeFromVersion66(@NonNull SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + GroupsTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + GroupsTable.TABLE_NAME + " ("
                + GroupsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + GroupsTable.COLUMN_GROUP_ID + " TEXT NOT NULL UNIQUE,"
                + GroupsTable.COLUMN_TIMESTAMP + " INTEGER,"
                + GroupsTable.COLUMN_GROUP_NAME + " TEXT,"
                + GroupsTable.COLUMN_IS_ACTIVE + " INTEGER DEFAULT 1,"
                + GroupsTable.COLUMN_GROUP_DESCRIPTION + " TEXT,"
                + GroupsTable.COLUMN_GROUP_AVATAR_ID + " TEXT,"
                + GroupsTable.COLUMN_THEME + " INTEGER DEFAULT 0,"
                + GroupsTable.COLUMN_INVITE_LINK + " TEXT"
                + ");");

        // Delete chats trigger as it was only used for group chats
        db.execSQL("DROP TRIGGER IF EXISTS " + ChatsTable.TRIGGER_DELETE);

        db.execSQL("DROP TRIGGER IF EXISTS " + GroupsTable.TRIGGER_DELETE);
        db.execSQL("CREATE TRIGGER " + GroupsTable.TRIGGER_DELETE + " AFTER DELETE ON " + GroupsTable.TABLE_NAME + " "
                + "BEGIN "
                +   "DELETE FROM " + GroupMembersTable.TABLE_NAME + " WHERE " + GroupMembersTable.COLUMN_GROUP_ID + "=OLD." + GroupsTable.COLUMN_GROUP_ID + "; "
                + "END;");

        db.execSQL("INSERT INTO " + GroupsTable.TABLE_NAME + "("
                + GroupsTable.COLUMN_GROUP_ID + ","
                + GroupsTable.COLUMN_TIMESTAMP + ","
                + GroupsTable.COLUMN_GROUP_NAME + ","
                + GroupsTable.COLUMN_IS_ACTIVE + ","
                + GroupsTable.COLUMN_GROUP_DESCRIPTION + ","
                + GroupsTable.COLUMN_GROUP_AVATAR_ID + ","
                + GroupsTable.COLUMN_THEME + ","
                + GroupsTable.COLUMN_INVITE_LINK + ")"
                + " SELECT "
                + ChatsTable.COLUMN_CHAT_ID + ","
                + ChatsTable.COLUMN_TIMESTAMP + ","
                + ChatsTable.COLUMN_CHAT_NAME + ","
                + ChatsTable.COLUMN_IS_ACTIVE + ","
                + ChatsTable.COLUMN_GROUP_DESCRIPTION + ","
                + ChatsTable.COLUMN_GROUP_AVATAR_ID + ","
                + ChatsTable.COLUMN_THEME + ","
                + ChatsTable.COLUMN_INVITE_LINK
                + " FROM " + ChatsTable.TABLE_NAME
                + " WHERE " + ChatsTable.COLUMN_IS_GROUP + "=1");
        db.execSQL("DELETE FROM " + ChatsTable.TABLE_NAME + " WHERE " + ChatsTable.COLUMN_IS_GROUP + "=1");
    }

    private void upgradeFromVersion67(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + MediaTable.TABLE_NAME + " ADD COLUMN " + MediaTable.COLUMN_PERCENT_TRANSFERRED + " INTEGER DEFAULT 0");
        db.execSQL("ALTER TABLE " + MediaTable.TABLE_NAME + " ADD COLUMN " + MediaTable.COLUMN_DOWNLOAD_SIZE + " INTEGER");
    }

    private void upgradeFromVersion68(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + RepliesTable.TABLE_NAME + " ADD COLUMN " + RepliesTable.COLUMN_POST_TYPE + " INTEGER");
    }

    private void upgradeFromVersion69(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + PostsTable.TABLE_NAME + " ADD COLUMN " + PostsTable.COLUMN_PSA_TAG + " TEXT");
    }

    private void upgradeFromVersion70(@NonNull SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + MomentsTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + MomentsTable.TABLE_NAME + " ("
                + MomentsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MomentsTable.COLUMN_POST_ID + " TEXT NOT NULL,"
                + MomentsTable.COLUMN_UNLOCKED_USER_ID + " TEXT"
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + MomentsTable.INDEX_POST_KEY);
        db.execSQL("CREATE INDEX " + MomentsTable.INDEX_POST_KEY + " ON " + MomentsTable.TABLE_NAME + "("
                + MomentsTable.COLUMN_POST_ID
                + ");");

        db.execSQL("DROP TRIGGER IF EXISTS " + PostsTable.TRIGGER_DELETE);
        //noinspection SyntaxError
        db.execSQL("CREATE TRIGGER " + PostsTable.TRIGGER_DELETE + " AFTER DELETE ON " + PostsTable.TABLE_NAME + " "
                + "BEGIN "
                +   " DELETE FROM " + MediaTable.TABLE_NAME + " WHERE " + MediaTable.COLUMN_PARENT_ROW_ID + "=OLD." + PostsTable._ID + " AND " + MediaTable.COLUMN_PARENT_TABLE + "='" + PostsTable.TABLE_NAME + "'; "
                +   " DELETE FROM " + MentionsTable.TABLE_NAME + " WHERE " + MentionsTable.COLUMN_PARENT_ROW_ID + "=OLD." + PostsTable._ID + " AND " + MentionsTable.COLUMN_PARENT_TABLE + "='" + PostsTable.TABLE_NAME + "'; "
                +   " DELETE FROM " + CommentsTable.TABLE_NAME + " WHERE " + CommentsTable.COLUMN_POST_ID + "=OLD." + PostsTable.COLUMN_POST_ID + "; "
                +   " DELETE FROM " + SeenTable.TABLE_NAME + " WHERE " + SeenTable.COLUMN_POST_ID + "=OLD." + PostsTable.COLUMN_POST_ID + " AND ''=OLD." + PostsTable.COLUMN_SENDER_USER_ID + "; "
                +   " DELETE FROM " + AudienceTable.TABLE_NAME + " WHERE " + AudienceTable.COLUMN_POST_ID + "=OLD." + AudienceTable.COLUMN_POST_ID + "; "
                +   " DELETE FROM " + FutureProofTable.TABLE_NAME + " WHERE " + FutureProofTable.COLUMN_PARENT_ROW_ID + "=OLD." + PostsTable._ID + " AND " + FutureProofTable.COLUMN_PARENT_TABLE + "='" + PostsTable.TABLE_NAME + "'; "
                +   " DELETE FROM " + UrlPreviewsTable.TABLE_NAME + " WHERE " + UrlPreviewsTable.COLUMN_PARENT_ROW_ID + "=OLD." + PostsTable._ID + " AND " + UrlPreviewsTable.COLUMN_PARENT_TABLE + "='" + PostsTable.TABLE_NAME + "'; "
                +   " DELETE FROM " + RerequestsTable.TABLE_NAME + " WHERE " + RerequestsTable.COLUMN_CONTENT_ID + "=OLD." + PostsTable.COLUMN_POST_ID + " AND " + RerequestsTable.COLUMN_PARENT_TABLE + "='" + PostsTable.TABLE_NAME + "';"
                +   " DELETE FROM " + MomentsTable.TABLE_NAME + " WHERE " + MomentsTable.COLUMN_POST_ID + "=OLD." + PostsTable.COLUMN_POST_ID + ";"
                + "END;");
    }

    private void upgradeFromVersion71(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + MomentsTable.TABLE_NAME + " ADD COLUMN " + MomentsTable.COLUMN_SCREENSHOTTED + " INTEGER");

        db.execSQL("DROP TABLE IF EXISTS " + ScreenshotsTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + ScreenshotsTable.TABLE_NAME + " ("
                + ScreenshotsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + ScreenshotsTable.COLUMN_POST_ID + " TEXT NOT NULL,"
                + ScreenshotsTable.COLUMN_SEEN_BY_USER_ID + " TEXT NOT NULL,"
                + ScreenshotsTable.COLUMN_TIMESTAMP
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + ScreenshotsTable.INDEX_SEEN_KEY);
        db.execSQL("CREATE UNIQUE INDEX " + ScreenshotsTable.INDEX_SEEN_KEY + " ON " + ScreenshotsTable.TABLE_NAME + "("
                + ScreenshotsTable.COLUMN_POST_ID + ", "
                + ScreenshotsTable.COLUMN_SEEN_BY_USER_ID
                + ");");

        db.execSQL("DROP TRIGGER IF EXISTS " + PostsTable.TRIGGER_DELETE);
        //noinspection SyntaxError
        db.execSQL("CREATE TRIGGER " + PostsTable.TRIGGER_DELETE + " AFTER DELETE ON " + PostsTable.TABLE_NAME + " "
                + "BEGIN "
                +   " DELETE FROM " + MediaTable.TABLE_NAME + " WHERE " + MediaTable.COLUMN_PARENT_ROW_ID + "=OLD." + PostsTable._ID + " AND " + MediaTable.COLUMN_PARENT_TABLE + "='" + PostsTable.TABLE_NAME + "'; "
                +   " DELETE FROM " + MentionsTable.TABLE_NAME + " WHERE " + MentionsTable.COLUMN_PARENT_ROW_ID + "=OLD." + PostsTable._ID + " AND " + MentionsTable.COLUMN_PARENT_TABLE + "='" + PostsTable.TABLE_NAME + "'; "
                +   " DELETE FROM " + CommentsTable.TABLE_NAME + " WHERE " + CommentsTable.COLUMN_POST_ID + "=OLD." + PostsTable.COLUMN_POST_ID + "; "
                +   " DELETE FROM " + SeenTable.TABLE_NAME + " WHERE " + SeenTable.COLUMN_POST_ID + "=OLD." + PostsTable.COLUMN_POST_ID + " AND ''=OLD." + PostsTable.COLUMN_SENDER_USER_ID + "; "
                +   " DELETE FROM " + ScreenshotsTable.TABLE_NAME + " WHERE " + ScreenshotsTable.COLUMN_POST_ID + "=OLD." + PostsTable.COLUMN_POST_ID + " AND ''=OLD." + PostsTable.COLUMN_SENDER_USER_ID + "; "
                +   " DELETE FROM " + AudienceTable.TABLE_NAME + " WHERE " + AudienceTable.COLUMN_POST_ID + "=OLD." + AudienceTable.COLUMN_POST_ID + "; "
                +   " DELETE FROM " + FutureProofTable.TABLE_NAME + " WHERE " + FutureProofTable.COLUMN_PARENT_ROW_ID + "=OLD." + PostsTable._ID + " AND " + FutureProofTable.COLUMN_PARENT_TABLE + "='" + PostsTable.TABLE_NAME + "'; "
                +   " DELETE FROM " + UrlPreviewsTable.TABLE_NAME + " WHERE " + UrlPreviewsTable.COLUMN_PARENT_ROW_ID + "=OLD." + PostsTable._ID + " AND " + UrlPreviewsTable.COLUMN_PARENT_TABLE + "='" + PostsTable.TABLE_NAME + "'; "
                +   " DELETE FROM " + RerequestsTable.TABLE_NAME + " WHERE " + RerequestsTable.COLUMN_CONTENT_ID + "=OLD." + PostsTable.COLUMN_POST_ID + " AND " + RerequestsTable.COLUMN_PARENT_TABLE + "='" + PostsTable.TABLE_NAME + "';"
                +   " DELETE FROM " + MomentsTable.TABLE_NAME + " WHERE " + MomentsTable.COLUMN_POST_ID + "=OLD." + PostsTable.COLUMN_POST_ID + ";"
                + "END;");
    }

    private void upgradeFromVersion72(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + PostsTable.TABLE_NAME + " ADD COLUMN " + PostsTable.COLUMN_COMMENT_KEY + " BLOB");
    }

    private void upgradeFromVersion73(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + PostsTable.TABLE_NAME + " ADD COLUMN " + PostsTable.COLUMN_EXPIRATION_TIME + " INTEGER");
        db.execSQL("UPDATE " + PostsTable.TABLE_NAME + " SET " + PostsTable.COLUMN_EXPIRATION_TIME + "=" + PostsTable.COLUMN_TIMESTAMP + "+" + Constants.POSTS_EXPIRATION);

        db.execSQL("UPDATE " + PostsTable.TABLE_NAME + " SET " + PostsTable.COLUMN_EXPIRATION_TIME + "=" + PostsTable.COLUMN_TIMESTAMP + "+" + Constants.MOMENT_EXPIRATION +
                " WHERE " + PostsTable.COLUMN_TYPE + "=" + Post.TYPE_MOMENT
                + " OR " + PostsTable.COLUMN_TYPE + "=" + Post.TYPE_MOMENT_PSA
                + " OR " + PostsTable.COLUMN_TYPE + "=" + Post.TYPE_RETRACTED_MOMENT);
    }

    private void upgradeFromVersion74(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + GroupsTable.TABLE_NAME + " ADD COLUMN " + GroupsTable.COLUMN_EXPIRATION_TYPE + " INTEGER DEFAULT 0");
        db.execSQL("ALTER TABLE " + GroupsTable.TABLE_NAME + " ADD COLUMN " + GroupsTable.COLUMN_EXPIRATION_TIME + " INTEGER DEFAULT " + Constants.DEFAULT_GROUP_EXPIRATION_TIME);
    }

    private void upgradeFromVersion75(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + PostsTable.TABLE_NAME + " ADD COLUMN " + PostsTable.COLUMN_FROM_HISTORY + " INTEGER DEFAULT 0");
        db.execSQL("ALTER TABLE " + CommentsTable.TABLE_NAME + " ADD COLUMN " + CommentsTable.COLUMN_FROM_HISTORY + " INTEGER DEFAULT 0");
    }

    private void upgradeFromVersion76(@NonNull SQLiteDatabase db) {
        db.execSQL("UPDATE " + PostsTable.TABLE_NAME + " SET " + PostsTable.COLUMN_EXPIRATION_TIME + "=" + PostsTable.COLUMN_TIMESTAMP + "+" + Constants.POSTS_EXPIRATION +
                " WHERE " + PostsTable.COLUMN_EXPIRATION_TIME + "=0");
    }

    private void upgradeFromVersion77(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + GroupsTable.TABLE_NAME + " ADD COLUMN " + GroupsTable.COLUMN_ADDED_TIMESTAMP + " INTEGER DEFAULT 0");
    }

    private void upgradeFromVersion78(@NonNull SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + ReactionsTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + ReactionsTable.TABLE_NAME + " ("
                + ReactionsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + ReactionsTable.COLUMN_CONTENT_ID + " TEXT NOT NULL,"
                + ReactionsTable.COLUMN_SENDER_USER_ID + " TEXT NOT NULL,"
                + ReactionsTable.COLUMN_REACTION_TYPE + " TEXT NOT NULL,"
                + ReactionsTable.COLUMN_TIMESTAMP + " INTEGER"
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + ReactionsTable.INDEX_REACTION_KEY);
        db.execSQL("CREATE UNIQUE INDEX " + ReactionsTable.INDEX_REACTION_KEY + " ON " + ReactionsTable.TABLE_NAME + "("
                + ReactionsTable.COLUMN_SENDER_USER_ID + ", "
                + ReactionsTable.COLUMN_CONTENT_ID
                + ");");
    }

    private void upgradeFromVersion79(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + ReactionsTable.TABLE_NAME + " ADD COLUMN " + ReactionsTable.COLUMN_SENT + " INTEGER DEFAULT 0");
    }

    private void upgradeFromVersion80(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + PostsTable.TABLE_NAME + " ADD COLUMN " + PostsTable.COLUMN_SHOW_SHARE_FOOTER + " INTEGER DEFAULT 0");
    }

    private void upgradeFromVersion81(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + ReactionsTable.TABLE_NAME + " ADD COLUMN " + ReactionsTable.COLUMN_REACTION_ID + " TEXT");
    }

    private void upgradeFromVersion82(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + PostsTable.TABLE_NAME + " ADD COLUMN " + PostsTable.COLUMN_EXPIRATION_MISMATCH + " INTEGER DEFAULT 0");
    }

    private void upgradeFromVersion83(@NonNull SQLiteDatabase db) {
        db.execSQL("UPDATE " + PostsTable.TABLE_NAME + " SET " + PostsTable.COLUMN_EXPIRATION_TIME + "=" + Post.POST_EXPIRATION_NEVER +
                " WHERE " + PostsTable.COLUMN_EXPIRATION_TIME + "=" + Constants.NEVER_EXPIRE_BUG_WORKAROUND_TIMESTAMP);
    }

    private void upgradeFromVersion84(@NonNull SQLiteDatabase db) {
        db.execSQL("UPDATE " + PostsTable.TABLE_NAME + " SET " + PostsTable.COLUMN_EXPIRATION_TIME + "=" + PostsTable.COLUMN_EXPIRATION_TIME + "/ 1000" +
                " WHERE " + PostsTable.COLUMN_EXPIRATION_TIME + ">=" + Constants.NEVER_EXPIRE_BUG_WORKAROUND_TIMESTAMP);
    }

    private void upgradeFromVersion85(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + MomentsTable.TABLE_NAME + " ADD COLUMN " + MomentsTable.COLUMN_SELFIE_MEDIA_INDEX + " INTEGER DEFAULT -1");
    }

    private void upgradeFromVersion86(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + MomentsTable.TABLE_NAME + " ADD COLUMN " + MomentsTable.COLUMN_LOCATION + " TEXT");
    }

    private void upgradeFromVersion87(@NonNull SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + GroupMessageSeenReceiptsTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + GroupMessageSeenReceiptsTable.TABLE_NAME + " ("
                + GroupMessageSeenReceiptsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + GroupMessageSeenReceiptsTable.COLUMN_CONTENT_ID + " TEXT NOT NULL,"
                + GroupMessageSeenReceiptsTable.COLUMN_USER_ID + " TEXT NOT NULL,"
                + GroupMessageSeenReceiptsTable.COLUMN_STATE + " INTEGER,"
                + GroupMessageSeenReceiptsTable.COLUMN_TIMESTAMP + " INTEGER"
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + GroupMessageSeenReceiptsTable.INDEX_GROUP_MESSAGE_SEEN_RECEIPT_KEY);
        db.execSQL("CREATE INDEX " + GroupMessageSeenReceiptsTable.INDEX_GROUP_MESSAGE_SEEN_RECEIPT_KEY + " ON " + GroupMessageSeenReceiptsTable.TABLE_NAME + "("
                + GroupMessageSeenReceiptsTable.COLUMN_CONTENT_ID + ", " + GroupMessageSeenReceiptsTable.COLUMN_USER_ID
                + ");");

        db.execSQL("DROP TRIGGER IF EXISTS " + MessagesTable.TRIGGER_DELETE);
        db.execSQL("CREATE TRIGGER " + MessagesTable.TRIGGER_DELETE + " AFTER DELETE ON " + MessagesTable.TABLE_NAME + " "
                + "BEGIN "
                +   " DELETE FROM " + MediaTable.TABLE_NAME + " WHERE " + MediaTable.COLUMN_PARENT_ROW_ID + "=OLD." + MessagesTable._ID + " AND " + MediaTable.COLUMN_PARENT_TABLE + "='" + MessagesTable.TABLE_NAME + "'; "
                +   " DELETE FROM " + MentionsTable.TABLE_NAME + " WHERE " + MentionsTable.COLUMN_PARENT_ROW_ID + "=OLD." + MessagesTable._ID + " AND " + MentionsTable.COLUMN_PARENT_TABLE + "='" + MentionsTable.TABLE_NAME + "'; "
                +   " DELETE FROM " + FutureProofTable.TABLE_NAME + " WHERE " + FutureProofTable.COLUMN_PARENT_ROW_ID + "=OLD." + MessagesTable._ID + " AND " + FutureProofTable.COLUMN_PARENT_TABLE + "='" + MessagesTable.TABLE_NAME + "'; "
                +   " DELETE FROM " + UrlPreviewsTable.TABLE_NAME + " WHERE " + UrlPreviewsTable.COLUMN_PARENT_ROW_ID + "=OLD." + MessagesTable._ID + " AND " + UrlPreviewsTable.COLUMN_PARENT_TABLE + "='" + MessagesTable.TABLE_NAME + "'; "
                +   " DELETE FROM " + CallsTable.TABLE_NAME + " WHERE " + CallsTable.COLUMN_CALL_ID + "=OLD." + MessagesTable.COLUMN_MESSAGE_ID + "; "
                +   " DELETE FROM " + GroupMessageSeenReceiptsTable.TABLE_NAME + " WHERE " + GroupMessageSeenReceiptsTable.COLUMN_CONTENT_ID + "=OLD." + MessagesTable.COLUMN_MESSAGE_ID + "; "
                + "END;");
    }

    private void upgradeFromVersion88(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + ReactionsTable.TABLE_NAME + " ADD COLUMN " + ReactionsTable.COLUMN_SEEN + " INTEGER DEFAULT 0");
    }

    private void upgradeFromVersion89(@NonNull SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + KatchupMomentsTable.TABLE_NAME);
        db.execSQL("CREATE TABLE " + KatchupMomentsTable.TABLE_NAME + " ("
                + KatchupMomentsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KatchupMomentsTable.COLUMN_POST_ID + " TEXT NOT NULL,"
                + KatchupMomentsTable.COLUMN_LOCATION + " TEXT,"
                + KatchupMomentsTable.COLUMN_NOTIFICATION_TIMESTAMP + " INTEGER,"
                + KatchupMomentsTable.COLUMN_SELFIE_X + " REAL,"
                + KatchupMomentsTable.COLUMN_SELFIE_Y + " REAL"
                + ");");

        db.execSQL("DROP INDEX IF EXISTS " + KatchupMomentsTable.INDEX_POST_KEY);
        db.execSQL("CREATE INDEX " + KatchupMomentsTable.INDEX_POST_KEY + " ON " + KatchupMomentsTable.TABLE_NAME + "("
                + KatchupMomentsTable.COLUMN_POST_ID
                + ");");
    }

    private void upgradeFromVersion90(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + KatchupMomentsTable.TABLE_NAME + " ADD COLUMN " + KatchupMomentsTable.COLUMN_NOTIFICATION_ID + " INTEGER");
        db.execSQL("ALTER TABLE " + KatchupMomentsTable.TABLE_NAME + " ADD COLUMN " + KatchupMomentsTable.COLUMN_NUM_TAKES + " INTEGER");
        db.execSQL("ALTER TABLE " + KatchupMomentsTable.TABLE_NAME + " ADD COLUMN " + KatchupMomentsTable.COLUMN_NUM_SELFIE_TAKES + " INTEGER");
        db.execSQL("ALTER TABLE " + KatchupMomentsTable.TABLE_NAME + " ADD COLUMN " + KatchupMomentsTable.COLUMN_TIME_TAKEN + " INTEGER");
    }

    private void upgradeFromVersion91(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + KatchupMomentsTable.TABLE_NAME + " ADD COLUMN " + KatchupMomentsTable.COLUMN_CONTENT_TYPE + " INTEGER");
    }

    /**
     * Recreates a table with a new schema specified by columns.
     *
     * Be careful as triggers AND indices on the old table will be deleted, and WILL need to be recreated.
     */
    private void recreateTable(@NonNull SQLiteDatabase db, @NonNull String tableName, @NonNull String [] columns) {
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
        Log.i("ContentDb: deleting db");
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

    private class DowngradeAttemptedException extends SQLiteException {
        public DowngradeAttemptedException(String message) {
            super(message);
        }
    }
}

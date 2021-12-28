package com.halloapp.content.tables;

import android.provider.BaseColumns;

public final class CommentsTable implements BaseColumns {

    private CommentsTable() { }

    public static final String TABLE_NAME = "comments";

    public static final String INDEX_COMMENT_KEY = "comment_key";

    public static final String TRIGGER_DELETE = "on_comment_delete";

    public static final String COLUMN_POST_ID = "post_id";
    public static final String COLUMN_COMMENT_SENDER_USER_ID = "comment_sender_user_id";
    public static final String COLUMN_COMMENT_ID = "comment_id";
    public static final String COLUMN_PARENT_ID = "parent_id";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_TRANSFERRED = "transferred";
    public static final String COLUMN_SEEN = "seen";
    public static final String COLUMN_PLAYED = "played";
    public static final String COLUMN_TEXT = "text";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_REREQUEST_COUNT = "rerequest_count";
    public static final String COLUMN_PROTO_HASH = "proto_hash";

    // for stats
    public static final String COLUMN_FAILURE_REASON = "failure_reason";
    public static final String COLUMN_CLIENT_VERSION = "client_version"; // at time id first seen
    public static final String COLUMN_SENDER_PLATFORM = "sender_platform";
    public static final String COLUMN_SENDER_VERSION = "sender_version";
    public static final String COLUMN_RECEIVE_TIME = "receive_time";
    public static final String COLUMN_RESULT_UPDATE_TIME = "result_update_time";
}


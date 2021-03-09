package com.halloapp.content.tables;

import android.provider.BaseColumns;

public final class MessagesTable implements BaseColumns {

    private MessagesTable() { }

    public static final String TABLE_NAME = "messages";

    public static final String INDEX_MESSAGE_KEY = "message_key";

    public static final String TRIGGER_DELETE = "on_message_delete";

    public static final String COLUMN_CHAT_ID = "chat_id";
    public static final String COLUMN_SENDER_USER_ID = "sender_user_id";
    public static final String COLUMN_MESSAGE_ID = "message_id";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_USAGE = "usage";
    public static final String COLUMN_STATE = "state";
    public static final String COLUMN_TEXT = "text";
    public static final String COLUMN_REREQUEST_COUNT = "rerequest_count";

    // for stats
    public static final String COLUMN_FAILURE_REASON = "failure_reason";
    public static final String COLUMN_CLIENT_VERSION = "client_version"; // at time message id first seen
    public static final String COLUMN_SENDER_PLATFORM = "sender_platform";
    public static final String COLUMN_SENDER_VERSION = "sender_version";
    public static final String COLUMN_RECEIVE_TIME = "receive_time";
    public static final String COLUMN_RESULT_UPDATE_TIME = "result_update_time";
}

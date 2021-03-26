package com.halloapp.content.tables;

import android.provider.BaseColumns;

// TODO(jack): Remove silent messages once no longer needed
public final class SilentMessagesTable implements BaseColumns {

    private SilentMessagesTable() {}

    public static final String TABLE_NAME = "silent_messages";

    public static final String INDEX_SILENT_MESSAGE_KEY = "silent_message_key";

    public static final String COLUMN_SENDER_USER_ID = "sender_user_id";
    public static final String COLUMN_MESSAGE_ID = "message_id";
    public static final String COLUMN_REREQUEST_COUNT = "rerequest_count";

    // for stats
    public static final String COLUMN_FAILURE_REASON = "failure_reason";
    public static final String COLUMN_CLIENT_VERSION = "client_version"; // at time message id first seen
    public static final String COLUMN_SENDER_PLATFORM = "sender_platform";
    public static final String COLUMN_SENDER_VERSION = "sender_version";
    public static final String COLUMN_RECEIVE_TIME = "receive_time";
    public static final String COLUMN_RESULT_UPDATE_TIME = "result_update_time";
}

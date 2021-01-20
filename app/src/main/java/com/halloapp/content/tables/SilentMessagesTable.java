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
}

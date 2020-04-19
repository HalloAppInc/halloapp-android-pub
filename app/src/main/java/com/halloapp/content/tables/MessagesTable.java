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
    public static final String COLUMN_REPLY_TO_ROW_ID = "reply_to_row_id";
    public static final String  COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_STATE = "state";
    public static final String COLUMN_TEXT = "text";
}

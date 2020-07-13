package com.halloapp.content.tables;

import android.provider.BaseColumns;

public class ChatsTable implements BaseColumns {

    private ChatsTable() { }

    public static final String TABLE_NAME = "chats";

    public static final String COLUMN_CHAT_ID = "chat_id";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_NEW_MESSAGE_COUNT = "new_message_count";
    public static final String COLUMN_LAST_MESSAGE_ROW_ID = "last_message_row_id";
    public static final String COLUMN_FIRST_UNSEEN_MESSAGE_ROW_ID = "first_unseen_message_row_id";
    public static final String COLUMN_CHAT_NAME = "chat_name";
}

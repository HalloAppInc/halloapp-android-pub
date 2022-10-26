package com.halloapp.content.tables;

import android.provider.BaseColumns;

public class GroupMessageSeenReceiptsTable implements BaseColumns {

    private GroupMessageSeenReceiptsTable() {}

    public static final String TABLE_NAME = "group_message_seen_receipts_table";

    public static final String INDEX_GROUP_MESSAGE_SEEN_RECEIPT_KEY = "group_message_seen_receipt_key";

    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_CONTENT_ID = "item_id";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_STATE = "state";

}


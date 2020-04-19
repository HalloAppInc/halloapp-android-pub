package com.halloapp.content.tables;

import android.provider.BaseColumns;

public class OutgoingSeenReceiptsTable implements BaseColumns {

    private OutgoingSeenReceiptsTable() { }

    public static final String TABLE_NAME = "outgoing_seen_receipts";

    public static final String INDEX_OUTGOING_RECEIPT_KEY = "outgoing_seen_receipt_key";

    public static final String COLUMN_CHAT_ID = "chat_id";
    public static final String COLUMN_SENDER_USER_ID = "sender_user_id";
    public static final String COLUMN_CONTENT_ITEM_ID = "item_id";
}

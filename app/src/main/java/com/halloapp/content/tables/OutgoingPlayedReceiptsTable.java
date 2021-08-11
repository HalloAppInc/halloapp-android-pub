package com.halloapp.content.tables;

import android.provider.BaseColumns;

public class OutgoingPlayedReceiptsTable implements BaseColumns {

    private OutgoingPlayedReceiptsTable() {
    }

    public static final String TABLE_NAME = "outgoing_played_receipts";

    public static final String INDEX_OUTGOING_RECEIPT_KEY = "outgoing_played_receipt_key";

    public static final String COLUMN_CHAT_ID = "chat_id";
    public static final String COLUMN_SENDER_USER_ID = "sender_user_id";
    public static final String COLUMN_CONTENT_ITEM_ID = "item_id";
}

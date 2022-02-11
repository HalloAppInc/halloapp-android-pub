package com.halloapp.content.tables;

import android.provider.BaseColumns;

public class HistoryResendPayloadTable implements BaseColumns {

    private HistoryResendPayloadTable() {}

    public static final String TABLE_NAME = "history_resend_payload";

    public static final String INDEX_HISTORY_RESEND_ID = "index_history_resend_id";

    public static final String COLUMN_GROUP_ID = "group_id";
    public static final String COLUMN_HISTORY_RESEND_ID = "history_resend_id";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_PAYLOAD = "payload";
}

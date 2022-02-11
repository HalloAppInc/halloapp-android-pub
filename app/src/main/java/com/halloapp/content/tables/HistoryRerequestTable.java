package com.halloapp.content.tables;

import android.provider.BaseColumns;

public final class HistoryRerequestTable implements BaseColumns {

    private HistoryRerequestTable() {}

    public static final String TABLE_NAME = "history_rerequests";

    public static final String COLUMN_HISTORY_RESEND_ID = "history_resend_id";
    public static final String COLUMN_SENDER_USER_ID = "sender_user_id";
    public static final String COLUMN_REREQUEST_COUNT = "rerequest_count";
    public static final String COLUMN_TIMESTAMP = "timestamp";

}


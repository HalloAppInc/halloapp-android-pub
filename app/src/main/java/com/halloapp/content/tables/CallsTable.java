package com.halloapp.content.tables;

import android.provider.BaseColumns;

public class CallsTable implements BaseColumns {

    public static final String TABLE_NAME = "call_log";

    public static final String INDEX_CALL_KEY = "call_log_key";

    public static final String COLUMN_CALL_ID = "call_id";
    public static final String COLUMN_CALL_DURATION = "call_duration";
}

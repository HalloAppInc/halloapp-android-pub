package com.halloapp.content.tables;

import android.provider.BaseColumns;

public final class RerequestsTable implements BaseColumns {

    private RerequestsTable() { }

    public static final String TABLE_NAME = "rerequests";

    public static final String INDEX_REREQUEST_KEY = "rerequest_key";

    public static final String COLUMN_CONTENT_ID = "content_id";
    public static final String COLUMN_REQUESTOR_USER_ID = "requestor_user_id";
    public static final String COLUMN_PARENT_TABLE = "parent_table";
    public static final String COLUMNT_REREQUEST_COUNT = "rerequest_count";
}


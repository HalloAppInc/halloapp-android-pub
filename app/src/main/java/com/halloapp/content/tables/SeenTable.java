package com.halloapp.content.tables;

import android.provider.BaseColumns;

public final class SeenTable implements BaseColumns {

    private SeenTable() { }

    public static final String TABLE_NAME = "seen";

    public static final String INDEX_SEEN_KEY = "seen_key";

    public static final String COLUMN_SEEN_BY_USER_ID = "user_id";
    public static final String COLUMN_POST_ID = "post_id";
    public static final String COLUMN_TIMESTAMP = "timestamp";
}

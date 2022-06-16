package com.halloapp.content.tables;

import android.provider.BaseColumns;

public class ScreenshotsTable implements BaseColumns {

    private ScreenshotsTable() { }

    public static final String TABLE_NAME = "screenshots";

    public static final String INDEX_SEEN_KEY = "screenshots_key";

    public static final String COLUMN_SEEN_BY_USER_ID = "user_id";
    public static final String COLUMN_POST_ID = "post_id";
    public static final String COLUMN_TIMESTAMP = "timestamp";

}

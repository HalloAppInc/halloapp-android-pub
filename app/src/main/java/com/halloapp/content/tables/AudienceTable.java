package com.halloapp.content.tables;

import android.provider.BaseColumns;

public class AudienceTable implements BaseColumns {

    private AudienceTable() { }

    public static final String TABLE_NAME = "audience_table";

    public static final String INDEX_AUDIENCE_KEY = "audience_key";

    public static final String COLUMN_POST_ID = "post_id";
    public static final String COLUMN_USER_ID = "user_id";
}

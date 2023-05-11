package com.halloapp.content.tables;

import android.provider.BaseColumns;

public class SeenPostsTable implements BaseColumns {

    private SeenPostsTable() { }

    public static final String TABLE_NAME = "seen_posts";

    public static final String INDEX_SEEN_KEY = "seen_posts_key";

    public static final String COLUMN_SENDER_USER_ID = "user_id";
    public static final String COLUMN_POST_ID = "post_id";
    public static final String COLUMN_TIMESTAMP = "timestamp";

}

package com.halloapp.content.tables;

import android.provider.BaseColumns;

public final class PostsTable implements BaseColumns {

    private PostsTable() { }

    public static final String TABLE_NAME = "posts";

    public static final String INDEX_POST_KEY = "post_key";
    public static final String INDEX_TIMESTAMP = "timestamp";

    public static final String TRIGGER_DELETE = "on_post_delete";

    public static final String COLUMN_SENDER_USER_ID = "sender_user_id";
    public static final String COLUMN_POST_ID = "post_id";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_TRANSFERRED = "transferred";
    public static final String COLUMN_SEEN = "seen";
    public static final String COLUMN_TEXT = "text";
    public static final String COLUMN_AUDIENCE_TYPE = "audience_type";
}


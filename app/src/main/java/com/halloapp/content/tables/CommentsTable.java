package com.halloapp.content.tables;

import android.provider.BaseColumns;

public final class CommentsTable implements BaseColumns {

    private CommentsTable() { }

    public static final String TABLE_NAME = "comments";

    public static final String INDEX_COMMENT_KEY = "comment_key";

    public static final String TRIGGER_DELETE = "on_comment_delete";

    public static final String COLUMN_POST_SENDER_USER_ID = "post_sender_user_id";
    public static final String COLUMN_POST_ID = "post_id";
    public static final String COLUMN_COMMENT_SENDER_USER_ID = "comment_sender_user_id";
    public static final String COLUMN_COMMENT_ID = "comment_id";
    public static final String COLUMN_PARENT_ID = "parent_id";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_TRANSFERRED = "transferred";
    public static final String COLUMN_SEEN = "seen";
    public static final String COLUMN_TEXT = "text";
}


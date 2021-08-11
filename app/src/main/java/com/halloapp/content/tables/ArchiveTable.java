package com.halloapp.content.tables;

import android.provider.BaseColumns;

public final class ArchiveTable implements BaseColumns {

    private ArchiveTable() { }

    public static final String TABLE_NAME = "archived_posts";

    public static final String INDEX_POST_KEY = "archive_post_key";
    public static final String INDEX_TIMESTAMP = "archive_timestamp";

    public static final String COLUMN_POST_ID = "post_id";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_TEXT = "text";
    public static final String COLUMN_GROUP_ID = "group_id";
    public static final String COLUMN_ARCHIVE_TIMESTAMP = "archive_timestamp";
}

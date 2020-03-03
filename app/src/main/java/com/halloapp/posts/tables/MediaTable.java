package com.halloapp.posts.tables;

import android.provider.BaseColumns;

public final class MediaTable implements BaseColumns {

    private MediaTable() { }

    public static final String TABLE_NAME = "media";

    public static final String COLUMN_MEDIA_ID = "id";
    public static final String COLUMN_POST_ROW_ID = "post_row_id";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_TRANSFERRED = "transferred";
    public static final String COLUMN_URL = "url";
    public static final String COLUMN_FILE = "file";
    public static final String COLUMN_ENC_KEY = "enckey";
    public static final String COLUMN_SHA256_HASH = "sha256hash";
    public static final String COLUMN_WIDTH = "width";
    public static final String COLUMN_HEIGHT = "height";
}


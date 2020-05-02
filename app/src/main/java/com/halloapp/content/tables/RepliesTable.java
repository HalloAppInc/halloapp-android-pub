package com.halloapp.content.tables;

import android.provider.BaseColumns;

public class RepliesTable implements BaseColumns {

    private RepliesTable() { }

    public static final String TABLE_NAME = "replies";

    public static final String INDEX_MESSAGE_KEY = "reply_message_key";

    public static final String COLUMN_MESSAGE_ROW_ID = "message_row_id";
    public static final String COLUMN_POST_ID = "post_id";
    public static final String COLUMN_POST_MEDIA_INDEX = "post_media_index";
    public static final String COLUMN_TEXT = "text";
    public static final String COLUMN_MEDIA_TYPE = "media_type";
    public static final String COLUMN_MEDIA_PREVIEW_FILE = "media_preview_file";
}

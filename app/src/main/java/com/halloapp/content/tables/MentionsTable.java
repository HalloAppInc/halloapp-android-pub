package com.halloapp.content.tables;

import android.provider.BaseColumns;

public final class MentionsTable implements BaseColumns {

    private MentionsTable() {
    }

    public static final String TABLE_NAME = "mentions";

    public static final String INDEX_MENTION_KEY = "mention_key";

    public static final String COLUMN_PARENT_TABLE = "parent_table";
    public static final String COLUMN_PARENT_ROW_ID = "parent_row_id";
    public static final String COLUMN_MENTION_INDEX = "mention_index";
    public static final String COLUMN_MENTION_USER_ID = "mention_user_id";
    public static final String COLUMN_MENTION_NAME = "mention_name";

}

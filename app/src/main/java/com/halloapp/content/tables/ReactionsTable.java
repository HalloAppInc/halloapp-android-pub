package com.halloapp.content.tables;

import android.provider.BaseColumns;

public class ReactionsTable implements BaseColumns {

    private ReactionsTable() { }

    public static final String TABLE_NAME = "reactions";

    public static final String INDEX_REACTION_KEY = "reaction_key";

    public static final String COLUMN_REACTION_ID = "reaction_id";
    public static final String COLUMN_CONTENT_ID = "content_id";
    public static final String COLUMN_SENDER_USER_ID = "user_id";
    public static final String COLUMN_REACTION_TYPE = "reaction_type";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_SENT = "sent";
    public static final String COLUMN_SEEN = "seen";
}

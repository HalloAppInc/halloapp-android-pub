package com.halloapp.content.tables;

import android.provider.BaseColumns;

public class MomentsTable implements BaseColumns {

    public static final String TABLE_NAME = "moments";

    public static final String INDEX_POST_KEY = "moment_post_key";

    public static final String COLUMN_POST_ID = "post_id";
    public static final String COLUMN_UNLOCKED_USER_ID = "unlocked_user_id";
    public static final String COLUMN_SCREENSHOTTED = "screenshotted";

}

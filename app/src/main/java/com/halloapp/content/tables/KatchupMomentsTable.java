package com.halloapp.content.tables;

import android.provider.BaseColumns;

public class KatchupMomentsTable implements BaseColumns {

    public static final String TABLE_NAME = "katchup_moments";

    public static final String INDEX_POST_KEY = "katchup_moment_post_key";

    public static final String COLUMN_POST_ID = "post_id";
    public static final String COLUMN_LOCATION = "location";
    public static final String COLUMN_NOTIFICATION_TIMESTAMP = "notification_timestamp";
    public static final String COLUMN_SELFIE_X = "selfie_x";
    public static final String COLUMN_SELFIE_Y = "selfie_y";
}

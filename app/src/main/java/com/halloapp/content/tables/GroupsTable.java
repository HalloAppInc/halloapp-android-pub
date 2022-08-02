package com.halloapp.content.tables;

import android.provider.BaseColumns;

public class GroupsTable implements BaseColumns {

    private GroupsTable() { }

    public static final String TABLE_NAME = "groups";

    public static final String TRIGGER_DELETE = "on_group_delete";

    public static final String COLUMN_GROUP_ID = "group_id";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_IS_ACTIVE = "is_active";
    public static final String COLUMN_GROUP_NAME = "group_name";
    public static final String COLUMN_GROUP_DESCRIPTION = "group_description";
    public static final String COLUMN_GROUP_AVATAR_ID = "group_avatar_id";
    public static final String COLUMN_THEME = "theme";
    public static final String COLUMN_EXPIRATION_TYPE = "expiration_type";
    public static final String COLUMN_EXPIRATION_TIME = "expiration_time";
    public static final String COLUMN_INVITE_LINK = "invite_link";
    public static final String COLUMN_ADDED_TIMESTAMP = "added_timestamp";

}

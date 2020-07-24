package com.halloapp.content.tables;

import android.provider.BaseColumns;

public class GroupMembersTable implements BaseColumns {

    private GroupMembersTable() { }

    public static final String TABLE_NAME = "group_members";

    public static final String COLUMN_GROUP_ID = "group_id";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_IS_ADMIN = "is_admin";
}

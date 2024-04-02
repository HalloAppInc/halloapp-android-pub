package com.halloapp.content.tables;

import android.provider.BaseColumns;

public class ChatsTable implements BaseColumns {

    private ChatsTable() { }

    public static final String TABLE_NAME = "chats";

    public static final String TRIGGER_DELETE = "on_chat_delete"; // TODO remove this trigger fully

    public static final String COLUMN_CHAT_ID = "chat_id";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_NEW_MESSAGE_COUNT = "new_message_count";
    public static final String COLUMN_LAST_MESSAGE_ROW_ID = "last_message_row_id";
    public static final String COLUMN_FIRST_UNSEEN_MESSAGE_ROW_ID = "first_unseen_message_row_id";

    // specific to group chats
    public static final String COLUMN_IS_GROUP = "is_group";
    public static final String COLUMN_IS_ACTIVE = "is_active";
    public static final String COLUMN_CHAT_NAME = "chat_name"; // TODO: rename to group_name?
    public static final String COLUMN_GROUP_DESCRIPTION = "group_description";
    public static final String COLUMN_GROUP_AVATAR_ID = "group_avatar_id";
    public static final String COLUMN_THEME = "theme";
    public static final String COLUMN_INVITE_LINK = "invite_link";
}

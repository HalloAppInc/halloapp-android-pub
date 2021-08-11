package com.halloapp.content.tables;

import android.provider.BaseColumns;

public class DeletedGroupNameTable implements BaseColumns {

    private DeletedGroupNameTable() { }

    public static final String TABLE_NAME = "chats_names";

    public static final String COLUMN_CHAT_ID = "chat_id";
    public static final String COLUMN_CHAT_NAME = "chat_name";
}

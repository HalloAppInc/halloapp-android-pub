package com.halloapp.content;

import com.halloapp.id.ChatId;

public class Chat {
    public static final int MARKED_UNSEEN = -1;
    public final long rowId;
    public final ChatId chatId;
    public final long timestamp;
    public final int newMessageCount;
    public final long lastMessageRowId;
    public final long firstUnseenMessageRowId;

    public final boolean isActive;
    public final boolean isGroup;
    public final String groupDescription;
    public final String groupAvatarId;
    public int theme;

    public String name;

    public Chat(
            long rowId,
            ChatId chatId,
            long timestamp,
            int newMessageCount,
            long lastMessageRowId,
            long firstUnseenMessageRowId,
            String name,
            boolean isGroup,
            String groupDescription,
            String groupAvatarId,
            boolean isActive,
            int theme
    ) {
        this.rowId = rowId;
        this.chatId = chatId;
        this.timestamp = timestamp;
        this.newMessageCount = newMessageCount;
        this.lastMessageRowId = lastMessageRowId;
        this.firstUnseenMessageRowId = firstUnseenMessageRowId;
        this.name = name;
        this.isGroup = isGroup;
        this.groupDescription = groupDescription;
        this.groupAvatarId = groupAvatarId;
        this.isActive = isActive;
        this.theme = theme;
    }
}

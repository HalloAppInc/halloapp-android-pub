package com.halloapp.content;

public class Chat {

    public final long rowId;
    public final String chatId;
    public final long timestamp;
    public final int newMessageCount;
    public final long lastMessageRowId;
    public final long firstUnseenMessageRowId;

    public final boolean isGroup;
    public final String groupDescription;
    public final String groupAvatarId;

    public String name;

    public Chat(
            long rowId,
            String chatId,
            long timestamp,
            int newMessageCount,
            long lastMessageRowId,
            long firstUnseenMessageRowId,
            String name,
            boolean isGroup,
            String groupDescription,
            String groupAvatarId
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
    }
}

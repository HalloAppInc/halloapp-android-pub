package com.halloapp.content;

public class Chat {

    public final long rowId;
    public final String chatId;
    public final long timestamp;
    public final int newMessageCount;
    public final long lastMessageRowId;
    public final long firstUnseenMessageRowId;

    public String name;

    public Chat(
            long rowId,
            String chatId,
            long timestamp,
            int newMessageCount,
            long lastMessageRowId,
            long firstUnseenMessageRowId,
            String name) {
        this.rowId = rowId;
        this.chatId = chatId;
        this.timestamp = timestamp;
        this.newMessageCount = newMessageCount;
        this.lastMessageRowId = lastMessageRowId;
        this.firstUnseenMessageRowId = firstUnseenMessageRowId;
        this.name = name;
    }
}

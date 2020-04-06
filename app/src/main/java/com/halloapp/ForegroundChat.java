package com.halloapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ForegroundChat {

    private static ForegroundChat instance;

    public static ForegroundChat getInstance() {
        if (instance == null) {
            synchronized (ForegroundChat.class) {
                if (instance == null) {
                    instance = new ForegroundChat();
                }
            }
        }
        return instance;
    }

    private String foregroundChatId;

    private ForegroundChat() {
    }

    synchronized public void setForegroundChatId(@Nullable String chatId) {
        foregroundChatId = chatId;
    }

    synchronized public boolean isForegroundChatId(@NonNull String chatId) {
        return chatId.equals(foregroundChatId);
    }
}

package com.halloapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.id.ChatId;

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

    private ChatId foregroundChatId;

    private ForegroundChat() {
    }

    synchronized public void setForegroundChatId(@Nullable ChatId chatId) {
        foregroundChatId = chatId;
    }

    synchronized public boolean isForegroundChatId(@NonNull ChatId chatId) {
        return chatId.equals(foregroundChatId);
    }
}

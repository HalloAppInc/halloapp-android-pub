package com.halloapp.content;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.id.ChatId;
import com.halloapp.id.UserId;

public class PlayedReceipt {

    public final ChatId chatId;
    public final UserId senderUserId;
    public final String itemId;

    public PlayedReceipt(@Nullable ChatId chatId, @NonNull UserId senderUserId, @NonNull String itemId) {
        this.chatId = chatId;
        this.senderUserId = senderUserId;
        this.itemId = itemId;
    }
}
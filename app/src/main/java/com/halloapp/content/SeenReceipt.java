package com.halloapp.content;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.id.UserId;

public class SeenReceipt {

    public final String chatId;
    public final UserId senderUserId;
    public final String itemId;

    public SeenReceipt(@Nullable String chatId, @NonNull UserId senderUserId, @NonNull String itemId) {
        this.chatId = chatId;
        this.senderUserId = senderUserId;
        this.itemId = itemId;
    }
}

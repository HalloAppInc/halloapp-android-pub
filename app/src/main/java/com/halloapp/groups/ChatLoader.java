package com.halloapp.groups;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.collection.LruCache;

import com.halloapp.content.Chat;
import com.halloapp.content.ContentDb;
import com.halloapp.id.ChatId;
import com.halloapp.util.ViewDataLoader;

import java.util.concurrent.Callable;

public class ChatLoader extends ViewDataLoader<View, Chat, ChatId> {

    private final LruCache<ChatId, Chat> cache = new LruCache<>(512);
    private final ContentDb contentDb;

    public ChatLoader() {
        contentDb = ContentDb.getInstance();
    }

    public void load(@NonNull View view, @NonNull Displayer<View, Chat> displayer, @NonNull ChatId key) {
        @NonNull Callable<Chat> loader = () -> {
            Chat chat = contentDb.getChat(key);
            if (chat != null) {
                return chat;
            } else {
                String name = contentDb.getDeletedChatName(key);
                return new Chat(-1,null,-1, -1, -1, -1, name, true, null, null, true, -1);
            }
        };
        load(view, loader, displayer, key, cache);
    }
}


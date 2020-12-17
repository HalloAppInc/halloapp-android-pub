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
        @NonNull Callable<Chat> loader = () -> contentDb.getChat(key);
        load(view, loader, displayer, key, cache);
    }
}


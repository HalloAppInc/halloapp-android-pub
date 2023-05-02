package com.halloapp.ui.groups;

import android.view.View;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.collection.LruCache;

import com.halloapp.content.ContentDb;
import com.halloapp.content.Post;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.util.ViewDataLoader;

import java.util.concurrent.Callable;

public class GroupPostLoader extends ViewDataLoader<View, Post, ChatId> {

    private final ContentDb contentDb;

    private final LruCache<ChatId, Post> cache;

    public GroupPostLoader() {
        contentDb = ContentDb.getInstance();

        cache = new LruCache<>(128);
    }

    @MainThread
    public void load(@NonNull View view, @NonNull ChatId chatId, @NonNull Displayer<View, Post> displayer) {
        final Callable<Post> loader = () -> {
            if (chatId instanceof GroupId) {
                return contentDb.getLastGroupPost((GroupId) chatId);
            }
            return null;
        };
        load(view, loader, displayer, chatId, cache);
    }

    public void removeFromCache(@NonNull ChatId chatId) {
        cache.remove(chatId);
    }
}

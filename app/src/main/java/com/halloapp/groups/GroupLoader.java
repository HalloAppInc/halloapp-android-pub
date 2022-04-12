package com.halloapp.groups;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.collection.LruCache;

import com.halloapp.content.Chat;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Group;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.util.ViewDataLoader;

import java.util.concurrent.Callable;

public class GroupLoader extends ViewDataLoader<View, Group, GroupId> {

    private final LruCache<GroupId, Group> cache = new LruCache<>(512);
    private final ContentDb contentDb;

    public GroupLoader() {
        contentDb = ContentDb.getInstance();
    }

    public void load(@NonNull View view, @NonNull Displayer<View, Group> displayer, @NonNull GroupId key) {
        @NonNull Callable<Group> loader = () -> {
            Group chat = contentDb.getGroup(key);
            if (chat != null) {
                return chat;
            } else {
                String name = contentDb.getDeletedChatName(key);
                return new Group(-1,null,-1, name, null, null, true, -1);
            }
        };
        load(view, loader, displayer, key, cache);
    }
}


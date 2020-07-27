package com.halloapp.content;

import android.content.Context;
import android.view.View;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.collection.LruCache;

import com.halloapp.id.UserId;
import com.halloapp.util.ViewDataLoader;

import java.util.Map;
import java.util.concurrent.Callable;

public class MessageLoader extends ViewDataLoader<View, Message, Long> {

    private final ContentDb contentDb;
    private final LruCache<Long, Message> cache;

    public MessageLoader(@NonNull Context context) {
        contentDb = ContentDb.getInstance(context);
        cache = new LruCache<>(1024);
    }

    @MainThread
    public void load(@NonNull View view, long messageRowId, @NonNull ViewDataLoader.Displayer<View, Message> displayer) {
        final Callable<Message> loader = () -> contentDb.getMessage(messageRowId);
        load(view, loader, displayer, messageRowId, cache);
    }

    public void removeFromCache(@NonNull String chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
        final Map<Long, Message> snapshot = cache.snapshot();
        for (Map.Entry<Long, Message> entry : snapshot.entrySet()) {
            final Message message = entry.getValue();
            if (chatId.equals(message.chatId) && senderUserId.equals(message.senderUserId) && messageId.equals(message.id)) {
                cache.remove(message.rowId);
                break;
            }
        }
    }
}

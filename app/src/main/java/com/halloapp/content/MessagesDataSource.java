package com.halloapp.content;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;

import com.halloapp.util.Log;

import java.util.List;

public class MessagesDataSource extends ItemKeyedDataSource<Long, Message> {

    private final ContentDb contentDb;
    private final String chatId;

    private Long keyRowId;

    public static class Factory extends DataSource.Factory<Long, Message> {

        private final ContentDb contentDb;
        private final String chatId;

        private MessagesDataSource latestSource;

        public Factory(@NonNull ContentDb contentDb, @NonNull String chatId) {
            this.contentDb = contentDb;
            this.chatId = chatId;
            latestSource = new MessagesDataSource(contentDb, chatId);
        }

        @Override
        public @NonNull DataSource<Long, Message> create() {
            if (latestSource.isInvalid()) {
                latestSource = new MessagesDataSource(contentDb, chatId);
            }
            return latestSource;
        }

        public void invalidateLatestDataSource() {
            latestSource.invalidate();
        }
    }

    private MessagesDataSource(@NonNull ContentDb contentDb, @NonNull String chatId) {
        this.contentDb = contentDb;
        this.chatId = chatId;
    }

    @Override
    public @NonNull Long getKey(@NonNull Message item) {
        if (keyRowId  != null) {
            return keyRowId;
        }
        return item.rowId;
    }

    public void reloadAt(long rowId) {
        // next call to getKey on this data source will be used by framework to find load point of next data source after current one is invalidated;
        // this ensures that next call to getKey returns rowId regardless of what actual message item is
        keyRowId = rowId;
        invalidate();
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<Long> params, @NonNull LoadInitialCallback<Message> callback) {
        final List<Message> messages;
        if (params.requestedInitialKey == null || params.requestedInitialKey == Long.MAX_VALUE) {
            messages = contentDb.getMessages(chatId,null, params.requestedLoadSize, true);
        } else {
            // load around params.requestedInitialKey, otherwise the view that represents this data may jump
            messages = contentDb.getMessages(chatId, params.requestedInitialKey, params.requestedLoadSize / 2, false);
            messages.addAll(contentDb.getMessages(chatId, params.requestedInitialKey + 1, params.requestedLoadSize / 2, true));

        }
        Log.d("MessagesDataSource.loadInitial: requestedInitialKey=" + params.requestedInitialKey + " requestedLoadSize:" + params.requestedLoadSize + " got " + messages.size() +
                (messages.isEmpty() ? "" : " messages from " + messages.get(0).timestamp + " to " + messages.get(messages.size()-1).timestamp));
        callback.onResult(messages);
    }

    @Override
    public void loadAfter(@NonNull LoadParams<Long> params, @NonNull LoadCallback<Message> callback) {
        Log.d("MessagesDataSource.loadAfter: key=" + params.key + " requestedLoadSize:" + params.requestedLoadSize);
        callback.onResult(contentDb.getMessages(chatId, params.key, params.requestedLoadSize, true));
    }

    @Override
    public void loadBefore(@NonNull LoadParams<Long> params, @NonNull LoadCallback<Message> callback) {
        Log.d("MessagesDataSource.loadBefore: key=" + params.key + " requestedLoadSize:" + params.requestedLoadSize);
        callback.onResult(contentDb.getMessages(chatId, params.key, params.requestedLoadSize, false));
    }
}

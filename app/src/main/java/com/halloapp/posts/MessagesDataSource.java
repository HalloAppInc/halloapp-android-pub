package com.halloapp.posts;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;

import com.halloapp.util.Log;

import java.util.List;

public class MessagesDataSource extends ItemKeyedDataSource<Long, ChatMessage> {

    private final PostsDb postsDb;
    private final String chatId;

    private Long keyTimestamp;

    public static class Factory extends DataSource.Factory<Long, ChatMessage> {

        private final PostsDb postsDb;
        private final String chatId;

        private final MutableLiveData<MessagesDataSource> sourceLiveData = new MutableLiveData<>();

        public Factory(@NonNull PostsDb postsDb, @NonNull String chatId) {
            this.postsDb = postsDb;
            this.chatId = chatId;
        }

        @Override
        public @NonNull DataSource<Long, ChatMessage> create() {
            final MessagesDataSource latestSource = new MessagesDataSource(postsDb, chatId);
            sourceLiveData.postValue(latestSource);
            return latestSource;
        }
    }

    private MessagesDataSource(@NonNull PostsDb postsDb, @NonNull String chatId) {
        this.postsDb = postsDb;
        this.chatId = chatId;
    }

    @Override
    public @NonNull Long getKey(@NonNull ChatMessage item) {
        if (keyTimestamp  != null) {
            return keyTimestamp;
        }
        return item.timestamp;
    }

    public void reloadAt(long timestamp) {
        // next call to getKey on this data source will be used by framework to find load point of next data source after current one is invalidated;
        // this ensures that next call to getKey returns timestamp regardless of what actual post item is
        keyTimestamp = timestamp;
        invalidate();
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<Long> params, @NonNull LoadInitialCallback<ChatMessage> callback) {
        final List<ChatMessage> messages;
        if (params.requestedInitialKey == null || params.requestedInitialKey == Long.MAX_VALUE) {
            messages = postsDb.getMessages(chatId,null, params.requestedLoadSize, true);
        } else {
            // load around params.requestedInitialKey, otherwise the view that represents this data may jump
            messages = postsDb.getMessages(chatId, params.requestedInitialKey, params.requestedLoadSize / 2, false);
            messages.addAll(postsDb.getMessages(chatId, params.requestedInitialKey + 1, params.requestedLoadSize / 2, true));

        }
        Log.d("MessagesDataSource.loadInitial: requestedInitialKey=" + params.requestedInitialKey + " requestedLoadSize:" + params.requestedLoadSize + " got " + messages.size() +
                (messages.isEmpty() ? "" : " messages from " + messages.get(0).timestamp + " to " + messages.get(messages.size()-1).timestamp));
        callback.onResult(messages);
    }

    @Override
    public void loadAfter(@NonNull LoadParams<Long> params, @NonNull LoadCallback<ChatMessage> callback) {
        Log.d("MessagesDataSource.loadAfter: key=" + params.key + " requestedLoadSize:" + params.requestedLoadSize);
        callback.onResult(postsDb.getMessages(chatId, params.key, params.requestedLoadSize, true));
    }

    @Override
    public void loadBefore(@NonNull LoadParams<Long> params, @NonNull LoadCallback<ChatMessage> callback) {
        Log.d("MessagesDataSource.loadBefore: key=" + params.key + " requestedLoadSize:" + params.requestedLoadSize);
        callback.onResult(postsDb.getMessages(chatId, params.key, params.requestedLoadSize, false));
    }
}

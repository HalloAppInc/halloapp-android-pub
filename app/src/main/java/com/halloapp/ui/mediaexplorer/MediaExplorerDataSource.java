package com.halloapp.ui.mediaexplorer;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;

import com.halloapp.content.ContentDb;
import com.halloapp.content.Media;
import com.halloapp.id.ChatId;

import java.util.List;

public class MediaExplorerDataSource extends ItemKeyedDataSource<Long, MediaExplorerViewModel.MediaModel> {

    public static class Factory extends DataSource.Factory<Long, MediaExplorerViewModel.MediaModel> {

        private final ContentDb contentDb;
        private final ChatId chatId;
        private final List<MediaExplorerViewModel.MediaModel> preloaded;

        private MediaExplorerDataSource source;

        public Factory(@NonNull ContentDb contentDb, @NonNull ChatId chatId, @NonNull List<MediaExplorerViewModel.MediaModel> preloaded) {
            this.contentDb = contentDb;
            this.chatId = chatId;
            this.preloaded = preloaded;

            source = new MediaExplorerDataSource(contentDb, chatId, preloaded);
        }

        @Override
        public @NonNull DataSource<Long, MediaExplorerViewModel.MediaModel> create() {
            if (source.isInvalid()) {
                source = new MediaExplorerDataSource(contentDb, chatId, preloaded);
            }

            return source;
        }
    }

    private final ContentDb contentDb;
    private final ChatId chatId;
    private final List<MediaExplorerViewModel.MediaModel> preloaded;

    private MediaExplorerDataSource(@NonNull ContentDb contentDb, @NonNull ChatId chatId, @NonNull List<MediaExplorerViewModel.MediaModel> preloaded) {
        this.contentDb = contentDb;
        this.chatId = chatId;
        this.preloaded = preloaded;
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<Long> params, @NonNull LoadInitialCallback<MediaExplorerViewModel.MediaModel> callback) {
        int total = (int) contentDb.getChatMediaCount(chatId);

        int position = 0;
        if (preloaded.size() > 0) {
            position = (int) contentDb.getChatMediaPosition(chatId, preloaded.get(0).rowId);
        }

        callback.onResult(preloaded, position, total);
    }

    @Override
    public void loadAfter(@NonNull LoadParams<Long> params, @NonNull LoadCallback<MediaExplorerViewModel.MediaModel> callback) {
        List<Media> media = contentDb.getChatMedia(chatId, params.key, params.requestedLoadSize, true);
        callback.onResult(MediaExplorerViewModel.MediaModel.fromMedia(media));
    }

    @Override
    public void loadBefore(@NonNull LoadParams<Long> params, @NonNull LoadCallback<MediaExplorerViewModel.MediaModel> callback) {
        List<Media> media = contentDb.getChatMedia(chatId, params.key, params.requestedLoadSize, false);
        callback.onResult(MediaExplorerViewModel.MediaModel.fromMedia(media));
    }

    @NonNull
    @Override
    public Long getKey(@NonNull MediaExplorerViewModel.MediaModel item) {
        return item.rowId;
    }
}

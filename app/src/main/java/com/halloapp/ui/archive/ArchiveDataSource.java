package com.halloapp.ui.archive;

import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;

import com.halloapp.content.ContentDb;
import com.halloapp.content.Post;
import com.halloapp.util.logs.Log;

import java.util.List;

public class ArchiveDataSource extends ItemKeyedDataSource<Pair<Long, String>, Post> {

    private final ContentDb contentDb;

    public static class Factory extends DataSource.Factory<Pair<Long, String>, Post> {
        private final ContentDb contentDb;
        private ArchiveDataSource latestSource;

        public Factory(@NonNull ContentDb contentDb) {
            this.contentDb = contentDb;
            latestSource = new ArchiveDataSource(contentDb);
        }

        @NonNull
        @Override
        public DataSource<Pair<Long, String>, Post> create() {
            if (latestSource.isInvalid()) {
                latestSource = new ArchiveDataSource(contentDb);
            }
            return latestSource;
        }

        public void invalidateLatestDataSource() {
            Log.i("ArchiveDataSource.Factory.invalidateLatestDataSource");
            latestSource.invalidate();
        }
    }

    private ArchiveDataSource(@NonNull ContentDb contentDb) {
        this.contentDb = contentDb;
    }

    public void invalidateLatestDataSource() {
        invalidate();
    }

    @NonNull
    @Override
    public Pair<Long, String> getKey(@NonNull Post post) {
        return new Pair<>(post.timestamp, post.id);
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<Pair<Long, String>> params, @NonNull LoadInitialCallback<Post> callback) {
        final List<Post> posts = contentDb.getArchivedPosts(null, params.requestedLoadSize, false);
        callback.onResult(posts);
    }

    @Override
    public void loadAfter(@NonNull LoadParams<Pair<Long, String>> params, @NonNull LoadCallback<Post> callback) {
        final List<Post> posts = contentDb.getArchivedPosts(params.key.first, params.requestedLoadSize, false);
        callback.onResult(posts);
    }

    @Override
    public void loadBefore(@NonNull LoadParams<Pair<Long, String>> params, @NonNull LoadCallback<Post> callback) {
        final List<Post> posts = contentDb.getArchivedPosts(params.key.first, params.requestedLoadSize, true);
        callback.onResult(posts);
    }
}

package com.halloapp.ui.archive;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.halloapp.content.ContentDb;
import com.halloapp.content.Post;

public class ArchiveViewModel extends AndroidViewModel {

    final LiveData<PagedList<Post>> archiveItemList;
    private final ArchiveDataSource.Factory dataSourceFactory;
    private final ContentDb contentDb;

    public ArchiveViewModel(@NonNull Application application) {
        super(application);
        contentDb = ContentDb.getInstance();
        contentDb.addObserver(contentObserver);

        dataSourceFactory = new ArchiveDataSource.Factory(contentDb);
        archiveItemList = new LivePagedListBuilder<>(dataSourceFactory, new PagedList.Config.Builder().setPageSize(40).setEnablePlaceholders(false).build()).build();

    }

    private final ContentDb.Observer contentObserver = new ContentDb.DefaultObserver() {
        @Override
        public void onArchivedPostRemoved(@NonNull Post post) {
            dataSourceFactory.invalidateLatestDataSource();
        }
    };
}

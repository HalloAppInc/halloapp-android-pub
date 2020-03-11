package com.halloapp.ui.mediapicker;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.core.util.Preconditions;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

public class MediaPickerViewModel extends AndroidViewModel {

    final LiveData<PagedList<GalleryItem>> mediaList;

    public MediaPickerViewModel(@NonNull Application application, boolean includeVideos) {
        super(application);

        final GalleryDataSource.Factory dataSourceFactory = new GalleryDataSource.Factory(getApplication().getContentResolver(), includeVideos);
        mediaList = new LivePagedListBuilder<>(dataSourceFactory, 250).build();
    }

    void invalidate() {
        Preconditions.checkNotNull(mediaList.getValue()).getDataSource().invalidate();
    }
}


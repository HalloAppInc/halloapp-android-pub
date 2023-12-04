package com.halloapp.ui.mediapicker;

import android.app.Application;
import android.content.ContentUris;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.halloapp.FileStore;
import com.halloapp.Preferences;
import com.halloapp.content.ContentDb;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.logs.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MediaPickerViewModel extends AndroidViewModel {
    public final static int LAYOUT_DAY_LARGE = 1;
    public final static int LAYOUT_DAY_SMALL = 2;
    public final static int LAYOUT_MONTH = 3;

    public final static int SPAN_COUNT_DAY_LARGE = 6;
    public final static int SPAN_COUNT_DAY_SMALL = 3;
    public final static int SPAN_COUNT_MONTH = 5;

    /**
     * The day layout with large thumbnails consists of blocks of up to 5 items.
     * Two items sit on the first row and three on the second.
     */
    public final static int BLOCK_SIZE_DAY_LARGE = 5;
    public final static int BLOCK_DAY_LARGE_SIZE_ROW_1 = 2;
    public final static int BLOCK_DAY_LARGE_SIZE_ROW_2 = 3;

    private final BgWorkers bgWorkers = BgWorkers.getInstance();
    private final Preferences preferences = Preferences.getInstance();
    private final ContentDb contentDb = ContentDb.getInstance();

    private final GalleryDataSource.Factory dataSourceFactory;
    private final MutableLiveData<Integer> layout = new MutableLiveData<>();
    private final LiveData<PagedList<GalleryItem>> mediaList;
    private final MutableLiveData<List<Long>> selected = new MutableLiveData<>();

    public ArrayList<Uri> original;
    public Bundle state;

    private final ContentDb.Observer contentObserver = new ContentDb.DefaultObserver() {

        @Override
        public void onSuggestedGalleryItemsAdded(@NonNull List<Long> suggestedGalleryItems) {
            selected.postValue(suggestedGalleryItems);
        }
    };

    public MediaPickerViewModel(@NonNull Application application, boolean includeVideos, @NonNull long[] selected) {
        this(application, includeVideos);

        ArrayList<Long> items = new ArrayList<>(selected.length);
        for (long id : selected) {
            items.add(id);
        }

        this.selected.setValue(items);
    }

    public MediaPickerViewModel(@NonNull Application application, boolean includeVideos, @NonNull List<Uri> selected) {
        this(application, includeVideos);
        setSelected(selected);
    }

    public MediaPickerViewModel(@NonNull Application application, boolean includeVideos) {
        super(application);

        dataSourceFactory = new GalleryDataSource.Factory(getApplication().getContentResolver(), includeVideos);
        mediaList = new LivePagedListBuilder<>(dataSourceFactory, 250).build();

        bgWorkers.execute(() -> layout.postValue(preferences.getPickerLayout()));
    }

    public MediaPickerViewModel(@NonNull Application application, boolean includeVideos, @NonNull String suggestionId, int size) {
        super(application);
        dataSourceFactory = new GalleryDataSource.Factory(getApplication().getContentResolver(), includeVideos, suggestionId, getApplication().getAssets());
        contentDb.addObserver(contentObserver);

        PagedList.Config config = new PagedList.Config.Builder().setInitialLoadSizeHint(size).setMaxSize(size).build();
        mediaList = new LivePagedListBuilder<>(dataSourceFactory, config).build();
        bgWorkers.execute(() -> {
            layout.postValue(preferences.getPickerLayout());
            List<Long> selectedIds = contentDb.getSelectedGalleryItemIds(suggestionId);
            if (!selectedIds.isEmpty()) {
                selected.postValue(selectedIds);
            }
        });
    }

    @Override
    protected void onCleared() {
        contentDb.removeObserver(contentObserver);
    }

    void invalidate() {
        dataSourceFactory.invalidateLatestDataSource();
    }

    public void setSelected(List<Uri> uris) {
        if (uris == null) {
            return;
        }

        ArrayList<Long> items = new ArrayList<>(uris.size());
        for (Uri uri : uris) {
            try {
                items.add(ContentUris.parseId(uri));
            } catch (UnsupportedOperationException | NumberFormatException e) {
                Log.e("MediaPickerViewModel: exception uri=" + uri.toString(), e);
            }
        }
        selected.setValue(items);
    }

    public LiveData<PagedList<GalleryItem>> getMediaList() {
        return mediaList;
    }

    public LiveData<List<Long>> getSelected() {
        return selected;
    }

    public long[] getSelectedArray() {
        List<Long> selected = getSelected().getValue();

        if (selected != null) {
            final long[] items = new long[selected.size()];
            int i = 0;
            for (Long id : selected) {
                items[i++] = id;
            }

            return items;
        }

        return null;
    }

    public void select(long id) {
        if (selected.getValue() == null) {
            ArrayList<Long> items = new ArrayList<>();
            items.add(id);

            selected.setValue(items);
        } else {
            List<Long> list = selected.getValue();
            list.add(id);
            selected.setValue(list);
        }
    }

    public void deselect(long id) {
        List<Long> list = selected.getValue();
        if (list != null) {
            list.remove(id);
            selected.setValue(list);
        }
    }

    public void deselectAll() {
        selected.setValue(new ArrayList<>());
    }

    public int selectedSize() {
        List<Long> list = selected.getValue();
        return list == null ? 0 : list.size();
    }

    public boolean isSelected(long id) {
        List<Long> list = selected.getValue();
        return list != null && list.contains(id);
    }

    public int indexOfSelected(long id) {
        List<Long> list = selected.getValue();
        return list == null ? -1 : list.indexOf(id);
    }

    public ArrayList<Uri> getSelectedUris() {
        List<Long> list = selected.getValue();

        if (list != null) {
            final ArrayList<Uri> uris = new ArrayList<>(list.size());

            for (Long id : list) {
                uris.add(ContentUris.withAppendedId(MediaStore.Files.getContentUri(GalleryDataSource.MEDIA_VOLUME), id));
            }

            return uris;
        }

        return null;
    }

    public void clean(List<Uri> uris) {
        bgWorkers.execute(() -> {
            final FileStore store = FileStore.getInstance();

            for (Uri uri : uris) {
                final File original = store.getTmpFileForUri(uri, null);
                final File edit = store.getTmpFileForUri(uri, "edit");

                if (original.exists()) {
                    original.delete();
                }

                if (edit.exists()) {
                    edit.delete();
                }
            }
        });
    }

    public LiveData<Integer> getLayout() {
        return layout;
    }

    @MainThread
    public void setLayout(int layout) {
        this.layout.setValue(layout);
        bgWorkers.execute(() -> preferences.setPickerLayout(layout));
    }
}


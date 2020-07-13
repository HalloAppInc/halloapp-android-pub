package com.halloapp.ui.mediapicker;

import android.app.Application;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.halloapp.util.Preconditions;

import java.util.ArrayList;
import java.util.List;

public class MediaPickerViewModel extends AndroidViewModel {

    final LiveData<PagedList<GalleryItem>> mediaList;
    final MutableLiveData<List<Long>> selected = new MutableLiveData<>();

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

        final GalleryDataSource.Factory dataSourceFactory = new GalleryDataSource.Factory(getApplication().getContentResolver(), includeVideos);
        mediaList = new LivePagedListBuilder<>(dataSourceFactory, 250).build();
    }

    void invalidate() {
        Preconditions.checkNotNull(mediaList.getValue()).getDataSource().invalidate();
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
                // Ignore, uri not part of gallery
            }
        }
        selected.setValue(items);
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
        // TODO(stefan): remove original and edit files from cache folder
    }
}


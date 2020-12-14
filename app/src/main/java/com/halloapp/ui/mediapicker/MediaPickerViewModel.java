package com.halloapp.ui.mediapicker;

import android.app.Application;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.halloapp.FileStore;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MediaPickerViewModel extends AndroidViewModel {

    final LiveData<PagedList<GalleryItem>> mediaList;
    final MutableLiveData<List<Long>> selected = new MutableLiveData<>();

    public ArrayList<Uri> original;
    public Bundle state;

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
                Log.e("MediaPickerViewModel: exception uri=" + uri.toString(), e);
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

    public void getUrisOrderedByDate(Observer<ArrayList<Uri>> observer) {
        BgWorkers.getInstance().execute(() -> {
            HashMap<Long, Long> dates = new HashMap<>();
            List<Long> list = selected.getValue();

            String selection = MediaStore.Files.FileColumns._ID + " in (" + TextUtils.join(",", list) + ")";
            final String[] projection = new String[] {
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATE_ADDED,
            };

            try(final Cursor cursor = getApplication().getContentResolver().query(
                    MediaStore.Files.getContentUri(GalleryDataSource.MEDIA_VOLUME),
                    projection,
                    selection,
                    null,
                    null)) {
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        dates.put(cursor.getLong(0), cursor.getLong(1));
                    }
                }
            } catch (SecurityException ex) {
                Log.w("MediaPickerViewModel.getOrderedByDateUris", ex);
            }

            Collections.sort(list, (a, b) -> {
                long dateA = dates.containsKey(a) ? dates.get(a) : 0;
                long dateB = dates.containsKey(b) ? dates.get(b) : 0;

                return Long.compare(dateA, dateB);
            });

            ArrayList<Uri> uris = new ArrayList<>(list.size());
            for (Long id : list) {
                uris.add(ContentUris.withAppendedId(MediaStore.Files.getContentUri(GalleryDataSource.MEDIA_VOLUME), id));
            }

            new Handler(getApplication().getMainLooper()).post(() -> observer.onChanged(uris));
        });
    }

    public void clean(List<Uri> uris) {
        BgWorkers.getInstance().execute(() -> {
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
}


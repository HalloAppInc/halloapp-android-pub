package com.halloapp.ui.mediapicker;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

public class MediaPickerViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;
    private final boolean includeVideos;
    private final long[] selectedIds;
    private final List<Uri> selelctedUris;
    private final String suggestionId;
    private final int size;


    MediaPickerViewModelFactory(@NonNull Application application, boolean includeVideos, @NonNull long[] selected) {
        this.application = application;
        this.includeVideos = includeVideos;
        this.selectedIds = selected;
        this.selelctedUris = null;
        this.suggestionId = null;
        this.size = 0;
    }

    MediaPickerViewModelFactory(@NonNull Application application, boolean includeVideos, @NonNull List<Uri> selected) {
        this.application = application;
        this.includeVideos = includeVideos;
        this.selectedIds = null;
        this.selelctedUris = selected;
        this.suggestionId = null;
        this.size = 0;
    }

    MediaPickerViewModelFactory(@NonNull Application application, boolean includeVideos) {
        this.application = application;
        this.includeVideos = includeVideos;
        this.selectedIds = null;
        this.selelctedUris = null;
        this.suggestionId = null;
        this.size = 0;
    }

    MediaPickerViewModelFactory(@NonNull Application application, boolean includeVideos, @NonNull String suggestionId, int size) {
        this.application = application;
        this.includeVideos = includeVideos;
        this.selectedIds = null;
        this.selelctedUris = null;
        this.suggestionId = suggestionId;
        this.size = size;
    }

    @Override
    public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(MediaPickerViewModel.class)) {
            if (selectedIds != null) {
                //noinspection unchecked
                return (T) new MediaPickerViewModel(application, includeVideos, selectedIds);
            } else if (selelctedUris != null) {
                //noinspection unchecked
                return (T) new MediaPickerViewModel(application, includeVideos, selelctedUris);
            } else if (suggestionId != null) {
                //noinspection unchecked
                return (T) new MediaPickerViewModel(application, includeVideos, suggestionId, size);
            } else {
                //noinspection unchecked
                return (T) new MediaPickerViewModel(application, includeVideos);
            }
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}

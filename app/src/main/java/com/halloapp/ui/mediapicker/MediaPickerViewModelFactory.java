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


    MediaPickerViewModelFactory(@NonNull Application application, boolean includeVideos, @NonNull long[] selected) {
        this.application = application;
        this.includeVideos = includeVideos;
        this.selectedIds = selected;
        this.selelctedUris = null;
    }

    MediaPickerViewModelFactory(@NonNull Application application, boolean includeVideos, @NonNull List<Uri> selected) {
        this.application = application;
        this.includeVideos = includeVideos;
        this.selectedIds = null;
        this.selelctedUris = selected;
    }

    MediaPickerViewModelFactory(@NonNull Application application, boolean includeVideos) {
        this.application = application;
        this.includeVideos = includeVideos;
        this.selectedIds = null;
        this.selelctedUris = null;
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
            } else {
                //noinspection unchecked
                return (T) new MediaPickerViewModel(application, includeVideos);
            }
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}

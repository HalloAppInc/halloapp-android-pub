package com.halloapp.ui.mediapicker;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class MediaPickerViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;
    private final boolean includeVideos;


    MediaPickerViewModelFactory(@NonNull Application application, boolean includeVideos) {
        this.application = application;
        this.includeVideos = includeVideos;
    }

    @Override
    public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(MediaPickerViewModel.class)) {
            //noinspection unchecked
            return (T) new MediaPickerViewModel(application, includeVideos);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}

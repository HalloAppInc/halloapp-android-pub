package com.halloapp.ui.contacts;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class ShareContactViewModel extends AndroidViewModel {

    private long contactId;

    public ShareContactViewModel(@NonNull Application application, long id) {
        super(application);
        this.contactId = id;
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final Application application;
        private final long id;

        Factory(@NonNull Application application, long id) {
            this.application = application;
            this.id = id;
        }

        @Override
        public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(ShareContactViewModel.class)) {
                //noinspection unchecked
                return (T) new ShareContactViewModel(application, id);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}

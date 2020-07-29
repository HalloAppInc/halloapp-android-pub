package com.halloapp.util;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.halloapp.Constants;

import java.lang.ref.WeakReference;

public class DelayedProgressLiveData<T> extends MutableLiveData<T> {

    private GlobalUI globalUI = GlobalUI.getInstance();

    @Override
    public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
        final WeakReference<LifecycleOwner> ownerWeakReference = new WeakReference<>(owner);
        globalUI.postDelayed(() -> {
            LifecycleOwner ownerRef = ownerWeakReference.get();
            if (ownerRef != null) {
                super.observe(owner, observer);
            }
        }, Constants.MINIMUM_PROGRESS_DIALOG_TIME_MILLIS);
    }
}

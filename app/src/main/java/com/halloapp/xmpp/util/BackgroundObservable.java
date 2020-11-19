package com.halloapp.xmpp.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.util.BgWorkers;

public class BackgroundObservable<T> extends MutableObservable<T> {

    private final @NonNull BgWorkers bgWorkers;

    public BackgroundObservable(@NonNull BgWorkers bgWorkers) {
        this.bgWorkers = bgWorkers;
    }

    @Override
    protected synchronized void handleResponse(@Nullable T response) {
        bgWorkers.execute(() -> {
            super.handleResponse(response);
        });
    }

    @Override
    protected synchronized void handleError(@NonNull Exception e) {
        bgWorkers.execute(() -> {
            super.handleError(e);
        });
    }
}

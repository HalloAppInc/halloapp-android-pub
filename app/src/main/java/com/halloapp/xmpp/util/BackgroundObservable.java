package com.halloapp.xmpp.util;

import androidx.annotation.NonNull;

import com.halloapp.util.BgWorkers;

public class BackgroundObservable<T> extends MutableObservable<T> {

    private @NonNull BgWorkers bgWorkers;

    public BackgroundObservable(@NonNull BgWorkers bgWorkers) {
        this.bgWorkers = bgWorkers;
    }

    @Override
    protected synchronized void handleResponse(T response) {
        bgWorkers.execute(() -> {
            super.handleResponse(response);
        });
    }

    @Override
    protected synchronized void handleError(Exception e) {
        bgWorkers.execute(() -> {
            super.handleError(e);
        });
    }
}

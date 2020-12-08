package com.halloapp.xmpp.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.util.Preconditions;

public class MutableObservable<T> extends Observable<T> {

    private boolean resolved;
    private T response;
    private Exception error;

    private boolean cancelled;

    private @Nullable ResponseHandler<T> responseHandler;
    private @Nullable ExceptionHandler exceptionHandler;

    @Override
    public synchronized Observable<T> onResponse(@NonNull ResponseHandler<T> handler) {
        this.responseHandler = handler;
        maybeNotifyResponse();
        return this;
    }

    @Override
    public synchronized Observable<T> onError(@NonNull ExceptionHandler handler) {
        this.exceptionHandler = handler;
        maybeNotifyError();
        return this;
    }

    public final synchronized void setResponse(@Nullable T response) {
        Preconditions.checkState(!resolved, "Trying to resolve to response but already resolved");
        resolved = true;
        this.response = response;
        maybeNotifyResponse();
    }

    public final synchronized void setException(@NonNull Exception e) {
        Preconditions.checkState(!resolved, "Trying to resolve to exception but already resolved");
        resolved = true;
        this.error = e;
        maybeNotifyError();
    }

    public synchronized void cancel() {
        this.cancelled = true;
    }

    private synchronized void maybeNotifyError() {
        if (cancelled || !resolved) {
            return;
        }
        if (error != null) {
            if (exceptionHandler != null) {
                handleError(error);
            }
        }
    }

    private synchronized void maybeNotifyResponse() {
        if (cancelled || !resolved || error != null) {
            return;
        }
        if (responseHandler != null) {
            handleResponse(response);
        }
    }

    protected synchronized void handleResponse(@Nullable T response) {
        if (responseHandler != null) {
            responseHandler.handleResponse(response);
        }
    }

    protected synchronized void handleError(@NonNull Exception e) {
        if (exceptionHandler != null) {
            exceptionHandler.handleException(e);
        }
    }
}

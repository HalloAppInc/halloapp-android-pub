package com.halloapp.xmpp.util;

import androidx.annotation.Nullable;

public class MutableObservable<T> implements Observable<T> {

    private T response;

    private Exception error;

    private boolean cancelled;

    private @Nullable ResponseHandler<T> responseHandler;
    private @Nullable ExceptionHandler exceptionHandler;

    @Override
    public synchronized Observable<T> onResponse(ResponseHandler<T> handler) {
        this.responseHandler = handler;
        maybeNotifyResponse();
        return this;
    }

    @Override
    public synchronized Observable<T> onError(ExceptionHandler handler) {
        this.exceptionHandler = handler;
        maybeNotifyResponse();
        return this;
    }

    public final synchronized void setResponse(T response) {
        this.response = response;
        maybeNotifyResponse();
    }

    public final synchronized void setException(Exception e) {
        this.error = e;
        maybeNotifyResponse();
    }

    public synchronized void cancel() {
        this.cancelled = true;
    }

    private synchronized void maybeNotifyResponse() {
        if (cancelled || (response == null && error == null)) {
            return;
        }
        if (response != null && responseHandler != null) {
            handleResponse(response);
        } else if (error != null && exceptionHandler != null) {
            handleError(error);
        }
    }

    protected synchronized void handleResponse(T response) {
        if (responseHandler != null) {
            responseHandler.handleResponse(response);
        }
    }

    protected synchronized void handleError(Exception e) {
        if (exceptionHandler != null) {
            exceptionHandler.handleException(e);
        }
    }
}

package com.halloapp.xmpp.util;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;

public abstract class Observable<T> {
    public abstract Observable<T> onResponse(ResponseHandler<T> handler);
    public abstract Observable<T> onError(ExceptionHandler handler);
    public abstract void cancel();

    public <R> Observable<R> map(@NonNull Function<T, R> mapFunc) {
        MutableObservable<R> resultResponse = new MutableObservable<>();
        this.onError(resultResponse::setException);
        this.onResponse(result -> {
            resultResponse.setResponse(mapFunc.apply(result));
        });
        return resultResponse;
    }
}

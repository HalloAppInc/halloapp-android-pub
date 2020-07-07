package com.halloapp.xmpp.util;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;

public interface Observable<T> {
    Observable<T> onResponse(ResponseHandler<T> handler);
    Observable<T> onError(ExceptionHandler handler);
    void cancel();

    static <X, Y> Observable<Y> map(@NonNull Observable<X> observable, @NonNull Function<X, Y> mapFunc) {
        MutableObservable<Y> resultResponse = new MutableObservable<>();
        observable.onError(resultResponse::setException);
        observable.onResponse(result -> {
            resultResponse.setResponse(mapFunc.apply(result));
        });
        return resultResponse;
    }
}

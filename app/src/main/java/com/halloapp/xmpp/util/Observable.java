package com.halloapp.xmpp.util;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

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

    private final CountDownLatch gate = new CountDownLatch(1);
    public T await() throws ObservableErrorException, InterruptedException {
        AtomicReference<T> response = new AtomicReference<>();
        this.onResponse(v -> {
            response.set(v);
            gate.countDown();
        });

        AtomicReference<Exception> error = new AtomicReference<>();
        this.onError(v -> {
            error.set(v);
            gate.countDown();
        });

        gate.await();
        if (error.get() != null) {
            throw new ObservableErrorException(error.get());
        }
        return response.get();
    }
}

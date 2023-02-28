package com.halloapp.xmpp.util;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public abstract class Observable<T> {
    private static final int REPLY_TIMEOUT = 20_000;

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

    public Observable<T> mapError(Function<Exception, Exception> errorFunc) {
        MutableObservable<T> resultResponse = new MutableObservable<>();
        this.onError(error -> {
            resultResponse.setException(errorFunc.apply(error));
        });
        this.onResponse(resultResponse::setResponse);
        return resultResponse;
    }

    private final CountDownLatch gate = new CountDownLatch(1);

    public T await() throws ObservableErrorException, InterruptedException {
        return await(REPLY_TIMEOUT);
    }

    public T await(long timeoutMs) throws ObservableErrorException, InterruptedException {
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
        if (timeoutMs <= 0) {
            gate.await();
        } else {
            if (!gate.await(timeoutMs, TimeUnit.MILLISECONDS)) {
                throw new ObservableErrorException(new InterruptedException("timed out"));
            }
        }
        if (error.get() != null) {
            throw new ObservableErrorException(error.get());
        }
        return response.get();
    }
}

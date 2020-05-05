package com.halloapp.util;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LruCache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

public class ViewDataLoader<V extends View, R, K> {

    private final ExecutorService executor;
    private final Map<View, Future> queue = new HashMap<>();
    private final Map<K, Semaphore> keyLoadGuards = new HashMap<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface Displayer<V, R> {
        void showResult(@NonNull V view, @Nullable R result);
        void showLoading(@NonNull V view);
    }

    protected ViewDataLoader(ExecutorService executor) {
        this.executor = executor;
    }

    protected ViewDataLoader() {
        this(Executors.newSingleThreadExecutor());
    }

    @MainThread
    public void load(@NonNull V view, @NonNull Callable<R> loader, @NonNull Displayer<V, R> displayer, @NonNull K key, @Nullable LruCache<K, R> cache) {

        final Future existing = queue.get(view);
        if (existing != null) {
            existing.cancel(true);
        }
        view.setTag(key);

        if (cache != null) {
            final R result = cache.get(key);
            if (result != null) {
                displayer.showResult(view, result);
                return;
            }
        }
        displayer.showLoading(view);

        final Future future = executor.submit(() -> {
            final Semaphore keyGuard;
            try {
                keyGuard = getKeyGuard(key);
                keyGuard.acquire();
            } catch (InterruptedException e) {
                executeShowResult(view, displayer, key, null);
                return;
            }
            R result = null;
            if (cache != null) {
                result = cache.get(key);
            }
            if (result == null) {
                try {
                    result = loader.call();
                } catch (Exception e) {
                    Log.e("ViewDataLoader: exception key=" + key, e);
                }
            }
            if (result != null && cache != null) {
                cache.put(key, result);
            }
            keyGuard.release();

            executeShowResult(view, displayer, key, result);
        });
        queue.put(view, future);
    }

    private Semaphore getKeyGuard(@NonNull K key) {
        Semaphore semaphore;
        synchronized (keyLoadGuards) {
            semaphore = keyLoadGuards.get(key);
            if (semaphore == null) {
                semaphore = new Semaphore(1);
                keyLoadGuards.put(key, semaphore);
            }
        }
        return semaphore;
    }

    @MainThread
    public void cancel(@NonNull V view) {
        final Future existing = queue.get(view);
        if (existing != null) {
            existing.cancel(true);
        }
        view.setTag(null);
    }

    @MainThread
    public void destroy() {
        executor.shutdownNow();
    }

    private void executeShowResult(@NonNull V view, @NonNull Displayer<V, R> displayer, @NonNull K key, @Nullable R result) {
        if (key.equals(view.getTag())) {
            mainHandler.post(() -> {
                if (key.equals(view.getTag())) {
                    displayer.showResult(view, result);
                }
            });
        }
    }
}

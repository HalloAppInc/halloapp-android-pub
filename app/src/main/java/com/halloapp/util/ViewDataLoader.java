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

public class ViewDataLoader<V extends View, R, K> {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Map<View, Future> queue = new HashMap<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface Displayer<V, R> {
        void showResult(@NonNull V view, @Nullable R result);
        void showLoading(@NonNull V view);
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
            try {
                final R result = loader.call();
                if (result != null && cache != null) {
                    cache.put(key, result);
                }
                executeShowResult(view, displayer, key, result);
            } catch (Exception e) {
                Log.e("ViewDataLoader: exception", e);
                executeShowResult(view, displayer, key, null);
            }
        });
        queue.put(view, future);
    }

    @MainThread
    public void destroy() {
        executor.shutdownNow();
    }

    private void executeShowResult(@NonNull V view, @NonNull Displayer<V, R> displayer, @NonNull K key, @Nullable R result) {
        if (view.getTag() == key) {
            mainHandler.post(() -> {
                if (view.getTag() == key) {
                    displayer.showResult(view, result);
                }
            });
        }
    }
}

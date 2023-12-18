package com.halloapp.util;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LruCache;

import com.halloapp.content.ContentDb;
import com.halloapp.util.logs.Log;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

public class ViewDataLoader<V extends View, R, K> {

    protected final ExecutorService executor;
    protected final Map<View, Future<?>> queue = new WeakHashMap<>();
    private final Map<K, Semaphore> keyLoadGuards = new HashMap<>();
    protected final Handler mainHandler = new Handler(Looper.getMainLooper());

    private boolean loadSync = false;

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

    public void forceSyncLoad() {
        loadSync = true;
    }

    @MainThread
    public void load(@NonNull V view, @NonNull Callable<R> loader, @NonNull Displayer<V, R> displayer, @NonNull K key, @Nullable LruCache<K, R> cache) {
        Displayer<V, List<R>> displayerAdapter = new Displayer<V, List<R>>() {
            @Override
            public void showResult(@NonNull V view, @Nullable List<R> result) {
                displayer.showResult(view, result == null ? null : result.size() == 0 ? null : result.get(0));
            }

            @Override
            public void showLoading(@NonNull V view) {
                displayer.showLoading(view);
            }
        };
        loadMultiple(view, Collections.singletonList(loader), displayerAdapter, Collections.singletonList(key), cache);
    }

    private void loadMultipleSync(@NonNull V view, @NonNull List<Callable<R>> loaders, @NonNull Displayer<V, List<R>> displayer, @NonNull List<K> keys, @Nullable LruCache<K, R> cache) {
        List<R> results = new ArrayList<>();
        for (int i=0; i<keys.size(); i++) {
            K key = keys.get(i);
            R result = null;
            if (cache != null) {
                result = cache.get(key);
            }
            if (result == null) {
                try {
                    result = loaders.get(i).call();
                } catch (Exception e) {
                    Log.e("ViewDataLoader: exception key=" + key, e);
                }
            }
            results.add(result);
            if (result != null && cache != null) {
                cache.put(key, result);
            }
        }
        displayer.showResult(view, results);
    }

    @MainThread
    public void loadMultiple(@NonNull V view, @NonNull List<Callable<R>> loaders, @NonNull Displayer<V, List<R>> displayer, @NonNull List<K> keys, @Nullable LruCache<K, R> cache) {
        if (loadSync) {
            loadMultipleSync(view, loaders, displayer, keys, cache);
            return;
        }
        final Future<?> existing = queue.get(view);
        if (existing != null) {
            existing.cancel(true);
        }
        view.setTag(getTag(keys));

        if (cache != null) {
            List<R> results = new ArrayList<>();
            for (K key : keys) {
                final R result = cache.get(key);
                if (result == null) {
                    break;
                }
                results.add(result);
            }
            if (results.size() == keys.size()) {
                displayer.showResult(view, results);
                return;
            }
        }
        displayer.showLoading(view);

        final Future<?> future = executor.submit(() -> {
            List<R> results = new ArrayList<>();
            for (int i=0; i<keys.size(); i++) {
                K key = keys.get(i);
                final Semaphore keyGuard;
                try {
                    keyGuard = getKeyGuard(key);
                    keyGuard.acquire();
                } catch (InterruptedException e) {
                    executeShowResult(view, displayer, keys, null);
                    return;
                }
                R result = null;
                if (cache != null) {
                    result = cache.get(key);
                }
                if (result == null) {
                    try {
                        result = loaders.get(i).call();
                    } catch (FileNotFoundException e) {
                        Log.w("ViewDataLoader: file not found exception key=" + key, e);
                        if (key instanceof Long) {
                            ContentDb.getInstance().deleteGalleryItemFromSuggestion((Long) key);
                            ContentDb.getInstance().deleteGalleryItem((Long) key);
                        }
                    } catch (Exception e) {
                        Log.e("ViewDataLoader: exception key=" + key, e);
                    }
                }
                results.add(result);
                if (result != null && cache != null) {
                    cache.put(key, result);
                }
                keyGuard.release();
            }

            executeShowResult(view, displayer, keys, results);
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
        final Future<?> existing = queue.get(view);
        if (existing != null) {
            existing.cancel(true);
        }
        view.setTag(null);
    }

    @MainThread
    public void destroy() {
        executor.shutdownNow();
    }

    protected void executeShowResult(@NonNull V view, @NonNull Displayer<V, List<R>> displayer, @NonNull List<K> keys, @Nullable List<R> result) {

        if (getTag(keys).equals(view.getTag())) {
            mainHandler.post(() -> {
                if (getTag(keys).equals(view.getTag())) {
                    displayer.showResult(view, result);
                }
            });
        }
    }

    private Object getTag(List<K> keys) {
        return keys.size() == 1 ? keys.get(0) : keys;
    }
}

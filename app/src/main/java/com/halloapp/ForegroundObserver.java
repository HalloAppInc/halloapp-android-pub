package com.halloapp;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

public class ForegroundObserver implements LifecycleObserver {

    private static ForegroundObserver instance;

    public static ForegroundObserver getInstance() {
        if (instance == null) {
            synchronized (ForegroundObserver.class) {
                if (instance == null) {
                    instance = new ForegroundObserver();
                }
            }
        }
        return instance;
    }

    private boolean isInForeground = false;

    @SuppressWarnings("unused")
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    void onBackground() {
        isInForeground = false;
    }

    @SuppressWarnings("unused")
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    void onForeground() {
        isInForeground = true;
    }

    public boolean isInForeground() {
        return isInForeground;
    }
}

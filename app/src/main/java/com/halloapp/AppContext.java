package com.halloapp;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;

import com.halloapp.util.Preconditions;

public class AppContext {

    private static AppContext instance;

    public static AppContext getInstance() {
        if (instance == null) {
            synchronized (AppContext.class) {
                if (instance == null) {
                    instance = new AppContext();
                }
            }
        }
        return instance;
    }

    private volatile Context applicationContext;

    @NonNull
    public Context get() {
        return Preconditions.checkNotNull(applicationContext);
    }

    public void setApplicationContext(@NonNull Application application) {
        this.applicationContext = application.getApplicationContext();
    }
}

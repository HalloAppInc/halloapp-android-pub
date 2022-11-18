package com.halloapp.newapp;

import android.app.Application;

import com.halloapp.AppContext;

public class NewApp extends Application {
    private final AppContext appContext = AppContext.getInstance();

    @Override
    public void onCreate() {
        super.onCreate();
        appContext.setApplicationContext(this);
    }
}

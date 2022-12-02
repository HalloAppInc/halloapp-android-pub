package com.halloapp;

import android.app.Application;

import com.halloapp.emoji.EmojiManager;
import com.halloapp.props.ServerProps;
import com.halloapp.ui.BlurManager;
import com.halloapp.util.logs.Log;

public class App extends Application {
    private final AppContext appContext = AppContext.getInstance();

    @Override
    public void onCreate() {
        super.onCreate();
        initSync();
    }

    /**
     * Synchronous init, try to do as little work here as possible
     */
    private void initSync() {
        appContext.setApplicationContext(this);
        Log.init(FileStore.getInstance());
        Log.i("HalloApp init " + BuildConfig.VERSION_NAME + " " + BuildConfig.GIT_HASH);

        Log.wrapCrashlytics();

        // Init server props synchronously so we have the correct values loaded
        ServerProps.getInstance().init();
        Preferences.getInstance().init();

        EmojiManager.getInstance().init(this);
        BlurManager.getInstance().init();
    }
}

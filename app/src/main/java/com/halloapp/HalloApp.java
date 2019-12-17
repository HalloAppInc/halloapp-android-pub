package com.halloapp;

import android.app.Application;
import android.content.res.Configuration;

import com.halloapp.posts.PostsDb;
import com.halloapp.util.Log;

import org.jivesoftware.smack.SmackConfiguration;

public class HalloApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        SmackConfiguration.DEBUG = BuildConfig.DEBUG;

        final PostsDb postsDb = PostsDb.getInstance(this);
        final Connection connection = Connection.getInstance(new ConnectionObserver(postsDb));
        postsDb.addObserver(new MainPostsObserver(connection));
        connection.connect("16502813677", "nAfPGIEv6lrovdTfpPq1OgIyy6u9L6kF");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.w("low memory");
    }
}

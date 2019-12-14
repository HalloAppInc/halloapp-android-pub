package com.halloapp;

import android.app.Application;
import android.content.res.Configuration;

import com.halloapp.util.Log;

import org.jivesoftware.smack.SmackConfiguration;

public class HalloApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        SmackConfiguration.DEBUG = BuildConfig.DEBUG;

        //Connection.getInstance().connect("16502813677", "11111111");
        Connection.getInstance().connect("16502813677", "nAfPGIEv6lrovdTfpPq1OgIyy6u9L6kF");

        //"14088922686@s.halloapp.net" //tony
        //"14154121848@s.halloapp.net" //michael
        //"13477521636@s.halloapp.net" //d
        //"16507967982@s.halloapp.net" //vipin
        //"16503363079@s.halloapp.net" //neeraj
        // 16505553000
        Connection.getInstance().sendMessage("14154121848@s.halloapp.net", "Hi! Current time is " + System.currentTimeMillis() + ".");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.w("low memory");
    }
}

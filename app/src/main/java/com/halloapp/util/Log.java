package com.halloapp.util;

import com.crashlytics.android.Crashlytics;

public class Log {

    private static final String TAG = "halloapp";

    public static void v(String msg) {
        android.util.Log.v(TAG, msg);
    }

    public static void d(String msg) {
        Crashlytics.log(android.util.Log.DEBUG, TAG, msg);
    }

    public static void i(String msg) {
        Crashlytics.log(android.util.Log.INFO, TAG, msg);
    }

    public static void w(String msg) {
        Crashlytics.log(android.util.Log.WARN, TAG, msg);
    }

    public static void w(String msg, Throwable tr) {
        Crashlytics.log(android.util.Log.WARN, TAG, msg + '\n' + android.util.Log.getStackTraceString(tr));
    }

    public static void e(String msg) {
        Crashlytics.log(android.util.Log.ERROR, TAG, msg);
    }

    public static void e(String msg, Throwable tr) {
        Crashlytics.log(android.util.Log.ERROR, TAG, msg + '\n' + android.util.Log.getStackTraceString(tr));
    }
}

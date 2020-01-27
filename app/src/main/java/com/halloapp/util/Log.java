package com.halloapp.util;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

public class Log {

    private static final String TAG = "halloapp";

    public static void v(String msg) {
        android.util.Log.v(TAG, msg);
    }

    public static void d(String msg) {
        if (Fabric.isInitialized()) {
            Crashlytics.log(android.util.Log.DEBUG, TAG, msg);
        } else {
            android.util.Log.d(TAG, msg);
        }
    }

    public static void i(String msg) {
        if (Fabric.isInitialized()) {
            Crashlytics.log(android.util.Log.INFO, TAG, msg);
        } else {
            android.util.Log.i(TAG, msg);
        }
    }

    public static void w(String msg) {
        if (Fabric.isInitialized()) {
            Crashlytics.log(android.util.Log.WARN, TAG, msg);
        } else {
            android.util.Log.w(TAG, msg);
        }
    }

    public static void w(String msg, Throwable tr) {
        if (Fabric.isInitialized()) {
            Crashlytics.log(android.util.Log.WARN, TAG, msg + '\n' + android.util.Log.getStackTraceString(tr));
        } else {
            android.util.Log.w(TAG, msg, tr);
        }
    }

    public static void e(String msg) {
        if (Fabric.isInitialized()) {
            Crashlytics.log(android.util.Log.ERROR, TAG, msg);
        } else {
            android.util.Log.e(TAG, msg);
        }
    }

    public static void e(String msg, Throwable tr) {
        if (Fabric.isInitialized()) {
            Crashlytics.log(android.util.Log.ERROR, TAG, msg + '\n' + android.util.Log.getStackTraceString(tr));
        } else {
            android.util.Log.e(TAG, msg, tr);
        }
    }

    public static void sendErrorReport(String msg) {
        Log.e(msg + " (sending error report)");
        if (Fabric.isInitialized()) {
            Crashlytics.logException(new RuntimeException(msg));
        }
    }
}

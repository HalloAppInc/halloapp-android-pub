package com.halloapp.util;

import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.crashlytics.android.Crashlytics;
import com.halloapp.Me;

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

    public static void setUser(@NonNull Me me) {
        new SetCrashlyticsUserTask(me).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private static class SetCrashlyticsUserTask extends AsyncTask<Void, Void, Void> {

        final Me me;

        SetCrashlyticsUserTask(@NonNull Me me) {
            this.me = me;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (Fabric.isInitialized()) {
                Crashlytics.setString("user", me.getUser());
            }
            return null;
        }
    }
}

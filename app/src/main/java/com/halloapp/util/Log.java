package com.halloapp.util;

import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.halloapp.Me;

import java.lang.reflect.Method;
import java.util.Arrays;


public class Log {

    private static final String TAG = "halloapp";

    public static void v(String msg) {
        android.util.Log.v(TAG, msg);
    }

    public static void d(String msg) {
        long threadID = Thread.currentThread().getId();
        FirebaseCrashlytics.getInstance().log(threadID + "/D/halloApp: " + msg);
    }

    public static void i(String msg) {
        long threadID = Thread.currentThread().getId();
        FirebaseCrashlytics.getInstance().log(threadID + "/I/halloApp: " + msg);
    }

    public static void i(String msg, Throwable tr) {
        long threadID = Thread.currentThread().getId();
        FirebaseCrashlytics.getInstance().log(threadID + "/I/halloApp: " + msg + "\n" + tr.getMessage());
    }

    public static void w(String msg) {
        long threadID = Thread.currentThread().getId();
        FirebaseCrashlytics.getInstance().log(threadID + "/W/halloApp: " + msg);
    }

    public static void w(String msg, Throwable tr) {
        long threadID = Thread.currentThread().getId();
        FirebaseCrashlytics.getInstance().log(threadID + "/W/halloApp: " + msg + "\n" + android.util.Log.getStackTraceString(tr));
    }

    public static void e(String msg) {
        long threadID = Thread.currentThread().getId();
        FirebaseCrashlytics.getInstance().log(threadID + "/E/halloApp: " + msg);
    }

    public static void e(String msg, Throwable tr) {
        long threadID = Thread.currentThread().getId();
        FirebaseCrashlytics.getInstance().log(threadID + "/E/halloApp: " + msg + "\n" + android.util.Log.getStackTraceString(tr));
    }

    public static void sendErrorReport(String msg) {
        Log.e(msg + " (sending error report)");

        // Cannot hard-code; proguard minification will change the names
        String logClassName = Log.class.getName();
        Method thisMethod = new Object() {}.getClass().getEnclosingMethod();
        String methodName = thisMethod == null ? "" : thisMethod.getName();

        // Remove irrelevant stack elements so that Crashlytics grouping works
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (int i = 0; i < stackTrace.length; i++) {
            StackTraceElement elem = stackTrace[i];
            if (elem.getClassName().equals(logClassName) && elem.getMethodName().equals(methodName)) {
                stackTrace = Arrays.copyOfRange(stackTrace, i + 1, stackTrace.length);
                break;
            }
        }
        Throwable e = new ConstructedException(msg, stackTrace);

        FirebaseCrashlytics.getInstance().recordException(e);
    }

    private static class ConstructedException extends Throwable {
        ConstructedException(String message, StackTraceElement[] stackTrace) {
            super(message, null, false, true);
            setStackTrace(stackTrace);
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
            FirebaseCrashlytics.getInstance().setCustomKey("user", me.getUser());
            return null;
        }
    }
}

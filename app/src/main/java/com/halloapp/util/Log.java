package com.halloapp.util;

import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.halloapp.BuildConfig;
import com.halloapp.Me;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Log {

    private static final String TAG = "halloapp";

    public static void v(String msg) {
        if (BuildConfig.DEBUG) {
            android.util.Log.v(TAG, msg);
        }
    }

    public static void d(String msg) {
        FirebaseCrashlytics.getInstance().log(Thread.currentThread().getName() +
                "/D/halloApp: " + msg);
        if (BuildConfig.DEBUG) {
            android.util.Log.d(TAG, msg);
        }
    }

    public static void i(String msg) {
        FirebaseCrashlytics.getInstance().log(Thread.currentThread().getName() +
                "/I/halloApp: " + msg);
        if (BuildConfig.DEBUG) {
            android.util.Log.i(TAG, msg);
        }
    }

    public static void i(String msg, Throwable tr) {
        FirebaseCrashlytics.getInstance().log(Thread.currentThread().getName() +
                "/I/halloApp: " + msg + "\n" + tr.getMessage());
        if (BuildConfig.DEBUG) {
            android.util.Log.i(TAG, msg + " " + tr.getMessage());
        }
    }

    public static void w(String msg) {
        FirebaseCrashlytics.getInstance().log(Thread.currentThread().getName() +
                "/W/halloApp: " + msg);
        if (BuildConfig.DEBUG) {
            android.util.Log.w(TAG, msg);
        }
    }

    public static void w(String msg, Throwable tr) {
        FirebaseCrashlytics.getInstance().log(Thread.currentThread().getName() +
                "/W/halloApp: " + msg + "\n" + android.util.Log.getStackTraceString(tr));
        if (BuildConfig.DEBUG) {
            android.util.Log.w(TAG, msg, tr);
        }
    }

    public static void e(String msg) {
        FirebaseCrashlytics.getInstance().log(Thread.currentThread().getName() +
                "/E/halloApp: " + msg);
        if (BuildConfig.DEBUG) {
            android.util.Log.e(TAG, msg);
        }
    }

    public static void e(String msg, Throwable tr) {
        FirebaseCrashlytics.getInstance().log(Thread.currentThread().getName() +
                "/E/halloApp: " + msg + "\n" + android.util.Log.getStackTraceString(tr));
        if (BuildConfig.DEBUG) {
            android.util.Log.e(TAG, msg, tr);
        }
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
                stackTrace = Arrays.copyOfRange(stackTrace, i, stackTrace.length);
                break;
            }
        }

        // Set file name to message and line number to 0 to consistently group by message on Firebase
        stackTrace[0] = new StackTraceElement(msg, "NoMethod", msg, 0);

        Throwable e = new ConstructedException(msg, stackTrace);
        FirebaseCrashlytics.getInstance().recordException(e);

        uploadUnsentReports();
    }

    public static void uploadUnsentReports() {
        if (!BuildConfig.DEBUG) {
            FirebaseCrashlytics.getInstance().sendUnsentReports();
        }
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
            String user = me.getUser();
            FirebaseCrashlytics.getInstance().setCustomKey("user", user);
            FirebaseCrashlytics.getInstance().setUserId(user);
            return null;
        }
    }

    // Cannot hard-code; proguard minification will change the names
    private static final List<String> classesToIgnore = Arrays.asList(
            Preconditions.class.getName()
    );

    public static void wrapCrashlytics() {
        Thread.UncaughtExceptionHandler original = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            // Remove irrelevant stack elements so that Crashlytics grouping works
            StackTraceElement[] stackTrace = throwable.getStackTrace();
            int i = 0;
            while (stackTrace.length > i && classesToIgnore.contains(stackTrace[i].getClassName())) {
                i++;
            }
            stackTrace = Arrays.copyOfRange(stackTrace, i, stackTrace.length);
            throwable.setStackTrace(stackTrace);

            if (original != null) {
                original.uncaughtException(thread, throwable);
            } else {
                e("No other handler to delegate", throwable);
                sendErrorReport("Crashlytics handler missing");
            }
        });
    }
}

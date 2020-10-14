package com.halloapp.util.logs;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.halloapp.FileStore;

public class ProductionLogger extends FileLogger {

    private static final int MIN_LOG_PRIORITY = Log.DEBUG;

    public ProductionLogger(@NonNull FileStore fileStore) {
        super(fileStore);
    }

    @Override
    public void log(int priority, @NonNull String message, @Nullable Throwable t) {
        String firebaseMsg = Thread.currentThread().getName() +
                '/' +
                convertPriorityToChar(priority) +
                '/' +
                "halloapp: " +
                message;
        if (t != null) {
            firebaseMsg += "\n" + android.util.Log.getStackTraceString(t);
        }
        FirebaseCrashlytics.getInstance().log(firebaseMsg);

        if (priority < MIN_LOG_PRIORITY) {
            return;
        }
        super.log(priority, message, t);
    }
}

package com.halloapp.util;

import android.net.TrafficStats;
import android.os.StrictMode;

import androidx.annotation.NonNull;

public class ThreadUtils {

    /**
     * Some phones trigger Thread Policy Violations with their platform code, so we
     * get strict mode crashes without any connection to our code (looking at you,
     * Samsung). This temporarily turns off Strict Mode to avoid this issue.
     */
    public static void runWithoutStrictModeRestrictions(Runnable runnable) {
        StrictMode.ThreadPolicy original = StrictMode.getThreadPolicy();
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX);
        runnable.run();
        StrictMode.setThreadPolicy(original);
    }

    public static void setSocketTag() {
        TrafficStats.setThreadStatsTag((int) Thread.currentThread().getId());
    }

    @NonNull
    public static Thread go(Runnable runnable, String threadName) {
        Thread thread = new Thread(runnable);
        thread.setName(threadName);
        thread.setDaemon(true);
        thread.start();
        return thread;
    }
}

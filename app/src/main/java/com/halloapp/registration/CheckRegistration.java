package com.halloapp.registration;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.halloapp.Me;
import com.halloapp.Preferences;

public class CheckRegistration {

    @WorkerThread
    @NonNull
    public static CheckResult checkRegistration(@NonNull Me me, @NonNull Preferences preferences) {
        return new CheckResult(me.isRegistered(), preferences.getLastFullContactSyncTime());
    }

    public static class CheckResult {
        public final boolean registered;
        public final long lastSyncTime;

        CheckResult(boolean registered, long lastSyncTime) {
            this.registered = registered;
            this.lastSyncTime = lastSyncTime;
        }
    }
}

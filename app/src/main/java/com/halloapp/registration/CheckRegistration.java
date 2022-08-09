package com.halloapp.registration;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.halloapp.Me;
import com.halloapp.Preferences;

public class CheckRegistration {

    @WorkerThread
    @NonNull
    public static CheckResult checkRegistration(@NonNull Me me, @NonNull Preferences preferences) {
        return new CheckResult(me.isRegistered(), preferences.getProfileSetup(), preferences.getLastFullContactSyncTime());
    }

    public static class CheckResult {
        public final boolean registered;
        public final long lastSyncTime;
        public final boolean profileSetup;

        CheckResult(boolean registered, boolean profileSetup, long lastSyncTime) {
            this.registered = registered;
            this.profileSetup = profileSetup;
            this.lastSyncTime = lastSyncTime;
        }
    }
}

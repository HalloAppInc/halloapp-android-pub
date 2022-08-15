package com.halloapp.registration;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.halloapp.Me;
import com.halloapp.Preferences;

public class CheckRegistration {

    @WorkerThread
    @NonNull
    public static CheckResult checkRegistration(@NonNull Me me, @NonNull Preferences preferences) {
        return new CheckResult(me.isRegistered(), preferences.getProfileSetup(), preferences.getLastFullContactSyncTime(), preferences.getCompletedFirstPostOnboarding());
    }

    public static class CheckResult {
        public final boolean registered;
        public final long lastSyncTime;
        public final boolean profileSetup;
        public final boolean completedFirstPostOnboarding;

        CheckResult(boolean registered, boolean profileSetup, long lastSyncTime, boolean completedFirstPostOnboarding) {
            this.registered = registered;
            this.profileSetup = profileSetup;
            this.lastSyncTime = lastSyncTime;
            this.completedFirstPostOnboarding = completedFirstPostOnboarding;
        }
    }
}

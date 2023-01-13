package com.halloapp.registration;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.halloapp.BuildConfig;
import com.halloapp.Me;
import com.halloapp.Preferences;

public class CheckRegistration {

    @WorkerThread
    @NonNull
    public static CheckResult checkRegistration(@NonNull Me me, @NonNull Preferences preferences) {
        return new CheckResult(me.isRegistered(), preferences.getProfileSetup(), preferences.getLastFullContactSyncTime(), preferences.getCompletedFirstPostOnboarding(), me.getName(), me.getUsername());
    }

    public static class CheckResult {
        public final boolean registered;
        public final long lastSyncTime;
        public final boolean profileSetup;
        public final boolean completedFirstPostOnboarding;
        public final String name;
        public final String username;

        CheckResult(boolean registered, boolean profileSetup, long lastSyncTime, boolean completedFirstPostOnboarding, String name, String username) {
            this.registered = registered;
            this.profileSetup = profileSetup;
            this.lastSyncTime = lastSyncTime;
            this.completedFirstPostOnboarding = completedFirstPostOnboarding;
            this.name = name;
            this.username = username;
        }
    }
}

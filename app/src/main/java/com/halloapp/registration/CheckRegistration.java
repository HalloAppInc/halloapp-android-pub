package com.halloapp.registration;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.halloapp.Me;
import com.halloapp.Preferences;

public class CheckRegistration {

    @WorkerThread
    @NonNull
    public static CheckResult checkRegistration(@NonNull Me me, @NonNull Preferences preferences) {
        return new CheckResult(
                me.isRegistered(),
                preferences.getProfileSetup(),
                preferences.getLastFullContactSyncTime(),
                preferences.getCompletedFirstPostOnboarding(),
                preferences.getContactsPermissionRequested() && preferences.getLocationPermissionRequested(),
                preferences.getOnboardingFollowingSetup(),
                preferences.getOnboardingGetStartedShown(),
                me.getName(),
                me.getUsername());
    }

    public static class CheckResult {
        public final boolean registered;
        public final long lastSyncTime;
        public final boolean profileSetup;
        public final boolean completedFirstPostOnboarding;
        public final boolean onboardingPermissionsSetup;
        public final boolean onboardingFollowingSetup;
        public final boolean onboardingGetStartedShown;
        public final String name;
        public final String username;

        CheckResult(boolean registered, boolean profileSetup, long lastSyncTime, boolean completedFirstPostOnboarding, boolean onboardingPermissionsSetup, boolean onboardingFollowingSetup, boolean onboardingGetStartedShown, String name, String username) {
            this.registered = registered;
            this.profileSetup = profileSetup;
            this.lastSyncTime = lastSyncTime;
            this.completedFirstPostOnboarding = completedFirstPostOnboarding;
            this.onboardingPermissionsSetup = onboardingPermissionsSetup;
            this.onboardingFollowingSetup = onboardingFollowingSetup;
            this.onboardingGetStartedShown = onboardingGetStartedShown;
            this.name = name;
            this.username = username;
        }
    }
}

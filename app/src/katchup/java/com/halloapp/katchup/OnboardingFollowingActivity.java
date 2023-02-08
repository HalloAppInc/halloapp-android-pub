package com.halloapp.katchup;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;

import com.halloapp.MainActivity;
import com.halloapp.Preferences;
import com.halloapp.R;
import com.halloapp.contacts.ContactsSync;
import com.halloapp.ui.HalloActivity;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.logs.Log;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class OnboardingFollowingActivity extends HalloActivity implements FollowingFragment.NextScreenHandler, EasyPermissions.PermissionCallbacks {
    private FollowingFragment followingFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding_following);

        Analytics.getInstance().openScreen("onboardingFollow");

        final FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        followingFragment = FollowingFragment.newInstance(true);
        fragmentTransaction.replace(R.id.fragment_placeholder, followingFragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Log.d("OnboardingFollowingActivity.onPermissionsGranted " + requestCode + " " + perms);
        if (requestCode == MainActivity.REQUEST_CODE_ASK_CONTACTS_PERMISSION) {
            Preferences.getInstance().clearContactSyncBackoffTime();
            ContactsSync.getInstance().forceFullContactsSync(true);
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Log.d("OnboardingFollowingActivity.onPermissionsDenied " + requestCode + " " + perms);
    }

    @Override
    public void nextScreen() {
        BgWorkers.getInstance().execute(() -> {
            Analytics.getInstance().onboardingFollowScreen(followingFragment.getNumFollowedDuringOnboarding());
            Preferences.getInstance().setOnboardingFollowingSetup(true);
            runOnUiThread(() -> {
                startActivity(new Intent(OnboardingFollowingActivity.this, GetStartedActivity.class));
                finish();
            });
        });
    }
}

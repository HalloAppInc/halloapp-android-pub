package com.halloapp.katchup;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;

import com.halloapp.MainActivity;
import com.halloapp.Preferences;
import com.halloapp.R;
import com.halloapp.ui.HalloActivity;
import com.halloapp.util.BgWorkers;

public class OnboardingFollowingActivity extends HalloActivity implements FollowingFragment.NextScreenHandler {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding_following);

        Analytics.getInstance().openScreen("onboardingFollow");

        final FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_placeholder, FollowingFragment.newInstance(true));
        fragmentTransaction.commit();
    }

    @Override
    public void nextScreen() {
        BgWorkers.getInstance().execute(() -> {
            Preferences.getInstance().setOnboardingFollowingSetup(true);
            runOnUiThread(() -> {
                startActivity(new Intent(OnboardingFollowingActivity.this, GetStartedActivity.class));
                finish();
            });
        });
    }
}

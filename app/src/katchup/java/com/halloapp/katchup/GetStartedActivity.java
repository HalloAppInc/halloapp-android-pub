package com.halloapp.katchup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.halloapp.MainActivity;
import com.halloapp.Preferences;
import com.halloapp.R;
import com.halloapp.ui.HalloActivity;
import com.halloapp.util.BgWorkers;

public class GetStartedActivity extends HalloActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_started);

        Analytics.getInstance().openScreen("onboardingComplete");

        final View getStarted = findViewById(R.id.get_started);
        getStarted.setOnClickListener(v -> getStarted());
    }

    public void getStarted() {
        BgWorkers.getInstance().execute(() -> {
            Preferences.getInstance().setOnboardingGetStartedShown(true);
            Analytics.getInstance().logOnboardingFinish();
            runOnUiThread(() -> {
                startActivity(new Intent(GetStartedActivity.this, MainActivity.class));
                finish();
            });
        });
    }
}

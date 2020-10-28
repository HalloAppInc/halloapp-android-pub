package com.halloapp.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.halloapp.BuildConfig;
import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.util.logs.Log;
import com.halloapp.util.logs.LogProvider;

public class HelpActivity extends HalloActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_help);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView versionTv = findViewById(R.id.app_version);
        versionTv.setText(getString(R.string.settings_version_footer, BuildConfig.VERSION_NAME));

        View termsPrivacy = findViewById(R.id.terms_privacy);
        termsPrivacy.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.MAIN_WEBSITE_URL));
            startActivity(intent);
        });

        View sendLogs = findViewById(R.id.send_logs);
        sendLogs.setOnClickListener(v -> {
            Log.sendErrorReport("User sent logs");

            if (BuildConfig.DEBUG) {
                LogProvider.openDebugLogcatIntent(this);
            } else {
                LogProvider.openEmailLogIntent(this);
            }
        });
    }
}

package com.halloapp.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.halloapp.BuildConfig;
import com.halloapp.Constants;
import com.halloapp.Debug;
import com.halloapp.DebugActivity;
import com.halloapp.DebugStorageActivity;
import com.halloapp.Preferences;
import com.halloapp.R;
import com.halloapp.props.ServerProps;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;
import com.halloapp.util.logs.LogProvider;
import com.halloapp.xmpp.Connection;

public class HelpActivity extends HalloActivity {

    private final BgWorkers bgWorkers = BgWorkers.getInstance();
    private final Connection connection = Connection.getInstance();
    private final Preferences preferences = Preferences.getInstance();
    private final ServerProps serverProps = ServerProps.getInstance();

    private SwitchCompat useDebugHostSwitch;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_help);

        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        TextView versionTv = findViewById(R.id.app_version);
        versionTv.setText(getString(R.string.settings_version_footer, BuildConfig.VERSION_NAME));

        View faqView = findViewById(R.id.faq);
        faqView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.FAQ_URL));
            startActivity(intent);
        });

        View termsOfServiceView = findViewById(R.id.terms_of_service);
        termsOfServiceView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.TERMS_OF_SERVICE_URL));
            startActivity(intent);
        });

        View privacyPolicyView = findViewById(R.id.privacy_policy);
        privacyPolicyView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.PRIVACY_POLICY_URL));
            startActivity(intent);
        });

        View sendLogs = findViewById(R.id.send_logs);
        sendLogs.setOnClickListener(v -> {
            Log.sendErrorReport("User sent logs");

            if (BuildConfig.DEBUG) {
                LogProvider.openDebugLogcatIntent(this, null);
            } else {
                LogProvider.openEmailLogIntent(this, null);
            }
        });

        useDebugHostSwitch = findViewById(R.id.use_debug_host_switch);
        if (serverProps.getIsInternalUser()) {
            View debugOptions = findViewById(R.id.debug_options);
            debugOptions.setVisibility(View.VISIBLE);

            View debugMenu = findViewById(R.id.debug_menu);
            debugMenu.setOnClickListener(v -> {
                Debug.showDebugMenu(this, debugMenu);
            });

            View debugStorage = findViewById(R.id.debug_storage);
            debugStorage.setOnClickListener(v -> {
                startActivity(new Intent(this, DebugStorageActivity.class));
            });

            View debugConfig = findViewById(R.id.debug_config);
            debugConfig.setOnClickListener(v -> {
                startActivity(new Intent(this, DebugActivity.class));
            });

            View useDebugHost = findViewById(R.id.use_debug_host);
            useDebugHost.setOnClickListener(v -> {
                boolean use = !useDebugHostSwitch.isChecked();
                useDebugHostSwitch.setChecked(use);
            });
            useDebugHostSwitch.setOnCheckedChangeListener((v, checked) -> {
                bgWorkers.execute(() -> {
                    preferences.setUseDebugHost(checked);
                    connection.disconnect();
                    connection.connect();
                });
            });

            bgWorkers.execute(() -> {
                boolean use = preferences.getUseDebugHost();
                useDebugHostSwitch.post(() -> {
                    useDebugHostSwitch.setChecked(use);
                });
            });
        }
    }
}

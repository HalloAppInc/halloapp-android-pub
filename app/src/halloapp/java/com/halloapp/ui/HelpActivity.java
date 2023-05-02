package com.halloapp.ui;

import android.app.ProgressDialog;
import android.content.Intent;
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
import com.halloapp.ui.camera.CameraActivity;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.IntentUtils;
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
        faqView.setOnClickListener(v -> IntentUtils.openOurWebsiteInBrowser(faqView, Constants.FAQ_SUFFIX));

        View termsOfServiceView = findViewById(R.id.terms_of_service);
        termsOfServiceView.setOnClickListener(v -> IntentUtils.openOurWebsiteInBrowser(termsOfServiceView, Constants.TERMS_OF_SERVICE_SUFFIX));

        View privacyPolicyView = findViewById(R.id.privacy_policy);
        privacyPolicyView.setOnClickListener(v -> IntentUtils.openOurWebsiteInBrowser(privacyPolicyView, Constants.PRIVACY_POLICY_SUFFIX));

        View sendLogs = findViewById(R.id.send_logs);
        sendLogs.setOnClickListener(v -> {
            Log.sendErrorReport("User sent logs");

            ProgressDialog progressDialog = ProgressDialog.show(this, null, getString(R.string.preparing_logs));
            LogProvider.openLogIntent(this).observe(this, intent -> {
                startActivity(intent);
                progressDialog.dismiss();
            });
        });

        useDebugHostSwitch = findViewById(R.id.use_debug_host_switch);
        if (serverProps.getIsInternalUser() || BuildConfig.DEBUG) {
            View debugOptions = findViewById(R.id.debug_options);
            debugOptions.setVisibility(View.VISIBLE);

            View debugMenu = findViewById(R.id.debug_menu);
            debugMenu.setOnClickListener(v -> {
                Debug.showMainDebugMenu(this, debugMenu);
            });

            View debugStorage = findViewById(R.id.debug_storage);
            debugStorage.setOnClickListener(v -> {
                startActivity(new Intent(this, DebugStorageActivity.class));
            });

            View debugConfig = findViewById(R.id.debug_config);
            debugConfig.setOnClickListener(v -> {
                startActivity(new Intent(this, DebugActivity.class));
            });

            View psaMoment = findViewById(R.id.psa_moment);
            psaMoment.setOnClickListener(v -> {
                Intent i = new Intent(v.getContext(), CameraActivity.class);
                i.putExtra(CameraActivity.EXTRA_PURPOSE, CameraActivity.PURPOSE_MOMENT_PSA);
                startActivity(i);
            });

            View linkAccounts = findViewById(R.id.link_accounts);
            linkAccounts.setVisibility(ServerProps.getInstance().getIsInternalUser() ? View.VISIBLE : View.GONE);
            linkAccounts.setOnClickListener(v -> {
                startActivity(new Intent(v.getContext(), LinkAccountActivity.class));
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

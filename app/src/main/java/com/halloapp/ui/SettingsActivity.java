package com.halloapp.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.preference.Preference;

import com.halloapp.BuildConfig;
import com.halloapp.Debug;
import com.halloapp.Me;
import com.halloapp.Preferences;
import com.halloapp.R;
import com.halloapp.ui.privacy.BlockListActivity;
import com.halloapp.ui.privacy.FeedPrivacyActivity;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.widget.CenterToast;
import com.halloapp.xmpp.Connection;

public class SettingsActivity extends HalloActivity {
    private static final String SUPPORT_EMAIL_URI = "mailto:android-support@halloapp.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("SettingsActivity.onCreate");
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setElevation(getResources().getDimension(R.dimen.action_bar_elevation));
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("SettingsActivity.onDestroy");
    }

    public static class SettingsFragment extends HalloPreferenceFragment {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

            getPreferenceManager().setSharedPreferencesName(Preferences.PREFS_NAME);

            final StrictMode.ThreadPolicy threadPolicy = StrictMode.allowThreadDiskReads();
            try {
                setPreferencesFromResource(R.xml.root_preferences, rootKey);
            } finally {
                StrictMode.setThreadPolicy(threadPolicy);
            }

            final Preference blocklistPreference = Preconditions.checkNotNull((findPreference("block_list")));
            blocklistPreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(requireContext(), BlockListActivity.class);
                startActivity(intent);
                return false;
            });

            final Preference feedPrivacyPreference = Preconditions.checkNotNull((findPreference("feed_privacy")));
            feedPrivacyPreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(requireContext(), FeedPrivacyActivity.class);
                startActivity(intent);
                return false;
            });


            final Preference sendLogsPreference = Preconditions.checkNotNull((findPreference("send_logs")));
            sendLogsPreference.setVisible(!BuildConfig.DEBUG);
            sendLogsPreference.setOnPreferenceClickListener(preference -> {
                Log.sendErrorReport(getString(R.string.send_logs));
                CenterToast.show(requireContext(), R.string.send_logs);
                requireActivity().finish();

                String uri = SUPPORT_EMAIL_URI
                        + "?subject=" + Uri.encode(getString(R.string.email_logs_subject))
                        + "&body=" + Uri.encode(getString(R.string.email_logs_text, Me.getInstance(requireContext()).getUser()));
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse(uri));
                startActivity(intent);
                return false;
            });

            final Preference hostPreference = Preconditions.checkNotNull(findPreference("use_debug_host"));
            hostPreference.setVisible(BuildConfig.DEBUG);
            hostPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Connection connection = Connection.getInstance();
                    connection.disconnect();
                    connection.connect(requireContext());
                    return true;
                }
            });

            final Preference debugPreference = Preconditions.checkNotNull(findPreference("debug"));
            debugPreference.setVisible(BuildConfig.DEBUG);
            debugPreference.setOnPreferenceClickListener(preference -> {
                View prefView = getListView().getChildAt(preference.getOrder());
                Debug.showDebugMenu(requireActivity(), prefView);
                return false;
            });
        }
    }
}

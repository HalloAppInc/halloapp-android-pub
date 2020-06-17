package com.halloapp.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.halloapp.BuildConfig;
import com.halloapp.Debug;
import com.halloapp.Me;
import com.halloapp.Preferences;
import com.halloapp.R;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.widget.CenterToast;
import com.halloapp.xmpp.Connection;

public class SettingsActivity extends AppCompatActivity {
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

            getPreferenceManager().setSharedPreferencesName(Preferences.PREFS_NAME);

            final StrictMode.ThreadPolicy threadPolicy = StrictMode.allowThreadDiskReads();
            try {
                setPreferencesFromResource(R.xml.root_preferences, rootKey);
            } finally {
                StrictMode.setThreadPolicy(threadPolicy);
            }

            Preconditions.checkNotNull((Preference)findPreference("send_logs")).setOnPreferenceClickListener(preference -> {
                Log.sendErrorReport(getString(R.string.send_logs));
                CenterToast.show(requireContext(), R.string.send_logs);
                requireActivity().finish();
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse(SUPPORT_EMAIL_URI));
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_logs_subject));
                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.email_logs_text, Me.getInstance(requireContext()).getUser()));
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
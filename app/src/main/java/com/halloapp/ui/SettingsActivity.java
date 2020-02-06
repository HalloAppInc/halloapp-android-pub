package com.halloapp.ui;

import android.os.Bundle;
import android.os.StrictMode;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Preconditions;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.halloapp.Preferences;
import com.halloapp.R;
import com.halloapp.util.Log;
import com.halloapp.widget.CenterToast;

public class SettingsActivity extends AppCompatActivity {

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
                CenterToast.show(Preconditions.checkNotNull(getContext()), R.string.send_logs);
                Preconditions.checkNotNull(getActivity()).finish();
                return false;
            });

        }
    }
}
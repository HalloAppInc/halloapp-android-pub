package com.halloapp.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;

import com.halloapp.BuildConfig;
import com.halloapp.Debug;
import com.halloapp.DebugActivity;
import com.halloapp.Preferences;
import com.halloapp.R;
import com.halloapp.props.ServerProps;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.HalloPreferenceFragment;
import com.halloapp.ui.privacy.BlockListActivity;
import com.halloapp.ui.privacy.FeedPrivacyActivity;
import com.halloapp.util.Preconditions;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.privacy.PrivacyList;

public class SettingsPrivacy extends HalloActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends HalloPreferenceFragment {

        private final ServerProps serverProps = ServerProps.getInstance();

        private Preference blocklistPreference;
        private Preference feedPrivacyPreference;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

            getPreferenceManager().setSharedPreferencesName(Preferences.PREFS_NAME);

            final StrictMode.ThreadPolicy threadPolicy = StrictMode.allowThreadDiskReads();
            try {
                setPreferencesFromResource(R.xml.settings_privacy, rootKey);
            } finally {
                StrictMode.setThreadPolicy(threadPolicy);
            }

            blocklistPreference = Preconditions.checkNotNull((findPreference("block_list")));
            blocklistPreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(requireContext(), BlockListActivity.class);
                startActivity(intent);
                return false;
            });

            feedPrivacyPreference = Preconditions.checkNotNull((findPreference("feed_privacy")));
            feedPrivacyPreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(requireContext(), FeedPrivacyActivity.class);
                startActivity(intent);
                return false;
            });

            final PreferenceCategory debugCategory = Preconditions.checkNotNull(findPreference("debug"));
            debugCategory.setVisible(serverProps.getIsInternalUser());

            final Preference hostPreference = Preconditions.checkNotNull(findPreference("use_debug_host"));
            hostPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Connection connection = Connection.getInstance();
                    connection.disconnect();
                    connection.connect();
                    return true;
                }
            });

            final Preference debugPreference = Preconditions.checkNotNull(findPreference("debug_menu"));
            debugPreference.setOnPreferenceClickListener(preference -> {
                View prefView = getListView().getChildAt(preference.getOrder());
                Debug.showDebugMenu(requireActivity(), prefView);
                return false;
            });

            final Preference debugConfigPreference = Preconditions.checkNotNull(findPreference("debug_config"));
            debugConfigPreference.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(getContext(), DebugActivity.class));
                return false;
            });
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setDividerHeight(getResources().getDimensionPixelSize(R.dimen.settings_divider_height));
            SettingsViewModel settingsViewModel = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);
            settingsViewModel.getBlockList().observe(getViewLifecycleOwner(), list -> {
                if (list == null) {
                    blocklistPreference.setSummary(" ");
                } else if (list.isEmpty()){
                    blocklistPreference.setSummary(getString(R.string.settings_block_list_none_summary));
                } else {
                    blocklistPreference.setSummary(getResources().getQuantityString(R.plurals.settings_block_list_summary, list.size(), list.size()));
                }
            });
            settingsViewModel.getFeedPrivacy().observe(getViewLifecycleOwner(), feedPrivacy -> {
                if (feedPrivacy == null) {
                    feedPrivacyPreference.setSummary(" ");
                } else if (PrivacyList.Type.ALL.equals(feedPrivacy.activeList)) {
                    feedPrivacyPreference.setSummary(getString(R.string.settings_feed_contacts_summary));
                } else if (PrivacyList.Type.EXCEPT.equals(feedPrivacy.activeList)) {
                    if (feedPrivacy.exceptList.isEmpty()) {
                        feedPrivacyPreference.setSummary(getString(R.string.settings_feed_contacts_summary));
                    } else {
                        feedPrivacyPreference.setSummary(getResources().getQuantityString(R.plurals.settings_feed_excluded_summary, feedPrivacy.exceptList.size(), feedPrivacy.exceptList.size()));
                    }
                } else if (PrivacyList.Type.ONLY.equals(feedPrivacy.activeList)) {
                    if (feedPrivacy.onlyList.isEmpty()) {
                        feedPrivacyPreference.setSummary(getString(R.string.settings_feed_selected_empty));
                    } else {
                        feedPrivacyPreference.setSummary(getResources().getQuantityString(R.plurals.settings_feed_selected, feedPrivacy.onlyList.size(), feedPrivacy.onlyList.size()));
                    }
                } else {
                    feedPrivacyPreference.setSummary(" ");
                }
            });
        }
    }
}

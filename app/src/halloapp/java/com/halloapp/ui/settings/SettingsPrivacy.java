package com.halloapp.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;

import com.halloapp.Constants;
import com.halloapp.Preferences;
import com.halloapp.R;
import com.halloapp.props.ServerProps;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.HalloPreferenceFragment;
import com.halloapp.ui.privacy.BlockListActivity;
import com.halloapp.ui.privacy.FeedPrivacyActivity;
import com.halloapp.util.IntentUtils;
import com.halloapp.util.Preconditions;

public class SettingsPrivacy extends HalloActivity {

    private static final String EXTRA_TARGET_SETTING = "target_setting";

    private static final String TARGET_SETTING_FEED_PRIVACY = "feed_privacy";

    public static Intent openFeedPrivacy(@NonNull Context context) {
        Intent i = new Intent(context, SettingsPrivacy.class);
        i.putExtra(EXTRA_TARGET_SETTING, TARGET_SETTING_FEED_PRIVACY);
        return i;
    }

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

        String targetSetting = getIntent().getStringExtra(EXTRA_TARGET_SETTING);
        if (TARGET_SETTING_FEED_PRIVACY.equals(targetSetting)) {
            Intent intent = new Intent(this, FeedPrivacyActivity.class);
            startActivity(intent);
        }
    }

    public static class SettingsFragment extends HalloPreferenceFragment {

        private final ServerProps serverProps = ServerProps.getInstance();

        private Preference blocklistPreference;
        private Preference feedPrivacyPreference;
        private Preference privacyPolicyPreference;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

            getPreferenceManager().setSharedPreferencesName(Preferences.DEVICE_LOCAL_PREFS_NAME);

            final StrictMode.ThreadPolicy threadPolicy = StrictMode.allowThreadDiskReads();
            try {
                setPreferencesFromResource(R.xml.settings_privacy, rootKey);
            } finally {
                StrictMode.setThreadPolicy(threadPolicy);
            }

            blocklistPreference = Preconditions.checkNotNull(findPreference("block_list"));
            blocklistPreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(requireContext(), BlockListActivity.class);
                startActivity(intent);
                return false;
            });

            feedPrivacyPreference = Preconditions.checkNotNull(findPreference("feed_privacy"));
            feedPrivacyPreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(requireContext(), FeedPrivacyActivity.class);
                startActivity(intent);
                return false;
            });
            feedPrivacyPreference.setVisible(false);

            privacyPolicyPreference = Preconditions.checkNotNull(findPreference("privacy_policy"));
            privacyPolicyPreference.setOnPreferenceClickListener(preference -> {
                IntentUtils.openOurWebsiteInBrowser(requireActivity(), Constants.PRIVACY_POLICY_SUFFIX);
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
        }
    }
}

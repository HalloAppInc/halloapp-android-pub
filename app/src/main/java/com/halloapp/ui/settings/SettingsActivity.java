package com.halloapp.ui.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;

import com.halloapp.BuildConfig;
import com.halloapp.Debug;
import com.halloapp.LogProvider;
import com.halloapp.Me;
import com.halloapp.Preferences;
import com.halloapp.R;
import com.halloapp.id.UserId;
import com.halloapp.props.ServerProps;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.HalloPreferenceFragment;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.invites.InviteFriendsActivity;
import com.halloapp.ui.privacy.BlockListActivity;
import com.halloapp.ui.privacy.FeedPrivacyActivity;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.util.StringUtils;
import com.halloapp.widget.CenterToast;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.privacy.PrivacyList;

public class SettingsActivity extends HalloActivity {
    public static final String SUPPORT_EMAIL = "android-support@halloapp.com";
    private static final String SUPPORT_EMAIL_URI = "mailto:" + SUPPORT_EMAIL;
    private static final String MAIN_WEBSITE_URL = "https://www.halloapp.com/";

    private SettingsViewModel settingsViewModel;

    private AvatarLoader avatarLoader;

    private ImageView avatarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("SettingsActivity.onCreate");
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        avatarLoader = AvatarLoader.getInstance(this);

        settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        avatarView = findViewById(R.id.avatar);
        final TextView nameView = findViewById(R.id.name);
        final TextView phoneView = findViewById(R.id.phone);

        final View profileContainer = findViewById(R.id.profile_container);

        settingsViewModel.getName().observe(this, nameView::setText);
        settingsViewModel.getPhone().observe(this, phoneNumber -> {
            phoneView.setText(StringUtils.formatPhoneNumber(phoneNumber));
        });

        avatarLoader.load(avatarView, UserId.ME);

        profileContainer.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsProfile.class));
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("SettingsActivity.onDestroy");
    }

    @Override
    protected void onResume() {
        super.onResume();
        settingsViewModel.refresh();
        avatarLoader.load(avatarView, UserId.ME);
    }

    public static class SettingsFragment extends HalloPreferenceFragment {

        private ServerProps serverProps = ServerProps.getInstance();

        private Preference blocklistPreference;
        private Preference inviteFriends;
        private Preference feedPrivacyPreference;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

            getPreferenceManager().setSharedPreferencesName(Preferences.PREFS_NAME);

            final StrictMode.ThreadPolicy threadPolicy = StrictMode.allowThreadDiskReads();
            try {
                setPreferencesFromResource(R.xml.root_preferences, rootKey);
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

            inviteFriends = Preconditions.checkNotNull((findPreference("invite_friends")));
            inviteFriends.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(requireContext(), InviteFriendsActivity.class);
                startActivity(intent);
                return false;
            });

            final Preference sendLogsPreference = Preconditions.checkNotNull((findPreference("send_logs")));
            sendLogsPreference.setOnPreferenceClickListener(preference -> {
                Log.sendErrorReport("User sent logs");
                CenterToast.show(requireContext(), R.string.send_logs);
                requireActivity().finish();

                if (BuildConfig.DEBUG) {
                    LogProvider.openLogIntent(requireContext());
                } else {
                    String uri = SUPPORT_EMAIL_URI
                            + "?subject=" + Uri.encode(getString(R.string.email_logs_subject, BuildConfig.VERSION_NAME))
                            + "&body=" + Uri.encode(getString(R.string.email_logs_text, Me.getInstance().getUser(), BuildConfig.VERSION_NAME));
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse(uri));
                    startActivity(intent);
                }

                return false;
            });

            final Preference termsOfServiceAndPolicyPreference = Preconditions.checkNotNull(findPreference("terms_and_policy"));
            termsOfServiceAndPolicyPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(MAIN_WEBSITE_URL));
                    startActivity(intent);
                    return false;
                }
            });

            final PreferenceCategory debugCategory = Preconditions.checkNotNull(findPreference("debug"));
            debugCategory.setVisible(BuildConfig.DEBUG || serverProps.getIsInternalUser());

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
            settingsViewModel.getInviteCount().observe(getViewLifecycleOwner(), count -> {
                if (count == null || count == -1) {
                    inviteFriends.setSummary(" ");
                } else if (count == 0){
                    inviteFriends.setSummary(getString(R.string.settings_invite_none_summary));
                } else {
                    inviteFriends.setSummary(getResources().getQuantityString(R.plurals.settings_invite_summary, count, count));
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

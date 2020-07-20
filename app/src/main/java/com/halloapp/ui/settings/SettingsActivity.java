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
import com.halloapp.contacts.UserId;
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
        avatarLoader.load(avatarView, UserId.ME);
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

            final Preference inviteFriends = Preconditions.checkNotNull((findPreference("invite_friends")));
            inviteFriends.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(requireContext(), InviteFriendsActivity.class);
                startActivity(intent);
                return false;
            });

            final Preference sendLogsPreference = Preconditions.checkNotNull((findPreference("send_logs")));
            sendLogsPreference.setOnPreferenceClickListener(preference -> {
                Log.sendErrorReport(getString(R.string.send_logs));
                CenterToast.show(requireContext(), R.string.send_logs);
                requireActivity().finish();

                if (BuildConfig.DEBUG) {
                    LogProvider.openLogIntent(requireContext());
                } else {
                    String uri = SUPPORT_EMAIL_URI
                            + "?subject=" + Uri.encode(getString(R.string.email_logs_subject))
                            + "&body=" + Uri.encode(getString(R.string.email_logs_text, Me.getInstance(requireContext()).getUser()));
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
            debugCategory.setVisible(BuildConfig.DEBUG);

            final Preference hostPreference = Preconditions.checkNotNull(findPreference("use_debug_host"));
            hostPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Connection connection = Connection.getInstance();
                    connection.disconnect();
                    connection.connect(requireContext());
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
        }
    }
}

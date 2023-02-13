package com.halloapp.katchup;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;

import com.halloapp.AppContext;
import com.halloapp.BuildConfig;
import com.halloapp.Constants;
import com.halloapp.FileStore;
import com.halloapp.Me;
import com.halloapp.Preferences;
import com.halloapp.R;
import com.halloapp.contacts.ContactsSync;
import com.halloapp.props.ServerProps;
import com.halloapp.proto.server.MomentNotification;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.HalloPreferenceFragment;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.IntentUtils;
import com.halloapp.util.Preconditions;

public class SettingsActivity extends HalloActivity {

    public static Intent open(@NonNull Context context) {
        return new Intent(context, SettingsActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_katchup);

        TextView titleView = findViewById(R.id.title);

        View prev = findViewById(R.id.prev);
        prev.setOnClickListener(v -> {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            } else {
                finish();
            }
        });

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();

        getSupportFragmentManager().addOnBackStackChangedListener(() ->
            titleView.setText(getTitle(getSupportFragmentManager().findFragmentById(R.id.settings)))
        );
    }

    private String getTitle(Fragment fragment) {
        if (fragment instanceof SettingsFragment) {
            return getString(R.string.settings_title);
        } else if (fragment instanceof AccountFragment) {
            return getString(R.string.settings_account_title);
        } else if (fragment instanceof SettingsAccountExportFragment) {
            return getString(R.string.account_export_title);
        } else if (fragment instanceof HelpFragment) {
            return getString(R.string.settings_help_title);
        } else if (fragment instanceof PrivacyFragment) {
            return getString(R.string.settings_privacy_title);
        } else if (fragment instanceof SettingsBlockedFragment) {
            return getString(R.string.settings_privacy_blocked_option);
        } else if (fragment instanceof SettingsAccountDeleteFragment) {
            return getString(R.string.settings_delete_account_title);
        } else if (fragment instanceof SettingsFeedbackFragment) {
            return getString(R.string.settings_feedback_title);
        } else if (fragment instanceof NotificationsFragment) {
            return getString(R.string.settings_notifications_title);
        } else if (fragment instanceof SettingsStorageFragment) {
            return getString(R.string.settings_storage_title);
        } else if (fragment instanceof DeveloperFragment) {
            return getString(R.string.settings_developer_option);
        }

        return "";
    }

    public static abstract class BaseSettingsFragment extends HalloPreferenceFragment {
        public void setOnPreferenceClickFragment(@NonNull String key, @NonNull Fragment fragment) {
            getPreference(key).setOnPreferenceClickListener(preference -> {
                addFragment(fragment);
                return true;
            });
        }

        public void addFragment(@NonNull Fragment fragment) {
            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    .replace(R.id.settings, fragment)
                    .addToBackStack(null)
                    .commit();
        }

        @NonNull
        public Preference getPreference(@NonNull String key) {
            return Preconditions.checkNotNull(findPreference(key));
        }
    }

    public static class SettingsFragment extends BaseSettingsFragment {
        private static final String PREF_KEY_ACCOUNT = "account";
        private static final String PREF_KEY_NOTIFICATIONS = "notifications";
        private static final String PREF_KEY_PRIVACY = "privacy";
        private static final String PREF_KEY_HELP = "help";
        private static final String PREF_KEY_FEEDBACK = "feedback";
        private static final String PREF_KEY_SHARE = "share";
        private static final String PREF_KEY_DEVELOPER = "developer";
        private static final String PREF_KEY_DEVELOPER_TOOLS = "developer_tools";


        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.settings, rootKey);

            Analytics.getInstance().openScreen("settings");

            setOnPreferenceClickFragment(PREF_KEY_ACCOUNT, new AccountFragment());
            setOnPreferenceClickFragment(PREF_KEY_NOTIFICATIONS, new NotificationsFragment());
            setOnPreferenceClickFragment(PREF_KEY_PRIVACY, new PrivacyFragment());
            setOnPreferenceClickFragment(PREF_KEY_HELP, new HelpFragment());
            setOnPreferenceClickFragment(PREF_KEY_FEEDBACK, new SettingsFeedbackFragment());

            getPreference(PREF_KEY_SHARE).setOnPreferenceClickListener(preference -> {
                Intent intent = IntentUtils.createShareTextIntent("https://katchup.com/" + Me.getInstance().getUsername());
                startActivity(intent);
                return true;
            });

            getPreference(PREF_KEY_DEVELOPER).setVisible(ServerProps.getInstance().getIsInternalUser() || BuildConfig.DEBUG);
            setOnPreferenceClickFragment(PREF_KEY_DEVELOPER_TOOLS, new DeveloperFragment());
        }
    }

    public static class AccountFragment extends BaseSettingsFragment {
        private static final String PREF_KEY_STORAGE = "storage";
        private static final String PREF_KEY_EXPORT = "export";
        private static final String PREF_KEY_DELETE = "delete";

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.settings_account, rootKey);

            setOnPreferenceClickFragment(PREF_KEY_STORAGE, new SettingsStorageFragment());
            setOnPreferenceClickFragment(PREF_KEY_EXPORT, new SettingsAccountExportFragment());
            setOnPreferenceClickFragment(PREF_KEY_DELETE, new SettingsAccountDeleteFragment());
        }
    }

    public static class HelpFragment extends BaseSettingsFragment {
        private static final String PREF_KEY_FAQ = "faq";
        private static final String PREF_KEY_TOS = "tos";
        private static final String PREF_KEY_PRIVACY_POLICY = "privacy_policy";

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.settings_help, rootKey);

            getPreference(PREF_KEY_FAQ).setOnPreferenceClickListener(pref -> {
                IntentUtils.openUrlInBrowser(requireActivity(), Constants.KATCHUP_WEBSITE_BASE_URL);
                return true;
            });

            getPreference(PREF_KEY_TOS).setOnPreferenceClickListener(pref -> {
                IntentUtils.openUrlInBrowser(requireActivity(), Constants.KATCHUP_TERMS_LINK);
                return true;
            });

            getPreference(PREF_KEY_PRIVACY_POLICY).setOnPreferenceClickListener(pref -> {
                IntentUtils.openUrlInBrowser(requireActivity(), Constants.KATCHUP_PRIVACY_NOTICE_LINK);
                return true;
            });
        }
    }

    public static class PrivacyFragment extends BaseSettingsFragment {
        private static final String PREF_KEY_BLOCKED = "blocked";

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.settings_privacy, rootKey);

            setOnPreferenceClickFragment(PREF_KEY_BLOCKED, new SettingsBlockedFragment());
        }
    }

    public static class NotificationsFragment extends BaseSettingsFragment {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            getPreferenceManager().setSharedPreferencesName(Preferences.DEVICE_LOCAL_PREFS_NAME);
            setPreferencesFromResource(R.xml.settings_notifications, rootKey);
        }
    }

    public static class DeveloperFragment extends BaseSettingsFragment {
        private static final String PREF_KEY_CRASH = "crash";
        private static final String PREF_KEY_CONTACT_SYNC = "contact_sync";
        private static final String PREF_KEY_RELATIONSHIP_SYNC = "relationship_sync";
        private static final String PREF_KEY_FAKE_DAILY_NOTIFICATION = "fake_daily_notification";
        private static final String PREF_KEY_EXPIRATION_ACTIVITY = "expiration_activity";
        private static final String PREF_KEY_SHOW_SERVER_SCORE = "show_server_score";
        private static final String PREF_KEY_CLEAN_CACHE_DIRS = "clean_cache_dirs";

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            getPreferenceManager().setSharedPreferencesName(Preferences.DEVICE_LOCAL_PREFS_NAME);
            setPreferencesFromResource(R.xml.settings_developer, rootKey);

            getPreference(PREF_KEY_CRASH).setOnPreferenceClickListener(preference -> {
                Preconditions.checkNotNull(null);
                return true;
            });

            getPreference(PREF_KEY_CONTACT_SYNC).setOnPreferenceClickListener(preference -> {
                ContactsSync.getInstance().forceFullContactsSync();
                return true;
            });

            getPreference(PREF_KEY_RELATIONSHIP_SYNC).setOnPreferenceClickListener(preference -> {
                Preferences.getInstance().setLastFullRelationshipSyncTime(0);
                RelationshipSyncWorker.schedule(requireContext());
                return true;
            });

            getPreference(PREF_KEY_FAKE_DAILY_NOTIFICATION).setOnPreferenceClickListener(preference -> {
                getView().postDelayed(() -> {
                    KatchupConnectionObserver.getInstance(AppContext.getInstance().get()).onMomentNotificationReceived(
                            MomentNotification.newBuilder()
                                    .setNotificationId(Preferences.getInstance().getMomentNotificationId())
                                    .setPrompt("Cool prompt")
                                    .setTimestamp(System.currentTimeMillis() / 1000L)
                                    .setType(MomentNotification.Type.LIVE_CAMERA)
                                    .build(), null
                    );
                }, 2);
                return true;
            });

            getPreference(PREF_KEY_EXPIRATION_ACTIVITY).setOnPreferenceClickListener(preference -> {
                Intent intent = AppExpirationActivity.open(requireContext(), 14);
                startActivity(intent);
                return true;
            });

            getPreference(PREF_KEY_CLEAN_CACHE_DIRS).setOnPreferenceClickListener(preference -> {
                FileStore.getInstance().cleanup();
                return true;
            });
        }
    }
}

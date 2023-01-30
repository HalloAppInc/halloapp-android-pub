package com.halloapp;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.halloapp.contacts.ContactsSync;
import com.halloapp.katchup.ContactsAndLocationAccessActivity;
import com.halloapp.katchup.FollowingFragment;
import com.halloapp.katchup.GetStartedActivity;
import com.halloapp.katchup.MainFragment;
import com.halloapp.katchup.NewProfileFragment;
import com.halloapp.katchup.OnboardingFollowingActivity;
import com.halloapp.katchup.SettingsFragment;
import com.halloapp.katchup.SetupNameProfileActivity;
import com.halloapp.katchup.SetupUsernameProfileActivity;
import com.halloapp.registration.CheckRegistration;
import com.halloapp.ui.HalloActivity;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.logs.Log;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends HalloActivity implements EasyPermissions.PermissionCallbacks, FollowingFragment.NextScreenHandler {

    // TODO(jack): Remove need for these being duplicated here from halloapp's MainActivity
    public static final String EXTRA_STACK_TOP_MOMENT_ID = "stack_top_moment";
    public static final String EXTRA_POST_ID = "target_post";
    public static final String EXTRA_POST_SHOW_COMMENTS = "show_comments";
    public static final String EXTRA_SCROLL_TO_TOP = "scroll_to_top";
    public static final String EXTRA_NAV_TARGET = "nav_target";
    public static final String EXTRA_POST_START_MOMENT_POST = "start_moment_post";
    public static final String NAV_TARGET_FEED = "feed";
    public static final String NAV_TARGET_GROUPS = "groups";
    public static final String NAV_TARGET_MESSAGES = "messages";
    public static final String NAV_TARGET_ACTIVITY = "activity";
    public static final int REQUEST_CODE_ASK_CONTACTS_PERMISSION = 1;
    private static final int REQUEST_CODE_CAPTURE_IMAGE = 2;
    private static final int REQUEST_CODE_SELECT_CONTACT = 3;
    private static final int REQUEST_CODE_ASK_CONTACTS_PERMISSION_CHAT = 4;
    private static final int REQUEST_CODE_ASK_CONTACTS_PERMISSION_CREATE_GROUP = 5;
    private static final int REQUEST_CODE_ASK_CONTACTS_PERMISSION_POST_TEXT = 6;
    private static final int REQUEST_CODE_ASK_CONTACTS_PERMISSION_POST_MOMENT = 7;
    private static final int REQUEST_CODE_ASK_CONTACTS_PERMISSION_POST_MEDIA = 8;
    public static final int REQUEST_CODE_ASK_CONTACTS_PERMISSION_INVITE = 9;

    private ViewPager2 viewPager;
    private FragmentStateAdapter pagerAdapter;
    private MainViewModel mainViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_main);

        viewPager = findViewById(R.id.pager);
        pagerAdapter = new SlidingPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(1);

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        mainViewModel.registrationStatus.getLiveData().observe(this, checkResult -> {
            if (checkResult == null) {
//                progress.setVisibility(View.VISIBLE);
                return;
            }
            if (!checkResult.onboardingPermissionsSetup) {
                Log.i("NewMainActivity.onStart: onboarding permissions not setup");
                startActivity(new Intent(getBaseContext(), ContactsAndLocationAccessActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return;
            } else if (!checkResult.registered) {
                Log.i("NewMainActivity.onStart: not registered");
                Intent regIntent = RegistrationRequestActivity.register(getBaseContext(), checkResult.lastSyncTime);
                startActivity(regIntent);
                overridePendingTransition(0, 0);
                finish();
                return;
            } else if (!checkResult.profileSetup){
                Log.i("NewMainActivity.onStart: profile not setup");
                if (TextUtils.isEmpty(checkResult.name)) {
                    startActivity(new Intent(getBaseContext(), SetupNameProfileActivity.class));
                } else {
                    startActivity(SetupUsernameProfileActivity.open(getBaseContext(), checkResult.name));
                }
                overridePendingTransition(0, 0);
                finish();
                return;
            } else if (TextUtils.isEmpty(checkResult.username)) {
                // TODO(vasil): need this so existing installations can simply set up their username on upgrade. Can remove later.
                Log.i("NewMainActivity.onStart: username not setup");
                startActivity(SetupUsernameProfileActivity.open(getBaseContext(), checkResult.name));
                overridePendingTransition(0, 0);
                finish();
                return;
            } else if (!checkResult.onboardingFollowingSetup) {
                Log.i("NewMainActivity.onStart: onboarding following not setup");
                startActivity(new Intent(getBaseContext(), OnboardingFollowingActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return;
            } else if (!checkResult.onboardingGetStartedShown) {
                Log.i("NewMainActivity.onStart: onboarding following not setup");
                startActivity(new Intent(getBaseContext(), GetStartedActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return;
            }
//            progress.setVisibility(View.GONE);
        });
    }

    @Override
    public void onBackPressed() {
        int currentItem = viewPager.getCurrentItem();
        if (currentItem == 1) { // Main screen
            super.onBackPressed();
        } else if (currentItem == 0) {
            viewPager.setCurrentItem(1);
        } else {
            viewPager.setCurrentItem(currentItem - 1);
        }
    }

    @Override
    public void nextScreen() {
        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
    }

    public void previousScreen() {
        viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Log.d("MainActivity.onPermissionsGranted " + requestCode + " " + perms);
        if (requestCode == REQUEST_CODE_ASK_CONTACTS_PERMISSION) {
            Preferences.getInstance().clearContactSyncBackoffTime();
            ContactsSync.getInstance().forceFullContactsSync(true);
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Log.d("MainActivity.onPermissionsDenied " + requestCode + " " + perms);
    }

    private class SlidingPagerAdapter extends FragmentStateAdapter {
        public SlidingPagerAdapter(FragmentActivity activity) {
            super(activity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0: return new FollowingFragment();
                case 1: return new MainFragment();
                case 2: return new NewProfileFragment();
                case 3: return new SettingsFragment();
                default: throw new IllegalArgumentException("Invalid position " + position);
            }
        }

        @Override
        public int getItemCount() {
            return 4;
        }
    }

    public static class MainViewModel extends AndroidViewModel {

        final ComputableLiveData<CheckRegistration.CheckResult> registrationStatus;

        private final Me me;
        private final Preferences preferences;

        public MainViewModel(@NonNull Application application) {
            super(application);

            me = Me.getInstance();
            preferences = Preferences.getInstance();

            registrationStatus = new ComputableLiveData<CheckRegistration.CheckResult>() {
                @Override
                protected CheckRegistration.CheckResult compute() {
                    return CheckRegistration.checkRegistration(me, preferences);
                }
            };
        }
    }
}

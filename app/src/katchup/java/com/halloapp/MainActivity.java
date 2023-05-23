package com.halloapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Application;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.splashscreen.SplashScreenViewProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.halloapp.contacts.ContactsSync;
import com.halloapp.id.UserId;
import com.halloapp.katchup.Analytics;
import com.halloapp.katchup.ContactsAndLocationAccessActivity;
import com.halloapp.katchup.FollowingFragment;
import com.halloapp.katchup.GetStartedActivity;
import com.halloapp.katchup.MainFragment;
import com.halloapp.katchup.NewProfileFragment;
import com.halloapp.katchup.OnboardingFollowingActivity;
import com.halloapp.katchup.SetupNameProfileActivity;
import com.halloapp.katchup.SetupUsernameProfileActivity;
import com.halloapp.registration.CheckRegistration;
import com.halloapp.ui.HalloActivity;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.logs.Log;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends HalloActivity implements EasyPermissions.PermissionCallbacks, FollowingFragment.NextScreenHandler, MainFragment.SlidableActivity {

    // TODO(jack): Remove need for these being duplicated here from halloapp's MainActivity
    public static final String EXTRA_STACK_TOP_MOMENT_ID = "stack_top_moment";
    public static final String EXTRA_POST_ID = "target_post";
    public static final String EXTRA_POST_SHOW_COMMENTS = "show_comments";
    public static final String EXTRA_SCROLL_TO_TOP = "scroll_to_top";
    public static final String EXTRA_NAV_TARGET = "nav_target";
    public static final String EXTRA_POST_START_MOMENT_POST = "start_moment_post";
    public static final String EXTRA_GO_TO_FOR_YOU = "go_to_for_you";
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

    private static final int SPLASH_FADE_OUT_DURATION_MILLISECONDS = 250;

    public static boolean registrationIsDone = false;

    private ViewPager2 viewPager;
    private SlidingPagerAdapter pagerAdapter;
    private MainViewModel mainViewModel;
    private boolean goToForYou = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        final SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);

        Window window = getWindow();
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.BLACK);

        splashScreen.setOnExitAnimationListener(this::removeSplash);

        setContentView(R.layout.activity_new_main);

        goToForYou = getIntent().getBooleanExtra(EXTRA_GO_TO_FOR_YOU, false);

        viewPager = findViewById(R.id.pager);
        pagerAdapter = new SlidingPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(1);

        if (getIntent().getBooleanExtra(Notifications.EXTRA_IS_NOTIFICATION, false)) {
            Analytics.getInstance().notificationOpened(getIntent().getStringExtra(Notifications.EXTRA_NOTIFICATION_TYPE));
        }

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        mainViewModel.registrationStatus.observe(this, checkResult -> {
            if (checkResult == null) {
//                progress.setVisibility(View.VISIBLE);
                return;
            }
            if (!checkResult.onboardingPermissionsSetup) {
                Log.i("MainActivity.onCreate.registrationStatus: onboarding permissions not setup");
                splashScreen.setKeepOnScreenCondition(() -> true);
                startActivity(new Intent(getBaseContext(), ContactsAndLocationAccessActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return;
            } else if (!checkResult.registered) {
                Log.i("MainActivity.onCreate.registrationStatus: not registered");
                Intent regIntent = RegistrationRequestActivity.register(getBaseContext(), checkResult.lastSyncTime);
                startActivity(regIntent);
                overridePendingTransition(0, 0);
                finish();
                return;
            } else if (!checkResult.profileSetup) {
                Log.i("MainActivity.onCreate.registrationStatus: profile not setup");
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
                Log.i("MainActivity.onCreate.registrationStatus: username not setup");
                startActivity(SetupUsernameProfileActivity.open(getBaseContext(), checkResult.name));
                overridePendingTransition(0, 0);
                finish();
                return;
            } else if (!checkResult.onboardingFollowingSetup) {
                Log.i("MainActivity.onCreate.registrationStatus: onboarding following not setup");
                Analytics.getInstance().setUserProperty("onboardingCompleted", true);
                startActivity(new Intent(getBaseContext(), OnboardingFollowingActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return;
            } else if (!checkResult.onboardingGetStartedShown) {
                Log.i("MainActivity.onCreate.registrationStatus: onboarding following not setup");
                Analytics.getInstance().setUserProperty("onboardingCompleted", true);
                startActivity(new Intent(getBaseContext(), GetStartedActivity.class));
                overridePendingTransition(0, 0);
                finish();
            } else {
                registrationIsDone = true;
                Analytics.getInstance().setUserProperty("onboardingCompleted", true);
                Analytics.getInstance().initUserProperties(getApplicationContext());
                return;
            }
//            progress.setVisibility(View.GONE);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mainViewModel.computeRegistrationStatus();
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
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);

        if (pagerAdapter.isMainFragment(viewPager.getCurrentItem())) {
            for (Fragment fragment : getSupportFragmentManager().getFragments()) {
                if (fragment instanceof MainFragment) {
                    ((MainFragment) fragment).onActivityReenter(resultCode, data);
                    break;
                }
            }
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

    private void removeSplash(SplashScreenViewProvider splashScreenViewProvider) {
        final ObjectAnimator fadeOut = ObjectAnimator.ofFloat(splashScreenViewProvider.getView(), View.ALPHA.getName(), 1f, 0f);
        fadeOut.setDuration(SPLASH_FADE_OUT_DURATION_MILLISECONDS);
        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                splashScreenViewProvider.remove();
            }
        });
        fadeOut.start();
    }

    @Override
    public void setSlideEnabled(boolean enabled) {
        viewPager.setUserInputEnabled(enabled);
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
                case 1: return MainFragment.create(goToForYou);
                case 2: return NewProfileFragment.newProfileFragment(UserId.ME);
                default: throw new IllegalArgumentException("Invalid position " + position);
            }
        }

        public boolean isMainFragment(int position) {
            return position == 1;
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }

    public static class MainViewModel extends AndroidViewModel {

        final MutableLiveData<CheckRegistration.CheckResult> registrationStatus = new MutableLiveData<>();

        private final Me me;
        private final BgWorkers bgWorkers;
        private final Preferences preferences;

        public MainViewModel(@NonNull Application application) {
            super(application);

            me = Me.getInstance();
            bgWorkers = BgWorkers.getInstance();
            preferences = Preferences.getInstance();
        }

        public void computeRegistrationStatus() {
            bgWorkers.execute(() -> {
                registrationStatus.postValue(CheckRegistration.checkRegistration(me, preferences));
            });
        }
    }
}

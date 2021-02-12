package com.halloapp.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.halloapp.BuildConfig;
import com.halloapp.Constants;
import com.halloapp.Debug;
import com.halloapp.Notifications;
import com.halloapp.R;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.ContactsSync;
import com.halloapp.id.ChatId;
import com.halloapp.id.UserId;
import com.halloapp.media.MediaUtils;
import com.halloapp.props.ServerProps;
import com.halloapp.ui.camera.CameraActivity;
import com.halloapp.ui.chat.ChatActivity;
import com.halloapp.ui.contacts.ContactsActivity;
import com.halloapp.ui.contacts.MultipleContactPickerActivity;
import com.halloapp.ui.groups.CreateGroupActivity;
import com.halloapp.ui.groups.GroupCreationPickerActivity;
import com.halloapp.ui.mediaexplorer.MediaExplorerActivity;
import com.halloapp.ui.mediapicker.MediaPickerActivity;
import com.halloapp.util.logs.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.widget.ActionBarShadowOnScrollListener;
import com.halloapp.widget.BadgedDrawable;
import com.halloapp.widget.NetworkIndicatorView;
import com.halloapp.xmpp.Connection;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends HalloActivity implements EasyPermissions.PermissionCallbacks, ActionBarShadowOnScrollListener.Host {

    public static final String EXTRA_POST_ID = "target_post";
    public static final String EXTRA_POST_SHOW_COMMENTS = "show_comments";
    public static final String EXTRA_NAV_TARGET = "nav_target";
    public static final String NAV_TARGET_FEED = "feed";
    public static final String NAV_TARGET_GROUPS = "groups";
    public static final String NAV_TARGET_MESSAGES = "messages";
    public static final String NAV_TARGET_PROFILE = "profile";

    private static final int REQUEST_CODE_ASK_CONTACTS_PERMISSION = 1;
    private static final int REQUEST_CODE_CAPTURE_IMAGE = 2;
    private static final int REQUEST_CODE_SELECT_CONTACT = 3;

    private final ServerProps serverProps = ServerProps.getInstance();

    private SpeedDialView fabView;
    private View toolbarContainer;
    private BottomNavigationView navView;

    private MainViewModel mainViewModel;
    private ProfileNuxViewModel profileNuxViewModel;

    private final ContactsDb.Observer contactsObserver = new ContactsDb.BaseObserver() {
        @Override
        public void onContactsReset() {
            startActivity(new Intent(getBaseContext(), InitialSyncActivity.class));
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 28) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        getWindow().getDecorView().setSystemUiVisibility(SystemUiVisibility.getDefaultSystemUiVisibility(this));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        setContentView(R.layout.activity_main);

        View progress = findViewById(R.id.progress);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbarContainer = findViewById(R.id.toolbar_container);

        navView = findViewById(R.id.nav_view);
        final AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home,
                R.id.navigation_groups,
                R.id.navigation_messages,
                R.id.navigation_profile).build();
        final NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        if (!serverProps.getGroupFeedEnabled()) {
            navView.getMenu().removeItem(R.id.navigation_groups);
        }

        navView.setSaveEnabled(false);
        navView.setOnNavigationItemReselectedListener(item -> scrollToTop());

        final NetworkIndicatorView networkIndicatorView = findViewById(R.id.network_indicator);
        networkIndicatorView.bind(this);

        final ViewGroup messagesTab = navView.findViewById(R.id.navigation_messages);
        messagesTab.setClipChildren(false);
        messagesTab.setClipToPadding(false);

        profileNuxViewModel = new ViewModelProvider(this).get(ProfileNuxViewModel.class);
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        mainViewModel.unseenChatsCount.getLiveData().observe(this,
                unseenChatsCount -> {
                    if (unseenChatsCount == null || unseenChatsCount == 0) {
                        navView.removeBadge(R.id.navigation_messages);
                    } else {
                        BadgeDrawable badge = navView.getOrCreateBadge(R.id.navigation_messages);
                        badge.setVerticalOffset(getResources().getDimensionPixelSize(R.dimen.badge_offset_vertical));
                        badge.setHorizontalOffset(getResources().getDimensionPixelSize(R.dimen.badge_offset_horizontal));
                        badge.setNumber(unseenChatsCount);
                    }
                });
        fabView = findViewById(R.id.speed_dial);
        fabView.getMainFab().setRippleColor(ContextCompat.getColor(this, R.color.white_20));
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (fabView.isOpen()) {
                fabView.close();
            }
            updateFab(destination.getId());
        });


        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == 0 && savedInstanceState == null) {
            // The activity was not launched from history
            processIntent(getIntent());
        }

        ContactsDb.getInstance().addObserver(contactsObserver);

        mainViewModel.registrationStatus.getLiveData().observe(this, checkResult -> {
            if (checkResult == null) {
                progress.setVisibility(View.VISIBLE);
                return;
            }
            if (!checkResult.registered) {
                Log.i("MainActivity.onStart: not registered");
                startActivity(new Intent(getBaseContext(), RegistrationRequestActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return;
            } else if (checkResult.lastSyncTime <= 0) {
                Log.i("MainActivity.onStart: not synced");
                startActivity(new Intent(getBaseContext(), InitialSyncActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return;
            } else {
                final String[] perms = {Manifest.permission.READ_CONTACTS};
                if (!EasyPermissions.hasPermissions(this, perms)) {
                    EasyPermissions.requestPermissions(this, getString(R.string.contacts_permission_rationale),
                            REQUEST_CODE_ASK_CONTACTS_PERMISSION, perms);
                }
            }
            progress.setVisibility(View.GONE);
        });
    }

    private void scrollToTop() {
        Fragment navigationFragment = getSupportFragmentManager().getPrimaryNavigationFragment();
        if (navigationFragment != null) {
            for (Fragment tabFragment : navigationFragment.getChildFragmentManager().getFragments()) {
                if (tabFragment instanceof MainNavFragment) {
                    ((MainNavFragment) tabFragment).resetScrollPosition();
                }
            }
        }
    }

    private void updateFab(@IdRes int id) {
        fabView.clearActionItems();
        if (id == R.id.navigation_messages) {
            fabView.show();
            fabView.findViewById(R.id.sd_main_fab).setContentDescription(getString(R.string.new_chat));
            fabView.setMainFabClosedDrawable(ContextCompat.getDrawable(this, R.drawable.ic_chat));
            fabView.setOnChangeListener(new SpeedDialView.OnChangeListener() {
                @Override
                public boolean onMainActionSelected() {
                    startActivityForResult(new Intent(getBaseContext(), ContactsActivity.class), REQUEST_CODE_SELECT_CONTACT);
                    return true;
                }

                @Override
                public void onToggleChanged(boolean b) {
                }
            });
            fabView.setOnActionSelectedListener(null);
        } else if (id == R.id.navigation_groups) {
            fabView.show();
            fabView.findViewById(R.id.sd_main_fab).setContentDescription(getString(R.string.new_group));
            fabView.setMainFabClosedDrawable(ContextCompat.getDrawable(this, R.drawable.ic_group_add));
            fabView.setOnChangeListener(new SpeedDialView.OnChangeListener() {
                @Override
                public boolean onMainActionSelected() {
                    startActivity(GroupCreationPickerActivity.newIntent(MainActivity.this, null));
                    return true;
                }

                @Override
                public void onToggleChanged(boolean b) {
                }
            });
            fabView.setOnActionSelectedListener(null);
        } else if (id == R.id.navigation_home){
            fabView.show();
            fabView.findViewById(R.id.sd_main_fab).setContentDescription(getString(R.string.add_post));
            fabView.setMainFabClosedDrawable(ContextCompat.getDrawable(this, R.drawable.ic_add));
            fabView.setOnChangeListener(new SpeedDialView.OnChangeListener() {
                @Override
                public boolean onMainActionSelected() {
                    return false;
                }

                @Override
                public void onToggleChanged(boolean isOpen) {
                }
            });
            fabView.setOnActionSelectedListener(actionItem -> {
                onFabActionSelected(actionItem.getId());
                return true;
            });
            addFabItem(fabView, R.id.add_post_gallery, R.drawable.ic_image, R.string.gallery_post);
            addFabItem(fabView, R.id.add_post_camera, R.drawable.ic_camera, R.string.camera_post);
            addFabItem(fabView, R.id.add_post_text, R.drawable.ic_text, R.string.text_post);
        } else {
            fabView.hide();
        }
    }

    private static void addFabItem(@NonNull SpeedDialView fabView, @IdRes int id, @DrawableRes int icon, @StringRes int label) {
        final View itemView = fabView.addActionItem(
                new SpeedDialActionItem.Builder(id, icon)
                        .setFabSize(FloatingActionButton.SIZE_NORMAL)
                        .setFabBackgroundColor(ContextCompat.getColor(fabView.getContext(), R.color.fab_background))
                        .setFabImageTintColor(ContextCompat.getColor(fabView.getContext(), android.R.color.white))
                        .create());
        Preconditions.checkNotNull(itemView).findViewById(R.id.sd_fab).setContentDescription(fabView.getContext().getString(label));
    }

    private void onFabActionSelected(@IdRes int id) {
        if (id == R.id.add_post_text) {
            startActivity(new Intent(this, ContentComposerActivity.class));
        } else if (id == R.id.add_post_gallery) {
            final Intent intent = new Intent(this, MediaPickerActivity.class);
            intent.putExtra(MediaPickerActivity.EXTRA_PICKER_PURPOSE, MediaPickerActivity.PICKER_PURPOSE_POST);
            startActivity(intent);
        } else if (id == R.id.add_post_camera) {
            final Intent intent = new Intent(this, CameraActivity.class);
            startActivity(intent);
        }
        fabView.close(false);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i("MainActivity.onNewIntent");
        processIntent(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ContactsDb.getInstance().removeObserver(contactsObserver);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Notifications.getInstance(this).clearFeedNotifications();
        if (Connection.getInstance().getClientExpired()) {
            AppExpirationActivity.open(this, 0);
        }
        mainViewModel.registrationStatus.invalidate();
    }

    @Override
    public void onBackPressed() {
        if (fabView.isOpen()) {
            fabView.close();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // Allow dismissal of FAB menu on scroll
        if (fabView.isOpen()) {
            if (ev.getX() < fabView.getX()
                    || ev.getX() > fabView.getX() + fabView.getWidth()
                    || ev.getY() > fabView.getY() + fabView.getHeight()
                    || ev.getY() < fabView.getY()) {
                fabView.close();
            }
        }
        if (navView.getSelectedItemId() == R.id.navigation_profile) {
            if (profileNuxViewModel.dismissMakePostNuxIfOpen()) {
                return true;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> list) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (requestCode) {
            case REQUEST_CODE_ASK_CONTACTS_PERMISSION: {
                ContactsSync.getInstance(this).startAddressBookListener();
                ContactsSync.getInstance(this).startContactsSync(true);
                break;
            }
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        // Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            //noinspection SwitchStatementWithTooFewBranches
            switch (requestCode) {
                case REQUEST_CODE_ASK_CONTACTS_PERMISSION: {
                    new AppSettingsDialog.Builder(this)
                            .setRationale(getString(R.string.contacts_permission_rationale_denied))
                            .build().show();
                    break;
                }
            }
        }
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);

        if (resultCode == RESULT_OK && data.hasExtra(MediaExplorerActivity.EXTRA_CONTENT_ID) && data.hasExtra(MediaExplorerActivity.EXTRA_SELECTED)) {
            String contentId = data.getStringExtra(MediaExplorerActivity.EXTRA_CONTENT_ID);
            int position = data.getIntExtra(MediaExplorerActivity.EXTRA_SELECTED, 0);

            ViewPager2 pager = findViewById(R.id.container).findViewWithTag(MediaPagerAdapter.getPagerTag(contentId));

            if (pager != null && pager.getCurrentItem() != position) {
                postponeEnterTransition();

                pager.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        pager.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        startPostponedEnterTransition();
                    }
                });

                pager.setCurrentItem(position, false);
            }
        }
    }

    @Override
    public void onActivityResult(final int request, final int result, final Intent data) {
        super.onActivityResult(request, result, data);
        switch (request) {
            case REQUEST_CODE_CAPTURE_IMAGE: {
                if (result == Activity.RESULT_OK) {
                    final Intent intent = new Intent(this, ContentComposerActivity.class);
                    intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,
                            new ArrayList<>(Collections.singleton(MediaUtils.getImageCaptureUri(this))));
                    startActivity(intent);
                }
                break;
            }
            case REQUEST_CODE_SELECT_CONTACT:
                if (result == RESULT_OK) {
                    String rawId = data.getStringExtra(ContactsActivity.RESULT_SELECTED_ID);
                    startActivity(new Intent(this, ChatActivity.class).putExtra(ChatActivity.EXTRA_CHAT_ID, ChatId.fromNullable(rawId)));
                }
                break;
        }
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (BuildConfig.DEBUG && keyCode == KeyEvent.KEYCODE_BACK) {
            Debug.showDebugMenu(this, findViewById(R.id.nav_view));
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    private void processIntent(Intent intent) {
        Log.i("MainActivity.processIntent " + intent.getAction() + " " + intent.getStringExtra(EXTRA_NAV_TARGET));
        final String extraNotificationNavTarget = intent.getStringExtra(EXTRA_NAV_TARGET);
        if (NAV_TARGET_FEED.equals(extraNotificationNavTarget)) {
            final BottomNavigationView navView = findViewById(R.id.nav_view);
            navView.setSelectedItemId(R.id.navigation_home);
        } else if (NAV_TARGET_MESSAGES.equals(extraNotificationNavTarget)) {
            final BottomNavigationView navView = findViewById(R.id.nav_view);
            navView.setSelectedItemId(R.id.navigation_messages);
        } else if (NAV_TARGET_PROFILE.equals(extraNotificationNavTarget)) {
            final BottomNavigationView navView = findViewById(R.id.nav_view);
            navView.setSelectedItemId(R.id.navigation_profile);
        } else if (NAV_TARGET_GROUPS.equals(extraNotificationNavTarget)) {
            final BottomNavigationView navView = findViewById(R.id.nav_view);
            navView.setSelectedItemId(R.id.navigation_groups);
        }
        String extraPostId = intent.getStringExtra(EXTRA_POST_ID);
        boolean showCommentsActivity = intent.getBooleanExtra(EXTRA_POST_SHOW_COMMENTS, false);
        if (extraPostId != null) {
            if (showCommentsActivity) {
                Intent viewIntent = new Intent(this, CommentsActivity.class);
                viewIntent.putExtra(CommentsActivity.EXTRA_POST_ID, extraPostId);
                startActivity(viewIntent);
            } else {
                scrollToTop();
            }
        }
    }

    @Override
    public View getToolbarView() {
        return toolbarContainer;
    }
}

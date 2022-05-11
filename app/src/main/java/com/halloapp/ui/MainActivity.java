package com.halloapp.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.SharedElementCallback;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.halloapp.BuildConfig;
import com.halloapp.Debug;
import com.halloapp.Notifications;
import com.halloapp.R;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.id.ChatId;
import com.halloapp.media.MediaUtils;
import com.halloapp.permissions.PermissionUtils;
import com.halloapp.props.ServerProps;
import com.halloapp.ui.camera.CameraActivity;
import com.halloapp.ui.chat.ChatActivity;
import com.halloapp.ui.contacts.ContactsActivity;
import com.halloapp.ui.groups.CreateGroupActivity;
import com.halloapp.ui.home.HomeViewModel;
import com.halloapp.ui.invites.InviteContactsActivity;
import com.halloapp.ui.mediaexplorer.MediaExplorerActivity;
import com.halloapp.ui.mediapicker.MediaPickerActivity;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.logs.Log;
import com.halloapp.util.stats.Events;
import com.halloapp.widget.ActionBarShadowOnScrollListener;
import com.halloapp.widget.FabExpandOnScrollListener;
import com.halloapp.widget.HACustomFab;
import com.halloapp.widget.NetworkIndicatorView;
import com.halloapp.xmpp.Connection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends HalloActivity implements EasyPermissions.PermissionCallbacks, ActionBarShadowOnScrollListener.Host, FabExpandOnScrollListener.Host {

    public static final String EXTRA_POST_ID = "target_post";
    public static final String EXTRA_POST_SHOW_COMMENTS = "show_comments";
    public static final String EXTRA_SCROLL_TO_TOP = "scroll_to_top";
    public static final String EXTRA_NAV_TARGET = "nav_target";
    public static final String NAV_TARGET_FEED = "feed";
    public static final String NAV_TARGET_GROUPS = "groups";
    public static final String NAV_TARGET_MESSAGES = "messages";
    public static final String NAV_TARGET_PROFILE = "profile";

    private static final int REQUEST_CODE_ASK_CONTACTS_PERMISSION = 1;
    private static final int REQUEST_CODE_CAPTURE_IMAGE = 2;
    private static final int REQUEST_CODE_SELECT_CONTACT = 3;
    private static final int REQUEST_CODE_ASK_CONTACTS_PERMISSION_CHAT = 4;
    private static final int REQUEST_CODE_ASK_CONTACTS_PERMISSION_CREATE_GROUP = 5;
    private static final int REQUEST_CODE_ASK_CONTACTS_PERMISSION_POST_TEXT = 6;
    private static final int REQUEST_CODE_ASK_CONTACTS_PERMISSION_POST_CAMERA = 7;
    private static final int REQUEST_CODE_ASK_CONTACTS_PERMISSION_POST_MEDIA = 8;
    public static final int REQUEST_CODE_ASK_CONTACTS_PERMISSION_INVITE = 9;


    private final BgWorkers bgWorkers = BgWorkers.getInstance();

    private View toolbarContainer;
    private BottomNavigationView navView;

    private MainViewModel mainViewModel;
    private HomeViewModel homeViewModel;

    private NavController navController;
    private HACustomFab haFabView;

    private final ContactsDb.Observer contactsObserver = new ContactsDb.BaseObserver() {
        @Override
        public void onContactsReset() {
            startActivity(new Intent(getBaseContext(), InitialSyncActivity.class));
            finish();
        }
    };

    private final SharedElementCallback sharedElementCallback = new SharedElementCallback() {
        @Override
        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
            for (String name : names) {
                View view = MediaPagerAdapter.getTransitionView(findViewById(R.id.container), name);
                if (view != null) {
                    sharedElements.put(name, view);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        supportRequestWindowFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        supportRequestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setExitSharedElementCallback(sharedElementCallback);

        bgWorkers.execute(new Runnable() {
            @Override
            public void run() {
                long numberBytesAvail = Environment.getDataDirectory().getFreeSpace();
                Log.d("Storage available bytes: " + numberBytesAvail);
                if (numberBytesAvail < LowStorageActivity.MINIMUM_STORAGE_BYTES) {
                    startActivity(new Intent(getBaseContext(), LowStorageActivity.class));
                    finish();
                }
            }
        });

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
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        if (ServerProps.getInstance().getGroupsRefreshEnabled()) {
            navController.setGraph(R.navigation.mobile_navigation_groups_v2);
        } else {
            navController.setGraph(R.navigation.mobile_navigation);
        }
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        navView.setOnNavigationItemReselectedListener(item -> scrollToTop());

        final NetworkIndicatorView networkIndicatorView = findViewById(R.id.network_indicator);
        networkIndicatorView.bind(this);

        final ViewGroup messagesTab = navView.findViewById(R.id.navigation_messages);
        messagesTab.setClipChildren(false);
        messagesTab.setClipToPadding(false);

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
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
        mainViewModel.unseenGroupsCount.getLiveData().observe(this,
                unseenGroupsCount -> {
                    if (unseenGroupsCount == null || unseenGroupsCount == 0) {
                        navView.removeBadge(R.id.navigation_groups);
                    } else {
                        BadgeDrawable badge = navView.getOrCreateBadge(R.id.navigation_groups);
                        badge.setVerticalOffset(getResources().getDimensionPixelSize(R.dimen.badge_offset_vertical));
                        badge.setHorizontalOffset(getResources().getDimensionPixelSize(R.dimen.badge_offset_horizontal));
                        badge.setNumber(unseenGroupsCount);
                    }
                });
        haFabView = findViewById(R.id.ha_fab);
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (haFabView.isOpen()) {
                haFabView.close();
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
            }
            progress.setVisibility(View.GONE);
        });

        homeViewModel.getUnseenHomePosts().observe(this, unseenPosts -> {
            if (unseenPosts == null || unseenPosts.isEmpty()) {
                navView.removeBadge(R.id.navigation_home);
            } else {
                BadgeDrawable badge = navView.getOrCreateBadge(R.id.navigation_home);
                badge.setBackgroundColor(ContextCompat.getColor(this, R.color.color_secondary));
                badge.setVerticalOffset(getResources().getDimensionPixelSize(R.dimen.badge_offset_vertical));
                badge.setHorizontalOffset(getResources().getDimensionPixelSize(R.dimen.badge_offset_horizontal));
            }
        });
    }

    public void refreshFab() {
        NavDestination current = navController.getCurrentDestination();
        if (current != null) {
            updateFab(current.getId());
        }
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

    private void startNewChat() {
        startActivityForResult(new Intent(getBaseContext(), ContactsActivity.class), REQUEST_CODE_SELECT_CONTACT);
    }

    private void createNewGroup() {
        startActivity(CreateGroupActivity.newPickerIntent(MainActivity.this));
    }

    private void updateFab(@IdRes int id) {
        haFabView.clearActionItems();
        if (id == R.id.navigation_messages) {
            haFabView.show();
            haFabView.setFabBackgroundTint(R.color.white);
            haFabView.setIconTint(R.color.color_primary);
            haFabView.setUseText(true);
            haFabView.setMainFabIcon(R.drawable.ic_compose, R.string.new_chat, R.string.compose_fab_label);
            haFabView.setOnFabClickListener(v -> {
                if (PermissionUtils.hasOrRequestContactPermissions(this, REQUEST_CODE_ASK_CONTACTS_PERMISSION_CHAT)) {
                    startNewChat();
                }
            });
            haFabView.setOnActionSelectedListener(null);
        } else if (id == R.id.navigation_groups) {
            haFabView.show();
            haFabView.setUseText(true);
            haFabView.setFabBackgroundTint(R.color.white);
            haFabView.setIconTint(R.color.color_primary);
            haFabView.setMainFabIcon(R.drawable.ic_fab_group_add, R.string.new_group, R.string.new_group_fab_label);
            haFabView.setOnFabClickListener(v -> {
                if (PermissionUtils.hasOrRequestContactPermissions(this, REQUEST_CODE_ASK_CONTACTS_PERMISSION_CREATE_GROUP)) {
                    createNewGroup();
                }
            });
            haFabView.setOnActionSelectedListener(null);
        } else if (id == R.id.navigation_home){
            haFabView.setUseText(false);
            haFabView.setFabBackgroundTint(R.color.color_primary);
            haFabView.setIconTint(R.color.white);
            if (EasyPermissions.hasPermissions(this, Manifest.permission.READ_CONTACTS)) {
                haFabView.show();
                haFabView.setMainFabIcon(R.drawable.ic_plus_expanded, R.string.add_post, R.string.post_fab_label);
                haFabView.setOnFabClickListener(null);
                haFabView.setOnActionSelectedListener(new HACustomFab.OnActionSelectedListener() {
                    @Override
                    public void onActionSelected(int actionId) {
                        onFabActionSelected(actionId);
                    }

                    @Override
                    public void onOverlay(boolean visible) {
                        ConstraintLayout root = findViewById(R.id.container);
                        for (int i = 0; i < root.getChildCount(); i++) {
                            View child = root.getChildAt(i);
                            if (child.getId() == R.id.ha_fab) {
                                continue;
                            }
                            if (visible) {
                                ViewCompat.setImportantForAccessibility(child, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
                            } else {
                                ViewCompat.setImportantForAccessibility(child, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
                            }
                        }
                        if (visible) {
                            homeViewModel.onFabMenuOpened();
                        } else {
                            homeViewModel.onFabMenuClosed();
                        }
                    }
                });
                haFabView.addSubFab(R.id.add_post_gallery, R.drawable.ic_image, R.string.gallery_post);
                haFabView.addSubFab(R.id.add_post_voice, R.drawable.ic_voice_post, R.string.voice_post);
                haFabView.addSubFab(R.id.add_post_text, R.drawable.ic_text, R.string.text_post);
                haFabView.addSubFab(R.id.add_post_camera, R.drawable.ic_camera, R.string.camera_post);
            } else {
                haFabView.hide();
            }
        } else {
            haFabView.hide();
        }
    }

    private void onFabActionSelected(@IdRes int id) {
        if (id == R.id.add_post_text) {
            if (PermissionUtils.hasOrRequestContactPermissions(this, REQUEST_CODE_ASK_CONTACTS_PERMISSION_POST_TEXT)) {
                startTextPost();
            }
        } else if (id == R.id.add_post_voice) {
            if (PermissionUtils.hasOrRequestContactPermissions(this, REQUEST_CODE_ASK_CONTACTS_PERMISSION_POST_TEXT)) {
                startVoicePost();
            }
        } else if (id == R.id.add_post_gallery) {
            if (PermissionUtils.hasOrRequestContactPermissions(this, REQUEST_CODE_ASK_CONTACTS_PERMISSION_POST_MEDIA)) {
                startMediaPost();
            }
        } else if (id == R.id.add_post_camera) {
            if (PermissionUtils.hasOrRequestContactPermissions(this, REQUEST_CODE_ASK_CONTACTS_PERMISSION_POST_CAMERA)) {
                startCameraPost();
            }
        }
        Events.getInstance().sendFabActionEvent(HACustomFab.viewIdToAction(id));
        haFabView.close(false);
    }

    private void startTextPost() {
        startActivity(ContentComposerActivity.newTextPost(this));
    }

    private void startVoicePost() {
        startActivity(ContentComposerActivity.newAudioPost(this));
    }

    private void startMediaPost() {
        final Intent intent = MediaPickerActivity.pickForPost(this);
        startActivity(intent);
    }

    private void startCameraPost() {
        final Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
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
        if (haFabView.isOpen()) {
            haFabView.close();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> list) {
        refreshFab();
        switch (requestCode) {
            case REQUEST_CODE_ASK_CONTACTS_PERMISSION: {
                // Just sync
                break;
            }
            case REQUEST_CODE_ASK_CONTACTS_PERMISSION_CHAT: {
                startNewChat();
                break;
            }
            case REQUEST_CODE_ASK_CONTACTS_PERMISSION_CREATE_GROUP: {
                createNewGroup();
                break;
            }
            case REQUEST_CODE_ASK_CONTACTS_PERMISSION_POST_TEXT: {
                startTextPost();
                break;
            }
            case REQUEST_CODE_ASK_CONTACTS_PERMISSION_POST_CAMERA: {
                startCameraPost();
                break;
            }
            case REQUEST_CODE_ASK_CONTACTS_PERMISSION_POST_MEDIA: {
                startMediaPost();
                break;
            }
            case REQUEST_CODE_ASK_CONTACTS_PERMISSION_INVITE: {
                startActivity(new Intent(this, InviteContactsActivity.class));
                break;
            }
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);

        if (resultCode == RESULT_OK && data != null && data.hasExtra(MediaExplorerActivity.EXTRA_CONTENT_ID) && data.hasExtra(MediaExplorerActivity.EXTRA_SELECTED)) {
            String contentId = data.getStringExtra(MediaExplorerActivity.EXTRA_CONTENT_ID);
            int position = data.getIntExtra(MediaExplorerActivity.EXTRA_SELECTED, 0);
            View root = findViewById(R.id.container);

            if (root != null && contentId != null) {
                postponeEnterTransition();
                MediaPagerAdapter.preparePagerForTransition(root, contentId, position, this::startPostponedEnterTransition);
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
                    startActivity(ChatActivity.open(this, ChatId.fromNullable(rawId)));
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
                Intent viewIntent = FlatCommentsActivity.viewComments(this, extraPostId);
                startActivity(viewIntent);
            } else {
                scrollToTop();
            }
        } else if (intent.getBooleanExtra(EXTRA_SCROLL_TO_TOP, false)) {
            scrollToTop();
        }
    }

    @Override
    public View getToolbarView() {
        return toolbarContainer;
    }

    @Override
    public HACustomFab getFab() {
        return haFabView;
    }
}

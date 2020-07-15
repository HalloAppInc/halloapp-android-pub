package com.halloapp.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.halloapp.BuildConfig;
import com.halloapp.Debug;
import com.halloapp.Me;
import com.halloapp.Notifications;
import com.halloapp.Preferences;
import com.halloapp.R;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.ContactsSync;
import com.halloapp.media.MediaUtils;
import com.halloapp.ui.chat.ChatActivity;
import com.halloapp.ui.contacts.ContactsActivity;
import com.halloapp.ui.mediapicker.MediaPickerActivity;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.widget.BadgedDrawable;
import com.halloapp.xmpp.Connection;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends HalloActivity implements EasyPermissions.PermissionCallbacks {

    public static final String EXTRA_NAV_TARGET = "nav_target";
    public static final String NAV_TARGET_FEED = "feed";
    public static final String NAV_TARGET_MESSAGES = "messages";

    private static final int REQUEST_CODE_ASK_CONTACTS_PERMISSION = 1;
    private static final int REQUEST_CODE_CAPTURE_IMAGE = 2;
    private static final int REQUEST_CODE_SELECT_CONTACT = 3;

    private SpeedDialView fabView;

    private final ContactsDb.Observer contactsObserver = new ContactsDb.Observer() {

        @Override
        public void onContactsChanged() {
        }

        @Override
        public void onContactsReset() {
            startActivity(new Intent(getBaseContext(), InitialSyncActivity.class));
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("MainActivity.onCreate");

        if (Build.VERSION.SDK_INT >= 28) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        getWindow().getDecorView().setSystemUiVisibility(SystemUiVisibility.getDefaultSystemUiVisibility(this));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        setContentView(R.layout.activity_main);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final BottomNavigationView navView = findViewById(R.id.nav_view);
        final AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home,
                R.id.navigation_messages,
                R.id.navigation_profile).build();
        final NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        final MenuItem messagesMenuItem = navView.getMenu().findItem(R.id.navigation_messages);
        final BadgedDrawable messageNotificationDrawable = new BadgedDrawable(
                this,
                messagesMenuItem.getIcon(),
                getResources().getColor(R.color.badge_text),
                getResources().getColor(R.color.badge_background),
                getResources().getColor(R.color.window_background),
                getResources().getDimension(R.dimen.badge));
        messagesMenuItem.setIcon(messageNotificationDrawable);

        final ViewGroup messagesTab = navView.findViewById(R.id.navigation_messages);
        messagesTab.setClipChildren(false);
        messagesTab.setClipToPadding(false);

        final MainViewModel viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        viewModel.unseenChatsCount.getLiveData().observe(this,
                unseenChatsCount -> messageNotificationDrawable.setBadge(
                        unseenChatsCount == null || unseenChatsCount == 0 ? "" : String.format(Locale.getDefault(), "%d", unseenChatsCount)));

        fabView = findViewById(R.id.speed_dial);
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

        ContactsDb.getInstance(this).addObserver(contactsObserver);
    }

    private void updateFab(@IdRes int id) {
        fabView.clearActionItems();
        if (id == R.id.navigation_messages) {
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
        } else {
            fabView.findViewById(R.id.sd_main_fab).setContentDescription(getString(R.string.add_post));
            fabView.setMainFabClosedDrawable(ContextCompat.getDrawable(this, R.drawable.ic_add));
            fabView.setOnChangeListener(null);
            fabView.setOnActionSelectedListener(actionItem -> {
                onFabActionSelected(actionItem.getId());
                return true;
            });
            addFabItem(fabView, R.id.add_post_gallery, R.drawable.ic_media_collection, R.string.gallery_post);
            addFabItem(fabView, R.id.add_post_camera, R.drawable.ic_camera, R.string.camera_post);
            addFabItem(fabView, R.id.add_post_text, R.drawable.ic_text, R.string.text_post);
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
        switch (id) {
            case R.id.add_post_text: {
                startActivity(new Intent(this, ContentComposerActivity.class));
                break;
            }
            case R.id.add_post_gallery: {
                final Intent intent = new Intent(this, MediaPickerActivity.class);
                intent.putExtra(MediaPickerActivity.EXTRA_PICKER_PURPOSE, MediaPickerActivity.PICKER_PURPOSE_SEND);
                startActivity(intent);
                break;
            }
            case R.id.add_post_camera: {
                final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, MediaUtils.getImageCaptureUri(this));
                startActivityForResult(intent, REQUEST_CODE_CAPTURE_IMAGE);
                break;
            }
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
        Log.i("MainActivity.onDestroy");
        ContactsDb.getInstance(this).removeObserver(contactsObserver);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("MainActivity.onStart");
        Notifications.getInstance(this).clearFeedNotifications();
        if (Connection.getInstance().clientExpired) {
            AppExpirationActivity.open(this, 0);
        }
        final CheckRegistrationTask checkRegistrationTask = new CheckRegistrationTask(Me.getInstance(this), Preferences.getInstance(this));
        checkRegistrationTask.result.observe(this, checkResult -> {
            if (!checkResult.registered) {
                Log.i("MainActivity.onStart: not registered");
                startActivity(new Intent(getBaseContext(), RegistrationRequestActivity.class));
                overridePendingTransition(0, 0);
                finish();
            } else if (checkResult.lastSyncTime <= 0) {
                Log.i("MainActivity.onStart: not synced");
                startActivity(new Intent(getBaseContext(), InitialSyncActivity.class));
                overridePendingTransition(0, 0);
                finish();
            } else {
                final String[] perms = {Manifest.permission.READ_CONTACTS};
                if (!EasyPermissions.hasPermissions(this, perms)) {
                    EasyPermissions.requestPermissions(this, getString(R.string.contacts_permission_rationale),
                            REQUEST_CODE_ASK_CONTACTS_PERMISSION, perms);
                }
            }
        });
        checkRegistrationTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("MainActivity.onStop");
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
    public void onActivityResult(final int request, final int result, final Intent data) {
        super.onActivityResult(request, result, data);
        //noinspection SwitchStatementWithTooFewBranches
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
                    startActivity(new Intent(this, ChatActivity.class).putExtra(ChatActivity.EXTRA_CHAT_ID, rawId));
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
        }
    }
}

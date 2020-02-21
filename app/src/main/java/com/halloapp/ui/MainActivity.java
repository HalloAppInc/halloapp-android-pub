package com.halloapp.ui;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.halloapp.BuildConfig;
import com.halloapp.Debug;
import com.halloapp.Me;
import com.halloapp.Notifications;
import com.halloapp.Preferences;
import com.halloapp.R;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.ContactsSync;
import com.halloapp.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private static final int REQUEST_CODE_ASK_CONTACTS_PERMISSION = 1;

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

        final CheckRegistrationTask checkRegistrationTask = new CheckRegistrationTask(Me.getInstance(this), Preferences.getInstance(this));
        checkRegistrationTask.result.observe(this, checkResult -> {
            if (!checkResult.registered) {
                Log.i("MainActivity.onCreate: not registered");
                startActivity(new Intent(getBaseContext(), RegistrationRequestActivity.class));
                overridePendingTransition(0, 0);
                finish();
            } else if (checkResult.lastSyncTime <= 0) {
                Log.i("MainActivity.onCreate: not synced");
                startActivity(new Intent(getBaseContext(), InitialSyncActivity.class));
                overridePendingTransition(0, 0);
                finish();
            }
        });
        checkRegistrationTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        setContentView(R.layout.activity_main);

        getWindow().getDecorView().setSystemUiVisibility(SystemUiVisibility.getDefaultSystemUiVisibility(this));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

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


        final String[] perms = {Manifest.permission.READ_CONTACTS};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, getString(R.string.contacts_permission_rationale),
                    REQUEST_CODE_ASK_CONTACTS_PERMISSION, perms);
        }

        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == 0 && savedInstanceState == null) {
            // The activity was not launched from history
            processIntent(getIntent());
        }

        ContactsDb.getInstance(this).addObserver(contactsObserver);
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
        Notifications.getInstance(this).clear();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("MainActivity.onStop");
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
                ContactsSync.getInstance(this).startAddressBookSync();
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
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (BuildConfig.DEBUG && keyCode == KeyEvent.KEYCODE_BACK) {
            Debug.showDebugMenu(this, findViewById(R.id.nav_view));
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    private void processIntent(Intent intent) {
        if (Intent.ACTION_SEND.equals(intent.getAction())) {
            final Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (uri != null) {
                final Intent composerIntent = new Intent(this, PostComposerActivity.class);
                composerIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, new ArrayList<>(Collections.singleton(uri)));
                startActivity(composerIntent);
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(intent.getAction())) {
            final ArrayList<Uri> uris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            if (uris != null && !uris.isEmpty()) {
                final Intent composerIntent = new Intent(this, PostComposerActivity.class);
                composerIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                startActivity(composerIntent);
            }
        }
    }
}

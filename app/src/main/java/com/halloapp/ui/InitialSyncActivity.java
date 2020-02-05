package com.halloapp.ui;

import android.Manifest;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.halloapp.Me;
import com.halloapp.Preferences;
import com.halloapp.R;
import com.halloapp.contacts.ContactsSync;
import com.halloapp.util.Log;

import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class InitialSyncActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private static final int REQUEST_CODE_ASK_CONTACTS_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("InitialSyncActivity.onCreate");
        setContentView(R.layout.activity_initial_sync);

        final CheckRegistrationTask checkRegistrationTask = new CheckRegistrationTask(Me.getInstance(this), Preferences.getInstance(this));
        checkRegistrationTask.result.observe(this, checkResult -> {
            if (!checkResult.registered) {
                startActivity(new Intent(getBaseContext(), RegistrationRequestActivity.class));
                overridePendingTransition(0, 0);
                finish();
            } else if (checkResult.lastSyncTime > 0) {
                startActivity(new Intent(getBaseContext(), MainActivity.class));
                overridePendingTransition(0, 0);
                finish();
            }
        });
        checkRegistrationTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        final View infoView = findViewById(R.id.info);
        final View loadingView = findViewById(R.id.loading);
        final View errorView = findViewById(R.id.error);
        final View retryView = findViewById(R.id.retry);

        retryView.setOnClickListener(v -> {
            infoView.setVisibility(View.VISIBLE);
            loadingView.setVisibility(View.VISIBLE);
            errorView.setVisibility(View.GONE);
            retryView.setVisibility(View.GONE);
            tryStartSync();

        });

        WorkManager.getInstance(this).cancelUniqueWork(ContactsSync.CONTACT_SYNC_WORK_ID);

        WorkManager.getInstance(this).getWorkInfosForUniqueWorkLiveData(ContactsSync.CONTACT_SYNC_WORK_ID)
                .observe(this, workInfos -> {
                    if (workInfos != null && workInfos.size() > 0) {
                        final WorkInfo workInfo = workInfos.get(0);
                        if (workInfo.getId().equals(ContactsSync.getInstance(getBaseContext()).getLastSyncRequestId())) {
                            if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                                startActivity(new Intent(getBaseContext(), MainActivity.class));
                                ContactsSync.getInstance(getBaseContext()).startAddressBookListener();
                                finish();
                            } else if (workInfo.getState().isFinished()) {
                                infoView.setVisibility(View.GONE);
                                loadingView.setVisibility(View.GONE);
                                errorView.setVisibility(View.VISIBLE);
                                retryView.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });

        tryStartSync();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("InitialSyncActivity.onDestroy");
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
                startSync();
                break;
            }
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
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

    private void tryStartSync() {
        final String[] perms = {Manifest.permission.READ_CONTACTS};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, getString(R.string.contacts_permission_rationale),
                    REQUEST_CODE_ASK_CONTACTS_PERMISSION, perms);
        } else {
            startSync();
        }
    }

    private void startSync() {
        ContactsSync.getInstance(this).startContactSync();
    }
}

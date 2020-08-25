package com.halloapp.ui;

import android.Manifest;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.work.WorkInfo;

import com.halloapp.Me;
import com.halloapp.Preferences;
import com.halloapp.R;
import com.halloapp.contacts.ContactsSync;
import com.halloapp.content.LoadPostsHistoryWorker;
import com.halloapp.util.Log;

import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class InitialSyncActivity extends HalloActivity implements EasyPermissions.PermissionCallbacks, EasyPermissions.RationaleCallbacks {

    private static final int REQUEST_CODE_ASK_CONTACTS_PERMISSION = 1;

    private View infoView;
    private View loadingView;
    private View errorView;
    private View retryView;
    private View rationaleView;
    private View continueView;

    private boolean syncInFlight = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_sync);

        final CheckRegistrationTask checkRegistrationTask = new CheckRegistrationTask(Me.getInstance(), Preferences.getInstance());
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

        infoView = findViewById(R.id.info);
        loadingView = findViewById(R.id.loading);
        errorView = findViewById(R.id.error);
        retryView = findViewById(R.id.retry);
        rationaleView = findViewById(R.id.rationale);
        continueView = findViewById(R.id.cont);

        retryView.setOnClickListener(v -> {
            tryStartSync();
        });

        continueView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ContactsSync contactsSync = ContactsSync.getInstance(InitialSyncActivity.this);
                contactsSync.cancelContactsSync();
                contactsSync.getWorkInfoLiveData()
                        .observe(InitialSyncActivity.this, workInfos -> {
                            if (workInfos != null) {
                                for (WorkInfo workInfo : workInfos) {
                                    if (workInfo.getId().equals(contactsSync.getLastSyncRequestId())) {
                                        if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                                            startActivity(new Intent(getBaseContext(), MainActivity.class));
                                            LoadPostsHistoryWorker.loadPostsHistory(InitialSyncActivity.this);
                                            ContactsSync.getInstance(getBaseContext()).startAddressBookListener();
                                            finish();
                                        } else if (workInfo.getState().isFinished()) {
                                            syncInFlight = false;
                                            showRetryState();
                                        }
                                        break;
                                    }
                                }
                            }
                        });

                tryStartSync();
            }
        });
    }

    private void showRunningState() {
        infoView.setVisibility(View.VISIBLE);
        loadingView.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
        retryView.setVisibility(View.GONE);
        rationaleView.setVisibility(View.GONE);
        continueView.setVisibility(View.GONE);
    }

    private void showRetryState() {
        infoView.setVisibility(View.GONE);
        loadingView.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);
        retryView.setVisibility(View.VISIBLE);
        rationaleView.setVisibility(View.GONE);
        continueView.setVisibility(View.GONE);
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
            //noinspection SwitchStatementWithTooFewBranches
            switch (requestCode) {
                case REQUEST_CODE_ASK_CONTACTS_PERMISSION: {
                    if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
                        new AppSettingsDialog.Builder(this)
                                .setRationale(getString(R.string.contacts_permission_rationale_denied))
                                .build().show();
                    } else {
                        tryStartSync();
                    }
                    break;
                }
            }
    }

    @Override
    public void onRationaleAccepted(int requestCode) {

    }

    @Override
    public void onRationaleDenied(int requestCode) {
        showRetryState();
    }

    private void tryStartSync() {
        final String[] perms = {Manifest.permission.READ_CONTACTS};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, getString(R.string.contacts_permission_rationale),
                    REQUEST_CODE_ASK_CONTACTS_PERMISSION, perms);
        } else if (!syncInFlight) {
            startSync();
        }
    }

    private void startSync() {
        syncInFlight = true;
        showRunningState();
        ContactsSync.getInstance(this).startContactsSync(true);
    }
}

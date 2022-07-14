package com.halloapp.ui;

import android.Manifest;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.WorkInfo;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.halloapp.Constants;
import com.halloapp.Me;
import com.halloapp.Preferences;
import com.halloapp.R;
import com.halloapp.contacts.ContactsSync;
import com.halloapp.ui.contacts.ContactHashInfoBottomSheetDialogFragment;
import com.halloapp.util.DialogFragmentUtils;
import com.halloapp.util.StringUtils;
import com.halloapp.util.logs.LogProvider;
import com.halloapp.widget.NetworkIndicatorView;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class InitialSyncActivity extends HalloActivity implements EasyPermissions.PermissionCallbacks, EasyPermissions.RationaleCallbacks {

    private static final int REQUEST_CODE_ASK_CONTACTS_PERMISSION = 1;

    private FirebaseAnalytics firebaseAnalytics;

    private InitialSyncViewModel viewModel;

    private View loadingView;
    private View errorView;
    private View retryView;
    private TextView rationaleView;
    private View continueView;
    private View logoView;
    private View titleView;
    private View sendLogsButton;

    private boolean syncInFlight = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_sync);

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        viewModel = new ViewModelProvider(this).get(InitialSyncViewModel.class);

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

        loadingView = findViewById(R.id.loading);
        errorView = findViewById(R.id.error);
        retryView = findViewById(R.id.retry);
        rationaleView = findViewById(R.id.rationale);
        continueView = findViewById(R.id.cont);
        titleView = findViewById(R.id.title);
        logoView = findViewById(R.id.logo);
        sendLogsButton = findViewById(R.id.send_logs);

        rationaleView.setText(StringUtils.replaceLink(this, Html.fromHtml(getString(R.string.contact_rationale_upload)), "learn-more", () -> {
            DialogFragmentUtils.showDialogFragmentOnce(new ContactHashInfoBottomSheetDialogFragment(), getSupportFragmentManager());
        }));
        rationaleView.setMovementMethod(LinkMovementMethod.getInstance());
        retryView.setOnClickListener(v -> {
            tryStartSync();
        });

        continueView.setOnClickListener(v -> {
            final ContactsSync contactsSync = ContactsSync.getInstance();
            contactsSync.cancelContactsSync();
            contactsSync.getWorkInfoLiveData()
                    .observe(InitialSyncActivity.this, workInfos -> {
                        if (workInfos != null) {
                            for (WorkInfo workInfo : workInfos) {
                                if (workInfo.getId().equals(contactsSync.getLastSyncRequestId())) {
                                    if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                                        firebaseAnalytics.logEvent("initial_sync_completed", null);
                                        startActivity(new Intent(getBaseContext(), MainActivity.class));
                                        ContactsSync.getInstance().startAddressBookListener();
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
        });

        final NetworkIndicatorView indicatorView = findViewById(R.id.network_indicator);
        indicatorView.bind(this);

        viewModel.showSendLogs.observe(this, show -> {
            sendLogsButton.setVisibility(Boolean.TRUE.equals(show) ? View.VISIBLE : View.INVISIBLE);
        });
        sendLogsButton.setOnClickListener(v -> {
            ProgressDialog progressDialog = ProgressDialog.show(this, null, getString(R.string.preparing_logs));
            LogProvider.openLogIntent(this).observe(this, intent -> {
                startActivity(intent);
                progressDialog.dismiss();
            });
        });
    }

    private void showRunningState() {
        loadingView.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
        retryView.setVisibility(View.GONE);
        rationaleView.setVisibility(View.GONE);
        continueView.setVisibility(View.GONE);
        logoView.setVisibility(View.GONE);
        titleView.setVisibility(View.GONE);
    }

    private void showRetryState() {
        loadingView.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);
        retryView.setVisibility(View.VISIBLE);
        rationaleView.setVisibility(View.GONE);
        continueView.setVisibility(View.GONE);
        logoView.setVisibility(View.GONE);
        titleView.setVisibility(View.GONE);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> list) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (requestCode) {
            case REQUEST_CODE_ASK_CONTACTS_PERMISSION: {
                firebaseAnalytics.logEvent("contacts_granted", null);
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
        final String[] perms = {Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS};
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
        Preferences.getInstance().clearContactSyncBackoffTime();
        ContactsSync.getInstance().forceFullContactsSync(true);
    }

    public static class InitialSyncViewModel extends AndroidViewModel {

        public final MutableLiveData<Boolean> showSendLogs = new MutableLiveData<>(false);

        public InitialSyncViewModel(@NonNull Application application) {
            super(application);

            Timer timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    showSendLogs.postValue(true);
                }
            };
            timer.schedule(timerTask, Constants.SEND_LOGS_BUTTON_DELAY_MS);
        }
    }
}

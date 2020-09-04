package com.halloapp.ui;

import android.Manifest;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.work.WorkInfo;

import com.halloapp.Me;
import com.halloapp.Preferences;
import com.halloapp.R;
import com.halloapp.contacts.ContactsSync;
import com.halloapp.content.LoadPostsHistoryWorker;
import com.halloapp.ui.contacts.ContactHashInfoBottomSheetDialogFragment;
import com.halloapp.util.DialogFragmentUtils;

import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class InitialSyncActivity extends HalloActivity implements EasyPermissions.PermissionCallbacks, EasyPermissions.RationaleCallbacks {

    private static final int REQUEST_CODE_ASK_CONTACTS_PERMISSION = 1;

    private View loadingView;
    private View errorView;
    private View retryView;
    private TextView rationaleView;
    private View continueView;
    private View logoView;
    private View titleView;

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

        loadingView = findViewById(R.id.loading);
        errorView = findViewById(R.id.error);
        retryView = findViewById(R.id.retry);
        rationaleView = findViewById(R.id.rationale);
        continueView = findViewById(R.id.cont);
        titleView = findViewById(R.id.title);
        logoView = findViewById(R.id.logo);


        SpannableStringBuilder current= new SpannableStringBuilder(rationaleView.getText());
        URLSpan[] spans= current.getSpans(0, current.length(), URLSpan.class);

        for (URLSpan span : spans) {
            int start = current.getSpanStart(span);
            int end = current.getSpanEnd(span);
            current.removeSpan(span);

            ClickableSpan learnMoreSpan = new ClickableSpan() {
                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    ds.setUnderlineText(false);
                    ds.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
                }

                @Override
                public void onClick(@NonNull View widget) {
                    DialogFragmentUtils.showDialogFragmentOnce(new ContactHashInfoBottomSheetDialogFragment(), getSupportFragmentManager());
                    Selection.removeSelection((Spannable)rationaleView.getText());
                }
            };
            current.setSpan(learnMoreSpan, start, end, 0);
        }
        rationaleView.setText(current);
        rationaleView.setMovementMethod(LinkMovementMethod.getInstance());
        retryView.setOnClickListener(v -> {
            tryStartSync();
        });

        continueView.setOnClickListener(v -> {
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

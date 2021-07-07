package com.halloapp.ui;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;
import com.halloapp.AppContext;
import com.halloapp.Constants;
import com.halloapp.Notifications;
import com.halloapp.Preferences;
import com.halloapp.R;
import com.halloapp.contacts.ContactsSync;
import com.halloapp.registration.Registration;
import com.halloapp.registration.SmsVerificationManager;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.Preconditions;
import com.halloapp.util.StringUtils;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.SnackbarHelper;
import com.hbb20.CountryCodePicker;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;


public class RegistrationRequestActivity extends HalloActivity {

    public static final String EXTRA_RE_VERIFY = "reverify";

    private static final int REQUEST_CODE_VERIFICATION = 1;
    private static final long INSTALL_REFERRER_TIMEOUT_MS = 2000;

    private final BgWorkers bgWorkers = BgWorkers.getInstance();
    private final SmsVerificationManager smsVerificationManager = SmsVerificationManager.getInstance();

    private RegistrationRequestViewModel registrationRequestViewModel;

    private CountryCodePicker countryCodePicker;
    private EditText phoneNumberEditText;
    private EditText nameEditText;
    private View nextButton;
    private View loadingProgressBar;
    private Preferences preferences;
    private ContactsSync contactsSync;
    private AvatarLoader avatarLoader;

    private boolean isReverification = false;

    public static void reVerify(final Context context) {
        context.startActivity(new Intent(context, RegistrationRequestActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(RegistrationRequestActivity.EXTRA_RE_VERIFY, true));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_request);

        preferences = Preferences.getInstance();
        contactsSync = ContactsSync.getInstance();
        avatarLoader = AvatarLoader.getInstance();

        nameEditText = findViewById(R.id.name);
        phoneNumberEditText = findViewById(R.id.phone_number);
        countryCodePicker = findViewById(R.id.ccp);
        countryCodePicker.registerCarrierNumberEditText(phoneNumberEditText);
        countryCodePicker.useFlagEmoji(Build.VERSION.SDK_INT >= 28);
        loadingProgressBar = findViewById(R.id.loading);
        nextButton = findViewById(R.id.next);

        final TextView titleView = findViewById(R.id.title);
        isReverification = getIntent().getBooleanExtra(EXTRA_RE_VERIFY, false);
        if (isReverification) {
            titleView.setVisibility(View.VISIBLE);
            titleView.setText(R.string.reverify_registration_title);
            findViewById(R.id.name_layout).setVisibility(View.GONE);
        } else {
            titleView.setVisibility(View.GONE);
        }
        Notifications.getInstance(this).clearLoginFailedNotification();

        phoneNumberEditText.setTextColor(phoneNumberEditText.getCurrentTextColor()); // so phoneNumberEditText.setEnabled(false) doesn't change color

        registrationRequestViewModel = new ViewModelProvider(this).get(RegistrationRequestViewModel.class);
        registrationRequestViewModel.getRegistrationRequestResult().observe(this, result -> {
            if (result == null) {
                return;
            }

            if (result.result == Registration.RegistrationRequestResult.RESULT_OK) {
                final Intent intent = new Intent(this, RegistrationVerificationActivity.class);
                intent.putExtra(RegistrationVerificationActivity.EXTRA_PHONE_NUMBER, result.phone);
                intent.putExtra(RegistrationVerificationActivity.EXTRA_RETRY_WAIT_TIME, result.retryWaitTimeSeconds);
                intent.putExtra(RegistrationVerificationActivity.EXTRA_GROUP_INVITE_TOKEN, registrationRequestViewModel.groupInviteToken);
                startActivityForResult(intent, REQUEST_CODE_VERIFICATION);
            } else {
                if (result.result == Registration.RegistrationRequestResult.RESULT_FAILED_SERVER_NO_FRIENDS
                        || result.result == Registration.RegistrationRequestResult.RESULT_FAILED_SERVER_NOT_INVITED) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(RegistrationRequestActivity.this)
                        .setMessage(getBaseContext().getString(R.string.registration_failed_no_friends))
                        .setTitle(getBaseContext().getString(R.string.registration_failed_no_friends_title))
                        .setPositiveButton(R.string.ok, null)
                        .setCancelable(true);
                    builder.show();
                } else if (result.result == Registration.RegistrationRequestResult.RESULT_FAILED_CLIENT_EXPIRED) {
                    AppExpirationActivity.open(this, 0);
                    finish();
                } else if (result.result == Registration.RegistrationRequestResult.RESULT_FAILED_RETRIED_TOO_SOON && result.phone != null) {
                    final Intent intent = new Intent(this, RegistrationVerificationActivity.class);
                    intent.putExtra(RegistrationVerificationActivity.EXTRA_PHONE_NUMBER, result.phone);
                    intent.putExtra(RegistrationVerificationActivity.EXTRA_RETRY_WAIT_TIME, result.retryWaitTimeSeconds);
                    intent.putExtra(RegistrationVerificationActivity.EXTRA_GROUP_INVITE_TOKEN, registrationRequestViewModel.groupInviteToken);
                    startActivityForResult(intent, REQUEST_CODE_VERIFICATION);
                } else {
                    SnackbarHelper.showInfo(this, R.string.registration_failed);
                }
                nextButton.setVisibility(View.VISIBLE);
                countryCodePicker.setCcpClickable(true);
            }
            loadingProgressBar.setVisibility(View.INVISIBLE );
        });

        phoneNumberEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                startRegistrationRequest();
            }
            return false;
        });
        phoneNumberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updateNextButton();
            }
        });

        final TextView counterView = findViewById(R.id.counter);
        nameEditText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(Constants.MAX_NAME_LENGTH)});
        nameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                counterView.setText(getString(R.string.counter, s.length(), Constants.MAX_NAME_LENGTH));
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateNextButton();
            }
        });
        nameEditText.requestFocus();

        findViewById(R.id.next).setOnClickListener(v -> startRegistrationRequest());

        updateNextButton();
    }

    private void updateNextButton() {
        boolean nameValid = isReverification || !TextUtils.isEmpty(nameEditText.getText().toString());
        nextButton.setEnabled(nameValid && countryCodePicker.isValidFullNumber());
    }

    @Override
    public void onActivityResult(final int request, final int result, final Intent data) {
        super.onActivityResult(request, result, data);
        //noinspection SwitchStatementWithTooFewBranches
        switch (request) {
            case REQUEST_CODE_VERIFICATION: {
                if (result == RESULT_OK) {
                    startActivity(new Intent(this, MainActivity.class));
                    if (preferences.getLastContactsSyncTime() > 0) {
                        onRereg();
                    }
                    finish();
                } else {
                    nextButton.setVisibility(View.VISIBLE);
                    phoneNumberEditText.setEnabled(true);
                    countryCodePicker.setCcpClickable(true);
                }
                break;
            }
        }
    }

    private void onRereg() {
        Log.i("RegistrationRequestActivity: rereg success");
        contactsSync.startContactsSync(true);
        bgWorkers.execute(() -> {
            preferences.setLastGroupSyncTime(0);
            avatarLoader.removeMyAvatar();
        });
    }

    private void startRegistrationRequest() {
        boolean reverify = getIntent().getBooleanExtra(EXTRA_RE_VERIFY, false);
        final String name;
        if (reverify) {
            name = null;
        } else {
            name = StringUtils.preparePostText(Preconditions.checkNotNull(nameEditText.getText()).toString());
            if (TextUtils.isEmpty(name)) {
                SnackbarHelper.showInfo(this, R.string.name_must_be_specified);
                nameEditText.requestFocus();
                return;
            }
        }
        if (!countryCodePicker.isValidFullNumber()) {
            SnackbarHelper.showInfo(this, R.string.invalid_phone_number);
            phoneNumberEditText.requestFocus();
            return;
        }

        loadingProgressBar.setVisibility(View.VISIBLE);
        nextButton.setVisibility(View.INVISIBLE);
        countryCodePicker.setCcpClickable(false);
        Log.i("RegistrationRequestActivity.startRegistrationRequest for " + countryCodePicker.getFullNumber());

        smsVerificationManager.start(getApplicationContext());
        registrationRequestViewModel.requestRegistration(countryCodePicker.getFullNumber(), name);
    }

    public static class RegistrationRequestViewModel extends AndroidViewModel {

        private final BgWorkers bgWorkers = BgWorkers.getInstance();
        private final Registration registration = Registration.getInstance();

        private final MutableLiveData<Registration.RegistrationRequestResult> registrationRequestResult = new MutableLiveData<>();

        private String groupInviteToken;

        public RegistrationRequestViewModel(@NonNull Application application) {
            super(application);
        }

        LiveData<Registration.RegistrationRequestResult> getRegistrationRequestResult() {
            return registrationRequestResult;
        }

        void requestRegistration(@NonNull String phone, @Nullable String name) {
            InstallReferrerClient referrerClient = InstallReferrerClient.newBuilder(AppContext.getInstance().get().getApplicationContext()).build();

            AtomicBoolean registerCalled = new AtomicBoolean(false);
            Timer timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    if (registerCalled.compareAndSet(false, true)) {
                        Log.i("RegistrationRequestViewModel InstallReferrer took too long");
                        registrationRequestResult.postValue(registration.registerPhoneNumber(name, phone, null));
                        referrerClient.endConnection();
                    }
                }
            };
            timer.schedule(timerTask, INSTALL_REFERRER_TIMEOUT_MS);

            referrerClient.startConnection(new InstallReferrerStateListener() {
                @Override
                public void onInstallReferrerSetupFinished(int responseCode) {
                    String inviteCode = null;
                    switch (responseCode) {
                        case InstallReferrerClient.InstallReferrerResponse.OK:
                            try {
                                ReferrerDetails details = referrerClient.getInstallReferrer();
                                inviteCode = details.getInstallReferrer();
                                if (!TextUtils.isEmpty(inviteCode) && inviteCode.startsWith("ginvite-")) {
                                    inviteCode = inviteCode.substring(8);
                                } else {
                                    Log.w("RegistrationRequestActivity/requestRegistration no referrer invite");
                                }
                            } catch (RemoteException e) {
                                Log.e("RegistrationRequestActivity/requestRegistration failed to get install referrer", e);
                            }
                            break;
                        case InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED:
                            Log.w("RegistrationRequestActivity/requestRegistration referrer not available");
                            break;
                        case InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE:
                            Log.w("RegistrationRequestActivity/requestRegistration no connection");
                            break;
                    }
                    final String code = inviteCode;
                    groupInviteToken = code;
                    bgWorkers.execute(() -> {
                        if (registerCalled.compareAndSet(false, true)) {
                            registrationRequestResult.postValue(registration.registerPhoneNumber(name, phone, code));
                            referrerClient.endConnection();
                        }
                    });
                }

                @Override
                public void onInstallReferrerServiceDisconnected() {
                }
            });
        }
    }

}

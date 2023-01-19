package com.halloapp;

import android.app.Application;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.text.method.DigitsKeyListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.halloapp.katchup.Analytics;
import com.halloapp.registration.Registration;
import com.halloapp.registration.SmsVerificationManager;
import com.halloapp.ui.DebouncedClickListener;
import com.halloapp.ui.HalloActivity;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.Preconditions;
import com.halloapp.util.ViewUtils;
import com.halloapp.util.logs.Log;
import com.halloapp.util.logs.LogProvider;
import com.halloapp.widget.NetworkIndicatorView;
import com.halloapp.widget.SnackbarHelper;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class RegistrationVerificationActivity extends HalloActivity {

    public static final String EXTRA_PHONE_NUMBER = "phone_number";
    public static final String EXTRA_RETRY_WAIT_TIME = "retry_wait_time";
    public static final String EXTRA_GROUP_INVITE_TOKEN = "group_invite_token";
    public static final String EXTRA_CAMPAIGN_ID = "campaign_id";

    private static final int CODE_LENGTH = 6;
    private static final long HASHCASH_MAX_WAIT_MS = 60_000;

    private final SmsVerificationManager smsVerificationManager = SmsVerificationManager.getInstance();

    private FirebaseAnalytics firebaseAnalytics;

    private RegistrationVerificationViewModel registrationVerificationViewModel;

    private TextView codeEditHint;
    private EditText codeEditText;
    private View loadingProgressBar;
    private View sendLogsButton;

    private View successView;
    private View otpRequestView;

    private String campaignId;
    private String groupInviteToken;

    private final SmsVerificationManager.Observer smsVerificationObserver = new SmsVerificationManager.Observer() {

        @Override
        public void onVerificationSmsReceived(String code) {
            Log.i("RegistrationVerificationActivity.smsVerificationObserver.onVerificationSmsReceived: " + code);
            codeEditText.setText(code);
        }

        @Override
        public void onVerificationSmsFailed() {
            Log.w("RegistrationVerificationActivity.smsVerificationObserver.onVerificationSmsFailed");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_verification);

        final NetworkIndicatorView indicatorView = findViewById(R.id.network_indicator);
        indicatorView.bind(this);

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        codeEditHint = findViewById(R.id.code_hint);
        codeEditText = findViewById(R.id.code);
        loadingProgressBar = findViewById(R.id.loading);
        sendLogsButton = findViewById(R.id.send_logs);

        successView = findViewById(R.id.success);
        otpRequestView = findViewById(R.id.otp_request_container);

        View wrongNumber = findViewById(R.id.wrong_number);
        wrongNumber.setOnClickListener(v -> {
            onBackPressed();
        });

        registrationVerificationViewModel = new ViewModelProvider(this).get(RegistrationVerificationViewModel.class);
        registrationVerificationViewModel.getRegistrationVerificationResult().observe(this, result -> {
            if (result == null) {
                return;
            }
            if (result.result == Registration.RegistrationVerificationResult.RESULT_OK) {
                Analytics.getInstance().logOnboardingEnteredOtp(true, null);
                AutoTransition autoTransition = new AutoTransition();
                autoTransition.setDuration(300);
                TransitionManager.beginDelayedTransition((ViewGroup) successView.getParent(), autoTransition);
                successView.setVisibility(View.VISIBLE);
                otpRequestView.setVisibility(View.GONE);
                firebaseAnalytics.logEvent("otp_accepted", null);
                successView.postDelayed(() -> {
                    setResult(RESULT_OK);
                    finish();
                }, 1000);
            } else {
                SnackbarHelper.showWarning(this, R.string.registration_code_invalid);
                Analytics.getInstance().logOnboardingEnteredOtp(false, result.reason);
                codeEditText.setText("");
                codeEditText.setEnabled(true);
                codeEditText.requestFocus();
                final InputMethodManager imm = Preconditions.checkNotNull((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE));
                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,0);
            }
            loadingProgressBar.setVisibility(View.GONE);
        });


        groupInviteToken = getIntent().getStringExtra(EXTRA_GROUP_INVITE_TOKEN);
        campaignId = getIntent().getStringExtra(EXTRA_CAMPAIGN_ID);
        final String phoneNumber = Preconditions.checkNotNull(getIntent().getStringExtra(EXTRA_PHONE_NUMBER));
        Log.i("RegistrationVerificationActivity got phone number " + phoneNumber);
        final String formattedNumber = PhoneNumberUtils.formatNumber("+" + phoneNumber, null);
        Log.i("RegistrationVerificationActivity formatted number " + formattedNumber);
        final String nonBreakingNumber = "\u202A" + formattedNumber + "\u202C";
        final TextView titleView = findViewById(R.id.title);
        titleView.setText(getString(R.string.verify_registration_title, nonBreakingNumber));

        codeEditText.setFilters(new InputFilter[]{
                new DigitsKeyListener(),
                new InputFilter.LengthFilter(CODE_LENGTH)
        });
        codeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String code = s.toString().replaceAll("[^\\d.]", "");
                if (code.length() == CODE_LENGTH) {
                    startVerification(phoneNumber, code);
                }
                codeEditHint.setVisibility(s.length() > 0 ? View.INVISIBLE : View.VISIBLE);
            }
        });
        codeEditText.requestFocus();

        final String lastSmsCode = smsVerificationManager.getLastReceivedCode();
        if (!TextUtils.isEmpty(lastSmsCode)) {
            codeEditText.setText(lastSmsCode);
        }
        smsVerificationManager.addObserver(smsVerificationObserver);

        TextView smsCounter = findViewById(R.id.resent_sms_timer);
        TextView callCounter = findViewById(R.id.call_me_timer);

        View callMe = findViewById(R.id.call_me);
        callMe.setOnClickListener(new DebouncedClickListener() {
            @Override
            public void onOneClick(@NonNull View view) {
                firebaseAnalytics.logEvent("extra_otp_call", null);
                Analytics.getInstance().logOnboardingRequestOTPCall();
                ProgressDialog progressDialog = ProgressDialog.show(RegistrationVerificationActivity.this, null, getString(R.string.registration_phone_code_progress));
                registrationVerificationViewModel.requestCall(phoneNumber, groupInviteToken, campaignId).observe(RegistrationVerificationActivity.this, result -> {
                    if (result != null) {
                        progressDialog.dismiss();
                        registrationVerificationViewModel.updateCallRetry(result.retryWaitTimeSeconds);
                    }
                });
            }
        });

        View smsMe = findViewById(R.id.resend_sms);
        smsMe.setOnClickListener(new DebouncedClickListener() {
            @Override
            public void onOneClick(@NonNull View view) {
                firebaseAnalytics.logEvent("extra_otp_sms", null);
                Analytics.getInstance().logOnboardingResendOTP();
                ProgressDialog progressDialog = ProgressDialog.show(RegistrationVerificationActivity.this, null, getString(R.string.registration_sms_retry_progress));
                registrationVerificationViewModel.requestSms(phoneNumber, groupInviteToken, campaignId).observe(RegistrationVerificationActivity.this, result -> {
                    if (result != null) {
                        progressDialog.dismiss();
                        registrationVerificationViewModel.updateSMSRetry(result.retryWaitTimeSeconds);
                    }
                });
            }
        });

        int waitTimeSeconds = getIntent().getIntExtra(EXTRA_RETRY_WAIT_TIME, 0);
        registrationVerificationViewModel.updateSMSRetry(waitTimeSeconds);

        registrationVerificationViewModel.getCallRetryWait().observe(this, callWait -> {
            if (callWait == null || callWait == 0) {
                ViewUtils.setViewAndChildrenEnabled(callMe, true);
                callCounter.setVisibility(View.GONE);
            } else {
                ViewUtils.setViewAndChildrenEnabled(callMe, false);
                callCounter.setVisibility(View.VISIBLE);
                callCounter.setText(DateUtils.formatElapsedTime(callWait));
            }
        });

        registrationVerificationViewModel.getSmsRetryWait().observe(this, smsWait -> {
            if (smsWait == null || smsWait == 0) {
                ViewUtils.setViewAndChildrenEnabled(smsMe, true);
                smsCounter.setVisibility(View.GONE);
            } else {
                ViewUtils.setViewAndChildrenEnabled(smsMe, false);
                smsCounter.setVisibility(View.VISIBLE);
                smsCounter.setText(DateUtils.formatElapsedTime(smsWait));
            }
        });

        registrationVerificationViewModel.showSendLogs.observe(this, show -> {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        smsVerificationManager.removeObserver(smsVerificationObserver);
    }

    private void startVerification(@NonNull String phone, @NonNull String code) {
        firebaseAnalytics.logEvent("otp_inputted", null);
        loadingProgressBar.setVisibility(View.VISIBLE);
        codeEditText.setEnabled(false);
        registrationVerificationViewModel.verifyRegistration(phone, code, campaignId, groupInviteToken);
    }

    public static class RegistrationVerificationViewModel extends AndroidViewModel {

        public final MutableLiveData<Boolean> showSendLogs = new MutableLiveData<>(false);

        private final BgWorkers bgWorkers = BgWorkers.getInstance();
        private final Registration registration = Registration.getInstance();

        private final MutableLiveData<Registration.RegistrationVerificationResult> registrationRequestResult = new MutableLiveData<>();

        private final MutableLiveData<Integer> callRetryWaitSeconds = new MutableLiveData<>();
        private final MutableLiveData<Integer> smsRetryWaitSeconds = new MutableLiveData<>();

        private CountDownLatch callHashcashLatch = new CountDownLatch(1);
        private Registration.HashcashResult callHashcashResult;
        private CountDownLatch smsHashcashLatch = new CountDownLatch(1);
        private Registration.HashcashResult smsHashcashResult;

        private CountDownTimer smsCountDownTimer;
        private CountDownTimer callCountDownTimer;

        public RegistrationVerificationViewModel(@NonNull Application application) {
            super(application);

            Timer timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    showSendLogs.postValue(true);
                }
            };
            timer.schedule(timerTask, Constants.SEND_LOGS_BUTTON_DELAY_MS);

            runSmsHashcash();
            runCallHashcash();
        }

        private void runCallHashcash() {
            bgWorkers.execute(() -> {
                callHashcashResult = registration.getHashcashSolution();
                if (callHashcashResult.result != Registration.HashcashResult.RESULT_OK) {
                    Log.e("RegistrationVerificationActivity/runCallHashcash Got hashcash failure " + callHashcashResult.result);
                    Log.sendErrorReport("Hashcash failed");
                }
                callHashcashLatch.countDown();
            });
        }

        private void runSmsHashcash() {
            bgWorkers.execute(() -> {
                smsHashcashResult = registration.getHashcashSolution();
                if (smsHashcashResult.result != Registration.HashcashResult.RESULT_OK) {
                    Log.e("RegistrationVerificationActivity/runSmsHashcash Got hashcash failure " + smsHashcashResult.result);
                    Log.sendErrorReport("Hashcash failed");
                }
                smsHashcashLatch.countDown();
            });
        }

        LiveData<Registration.RegistrationVerificationResult> getRegistrationVerificationResult() {
            return registrationRequestResult;
        }

        void verifyRegistration(@NonNull String phone, @NonNull String code, @Nullable String campaignId, @Nullable String groupInviteToken) {
            new RegistrationVerificationTask(this, phone, code, campaignId, groupInviteToken).execute();
        }

        LiveData<Integer> getCallRetryWait() {
            return callRetryWaitSeconds;
        }

        LiveData<Integer> getSmsRetryWait() {
            return smsRetryWaitSeconds;
        }

        @UiThread
        public void updateSMSRetry(int retryWait) {
            smsRetryWaitSeconds.postValue(retryWait);

            synchronized (this) {
                if (smsCountDownTimer == null && retryWait > 0) {
                    smsCountDownTimer = new CountDownTimer(retryWait * 1000L, 1000L) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            int seconds = (int) (millisUntilFinished / 1000);
                            smsRetryWaitSeconds.postValue(seconds);
                        }

                        @Override
                        public void onFinish() {
                            synchronized (RegistrationVerificationViewModel.this) {
                                smsCountDownTimer = null;
                                updateSMSRetry(0);
                            }
                        }
                    };
                    smsCountDownTimer.start();
                }
            }
        }

        @UiThread
        public void updateCallRetry(int retryWait) {
            callRetryWaitSeconds.postValue(retryWait);

            synchronized (this) {
                if (callCountDownTimer == null && retryWait > 0) {
                    callCountDownTimer = new CountDownTimer(retryWait * 1000L, 1000L) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            int seconds = (int) (millisUntilFinished / 1000);
                            callRetryWaitSeconds.postValue(seconds);
                        }

                        @Override
                        public void onFinish() {
                            synchronized (RegistrationVerificationViewModel.this) {
                                callCountDownTimer = null;
                                updateCallRetry(0);
                            }
                        }
                    };
                    callCountDownTimer.start();
                }
            }
        }


        public LiveData<Registration.RegistrationRequestResult> requestSms(String phone, @Nullable String token, @Nullable String campaignId) {
            MutableLiveData<Registration.RegistrationRequestResult> result = new MutableLiveData<>();
            bgWorkers.execute(() -> {
                try {
                    smsHashcashLatch.await(HASHCASH_MAX_WAIT_MS, TimeUnit.MILLISECONDS);
                    Log.i("RegistrationVerificationActivity/requestSms done waiting for hashcashLatch");
                } catch (InterruptedException e) {
                    Log.e("Interrupted while waiting for hashcash", e);
                }
                Registration.RegistrationRequestResult requestResult = registration.requestRegistration(phone, token, campaignId, smsHashcashResult);
                Log.i("RegistrationVerificationActivity/requestSms request sent; restarting hashcash");
                smsHashcashLatch = new CountDownLatch(1);
                smsHashcashResult = null;
                runSmsHashcash();
                result.postValue(requestResult);
            });
            return result;
        }

        public LiveData<Registration.RegistrationRequestResult> requestCall(String phone, @Nullable String token, @Nullable String campaignId) {
            MutableLiveData<Registration.RegistrationRequestResult> result = new MutableLiveData<>();
            bgWorkers.execute(() -> {
                try {
                    callHashcashLatch.await(HASHCASH_MAX_WAIT_MS, TimeUnit.MILLISECONDS);
                    Log.i("RegistrationVerificationActivity/requestCall done waiting for hashcashLatch");
                } catch (InterruptedException e) {
                    Log.e("Interrupted while waiting for hashcash", e);
                }
                Registration.RegistrationRequestResult requestResult = registration.requestRegistrationViaVoiceCall(phone, token, campaignId, callHashcashResult);
                Log.i("RegistrationVerificationActivity/requestCall request sent; restarting hashcash");
                callHashcashLatch = new CountDownLatch(1);
                callHashcashResult = null;
                runCallHashcash();
                result.postValue(requestResult);
            });
            return result;
        }
    }

    private static class RegistrationVerificationTask extends AsyncTask<Void, Void, Registration.RegistrationVerificationResult> {

        final RegistrationVerificationViewModel viewModel;
        final String phone;
        final String code;
        final String campaignId;
        final String groupInviteToken;

        RegistrationVerificationTask(@NonNull RegistrationVerificationViewModel viewModel, @NonNull String phone, @NonNull String code, @Nullable String campaignId, @Nullable String groupInviteToken) {
            this.viewModel = viewModel;
            this.phone = phone;
            this.code = code;
            this.campaignId = campaignId;
            this.groupInviteToken = groupInviteToken;
        }

        @Override
        protected Registration.RegistrationVerificationResult doInBackground(Void... voids) {
            return viewModel.registration.verifyPhoneNumber(phone, code, campaignId, groupInviteToken);
        }

        @Override
        protected void onPostExecute(final Registration.RegistrationVerificationResult result) {
            viewModel.registrationRequestResult.setValue(result);
        }
    }
}

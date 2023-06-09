package com.halloapp.katchup;

import android.app.Application;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.text.method.DigitsKeyListener;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.Me;
import com.halloapp.R;
import com.halloapp.registration.Registration;
import com.halloapp.registration.SmsVerificationManager;
import com.halloapp.ui.DebouncedClickListener;
import com.halloapp.ui.HalloActivity;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.Preconditions;
import com.halloapp.util.ViewUtils;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.SnackbarHelper;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ProfileVerifyPhoneActivity extends HalloActivity {

    public static final String EXTRA_PHONE = "phone";
    public static final String EXTRA_RETRY_WAIT_TIME = "retry_wait_time";
    private static final int CODE_LENGTH = 6;
    private static final long HASHCASH_MAX_WAIT_MS = 60_000;

    private TextView codeEditHint;
    private TextView codeEditText;
    private View otpRequestView;

    private final SmsVerificationManager smsVerificationManager = SmsVerificationManager.getInstance();

    private ProfileVerifyPhoneViewModel profileVerifyPhoneViewModel;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile_verify_phone);

        codeEditHint = findViewById(R.id.code_hint);
        codeEditText = findViewById(R.id.code);
        otpRequestView = findViewById(R.id.otp_request_container);

        View wrongNumber = findViewById(R.id.wrong_number);
        wrongNumber.setOnClickListener(v -> {
            onBackPressed();
        });

        View prev = findViewById(R.id.prev);
        prev.setOnClickListener(v -> finish());

        profileVerifyPhoneViewModel = new ViewModelProvider(this).get(ProfileVerifyPhoneViewModel.class);
        profileVerifyPhoneViewModel.getRegistrationVerificationResult().observe(this, result -> {
            if (result == null) {
                return;
            }
            if (result.result == Registration.RegistrationVerificationResult.RESULT_OK) {
                Analytics.getInstance().addedPhone();
                setResult(RESULT_OK);
                finish();
            } else {
                SnackbarHelper.showWarning(this, R.string.registration_code_invalid);
                codeEditText.setText("");
                codeEditText.setEnabled(true);
                codeEditText.requestFocus();
                final InputMethodManager imm = Preconditions.checkNotNull((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE));
                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,0);
            }
        });

        final String phoneNumber = Preconditions.checkNotNull(getIntent().getStringExtra(EXTRA_PHONE));

        codeEditText.setFilters(new InputFilter[]{
                new DigitsKeyListener(),
                new InputFilter.LengthFilter(CODE_LENGTH)
        });
        codeEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

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
                ProgressDialog progressDialog = ProgressDialog.show(ProfileVerifyPhoneActivity.this, null, getString(R.string.registration_phone_code_progress));
                profileVerifyPhoneViewModel.requestCall(phoneNumber).observe(ProfileVerifyPhoneActivity.this, result -> {
                    if (result != null) {
                        progressDialog.dismiss();
                        profileVerifyPhoneViewModel.updateCallRetry(result.retryWaitTimeSeconds);
                    }
                });
            }
        });

        View smsMe = findViewById(R.id.resend_sms);
        smsMe.setOnClickListener(new DebouncedClickListener() {

            @Override
            public void onOneClick(@NonNull View view) {
                ProgressDialog progressDialog = ProgressDialog.show(ProfileVerifyPhoneActivity.this, null, getString(R.string.registration_sms_retry_progress));
                profileVerifyPhoneViewModel.requestSms(phoneNumber).observe(ProfileVerifyPhoneActivity.this, result -> {
                    if (result != null) {
                        progressDialog.dismiss();
                        profileVerifyPhoneViewModel.updateSMSRetry(result.retryWaitTimeSeconds);
                    }
                });
            }
        });

        int waitTimeSeconds = getIntent().getIntExtra(EXTRA_RETRY_WAIT_TIME, 0);
        profileVerifyPhoneViewModel.updateSMSRetry(waitTimeSeconds);

        profileVerifyPhoneViewModel.getCallRetryWait().observe(this, callWait -> {
            if (callWait == null || callWait == 0) {
                ViewUtils.setViewAndChildrenEnabled(callMe, true);
                callCounter.setVisibility(View.GONE);
            } else {
                ViewUtils.setViewAndChildrenEnabled(callMe, false);
                callCounter.setVisibility(View.VISIBLE);
                callCounter.setText(DateUtils.formatElapsedTime(callWait));
            }
        });

        profileVerifyPhoneViewModel.getSmsRetryWait().observe(this, smsWait -> {
            if (smsWait == null || smsWait == 0) {
                ViewUtils.setViewAndChildrenEnabled(smsMe, true);
                smsCounter.setVisibility(View.GONE);
            } else {
                ViewUtils.setViewAndChildrenEnabled(smsMe, false);
                smsCounter.setVisibility(View.VISIBLE);
                smsCounter.setText(DateUtils.formatElapsedTime(smsWait));
            }
        });
    }


    private final SmsVerificationManager.Observer smsVerificationObserver = new SmsVerificationManager.Observer() {

        @Override
        public void onVerificationSmsReceived(String code) {
            Log.i("ProfileVerifyPhoneActivity.smsVerificationObserver.onVerificationSmsReceived: " + code);
            codeEditText.setText(code);
        }

        @Override
        public void onVerificationSmsFailed() {
            Log.w("ProfileVerifyPhoneActivity.smsVerificationObserver.onVerificationSmsFailed");
        }
    };

    private void startVerification(@NonNull String phone, @NonNull String code) {
        codeEditText.setEnabled(false);
        profileVerifyPhoneViewModel.verifyRegistration(phone, code);
    }

    public static class ProfileVerifyPhoneViewModel extends AndroidViewModel {

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

        public ProfileVerifyPhoneViewModel(@NonNull Application application) {
            super(application);

            runSmsHashcash();
            runCallHashcash();
        }

        private void runCallHashcash() {
            bgWorkers.execute(() -> {
                callHashcashResult = registration.getHashcashSolution();
                if (callHashcashResult.result != Registration.HashcashResult.RESULT_OK) {
                    Log.e("ProfileVerifyPhoneActivity/runCallHashcash Got hashcash failure " + callHashcashResult.result);
                    Log.sendErrorReport("Hashcash failed");
                }
                callHashcashLatch.countDown();
            });
        }

        private void runSmsHashcash() {
            bgWorkers.execute(() -> {
                smsHashcashResult = registration.getHashcashSolution();
                if (smsHashcashResult.result != Registration.HashcashResult.RESULT_OK) {
                    Log.e("ProfileVerifyPhoneActivity/runSmsHashcash Got hashcash failure " + smsHashcashResult.result);
                    Log.sendErrorReport("Hashcash failed");
                }
                smsHashcashLatch.countDown();
            });
        }

        LiveData<Registration.RegistrationVerificationResult> getRegistrationVerificationResult() {
            return registrationRequestResult;
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
                            synchronized (ProfileVerifyPhoneViewModel.this) {
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
                            synchronized (ProfileVerifyPhoneViewModel.this) {
                                callCountDownTimer = null;
                                updateCallRetry(0);
                            }
                        }
                    };
                    callCountDownTimer.start();
                }
            }
        }

        public LiveData<Registration.RegistrationRequestResult> requestSms(String phone) {
            MutableLiveData<Registration.RegistrationRequestResult> result = new MutableLiveData<>();
            bgWorkers.execute(() -> {
                try {
                    smsHashcashLatch.await(HASHCASH_MAX_WAIT_MS, TimeUnit.MILLISECONDS);
                    Log.i("ProfileVerifyPhoneActivity/requestSms done waiting for hashcashLatch");
                } catch (InterruptedException e) {
                    Log.e("Interrupted while waiting for hashcash", e);
                }
                Registration.RegistrationRequestResult requestResult = registration.requestRegistration(phone, null, null, smsHashcashResult);
                Log.i("ProfileVerifyPhoneActivity/requestSms request sent; restarting hashcash");
                smsHashcashLatch = new CountDownLatch(1);
                smsHashcashResult = null;
                runSmsHashcash();
                result.postValue(requestResult);
            });
            return result;
        }

        public LiveData<Registration.RegistrationRequestResult> requestCall(String phone) {
            MutableLiveData<Registration.RegistrationRequestResult> result = new MutableLiveData<>();
            bgWorkers.execute(() -> {
                try {
                    callHashcashLatch.await(HASHCASH_MAX_WAIT_MS, TimeUnit.MILLISECONDS);
                    Log.i("ProfileVerifyPhoneActivity/requestCall done waiting for hashcashLatch");
                } catch (InterruptedException e) {
                    Log.e("Interrupted while waiting for hashcash", e);
                }
                Registration.RegistrationRequestResult requestResult = registration.requestRegistrationViaVoiceCall(phone, null, null, callHashcashResult);
                Log.i("ProfileVerifyPhoneActivity/requestCall request sent; restarting hashcash");
                callHashcashLatch = new CountDownLatch(1);
                callHashcashResult = null;
                runCallHashcash();
                result.postValue(requestResult);
            });
            return result;
        }

        public void verifyRegistration(@NonNull String phone, @NonNull String code) {
            String uid = Me.getInstance().getUser();
            bgWorkers.execute(() -> {
                Registration.RegistrationVerificationResult result = registration.verifyPhoneNumber(phone, code, null, null, uid);
                registrationRequestResult.postValue(result);
            });
        }
    }
}

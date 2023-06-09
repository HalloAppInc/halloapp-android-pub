package com.halloapp.katchup;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.R;
import com.halloapp.registration.Registration;
import com.halloapp.registration.SmsVerificationManager;
import com.halloapp.ui.DebouncedClickListener;
import com.halloapp.ui.HalloActivity;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.KeyboardUtils;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.SnackbarHelper;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.michaelrocks.libphonenumber.android.NumberParseException;
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil;
import io.michaelrocks.libphonenumber.android.Phonenumber;

public class ProfileAddPhoneActivity extends HalloActivity {

    public static final String EXTRA_PHONE = "phone";
    private static final int REQUEST_CODE_VERIFICATION = 1;
    private static final long HASHCASH_MAX_WAIT_MS = 60_000;

    private final SmsVerificationManager smsVerificationManager = SmsVerificationManager.getInstance();
    private CountryCodePicker countryCodePicker;
    private EditText phoneNumberEditText;
    private View nextButton;

    private ProfileAddPhoneViewModel profileAddPhoneViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile_add_phone);

        phoneNumberEditText = findViewById(R.id.phone_number);
        countryCodePicker = findViewById(R.id.ccp);
        countryCodePicker.registerCarrierNumberEditText(phoneNumberEditText);
        countryCodePicker.useFlagEmoji(Build.VERSION.SDK_INT >= 28);

        nextButton = findViewById(R.id.next);

        View prev = findViewById(R.id.prev);
        prev.setOnClickListener(v -> finish());

        phoneNumberEditText.setTextColor(phoneNumberEditText.getCurrentTextColor());

        profileAddPhoneViewModel = new ViewModelProvider(this).get(ProfileAddPhoneViewModel.class);

        profileAddPhoneViewModel.getRegistrationRequestResult().observe(this, result -> {
            if (result == null) {
                return;
            }
            if (result.result == Registration.RegistrationRequestResult.RESULT_OK) {
                final Intent intent = new Intent(this, ProfileVerifyPhoneActivity.class);
                intent.putExtra(ProfileVerifyPhoneActivity.EXTRA_PHONE, result.phone);
                intent.putExtra(ProfileVerifyPhoneActivity.EXTRA_RETRY_WAIT_TIME, result.retryWaitTimeSeconds);
                startActivityForResult(intent, REQUEST_CODE_VERIFICATION);
            } else {
                 if (result.result == Registration.RegistrationRequestResult.RESULT_FAILED_RETRIED_TOO_SOON && result.phone != null) {
                    final Intent intent = new Intent(this, ProfileVerifyPhoneActivity.class);
                    intent.putExtra(ProfileVerifyPhoneActivity.EXTRA_PHONE, result.phone);
                    intent.putExtra(ProfileVerifyPhoneActivity.EXTRA_RETRY_WAIT_TIME, result.retryWaitTimeSeconds);
                    startActivityForResult(intent, REQUEST_CODE_VERIFICATION);
                } else if (result.result == Registration.RegistrationRequestResult.RESULT_FAILED_INVALID_PHONE_NUMBER) {
                    SnackbarHelper.showWarning(this, R.string.invalid_phone_number);
                } else {
                    SnackbarHelper.showInfo(this, R.string.registration_failed);
                }
                nextButton.setVisibility(View.VISIBLE);
                countryCodePicker.setCcpClickable(true);
            }
        });

        phoneNumberEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                startRegistrationRequestListener.onClick(v);
            }
            return false;
        });

        phoneNumberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateNextButton();
            }
        });
        nextButton.setOnClickListener(startRegistrationRequestListener);

        updateNextButton();
        KeyboardUtils.showSoftKeyboard(phoneNumberEditText);
    }

    @Override
    public void onActivityResult(final int request, final int result, final Intent data) {
        super.onActivityResult(request, result, data);
        switch (request) {
            case REQUEST_CODE_VERIFICATION: {
                if (result == RESULT_OK) {
                    Intent intent = new Intent();
                    intent.putExtra(EXTRA_PHONE, countryCodePicker.getFullNumber());
                    setResult(RESULT_OK, intent);
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

    private final View.OnClickListener startRegistrationRequestListener = new DebouncedClickListener() {

        @Override
        public void onOneClick(@NonNull View view) {
            startRegistrationRequest();
        }
    };

    private void updateNextButton() {
        boolean phoneOkayLength = isPhoneOkayLength();
        nextButton.setVisibility(phoneOkayLength ? View.VISIBLE : View.GONE);
    }

    private boolean isPhoneOkayLength() {
        boolean phoneOkayLength = false;
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.createInstance(this);
        try {
            Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(countryCodePicker.getFormattedFullNumber(), countryCodePicker.getSelectedCountryNameCode());
            phoneOkayLength = phoneNumberUtil.isPossibleNumberWithReason(phoneNumber) == PhoneNumberUtil.ValidationResult.IS_POSSIBLE;
        } catch (NumberParseException e) {
            Log.i("Failed to parse number: " + e);
        }
        return phoneOkayLength;
    }

    private void startRegistrationRequest() {
        if (!isPhoneOkayLength()) {
            SnackbarHelper.showInfo(this, R.string.invalid_phone_number);
            phoneNumberEditText.requestFocus();
            return;
        }

        nextButton.setVisibility(View.INVISIBLE);
        countryCodePicker.setCcpClickable(false);

        Log.i("ProfileAddPhoneActivity.startRequestPhoneRegistration for " + countryCodePicker.getFullNumber());
        smsVerificationManager.start(getApplicationContext());
        profileAddPhoneViewModel.requestPhoneRegistration(countryCodePicker.getFullNumber());
    }

    public static class ProfileAddPhoneViewModel extends ViewModel {

        private final BgWorkers bgWorkers = BgWorkers.getInstance();
        private final Registration registration = Registration.getInstance();

        private final MutableLiveData<Registration.RegistrationRequestResult> registrationRequestResult = new MutableLiveData<>();
        private CountDownLatch hashcashLatch = new CountDownLatch(1);
        private Registration.HashcashResult hashcashResult;

        public ProfileAddPhoneViewModel() {
            runHashcash();
        }

        void runHashcash() {
            bgWorkers.execute(() -> {
                hashcashResult = registration.getHashcashSolution();
                if (hashcashResult.result != Registration.HashcashResult.RESULT_OK) {
                    Log.e("Got hashcash failure " + hashcashResult.result);
                    Log.sendErrorReport("Hashcash failed");
                }
                hashcashLatch.countDown();
            });
        }

        LiveData<Registration.RegistrationRequestResult> getRegistrationRequestResult() {
            return registrationRequestResult;
        }

        void requestPhoneRegistration(@NonNull String phone) {
            bgWorkers.execute(() -> {
                try {
                    hashcashLatch.await(HASHCASH_MAX_WAIT_MS, TimeUnit.MILLISECONDS);
                    Log.i("ProfileAddPhoneActivity/requestPhoneRegistration done waiting for hashcashLatch");
                } catch (InterruptedException e) {
                    Log.e("Interrupted while waiting for hashcash", e);
                }

                Registration.RegistrationRequestResult result = registration.registerPhoneNumber(null, phone, null, null, hashcashResult);
                hashcashLatch = new CountDownLatch(1);
                hashcashResult = null;
                runHashcash();
                registrationRequestResult.postValue(result);
            });
        }
    }
}

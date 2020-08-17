package com.halloapp.ui;

import android.app.Application;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.R;
import com.halloapp.registration.Registration;
import com.halloapp.registration.SmsVerificationManager;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.widget.SnackbarHelper;

public class RegistrationVerificationActivity extends HalloActivity {

    public static final String EXTRA_PHONE_NUMBER = "phone_number";

    private static final int CODE_LENGTH = 6;

    private final SmsVerificationManager smsVerificationManager = SmsVerificationManager.getInstance();

    private RegistrationVerificationViewModel registrationVerificationViewModel;

    private TextView codeEditText;
    private View loadingProgressBar;

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
        Log.i("RegistrationVerificationActivity.onCreate");
        setContentView(R.layout.activity_registration_verification);

        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        codeEditText = findViewById(R.id.code);
        loadingProgressBar = findViewById(R.id.loading);

        registrationVerificationViewModel = new ViewModelProvider(this).get(RegistrationVerificationViewModel.class);
        registrationVerificationViewModel.getRegistrationVerificationResult().observe(this, result -> {
            if (result == null) {
                return;
            }
            if (result.result == Registration.RegistrationVerificationResult.RESULT_OK) {
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
            loadingProgressBar.setVisibility(View.GONE);
        });


        final String phoneNumber = Preconditions.checkNotNull(getIntent().getStringExtra(EXTRA_PHONE_NUMBER));
        final TextView titleView = findViewById(R.id.title);
        titleView.setText(getString(R.string.verify_registration_title, PhoneNumberUtils.formatNumber("+" + phoneNumber, null)));

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

            }
        });
        codeEditText.requestFocus();

        final String lastSmsCode = smsVerificationManager.getLastReceivedCode();
        if (!TextUtils.isEmpty(lastSmsCode)) {
            codeEditText.setText(lastSmsCode);
        }
        smsVerificationManager.addObserver(smsVerificationObserver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("RegistrationVerificationActivity.onDestroy");
        smsVerificationManager.removeObserver(smsVerificationObserver);
    }

    private void startVerification(@NonNull String phone, @NonNull String code) {
        loadingProgressBar.setVisibility(View.VISIBLE);
        codeEditText.setEnabled(false);
        registrationVerificationViewModel.verifyRegistration(phone, code);
    }

    public static class RegistrationVerificationViewModel extends AndroidViewModel {

        private final Registration registration = Registration.getInstance();

        private final MutableLiveData<Registration.RegistrationVerificationResult> registrationRequestResult = new MutableLiveData<>();

        public RegistrationVerificationViewModel(@NonNull Application application) {
            super(application);
        }

        LiveData<Registration.RegistrationVerificationResult> getRegistrationVerificationResult() {
            return registrationRequestResult;
        }

        void verifyRegistration(@NonNull String phone, @NonNull String code) {
            new RegistrationVerificationTask(this, phone, code).execute();
        }
    }

    private static class RegistrationVerificationTask extends AsyncTask<Void, Void, Registration.RegistrationVerificationResult> {

        final RegistrationVerificationViewModel viewModel;
        final String phone;
        final String code;

        RegistrationVerificationTask(@NonNull RegistrationVerificationViewModel viewModel, @NonNull String phone, @NonNull String code) {
            this.viewModel = viewModel;
            this.phone = phone;
            this.code = code;
        }

        @Override
        protected Registration.RegistrationVerificationResult doInBackground(Void... voids) {
            return viewModel.registration.verifyPhoneNumber(phone, code);
        }

        @Override
        protected void onPostExecute(final Registration.RegistrationVerificationResult result) {
            viewModel.registrationRequestResult.setValue(result);
        }
    }
}

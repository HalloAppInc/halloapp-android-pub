package com.halloapp.ui;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.Notifications;
import com.halloapp.R;
import com.halloapp.registration.Registration;
import com.halloapp.registration.SmsVerificationManager;
import com.halloapp.util.Log;
import com.halloapp.widget.CenterToast;
import com.hbb20.CountryCodePicker;


public class RegistrationRequestActivity extends AppCompatActivity {

    public static final String EXTRA_RE_VERIFY = "reverify";

    private static final int REQUEST_CODE_VERIFICATION = 1;

    private RegistrationRequestViewModel registrationRequestViewModel;

    private CountryCodePicker countryCodePicker;
    private EditText phoneNumberEditText;
    private View nextButton;
    private View loadingProgressBar;

    public static void reVerify(final Context context) {
        context.startActivity(new Intent(context, RegistrationRequestActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(RegistrationRequestActivity.EXTRA_RE_VERIFY, true));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("RegistrationRequestActivity.onCreate");
        setContentView(R.layout.activity_registration_request);

        phoneNumberEditText = findViewById(R.id.phone_number);
        countryCodePicker = findViewById(R.id.ccp);
        countryCodePicker.registerCarrierNumberEditText(phoneNumberEditText);
        countryCodePicker.useFlagEmoji(Build.VERSION.SDK_INT >= 28);
        loadingProgressBar = findViewById(R.id.loading);
        nextButton = findViewById(R.id.next);

        if (getIntent().getBooleanExtra(EXTRA_RE_VERIFY, false)) {
            final TextView titleView = findViewById(R.id.title);
            titleView.setText(R.string.reverify_registration_title);
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
                startActivityForResult(intent, REQUEST_CODE_VERIFICATION);
            } else {
                CenterToast.show(this, R.string.registration_failed);
                nextButton.setVisibility(View.VISIBLE);
                phoneNumberEditText.setEnabled(true);
                countryCodePicker.setCcpClickable(true);
            }
            loadingProgressBar.setVisibility(View.GONE);
        });

        findViewById(R.id.phone_number);
        phoneNumberEditText.requestFocus();
        phoneNumberEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                startRegistrationRequest();
            }
            return false;
        });

        findViewById(R.id.next).setOnClickListener(v -> startRegistrationRequest());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("RegistrationRequestActivity.onDestroy");
    }

    @Override
    public void onActivityResult(final int request, final int result, final Intent data) {
        super.onActivityResult(request, result, data);
        //noinspection SwitchStatementWithTooFewBranches
        switch (request) {
            case REQUEST_CODE_VERIFICATION: {
                if (result == RESULT_OK) {
                    startActivity(new Intent(this, MainActivity.class));
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

    private void startRegistrationRequest() {
        if (!countryCodePicker.isValidFullNumber()) {
            CenterToast.show(this, R.string.invalid_phone_number);
            return;
        }

        loadingProgressBar.setVisibility(View.VISIBLE);
        nextButton.setVisibility(View.GONE);
        phoneNumberEditText.setEnabled(false);
        countryCodePicker.setCcpClickable(false);
        Log.i("RegistrationRequestActivity.startRegistrationRequest for " + countryCodePicker.getFullNumber());

        SmsVerificationManager.getInstance().start(getApplicationContext());
        registrationRequestViewModel.requestRegistration(countryCodePicker.getFullNumber());
    }

    public static class RegistrationRequestViewModel extends ViewModel {

        private final MutableLiveData<Registration.RegistrationRequestResult> registrationRequestResult = new MutableLiveData<>();

        LiveData<Registration.RegistrationRequestResult> getRegistrationRequestResult() {
            return registrationRequestResult;
        }

        void requestRegistration(@NonNull String phone) {
            new RegistrationRequestTask(this, phone).execute();
        }
    }

    private static class RegistrationRequestTask extends AsyncTask<Void, Void, Registration.RegistrationRequestResult> {

        final RegistrationRequestViewModel viewModel;
        final String phone;

        RegistrationRequestTask(@NonNull RegistrationRequestViewModel viewModel, @NonNull String phone) {
            this.viewModel = viewModel;
            this.phone = phone;
        }

        @Override
        protected Registration.RegistrationRequestResult doInBackground(Void... voids) {
            return Registration.getInstance().requestRegistration(phone);
        }

        @Override
        protected void onPostExecute(final Registration.RegistrationRequestResult result) {
            viewModel.registrationRequestResult.setValue(result);
        }
    }
}

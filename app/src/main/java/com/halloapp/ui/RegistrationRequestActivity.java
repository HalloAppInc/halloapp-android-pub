package com.halloapp.ui;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.Constants;
import com.halloapp.Me;
import com.halloapp.Notifications;
import com.halloapp.R;
import com.halloapp.content.ContentDb;
import com.halloapp.registration.Registration;
import com.halloapp.registration.SmsVerificationManager;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.util.StringUtils;
import com.halloapp.widget.CenterToast;
import com.hbb20.CountryCodePicker;


public class RegistrationRequestActivity extends AppCompatActivity {

    public static final String EXTRA_RE_VERIFY = "reverify";

    private static final int REQUEST_CODE_VERIFICATION = 1;

    private RegistrationRequestViewModel registrationRequestViewModel;

    private CountryCodePicker countryCodePicker;
    private EditText phoneNumberEditText;
    private EditText nameEditText;
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

        nameEditText = findViewById(R.id.name);
        phoneNumberEditText = findViewById(R.id.phone_number);
        countryCodePicker = findViewById(R.id.ccp);
        countryCodePicker.registerCarrierNumberEditText(phoneNumberEditText);
        countryCodePicker.useFlagEmoji(Build.VERSION.SDK_INT >= 28);
        loadingProgressBar = findViewById(R.id.loading);
        nextButton = findViewById(R.id.next);

        if (getIntent().getBooleanExtra(EXTRA_RE_VERIFY, false)) {
            final TextView titleView = findViewById(R.id.title);
            titleView.setText(R.string.reverify_registration_title);
            findViewById(R.id.name_layout).setVisibility(View.GONE);
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
                if (result.result == Registration.RegistrationRequestResult.RESULT_FAILED_SERVER_NO_FRIENDS) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(RegistrationRequestActivity.this)
                        .setMessage(getBaseContext().getString(R.string.registration_failed_no_friends))
                        .setPositiveButton(R.string.ok, null)
                        .setCancelable(true);
                    builder.show();
                } else {
                    CenterToast.show(this, R.string.registration_failed);
                }
                nextButton.setVisibility(View.VISIBLE);
                phoneNumberEditText.setEnabled(true);
                countryCodePicker.setCcpClickable(true);
            }
            loadingProgressBar.setVisibility(View.GONE);
        });

        phoneNumberEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                startRegistrationRequest();
            }
            return false;
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
            }
        });
        nameEditText.requestFocus();

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
        final String name = StringUtils.preparePostText(Preconditions.checkNotNull(nameEditText.getText()).toString());
        if (TextUtils.isEmpty(name)) {
            CenterToast.show(this, R.string.name_must_be_specified);
            nameEditText.requestFocus();
            return;
        }
        if (!countryCodePicker.isValidFullNumber()) {
            CenterToast.show(this, R.string.invalid_phone_number);
            phoneNumberEditText.requestFocus();
            return;
        }

        loadingProgressBar.setVisibility(View.VISIBLE);
        nextButton.setVisibility(View.GONE);
        phoneNumberEditText.setEnabled(false);
        countryCodePicker.setCcpClickable(false);
        Log.i("RegistrationRequestActivity.startRegistrationRequest for " + countryCodePicker.getFullNumber());

        SmsVerificationManager.getInstance().start(getApplicationContext());
        registrationRequestViewModel.requestRegistration(countryCodePicker.getFullNumber(), name);
    }

    public static class RegistrationRequestViewModel extends AndroidViewModel {

        private final MutableLiveData<Registration.RegistrationRequestResult> registrationRequestResult = new MutableLiveData<>();

        public RegistrationRequestViewModel(@NonNull Application application) {
            super(application);
        }

        LiveData<Registration.RegistrationRequestResult> getRegistrationRequestResult() {
            return registrationRequestResult;
        }

        void requestRegistration(@NonNull String phone, @NonNull String name) {
            new RegistrationRequestTask(this, phone, name).execute();
        }
    }

    private static class RegistrationRequestTask extends AsyncTask<Void, Void, Registration.RegistrationRequestResult> {

        final RegistrationRequestViewModel viewModel;
        final String phone;
        final String name;

        RegistrationRequestTask(@NonNull RegistrationRequestViewModel viewModel, @NonNull String phone, @NonNull String name) {
            this.viewModel = viewModel;
            this.phone = phone;
            this.name = name;
        }

        @Override
        protected Registration.RegistrationRequestResult doInBackground(Void... voids) {
            Me.getInstance(viewModel.getApplication()).saveName(name);
            return Registration.getInstance().requestRegistration(phone);
        }

        @Override
        protected void onPostExecute(final Registration.RegistrationRequestResult result) {
            viewModel.registrationRequestResult.setValue(result);
        }
    }
}

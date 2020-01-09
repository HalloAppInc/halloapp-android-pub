package com.halloapp.ui;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Preconditions;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.halloapp.MainActivity;
import com.halloapp.R;
import com.halloapp.Registration;
import com.halloapp.widget.CenterToast;
import com.halloapp.util.Log;

import org.json.JSONException;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


public class RegistrationRequestActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_VERIFICATION = 1;

    private RegistrationRequestViewModel registrationRequestViewModel;

    private EditText phoneNumberEditText;
    private View nextButton;
    private View loadingProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("RegistrationRequestActivity.onCreate");
        setContentView(R.layout.activity_registration_request);

        setTitle("");
        Preconditions.checkNotNull(getSupportActionBar()).setElevation(0);

        phoneNumberEditText = findViewById(R.id.phone_number);
        loadingProgressBar = findViewById(R.id.loading);
        nextButton = findViewById(R.id.next);

        registrationRequestViewModel = ViewModelProviders.of(this).get(RegistrationRequestViewModel.class);
        registrationRequestViewModel.getRegistrationRequestResult().observe(this, result -> {
            if (result == null) {
                return;
            }

            if (result.result == RegistrationRequestResult.RESULT_OK) {
                final Intent intent = new Intent(this, RegistrationVerificationActivity.class);
                intent.putExtra(RegistrationVerificationActivity.EXTRA_PHONE_NUMBER, result.phone);
                startActivityForResult(intent, REQUEST_CODE_VERIFICATION);
            } else {
                CenterToast.show(this, R.string.registration_failed);
                nextButton.setVisibility(View.VISIBLE);
                phoneNumberEditText.setEnabled(true);
            }
            loadingProgressBar.setVisibility(View.GONE);
        });

        findViewById(R.id.phone_number);
        phoneNumberEditText.requestFocus();
        phoneNumberEditText.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
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
                }
                break;
            }
        }
    }

    private void startRegistrationRequest() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        nextButton.setVisibility(View.GONE);
        phoneNumberEditText.setEnabled(false);
        registrationRequestViewModel.requestRegistration(phoneNumberEditText.getText().toString().replaceAll("[^\\d.]", ""));
    }

    private static class RegistrationRequestResult {

        @Retention(RetentionPolicy.SOURCE)
        @IntDef({RESULT_OK, RESULT_FAILED_SERVER, RESULT_FAILED_NETWORK})
        @interface Result {}
        static final int RESULT_OK = 0;
        static final int RESULT_FAILED_SERVER = 1;
        static final int RESULT_FAILED_NETWORK = 2;

        final String phone;
        final @Result int result;

        RegistrationRequestResult(@NonNull String phone, @Result int result) {
            this.phone = phone;
            this.result = result;
        }
    }

    public static class RegistrationRequestViewModel extends ViewModel {

        private MutableLiveData<RegistrationRequestResult> registrationRequestResult = new MutableLiveData<>();

        LiveData<RegistrationRequestResult> getRegistrationRequestResult() {
            return registrationRequestResult;
        }

        void requestRegistration(@NonNull String phone) {
            new RegistrationRequestTask(this, phone).execute();
        }
    }

    private static class RegistrationRequestTask extends AsyncTask<Void, Void, RegistrationRequestResult> {

        final RegistrationRequestViewModel viewModel;
        final String phone;

        RegistrationRequestTask(@NonNull RegistrationRequestViewModel viewModel, @NonNull String phone) {
            this.viewModel = viewModel;
            this.phone = phone;
        }

        @Override
        protected RegistrationRequestResult doInBackground(Void... voids) {
            try {
                Registration.getInstance().requestRegistration(phone);
                return new RegistrationRequestResult(phone, RegistrationRequestResult.RESULT_OK);
            } catch (IOException | JSONException e) {
                Log.e("RegistrationRequestTask", e);
                return new RegistrationRequestResult(phone, RegistrationRequestResult.RESULT_FAILED_NETWORK);
            }
        }

        @Override
        protected void onPostExecute(final RegistrationRequestResult result) {
            viewModel.registrationRequestResult.setValue(result);
        }
    }
}

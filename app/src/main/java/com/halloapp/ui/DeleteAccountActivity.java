package com.halloapp.ui;

import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.FileStore;
import com.halloapp.Me;
import com.halloapp.R;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.ContentDb;
import com.halloapp.util.FileUtils;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.SnackbarHelper;
import com.halloapp.xmpp.Connection;
import com.hbb20.CountryCodePicker;

import io.michaelrocks.libphonenumber.android.NumberParseException;
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil;
import io.michaelrocks.libphonenumber.android.Phonenumber;

public class DeleteAccountActivity extends HalloActivity {

    private DeleteAccountViewModel viewModel;

    private CountryCodePicker countryCodePicker;
    private EditText phoneNumberEditText;
    private View nextButton;
    private View loadingProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_account);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        viewModel = new ViewModelProvider(this).get(DeleteAccountViewModel.class);

        phoneNumberEditText = findViewById(R.id.phone_number);
        countryCodePicker = findViewById(R.id.ccp);
        countryCodePicker.registerCarrierNumberEditText(phoneNumberEditText);
        countryCodePicker.useFlagEmoji(Build.VERSION.SDK_INT >= 28);
        loadingProgressBar = findViewById(R.id.loading);
        nextButton = findViewById(R.id.next);

        phoneNumberEditText.setTextColor(phoneNumberEditText.getCurrentTextColor()); // so phoneNumberEditText.setEnabled(false) doesn't change color

        viewModel.getResult().observe(this, success -> {
            nextButton.setEnabled(true);
            loadingProgressBar.setVisibility(View.INVISIBLE);
            if (!Boolean.TRUE.equals(success)) {
                SnackbarHelper.showWarning(this, R.string.delete_account_failed);
            }
        });

        nextButton.setOnClickListener(v -> {
            nextButton.setEnabled(false);
            loadingProgressBar.setVisibility(View.VISIBLE);
            deleteAccount();
        });
    }

    private void deleteAccount() {
        if (!isPhoneOkayLength()) {
            SnackbarHelper.showWarning(this, R.string.invalid_phone_number);
            loadingProgressBar.setVisibility(View.INVISIBLE);
            nextButton.setEnabled(true);
            phoneNumberEditText.requestFocus();
            return;
        }

        String phone = countryCodePicker.getFullNumberWithPlus();
        viewModel.deleteAccount(phone);
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

    public static class DeleteAccountViewModel extends AndroidViewModel {

        private final Connection connection = Connection.getInstance();

        private final MutableLiveData<Boolean> result = new MutableLiveData<>();

        public DeleteAccountViewModel(@NonNull Application application) {
            super(application);
        }

        LiveData<Boolean> getResult() {
            return result;
        }

        void deleteAccount(@NonNull String phone) {
            connection.deleteAccount(phone).onResponse(iq -> {
                deleteAllUserData();
                result.postValue(true);
                final Intent intent = new Intent(getApplication(), DeletionConfirmationActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                getApplication().startActivity(intent);
                Runtime.getRuntime().exit(0);
            }).onError(e -> {
                result.postValue(false);
            });
        }
    }

    public static void deleteAllUserData() {
        Me.getInstance().resetRegistration();
        ContactsDb.getInstance().deleteDb();
        ContentDb.getInstance().deleteDb();
        FileUtils.deleteRecursive(FileStore.getInstance().getMediaDir());
        FileUtils.deleteRecursive(FileStore.getInstance().getTmpDir());
    }
}

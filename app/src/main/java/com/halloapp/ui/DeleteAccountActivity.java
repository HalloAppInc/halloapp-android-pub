package com.halloapp.ui;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.AppContext;
import com.halloapp.FileStore;
import com.halloapp.Me;
import com.halloapp.Preferences;
import com.halloapp.R;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.RawContactDatabase;
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
    private EditText deleteReason;
    private View nextButton;

    private View deleteExplanationContainer;
    private View deleteProgressContainer;
    private View deleteConfirmationContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_account);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        viewModel = new ViewModelProvider(this).get(DeleteAccountViewModel.class);

        deleteExplanationContainer = findViewById(R.id.delete_explanation);
        deleteProgressContainer = findViewById(R.id.delete_progress);
        deleteConfirmationContainer = findViewById(R.id.delete_step);
        deleteReason = findViewById(R.id.feedback_edit_text);

        View continueButton = findViewById(R.id.continue_delete);
        continueButton.setOnClickListener(v -> {
            deleteExplanationContainer.setVisibility(View.GONE);
            deleteConfirmationContainer.setVisibility(View.VISIBLE);
        });

        phoneNumberEditText = findViewById(R.id.phone_number);
        countryCodePicker = findViewById(R.id.ccp);
        countryCodePicker.registerCarrierNumberEditText(phoneNumberEditText);
        countryCodePicker.useFlagEmoji(Build.VERSION.SDK_INT >= 28);
        nextButton = findViewById(R.id.next);

        phoneNumberEditText.setTextColor(phoneNumberEditText.getCurrentTextColor()); // so phoneNumberEditText.setEnabled(false) doesn't change color

        viewModel.getResult().observe(this, success -> {
            nextButton.setEnabled(true);
            deleteProgressContainer.setVisibility(View.GONE);
            deleteConfirmationContainer.setVisibility(View.VISIBLE);
            if (!Boolean.TRUE.equals(success)) {
                SnackbarHelper.showWarning(this, R.string.delete_account_failed);
            }
        });

        nextButton.setOnClickListener(v -> {
            if (isPhoneNumberValid()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.delete_account_confirmation_message);
                builder.setPositiveButton(R.string.delete_account_button, (dialog, which) -> {
                    deleteAccount();
                });
                builder.setNegativeButton(R.string.cancel, null);
                builder.show();
            }
        });
    }

    private boolean isPhoneNumberValid() {
        if (!isPhoneOkayLength()) {
            SnackbarHelper.showWarning(this, R.string.invalid_phone_number);
            nextButton.setEnabled(true);
            phoneNumberEditText.requestFocus();
            return false;
        }
        return true;
    }

    private void deleteAccount() {
        nextButton.setEnabled(false);
        String phone = countryCodePicker.getFullNumberWithPlus();
        deleteConfirmationContainer.setVisibility(View.GONE);
        deleteProgressContainer.setVisibility(View.VISIBLE);
        viewModel.deleteAccount(phone, deleteReason.getText().toString());
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
        private final String username;

        public DeleteAccountViewModel(@NonNull Application application) {
            super(application);
            username = Me.getInstance().getUsername();
        }

        LiveData<Boolean> getResult() {
            return result;
        }

        void deleteAccount(@NonNull String phone, @Nullable String reason) {
            connection.deleteAccount(phone, username, reason).onResponse(iq -> {
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
        Preferences.getInstance().wipePreferences();
        RawContactDatabase.deleteRawContactsAccount(AppContext.getInstance().get());
    }
}

package com.halloapp.katchup;

import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Outline;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.transition.TransitionManager;

import com.halloapp.FileStore;
import com.halloapp.Me;
import com.halloapp.Preferences;
import com.halloapp.R;
import com.halloapp.RegistrationRequestActivity;
import com.halloapp.content.Media;
import com.halloapp.media.ChunkedMediaParametersException;
import com.halloapp.media.Downloader;
import com.halloapp.media.ForeignRemoteAuthorityException;
import com.halloapp.proto.server.ExportData;
import com.halloapp.ui.HalloFragment;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.KeyboardUtils;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.SnackbarHelper;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.ExportDataResponseIq;
import com.halloapp.xmpp.util.ObservableErrorException;
import com.hbb20.CountryCodePicker;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SettingsAccountDeleteFragment extends HalloFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_account_delete, container, false);

        View warningView = root.findViewById(R.id.warning);
        View continueBtn = root.findViewById(R.id.continue_button);
        View cancelBtn = root.findViewById(R.id.cancel);
        View confirmFormView = root.findViewById(R.id.confirm_form);
        View deleteBtn = root.findViewById(R.id.delete);
        CountryCodePicker countryCodePicker = root.findViewById(R.id.ccp);
        EditText phoneNumberView = root.findViewById(R.id.phone_number);
        EditText feedbackView = root.findViewById(R.id.feedback);

        AccountDeleteViewModel viewModel = new ViewModelProvider(this).get(AccountDeleteViewModel.class);

        countryCodePicker.registerCarrierNumberEditText(phoneNumberView);
        countryCodePicker.useFlagEmoji(Build.VERSION.SDK_INT >= 28);

        warningView.setVisibility(View.VISIBLE);
        continueBtn.setVisibility(View.VISIBLE);
        confirmFormView.setVisibility(View.GONE);
        deleteBtn.setVisibility(View.GONE);
        deleteBtn.setEnabled(false);

        ViewOutlineProvider roundedOutlineProvider = new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), view.getHeight());
            }
        };
        continueBtn.setClipToOutline(true);
        continueBtn.setOutlineProvider(roundedOutlineProvider);
        deleteBtn.setClipToOutline(true);
        deleteBtn.setOutlineProvider(roundedOutlineProvider);

        cancelBtn.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        continueBtn.setOnClickListener(v -> {
            TransitionManager.beginDelayedTransition((ViewGroup) warningView.getParent());
            warningView.setVisibility(View.GONE);
            continueBtn.setVisibility(View.GONE);
            confirmFormView.setVisibility(View.VISIBLE);
            deleteBtn.setVisibility(View.VISIBLE);
        });

        deleteBtn.setOnClickListener(v -> {
            if (phoneNumberView.hasFocus()) {
                KeyboardUtils.hideSoftKeyboard(phoneNumberView);
            } else if (feedbackView.hasFocus()) {
                KeyboardUtils.hideSoftKeyboard(feedbackView);
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                    .setMessage(getString(R.string.delete_account_alert_message))
                    .setTitle(getString(R.string.delete_account_alert_title))
                    .setPositiveButton(R.string.delete, (dialog, which) -> {
                        viewModel.deleteAccount(feedbackView.getText().toString().trim()).observe(getViewLifecycleOwner(), success -> {
                            if (Boolean.TRUE.equals(success)) {
                                startActivity(new Intent(requireContext(), ContactsAndLocationAccessActivity.class));
                            } else {
                                SnackbarHelper.showWarning(requireActivity(), R.string.error_unknown);
                            }
                        });
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .setCancelable(true);
            builder.show();
        });

        phoneNumberView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                deleteBtn.setEnabled(Me.getInstance().getPhone().equals(countryCodePicker.getFullNumber()));
            }
        });

        return root;
    }

    public static class AccountDeleteViewModel extends AndroidViewModel {
        private final BgWorkers bgWorkers = BgWorkers.getInstance();
        private final Connection connection = Connection.getInstance();

        private boolean processing = false;

        public AccountDeleteViewModel(@NonNull Application application) {
            super(application);
        }

        public LiveData<Boolean> deleteAccount(String feedback) {
            MutableLiveData<Boolean> success = new MutableLiveData<>();

            if (processing) {
                return success;
            }
            processing = true;

            bgWorkers.execute(() -> {
                try {
                    connection.deleteAccount(Me.getInstance().getPhone(), feedback).await();
                    Analytics.getInstance().deleteAccount();
                    Me.getInstance().resetRegistration();
                    final Preferences preferences = Preferences.getInstance();
                    preferences.setOnboardingFollowingSetup(false);
                    preferences.setOnboardingGetStartedShown(false);
                    preferences.setContactsPermissionRequested(false);
                    preferences.setLocationPermissionRequested(false);
                    success.postValue(true);
                } catch (ObservableErrorException e) {
                    Log.e("AccountDeleteViewModel.deleteAccount observable error", e);
                    success.postValue(false);
                } catch (InterruptedException e) {
                    Log.e("AccountDeleteViewModel.deleteAccount interrupted", e);
                    success.postValue(false);
                }

                processing = false;
            });

            return success;
        }
    }
}

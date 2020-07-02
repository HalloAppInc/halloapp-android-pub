package com.halloapp.ui.invites;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.R;
import com.halloapp.ui.HalloActivity;
import com.halloapp.xmpp.InvitesResponseIq;
import com.hbb20.CountryCodePicker;

public class InviteFriendsActivity extends HalloActivity {

    private CountryCodePicker countryCodePicker;
    private EditText phoneNumberEditText;

    private InviteFriendsViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_invite_friends);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setElevation(getResources().getDimension(R.dimen.action_bar_elevation));
        }

        viewModel = new ViewModelProvider(this).get(InviteFriendsViewModel.class);

        View inviteSending = findViewById(R.id.invite_sending);
        View progress = findViewById(R.id.progress);
        View errorView = findViewById(R.id.error);

        phoneNumberEditText = findViewById(R.id.phone_number);
        countryCodePicker = findViewById(R.id.ccp);
        countryCodePicker.registerCarrierNumberEditText(phoneNumberEditText);

        Button sendButton = findViewById(R.id.send);
        sendButton.setOnClickListener(v -> {
            String phone = countryCodePicker.getFullNumber();
            if (!countryCodePicker.isValidFullNumber()) {
                showErrorDialog(InvitesResponseIq.Result.INVALID_NUMBER);
                return;
            }
            ProgressDialog dialog = ProgressDialog.show(this, null, getString(R.string.invite_creation_in_progress));
            viewModel.sendInvite(phone).observe(this, nullableResult -> {
                dialog.cancel();
                if (nullableResult != InvitesResponseIq.Result.SUCCESS) {
                    showErrorDialog(nullableResult);
                } else {
                    onSuccessfulInvite(phone);
                }
            });
        });

        View tryAgain = findViewById(R.id.try_again);
        tryAgain.setOnClickListener(v -> {
            errorView.setVisibility(View.GONE);
            progress.setVisibility(View.VISIBLE);
            viewModel.inviteCountData.invalidate();
        });

        TextView inviteCount = findViewById(R.id.invite_count);
        viewModel.inviteCountData.getLiveData().observe(this, nullableCount -> {
            if (nullableCount == null) {
                inviteSending.setVisibility(View.INVISIBLE);
                progress.setVisibility(View.VISIBLE);
                errorView.setVisibility(View.GONE);
                return;
            } else if (nullableCount == InviteFriendsViewModel.RESPONSE_RETRYABLE) {
                errorView.setVisibility(View.VISIBLE);
                inviteSending.setVisibility(View.INVISIBLE);
                progress.setVisibility(View.GONE);
                return;
            }
            if (inviteSending.getVisibility() != View.VISIBLE) {
                progress.setVisibility(View.GONE);
                errorView.setVisibility(View.GONE);
                inviteSending.setVisibility(View.VISIBLE);
                inviteSending.clearAnimation();
                inviteSending.setAlpha(0);
                inviteSending.animate().alpha(1).start();
            }
            inviteCount.setText(String.format(getResources().getConfiguration().locale, "%d", nullableCount));
            sendButton.setEnabled(nullableCount > 0);
        });

    }

    private void onSuccessfulInvite(String phone) {
        Uri mUri = Uri.parse("smsto:" + phone);
        Intent mIntent = new Intent(Intent.ACTION_SENDTO, mUri);
        mIntent.putExtra("sms_body", getString(R.string.invite_text));
        phoneNumberEditText.setText("");
        startActivity(Intent.createChooser(mIntent, "Invite"));
    }

    private void showErrorDialog(@Nullable @InvitesResponseIq.Result Integer result) {
        @StringRes int errorMessageRes;
        if (result == null) {
            errorMessageRes = R.string.invite_failed_internet;
        } else {
            switch (result) {
                case InvitesResponseIq.Result.EXISTING_USER:
                    errorMessageRes = R.string.invite_failed_existing_user;
                    break;
                case InvitesResponseIq.Result.INVALID_NUMBER:
                    errorMessageRes = R.string.invite_failed_invalid_number;
                    break;
                case InvitesResponseIq.Result.NO_INVITES_LEFT:
                    errorMessageRes = R.string.invite_failed_no_invites;
                    break;
                case InvitesResponseIq.Result.NO_ACCOUNT:
                case InvitesResponseIq.Result.UNKNOWN:
                default:
                    errorMessageRes = R.string.invite_failed_unknown;
                    break;
            }
        }
        AlertDialog dialog = new AlertDialog.Builder(this).setMessage(errorMessageRes).setPositiveButton(R.string.ok, null).create();
        dialog.show();
    }


}

package com.halloapp.ui.groups;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.R;
import com.halloapp.id.GroupId;
import com.halloapp.ui.HalloActivity;
import com.halloapp.util.ClipUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.CenterToast;

public class GroupInviteLinkActivity extends HalloActivity {

    private static final String EXTRA_GROUP_ID = "group_id";

    public static Intent newIntent(@NonNull Context context, @NonNull GroupId groupId) {
        Preconditions.checkNotNull(groupId);
        Intent intent = new Intent(context, GroupInviteLinkActivity.class);
        intent.putExtra(EXTRA_GROUP_ID, groupId);
        return intent;
    }

    private GroupInviteLinkViewModel viewModel;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_group_invite_link);

        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        GroupId groupId = getIntent().getParcelableExtra(EXTRA_GROUP_ID);

        if (groupId == null) {
            Log.e("GroupInviteLinkActivity/onCreate missing group invite link activity");
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this, new GroupInviteLinkViewModel.Factory(getApplication(), groupId)).get(GroupInviteLinkViewModel.class);

        TextView inviteText = findViewById(R.id.invite_link_text);
        View resetLink = findViewById(R.id.reset_link);
        View shareLink = findViewById(R.id.share_link);
        View copyLink = findViewById(R.id.copy_link);
        View inviteLinkContainer = findViewById(R.id.invite_link_container);
        View qrCode = findViewById(R.id.qr_code);

        qrCode.setOnClickListener((v -> {
            String url = viewModel.getInviteLink().getValue();
            Intent intent = GroupInviteLinkQrActivity.newIntent(this, url, groupId);
            startActivity(intent);
        }));

        copyLink.setOnClickListener(v -> {
            ClipUtils.copyToClipboard(getInviteLinkText());
            CenterToast.show(this, R.string.invite_link_copied);
        });
        viewModel.getInviteLink().observe(this, inviteText::setText);

        resetLink.setOnClickListener(v -> {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getBaseContext().getString(R.string.reset_invite_link_confirmation));
            builder.setCancelable(true);
            builder.setPositiveButton(R.string.yes, (dialog, which) -> resetInviteLink());
            builder.setNegativeButton(R.string.no, null);
            builder.show();
        });

        inviteLinkContainer.setOnClickListener(v -> shareInviteLink());
        shareLink.setOnClickListener(v -> shareInviteLink());
    }

    private void resetInviteLink() {
        ProgressDialog progressDialog = ProgressDialog.show(this, null, getString(R.string.invite_link_resetting_progress));
        viewModel.resetInviteLink().observe(this, success -> {
            if (success == null) {
                return;
            }
            if (success) {
                CenterToast.show(this, R.string.invite_link_reset);
            } else {
                CenterToast.show(this, R.string.invite_link_reset_failed);
            }
            progressDialog.cancel();
        });
    }

    private void shareInviteLink() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        String link = getInviteLinkText();
        if (!TextUtils.isEmpty(link)) {
            sendIntent.putExtra(Intent.EXTRA_TEXT, link);
            sendIntent.setType("text/plain");

            Intent shareIntent = Intent.createChooser(sendIntent, null);
            startActivity(shareIntent);
        }
    }

    @Nullable
    private String getInviteLinkText() {
        String url = viewModel.getInviteLink().getValue();
        return getString(R.string.group_invite_link_context, url);
    }
}

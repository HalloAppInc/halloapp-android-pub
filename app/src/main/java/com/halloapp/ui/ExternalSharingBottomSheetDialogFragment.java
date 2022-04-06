package com.halloapp.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.R;
import com.halloapp.util.ClipUtils;
import com.halloapp.util.IntentUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.widget.SnackbarHelper;

public class ExternalSharingBottomSheetDialogFragment extends HalloBottomSheetDialogFragment {

    private static final String ARG_POST_ID = "post_id";

    public static ExternalSharingBottomSheetDialogFragment newInstance(@NonNull String postId) {
        Bundle args = new Bundle();
        args.putString(ARG_POST_ID, postId);
        ExternalSharingBottomSheetDialogFragment dialogFragment = new ExternalSharingBottomSheetDialogFragment();
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    private ExternalSharingViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle args = requireArguments();
        String postId = Preconditions.checkNotNull(args.getString(ARG_POST_ID));

        viewModel = new ViewModelProvider(this, new ExternalSharingViewModel.Factory(postId)).get(ExternalSharingViewModel.class);

        final View view = inflater.inflate(R.layout.external_sharing_bottom_sheet, container, false);

        final View shareExternally = view.findViewById(R.id.share_externally);
        final View copyLink = view.findViewById(R.id.copy_link);
        final View revokeLink = view.findViewById(R.id.revoke_link);
        final View shareInfo = view.findViewById(R.id.share_info);

        viewModel.getIsRevokable().observe(this, revocable -> {
            revokeLink.setVisibility(Boolean.TRUE.equals(revocable) ? View.VISIBLE : View.GONE);
            shareInfo.setVisibility(Boolean.TRUE.equals(revocable) ? View.GONE : View.VISIBLE);
        });

        shareExternally.setOnClickListener(v -> {
            ProgressDialog progressDialog = ProgressDialog.show(getContext(), null, getString(R.string.external_share_in_progress));
            viewModel.shareExternally().observe(this, url -> {
                progressDialog.dismiss();
                if (url != null) {
                    String text = getString(R.string.external_share_copy, url);
                    Intent intent = IntentUtils.createShareTextIntent(text);
                    startActivity(intent);
                    dismiss();
                } else {
                    SnackbarHelper.showWarning(v, R.string.external_share_failed);
                }
            });
        });
        copyLink.setOnClickListener(v -> {
            ProgressDialog progressDialog = ProgressDialog.show(getContext(), null, getString(R.string.external_share_in_progress));
            viewModel.shareExternally().observe(this, url -> {
                progressDialog.dismiss();
                if (url != null) {
                    String text = getString(R.string.external_share_copy, url);
                    ClipUtils.copyToClipboard(text);
                    SnackbarHelper.showInfo(getActivity(), R.string.invite_link_copied);
                    dismiss();
                } else {
                    SnackbarHelper.showWarning(v, R.string.external_share_failed);
                }
            });
        });
        revokeLink.setOnClickListener(v -> {
            ProgressDialog progressDialog = ProgressDialog.show(getContext(), null, getString(R.string.revoke_link_in_progress));
            viewModel.revokeLink().observe(this, success -> {
                progressDialog.dismiss();
                if (success != null && success) {
                    SnackbarHelper.showInfo(getActivity(), R.string.link_revoked);
                    dismiss();
                } else {
                    SnackbarHelper.showWarning(v, R.string.link_revoke_failed);
                }
            });
        });
        return view;
    }
}

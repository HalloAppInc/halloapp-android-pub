package com.halloapp.ui;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Outline;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.R;
import com.halloapp.util.ClipUtils;
import com.halloapp.util.IntentUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.widget.ShareExternallyView;
import com.halloapp.widget.SnackbarHelper;

public class ExternalSharingBottomSheetDialogFragment extends HalloBottomSheetDialogFragment {

    private static final String ARG_POST_ID = "post_id";
    private static final String ARG_SHARE_TO_PACKAGE = "share_to_package";
    private static final String ARG_SHARE_DIRECTLY = "share_directly";
    private static final String ARG_SELECTED_PREVIEW_IMAGE = "selected_preview_image";

    public static ExternalSharingBottomSheetDialogFragment newInstance(@NonNull String postId) {
        Bundle args = new Bundle();
        args.putString(ARG_POST_ID, postId);
        ExternalSharingBottomSheetDialogFragment dialogFragment = new ExternalSharingBottomSheetDialogFragment();
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    public static ExternalSharingBottomSheetDialogFragment shareDirectly(@NonNull String postId, String packageName) {
        return shareDirectly(postId, packageName, 0);
    }

    public static ExternalSharingBottomSheetDialogFragment shareDirectly(@NonNull String postId, String packageName, int selectedPreviewImage) {
        Bundle args = new Bundle();
        args.putString(ARG_POST_ID, postId);
        args.putString(ARG_SHARE_TO_PACKAGE, packageName);
        args.putBoolean(ARG_SHARE_DIRECTLY, true);
        args.putInt(ARG_SELECTED_PREVIEW_IMAGE, selectedPreviewImage);
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
        String targetPackage = args.getString(ARG_SHARE_TO_PACKAGE);

        viewModel = new ViewModelProvider(this, new ExternalSharingViewModel.Factory(postId)).get(ExternalSharingViewModel.class);

        final View view = inflater.inflate(R.layout.external_sharing_bottom_sheet, container, false);

        final View copyLink = view.findViewById(R.id.copy_link);
        final View revokeLink = view.findViewById(R.id.revoke_link);
        final View shareInfo = view.findViewById(R.id.share_info);
        final TextView shareTitle = view.findViewById(R.id.title);
        final TextView description = view.findViewById(R.id.description);
        final ImageView thumbnail = view.findViewById(R.id.media_thumb);
        final ShareExternallyView shareExternallyView = view.findViewById(R.id.share_external_view);

        viewModel.getTitle().observe(this, shareTitle::setText);
        viewModel.getDescription().observe(this, description::setText);

        viewModel.getIsRevokable().observe(this, revocable -> {
            revokeLink.setVisibility(Boolean.TRUE.equals(revocable) ? View.VISIBLE : View.GONE);
            shareInfo.setVisibility(Boolean.TRUE.equals(revocable) ? View.GONE : View.VISIBLE);
        });
        viewModel.getThumbnail().observe(this, bitmap -> {
            if (bitmap == null) {
                thumbnail.setVisibility(View.GONE);
            } else {
                thumbnail.setVisibility(View.VISIBLE);
                thumbnail.setImageBitmap(bitmap);
                thumbnail.setOutlineProvider(new ViewOutlineProvider() {
                    @Override
                    public void getOutline(View view, Outline outline) {
                        outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), getResources().getDimension(R.dimen.external_share_preview_radius));
                    }
                });
                thumbnail.setClipToOutline(true);
            }
        });
        if (!TextUtils.isEmpty(targetPackage)) {
            shareExternallyToTarget(shareExternallyView, targetPackage, args.getInt(ARG_SELECTED_PREVIEW_IMAGE, 0));
            return null;
        }
        if (args.getBoolean(ARG_SHARE_DIRECTLY, false)) {
            openShareExternalChooser(shareExternallyView);
            return null;
        }

        shareExternallyView.setListener(new ShareExternallyView.ShareListener() {
            @Override
            public void onOpenShare() {
                openShareExternalChooser(shareExternallyView);
            }

            @Override
            public void onShareTo(ShareExternallyView.ShareTarget target) {
                shareExternallyToTarget(shareExternallyView, target.getPackageName(), args.getInt(ARG_SELECTED_PREVIEW_IMAGE, 0));
            }
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

    private void shareExternallyToTarget(View view, String targetPackage, int previewImageIndex) {
        ProgressDialog progressDialog = ProgressDialog.show(getContext(), null, getString(R.string.external_share_in_progress));
        viewModel.shareExternallyWithPreview(requireContext(), targetPackage, previewImageIndex).observe(this, url -> {
            progressDialog.dismiss();
            if (url != null) {
                startActivity(url);
                dismiss();
            } else {
                SnackbarHelper.showWarning(view, R.string.external_share_failed);
            }
        });
    }

    private void openShareExternalChooser(View v) {
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
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog d = getDialog();
        Window w = d == null ? null : d.getWindow();
        String targetPackage = requireArguments().getString(ARG_SHARE_TO_PACKAGE);
        if (w != null && !TextUtils.isEmpty(targetPackage)) {
            w.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }
    }
}

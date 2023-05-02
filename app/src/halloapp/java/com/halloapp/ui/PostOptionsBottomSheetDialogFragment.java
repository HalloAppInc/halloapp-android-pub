package com.halloapp.ui;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.R;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Media;
import com.halloapp.content.Post;
import com.halloapp.ui.profile.ViewProfileActivity;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.SnackbarHelper;
import com.halloapp.xmpp.Connection;

import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class PostOptionsBottomSheetDialogFragment extends HalloBottomSheetDialogFragment implements EasyPermissions.PermissionCallbacks {

    private static final int REQUEST_EXTERNAL_STORAGE_PERMISSIONS = 1;

    private static final String ARG_POST_ID = "post_id";
    private static final String ARG_IS_ARCHIVE = "is_archived";

    public static PostOptionsBottomSheetDialogFragment newInstance(@NonNull String postId, boolean isArchived) {
        Bundle args = new Bundle();
        args.putString(ARG_POST_ID, postId);
        args.putBoolean(ARG_IS_ARCHIVE, isArchived);
        PostOptionsBottomSheetDialogFragment dialogFragment = new PostOptionsBottomSheetDialogFragment();
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    private PostOptionsViewModel viewModel;

    private ContactLoader contactLoader;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle args = requireArguments();
        String postId = args.getString(ARG_POST_ID);
        boolean isArchived = args.getBoolean(ARG_IS_ARCHIVE, false);

        viewModel = new ViewModelProvider(this, new PostOptionsViewModel.Factory(postId, isArchived)).get(PostOptionsViewModel.class);

        final View view = inflater.inflate(R.layout.post_menu_bottom_sheet, container, false);
        contactLoader = new ContactLoader(userId -> {
            view.getContext().startActivity(ViewProfileActivity.viewProfile(view.getContext(), userId));
            return null;
        });
        final TextView contactName = view.findViewById(R.id.contact_name);

        final View saveToGallery = view.findViewById(R.id.save_to_gallery);
        final View deletePost = view.findViewById(R.id.delete_post);
        final View reportPost = view.findViewById(R.id.report_post);
        viewModel.post.getLiveData().observe(this, post -> {
            if (post == null) {
                saveToGallery.setVisibility(View.INVISIBLE);
                deletePost.setVisibility(View.INVISIBLE);
                return;
            }
            contactLoader.cancel(contactName);
            if (post.senderUserId.isMe()) {
                contactName.setText((post.type == Post.TYPE_MOMENT || post.type == Post.TYPE_MOMENT_PSA) ? R.string. my_moment : R.string.my_post);
                deletePost.setVisibility(View.VISIBLE);
                reportPost.setVisibility(View.GONE);
            } else {
                contactLoader.load(contactName, post.senderUserId, false);
                deletePost.setVisibility(View.GONE);
                reportPost.setVisibility(View.VISIBLE);
            }
            if (post.media.isEmpty() || !Media.canBeSavedToGallery(post.media)) {
                saveToGallery.setVisibility(View.GONE);
            } else {
                saveToGallery.setVisibility(View.VISIBLE);
            }

            if (post.type == Post.TYPE_MOMENT || post.type == Post.TYPE_MOMENT_PSA) {
                TextView deletePostTextView = deletePost.findViewById(R.id.delete_post_text);
                deletePostTextView.setText(R.string.delete_moment);

                TextView reportPostTextView = reportPost.findViewById(R.id.report_post_text);
                reportPostTextView.setText(R.string.report_moment);
            }
        });

        viewModel.postDeleted.observe(this, deleted -> {
            if (Boolean.TRUE.equals(deleted)) {
                dismiss();
            }
        });
        saveToGallery.setOnClickListener(v -> {
            Post post = viewModel.post.getLiveData().getValue();
            if (post == null) {
                return;
            }
            if (Build.VERSION.SDK_INT < 29) {
                if (!EasyPermissions.hasPermissions(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    if (EasyPermissions.permissionPermanentlyDenied(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        new AppSettingsDialog.Builder(this)
                                .setRationale(getString(R.string.save_to_gallery_storage_permission_rationale_denied))
                                .build().show();
                    } else {
                        EasyPermissions.requestPermissions(this, getString(R.string.save_to_gallery_storage_permission_rationale), REQUEST_EXTERNAL_STORAGE_PERMISSIONS, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    }
                    return;
                }
            }
            saveToGalleryAndDismiss();
        });
        deletePost.setOnClickListener(v -> {
            Post post = viewModel.post.getLiveData().getValue();
            if (post != null) {
                DialogInterface.OnClickListener listener = post.isArchived ?
                        (dialog, which) -> {
                            ContentDb.getInstance().removePostFromArchive(post);
                            this.getActivity().finish();
                        } :
                        (dialog, which) -> ContentDb.getInstance().retractPost(post);

                String message = getString(R.string.retract_post_confirmation);
                if (post.type == Post.TYPE_MOMENT || post.type == Post.TYPE_MOMENT_PSA) {
                    message = getString(R.string.retract_moment_confirmation);
                }

                new AlertDialog.Builder(requireContext())
                        .setMessage(message)
                        .setCancelable(true)
                        .setPositiveButton(R.string.yes, listener)
                        .setNegativeButton(R.string.no, null)
                        .show();
            }
        });
        reportPost.setOnClickListener(v -> {
            Post post = viewModel.post.getLiveData().getValue();
            if (post == null) {
                return;
            }

            String alertMessage, progressMessage;
            int reportSuccessMessage, reportFailMessage;
            if (post.type == Post.TYPE_MOMENT || post.type == Post.TYPE_MOMENT_PSA) {
                alertMessage = getString(R.string.report_moment_dialog_message);
                progressMessage = getString(R.string.report_moment_in_progress);
                reportSuccessMessage = R.string.report_moment_succeeded;
                reportFailMessage = R.string.report_moment_failed;
            } else {
                alertMessage = getString(R.string.report_post_dialog_message);
                progressMessage = getString(R.string.report_post_in_progress);
                reportSuccessMessage = R.string.report_post_succeeded;
                reportFailMessage = R.string.report_post_failed;
            }

            new AlertDialog.Builder(requireContext())
                    .setMessage(alertMessage)
                    .setCancelable(true)
                    .setPositiveButton(R.string.yes, (dialog, which) -> {
                        ProgressDialog progressDialog = ProgressDialog.show(getContext(), null, progressMessage, true);
                        BgWorkers.getInstance().execute(() -> {
                            Connection.getInstance().reportUserContent(post.senderUserId, postId, null)
                                    .onResponse(response -> {
                                        ContentDb.getInstance().deletePost(post, () -> {
                                            progressDialog.dismiss();
                                            dismiss();
                                            SnackbarHelper.showInfo(requireActivity(), reportSuccessMessage);
                                        });
                                    }).onError(error -> {
                                        Log.e("Failed to report user content", error);
                                        progressDialog.dismiss();
                                        dismiss();
                                        SnackbarHelper.showWarning(requireActivity(), reportFailMessage);
                                    });
                        });
                    })
                    .setNegativeButton(R.string.no, null)
                    .show();
        });
        return view;
    }

    private void saveToGalleryAndDismiss() {
        Activity parent = requireActivity();
        viewModel.savePostToGallery().observe(requireActivity(), success -> {
            if (success == null) {
                return;
            }
            if (success) {
                SnackbarHelper.showInfo(parent, R.string.media_saved_to_gallery);
            } else {
                SnackbarHelper.showWarning(parent, R.string.media_save_to_gallery_failed);
            }
        });
        dismiss();
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (requestCode == REQUEST_EXTERNAL_STORAGE_PERMISSIONS) {
            saveToGalleryAndDismiss();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        new AppSettingsDialog.Builder(this)
                .setRationale(getString(R.string.save_to_gallery_storage_permission_rationale_denied))
                .build().show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (contactLoader != null) {
            contactLoader.destroy();
        }
    }
}

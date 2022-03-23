package com.halloapp.ui;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.halloapp.id.GroupId;
import com.halloapp.props.ServerProps;
import com.halloapp.util.ClipUtils;
import com.halloapp.util.IntentUtils;
import com.halloapp.widget.SnackbarHelper;

import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class PostOptionsBottomSheetDialogFragment extends HalloBottomSheetDialogFragment implements EasyPermissions.PermissionCallbacks {

    private static final int REQUEST_EXTERNAL_STORAGE_PERMISSIONS = 1;

    private static final String ARG_POST_ID = "post_id";
    private static final String ARG_IS_GROUP = "is_group";
    private static final String ARG_IS_ARCHIVE = "is_archived";

    public static PostOptionsBottomSheetDialogFragment newInstance(@NonNull String postId, @Nullable GroupId groupId, boolean isArchived) {
        Bundle args = new Bundle();
        args.putString(ARG_POST_ID, postId);
        args.putBoolean(ARG_IS_GROUP, groupId != null);
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
        boolean isGroup = args.getBoolean(ARG_IS_GROUP, false);
        boolean showExternalShare = ServerProps.getInstance().getExternalSharing() && !isGroup;

        viewModel = new ViewModelProvider(this, new PostOptionsViewModel.Factory(postId, isArchived)).get(PostOptionsViewModel.class);
        contactLoader = new ContactLoader();

        final View view = inflater.inflate(R.layout.post_menu_bottom_sheet, container, false);
        final TextView contactName = view.findViewById(R.id.contact_name);

        final View saveToGallery = view.findViewById(R.id.save_to_gallery);
        final View shareExternally = view.findViewById(R.id.share_externally);
        final View copyLink = view.findViewById(R.id.copy_link);
        final View deletePost = view.findViewById(R.id.delete_post);
        viewModel.post.getLiveData().observe(this, post -> {
            if (post == null) {
                saveToGallery.setVisibility(View.INVISIBLE);
                deletePost.setVisibility(View.INVISIBLE);
                return;
            }
            contactLoader.cancel(contactName);
            if (post.senderUserId.isMe()) {
                contactName.setText(R.string.my_post);
                deletePost.setVisibility(View.VISIBLE);
                shareExternally.setVisibility(showExternalShare ? View.VISIBLE : View.GONE);
                copyLink.setVisibility(showExternalShare ? View.VISIBLE : View.GONE);
            } else {
                contactLoader.load(contactName, post.senderUserId, false);
                deletePost.setVisibility(View.GONE);
                shareExternally.setVisibility(View.GONE);
                copyLink.setVisibility(View.GONE);
            }
            if (post.media.isEmpty() || !Media.canBeSavedToGallery(post.media)) {
                saveToGallery.setVisibility(View.GONE);
            } else {
                saveToGallery.setVisibility(View.VISIBLE);
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
        deletePost.setOnClickListener(v -> {
            Post post = viewModel.post.getLiveData().getValue();
            if (post != null) {
                DialogInterface.OnClickListener listener = post.isArchived ?
                        (dialog, which) -> {
                            ContentDb.getInstance().removePostFromArchive(post);
                            this.getActivity().finish();
                        } :
                        (dialog, which) -> ContentDb.getInstance().retractPost(post);

                new AlertDialog.Builder(requireContext())
                        .setMessage(getString(R.string.retract_post_confirmation))
                        .setCancelable(true)
                        .setPositiveButton(R.string.yes, listener)
                        .setNegativeButton(R.string.no, null)
                        .show();
            }
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

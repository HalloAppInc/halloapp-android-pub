package com.halloapp.ui;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import com.google.protobuf.ByteString;
import com.halloapp.R;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Media;
import com.halloapp.content.Post;
import com.halloapp.crypto.CryptoByteUtils;
import com.halloapp.crypto.CryptoUtils;
import com.halloapp.props.ServerProps;
import com.halloapp.proto.clients.Container;
import com.halloapp.proto.clients.PostContainer;
import com.halloapp.proto.server.ExternalSharePost;
import com.halloapp.proto.server.Iq;
import com.halloapp.util.IntentUtils;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.SnackbarHelper;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.ExternalShareResponseIq;
import com.halloapp.xmpp.HalloIq;
import com.halloapp.xmpp.feed.FeedContentEncoder;
import com.halloapp.xmpp.util.Observable;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

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
        contactLoader = new ContactLoader();

        final View view = inflater.inflate(R.layout.post_menu_bottom_sheet, container, false);
        final TextView contactName = view.findViewById(R.id.contact_name);

        final View saveToGallery = view.findViewById(R.id.save_to_gallery);
        final View resharePost = view.findViewById(R.id.reshare_post);
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
                resharePost.setVisibility(ServerProps.getInstance().getIsInternalUser() ? View.VISIBLE : View.GONE);
            } else {
                contactLoader.load(contactName, post.senderUserId, false);
                deletePost.setVisibility(View.GONE);
                resharePost.setVisibility(View.GONE);
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
        resharePost.setOnClickListener(v -> {
            PostContainer.Builder builder = PostContainer.newBuilder();
            // TODO(jack): Fix for voice notes
            Container.Builder containerBuilder = Container.newBuilder();
            FeedContentEncoder.encodePost(containerBuilder, viewModel.post.getLiveData().getValue());
            PostContainer postContainer = containerBuilder.getPostContainer();
            if (postContainer.hasAlbum()) {
                builder.setAlbum(postContainer.getAlbum());
            } else if (postContainer.hasText()) {
                builder.setText(postContainer.getText());
            }

            byte[] payload = builder.build().toByteArray();

            byte[] attachmentKey = new byte[15];
            new SecureRandom().nextBytes(attachmentKey);
            try {
                byte[] fullKey = CryptoUtils.hkdf(attachmentKey, null, "HalloApp Share Post".getBytes(StandardCharsets.UTF_8), 80);
                byte[] iv = Arrays.copyOfRange(fullKey, 0, 16);
                byte[] aesKey = Arrays.copyOfRange(fullKey, 16, 48);
                byte[] hmacKey = Arrays.copyOfRange(fullKey, 48, 80);

                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(aesKey, "AES"), new IvParameterSpec(iv));
                byte[] encrypted = cipher.doFinal(payload);

                Mac mac = Mac.getInstance("HmacSHA256");
                mac.init(new SecretKeySpec(hmacKey, "HmacSHA256"));
                byte[] hmac = mac.doFinal(encrypted);

                byte[] encryptedPayload = CryptoByteUtils.concat(encrypted, hmac);

                final Observable<ExternalShareResponseIq> observable = Connection.getInstance().sendRequestIq(new HalloIq() {
                    @Override
                    public Iq toProtoIq() {
                        ExternalSharePost externalSharePost = ExternalSharePost.newBuilder()
                                .setAction(ExternalSharePost.Action.STORE)
                                .setBlob(ByteString.copyFrom(encryptedPayload))
                                .setExpiresInSeconds(3 * 60 * 60 * 24)
                                .build();
                        return Iq.newBuilder()
                                .setId(RandomId.create())
                                .setType(Iq.Type.SET)
                                .setExternalSharePost(externalSharePost)
                                .build();
                    }
                });
                observable.onError(e -> Log.e("Failed to send for external sharing"))
                        .onResponse(response -> {
                            Log.i("Got external sharing response " + response);
                            String url = "https://share.halloapp.com/" + response.blobId + "?k=" + Base64.encodeToString(attachmentKey, Base64.NO_WRAP | Base64.URL_SAFE);
                            IntentUtils.openUrlInBrowser(v, url);
                        });
            } catch (GeneralSecurityException e) {
                Log.e("Failed to encrypt for external sharing", e);
            }
            dismiss();
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
                SnackbarHelper.showInfo(parent, R.string.media_save_to_gallery_failed);
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

package com.halloapp.ui.contacts;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.halloapp.BuildConfig;
import com.halloapp.R;
import com.halloapp.util.DialogFragmentUtils;
import com.halloapp.util.StringUtils;

import java.util.Arrays;

import pub.devrel.easypermissions.EasyPermissions;

public class ContactPermissionBottomSheetDialog extends BottomSheetDialogFragment {

    private static final int REQUEST_CODE_CONTACT_PERMISSIONS = 1;
    private static final int REQUEST_CODE_SETTINGS = 2;

    private static final String ARG_REQUEST_CODE = "request_code";

    public static ContactPermissionBottomSheetDialog newInstance(int requestCode) {
        ContactPermissionBottomSheetDialog instance = new ContactPermissionBottomSheetDialog();

        Bundle args = new Bundle();
        args.putInt(ARG_REQUEST_CODE, requestCode);
        instance.setArguments(args);

        return instance;
    }

    public static void showRequest(FragmentManager fragmentManager, int requestCode) {
        DialogFragmentUtils.showDialogFragmentOnce(ContactPermissionBottomSheetDialog.newInstance(requestCode), fragmentManager);
    }

    private static final String[] permissions = new String[] { Manifest.permission.READ_CONTACTS };

    private EasyPermissions.PermissionCallbacks permissionCallbacks;

    private TextView info;
    private TextView continueButton;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_CONTACT_PERMISSIONS: {
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (permissionCallbacks != null) {
                        permissionCallbacks.onPermissionsGranted(requireArguments().getInt(ARG_REQUEST_CODE), Arrays.asList(permissions));
                    }
                } else {
                    if (permissionCallbacks != null) {
                        permissionCallbacks.onPermissionsDenied(requireArguments().getInt(ARG_REQUEST_CODE), Arrays.asList(permissions));
                    }
                }
                dismiss();
                break;
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.info_bottom_sheet, container, false);

        TextView title = view.findViewById(R.id.title);
        info = view.findViewById(R.id.info);
        title.setText(R.string.contact_permissions_dialog_title);

        Bundle args = requireArguments();

        int requestCode = args.getInt(ARG_REQUEST_CODE);

        Spanned current = replaceLearnMore(info.getText());
        info.setText(current);
        info.setMovementMethod(LinkMovementMethod.getInstance());

        continueButton = view.findViewById(R.id.button2);
        continueButton.setText(R.string.continue_button);
        continueButton.setOnClickListener(v -> {
            if (EasyPermissions.permissionPermanentlyDenied(requireActivity(), Manifest.permission.READ_CONTACTS)) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .setData(Uri.fromParts("package", BuildConfig.APPLICATION_ID, null));
                startActivityForResult(intent, REQUEST_CODE_SETTINGS);
            } else {
                // You can directly ask for the permission.
                requestPermissions(permissions,
                        REQUEST_CODE_CONTACT_PERMISSIONS);
            }
        });

        Button notNow = view.findViewById(R.id.button1);
        notNow.setText(R.string.not_now);
        notNow.setVisibility(View.VISIBLE);
        notNow.setOnClickListener(v -> {
            dismiss();
            if (permissionCallbacks != null) {
                permissionCallbacks.onPermissionsDenied(requestCode, Arrays.asList(permissions));
            }
        });

        updateNagContents();
        return view;
    }

    private Spanned replaceLearnMore(CharSequence text) {
        return StringUtils.replaceLink(requireContext(), text, "learn-more", () -> {
            DialogFragmentUtils.showDialogFragmentOnce(new ContactHashInfoBottomSheetDialogFragment(), getChildFragmentManager());
        });
    }

    private void updateNagContents() {
        if (EasyPermissions.permissionPermanentlyDenied(requireActivity(), Manifest.permission.READ_CONTACTS)) {
            info.setText(replaceLearnMore(Html.fromHtml(getString(R.string.contact_permissions_nag_permanently_blocked))));
            continueButton.setText(R.string.settings);
        } else {
            info.setText(replaceLearnMore(Html.fromHtml(getString(R.string.contact_permissions_nag))));
            continueButton.setText(R.string.continue_button);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateNagContents();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (getParentFragment() != null) {
            if (getParentFragment() instanceof EasyPermissions.PermissionCallbacks) {
                permissionCallbacks = (EasyPermissions.PermissionCallbacks) getParentFragment();
            }
        }

        if (context instanceof EasyPermissions.PermissionCallbacks) {
            permissionCallbacks = (EasyPermissions.PermissionCallbacks) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        permissionCallbacks = null;
    }
}

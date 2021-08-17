package com.halloapp.ui;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.halloapp.permissions.PermissionWatcher;

import pub.devrel.easypermissions.EasyPermissions;

public class HalloBottomSheetDialogFragment extends BottomSheetDialogFragment {
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
        PermissionWatcher.getInstance().onRequestPermissionsResult(permissions, grantResults);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new HalloBottomSheetDialog(requireActivity(), getTheme());
    }
}

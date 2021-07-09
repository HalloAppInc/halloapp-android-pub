package com.halloapp.ui;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.halloapp.permissions.PermissionWatcher;

import pub.devrel.easypermissions.EasyPermissions;

public class HalloBottomSheetDialogFragment extends BottomSheetDialogFragment {
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionWatcher.getInstance().onRequestPermissionsResult(permissions, grantResults);
    }
}

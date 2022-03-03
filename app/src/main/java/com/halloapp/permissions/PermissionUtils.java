package com.halloapp.permissions;

import android.Manifest;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.halloapp.ui.contacts.ContactPermissionBottomSheetDialog;
import com.halloapp.util.logs.Log;

import pub.devrel.easypermissions.EasyPermissions;

public class PermissionUtils {
    public static boolean hasOrRequestContactPermissions(@NonNull FragmentActivity activity, int requestCode) {
        if (Build.VERSION.SDK_INT >= 23) {
            final String[] perms = {Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS};
            if (!EasyPermissions.hasPermissions(activity, perms)) {
                if (EasyPermissions.hasPermissions(activity, Manifest.permission.READ_CONTACTS)) {
                    Log.i("PermissionUtils/checkContactPermissions read contacts already granted");
                    activity.requestPermissions(perms, requestCode);
                } else {
                    ContactPermissionBottomSheetDialog.showRequest(activity.getSupportFragmentManager(), requestCode);
                }
                return false;
            }
        }
        return true;
    }
}

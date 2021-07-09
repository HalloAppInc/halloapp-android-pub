package com.halloapp.permissions;

import androidx.annotation.NonNull;

public abstract class PermissionObserver {
    public abstract void onWatchedPermissionGranted(@NonNull String permission);
}

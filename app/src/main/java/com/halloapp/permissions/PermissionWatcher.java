package com.halloapp.permissions;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.OnLifecycleEvent;

import com.halloapp.AppContext;
import com.halloapp.util.Preconditions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import pub.devrel.easypermissions.EasyPermissions;

public class PermissionWatcher implements LifecycleObserver {

    private static final String[] WATCHED_PERMISSIONS = { Manifest.permission.READ_CONTACTS };
    private final Set<PermissionObserver> observers = new HashSet<>();

    private static PermissionWatcher instance;

    private final AppContext appContext;

    public static PermissionWatcher getInstance() {
        if (instance == null) {
            synchronized (PermissionWatcher.class) {
                if (instance == null) {
                    instance = new PermissionWatcher(AppContext.getInstance());
                }
            }
        }
        return instance;
    }

    private final HashMap<String, MutableLiveData<Boolean>> permissionLiveDatas = new HashMap<>();

    private final HashSet<String> deniedPermissions = new HashSet<>();

    private PermissionWatcher(@NonNull AppContext appContext) {
        this.appContext = appContext;

        Context context = appContext.get();
        for (String perm : WATCHED_PERMISSIONS) {
            boolean hasPermission = EasyPermissions.hasPermissions(context, perm);
            if (!hasPermission) {
                deniedPermissions.add(perm);
            }
            permissionLiveDatas.put(perm, new MutableLiveData<>(hasPermission));
        }
    }

    public void addObserver(@NonNull PermissionObserver observer) {
        synchronized (observers) {
            observers.add(observer);
        }
    }

    public void removeObserver(@NonNull PermissionObserver observer) {
        synchronized (observers) {
            observers.remove(observer);
        }
    }

    public void notifyPermissionGranted(@NonNull String permission) {
        synchronized (observers) {
            for (PermissionObserver observer : observers) {
                observer.onWatchedPermissionGranted(permission);
            }
        }
    }

    public LiveData<Boolean> getPermissionLiveData(@NonNull String permission) {
        return permissionLiveDatas.get(permission);
    }

    private void updatePermissionLiveData(@NonNull String permission, boolean hasPermission) {
        Preconditions.checkNotNull(permissionLiveDatas.get(permission)).postValue(hasPermission);
    }

    @SuppressWarnings("unused")
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    void onResume() {
        checkChangedPermissions();
    }

    @MainThread
    private void checkChangedPermissions() {
        Context context = appContext.get();
        Iterator<String> deniedIterator = deniedPermissions.iterator();
        while (deniedIterator.hasNext()) {
            String perm = deniedIterator.next();
            if (EasyPermissions.hasPermissions(context, perm)) {
                deniedIterator.remove();
                notifyPermissionGranted(perm);
                updatePermissionLiveData(perm, true);
            }
        }
    }

    public void onRequestPermissionsResult(@NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int i = 0; i < permissions.length; i++) {
            String perm = permissions[i];
            if (deniedPermissions.contains(perm)) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    deniedPermissions.remove(perm);
                    notifyPermissionGranted(perm);
                    updatePermissionLiveData(perm, true);
                } else {
                    updatePermissionLiveData(perm, false);
                }
            }
        }
    }
}

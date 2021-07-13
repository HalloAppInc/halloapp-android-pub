package com.halloapp.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.halloapp.permissions.PermissionWatcher;
import com.halloapp.util.logs.Log;
import com.halloapp.util.ThreadUtils;

import pub.devrel.easypermissions.EasyPermissions;

public class HalloActivity extends AppCompatActivity {

    private final String activityName;

    public HalloActivity() {
        super();
        activityName = this.getClass().getSimpleName();
    }

    protected String getActivityName() {
        return activityName;
    }

    protected void logTrace(String section, String... args) {
        StringBuilder sb = new StringBuilder();
        sb.append(getActivityName()).append(".").append(section);
        if (args != null) {
            for (String arg : args) {
                sb.append(" ").append(arg);
            }
        }
        Log.i(sb.toString());
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        logTrace("onCreate");

        if (Build.VERSION.SDK_INT >= 26) {
            if (getResources().getConfiguration().isScreenWideColorGamut() && getWindowManager().getDefaultDisplay().isWideColorGamut()) {
                getWindow().setColorMode(ActivityInfo.COLOR_MODE_WIDE_COLOR_GAMUT);
            }
        }

        ThreadUtils.runWithoutStrictModeRestrictions(() -> {
            super.onCreate(savedInstanceState);
        });
    }

    @Override
    protected void onStart() {
        logTrace("onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        logTrace("onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        logTrace("onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        logTrace("onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        logTrace("onDestroy");
        super.onDestroy();
    }

    @Override
    public void startActivity(Intent intent) {
        ComponentName component = intent.getComponent();
        logTrace("startActivity", component == null ? "" : component.getShortClassName());
        ThreadUtils.runWithoutStrictModeRestrictions(() -> {
            super.startActivity(intent);
        });
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        ComponentName component = intent.getComponent();
        logTrace("startActivityForResult", Integer.toString(requestCode), component == null ? "" : component.getShortClassName());
        ThreadUtils.runWithoutStrictModeRestrictions(() -> {
            super.startActivityForResult(intent, requestCode);
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
        PermissionWatcher.getInstance().onRequestPermissionsResult(permissions, grantResults);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

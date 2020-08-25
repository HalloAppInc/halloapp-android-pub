package com.halloapp.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.halloapp.util.Log;
import com.halloapp.util.ThreadUtils;

public class HalloActivity extends AppCompatActivity {

    private String activityName;

    public HalloActivity() {
        super();
        activityName = this.getClass().getSimpleName();
    }

    protected String getActivityName() {
        return activityName;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.i(activityName + ".onCreate");
        ThreadUtils.runWithoutStrictModeRestrictions(() -> {
            super.onCreate(savedInstanceState);
        });
    }

    @Override
    protected void onStart() {
        Log.i(getActivityName() + ".onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.i(getActivityName() + ".onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.i(getActivityName() + ".onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.i(getActivityName() + ".onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.i(getActivityName() + ".onDestroy");
        super.onDestroy();
    }

    @Override
    public void startActivity(Intent intent) {
        ThreadUtils.runWithoutStrictModeRestrictions(() -> {
            super.startActivity(intent);
        });
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        ThreadUtils.runWithoutStrictModeRestrictions(() -> {
            super.startActivityForResult(intent, requestCode);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

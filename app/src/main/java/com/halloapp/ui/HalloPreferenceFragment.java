package com.halloapp.ui;

import android.content.Intent;

import androidx.preference.PreferenceFragmentCompat;

import com.halloapp.util.ThreadUtils;

public abstract class HalloPreferenceFragment extends PreferenceFragmentCompat {
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
}

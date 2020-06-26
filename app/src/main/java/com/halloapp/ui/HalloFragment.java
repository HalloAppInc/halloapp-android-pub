package com.halloapp.ui;

import android.content.Intent;

import androidx.fragment.app.Fragment;

import com.halloapp.util.ThreadUtils;

public class HalloFragment extends Fragment {
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

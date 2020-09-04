package com.halloapp.ui;

import android.content.ComponentName;
import android.content.Intent;

import androidx.fragment.app.Fragment;

import com.halloapp.util.Log;
import com.halloapp.util.ThreadUtils;

public class HalloFragment extends Fragment {

    private final String fragmentName;

    public HalloFragment() {
        super();
        fragmentName = getClass().getSimpleName();
    }

    protected String getFragmentName() {
        return fragmentName;
    }

    protected void logTrace(String section, String... args) {
        StringBuilder sb = new StringBuilder();
        sb.append(getFragmentName()).append(".").append(section);
        if (args != null) {
            for (String arg : args) {
                sb.append(" ").append(arg);
            }
        }
        Log.i(sb.toString());
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
}

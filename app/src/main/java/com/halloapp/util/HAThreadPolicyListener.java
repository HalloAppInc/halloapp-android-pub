package com.halloapp.util;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.os.strictmode.Violation;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.halloapp.AppContext;
import com.halloapp.util.logs.Log;

@RequiresApi(api = Build.VERSION_CODES.P)
public class HAThreadPolicyListener implements StrictMode.OnThreadViolationListener {

    @Override
    public void onThreadViolation(Violation v) {
        Log.e("THREAD POLICY VIOLATION: ", v);
        String error = android.util.Log.getStackTraceString(v);
        if (error.contains("com.halloapp")) {
            throw new RuntimeException("StrictMode ThreadPolicy violation", v);
        }
    }
}

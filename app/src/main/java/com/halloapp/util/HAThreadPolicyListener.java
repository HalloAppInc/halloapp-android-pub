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

    private final AppContext appContext;

    private Handler mainHandler;

    public HAThreadPolicyListener() {
        appContext = AppContext.getInstance();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onThreadViolation(Violation v) {
        Log.e("THREAD POLICY VIOLATION: ", v);
        String error = android.util.Log.getStackTraceString(v);
        if (error.contains("com.halloapp")) {
            throw new RuntimeException("StrictMode ThreadPolicy violation", v);
        } else {
            mainHandler.post(() -> {
                Toast.makeText(appContext.get(), "Potential thread policy violation, check logs", Toast.LENGTH_SHORT).show();
            });
        }
    }
}

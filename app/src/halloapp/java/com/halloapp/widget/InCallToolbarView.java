package com.halloapp.widget;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.Window;
import android.widget.Chronometer;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.halloapp.R;
import com.halloapp.calling.calling.CallManager;
import com.halloapp.id.UserId;
import com.halloapp.ui.calling.calling.CallActivity;
import com.halloapp.util.ContextUtils;

public class InCallToolbarView extends LinearLayout {

    private final CallManager callManager = CallManager.getInstance();

    private Chronometer callDuration;

    public InCallToolbarView(Context context) {
        super(context);

        init();
    }

    public InCallToolbarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public InCallToolbarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_call_toolbar, this);

        setBackgroundColor(ContextCompat.getColor(getContext(), R.color.color_primary));

        callDuration = findViewById(R.id.call_duration_timer);

        setOnClickListener(v -> {
            final Context context = getContext();
            final UserId peerUid = callManager.getPeerUid();
            if (peerUid != null) {
                context.startActivity(CallActivity.getReturnToCallIntent(context, peerUid));
            }
        });
    }

    private boolean chronometerSet = false;
    private int oldStatusBarColor;
    private int systemUiVisibility;

    public void bind(LifecycleOwner lifecycleOwner) {
        callManager.getCallStartTimeLiveData().observe(lifecycleOwner, startTime -> {
            if (startTime == null) {
                setVisibility(View.GONE);
                callDuration.stop();
                chronometerSet = false;
                if (oldStatusBarColor != 0) {
                    Activity activity = ContextUtils.getActivity(getContext());
                    if (activity != null) {
                        Window window = activity.getWindow();
                        window.setStatusBarColor(oldStatusBarColor);
                        if (Build.VERSION.SDK_INT >= 23) {
                            window.getDecorView().setSystemUiVisibility(systemUiVisibility);
                        }
                    }
                }
            } else {
                setVisibility(View.VISIBLE);
                if (!chronometerSet) {
                    chronometerSet = true;
                    Activity activity = ContextUtils.getActivity(getContext());
                    if (activity != null) {
                        Window window = activity.getWindow();
                        oldStatusBarColor = window.getStatusBarColor();
                        window.setStatusBarColor(ContextCompat.getColor(getContext(), R.color.color_primary));
                        if (Build.VERSION.SDK_INT >= 23) {
                            systemUiVisibility = window.getDecorView().getSystemUiVisibility();
                            window.getDecorView().setSystemUiVisibility(systemUiVisibility & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                        }
                    }
                    callDuration.setBase(startTime);
                    callDuration.start();
                }
            }
        });
    }
}

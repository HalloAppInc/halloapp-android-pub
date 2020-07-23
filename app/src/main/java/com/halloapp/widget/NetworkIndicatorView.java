package com.halloapp.widget;

import android.content.Context;
import android.net.NetworkInfo;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

import com.halloapp.NetworkConnectivityManager;
import com.halloapp.R;

public class NetworkIndicatorView extends FrameLayout {

    private NetworkConnectivityManager networkConnectivityManager = NetworkConnectivityManager.getInstance();

    private TextView statusTextView;

    public NetworkIndicatorView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public NetworkIndicatorView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public NetworkIndicatorView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void bind(@NonNull LifecycleOwner lifecycleOwner) {
        networkConnectivityManager.getNetworkInfo().observe(lifecycleOwner, this::setNetworkInfo);
    }

    private void init(@NonNull Context context) {
        inflate(context, R.layout.view_network_indicator, this);
        statusTextView = findViewById(R.id.connection_text);
    }

    public void setNetworkInfo(@Nullable NetworkInfo networkInfo) {
        if (networkInfo == null) {
            setVisibility(View.VISIBLE);
            boolean isAirplaneMode = NetworkConnectivityManager.isAirplaneMode(getContext());
            statusTextView.setText(isAirplaneMode ? R.string.connectivity_info_airplane_mode : R.string.connectivity_info_not_connected);
        } else {
            setVisibility(View.GONE);
        }
    }
}

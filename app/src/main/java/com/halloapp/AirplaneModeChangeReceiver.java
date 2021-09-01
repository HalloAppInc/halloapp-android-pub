package com.halloapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AirplaneModeChangeReceiver extends BroadcastReceiver {

    private final NetworkConnectivityManager networkConnectivityManager = NetworkConnectivityManager.getInstance();

    @Override
    public void onReceive(Context context, Intent intent) {
        networkConnectivityManager.onAirplaneModeChanged();
    }
}

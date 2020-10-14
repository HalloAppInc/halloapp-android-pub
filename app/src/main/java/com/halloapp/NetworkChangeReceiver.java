package com.halloapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.halloapp.util.logs.Log;
import com.halloapp.util.Preconditions;

public class NetworkChangeReceiver extends BroadcastReceiver {

    private final NetworkConnectivityManager networkConnectivityManager = NetworkConnectivityManager.getInstance();

    @Override
    public void onReceive(final Context context, final Intent intent) {

        final ConnectivityManager connectivityManager = Preconditions.checkNotNull((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        Log.i("NetworkChangeReceiver: " + intent.getAction() + " " + (activeNetwork != null ? activeNetwork.getType() : "null"));
        networkConnectivityManager.onUpdatedNetworkInfo(activeNetwork);
        if (activeNetwork != null) {
            onConnected(activeNetwork.getType());
        } else {
            onDisconnected();
        }
    }

    public void onConnected(int type) {
    }

    public void onDisconnected() {
    }
}

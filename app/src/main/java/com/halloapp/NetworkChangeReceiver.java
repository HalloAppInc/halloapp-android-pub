package com.halloapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

import com.halloapp.util.logs.Log;
import com.halloapp.util.Preconditions;

public class NetworkChangeReceiver extends BroadcastReceiver {

    private final NetworkConnectivityManager networkConnectivityManager = NetworkConnectivityManager.getInstance();

    @Override
    public void onReceive(final Context context, final Intent intent) {

        final ConnectivityManager connectivityManager = Preconditions.checkNotNull((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        Log.i("NetworkChangeReceiver: " + intent.getAction() + " " + (activeNetworkInfo != null ? activeNetworkInfo.getTypeName() : "null"));
        networkConnectivityManager.onUpdatedNetworkInfo(activeNetworkInfo);
        if (activeNetworkInfo != null) {
            onConnected(activeNetworkInfo.getType());
        } else {
            onDisconnected();
        }

        if (Build.VERSION.SDK_INT >= 23) {
            Network activeNetwork = connectivityManager.getActiveNetwork();
            if (activeNetwork != null) {
                NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
                if (networkCapabilities != null) {
                    boolean usingVpn = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN);
                    Log.i("NetworkChangeReceiver: using vpn? " + usingVpn);
                    Log.i("NetworkChangeReceiver: all capabilities: " + networkCapabilities);
                }
            }
        }
    }

    public void onConnected(int type) {
    }

    public void onDisconnected() {
    }
}

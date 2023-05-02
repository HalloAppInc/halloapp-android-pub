package com.halloapp.calling.calling;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;

import androidx.annotation.NonNull;

import com.halloapp.util.logs.Log;

public class CallNetworkObserver extends ConnectivityManager.NetworkCallback {

    private static CallNetworkObserver instance;

    public static CallNetworkObserver getInstance() {
        if (instance == null) {
            synchronized (CallNetworkObserver.class) {
                if (instance == null) {
                    instance = new CallNetworkObserver();
                }
            }
        }
        return instance;
    }

    private ConnectivityManager connectivityManager;
    private boolean registered = false;

    public CallNetworkObserver() {
    }

    public void register(Context context) {
        if (registered) {
            return;
        }
        registered = true;
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        connectivityManager.registerNetworkCallback(networkRequest, this);
    }

    @Override
    public void onAvailable(@NonNull Network network) {
        String transportType = getNetworkType(network);
        Log.i("CallNetworkObserver: onAvailable(" + network + ") " + transportType);
    }

    @Override
    public void onLost(@NonNull Network network) {
        String transportType = getNetworkType(network);
        Log.i("CallNetworkObserver: onLost(" + network + ") " + transportType);
    }

    @Override
    public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
        Log.d("CallNetworkObserver: onCapabilitiesChanged() network:" + network + " cap:" + networkCapabilities);
    }

    @Override
    public void onLinkPropertiesChanged(@NonNull Network network, @NonNull LinkProperties linkProperties) {
        Log.d("CallNetworkObserver: onLinkPropertiesChanged() network:" + network + " props:" + linkProperties);
    }

    private String getNetworkType(Network network) {
        NetworkCapabilities nc = connectivityManager.getNetworkCapabilities(network);
        String transportType;
        if (nc != null && nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            transportType = "CELLULAR";
        } else if (nc != null && nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            transportType = "WIFI";
        } else {
            transportType = "UNKNOWN";
        }
        return transportType;
    }
}

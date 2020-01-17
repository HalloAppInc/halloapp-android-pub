package com.halloapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.core.util.Preconditions;

import com.halloapp.util.Log;

public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {

        final ConnectivityManager connectivityManager = Preconditions.checkNotNull((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        Log.i("NetworkChangeReceiver: " + intent.getAction() + " " + (activeNetwork != null ? activeNetwork.getType() : "null"));
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

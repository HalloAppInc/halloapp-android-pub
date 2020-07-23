package com.halloapp;

import android.content.Context;
import android.net.NetworkInfo;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

public class NetworkConnectivityManager {

    private static NetworkConnectivityManager instance;

    public static NetworkConnectivityManager getInstance() {
        if (instance == null) {
            synchronized (NetworkConnectivityManager.class) {
                if (instance == null) {
                    instance = new NetworkConnectivityManager();
                }
            }
        }
        return instance;
    }

    private MediatorLiveData<NetworkInfo> networkInfoLiveData;
    private MutableLiveData<Boolean> airplaneModeLiveData;

    private NetworkConnectivityManager() {
        airplaneModeLiveData = new MutableLiveData<>();
        networkInfoLiveData = new MediatorLiveData<>();
        networkInfoLiveData.addSource(airplaneModeLiveData, airplaneMode -> {
            networkInfoLiveData.setValue(networkInfoLiveData.getValue());
        });
    }

    private NetworkInfo networkInfo;

    public void onUpdatedNetworkInfo(@Nullable NetworkInfo networkInfo) {
        this.networkInfo = networkInfo;
        networkInfoLiveData.postValue(networkInfo);
    }

    public void onAirplaneModeChanged() {
        networkInfoLiveData.postValue(networkInfo);
    }

    public static boolean isAirplaneMode(Context context) {
        return Settings.System.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) == 1;
    }

    @NonNull
    public LiveData<NetworkInfo> getNetworkInfo() {
        return networkInfoLiveData;
    }

}

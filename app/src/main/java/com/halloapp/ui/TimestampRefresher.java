package com.halloapp.ui;

import android.os.Handler;
import android.os.Looper;
import android.text.format.DateUtils;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.halloapp.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimestampRefresher extends ViewModel {

    final MutableLiveData<Long> refresh = new MutableLiveData<>();

    private long refreshTimestampsTime = Long.MAX_VALUE;
    private final Runnable refreshTimestampsRunnable = () -> {
        Log.v("TimestampRefresher: refreshing timestamps at " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US).format(new Date(System.currentTimeMillis())));
        refreshTimestampsTime = Long.MAX_VALUE;
        refresh.setValue(System.currentTimeMillis());
    };
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    void scheduleTimestampRefresh(long postTimestamp) {
        long refreshTime = getRefreshTime(postTimestamp);
        if (refreshTime < refreshTimestampsTime) {
            refreshTimestampsTime = refreshTime;
            Log.v("TimestampRefresher: will refresh timestamps at " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US).format(new Date(refreshTimestampsTime)));
            mainHandler.removeCallbacks(refreshTimestampsRunnable);
            mainHandler.postDelayed(refreshTimestampsRunnable, refreshTimestampsTime - System.currentTimeMillis());
        }
    }

    @Override
    protected void onCleared() {
        mainHandler.removeCallbacks(refreshTimestampsRunnable);
    }

    private static long getRefreshTime(long time) {
        long now = System.currentTimeMillis();
        long timeDiff = now - time;
        long refreshResolution;
        if (timeDiff < DateUtils.HOUR_IN_MILLIS) {
            refreshResolution = DateUtils.MINUTE_IN_MILLIS;
        } else if (timeDiff < DateUtils.DAY_IN_MILLIS) {
            refreshResolution = DateUtils.HOUR_IN_MILLIS;
        } else {
            refreshResolution = DateUtils.DAY_IN_MILLIS;
        }
        return time + refreshResolution * ((timeDiff + refreshResolution - 1) / refreshResolution) + 1000L;
    }

}

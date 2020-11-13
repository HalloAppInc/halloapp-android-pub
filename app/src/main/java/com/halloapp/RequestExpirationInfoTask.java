package com.halloapp;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.ui.AppExpirationActivity;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.util.ObservableErrorException;

import java.util.concurrent.ExecutionException;

public class RequestExpirationInfoTask extends AsyncTask<Void, Void, Integer> {

    private static final int EXPIRES_SOON_THRESHOLD_DAYS = 7;

    private static final int SECONDS_PER_DAY = 60 * 60 * 24;

    private final Connection connection;
    private final Context context;

    public RequestExpirationInfoTask(@NonNull Connection connection, @NonNull Context context) {
        this.connection = connection;
        this.context = context.getApplicationContext();
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        try {
            return connection.requestSecondsToExpiration().await();
        } catch (InterruptedException | ObservableErrorException e) {
            Log.e("RequestExpirationInfoTask: failed to get days_left", e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(@Nullable Integer secondsLeft) {
        if (secondsLeft != null) {
            int daysLeft = (secondsLeft / SECONDS_PER_DAY) + 1;
            Log.d("RequestExpirationInfoTask onPostExecute daysLeft=" + daysLeft);
            if (daysLeft <= EXPIRES_SOON_THRESHOLD_DAYS) {
                if (daysLeft <= 0) {
                    connection.clientExpired();
                }
                if (ForegroundObserver.getInstance().isInForeground()) {
                    AppExpirationActivity.open(context, daysLeft);
                } else {
                    Notifications.getInstance(context).showExpirationNotification(daysLeft);
                }
            }
        }
    }
}

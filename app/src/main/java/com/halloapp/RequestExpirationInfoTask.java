package com.halloapp;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.ui.AppExpirationActivity;
import com.halloapp.util.Log;
import com.halloapp.xmpp.Connection;

import java.util.concurrent.ExecutionException;

public class RequestExpirationInfoTask extends AsyncTask<Void, Void, Integer> {

    private static final int EXPIRES_SOON_THRESHOLD_DAYS = 7;

    private final Connection connection;
    private final Context context;

    public RequestExpirationInfoTask(@NonNull Connection connection, @NonNull Context context) {
        this.connection = connection;
        this.context = context;
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        try {
            return connection.requestDaysToExpiration().get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e("RequestExpirationInfoTask: failed to get days_left", e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(@Nullable Integer daysLeft) {
        Log.d("RequestExpirationInfoTask onPostExecute daysLeft=" + daysLeft);
        if (daysLeft != null && daysLeft <= EXPIRES_SOON_THRESHOLD_DAYS) {
            if (daysLeft <= 0) {
                connection.clientExpired();
            }
            AppExpirationActivity.open(context, daysLeft);
        }
    }
}

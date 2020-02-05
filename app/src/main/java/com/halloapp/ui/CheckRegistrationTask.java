package com.halloapp.ui;

import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.halloapp.Me;
import com.halloapp.Preferences;

public class CheckRegistrationTask extends AsyncTask<Void, Void, Void> {

    private final Me me;
    private final Preferences preferences;

    public final MutableLiveData<CheckResult> result = new MutableLiveData<>();

    public CheckRegistrationTask(@NonNull Me me, @NonNull Preferences preferences) {
        this.me = me;
        this.preferences = preferences;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        result.postValue(new CheckResult(me.isRegistered(), preferences.getLastSyncTime()));
        return null;
    }

    static class CheckResult {
        final boolean registered;
        final long lastSyncTime;

        public CheckResult(boolean registered, long lastSyncTime) {
            this.registered = registered;
            this.lastSyncTime = lastSyncTime;
        }
    }
}

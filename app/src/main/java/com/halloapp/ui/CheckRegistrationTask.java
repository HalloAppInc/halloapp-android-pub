package com.halloapp.ui;

import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.halloapp.Me;
import com.halloapp.Preferences;
import com.halloapp.registration.CheckRegistration;

public class CheckRegistrationTask extends AsyncTask<Void, Void, Void> {

    private final Me me;
    private final Preferences preferences;

    public final MutableLiveData<CheckRegistration.CheckResult> result = new MutableLiveData<>();

    CheckRegistrationTask(@NonNull Me me, @NonNull Preferences preferences) {
        this.me = me;
        this.preferences = preferences;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        result.postValue(CheckRegistration.checkRegistration(me, preferences));
        return null;
    }
}

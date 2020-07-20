package com.halloapp.ui.settings;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.halloapp.Me;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;

public class SettingsProfileViewModel extends AndroidViewModel {

    private Me me;
    private BgWorkers bgWorkers;

    private ComputableLiveData<String> phoneNumberLiveData;

    public SettingsProfileViewModel(@NonNull Application application) {
        super(application);

        me = Me.getInstance(application);
        bgWorkers = BgWorkers.getInstance();

        phoneNumberLiveData = new ComputableLiveData<String>() {
            @Override
            protected String compute() {
                return me.getPhone();
            }
        };
        phoneNumberLiveData.invalidate();
        bgWorkers.execute(() -> me.getName());
    }

    public LiveData<String> getPhone() {
        return phoneNumberLiveData.getLiveData();
    }

    public LiveData<String> getName() {
        return me.name;
    }
}

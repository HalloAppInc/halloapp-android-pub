package com.halloapp.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.halloapp.Me;
import com.halloapp.util.ComputableLiveData;

public class MyProfileViewModel extends ViewModel {

    private Me me = Me.getInstance();

    private ComputableLiveData<String> phoneNumberLiveData;

    public MyProfileViewModel() {
        phoneNumberLiveData = new ComputableLiveData<String>() {
            @Override
            protected String compute() {
                return me.getPhone();
            }
        };
    }

    public LiveData<String> getPhone() {
        return phoneNumberLiveData.getLiveData();
    }

    public LiveData<String> getName() {
        return me.name;
    }
}

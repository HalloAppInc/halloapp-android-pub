package com.halloapp.ui;

import android.annotation.SuppressLint;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.halloapp.Me;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.StringUtils;

public class MyProfileViewModel extends ViewModel {

    private final Me me = Me.getInstance();

    private final ComputableLiveData<String> phoneNumberLiveData;

    public MyProfileViewModel() {
        phoneNumberLiveData = new ComputableLiveData<String>() {
            @SuppressLint("RestrictedApi")
            @Override
            protected String compute() {
                return StringUtils.formatPhoneNumber(me.getPhone());
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

package com.halloapp.util;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

/*
* TODO : implement ComputableLiveData here unless google changes its mind and makes androidx.lifecycle.ComputableLiveData "public"
* */
@SuppressLint("RestrictedApi")
public abstract class ComputableLiveData<T> extends androidx.lifecycle.ComputableLiveData<T> {

    @Override
    public @NonNull LiveData<T> getLiveData() {
        return super.getLiveData();
    }

    @Override
    public void invalidate() {
        super.invalidate();
    }
}

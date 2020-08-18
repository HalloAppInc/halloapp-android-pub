package com.halloapp.props;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.halloapp.util.Log;

public class BooleanProp implements Prop {

    private final String key;
    private final boolean defaultValue;
    private boolean value;

    public BooleanProp(@NonNull String key, boolean defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public boolean getValue() {
        return value;
    }

    @NonNull
    @Override
    public String getKey() {
        return key;
    }

    @Override
    public void parse(@NonNull String string) {
         value = Boolean.parseBoolean(string);
    }

    @Override
    public void load(@NonNull SharedPreferences sharedPreferences) {
        value = sharedPreferences.getBoolean(key, defaultValue);
    }

    @Override
    public void save(@NonNull SharedPreferences sharedPreferences) {
        sharedPreferences.edit().putBoolean(key, value).apply();
    }
}

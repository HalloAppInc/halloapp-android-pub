package com.halloapp.props;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.halloapp.util.logs.Log;

public class StringProp implements Prop {

    private final String key;
    private final String defaultValue;
    private String value;

    public StringProp(@NonNull String propKey, String defaultValue) {
        this.key = propKey;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public String getValue() {
        return value;
    }

    @NonNull
    @Override
    public String getKey() {
        return key;
    }

    @Override
    public void parse(@NonNull String string) {
        value = string;
    }

    @Override
    public void load(@NonNull SharedPreferences sharedPreferences) {
        value = sharedPreferences.getString(key, defaultValue);
    }

    @Override
    public void save(@NonNull SharedPreferences sharedPreferences) {
        sharedPreferences.edit().putString(key, value).apply();
    }
}

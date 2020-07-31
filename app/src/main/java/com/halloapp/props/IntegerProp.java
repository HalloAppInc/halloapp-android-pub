package com.halloapp.props;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.halloapp.util.Log;

public class IntegerProp implements Prop {

    private final String key;
    private final int defaultValue;
    private int value;

    public IntegerProp(@NonNull String propKey, int defaultValue) {
        this.key = propKey;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public int getValue() {
        return value;
    }

    @NonNull
    @Override
    public String getKey() {
        return key;
    }

    @Override
    public void parse(@NonNull String string) {
        Integer parsedValue = null;
        try {
            parsedValue = Integer.parseInt(string);
        } catch (Exception e) {
            Log.e("IntegerProp/invalid prop received for " + key + ": " + string, e);
        }
        if (parsedValue != null) {
            value = parsedValue;
        }
    }

    @Override
    public void load(@NonNull SharedPreferences sharedPreferences) {
        sharedPreferences.getInt(key, defaultValue);
    }

    @Override
    public void save(@NonNull SharedPreferences sharedPreferences) {
        sharedPreferences.edit().putInt(key, defaultValue).apply();
    }
}

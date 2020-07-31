package com.halloapp.props;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;

public interface Prop {
    @NonNull String getKey();
    void parse(@NonNull String string);
    void load(@NonNull SharedPreferences sharedPreferences);
    void save(@NonNull SharedPreferences sharedPreferences);
}

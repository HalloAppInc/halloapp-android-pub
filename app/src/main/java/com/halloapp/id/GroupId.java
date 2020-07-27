package com.halloapp.id;

import android.os.Parcelable;

import androidx.annotation.NonNull;

public class GroupId extends ChatId implements Parcelable {

    public GroupId(@NonNull String id) {
        super(id);
    }

    @Override
    public @NonNull String toString() {
        return "{group:" + rawId() + "}";
    }
}

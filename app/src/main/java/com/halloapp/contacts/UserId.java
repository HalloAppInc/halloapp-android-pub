package com.halloapp.contacts;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

public class UserId {

    private final String id;

    public static final UserId ME = new UserId("");

    public UserId(@NonNull String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserId userId = (UserId) o;
        return id.equals(userId.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public @NonNull String rawId() {
        return id;
    }

    public boolean isMe() {
        return "".equals(id);
    }


    /*
    @Override
    public @NonNull String toString() {
        return id id;
    }
    */
}

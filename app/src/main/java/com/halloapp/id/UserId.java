package com.halloapp.id;

import android.os.Parcelable;

import androidx.annotation.NonNull;

public class UserId extends ChatId implements Parcelable {

    public static final UserId ME = new UserId("");

    public UserId(@NonNull String id) {
        super(id);
    }

    public boolean isMe() {
        return "".equals(rawId());
    }

    @Override
    public @NonNull String toString() {
        return "{user:" + (isMe() ? "me" : rawId()) + "}";
    }
}

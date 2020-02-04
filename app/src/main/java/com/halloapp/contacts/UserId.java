package com.halloapp.contacts;

import android.telephony.PhoneNumberUtils;

import androidx.annotation.NonNull;

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

    public String formatPhoneNumber() {
        return PhoneNumberUtils.formatNumber("+" + id, null);
    }

    @Override
    public @NonNull String toString() {
        return "{user:" + (isMe() ? "me" : id) + "}";
    }
}

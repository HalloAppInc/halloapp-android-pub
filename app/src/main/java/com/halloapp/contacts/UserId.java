package com.halloapp.contacts;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Objects;

public class UserId implements Parcelable {

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

    @Override
    public @NonNull String toString() {
        return "{user:" + (isMe() ? "me" : id) + "}";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
    }

    public static final Parcelable.Creator<UserId> CREATOR
            = new Parcelable.Creator<UserId>() {
        public UserId createFromParcel(Parcel in) {
            String id = in.readString();
            id = id == null ? "" : id;
            return new UserId(id);
        }

        public UserId[] newArray(int size) {
            return new UserId[size];
        }
    };
}

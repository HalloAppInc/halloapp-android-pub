package com.halloapp.id;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.util.Log;

import java.util.Objects;

public abstract class ChatId implements Parcelable {

    public @Nullable static ChatId fromNullable(@Nullable String s) {
        if (s == null) {
            Log.w("Returning null ChatId for null String");
            return null;
        } else if (s.startsWith("g")) {
            return new GroupId(s);
        } else {
            return new UserId(s);
        }
    }

    private final String id;

    protected ChatId(@NonNull String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatId userId = (ChatId) o;
        return id.equals(userId.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public @NonNull String rawId() {
        return id;
    }

    @Override
    public @NonNull String toString() {
        return "{chat:" + id + "}";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
    }

    public static final Creator<ChatId> CREATOR = new Creator<ChatId>() {
        public ChatId createFromParcel(Parcel in) {
            String id = in.readString();
            id = id == null ? "" : id;
            return fromNullable(id);
        }

        public ChatId[] newArray(int size) {
            return new ChatId[size];
        }
    };
}

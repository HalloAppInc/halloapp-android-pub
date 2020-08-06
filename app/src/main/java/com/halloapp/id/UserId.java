package com.halloapp.id;

import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.halloapp.util.Preconditions;

import java.util.regex.Pattern;

public class UserId extends ChatId implements Parcelable {

    // https://github.com/HalloAppInc/server/blob/master/doc/user_ids.md
    private static final Pattern pattern = Pattern.compile("\\d{19}");

    public static final UserId ME = new UserId("");

    public UserId(@NonNull String id) {
        super(id);
        Preconditions.checkArgument("".equals(id) || pattern.matcher(id).matches());
    }

    public boolean isMe() {
        return "".equals(rawId());
    }

    @Override
    public @NonNull String toString() {
        return "{user:" + (isMe() ? "me" : rawId()) + "}";
    }
}

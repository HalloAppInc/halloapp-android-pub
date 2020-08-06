package com.halloapp.id;

import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.halloapp.util.Preconditions;

import java.util.regex.Pattern;

public class GroupId extends ChatId implements Parcelable {

    // https://github.com/HalloAppInc/server/blob/master/doc/groups-design.md
    private static final Pattern pattern = Pattern.compile("g[\\w\\-]{21}");

    public GroupId(@NonNull String id) {
        super(id);
        Preconditions.checkArgument(pattern.matcher(id).matches());
    }

    @Override
    public @NonNull String toString() {
        return "{group:" + rawId() + "}";
    }
}

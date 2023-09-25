package com.halloapp.xmpp;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.proto.server.FriendListRequest;
import com.halloapp.proto.server.Iq;

public class FriendListRequestIq extends HalloIq {

    private final FriendListRequest.Action action;
    private final String cursor;

    public FriendListRequestIq(@Nullable String cursor, @NonNull FriendListRequest.Action action) {
        this.action = action;
        this.cursor = cursor;
    }

    @Override
    public Iq.Builder toProtoIq() {
        FriendListRequest.Builder builder = FriendListRequest.newBuilder();
        if (!TextUtils.isEmpty(cursor)) {
            builder.setCursor(cursor);
        }
        builder.setAction(action);
        return Iq.newBuilder()
                .setType(Iq.Type.GET)
                .setFriendListRequest(builder);
    }
}

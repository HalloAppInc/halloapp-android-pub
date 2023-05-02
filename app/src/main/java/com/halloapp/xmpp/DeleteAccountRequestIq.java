package com.halloapp.xmpp;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.halloapp.proto.server.DeleteAccount;
import com.halloapp.proto.server.Iq;

public class DeleteAccountRequestIq extends HalloIq {

    private final String phone;
    private final String reason;
    private final String username;

    DeleteAccountRequestIq(@Nullable String phone, @Nullable String username, @Nullable String reason) {
        this.phone = phone;
        this.username = username;
        this.reason = reason;
    }

    @Override
    public Iq.Builder toProtoIq() {
        DeleteAccount.Builder builder = DeleteAccount.newBuilder();
        if (!TextUtils.isEmpty(phone)) {
            builder.setPhone(phone);
        }
        if (!TextUtils.isEmpty(username)) {
            builder.setUsername(username);
        }
        if (!TextUtils.isEmpty(reason)) {
            builder.setFeedback(reason);
        }
        return Iq.newBuilder()
                .setType(Iq.Type.SET)
                .setDeleteAccount(builder.build());
    }
}


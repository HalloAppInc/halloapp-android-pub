package com.halloapp.xmpp;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.halloapp.proto.server.DeleteAccount;
import com.halloapp.proto.server.Iq;

public class DeleteAccountRequestIq extends HalloIq {

    private final String phone;
    private final String reason;

    DeleteAccountRequestIq(@NonNull String phone, @NonNull String reason) {
        this.phone = phone;
        this.reason = reason;
    }

    @Override
    public Iq.Builder toProtoIq() {
        DeleteAccount.Builder builder = DeleteAccount.newBuilder().setPhone(phone);
        if (!TextUtils.isEmpty(reason)) {
            builder.setFeedback(reason);
        }
        return Iq.newBuilder()
                .setType(Iq.Type.SET)
                .setDeleteAccount(builder.build());
    }
}


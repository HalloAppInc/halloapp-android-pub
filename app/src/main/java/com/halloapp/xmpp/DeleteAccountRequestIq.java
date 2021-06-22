package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.proto.server.DeleteAccount;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.PushRegister;
import com.halloapp.proto.server.PushToken;

public class DeleteAccountRequestIq extends HalloIq {

    private final String phone;

    DeleteAccountRequestIq(@NonNull String phone) {
        this.phone = phone;
    }

    @Override
    public Iq toProtoIq() {
        return Iq.newBuilder()
                .setId(getStanzaId())
                .setType(Iq.Type.SET)
                .setDeleteAccount(DeleteAccount.newBuilder().setPhone(phone).build())
                .build();
    }
}


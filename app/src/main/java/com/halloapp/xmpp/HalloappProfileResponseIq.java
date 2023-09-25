package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.proto.server.HalloappProfileResult;
import com.halloapp.proto.server.HalloappUserProfile;
import com.halloapp.proto.server.Iq;

public class HalloappProfileResponseIq extends HalloIq {

    public boolean success;
    public HalloappUserProfile profile;

    public HalloappProfileResponseIq(@NonNull HalloappProfileResult halloappProfileResult) {
        this.success = halloappProfileResult.getResult().equals(HalloappProfileResult.Result.OK);
        this.profile = halloappProfileResult.getProfile();
    }

    @Override
    public Iq.Builder toProtoIq() {
        return null;
    }

    public static HalloappProfileResponseIq fromProto(@NonNull HalloappProfileResult halloappProfileResult) {
        return new HalloappProfileResponseIq(halloappProfileResult);
    }
}


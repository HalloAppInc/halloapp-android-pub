package com.halloapp.crypto.group;

import com.halloapp.proto.server.SenderStateBundle;

import java.util.List;

public class GroupSetupInfo {
    public final byte[] audienceHash;
    public final List<SenderStateBundle> senderStateBundles;

    public GroupSetupInfo(byte[] audienceHash, List<SenderStateBundle> senderStateBundles) {
        this.audienceHash = audienceHash;
        this.senderStateBundles = senderStateBundles;
    }
}

package com.halloapp.crypto.home;

import com.halloapp.proto.server.SenderStateBundle;

import java.util.List;

public class HomePostSetupInfo {
    public final List<SenderStateBundle> senderStateBundles;

    public HomePostSetupInfo(List<SenderStateBundle> senderStateBundles) {
        this.senderStateBundles = senderStateBundles;
    }
}

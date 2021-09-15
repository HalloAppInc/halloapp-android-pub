package com.halloapp.crypto.signal;

import com.halloapp.crypto.keys.PublicEdECKey;

public class SignalSessionSetupInfo {
    public final PublicEdECKey identityKey;
    public final Integer oneTimePreKeyId;

    public SignalSessionSetupInfo(PublicEdECKey identityKey, Integer oneTimePreKeyId) {
        this.identityKey = identityKey;
        this.oneTimePreKeyId = oneTimePreKeyId;
    }
}

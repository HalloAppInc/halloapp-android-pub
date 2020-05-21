package com.halloapp.crypto;

import com.halloapp.crypto.keys.PublicEdECKey;

public class SessionSetupInfo {
    public final PublicEdECKey identityKey;
    public final Integer oneTimePreKeyId;

    public SessionSetupInfo(PublicEdECKey identityKey, Integer oneTimePreKeyId) {
        this.identityKey = identityKey;
        this.oneTimePreKeyId = oneTimePreKeyId;
    }
}

package com.halloapp.crypto;

import com.halloapp.crypto.keys.PublicECKey;

public class SessionSetupInfo {
    public final PublicECKey ephemeralKey;
    public final Integer ephemeralKeyId;
    public final PublicECKey identityKey;
    public final Integer oneTimePreKeyId;

    public SessionSetupInfo(PublicECKey ephemeralKey, Integer ephemeralKeyId, PublicECKey identityKey, Integer oneTimePreKeyId) {
        this.ephemeralKey = ephemeralKey;
        this.ephemeralKeyId = ephemeralKeyId;
        this.identityKey = identityKey;
        this.oneTimePreKeyId = oneTimePreKeyId;
    }
}

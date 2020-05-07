package com.halloapp.crypto;

import com.halloapp.crypto.keys.PublicXECKey;

public class SessionSetupInfo {
    public final PublicXECKey ephemeralKey;
    public final Integer ephemeralKeyId;
    public final byte[] identityKey;
    public final Integer oneTimePreKeyId;

    public SessionSetupInfo(PublicXECKey ephemeralKey, Integer ephemeralKeyId, byte[] identityKey, Integer oneTimePreKeyId) {
        this.ephemeralKey = ephemeralKey;
        this.ephemeralKeyId = ephemeralKeyId;
        this.identityKey = identityKey;
        this.oneTimePreKeyId = oneTimePreKeyId;
    }
}

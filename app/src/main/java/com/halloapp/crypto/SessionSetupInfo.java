package com.halloapp.crypto;

import com.halloapp.crypto.keys.PublicEdECKey;
import com.halloapp.crypto.keys.PublicXECKey;

public class SessionSetupInfo {
    public final PublicXECKey ephemeralKey;
    public final Integer ephemeralKeyId;
    public final PublicEdECKey identityKey;
    public final Integer oneTimePreKeyId;

    SessionSetupInfo(PublicXECKey ephemeralKey, Integer ephemeralKeyId, PublicEdECKey identityKey, Integer oneTimePreKeyId) {
        this.ephemeralKey = ephemeralKey;
        this.ephemeralKeyId = ephemeralKeyId;
        this.identityKey = identityKey;
        this.oneTimePreKeyId = oneTimePreKeyId;
    }
}

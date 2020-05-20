package com.halloapp.crypto;

import com.halloapp.crypto.keys.PublicEdECKey;
import com.halloapp.crypto.keys.PublicXECKey;

public class SessionSetupInfo {
    public final PublicEdECKey identityKey;
    public final Integer oneTimePreKeyId;

    SessionSetupInfo(PublicEdECKey identityKey, Integer oneTimePreKeyId) {
        this.identityKey = identityKey;
        this.oneTimePreKeyId = oneTimePreKeyId;
    }
}

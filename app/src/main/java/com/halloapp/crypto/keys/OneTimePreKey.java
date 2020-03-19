package com.halloapp.crypto.keys;

public class OneTimePreKey {
    public final PublicECKey publicECKey;
    public final int id;
    public OneTimePreKey(PublicECKey publicECKey, int id) {
        this.publicECKey = publicECKey;
        this.id = id;
    }
}

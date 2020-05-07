package com.halloapp.crypto.keys;

public class OneTimePreKey {
    public final PublicXECKey publicECKey;
    public final int id;
    public OneTimePreKey(PublicXECKey publicECKey, int id) {
        this.publicECKey = publicECKey;
        this.id = id;
    }
}

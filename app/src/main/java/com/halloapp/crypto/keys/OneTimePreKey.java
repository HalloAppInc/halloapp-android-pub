package com.halloapp.crypto.keys;

public class OneTimePreKey {
    public final PublicXECKey publicXECKey;
    public final int id;
    public OneTimePreKey(PublicXECKey publicXECKey, int id) {
        this.publicXECKey = publicXECKey;
        this.id = id;
    }
}

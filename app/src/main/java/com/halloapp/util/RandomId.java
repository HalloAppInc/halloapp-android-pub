package com.halloapp.util;

import android.util.Base64;

import java.util.Random;

public class RandomId {

    public static String create() {
        byte[] bytes = new byte[18];
        new Random().nextBytes(bytes);
        return Base64.encodeToString(bytes, Base64.URL_SAFE | Base64.NO_WRAP);
    }
}

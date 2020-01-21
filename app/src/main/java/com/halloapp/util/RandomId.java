package com.halloapp.util;

import java.util.UUID;

public class RandomId {

    public static String create() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}

package com.halloapp.xmpp;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ContactSyncRequest {

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            SET,
            ADD,
            DELETE
    })
    public @interface Type {}
    public static final String SET = "set";
    public static final String ADD = "add";
    public static final String DELETE = "delete";
}

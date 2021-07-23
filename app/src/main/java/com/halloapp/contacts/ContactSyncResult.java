package com.halloapp.contacts;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.xmpp.ContactInfo;

import java.util.List;

public class ContactSyncResult {
    public final boolean success;
    public final List<ContactInfo> contactList;
    public final Integer retryAfterSecs;

    public static ContactSyncResult success(@NonNull List<ContactInfo> contactList) {
        return new ContactSyncResult(true, contactList, null);
    }

    public static ContactSyncResult failure(int retryAfterSecs) {
        return new ContactSyncResult(false, null, retryAfterSecs);
    }

    private ContactSyncResult(boolean success, @Nullable List<ContactInfo> contactList, Integer retryAfterSecs) {
        this.success = success;
        this.contactList = contactList;
        this.retryAfterSecs = retryAfterSecs;
    }
}

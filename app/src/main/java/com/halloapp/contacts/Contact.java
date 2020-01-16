package com.halloapp.contacts;

import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;

import androidx.annotation.Nullable;

public class Contact {

    final long id;
    final long addressBookId;
    public @Nullable String name;
    public @Nullable String phone;
    public @Nullable UserId userId;
    public boolean member;

    public Contact(long id, long addressBookId, @Nullable String name, @Nullable String phone, @Nullable UserId userId, boolean member) {
        this.id = id;
        this.addressBookId = addressBookId;
        this.name = name;
        this.phone = phone;
        this.userId = userId;
        this.member = member;
    }

    public @Nullable String getRawUserId() {
        return userId == null ? null : userId.rawId();
    }

    public String getDisplayName() {
        return TextUtils.isEmpty(name) ? phone : name;
    }

    public String getInternationalPhone() {
        if (userId != null) {
            return PhoneNumberUtils.formatNumber("+" + userId.rawId(), null);
        } else {
            return phone;
        }
    }
}

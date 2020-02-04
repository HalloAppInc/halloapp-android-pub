package com.halloapp.contacts;

import android.telephony.PhoneNumberUtils;
import android.text.BidiFormatter;
import android.text.TextUtils;

import androidx.annotation.NonNull;
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

    public Contact(@NonNull UserId userId) {
        this(0, 0, null, null, userId, true);
    }

    public @Nullable String getRawUserId() {
        return userId == null ? null : userId.rawId();
    }

    public String getDisplayName() {
        return TextUtils.isEmpty(name) ? getInternationalPhone() : name;
    }

    public String getInternationalPhone() {
        final String internationalPhone;
        if (userId != null) {
            internationalPhone = userId.formatPhoneNumber();
        } else {
            internationalPhone = phone;
        }
        return BidiFormatter.getInstance().unicodeWrap(internationalPhone, false);
    }
}

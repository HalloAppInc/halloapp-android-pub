package com.halloapp.contacts;

import android.telephony.PhoneNumberUtils;
import android.text.BidiFormatter;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Contact {

    final long rowId;
    final long addressBookId;
    public @Nullable String addressBookName; // name from address book
    public @Nullable String addressBookPhone; // phone from address book
    public @Nullable String halloName; // from server
    public @Nullable String normalizedPhone; // phone from server contact sync
    public @Nullable UserId userId;
    public boolean friend;

    public Contact(long rowId,
                   long addressBookId, @Nullable String addressBookName, @Nullable String addressBookPhone,
                   @Nullable String normalizedPhone, @Nullable UserId userId, boolean friend) {
        this.rowId = rowId;
        this.addressBookId = addressBookId;
        this.addressBookName = addressBookName;
        this.addressBookPhone = addressBookPhone;
        this.normalizedPhone = normalizedPhone;
        this.userId = userId;
        this.friend = friend;
    }

    public Contact(@NonNull UserId userId, @Nullable String name, @Nullable String halloName) {
        this(0, 0, name, null, null, userId, true);
        this.halloName = halloName;
    }

    public @Nullable String getRawUserId() {
        return userId == null ? null : userId.rawId();
    }

    public String getDisplayName() {
        return TextUtils.isEmpty(addressBookName) ? (TextUtils.isEmpty(halloName) ? getDisplayPhone() : ("~" + halloName)) : addressBookName;
    }

    public @Nullable String getDisplayPhone() {
        final String internationalPhone;
        if (normalizedPhone != null) {
            internationalPhone = PhoneNumberUtils.formatNumber("+" + normalizedPhone, null);
        } else {
            internationalPhone = addressBookPhone;
        }
        return BidiFormatter.getInstance().unicodeWrap(internationalPhone, false);
    }
}

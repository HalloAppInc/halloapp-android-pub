package com.halloapp.contacts;

import android.telephony.PhoneNumberUtils;
import android.text.BidiFormatter;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.id.UserId;

import java.text.Collator;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class Contact {

    final long rowId;
    final long addressBookId;
    public @Nullable String addressBookName; // name from address book
    public @Nullable String addressBookPhone; // phone from address book
    public @Nullable String halloName; // from server
    public @Nullable String fallbackName; // Not stored
    public @Nullable String normalizedPhone; // phone from server contact sync
    public @Nullable String avatarId; // from server
    public @Nullable
    UserId userId;
    public boolean friend;

    public Contact(long rowId,
                   long addressBookId, @Nullable String addressBookName, @Nullable String addressBookPhone,
                   @Nullable String normalizedPhone, @Nullable String avatarId, @Nullable UserId userId, boolean friend) {
        this.rowId = rowId;
        this.addressBookId = addressBookId;
        this.addressBookName = addressBookName;
        this.addressBookPhone = addressBookPhone;
        this.normalizedPhone = normalizedPhone;
        this.avatarId = avatarId;
        this.userId = userId;
        this.friend = friend;
    }

    public Contact(@NonNull UserId userId, @Nullable String name, @Nullable String halloName) {
        this(0, 0, name, null, null, null, userId, false);
        this.halloName = halloName;
    }

    public @Nullable String getRawUserId() {
        return userId == null ? null : userId.rawId();
    }

    public String getDisplayName() {
        if (!TextUtils.isEmpty(addressBookName)) {
            return addressBookName;
        }
        if (!TextUtils.isEmpty(halloName)) {
            return "~" + halloName;
        }
        if (!TextUtils.isEmpty(fallbackName)) {
            return "~" + fallbackName;
        }
        if (normalizedPhone != null) {
            return getDisplayPhone();
        }
        return null;
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

    public static List<Contact> sort(@NonNull List<Contact> contacts) {
        Collator collator = Collator.getInstance(Locale.getDefault());
        Collections.sort(contacts, (o1, o2) -> {
            boolean alpha1 = Character.isAlphabetic(o1.getDisplayName().codePointAt(0));
            boolean alpha2 = Character.isAlphabetic(o2.getDisplayName().codePointAt(0));
            if (alpha1 == alpha2) {
                return collator.compare(o1.getDisplayName(), o2.getDisplayName());
            } else {
                return alpha1 ? -1 : 1;
            }
        });
        return contacts;
    }
}

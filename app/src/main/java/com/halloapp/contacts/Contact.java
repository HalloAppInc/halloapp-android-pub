package com.halloapp.contacts;

import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.PhoneNumberUtils;
import android.text.BidiFormatter;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.id.ChatId;
import com.halloapp.id.UserId;

import java.text.Collator;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class Contact implements Parcelable {

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
    @Deprecated
    public boolean friend;
    public boolean newConnection;
    public long connectionTime;
    public long numPotentialFriends;
    public boolean hideChat;
    public boolean invited;

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

    public String getShortName() {
        String displayName = getDisplayName();
        String[] parts = displayName.split(" ");
        return parts[0];
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

    public static final Parcelable.Creator<Contact> CREATOR = new Parcelable.Creator<Contact>() {
        public Contact createFromParcel(Parcel in) {
            long rowId = in.readLong();
            long addressBookId = in.readLong();
            String addressBookName = in.readString();
            String addressBookPhone = in.readString();
            String halloName = in.readString();
            String fallbackName = in.readString();
            String normalizedPhone = in.readString();
            String avatarId = in.readString();
            UserId userId = in.readParcelable(UserId.class.getClassLoader());
            boolean friend = in.readInt() == 1;
            return new Contact(rowId, addressBookId, addressBookName, addressBookPhone, normalizedPhone, avatarId, userId, friend);
        }

        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(rowId);
        dest.writeLong(addressBookId);
        dest.writeString(addressBookName);
        dest.writeString(addressBookPhone);
        dest.writeString(halloName);
        dest.writeString(fallbackName);
        dest.writeString(normalizedPhone);
        dest.writeString(avatarId);
        dest.writeParcelable(userId, flags);
        dest.writeInt(friend ? 1 : 0);
    }
}

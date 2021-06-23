package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.proto.server.Contact;

public class ContactInfo {

    public String userId;
    public String phone;
    public String normalizedPhone;
    public String avatarId;
    public String halloName;
    public long numPotentialFriends;

    ContactInfo(@NonNull Contact contact) {
        if (contact.getRaw() != null) {
            phone = contact.getRaw();
        }
        if (contact.getNormalized() != null) {
            normalizedPhone = contact.getNormalized();
        }
        if (contact.getUid() != 0) {
            userId = Long.toString(contact.getUid());
        }
        if (contact.getAvatarId() != null) {
            avatarId = contact.getAvatarId();
        }
        if (contact.getName() != null) {
            halloName = contact.getName();
        }
        numPotentialFriends = contact.getNumPotentialFriends();
    }
}

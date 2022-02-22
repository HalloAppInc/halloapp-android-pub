package com.halloapp.xmpp;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.halloapp.proto.server.Contact;
import com.halloapp.proto.server.ContactList;
import com.halloapp.proto.server.Iq;

import java.util.Collection;

public class ContactsSyncRequestIq extends HalloIq {

    private final boolean isFullSync;
    private final String syncId;
    private final int index;
    private final boolean lastBatch;
    private final Collection<String> addPhones;
    private final Collection<String> deletePhones;

    ContactsSyncRequestIq(@Nullable Collection<String> addPhones, @Nullable Collection<String> deletePhones,
                          boolean isFullSync, @Nullable String syncId, int index, boolean lastBatch) {
        this.addPhones = addPhones;
        this.deletePhones = deletePhones;
        this.isFullSync = isFullSync;
        this.index = index;
        this.syncId = syncId;
        this.lastBatch = lastBatch;
    }

    @Override
    public Iq toProtoIq() {
        ContactList.Builder builder = ContactList.newBuilder();
        builder.setType(isFullSync ? ContactList.Type.FULL : ContactList.Type.DELTA);
        if (syncId != null) {
            builder.setSyncId(syncId);
            builder.setBatchIndex(index);
            builder.setIsLast(lastBatch);
        }
        if (deletePhones != null) {
            for (String phone : deletePhones) {
                if (TextUtils.isEmpty(phone)) {
                    continue;
                }
                Contact contact = Contact.newBuilder()
                        .setAction(Contact.Action.DELETE)
                        .setNormalized(phone)
                        .build();
                builder.addContacts(contact);
            }
        }
        if (addPhones != null) {
            for (String phone : addPhones) {
                if (TextUtils.isEmpty(phone)) {
                    continue;
                }
                Contact contact = Contact.newBuilder()
                        .setAction(Contact.Action.ADD)
                        .setRaw(phone)
                        .build();
                builder.addContacts(contact);
            }
        }
        return Iq.newBuilder()
                .setType(Iq.Type.SET)
                .setId(getStanzaId())
                .setContactList(builder)
                .build();
    }
}

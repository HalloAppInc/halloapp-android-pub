package com.halloapp.xmpp.privacy;

import androidx.annotation.Nullable;

import com.google.protobuf.ByteString;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.id.UserId;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.UidElement;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.HalloIq;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class SetPrivacyListIq extends HalloIq {

    private final ContactsDb contactsDb = ContactsDb.getInstance();

    private final @PrivacyList.Type String type;

    private final List<UserId> usersAdd = new ArrayList<>();
    private final List<UserId> usersDelete = new ArrayList<>();

    protected SetPrivacyListIq(@PrivacyList.Type String listType, @Nullable Collection<UserId> usersToAdd, @Nullable Collection<UserId> usersToDelete) {
        this.type = listType;
        if (usersToAdd != null) {
            this.usersAdd.addAll(usersToAdd);
        }
        if (usersToDelete != null) {
            this.usersDelete.addAll(usersToDelete);
        }
    }

    @Override
    public Iq toProtoIq() {
        com.halloapp.proto.server.PrivacyList.Builder builder = com.halloapp.proto.server.PrivacyList.newBuilder();
        builder.setType(com.halloapp.proto.server.PrivacyList.Type.valueOf(type.toUpperCase(Locale.US)));

        List<UserId> userList;
        //noinspection IfCanBeSwitch
        if (PrivacyList.Type.EXCEPT.equals(type)) {
            userList = contactsDb.getFeedExclusionListForServer();
        } else if (PrivacyList.Type.ONLY.equals(type)) {
            userList = contactsDb.getFeedShareListForServer();
        } else if (PrivacyList.Type.BLOCK.equals(type)) {
            userList = contactsDb.getBlockList();
        } else if (PrivacyList.Type.ALL.equals(type)) {
            userList = new ArrayList<>();
        } else {
            throw new IllegalStateException("Unexpected privacy list type " + type);
        }

        for (UserId userId : usersAdd) {
            updateUid(builder, userId, true);
            if (!userList.contains(userId)) {
                userList.add(userId);
            }
        }
        for (UserId userId : usersDelete) {
            updateUid(builder, userId, false);
            userList.remove(userId);
        }

        if (!PrivacyList.Type.ALL.equals(type)) {
            builder.setHash(ByteString.copyFrom(computeUserListHash(userList)));
        }

        return Iq.newBuilder()
                .setType(Iq.Type.SET)
                .setId(getStanzaId())
                .setPrivacyList(builder)
                .build();
    }

    private void updateUid(com.halloapp.proto.server.PrivacyList.Builder builder, UserId userId, boolean add) {
        UidElement uidElement = UidElement.newBuilder()
                .setAction(add ? UidElement.Action.ADD : UidElement.Action.DELETE)
                .setUid(Long.parseLong(userId.rawId()))
                .build();
        builder.addUidElements(uidElement);
    }

    private byte[] computeUserListHash(List<UserId> list) {
        List<String> rawIds = new ArrayList<>();
        for (UserId userId : list) {
            rawIds.add(userId.rawId());
        }
        Collections.sort(rawIds);

        StringBuilder sb = new StringBuilder();
        for (String id : rawIds) {
            sb.append(",").append(id);
        }

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            return messageDigest.digest(sb.toString().getBytes());
        } catch (NoSuchAlgorithmException e) {
            Log.e("Could not find sha256", e);
            throw new IllegalStateException("SHA-256 missing");
        }
    }
}

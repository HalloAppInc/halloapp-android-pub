package com.halloapp.contacts;

import com.halloapp.Connection;

import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Domainpart;
import org.jxmpp.jid.parts.Localpart;

public class Contact {

    final long id;
    final long addressBookId;
    String name;
    String phone;
    Jid jid;
    boolean member;

    public Contact(long id, long addressBookId, String name, String phone, String userId, boolean member) {
        this(id, addressBookId, name, phone, userId == null ? null : JidCreate.entityBareFrom(Localpart.fromOrThrowUnchecked(userId), Domainpart.fromOrNull(Connection.XMPP_DOMAIN)), member);
    }

    public Contact(long id, long addressBookId, String name, String phone, Jid jid, boolean member) {
        this.id = id;
        this.addressBookId = addressBookId;
        this.name = name;
        this.phone = phone;
        this.jid = jid;
        this.member = member;
    }
}

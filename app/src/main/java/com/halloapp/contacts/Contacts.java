package com.halloapp.contacts;

import com.halloapp.Connection;

import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Domainpart;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class Contacts {

    private static Contacts instance;

    final List<Contact> contacts = Arrays.asList(
            new Contact(1, 1, "Duygu", "+1 (347) 752-1636", "13477521636", true),
            new Contact(2, 2, "Murali", "1 (470) 338-1473", "14703381473", true),
            new Contact(3, 3, "Michael", "14154121848", "14154121848", true),
            new Contact(4, 4, "Tony", "14088922686", "14088922686", true),
            new Contact(5, 5, "Katya", "(650) 5742675", "16505742675", true),
            new Contact(6, 6, "Dmitri", "(650) 275-2675", "16502752675", true),
            new Contact(7, 7, "Dima", "(650) 281-3677", "16502813677", true),
            new Contact(8, 8, "Unknown", "(650) 555-3677", "16505553677", false)
    );

    public static Contacts getInstance() {
        if (instance == null) {
            synchronized(Contacts.class) {
                if (instance == null) {
                    instance = new Contacts();
                }
            }
        }
        return instance;
    }

    private Contacts() {
    }

    public String getName(String jidString) {
        Jid jid;
        try {
            jid = JidCreate.bareFrom(jidString);
        } catch (XmppStringprepException e) {
            return null;
        }
        String user = jid.getLocalpartOrThrow().toString();
        for (Contact contact : contacts) {
            if (user.equals(contact.user)) {
                return contact.name;
            }
        }
        return null;
    }

    public Collection<Jid> getMemberJids() {
        final Collection<Jid> jids = new ArrayList<>();
        for (Contact contact : contacts) {
            if (contact.member) {
                jids.add(JidCreate.entityBareFrom(Localpart.fromOrThrowUnchecked(contact.user), Domainpart.fromOrNull(Connection.XMPP_DOMAIN)));
            }
        }
        return jids;
    }

    public Collection<String> getPhones() {
        final Collection<String> phones = new HashSet<>();
        for (Contact contact : contacts) {
            phones.add(contact.phone);
        }
        return phones;
    }
}

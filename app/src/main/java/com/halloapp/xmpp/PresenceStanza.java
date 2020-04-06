package com.halloapp.xmpp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.util.XmlStringBuilder;
import org.jxmpp.jid.Jid;

public class PresenceStanza extends Stanza {

    private static final String ELEMENT = "presence";

    public final String type;
    public final Long lastSeen;

    PresenceStanza(@NonNull Jid to, @NonNull String id, @NonNull String type, @Nullable Long lastSeen) {
        setTo(to);
        setStanzaId(id);
        this.type = type;
        this.lastSeen = lastSeen;
    }

    @Override
    public XmlStringBuilder toXML(String enclosingNamespace) {
        final XmlStringBuilder buf = new XmlStringBuilder(enclosingNamespace);
        buf.halfOpenElement(ELEMENT);
        addCommonAttributes(buf, enclosingNamespace);
        buf.attribute("type", type);
        buf.closeEmptyElement();
        return buf;
    }

    @Override
    public @NonNull String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Ack Stanza [");
        logCommonAttributes(sb);
        sb.append(']');
        return sb.toString();
    }
}

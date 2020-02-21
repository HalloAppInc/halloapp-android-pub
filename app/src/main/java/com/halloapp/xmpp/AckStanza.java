package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.util.XmlStringBuilder;
import org.jxmpp.jid.Jid;

public class AckStanza extends Stanza {

    private static final String ELEMENT = "ack";

    AckStanza(@NonNull Jid to, @NonNull String id) {
        setTo(to);
        setStanzaId(id);
    }

    @Override
    public XmlStringBuilder toXML(String enclosingNamespace) {
        final XmlStringBuilder buf = new XmlStringBuilder(enclosingNamespace);
        buf.halfOpenElement(ELEMENT);
        addCommonAttributes(buf, enclosingNamespace);
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

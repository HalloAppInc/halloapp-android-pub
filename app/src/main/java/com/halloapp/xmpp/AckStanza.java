package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.proto.server.Ack;

import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.util.XmlStringBuilder;

public class AckStanza extends Stanza {

    private static final String ELEMENT = "ack";

    AckStanza(@NonNull String id) {
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

    public Ack toProto() {
        return Ack.newBuilder().setId(getStanzaId()).build();
    }
}

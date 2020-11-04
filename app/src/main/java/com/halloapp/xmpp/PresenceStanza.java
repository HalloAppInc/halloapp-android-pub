package com.halloapp.xmpp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.id.UserId;
import com.halloapp.proto.server.Presence;

import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.util.XmlStringBuilder;
import org.jxmpp.jid.parts.Localpart;

import java.util.Locale;

public class PresenceStanza extends Stanza {

    private static final String ELEMENT = "presence";

    public final UserId userId;
    public final String type;
    public final Long lastSeen;

    PresenceStanza(@Nullable UserId userId, @NonNull String type) {
        this.userId = userId;
        this.type = type;
        this.lastSeen = null;
    }

    PresenceStanza(@Nullable UserId userId, @NonNull String id, @NonNull String type, @Nullable Long lastSeen) {
        setStanzaId(id);
        this.userId = userId;
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
        sb.append("Presence Stanza [");
        logCommonAttributes(sb);
        sb.append("type=").append(type);
        sb.append(']');
        return sb.toString();
    }

    public Presence toProto() {
        Presence.Builder builder = Presence.newBuilder();
        builder.setId(getStanzaId());
        builder.setType(Presence.Type.valueOf(type.toUpperCase(Locale.US)));
        if (userId != null) {
            builder.setUid(Long.parseLong(userId.rawId()));
        }
        return builder.build();
    }
}

package com.halloapp.xmpp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.Preferences;
import com.halloapp.id.UserId;
import com.halloapp.proto.server.Presence;

import java.util.Locale;

public class PresenceStanza extends HalloStanza {

    private static final Preferences preferences = Preferences.getInstance();

    public final UserId userId;
    public final String type;
    public final Long lastSeen;

    PresenceStanza(@Nullable UserId userId, @NonNull String type) {
        super(preferences.getAndIncrementPresenceId());
        this.userId = userId;
        this.type = type;
        this.lastSeen = null;
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
            builder.setToUid(Long.parseLong(userId.rawId()));
        }
        return builder.build();
    }
}

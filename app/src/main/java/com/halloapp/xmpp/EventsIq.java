package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.proto.log_events.EventData;
import com.halloapp.proto.server.ClientLog;
import com.halloapp.proto.server.Iq;

import java.util.Collection;

public class EventsIq extends HalloIq {

    private final Collection<EventData> events;

    EventsIq(@NonNull Collection<EventData> events) {
        this.events = events;
    }

    @Override
    public Iq.Builder toProtoIq() {
        ClientLog.Builder builder = ClientLog.newBuilder();
        for (EventData event : events) {
            builder.addEvents(event);
        }
        return Iq.newBuilder()
                .setId(getStanzaId())
                .setType(Iq.Type.SET)
                .setClientLog(builder);
    }
}

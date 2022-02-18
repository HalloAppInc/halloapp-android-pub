package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.proto.server.ClientLog;
import com.halloapp.proto.server.Count;
import com.halloapp.proto.server.Dim;
import com.halloapp.proto.server.Iq;
import com.halloapp.util.Preconditions;
import com.halloapp.util.stats.Counter;
import com.halloapp.util.stats.Dimensions;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class StatsIq extends HalloIq {

    private final List<Counter> counters;

    StatsIq(@NonNull List<Counter> counters) {
        this.counters = counters;
    }

    @Override
    public Iq.Builder toProtoIq() {
        ClientLog.Builder builder = ClientLog.newBuilder();

        for (Counter counter : counters) {
            Map<Dimensions, Long> counts = counter.fetchAndReset();
            for (Dimensions dimensions : counts.keySet()) {
                Count.Builder cb = Count.newBuilder();
                cb.setMetric(counter.getMetric());
                cb.setNamespace(counter.getNamespace());
                cb.setCount(Preconditions.checkNotNull(counts.get(dimensions)));

                Set<String> keys = dimensions.getKeys();
                if (!keys.isEmpty()) {
                    for (String key : keys) {
                        cb.addDims(Dim.newBuilder().setName(key).setValue(dimensions.get(key)));
                    }
                }

                builder.addCounts(cb);
            }
        }

        return Iq.newBuilder()
                .setId(getStanzaId())
                .setType(Iq.Type.SET)
                .setClientLog(builder);
    }
}


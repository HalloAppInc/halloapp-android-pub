package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.proto.server.ClientLog;
import com.halloapp.proto.server.Count;
import com.halloapp.proto.server.Dim;
import com.halloapp.proto.server.Iq;
import com.halloapp.util.Preconditions;
import com.halloapp.util.stats.Dimensions;
import com.halloapp.util.stats.Stats;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class StatsIq extends HalloIq {

    public static final String ELEMENT = "client_log";
    public static final String NAMESPACE = "halloapp:client_log";

    private static final String ELEMENT_COUNT = "count";
    private static final String ELEMENT_DIM = "dim";

    private static final String ATTRIBUTE_NAMESPACE = "namespace";
    private static final String ATTRIBUTE_METRIC = "metric";
    private static final String ATTRIBUTE_COUNT = "count";
    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_VALUE = "value";

    private final List<Stats.Counter> counters;

    StatsIq(@NonNull List<Stats.Counter> counters) {
        super(ELEMENT, NAMESPACE);
        setType(Type.set);
        this.counters = counters;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {

        xml.rightAngleBracket();

        for (Stats.Counter counter : counters) {
            Map<Dimensions, Long> counts = counter.fetchAndReset();
            for (Dimensions dimensions : counts.keySet()) {
                xml.halfOpenElement(ELEMENT_COUNT);
                xml.attribute(ATTRIBUTE_NAMESPACE, counter.getNamespace());
                xml.attribute(ATTRIBUTE_METRIC, counter.getMetric());
                xml.attribute(ATTRIBUTE_COUNT, counts.get(dimensions) + "");

                Set<String> keys = dimensions.getKeys();
                if (keys.isEmpty()) {
                    xml.closeEmptyElement();
                } else {
                    xml.rightAngleBracket();

                    for (String key : keys) {
                        xml.halfOpenElement(ELEMENT_DIM);
                        xml.attribute(ATTRIBUTE_NAME, key);
                        xml.attribute(ATTRIBUTE_VALUE, dimensions.get(key));
                        xml.closeEmptyElement();
                    }
                }

                xml.closeElement(ELEMENT_COUNT);
            }
        }

        return xml;
    }

    @Override
    public Iq toProtoIq() {
        ClientLog.Builder builder = ClientLog.newBuilder();

        for (Stats.Counter counter : counters) {
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

                builder.addCounts(cb.build());
            }
        }

        return Iq.newBuilder()
                .setId(getStanzaId())
                .setType(Iq.Type.SET)
                .setClientLog(builder.build())
                .build();
    }
}


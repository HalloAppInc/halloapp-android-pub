package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.util.stats.Dimensions;
import com.halloapp.util.stats.Stats;

import org.jivesoftware.smack.packet.IQ;
import org.jxmpp.jid.Jid;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class StatsIq extends IQ {

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

    StatsIq(@NonNull Jid to, @NonNull List<Stats.Counter> counters) {
        super(ELEMENT, NAMESPACE);
        setType(Type.set);
        setTo(to);
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
}


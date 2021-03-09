package com.halloapp.util.stats;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Counter {
    private final String namespace;
    private final String metric;

    private Map<Dimensions, Long> map = new ConcurrentHashMap<>();

    public Counter(String namespace, String metric) {
        this.namespace = namespace;
        this.metric = metric;
    }

    protected void reportEvent(Dimensions key) {
        Long currentValue = map.get(key);
        if (currentValue == null) {
            currentValue = 0L;
        }
        map.put(key, currentValue + 1);
    }

    public Map<Dimensions, Long> fetchAndReset() {
        Map<Dimensions, Long> ret = map;
        map = new ConcurrentHashMap<>();
        return ret;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getMetric() {
        return metric;
    }
}

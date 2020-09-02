package com.halloapp.util.stats;

import android.text.format.DateUtils;

import com.halloapp.xmpp.Connection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class Stats {

    private static final Long BUFFER_DELAY_MS = DateUtils.MINUTE_IN_MILLIS;

    private static Stats instance;

    public static Stats getInstance() {
        if (instance == null) {
            synchronized (Stats.class) {
                if (instance == null) {
                    instance = new Stats(Connection.getInstance());
                }
            }
        }
        return instance;
    }

    private final List<Counter> counters = new ArrayList<>();

    private final SuccessCounter encryption = new SuccessCounter("crypto", "encryption");
    private final SuccessCounter decryption = new SuccessCounter("crypto", "decryption");

    private boolean scheduled = false;
    private final Timer timer = new Timer();

    private final Connection connection;

    private Stats(Connection connection) {
        this.connection = connection;
    }

    private void ensureTimer() {
        if (!scheduled) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    scheduled = false;
                    connection.sendStats(counters);
                }
            }, BUFFER_DELAY_MS);
            scheduled = true;
        }
    }

    public void reportEncryptSuccess() {
        encryption.reportSuccess();
    }

    public void reportEncryptError(String error) {
        encryption.reportError(error);
    }

    public void reportDecryptSuccess() {
        decryption.reportSuccess();
    }

    public void reportDecryptError(String error) {
        decryption.reportError(error);
    }

    public class Counter {
        private final String namespace;
        private final String metric;

        private Map<Dimensions, Long> map = new ConcurrentHashMap<>();

        public Counter(String namespace, String metric) {
            this.namespace = namespace;
            this.metric = metric;

            counters.add(this);
        }

        protected void reportEvent(Dimensions key) {
            Long currentValue = map.get(key);
            if (currentValue == null) {
                currentValue = 0L;
            }
            map.put(key, currentValue + 1);
            ensureTimer();
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

    public class SuccessCounter extends Counter {

        public SuccessCounter(String namespace, String metric) {
            super(namespace, metric);
        }

        public void reportSuccess() {
            Dimensions.Builder builder = new Dimensions.Builder()
                    .put("success", "true");
            reportEvent(builder.build());
        }

        public void reportError(String error) {
            Dimensions.Builder builder = new Dimensions.Builder()
                    .put("success", "false")
                    .put("error", error);
            reportEvent(builder.build());
        }
    }
}

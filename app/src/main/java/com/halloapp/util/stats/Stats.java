package com.halloapp.util.stats;

import android.text.format.DateUtils;

import com.halloapp.BuildConfig;
import com.halloapp.xmpp.Connection;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Stats {

    private static final long BUFFER_DELAY_MS = DateUtils.MINUTE_IN_MILLIS;

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
    private final DecryptCounter decryption = new DecryptCounter();

    private boolean scheduled = false;
    private final Timer timer = new Timer();

    private final Connection connection;

    private Stats(Connection connection) {
        this.connection = connection;
    }

    private void ensureTimer() {
        if (!scheduled && !BuildConfig.DEBUG) {
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

    public void reportDecryptSuccess(String senderPlatform, String senderVersion) {
        decryption.reportSuccess(senderPlatform, senderVersion);
    }

    public void reportDecryptError(String error, String senderPlatform, String senderVersion) {
        decryption.reportError(error, senderPlatform, senderVersion);
    }

    private class TimerUploadCounter extends Counter {

        public TimerUploadCounter(String namespace, String metric) {
            super(namespace, metric);
            counters.add(this);
        }

        @Override
        protected void reportEvent(Dimensions key) {
            super.reportEvent(key);
            ensureTimer();
        }
    }

    private class SuccessCounter extends TimerUploadCounter {
        private static final String DIM_RESULT = "result";

        public SuccessCounter(String namespace, String metric) {
            super(namespace, metric);
        }

        public void reportSuccess() {
            Dimensions.Builder builder = new Dimensions.Builder()
                    .put(DIM_RESULT, "success");
            reportEvent(builder.build());
        }

        public void reportError(String error) {
            Dimensions.Builder builder = new Dimensions.Builder()
                    .put(DIM_RESULT, error);
            reportEvent(builder.build());
        }
    }

    private class DecryptCounter extends TimerUploadCounter {
        private static final String DIM_RESULT = "result";
        private static final String DIM_SENDER_PLATFORM = "senderPlatform";
        private static final String DIM_SENDER_VERSION = "senderVersion";

        public DecryptCounter() {
            super("crypto", "decryption");
        }

        public void reportSuccess(String senderPlatform, String senderVersion) {
            Dimensions.Builder builder = new Dimensions.Builder()
                    .put(DIM_RESULT, "success")
                    .put(DIM_SENDER_PLATFORM, senderPlatform)
                    .put(DIM_SENDER_VERSION, senderVersion);
            reportEvent(builder.build());
        }

        public void reportError(String error, String senderPlatform, String senderVersion) {
            Dimensions.Builder builder = new Dimensions.Builder()
                    .put(DIM_RESULT, error)
                    .put(DIM_SENDER_PLATFORM, senderPlatform)
                    .put(DIM_SENDER_VERSION, senderVersion);
            reportEvent(builder.build());
        }
    }
}

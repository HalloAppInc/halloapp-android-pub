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
    private final GroupSuccessCounter groupEncryption = new GroupSuccessCounter();
    private final GroupDecryptCounter groupDecryption = new GroupDecryptCounter();
    private final HomeSuccessCounter homeEncryption = new HomeSuccessCounter();
    private final HomeDecryptCounter homeDecryption = new HomeDecryptCounter();
    private final SignalResetCounter signalResetCounter = new SignalResetCounter();
    private final CallSettingCounter callSettingCounter = new CallSettingCounter();

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

    public void reportGroupEncryptSuccess(boolean isComment) {
        groupEncryption.reportSuccess(isComment);
    }

    public void reportGroupEncryptError(String error, boolean isComment) {
        groupEncryption.reportError(error, isComment);
    }

    public void reportGroupDecryptSuccess(boolean isComment, String senderPlatform, String senderVersion) {
        groupDecryption.reportSuccess(isComment, senderPlatform, senderVersion);
    }

    public void reportGroupDecryptError(String error, boolean isComment, String senderPlatform, String senderVersion) {
        groupDecryption.reportError(error, isComment, senderPlatform, senderVersion);
    }

    public void reportHomeEncryptSuccess(boolean isComment) {
        homeEncryption.reportSuccess(isComment);
    }

    public void reportHomeEncryptError(String error, boolean isComment) {
        homeEncryption.reportError(error, isComment);
    }

    public void reportHomeDecryptSuccess(boolean isComment, String senderPlatform, String senderVersion) {
        homeDecryption.reportSuccess(isComment, senderPlatform, senderVersion);
    }

    public void reportHomeDecryptError(String error, boolean isComment, String senderPlatform, String senderVersion) {
        homeDecryption.reportError(error, isComment, senderPlatform, senderVersion);
    }

    public void reportSignalSessionEstablished(boolean isReset) {
        signalResetCounter.report(isReset);
    }

    public void reportCallSettings(boolean isAudio, boolean isNoiseSuppressionSet) {
        callSettingCounter.report(isAudio, isNoiseSuppressionSet);
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

    private class GroupSuccessCounter extends TimerUploadCounter {
        private static final String DIM_RESULT = "result";
        private static final String DIM_FAILURE_REASON = "failure_reason";
        private static final String DIM_ITEM_TYPE = "item_type";

        public GroupSuccessCounter() {
            super("crypto", "group_encryption");
        }

        public void reportSuccess(boolean isComment) {
            Dimensions.Builder builder = new Dimensions.Builder()
                    .put(DIM_RESULT, "ok")
                    .put(DIM_ITEM_TYPE, isComment ? "comment" : "post");
            reportEvent(builder.build());
        }

        public void reportError(String error, boolean isComment) {
            Dimensions.Builder builder = new Dimensions.Builder()
                    .put(DIM_RESULT, "fail")
                    .put(DIM_FAILURE_REASON, error)
                    .put(DIM_ITEM_TYPE, isComment ? "comment" : "post");
            reportEvent(builder.build());
        }
    }

    private class GroupDecryptCounter extends TimerUploadCounter {
        private static final String DIM_RESULT = "result";
        private static final String DIM_FAILURE_REASON = "failure_reason";
        private static final String DIM_ITEM_TYPE = "item_type";
        private static final String DIM_SENDER_PLATFORM = "senderPlatform";
        private static final String DIM_SENDER_VERSION = "senderVersion";

        public GroupDecryptCounter() {
            super("crypto", "group_decryption");
        }

        public void reportSuccess(boolean isComment, String senderPlatform, String senderVersion) {
            Dimensions.Builder builder = new Dimensions.Builder()
                    .put(DIM_RESULT, "ok")
                    .put(DIM_ITEM_TYPE, isComment ? "comment" : "post")
                    .put(DIM_SENDER_PLATFORM, senderPlatform)
                    .put(DIM_SENDER_VERSION, senderVersion);
            reportEvent(builder.build());
        }

        public void reportError(String error, boolean isComment, String senderPlatform, String senderVersion) {
            Dimensions.Builder builder = new Dimensions.Builder()
                    .put(DIM_RESULT, "fail")
                    .put(DIM_FAILURE_REASON, error)
                    .put(DIM_ITEM_TYPE, isComment ? "comment" : "post")
                    .put(DIM_SENDER_PLATFORM, senderPlatform)
                    .put(DIM_SENDER_VERSION, senderVersion);
            reportEvent(builder.build());
        }
    }

    private class HomeSuccessCounter extends TimerUploadCounter {
        private static final String DIM_RESULT = "result";
        private static final String DIM_FAILURE_REASON = "failure_reason";
        private static final String DIM_ITEM_TYPE = "item_type";

        public HomeSuccessCounter() {
            super("crypto", "home_encryption");
        }

        public void reportSuccess(boolean isComment) {
            Dimensions.Builder builder = new Dimensions.Builder()
                    .put(DIM_RESULT, "ok")
                    .put(DIM_ITEM_TYPE, isComment ? "comment" : "post");
            reportEvent(builder.build());
        }

        public void reportError(String error, boolean isComment) {
            Dimensions.Builder builder = new Dimensions.Builder()
                    .put(DIM_RESULT, "fail")
                    .put(DIM_FAILURE_REASON, error)
                    .put(DIM_ITEM_TYPE, isComment ? "comment" : "post");
            reportEvent(builder.build());
        }
    }

    private class HomeDecryptCounter extends TimerUploadCounter {
        private static final String DIM_RESULT = "result";
        private static final String DIM_FAILURE_REASON = "failure_reason";
        private static final String DIM_ITEM_TYPE = "item_type";
        private static final String DIM_SENDER_PLATFORM = "senderPlatform";
        private static final String DIM_SENDER_VERSION = "senderVersion";

        public HomeDecryptCounter() {
            super("crypto", "home_decryption");
        }

        public void reportSuccess(boolean isComment, String senderPlatform, String senderVersion) {
            Dimensions.Builder builder = new Dimensions.Builder()
                    .put(DIM_RESULT, "ok")
                    .put(DIM_ITEM_TYPE, isComment ? "comment" : "post")
                    .put(DIM_SENDER_PLATFORM, senderPlatform)
                    .put(DIM_SENDER_VERSION, senderVersion);
            reportEvent(builder.build());
        }

        public void reportError(String error, boolean isComment, String senderPlatform, String senderVersion) {
            Dimensions.Builder builder = new Dimensions.Builder()
                    .put(DIM_RESULT, "fail")
                    .put(DIM_FAILURE_REASON, error)
                    .put(DIM_ITEM_TYPE, isComment ? "comment" : "post")
                    .put(DIM_SENDER_PLATFORM, senderPlatform)
                    .put(DIM_SENDER_VERSION, senderVersion);
            reportEvent(builder.build());
        }
    }

    private class SignalResetCounter extends TimerUploadCounter {
        private static final String DIM_RESET = "reset";

        public SignalResetCounter() {
            super("crypto", "e2e_session");
        }

        public void report(boolean isReset) {
            Dimensions.Builder builder = new Dimensions.Builder()
                    .put(DIM_RESET, isReset ? "true" : "false");
            reportEvent(builder.build());
        }
    }

    private class CallSettingCounter extends TimerUploadCounter {
        private static final String DIM_CALL_TYPE = "call_type";
        private static final String DIM_NOISE_SUPPRESSION = "noise_suppression";

        public CallSettingCounter() {
            super("call", "setting");
        }

        public void report(boolean isAudio, boolean isNoiseSuppressionSet) {
            Dimensions.Builder builder = new Dimensions.Builder()
                    .put(DIM_CALL_TYPE, isAudio ? "audio" : "video")
                    .put(DIM_NOISE_SUPPRESSION, isNoiseSuppressionSet ? "true" : "false");
            reportEvent(builder.build());
        }
    }
}

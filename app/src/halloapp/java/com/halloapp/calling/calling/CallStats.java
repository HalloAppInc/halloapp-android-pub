package com.halloapp.calling.calling;

import android.content.Context;
import android.net.ConnectivityManager;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.halloapp.AppContext;
import com.halloapp.BuildConfig;
import com.halloapp.id.UserId;
import com.halloapp.proto.log_events.Call;
import com.halloapp.proto.server.CallType;
import com.halloapp.proto.server.EndCall;
import com.halloapp.util.logs.Log;
import com.halloapp.util.stats.Events;

import org.webrtc.RTCStats;
import org.webrtc.RTCStatsReport;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class CallStats {

    public static final long REPORT_INTERVAL = 5_000;
    public static final List<String> STATS_KEYS = Arrays.asList(
            "packetsReceived", "bytesReceived", "packetsLost", "packetsDiscarded",
            "packetsSent", "bytesSent", "retransmittedBytesSent", "retransmittedPacketsSent",
            "insertedSamplesForDeceleration", "jitter", "jitterBufferDelay", "jitterBufferEmittedCount",
            "framesReceived", "framesPerSecond", "framesDecoded", "keyFramesDecoded",
            "framesDropped", "partialFramesLost", "fullFramesLost");

    public static final List<String> STATS_KEYS_NO_DIFF = Arrays.asList("framesPerSecond");

    @Nullable
    private RTCStatsReport lastReport;
    @Nullable
    private Timer timer;
    @Nullable
    private TimerTask statsTask;

    public CallStats() {
    }

    public void startStatsCollection() {
        if (this.timer == null) {
            this.timer = new Timer();
        }
        if (statsTask != null) {
            statsTask.cancel();
        }
        statsTask = new TimerTask() {
            @Override
            public void run() {
                boolean success = CallManager.getInstance().getPeerConnectionStats(report -> processStats(report));
                if (!success) {
                    Log.w("CallStats statsTask was not canceled properly");
                    stopStatsCollection();
                }
            }
        };
        Log.i("CallStats: started");
        timer.scheduleAtFixedRate(statsTask, REPORT_INTERVAL, REPORT_INTERVAL);
    }

    public void stopStatsCollection() {
        if (statsTask != null) {
            statsTask.cancel();
            statsTask = null;
        }
        if (timer != null) {
            timer.purge();
            timer.cancel();
            timer = null;
        }
        Log.i("CallStats: stopped");
    }

    public void clear() {
        lastReport = null;
    }

    /**
     * Processes and logs stats about WebRTC call by subtracting the stats from the lastReport.
     * @param report Latest report
     */
    public void processStats(RTCStatsReport report) {
        if (report == null) {
            Log.e("CallStats: unexpected null report");
            return;
        }
        for (Map.Entry<String, RTCStats> e : report.getStatsMap().entrySet()) {
            RTCStats s = e.getValue();
            String type = s.getType();

            if ("inbound-rtp".equals(type) || "outbound-rtp".equals(type)) {
                String statId = s.getId();
                String mediaType = statId.toLowerCase(Locale.US).contains("video") ? "V" : "A";
                String direction = type.contains("inbound")? "in" : "out";
                if (s == null) {
                    Log.e("CallStats: s is null");
                    continue;
                }
                Map<String, Object> values = s.getMembers();
                Map<String, Object> lastValues = null;
                if (lastReport != null) {
                    RTCStats lastStats = lastReport.getStatsMap().get(e.getKey());
                    if (lastStats == null) {
                        continue;
                    }
                    lastValues = lastStats.getMembers();
                }

                StringBuilder log = new StringBuilder();
                for (String k : STATS_KEYS) {
                    if (!values.containsKey(k)) {
                        continue;
                    }
                    if (lastValues != null && lastValues.containsKey(k) && !STATS_KEYS_NO_DIFF.contains(k)) {
                        log.append(k).append(":").append(diff(values.get(k), lastValues.get(k))).append(" ");
                    } else {
                        log.append(k).append(":").append(values.get(k)).append(" ");
                    }
                }
                Log.i("rtp-stats: " + mediaType + "-" + direction + " " + log);
            }
        }
        lastReport = report;
    }

    public static Call.NetworkType getNetworkType(final Context context) {
        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (wifi != null && wifi.isAvailable()) {
            return Call.NetworkType.WIFI;
        } else if (mobile != null && mobile.isAvailable()) {
            return Call.NetworkType.CELLULAR;
        } else {
            return Call.NetworkType.UNKNOWN_NETWORK;
        }
    }

    public static Map<String, RTCStats> collectStats(RTCStatsReport report) {
        Map<String, RTCStats> result = new HashMap<>();
        // TODO: try to only send the codec that was used and the candidate-pair that was selected.
        // Otherwise there are too many codecs and candidate-pairs.
        Set<String> unwantedTypes = new HashSet<>(Arrays.asList("codec", "certificate", "media-source", "candidate-pair", "local-candidate", "remote-candidate"));
        for (Map.Entry<String, RTCStats> e : report.getStatsMap().entrySet()) {
            String type = e.getValue().getType();
            if (unwantedTypes.contains(type)) {
                continue;
            }
            result.put(e.getKey(), e.getValue());
        }
        return result;
    }

    public static String serializeWebrtcStats(Map<String, RTCStats> data) {
        try {
            Gson gson = new Gson();
            return gson.toJson(data);
        } catch (Exception e) {
            Log.e("Crash in stats processing", e);
            return "";
        }
    }

    private static void cleanupReportData(Map<String, RTCStats> data) {
        for (Map.Entry<String, RTCStats> e : data.entrySet()) {
            Map<String, Object> members = e.getValue().getMembers();
            members.remove("localCertificateId");
            members.remove("remoteCertificateId");
        }
    }

    private static void debugLogReportData(Map<String, RTCStats> data) {
        Gson gson = new GsonBuilder().create();
        List<String> keys = new ArrayList<>(data.keySet());
        Collections.sort(keys);
        for (String k : keys) {
            RTCStats stats = data.get(k);
            Log.d("CallStats: " + stats.getType() + " " + k + "\n" + gson.toJson(stats.getMembers()));
        }
    }

    public static String toPrettyJson(Map<String, RTCStats> data) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(data);
    }

    private static String diff(Object a, Object b) {
        if (a instanceof Integer) {
            return String.valueOf(((Integer) a) - ((Integer) b));
        } else if (a instanceof  Long) {
            return String.valueOf(((Long) a) - ((Long) b));
        } else if (a instanceof BigInteger) {
            return ((BigInteger) a).subtract((BigInteger) b).toString();
        } else if (a instanceof  Double) {
            return String.format(Locale.US, "%.3f", ((Double) a) - ((Double) b));
        } else {
            return "unable to handle " + a.getClass().toString();
        }
    }


    public static void sendEndCallEvent(String callId, UserId peerUid, CallType callType, boolean isInitiator, boolean isConnected, boolean isAnswered, boolean isLocalEnded, long callDuration, long iceTimeTaken, EndCall.Reason reason, RTCStatsReport report) {
        Log.i("CallManager sending call event " + callId);
        Call.NetworkType networkType = getNetworkType(AppContext.getInstance().get());

        Call.CallType protoCallType = Call.CallType.UNKNOWN_TYPE;
        if (callType == CallType.AUDIO) {
            protoCallType = Call.CallType.AUDIO;
        } else if (callType == CallType.VIDEO) {
            protoCallType = Call.CallType.VIDEO;
        }

        Map<String, RTCStats> reportData = collectStats(report);
        String webrtcStats = serializeWebrtcStats(reportData);
        cleanupReportData(reportData);
        if (BuildConfig.DEBUG) {
            Log.d("CallStats: " + toPrettyJson(reportData));
        } else {
            debugLogReportData(reportData);
        }

        Call.Builder callBuilder = Call.newBuilder()
                .setCallId(callId)
                .setPeerUid(peerUid.rawIdLong())
                .setType(protoCallType)
                .setDirection((isInitiator)? Call.CallDirection.OUTGOING : Call.CallDirection.INCOMING)
                .setAnswered(isAnswered)
                .setConnected(isConnected)
                .setDurationMs(callDuration)
                .setEndCallReason(reason.name())
                .setLocalEndCall(isLocalEnded)
                .setIceTimeTakenMs(iceTimeTaken)
                .setNetworkType(networkType)
                .setWebrtcStats(webrtcStats);
        Events.getInstance().sendEvent(callBuilder.build());
    }


}

package com.halloapp.calling;

import android.content.Context;
import android.net.ConnectivityManager;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.halloapp.AppContext;
import com.halloapp.id.UserId;
import com.halloapp.proto.log_events.Call;
import com.halloapp.proto.server.EndCall;
import com.halloapp.util.logs.Log;
import com.halloapp.util.stats.Events;

import org.webrtc.RTCStats;
import org.webrtc.RTCStatsReport;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class CallStats {

    public static final long REPORT_INTERVAL = 10000;
    public static final List<String> STATS_KEYS = Arrays.asList("packetsReceived", "bytesReceived", "packetsLost", "packetsDiscarded", "packetsSent", "bytesSent");

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
            String type = e.getValue().getType();
            if ("inbound-rtp".equals(type) || "outbound-rtp".equals(type)) {
                RTCStats s = e.getValue();
                if (s == null) {
                    Log.e("CallStats: s is null");
                    continue;
                }
                Map<String, Object> values = s.getMembers();
                Map<String, Object> lastValues;
                if (lastReport == null) {
                    continue;
                }
                RTCStats lastStats = lastReport.getStatsMap().get(e.getKey());
                if (lastStats == null) {
                    continue;
                }
                lastValues = lastStats.getMembers();

                StringBuilder log = new StringBuilder();
                for (String k : STATS_KEYS) {
                    if (values.containsKey(k) && lastValues.containsKey(k)) {
                        log.append(k).append(":").append(diff(values.get(k), lastValues.get(k))).append(" ");
                    }
                }
                Log.i("rtp-stats: " + log);
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

    public static String serializeWebrtcStats(RTCStatsReport report) {
        try {
            Gson gson = new Gson();
            Map<String, RTCStats> statsToSend = new HashMap<>();
            Set<String> unwantedTypes = new HashSet<>(Arrays.asList("codec", "certificate", "media-source", "candidate-pair", "local-candidate", "remote-candidate"));
            for (Map.Entry<String, RTCStats> e : report.getStatsMap().entrySet()) {
                String type = e.getValue().getType();
                if (unwantedTypes.contains(type)) {
                    continue;
                }
                statsToSend.put(e.getKey(), e.getValue());
            }
            return gson.toJson(statsToSend);
        } catch (Exception e) {
            Log.e("Crash in stats processing", e);
            return "";
        }
    }

    private static String diff(Object a, Object b) {
        if (a instanceof Integer) {
            return String.valueOf(((Integer) a) - ((Integer) b));
        } else if (a instanceof  Long) {
            return String.valueOf(((Long) a) - ((Long) b));
        } else if (a instanceof BigInteger) {
            return ((BigInteger) a).subtract((BigInteger) b).toString();
        } else if (a instanceof  Double) {
            return String.valueOf(((Double) a) - ((Double) b));
        } else {
            return "unable to handle " + a.getClass().toString();
        }
    }

    public static void sendEndCallEvent(String callId, UserId peerUid, boolean isInitiator, boolean isAnswered, long callDuration, EndCall.Reason reason, RTCStatsReport report) {
        Log.i("CallManager sending call event " + callId);
        Call.NetworkType networkType = getNetworkType(AppContext.getInstance().get());

        Call.Builder callBuilder = Call.newBuilder()
                .setCallId(callId)
                .setPeerUid(peerUid.rawIdLong())
                .setType(Call.CallType.AUDIO)
                .setDirection((isInitiator)? Call.CallDirection.OUTGOING : Call.CallDirection.INCOMING)
                .setAnswered(isAnswered)
                .setConnected(false)  // TODO(nikola): implement this
                .setDurationMs(callDuration)
                .setEndCallReason(reason.name())
                .setLocalEndCall(true)  // TODO(nikola): implement this
                .setNetworkType(networkType)
                .setWebrtcStats(serializeWebrtcStats(report));
        Events.getInstance().sendEvent(callBuilder.build());
    }


}

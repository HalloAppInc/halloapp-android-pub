package com.halloapp.calling;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.telecom.CallAudioState;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.widget.Toast;

import androidx.annotation.IntDef;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.halloapp.AppContext;
import com.halloapp.Constants;
import com.halloapp.NetworkConnectivityManager;
import com.halloapp.Notifications;
import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.CallMessage;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Message;
import com.halloapp.crypto.CryptoException;
import com.halloapp.id.UserId;
import com.halloapp.proto.server.CallType;
import com.halloapp.proto.server.EndCall;
import com.halloapp.proto.server.StartCallResult;
import com.halloapp.proto.server.StunServer;
import com.halloapp.proto.server.TurnServer;
import com.halloapp.proto.server.WebRtcSessionDescription;
import com.halloapp.ui.calling.CallActivity;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.calls.CallsApi;
import com.halloapp.xmpp.calls.GetCallServersResponseIq;
import com.halloapp.xmpp.calls.StartCallResponseIq;
import com.halloapp.xmpp.util.Observable;
import com.halloapp.xmpp.util.ObservableErrorException;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.CandidatePairChangeEvent;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RTCStatsCollectorCallback;
import org.webrtc.RtpReceiver;
import org.webrtc.RtpTransceiver;
import org.webrtc.SessionDescription;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.michaelrocks.libphonenumber.android.NumberParseException;
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil;
import io.michaelrocks.libphonenumber.android.Phonenumber;

public class CallManager {

    @IntDef({State.IDLE, State.CALLING, State.CALLING_RINGING, State.IN_CALL_CONNECTING, State.IN_CALL, State.INCOMING_RINGING, State.END})
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {
        int IDLE = 0;
        int CALLING = 1;
        int CALLING_RINGING = 2;
        int INCOMING_RINGING = 3;
        int IN_CALL_CONNECTING = 4;
        int IN_CALL = 5;
        int END = 6;
    }

    private @State int state;
    private boolean isInitiator;
    private boolean isAnswered;

    private boolean isMicrophoneMuted = false;
    private boolean isSpeakerPhoneOn = false;  // The default will have to change for video calls

    MediaConstraints audioConstraints;
    AudioSource audioSource;
    AudioTrack localAudioTrack;

    private PeerConnection peerConnection;
    private PeerConnectionFactory factory;

    private final CallAudioManager audioManager;
    private final OutgoingRingtone outgoingRingtone;

    private String callId;
    private UserId peerUid;

    private int restartIndex = 0;

    @Nullable
    private PhoneAccountHandle phoneAccountHandle = null;
    @Nullable
    private HaTelecomConnection telecomConnection;

    private ComponentName callService;
    @Nullable
    private final PowerManager.WakeLock proximityLock;

    @NonNull
    private final Timer timer = new Timer();

    @Nullable
    private TimerTask ringingTimeoutTimerTask;
    @Nullable
    private TimerTask iceRestartTimerTask;
    @NonNull
    private final CallStats callStats;

    private long callAnswerTimestamp = 0;
    private long callStartTimestamp = 0;
    private final MutableLiveData<Long> callStartLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isInCall = new MutableLiveData<>(false);

    private final Queue<HaIceCandidate> iceCandidateQueue = new LinkedList<>();

    private int outboundRerequestCount = 0;
    private static final int MAX_CALL_REREQUESTS = 5;

    // Executor thread is started once in private ctor and is used for all
    // peer connection API calls to ensure new peer connection factory is
    // created on the same thread as previously destroyed factory.
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private static CallManager instance;
    private final Set<CallObserver> observers;
    private final CallsApi callsApi;
    private final ContentDb contentDb;
    private final AppContext appContext;

    public static CallManager getInstance() {
        if (instance == null) {
            synchronized (CallManager.class) {
                if (instance == null) {
                    instance = new CallManager();
                }
            }
        }
        return instance;
    }

    private CallManager() {
        // TODO(nikola): CallsManager should observe the CallsApi for incoming events instead of passing this in the constructor
        this.callsApi = CallsApi.getInstance(this);
        this.contentDb = ContentDb.getInstance();
        this.appContext = AppContext.getInstance();
        this.outgoingRingtone = new OutgoingRingtone();
        this.proximityLock = createProximityLock();
        this.state = State.IDLE;

        this.audioManager = CallAudioManager.create(appContext.get());
        this.observers = new HashSet<>();
        this.callStats = new CallStats();

        if (Build.VERSION.SDK_INT >= 23) {
            executor.execute(this::telecomRegisterAccount);
        }

        NetworkConnectivityManager.getInstance().getNetworkInfo().observeForever(networkInfo -> {
            if (networkInfo != null) {
                Log.i("CallManager: network changed: " + networkInfo.getTypeName());
            } else {
                Log.i("CallManager: network changed: null");
            }
        });

        CallNetworkObserver.getInstance().register(appContext.get());
    }

    public void startCallActivity(Context context, UserId userId) {
        if (state == CallManager.State.IDLE) {
            context.startActivity(CallActivity.getStartCallIntent(context, userId));
        } else {
            Log.w("CallManager: user is already in a call " + toString() + ". Can not start new call to " + userId);
            String text = context.getString(R.string.unable_to_start_call);
            Toast.makeText(AppContext.getInstance().get(), text, Toast.LENGTH_SHORT).show();
        }
    }

    @WorkerThread
    @RequiresApi(api = 23)
    private void telecomRegisterAccount() {
        Log.i("CallManager: telecomRegisterAccount()");
        TelecomManager tm = (TelecomManager) appContext.get().getSystemService(Context.TELECOM_SERVICE);
        if (tm != null) {
            ComponentName cName = new ComponentName(appContext.get(), HaTelecomConnectionService.class);
            this.phoneAccountHandle = new PhoneAccountHandle(cName, "HalloApp");
            Log.i("CallManager: telecomRegisterAccount: phoneAccountHandle: " + phoneAccountHandle);

            final Icon icon = Icon.createWithResource(appContext.get(), R.drawable.ic_launcher_foreground);
            PhoneAccount phoneAccount = PhoneAccount.builder(phoneAccountHandle, "HalloApp")
                    // TODO(nikola): Add here for video calls: CAPABILITY_VIDEO_CALLING
                    // TODO(nikola): Telecom framework SELF_MANAGED was added in API level 26.. This is the reason
                    // why older android device get security exception.
                    .setCapabilities(PhoneAccount.CAPABILITY_SELF_MANAGED)
                    .setIcon(icon)
                    .build();

            tm.registerPhoneAccount(phoneAccount);
            Log.i("CallManager: phone account registered with telecom manager: " + phoneAccount);
        }
    }

    public void addObserver(CallObserver observer) {
        synchronized (observers) {
            observers.add(observer);
        }
    }

    public void removeObserver(CallObserver observer) {
        synchronized (observers) {
            observers.remove(observer);
        }
    }

    public @State int getState() {
        return state;
    }

    public long getCallStartTimestamp() {
        return callStartTimestamp;
    }

    public UserId getPeerUid() {
        return peerUid;
    }

    public LiveData<Boolean> getIsInCall() {
        return isInCall;
    }

    @MainThread
    public synchronized boolean startCall(@NonNull UserId peerUid) {
        if (this.state != State.IDLE) {
            Log.w("CallManager.startCall failed: state is not idle. State: " + stateToString(this.state));
            return false;
        }
        this.callId = RandomId.create();
        Log.i("CallManager.startCall callId: " + callId + " peerUid: " + peerUid);
        this.peerUid = peerUid;
        this.isInitiator = true;
        this.isAnswered = false;
        this.callService = startCallService();
        this.callStats.startStatsCollection();
        this.state = State.CALLING;
        this.isInCall.postValue(true);
        acquireLock();

        if (Build.VERSION.SDK_INT >= 23) {
            executor.execute(this::telecomPlaceCall);
        } else {
            finishStartCall();
        }
        return true;
    }

    public synchronized void finishStartCall() {
        if (callId == null) {
            Log.i("CallManager: finishStartCall() call already stopped");
            return;
        }
        Log.i("CallManager: finishStartCall()");
        mainHandler.post(this::startAudioManager);
        executor.execute(this::setupWebrtc);
    }

    @RequiresApi(api = 23)
    private void telecomPlaceCall() {
        TelecomManager tm = (TelecomManager) appContext.get().getSystemService(Context.TELECOM_SERVICE);
        if (tm != null) {
            Bundle extras = new Bundle();
            Contact contact = ContactsDb.getInstance().getContact(peerUid);
            Bundle innerExtras = new Bundle();
            innerExtras.putString(HaTelecomConnectionService.EXTRA_CALL_ID, callId);
            innerExtras.putString(HaTelecomConnectionService.EXTRA_PEER_UID, peerUid.rawId());
            innerExtras.putString(HaTelecomConnectionService.EXTRA_PEER_UID_NAME, contact.getDisplayName());
            innerExtras.putString(HaTelecomConnectionService.EXTRA_PEER_UID_PHONE, contact.getDisplayPhone());
            extras.putBundle(TelecomManager.EXTRA_OUTGOING_CALL_EXTRAS, innerExtras);
            extras.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, this.phoneAccountHandle);
            Uri uri = Uri.fromParts("tel", contact.normalizedPhone, null);
            try {
                // If the telecom framework approves the call it will call HaTelecomService.onCreateOutgoingConnection
                // which will come back here and call finishStartCall
                tm.placeCall(uri, extras);
            } catch (SecurityException e) {
                Log.e("TelecomManager.placeCall raised SecurityException " + e);
                Log.sendErrorReport("SecurityException while calling TelecomManager.placeCall");
                finishStartCall();
            }
        }
    }

    private ComponentName startCallService() {
        Log.i("startCallService");
        Intent serviceIntent = CallService.getIntent(peerUid, isInitiator);
        if (Build.VERSION.SDK_INT >= 26) {
            return appContext.get().startForegroundService(serviceIntent);
        } else {
            return appContext.get().startService(serviceIntent);
        }
    }

    @WorkerThread
    private void setupWebrtc() {
        Log.i("Initialize WebRTC");
        initializePeerConnectionFactory();
        createAVTracks();
        initializePeerConnections();
        startStreams();
        if (isInitiator) {
            getCallServersAndStartCall();
        }
    }

    public synchronized void stop(EndCall.Reason reason) {
        final long callDuration = (this.callStartTimestamp > 0)? SystemClock.elapsedRealtime() - this.callStartTimestamp : 0;
        Log.i("stop callId: " + callId + " peerUid" + peerUid + " reason: " + reason + " duration: " + callDuration / 1000);
        stopAudioManager();
        stopOutgoingRingtone();
        if (localAudioTrack != null) {
            localAudioTrack.setEnabled(false);
            localAudioTrack = null;
        }
        if (peerConnection != null) {
            peerConnection.getStats(report -> CallStats.sendEndCallEvent(callId, peerUid, isInitiator, isAnswered, callDuration, reason, report));
            peerConnection.close();
            peerConnection = null;
        }
        if (callService != null) {
            appContext.get().stopService(new Intent(appContext.get(), CallService.class));
            callService = null;
        }
        if (callDuration > 0) {
            storeCallLogMsg(peerUid, callId, callDuration);
        }
        cancelRingingTimeout();
        releaseLock();
        callStats.stopStatsCollection();
        isInitiator = false;
        isMicrophoneMuted = false;
        isSpeakerPhoneOn = false;
        callId = null;
        peerUid = null;
        clearCallTimer();
        restartIndex = 0;
        outboundRerequestCount = 0;
        state = State.IDLE;
        isInCall.postValue(false);
        if (telecomConnection != null) {
            if (Build.VERSION.SDK_INT >= 23) {
                telecomConnection.stop(reason);
            }
            telecomConnection = null;
        }
    }

    public synchronized void handleIncomingCall(@NonNull String callId, @NonNull UserId peerUid, @NonNull CallType callType, @Nullable String webrtcOffer,
                                   @NonNull List<StunServer> stunServers, @NonNull List<TurnServer> turnServers,
                                   long timestamp, long serverSentTimestamp, @Nullable CryptoException cryptoException) {
        Log.i("CallManager.handleIncomingCall " + callId + " peerUid: " + peerUid + " " + callType + " " + timestamp);
        if (serverSentTimestamp > 0 && timestamp > 0 && serverSentTimestamp - timestamp > Constants.CALL_RINGING_TIMEOUT_MS) {
            Log.i("CallManager: received stale call " + callId + " from " + peerUid);
            Log.i("CallManager: timestamp: " + timestamp + " serverSentTimestamp: " + serverSentTimestamp + " diff: " + (serverSentTimestamp - timestamp));
            storeMissedCallMsg(peerUid, callId, callType, timestamp);
            return;
        }
        if (this.callId != null && this.callId.equals(callId)) {
            Log.i("CallManager: duplicate incoming-call msg CallId: " + callId + " peerUid: " + peerUid);
            Log.i(toString());
            return;
        }
        if (this.state != State.IDLE && !callId.equals(this.callId)) {
            Log.i("CallManager: rejecting incoming call " + callId + " from " + peerUid + " because already in call.");
            Log.i(toString());
            callsApi.sendEndCall(callId, peerUid, EndCall.Reason.BUSY);
            storeMissedCallMsg(peerUid, callId, callType, timestamp);
            return;
        }

        if (!CallType.AUDIO.equals(callType)) {
            Log.i("CallManager: rejecting incoming call " + callId + " from " + peerUid + " because it's not audio");
            callsApi.sendEndCall(callId, peerUid, EndCall.Reason.VIDEO_UNSUPPORTED);
            storeMissedCallMsg(peerUid, callId, callType, timestamp);
            return;
        }
        this.isInitiator = false;
        this.peerUid = peerUid;
        this.callId = callId;

        if (webrtcOffer == null) {
            Log.e("handleIncomingCall() Failed to decrypt webrtcOffer callId:" + callId);
            sendRerequest(cryptoException);
            endCall(EndCall.Reason.DECRYPTION_FAILED);
            return;
        }

        setupWebrtc();
        Log.i("Setting webrtc offer " + webrtcOffer);
        peerConnection.setRemoteDescription(
                new SimpleSdpObserver(),
                new SessionDescription(SessionDescription.Type.OFFER, webrtcOffer));
        setStunTurnServers(stunServers, turnServers);

        this.state = State.INCOMING_RINGING;
        this.isInCall.postValue(true);
        notifyOnIncomingCall();

        if (Build.VERSION.SDK_INT >= 23) {
            telecomHandleIncomingCall();
        } else {
            showIncomingCallNotification();
        }
        processQueuedIceCandidates();
    }

    public void showIncomingCallNotification() {
        if (callId == null) {
            Log.w("CallManager: showIncomingCallNotification(): callId is null. call was already canceled");
            return;
        }
        Notifications.getInstance(appContext.get()).showIncomingCallNotification(callId, peerUid);
        callsApi.sendRinging(callId, peerUid);
        startRingingTimeoutTimer();
    }

    @RequiresApi(api = 23)
    public void telecomHandleIncomingCall() {
        TelecomManager tm = (TelecomManager) appContext.get().getSystemService(Context.TELECOM_SERVICE);
        if (tm != null && phoneAccountHandle != null) {
            Bundle extras = new Bundle();
            Contact c = ContactsDb.getInstance().getContact(peerUid);
            extras.putString(HaTelecomConnectionService.EXTRA_CALL_ID, callId);
            extras.putString(HaTelecomConnectionService.EXTRA_PEER_UID, peerUid.rawId());
            extras.putString(HaTelecomConnectionService.EXTRA_PEER_UID_NAME, c.getDisplayName());
            extras.putString(HaTelecomConnectionService.EXTRA_PEER_UID_PHONE, c.getDisplayPhone());
            tm.addNewIncomingCall(phoneAccountHandle, extras);
        }
    }

    public void telecomSetActive() {
        if (telecomConnection != null && Build.VERSION.SDK_INT >= 23) {
            telecomConnection.setActive();
        }
    }

    public void telecomOnAnswer() {
        Notifications.getInstance(appContext.get()).clearIncomingCallNotification();
        mainHandler.post(this::acceptCall);
    }

    public synchronized void setTelecomConnection(HaTelecomConnection telecomConnection) {
        if (callId == null) {
            if (Build.VERSION.SDK_INT >= 23) {
                telecomConnection.stop(EndCall.Reason.CALL_END);
            }
            return;
        }
        this.telecomConnection = telecomConnection;
    }

    public synchronized void handleCallRinging(@NonNull String callId, @NonNull UserId peerUid,@NonNull Long timestamp) {
        Log.i("CallManager: CallRinging callId: " + callId + " peerUid: " + peerUid + " ts: " + timestamp);
        if (this.callId == null || !this.callId.equals(callId) ) {
            Log.e("CallManager: Error: got call ringing message for call " + callId +
                    " but my call id is " + this.callId);
            return;
        }
        // TODO(nikola): check the peerUid
        if (!this.isInitiator) {
            Log.e("CallManager: Error: unexpected call ringing, not initiator");
            return;
        }
        if (this.state != State.CALLING) {
            Log.w("CallManager: Unexpected call-ringing message callId: " + callId + " peerUid: " + peerUid);
            Log.i("CallManager: " + toString());
            return;
        }
        this.state = State.CALLING_RINGING;
        notifyOnPeerIsRinging();
        startOutgoingRingtone();
    }

    public synchronized void handleAnswerCall(@NonNull String callId, @NonNull UserId peerUid, @Nullable String webrtcOffer, @NonNull Long timestamp,
                                              @Nullable CryptoException cryptoException) {
        Log.i("AnswerCall callId: " + callId + " peerUid: " + peerUid + " " + timestamp);

        if (this.callId == null || !this.callId.equals(callId)) {
            Log.e("Ignoring incoming answer call msg callId: " + callId + " from peerUid: " + peerUid + " " + toString());
            return;
        }
        if (this.peerConnection == null) {
            Log.e("Ignoring incoming answer call msg. peerConnection is not initialized " + toString());
            return;
        }

        stopOutgoingRingtone();
        cancelRingingTimeout();

        if (webrtcOffer == null) {
            sendRerequest(cryptoException);
            endCall(EndCall.Reason.DECRYPTION_FAILED);
            return;
        }
        if (this.isAnswered && callId.equals(this.callId)) {
            Log.w("CallManager: Duplicate answer-call msg " + toString());
            return;
        }

        peerConnection.setRemoteDescription(new SimpleSdpObserver(), new SessionDescription(SessionDescription.Type.ANSWER, webrtcOffer));
        this.state = State.IN_CALL_CONNECTING;
        this.isAnswered = true;
        this.callAnswerTimestamp = SystemClock.elapsedRealtime();
        telecomSetActive();
        notifyOnAnsweredCall();
        processQueuedIceCandidates();
    }

    public synchronized void handleEndCall(@NonNull String callId, @NonNull UserId peerUid,
                               @NonNull EndCall.Reason reason, @NonNull Long timestamp) {
        Log.i("got EndCall callId: " + callId + " peerUid: " + peerUid + " reason: " + reason.name() + " " + timestamp);
        if (reason == EndCall.Reason.CANCEL || reason == EndCall.Reason.TIMEOUT) {
            // TODO(nikola): fix here when we do video calls
            storeMissedCallMsg(peerUid, callId, CallType.AUDIO, timestamp);
        }
        if (this.callId == null || !this.callId.equals(callId)) {
            Log.i("got EndCall for wrong call. " + toString());
            return;
        }
        this.state = State.IDLE;
        notifyOnEndCall();
        // TODO(nikola): Handle multiple calls at the same time. We should only cancel the right
        // notification
        Notifications.getInstance(appContext.get()).clearIncomingCallNotification();
        stop(reason);
    }

    public void handleIceCandidate(@NonNull String callId, @NonNull UserId peerUid,
                                    @NonNull String sdpMediaId, int sdpMediaLineIndex, @NonNull String sdp) {
        Log.i("CallManager: got IceCandidate callId: " + callId + " " + sdpMediaId + ":" + sdpMediaLineIndex + ": sdp: " + sdp);
        IceCandidate candidate = new IceCandidate(sdpMediaId, sdpMediaLineIndex, sdp);

        if (this.callId != null && !this.callId.equals(callId)) {
            // TODO(nikola): This code is similar to many other messages
            Log.i("CallManager: got IceCandidates for the wrong callId: " + callId + " peerUid: " + peerUid + " state: " + toString());
            return;
        }
        if (state == State.IN_CALL || state == State.IN_CALL_CONNECTING || state == State.INCOMING_RINGING) {
            peerConnection.addIceCandidate(candidate);
        } else {
            HaIceCandidate haIceCandidate = new HaIceCandidate(callId, candidate);
            iceCandidateQueue.offer(haIceCandidate);
        }
    }

    public void handleIceRestartOffer(@NonNull String callId, int restartIndex, @Nullable String webrtcRestartOffer,
                                      @Nullable CryptoException cryptoException) {
        Log.i("CallManager: got iceRestartOffer callId: " + callId);
        if (this.callId == null || !this.callId.equals(callId)) {
            // TODO(nikola): This code is similar to many other messages
            Log.i("CallManager: got IceRestartOffer for the wrong callId: " + callId + " peerUid: " + peerUid + " state: " + toString());
            return;
        }
        if (webrtcRestartOffer == null) {
            Log.e("CallManager: failed to decrypt iceRestartOffer...");
            sendRerequest(cryptoException);
            return;
        }
        if (peerConnection != null) {
            Log.i("CallManager peerConnection.setRemoteDescription called after receiving iceRestartOffer");
            peerConnection.setRemoteDescription(new SimpleSdpObserver(), new SessionDescription(SessionDescription.Type.OFFER, webrtcRestartOffer));
            peerConnection.createAnswer(new SimpleSdpObserver() {
                @Override
                public void onCreateSuccess(@NonNull SessionDescription sessionDescription) {
                    Log.i("PeerConnection ice restart answer is ready " + sessionDescription);
                    peerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);

                    try {
                        callsApi.sendIceRestartAnswer(callId, peerUid, restartIndex, sessionDescription.description);
                    } catch (CryptoException e) {
                        Log.e("CallManager: failed to encrypt ice restart Answer", e);
                    }
                }
            }, new MediaConstraints());
        }
    }

    public void handleIceRestartAnswer(@NonNull String callId, int restartIndex, @Nullable String webrtcRestartAnswer,
                                       @Nullable CryptoException cryptoException) {
        Log.i("CallManager: got iceRestartAnswer callId: " + callId);
        if (this.callId == null || !this.callId.equals(callId)) {
            // TODO(nikola): This code is similar to many other messages
            Log.i("CallManager: got IceRestartAnswer for the wrong callId: " + callId + " peerUid: " + peerUid + " state: " + toString());
            return;
        }
        if (webrtcRestartAnswer == null) {
            Log.e("CallManager: failed to decrypt iceRestartAnswer...");
            sendRerequest(cryptoException);
            return;
        }
        if (peerConnection != null) {
            Log.i("CallManager peerConnection.setRemoteDescription called after receiving iceRestartAnswer");
            peerConnection.setRemoteDescription(new SimpleSdpObserver(), new SessionDescription(SessionDescription.Type.ANSWER, webrtcRestartAnswer));
        }
    }

    @MainThread
    public synchronized boolean acceptCall() {
        if (this.isInitiator) {
            Log.e("ERROR user clicked accept call but is the call initiator callId: " + callId);
            return false;
        }
        if (this.state != State.INCOMING_RINGING) {
            Log.w("CallManager.acceptCall call is not in INCOMING_RINGING state. State: " + stateToString(state));
            return false;
        }

        cancelRingingTimeout();
        doAnswer();
        this.state = State.IN_CALL_CONNECTING;
        this.isAnswered = true;
        this.callAnswerTimestamp = SystemClock.elapsedRealtime();
        notifyOnAnsweredCall();
        telecomSetActive();
        return true;
    }

    public LiveData<Long> getCallStartTimeLiveData() {
        return callStartLiveData;
    }

    private void processQueuedIceCandidates() {
        Log.i("CallManager: processing iceCandidateQueue: " + iceCandidateQueue.size());
        HaIceCandidate haIceCandidate;
        while (!iceCandidateQueue.isEmpty()) {
            haIceCandidate = iceCandidateQueue.poll();
            if (haIceCandidate == null) continue;
            if (this.callId.equals(haIceCandidate.getCallId())) {
                IceCandidate ic = haIceCandidate.getIceCandidate();
                Log.i("CallManager: adding queued IceCandidate " + ic);
                peerConnection.addIceCandidate(ic);
            } else {
                Log.w("CallManager: dropping IceCandidate callId:" + haIceCandidate.getCallId() +  " ic: " + haIceCandidate.getIceCandidate());
            }
        }
    }

    private void initializePeerConnectionFactory() {
        // TODO(nikola): when we want to do video
//        final VideoEncoderFactory encoderFactory;
//        final VideoDecoderFactory decoderFactory;
//        encoderFactory = new DefaultVideoEncoderFactory(
//                rootEglBase.getEglBaseContext(), true /* enableIntelVp8Encoder */, true);
//        decoderFactory = new DefaultVideoDecoderFactory(
//                rootEglBase.getEglBaseContext());
        PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions.builder(appContext.get())
                        .setEnableInternalTracer(true)
                        .createInitializationOptions());
        PeerConnectionFactory.Builder builder = PeerConnectionFactory.builder();
//                .setVideoEncoderFactory(encoderFactory)
//                .setVideoDecoderFactory(decoderFactory);
        builder.setOptions(null);
        factory = builder.createPeerConnectionFactory();
    }

    private void createAVTracks() {
        audioConstraints = new MediaConstraints();
        // TODO(nikola): enable this for video calls
//        VideoCapturer videoCapturer = createVideoCapturer();
//        VideoSource videoSource = factory.createVideoSource(videoCapturer);
//        videoCapturer.startCapture(VIDEO_RESOLUTION_WIDTH, VIDEO_RESOLUTION_HEIGHT, FPS);

//        videoTrackFromCamera = factory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
//        videoTrackFromCamera.setEnabled(true);
//        videoTrackFromCamera.addRenderer(new VideoRenderer(binding.surfaceView));

        //create an AudioSource instance
        audioSource = factory.createAudioSource(audioConstraints);
        localAudioTrack = factory.createAudioTrack("101", audioSource);
    }

    private void initializePeerConnections() {
        peerConnection = createPeerConnection(factory);
        Log.i("PeerConnection " + peerConnection + " created");
    }

    private void clearCallTimer() {
        callStartTimestamp = 0;
        callStartLiveData.postValue(null);
    }

    private void initializeCallTimer() {
        if (callStartTimestamp == 0) {
            callStartTimestamp = SystemClock.elapsedRealtime();
            callStartLiveData.postValue(callStartTimestamp);
        }
    }

    private void startStreams() {
        MediaStream mediaStream = factory.createLocalMediaStream("ARDAMS");
        // TODO(nikola): do this for video call
//        mediaStream.addTrack(videoTrackFromCamera);
        mediaStream.addTrack(localAudioTrack);
        peerConnection.addStream(mediaStream);
    }


    private PeerConnection createPeerConnection(@NonNull PeerConnectionFactory factory) {
        ArrayList<PeerConnection.IceServer> iceServers = new ArrayList<>();
        // TODO(nikola): maybe we should have some default stun server?
//        String URL = "stun:stun.l.google.com:19302";
//        String STUN_URL = "stun:stun.halloapp.dev:3478";
//        iceServers.add(PeerConnection.IceServer.builder(STUN_URL).createIceServer());
//        Log.i("ice servers: " + iceServers);

        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);

        // TODO(nikola): log better this events on the peer connection.
        PeerConnection.Observer pcObserver = new PeerConnection.Observer() {
            @Override
            public void onSignalingChange(PeerConnection.SignalingState signalingState) {
                Log.i("PeerConnection: onSignalingChange: ");
            }

            @Override
            public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
                Log.i("PeerConnection: onIceConnectionChange: " + iceConnectionState);
                if (iceConnectionState == PeerConnection.IceConnectionState.DISCONNECTED) {
                    // TODO(nikola): Maybe do a IN_CALL_RECONNECTING state if the IceConnectionState is FAILED/DISCONNECTED
                    startIceReconnectTimer();
                }
                if (iceConnectionState == PeerConnection.IceConnectionState.CONNECTED && state == State.IN_CALL_CONNECTING) {
                    iceConnected();
                }
            }

            @Override
            public void onIceConnectionReceivingChange(boolean b) {
                Log.i("PeerConnection: onIceConnectionReceivingChange: ");
            }

            @Override
            public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
                Log.i("PeerConnection: onIceGatheringChange: ");
            }

            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                Log.i("PeerConnection: onIceCandidate: " + iceCandidate);
                callsApi.sendIceCandidate(callId, peerUid, iceCandidate);
            }

            @Override
            public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
                Log.i("PeerConnection: onIceCandidatesRemoved: ");
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                Log.i("PeerConnection: onAddStream: " + mediaStream.audioTracks.size() + " "
                        + mediaStream.videoTracks.size());
                // TODO: enable for video calls
                //VideoTrack remoteVideoTrack = mediaStream.videoTracks.get(0);
                AudioTrack remoteAudioTrack = mediaStream.audioTracks.get(0);
                remoteAudioTrack.setEnabled(true);

                //remoteVideoTrack.setEnabled(true);
                //remoteVideoTrack.addRenderer(new VideoRenderer(binding.surfaceView2));

            }

            @Override
            public void onRemoveStream(MediaStream mediaStream) {
                Log.i("PeerConnection: onRemoveStream: ");
            }

            @Override
            public void onDataChannel(DataChannel dataChannel) {
                Log.i("PeerConnection: onDataChannel: ");
            }

            @Override
            public void onRenegotiationNeeded() {
                Log.i("PeerConnection: onRenegotiationNeeded: ");
            }

            @Override
            public void onStandardizedIceConnectionChange(PeerConnection.IceConnectionState newState) {
                Log.i("PeerConnection: onStandardizedIceConnectionChange: " + newState);
            }

            @Override
            public void onConnectionChange(PeerConnection.PeerConnectionState newState) {
                Log.i("PeerConnection: onConnectionChange: newState:" + newState);
            }

            @Override
            public void onSelectedCandidatePairChanged(CandidatePairChangeEvent event) {
                Log.i("PeerConnection: onSelectedCandidatePairChanged: local:" + event.local + " remote:" + event.remote + " reason:" + event.reason);
            }

            @Override
            public void onAddTrack(RtpReceiver receiver, MediaStream[] mediaStreams) {
                receiver.SetObserver(mediaType ->
                        Log.i("PeerConnection: OnFirestPacketReceived: RtpReceiver: " + receiver.id() + " mediaType:" + mediaType));
                Log.i("PeerConnection: onAddTrack: RtpReceiver: " + receiver.id() + " mediaStreams:" + mediaStreams.toString());
            }

            @Override
            public void onRemoveTrack(RtpReceiver receiver) {
                Log.i("PeerConnection: onAddTrack: RtpReceiver: " + receiver.id());
            }

            @Override
            public void onTrack(RtpTransceiver transceiver) {
                Log.i("PeerConnection: onAddTrack: RtpTransceiver: " + transceiver);
            }
        };

        return factory.createPeerConnection(rtcConfig, pcObserver);
    }

    public void iceConnected() {
        this.state = State.IN_CALL;
        initializeCallTimer();
        notifyOnCallConnected();
        Log.i(String.format("CallManager: ice is now connected. Took %dms", this.callStartTimestamp - this.callAnswerTimestamp));
    }

    private void getCallServersAndStartCall() {
        Observable<GetCallServersResponseIq> observable = callsApi.getCallServers(callId, peerUid, CallType.AUDIO);
        observable.onResponse(response -> {
            Log.i("CallManager: got call servers " + response);
            if (peerConnection == null || response == null) {
                // call probably was canceled while we were waiting for the server response.
                return;
            }
            if ((response.turnServers != null && response.turnServers.size() > 0) ||
                    (response.stunServers != null && response.stunServers.size() > 0)) {
                setStunTurnServers(response.stunServers, response.turnServers);
                doStartCall();
            } else {
                Log.e("CallManager: Did not get any stun or turn servers " + response);
                stop(EndCall.Reason.SYSTEM_ERROR);
            }
        }).onError(e -> {
            Log.e("CallManager: Failed to start call, did not get ice servers", e);
            stop(EndCall.Reason.SYSTEM_ERROR);
        });
    }

    private void doStartCall() {
        MediaConstraints sdpMediaConstraints = new MediaConstraints();

        sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        // TODO(nikola): add for video calls
        // sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));

        peerConnection.createOffer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(@NonNull SessionDescription sessionDescription) {
                Log.i("onCreateSuccess: ");
                peerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);

                try {
                    Observable<StartCallResponseIq> observable = callsApi.startCall(
                            callId, peerUid, CallType.AUDIO, sessionDescription.description);

                    StartCallResponseIq response = observable.await();
                    Log.i("received StartCallResult " + response.result +
                            " turn " + response.turnServers +
                            " stun " + response.stunServers +
                            " ts " + response.timestamp);
                    if (response.result == StartCallResult.Result.OK) {
                        startRingingTimeoutTimer();
                    } else {
                        Log.w("StartCall failed " + response.result);
                        // TODO(nikola): handle call not ok
                    }
                    // TODO(nikola): handle the exceptions. Call stop()
                } catch (CryptoException e) {
                    Log.e("CallManager: CryptoException, Failed to send the start call IQ callId: " + callId + " peerUid: " + peerUid, e);
                } catch (InterruptedException | ObservableErrorException e) {
                    Log.e("CallManager: Failed to send the start call IQ callId: " + callId + " peerUid: " + peerUid, e);
                }
            }
        }, sdpMediaConstraints);
    }

    private void setStunTurnServers(@Nullable List<StunServer> stunServers, @Nullable List<TurnServer> turnServers) {
        // insert the stun and turn servers and update the peerConnection configuration.
        // stun/turn servers URLS look like this "stun:stun.l.google.com:19302";
        ArrayList<PeerConnection.IceServer> iceServers = new ArrayList<>();
        if (stunServers != null) {
            for (StunServer stunServer : stunServers) {
                String stunUrl = "stun:" + stunServer.getHost() + ":" + stunServer.getPort();
                iceServers.add(PeerConnection.IceServer.builder(stunUrl).createIceServer());
            }
        }

        if (turnServers != null) {
            for (TurnServer turnServer : turnServers) {
                String turnUrl = "turn:" + turnServer.getHost() + ":" + turnServer.getPort();
                iceServers.add(PeerConnection.IceServer.builder(turnUrl)
                        .setUsername(turnServer.getUsername())
                        .setPassword(turnServer.getPassword())
                        .createIceServer());
            }
        }
        Log.i("CallManager: iceServers: " + iceServers);

        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
        peerConnection.setConfiguration(rtcConfig);
    }

    @MainThread
    private void doAnswer() {
        Log.i("Answering callId: " + callId + " peerUid: " + peerUid);
        if (this.callService == null) {
            this.callService = startCallService();
        }
        acquireLock();
        startAudioManager();
        callStats.startStatsCollection();

        peerConnection.createAnswer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(@NonNull SessionDescription sessionDescription) {
                Log.i("PeerConnection answer is ready " + sessionDescription);
                peerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);

                try {
                    WebRtcSessionDescription answer = CallsApi.encryptCallPayload(sessionDescription.description, peerUid);
                    Log.i("CallManager: encrypted answer: size:" +  answer.getEncPayload().size());
                    callsApi.sendAnswerCall(callId, peerUid, answer);
                } catch (CryptoException e) {
                    Log.e("CallManager: failed to encrypt webrtc Answer", e);
                    endCall(EndCall.Reason.ENCRYPTION_FAILED);
                }
            }
        }, new MediaConstraints());
    }

    @SuppressWarnings("UnusedReturnValue")
    private boolean sendRerequest(CryptoException e) {
        byte[] teardownKey = (e != null) ? e.teardownKey : null;
        outboundRerequestCount++;
        if (outboundRerequestCount <= MAX_CALL_REREQUESTS) {
            callsApi.sendCallRerequest(callId, peerUid, outboundRerequestCount, teardownKey);
            return true;
        } else {
            Log.e("CallManager: reached max rerequest count. Not sending any more rerequests");
            return false;
        }
    }

    public void endCall(EndCall.Reason reason) {
        if (callId == null) {
            return;
        }
        Log.i("CallManager.endCall callId: " + callId + " reason: " + reason);
        callsApi.sendEndCall(callId, peerUid, reason);
        notifyOnEndCall();
        stop(reason);
    }

    public boolean getPeerConnectionStats(RTCStatsCollectorCallback c) {
        if (peerConnection != null) {
            peerConnection.getStats(c);
            return true;
        }
        return false;
    }

    public boolean isMicrophoneMuted() {
        return isMicrophoneMuted;
    }

    public void setMicrophoneMute(boolean mute) {
        Log.i("CallManager.setMicrophoneMute(" + mute + ") was: " + this.isMicrophoneMuted);
        isMicrophoneMuted = mute;

        if (Build.VERSION.SDK_INT >= 26) {
            localAudioTrack.setEnabled(!mute);
        } else {
            audioManager.setMicrophoneMute(mute);
        }
        notifyOnMicrophoneMuteToggle();
    }

    public void toggleMicrophoneMute() {
        setMicrophoneMute(!isMicrophoneMuted());
    }

    public boolean isSpeakerPhoneOn() {
        return isSpeakerPhoneOn;
    }

    public void setSpeakerPhoneOn(boolean on) {
        Log.i("CallManager.setSpeakerPhoneOn(" + on + ") was: " + isSpeakerPhoneOn);
        if (Build.VERSION.SDK_INT >= 26 && telecomConnection != null) {
            // TODO(nikola): what if the call is going to bluetooth right now?
            telecomConnection.setAudioRoute(on ? CallAudioState.ROUTE_SPEAKER : CallAudioState.ROUTE_EARPIECE);
        } else {
            audioManager.setDefaultAudioDevice(on ? CallAudioManager.AudioDevice.SPEAKER_PHONE : CallAudioManager.AudioDevice.EARPIECE);
        }
        isSpeakerPhoneOn = on;
        notifyOnSpeakerPhoneToggle();
    }

    public void toggleSpeakerPhone() {
        setSpeakerPhoneOn(!isSpeakerPhoneOn());
    }

    private void notifyOnIncomingCall() {
        synchronized (observers) {
            for (CallObserver o : observers) {
                o.onIncomingCall(callId, peerUid);
            }
        }
    }

    private void notifyOnPeerIsRinging() {
        synchronized (observers) {
            for (CallObserver o : observers) {
                o.onPeerIsRinging(callId, peerUid);
            }
        }
    }

    private void notifyOnAnsweredCall() {
        synchronized (observers) {
            for (CallObserver o : observers) {
                o.onAnsweredCall(callId, peerUid);
            }
        }
    }

    private void notifyOnEndCall() {
        synchronized (observers) {
            for (CallObserver o : observers) {
                o.onEndCall(callId, peerUid);
            }
        }
    }

    private void notifyOnMicrophoneMuteToggle() {
        synchronized (observers) {
            for (CallObserver o : observers) {
                o.onMicrophoneMute(isMicrophoneMuted);
            }
        }
    }

    private void notifyOnSpeakerPhoneToggle() {
        synchronized (observers) {
            for (CallObserver o : observers) {
                o.onSpeakerPhoneOn(isSpeakerPhoneOn);
            }
        }
    }

    private void notifyOnCallConnected() {
        synchronized (observers) {
            for (CallObserver o : observers) {
                o.onCallConnected(callId);
            }
        }
    }

    private void startRingingTimeoutTimer() {
        synchronized (timer) {
            if (ringingTimeoutTimerTask != null) {
                Log.e("another outgoingRingTimerTask already exists");
                ringingTimeoutTimerTask.cancel();
            }
            ringingTimeoutTimerTask = new TimerTask() {
                @Override
                public void run() {
                    onRingingTimeout(callId);
                }
            };
            timer.schedule(ringingTimeoutTimerTask, Constants.CALL_RINGING_TIMEOUT_MS);
        }
    }

    private void onRingingTimeout(@NonNull String callId) {
        synchronized (timer) {
            // this code runs for both incoming and outgoing call ringing timeout
            Log.i("onCallTimeout");
            if (this.callId != null && this.callId.equals(callId)) {
                if (!this.isInitiator && this.state == State.INCOMING_RINGING) {
                    // TODO(nikola): fix here when we do video)
                    storeMissedCallMsg(this.peerUid, this.callId, CallType.AUDIO);
                }
                // TODO(nikola): this could clear the wrong notification if we have multiple incoming calls.
                Notifications.getInstance(appContext.get()).clearIncomingCallNotification();
                endCall(EndCall.Reason.TIMEOUT);
            }
        }
    }

    private void cancelRingingTimeout() {
        synchronized (timer) {
            if (ringingTimeoutTimerTask != null) {
                Log.i("canceling ringingTimeoutTimerTask");
                ringingTimeoutTimerTask.cancel();
                ringingTimeoutTimerTask = null;
            }
        }
    }

    // The android media player is not thread safe. Making sure it is always interacted on from the same thread.
    private void startOutgoingRingtone() {
        executor.execute(() -> {
            ContactsDb contactsDb = ContactsDb.getInstance();
            Contact contact = contactsDb.getContact(peerUid);
            String phone = contact.normalizedPhone;
            PhoneNumberUtil phoneUtil = PhoneNumberUtil.createInstance(AppContext.getInstance().get());
            Log.i("CallManager: peerUid " + peerUid.rawId() + " phone: " + phone);
            String peerCC = "ZZ";
            try {
                Phonenumber.PhoneNumber phoneNumber = phoneUtil.parse("+" + phone, null);
                peerCC = phoneUtil.getRegionCodeForCountryCode(phoneNumber.getCountryCode());
            } catch (NumberParseException e) {
                Log.e("Failed to parse peerUid phone " + phone, e);
                Log.sendErrorReport("Failed to parse peerUid phone");
            }
            Log.i("CallManager: peerUid phone: " + phone + " CC: " + peerCC);

            outgoingRingtone.start(OutgoingRingtone.Type.RINGING, peerCC);
        });
    }

    private void stopOutgoingRingtone() {
        executor.execute(outgoingRingtone::stop);
    }

    private void startIceReconnectTimer() {
        synchronized (timer) {
            if (iceRestartTimerTask != null) {
                Log.i("CallManager: another iceRestartTimerTask already exists");
                iceRestartTimerTask.cancel();
            }
            iceRestartTimerTask = new TimerTask() {
                @Override
                public void run() {
                    maybeRestartIce();
                }
            };
            Log.i("CallManager: start IceRestartTimerTask");
            timer.schedule(iceRestartTimerTask, Constants.CALL_ICE_RESTART_TIMEOUT_MS);
        }
    }

    private void maybeRestartIce() {
        Log.i("CallManager.maybeRestartIce()");
        if ((state == State.IN_CALL || state == State.IN_CALL_CONNECTING) && isInitiator && peerConnection != null) {
            PeerConnection.IceConnectionState iceConnectionState = peerConnection.iceConnectionState();
            if (iceConnectionState == PeerConnection.IceConnectionState.DISCONNECTED || iceConnectionState == PeerConnection.IceConnectionState.FAILED) {
                Log.i("CallManager: Performing ICE Restart");
                peerConnection.restartIce();

                MediaConstraints sdpMediaConstraints = new MediaConstraints();
                // TODO(nikola): code is duplicated with startCall
                sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
                // TODO(nikola): add for video calls
                // sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));

                peerConnection.createOffer(new SimpleSdpObserver() {
                    @Override
                    public void onCreateSuccess(@NonNull SessionDescription sessionDescription) {
                        Log.i("CallManager: offerCreated: ");
                        peerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
                        try {
                            Log.i("CallManager: sending iceRestartOffer");
                            callsApi.sendIceRestartOffer(callId, peerUid, restartIndex, sessionDescription.description);
                            // TODO(nikola): handle the exceptions. Call stop()
                        } catch (CryptoException e) {
                            Log.e("CallManager: CryptoException, Failed to send the iceRestartOffer Msg callId: " + callId + " peerUid: " + peerUid, e);
                        }
                    }
                }, sdpMediaConstraints);
            }
        }
    }

    private @Nullable PowerManager.WakeLock createProximityLock() {
        PowerManager pm = (PowerManager) appContext.get().getSystemService(Context.POWER_SERVICE);
        if (pm.isWakeLockLevelSupported(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK)) {
            return pm.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "halloapp:call");
        } else {
            return null;
        }
    }

    @SuppressLint("WakelockTimeout")
    private void acquireLock() {
        if (proximityLock != null && !proximityLock.isHeld()) {
            proximityLock.acquire();
        }
    }

    private void releaseLock() {
        if (proximityLock != null && proximityLock.isHeld()) {
            proximityLock.release(PowerManager.RELEASE_FLAG_WAIT_FOR_NO_PROXIMITY);
        }
    }

    public static String stateToString(@State int state) {
        switch (state) {
            case State.IDLE:
                return "idle";
            case State.CALLING:
                return "calling";
            case State.CALLING_RINGING:
                return "calling-ringing";
            case State.INCOMING_RINGING:
                return "ringing";
            case State.IN_CALL_CONNECTING:
                return "in-call-connecting";
            case State.IN_CALL:
                return "in-call";
            case State.END:
                return "end";
            default:
                return "unknown";
        }
    }

    public @NonNull String toString() {
        return "CallManager{state=" + stateToString(this.state) + ",callId=" + this.callId + ",peerUid=" + peerUid + "}";
    }

    @MainThread
    private void startAudioManager() {
        // Store existing audio settings and change audio mode to
        // MODE_IN_COMMUNICATION for best possible VoIP performance.
        if (Build.VERSION.SDK_INT < 26) {
            Log.i("Starting the audio manager " + audioManager);
            audioManager.start((audioDevice, availableAudioDevices) -> Log.i("onAudioManagerDevicesChanged: " + availableAudioDevices + ", " + "selected: " + audioDevice));
        }
    }

    private void stopAudioManager() {
        Log.i("CallManager: stoping CallAudioManager");
        if (Build.VERSION.SDK_INT < 26) {
            mainHandler.post(audioManager::stop);
        }
    }

    private void storeCallLogMsg(@NonNull UserId userId, @NonNull String callId, long callDuration) {
        int msgType = CallMessage.Usage.LOGGED_VOICE_CALL;
        final CallMessage message = new CallMessage(0,
                userId,
                isInitiator ? UserId.ME : userId,
                callId,
                System.currentTimeMillis(),
                msgType,
                Message.STATE_OUTGOING_DELIVERED);
        message.callDuration = callDuration;
        message.addToStorage(contentDb);
    }

    private void storeMissedCallMsg(@NonNull UserId userId, @NonNull String callId, @NonNull CallType callType) {
        storeMissedCallMsg(userId, callId, callType, System.currentTimeMillis());
    }

    private void storeMissedCallMsg(@NonNull UserId userId, @NonNull String callId, @NonNull CallType callType, long timestamp) {
        int msgType = CallMessage.Usage.MISSED_VOICE_CALL;
        if (callType == CallType.VIDEO) {
            msgType = CallMessage.Usage.MISSED_VIDEO_CALL;
        }
        final Message message = new CallMessage(0,
                userId,
                userId,
                callId,
                timestamp,
                msgType,
                Message.STATE_OUTGOING_DELIVERED);
        message.addToStorage(contentDb);
    }
}

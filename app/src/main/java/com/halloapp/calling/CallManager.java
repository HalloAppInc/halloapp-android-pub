package com.halloapp.calling;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import com.halloapp.ConnectionObservers;
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
import com.halloapp.proto.clients.Video;
import com.halloapp.proto.server.CallType;
import com.halloapp.proto.server.EndCall;
import com.halloapp.proto.server.StartCallResult;
import com.halloapp.proto.server.StunServer;
import com.halloapp.proto.server.TurnServer;
import com.halloapp.proto.server.WebRtcSessionDescription;
import com.halloapp.ui.calling.CallActivity;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.calls.CallsApi;
import com.halloapp.xmpp.calls.GetCallServersResponseIq;
import com.halloapp.xmpp.calls.StartCallResponseIq;
import com.halloapp.xmpp.util.Observable;
import com.halloapp.xmpp.util.ObservableErrorException;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.CandidatePairChangeEvent;
import org.webrtc.DataChannel;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RTCStatsCollectorCallback;
import org.webrtc.RtpReceiver;
import org.webrtc.RtpTransceiver;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoSink;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

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
    private static final String VIDEO_TRACK_ID = "vt0";
    private static final String AUDIO_TRACK_ID = "at0";
    private static final int VIDEO_HEIGHT = 1280;
    private static final int VIDEO_WIDTH = 720;
    private static final int VIDEO_FPS = 30;


    private @State int state;
    private CallType callType = CallType.UNKNOWN_TYPE;
    private boolean isInitiator;
    private boolean isAnswered;

    private boolean isMicrophoneMuted = false;
    private boolean isSpeakerPhoneOn = false;  // The default will have to change for video calls

    MediaConstraints audioConstraints;
    AudioSource audioSource;
    AudioTrack localAudioTrack;
    AudioTrack remoteAudioTrack;

    @Nullable
    private VideoCapturer videoCapturer;
    @Nullable
    private SurfaceTextureHelper surfaceTextureHelper;
    @Nullable
    private VideoSource videoSource;
    private VideoTrack localVideoTrack;
    private VideoTrack remoteVideoTrack;
    private EglBase rootEglBase;
    private VideoSink localVideoSink;
    private VideoSink remoteVideoSink;


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
    @Nullable
    private TimerTask noConnectionTimerTask;
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
    private CallsApi callsApi;
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
        this.contentDb = ContentDb.getInstance();
        this.appContext = AppContext.getInstance();
        this.outgoingRingtone = new OutgoingRingtone();
        this.proximityLock = createProximityLock();
        this.state = State.IDLE;

        this.audioManager = CallAudioManager.create(appContext.get());
        this.observers = new HashSet<>();
        this.callStats = new CallStats();

        executor.execute(this::initializePeerConnectionFactory);
        if (Build.VERSION.SDK_INT >= 26) {
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

    public void init() {
        this.callsApi = new CallsApi(this, Connection.getInstance());
        this.callsApi.init();
    }

    public void startCallActivity(Context context, UserId userId, CallType callType) {
        if (state == CallManager.State.IDLE) {
            context.startActivity(CallActivity.getStartCallIntent(context, userId, callType));
        } else {
            Log.w("CallManager: user is already in a call " + toString() + ". Can not start new call to " + userId);
            String text = context.getString(R.string.unable_to_start_call);
            Toast.makeText(AppContext.getInstance().get(), text, Toast.LENGTH_SHORT).show();
        }
    }

    @WorkerThread
    @RequiresApi(api = 26)
    private void telecomRegisterAccount() {
        Log.i("CallManager: telecomRegisterAccount()");
        TelecomManager tm = (TelecomManager) appContext.get().getSystemService(Context.TELECOM_SERVICE);
        if (tm != null) {
            ComponentName cName = new ComponentName(appContext.get(), HaTelecomConnectionService.class);
            this.phoneAccountHandle = new PhoneAccountHandle(cName, "HalloApp");
            Log.i("CallManager: telecomRegisterAccount: phoneAccountHandle: " + phoneAccountHandle);

            final Icon icon = Icon.createWithResource(appContext.get(), R.drawable.ic_launcher_foreground);
            PhoneAccount phoneAccount = PhoneAccount.builder(phoneAccountHandle, "HalloApp")
                    // Telecom framework SELF_MANAGED was added in API level 26.. This is the reason
                    // why older android device get security exception.
                    .setCapabilities(PhoneAccount.CAPABILITY_SELF_MANAGED)
                    // TODO(nikola): Look into this further
                    // I thought we should add this capability but this caused the phone call app to take over our call somehow
                    // There should be something to tell the Telecom framework our app supports video calls
                    //.setCapabilities(PhoneAccount.CAPABILITY_VIDEO_CALLING)
                    .setIcon(icon)
                    .build();
            // TODO(nikola): Explore adding EXTRA_LOG_SELF_MANAGED_CALLS
            // and EXTRA_ADD_SELF_MANAGED_CALLS_TO_INCALLSERVICE

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

    public EglBase getEglBase() {
        if (rootEglBase == null) {
            rootEglBase = EglBase.create();
        }
        return rootEglBase;
    }

    public void setVideoSinks(VideoSink localVideoSink, VideoSink remoteVideoSink) {
        VideoSink oldLocalVideoSink = this.localVideoSink;
        VideoSink oldRemoteVideoSink = this.remoteVideoSink;
        this.localVideoSink = localVideoSink;
        this.remoteVideoSink = remoteVideoSink;
        if (localVideoTrack != null) {
            if (oldLocalVideoSink != null) {
                localVideoTrack.removeSink(oldLocalVideoSink);
            }
            localVideoTrack.addSink(localVideoSink);
        }
        if (remoteVideoTrack != null) {
            if (oldRemoteVideoSink != null) {
                remoteVideoTrack.removeSink(oldRemoteVideoSink);
            }
            remoteVideoTrack.addSink(remoteVideoSink);
        }
    }


    @MainThread
    public synchronized boolean startCall(@NonNull UserId peerUid, @NonNull CallType callType, @Nullable VideoCapturer videoCapturer,
                                          @Nullable VideoSource videoSource, @Nullable SurfaceTextureHelper surfaceTextureHelper) {
        if (this.state != State.IDLE) {
            Log.w("CallManager.startCall failed: state is not idle. State: " + stateToString(this.state));
            return false;
        }
        this.callId = RandomId.create();
        Log.i("CallManager.startCall callId: " + callId + " peerUid: " + peerUid);
        this.peerUid = peerUid;
        this.callType = callType;
        this.isInitiator = true;
        this.isAnswered = false;
        this.callService = startCallService();
        this.callStats.startStatsCollection();
        this.state = State.CALLING;
        this.isInCall.postValue(true);
        this.videoCapturer = videoCapturer;
        this.videoSource = videoSource;
        this.surfaceTextureHelper = surfaceTextureHelper;
        acquireLock();
        if (callType == CallType.VIDEO) {
            isSpeakerPhoneOn = true;
            notifyOnSpeakerPhoneToggle();
        }

        if (Build.VERSION.SDK_INT >= 26) {
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

    @RequiresApi(api = 26)
    private void telecomPlaceCall() {
        TelecomManager tm = (TelecomManager) appContext.get().getSystemService(Context.TELECOM_SERVICE);
        if (tm != null) {
            Bundle extras = new Bundle();
            Contact contact = ContactsDb.getInstance().getContact(peerUid);
            if (contact.normalizedPhone == null) {
                finishStartCall();
                return;
            }
            Bundle innerExtras = new Bundle();
            innerExtras.putString(HaTelecomConnectionService.EXTRA_CALL_ID, callId);
            innerExtras.putString(HaTelecomConnectionService.EXTRA_PEER_UID, peerUid.rawId());
            innerExtras.putString(HaTelecomConnectionService.EXTRA_PEER_UID_NAME, contact.getDisplayName());
            innerExtras.putString(HaTelecomConnectionService.EXTRA_PEER_UID_PHONE, contact.getDisplayPhone());
            extras.putBundle(TelecomManager.EXTRA_OUTGOING_CALL_EXTRAS, innerExtras);
            extras.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, this.phoneAccountHandle);
            if (callType == CallType.VIDEO) {
                Log.i("CallManager: requesting speakerphone for video call");
                extras.putBoolean(TelecomManager.EXTRA_START_CALL_WITH_SPEAKERPHONE, true);
            }
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
        initializePeerConnections();
        if (isInitiator) {
            createAVTracks();
            startStreams();
            getCallServersAndStartCall();
        }
    }

    public synchronized void stop(EndCall.Reason reason) {
        final long callDuration = (this.callStartTimestamp > 0)? SystemClock.elapsedRealtime() - this.callStartTimestamp : 0;
        Log.i("CallManager: stop callId: " + callId + " peerUid" + peerUid + " reason: " + reason + " duration: " + callDuration / 1000);
        stopAudioManager();
        stopOutgoingRingtone();

        if (peerConnection != null) {
            peerConnection.getStats(report -> CallStats.sendEndCallEvent(callId, peerUid, callType, isInitiator, isAnswered, callDuration, reason, report));
            peerConnection.close();
            peerConnection.dispose();
            peerConnection = null;
        }

        if (videoCapturer != null) {
            try {
                videoCapturer.stopCapture();
            } catch (InterruptedException e) {
                Log.e("CallManager: videoCapturer.stopCapture exception ", e);
            }
            videoCapturer.dispose();
            videoCapturer = null;
        }

        if (audioSource != null) {
            audioSource.dispose();
            audioSource = null;
        }

        if (videoSource != null) {
            videoSource.dispose();
            videoSource = null;
        }

        if (surfaceTextureHelper != null) {
            surfaceTextureHelper.dispose();
            surfaceTextureHelper = null;
        }

        // these get disposed by the peerConnection.dispose()
        localAudioTrack = null;
        localVideoTrack = null;
        remoteVideoTrack = null;
        remoteAudioTrack = null;
        // those get cleaned up by the CallActivity
        localVideoSink = null;
        remoteVideoSink = null;

        if (rootEglBase != null) {
            //TODO(nikola): this could be a problem if the activity is still open? The same rootEglBase is used there
            rootEglBase.release();
            rootEglBase = null;
        }

        if (callService != null) {
            appContext.get().stopService(new Intent(appContext.get(), CallService.class));
            callService = null;
        }
        if (callDuration > 0) {
            storeCallLogMsg(peerUid, callId, callType, callDuration);
        }
        cancelRingingTimeout();
        cancelIceRestartTimer();
        cancelNoConnectionTimer();
        releaseLock();
        callStats.stopStatsCollection();
        isInitiator = false;
        isMicrophoneMuted = false;
        isSpeakerPhoneOn = false;
        callId = null;
        peerUid = null;
        clearCallTimer();
        callType = CallType.UNKNOWN_TYPE;
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

        this.isInitiator = false;
        this.peerUid = peerUid;
        this.callType = callType;
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

        if (Build.VERSION.SDK_INT >= 26) {
            telecomHandleIncomingCall();
        } else {
            showIncomingCallNotification();
        }
        processQueuedIceCandidates();
    }

    public void showIncomingCallNotification() {
        Log.i("CallManager: showIncomingCallNotification");
        if (callId == null) {
            Log.w("CallManager: showIncomingCallNotification(): callId is null. call was already canceled");
            return;
        }
        Notifications.getInstance(appContext.get()).showIncomingCallNotification(callId, peerUid, callType);
        callsApi.sendRinging(callId, peerUid);
        startRingingTimeoutTimer();
    }

    @RequiresApi(api = 23)
    public void telecomHandleIncomingCall() {
        Log.i("CallManager: telecomHandleIncomingCall");
        TelecomManager tm = (TelecomManager) appContext.get().getSystemService(Context.TELECOM_SERVICE);
        if (tm != null && phoneAccountHandle != null) {
            Bundle extras = new Bundle();
            Contact c = ContactsDb.getInstance().getContact(peerUid);
            extras.putString(HaTelecomConnectionService.EXTRA_CALL_ID, callId);
            extras.putString(HaTelecomConnectionService.EXTRA_PEER_UID, peerUid.rawId());
            extras.putString(HaTelecomConnectionService.EXTRA_PEER_UID_NAME, c.getDisplayName());
            extras.putString(HaTelecomConnectionService.EXTRA_PEER_UID_PHONE, c.getDisplayPhone());
            if (this.callType == CallType.VIDEO) {
                Log.i("CallManager: telecomHandleIncomingCall: requesting speakerphone ");
                extras.putBoolean(TelecomManager.EXTRA_START_CALL_WITH_SPEAKERPHONE, true);
            }
            Log.i("CallManager: TelecomManager.addNewIncomingCall");
            tm.addNewIncomingCall(phoneAccountHandle, extras);
        }
    }

    public void telecomSetActive() {
        if (telecomConnection != null && Build.VERSION.SDK_INT >= 23) {
            telecomConnection.setActive();
        }
    }

    public void telecomOnAnswer() {
        // TODO(nikola): we should check the callId and peerUID
        Intent acceptIntent = CallActivity.acceptCallIntent(appContext.get(), callId, peerUid, callType);
        acceptIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // TODO(nikola): this call should not be needed because there is not supposed to be any notification generated by us
        // the notification was displayed by the telecom system
        Notifications.getInstance(appContext.get()).clearIncomingCallNotification();

        appContext.get().startActivity(acceptIntent);
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
        Log.i("CallManager: call_ringing callId: " + callId + " peerUid: " + peerUid + " ts: " + timestamp);
        if (checkWrongCall(callId, "call_ringing")) {
            return;
        }
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
        Log.i("CallManager: answer_call callId: " + callId + " peerUid: " + peerUid + " " + timestamp);
        if (checkWrongCall(callId, "answer_call")) {
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
        if (isSpeakerPhoneOn) {
            setSpeakerPhoneOn(true);
        }
        notifyOnAnsweredCall();
        processQueuedIceCandidates();
    }

    public synchronized void handleEndCall(@NonNull String callId, @NonNull UserId peerUid,
                               @NonNull EndCall.Reason reason, @NonNull Long timestamp) {
        Log.i("got EndCall callId: " + callId + " peerUid: " + peerUid + " reason: " + reason.name() + " " + timestamp);
        if (reason == EndCall.Reason.CANCEL || reason == EndCall.Reason.TIMEOUT) {
            storeMissedCallMsg(peerUid, callId, callType, timestamp);
        }
        if (checkWrongCall(callId, "end_call")) {
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
        Log.i("CallManager: got ice_candidate callId: " + callId + " " + peerUid + "  sdp: " + sdp);
        if (this.callId != null && !this.callId.equals(callId)) {
            Log.i("CallManager: got IceCandidates for the wrong callId: " + callId + " peerUid: " + peerUid + " state: " + toString());
            return;
        }

        IceCandidate candidate = new IceCandidate(sdpMediaId, sdpMediaLineIndex, sdp);
        if (state == State.IN_CALL || state == State.IN_CALL_CONNECTING || state == State.INCOMING_RINGING) {
            peerConnection.addIceCandidate(candidate);
        } else {
            HaIceCandidate haIceCandidate = new HaIceCandidate(callId, candidate);
            iceCandidateQueue.offer(haIceCandidate);
        }
    }

    public void handleIceRestartOffer(@NonNull String callId, int restartIndex, @Nullable String webrtcRestartOffer,
                                      @Nullable CryptoException cryptoException) {
        Log.i("CallManager: got ice_restart_offer callId: " + callId);
        if (checkWrongCall(callId, "ice_restart_offer")) {
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
        Log.i("CallManager: got ice_restart_answer callId: " + callId);
        if (checkWrongCall(callId, "ice_restart_answer")) {
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
    public synchronized boolean acceptCall(@Nullable VideoCapturer videoCapturer, @Nullable VideoSource videoSource, @Nullable SurfaceTextureHelper surfaceTextureHelper) {
        if (this.isInitiator) {
            Log.e("ERROR user clicked accept call but is the call initiator callId: " + callId);
            return false;
        }
        if (this.state != State.INCOMING_RINGING) {
            Log.w("CallManager.acceptCall call is not in INCOMING_RINGING state. State: " + stateToString(state));
            return false;
        }
        this.videoCapturer = videoCapturer;
        this.videoSource = videoSource;
        this.surfaceTextureHelper = surfaceTextureHelper;
        createAVTracks();
        startStreams();

        cancelRingingTimeout();
        doAnswer();
        this.state = State.IN_CALL_CONNECTING;
        this.isAnswered = true;
        this.callAnswerTimestamp = SystemClock.elapsedRealtime();
        notifyOnAnsweredCall();
        telecomSetActive();
        if (this.callType == CallType.VIDEO) {
            Log.i("CallManager: acceptCall: setting speakerphone on");
            setSpeakerPhoneOn(true);
        }
        return true;
    }

    private boolean checkWrongCall(@NonNull String callId, @NonNull String msgType) {
        if (this.callId == null || !this.callId.equals(callId)) {
            Log.i("CallManager: got " + msgType + " for the wrong callId: " + callId + " state: " + toString());
            return true;
        }
        return false;
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
        final VideoEncoderFactory encoderFactory = new DefaultVideoEncoderFactory(getEglBase().getEglBaseContext(), true, true);
        final VideoDecoderFactory decoderFactory = new DefaultVideoDecoderFactory(getEglBase().getEglBaseContext());
        PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions.builder(appContext.get())
                        .setEnableInternalTracer(true)
                        .createInitializationOptions());
        PeerConnectionFactory.Builder builder = PeerConnectionFactory.builder();
        builder.setVideoEncoderFactory(encoderFactory);
        builder.setVideoDecoderFactory(decoderFactory);
        builder.setOptions(null);
        factory = builder.createPeerConnectionFactory();
    }

    private void createAVTracks() {
        audioConstraints = new MediaConstraints();
        audioSource = factory.createAudioSource(audioConstraints);
        localAudioTrack = factory.createAudioTrack(AUDIO_TRACK_ID, audioSource);

        if (callType == CallType.VIDEO) {
            localVideoTrack = createVideoTrack();
        }
    }


    private VideoTrack createVideoTrack() {
        videoCapturer.startCapture(VIDEO_WIDTH, VIDEO_HEIGHT, VIDEO_FPS);

        VideoTrack track = factory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
        track.setEnabled(true);
        if (localVideoSink != null) {
            track.addSink(localVideoSink);
        }
        return track;
    }

    public VideoSource createVideoSource(@NonNull VideoCapturer videoCapturer) {
        return factory.createVideoSource(videoCapturer.isScreencast());
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
        mediaStream.addTrack(localAudioTrack);
        if (callType == CallType.VIDEO) {
            mediaStream.addTrack(localVideoTrack);
        }
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
                    startNoConnectionEndCallTimer();
                } else if (iceConnectionState == PeerConnection.IceConnectionState.CONNECTED) {
                    cancelNoConnectionTimer();
                    cancelIceRestartTimer();
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

                if (mediaStream.audioTracks.size() >= 1) {
                    remoteAudioTrack = mediaStream.audioTracks.get(0);
                    remoteAudioTrack.setEnabled(true);
                }
                if (mediaStream.audioTracks.size() != 1) {
                    Log.e("CallManager: onAddStream: Unexpected number of audio tracks " + mediaStream.audioTracks);
                }

                if (callType == CallType.VIDEO) {
                    if (mediaStream.videoTracks.size() >= 1) {
                        remoteVideoTrack = mediaStream.videoTracks.get(0);
                        remoteVideoTrack.setEnabled(true);
                        if (remoteVideoSink != null) {
                            remoteVideoTrack.addSink(remoteVideoSink);
                        }
                        // TODO(nikola): what is the difference between addSink and addRender?
                        //remoteVideoTrack.addRenderer(new VideoRenderer(binding.surfaceView2));
                    }
                    if (mediaStream.videoTracks.size() != 1) {
                        Log.e("CallManager: onAddStream: Unexpected number of video tracks " + mediaStream.videoTracks);
                    }
                }
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
        Observable<GetCallServersResponseIq> observable = callsApi.getCallServers(callId, peerUid, callType);
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
                Log.sendErrorReport("CallManager: got 0 call servers");
                endCall(EndCall.Reason.SYSTEM_ERROR, false);
            }
        }).onError(e -> {
            Log.e("CallManager: Failed to start call, did not get ice servers", e);
            Log.sendErrorReport("CallManager: Failed to getCallServer: " + e.getMessage());
            endCall(EndCall.Reason.SYSTEM_ERROR, false);
            // TODO(nikola): We should retry the IQs for 60 secs. Until we implement this calling stop is better
        });
    }

    private void doStartCall() {
        MediaConstraints sdpMediaConstraints = new MediaConstraints();

        sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        if (callType == CallType.VIDEO) {
            sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        }

        peerConnection.createOffer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(@NonNull SessionDescription sessionDescription) {
                Log.i("CallManager: createOffer.onCreateSuccess: ");
                peerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);

                try {
                    Observable<StartCallResponseIq> observable = callsApi.startCall(
                            callId, peerUid, callType, sessionDescription.description);

                    // TODO(nikola): we are blocking the webrtc thread here... Do this non-blocking
                    StartCallResponseIq response = observable.await();
                    Log.i("received StartCallResult " + response.result +
                            " turn " + response.turnServers +
                            " stun " + response.stunServers +
                            " ts " + response.timestamp);
                    if (response.result == StartCallResult.Result.OK) {
                        startRingingTimeoutTimer();
                    } else {
                        Log.e("CallManager: StartCall failed " + response.result);
                        Log.sendErrorReport("CallManager: startCall result is: " + response.result);
                        endCall(EndCall.Reason.SYSTEM_ERROR, false);
                    }
                } catch (CryptoException e) {
                    Log.e("CallManager: CryptoException, Failed to send the start call IQ callId: " + callId + " peerUid: " + peerUid, e);
                    Log.sendErrorReport("CallManager: failed to startCall CryptoException");
                    endCall(EndCall.Reason.SYSTEM_ERROR, false);
                } catch (InterruptedException | ObservableErrorException e) {
                    Log.e("CallManager: Failed to send the start call IQ callId: " + callId + " peerUid: " + peerUid, e);
                    Log.sendErrorReport("CallManager: failed to startCall");
                    endCall(EndCall.Reason.SYSTEM_ERROR, false);
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
        endCall(reason, true);
    }

    public void endCall(EndCall.Reason reason, boolean sendEndCall) {
        if (callId == null) {
            return;
        }
        Log.i("CallManager.endCall callId: " + callId + " reason: " + reason);
        if (sendEndCall) {
            callsApi.sendEndCall(callId, peerUid, reason);
        }
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

    public boolean getIsInitiator() {
        return isInitiator;
    }

    public CallType getCallType() {
        return callType;
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
                    storeMissedCallMsg(this.peerUid, this.callId, callType);
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
                Log.i("CallManager: canceling ringingTimeoutTimerTask");
                ringingTimeoutTimerTask.cancel();
                ringingTimeoutTimerTask = null;
            }
        }
    }

    private void cancelNoConnectionTimer() {
        synchronized (timer) {
            if (noConnectionTimerTask != null) {
                Log.i("CallManager: canceling noConnectionTimerTask");
                noConnectionTimerTask.cancel();
                noConnectionTimerTask = null;
            }
        }
    }

    private void cancelIceRestartTimer() {
        synchronized (timer) {
            if (iceRestartTimerTask != null) {
                Log.i("CallManager: canceling iceRestartTimerTask");
                iceRestartTimerTask.cancel();
                iceRestartTimerTask = null;
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

    private void startNoConnectionEndCallTimer() {
        synchronized (timer) {
            if (noConnectionTimerTask != null) {
                Log.i("CallManager: another noConnectionTimerTask already exists");
                return;
            }
            noConnectionTimerTask = new TimerTask() {
                @Override
                public void run() {
                    Log.i("CallManager: IceConnection has been down for too long. Ending the call");
                    endCall(EndCall.Reason.CONNECTION_ERROR, true);
                }
            };
            Log.i("CallManager: start noConnection Timer");
            timer.schedule(noConnectionTimerTask, Constants.CALL_NO_CONNECTION_TIMEOUT_MS);
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
            if (callType == CallType.VIDEO) {
                audioManager.setDefaultAudioDevice(CallAudioManager.AudioDevice.SPEAKER_PHONE);
            } else {
                audioManager.setDefaultAudioDevice(CallAudioManager.AudioDevice.EARPIECE);
            }
        }
    }

    private void stopAudioManager() {
        Log.i("CallManager: stoping CallAudioManager");
        if (Build.VERSION.SDK_INT < 26) {
            mainHandler.post(audioManager::stop);
        }
    }

    private void storeCallLogMsg(@NonNull UserId userId, @NonNull String callId, @NonNull CallType callType, long callDuration) {
        int msgType = CallMessage.Usage.LOGGED_VOICE_CALL;
        if (callType == CallType.VIDEO) {
            msgType = CallMessage.Usage.LOGGED_VIDEO_CALL;
        }
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
        Log.i("CallManager: storeMissedCallMsg callId: " + callId + " userId: " + userId + " " + callType);
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

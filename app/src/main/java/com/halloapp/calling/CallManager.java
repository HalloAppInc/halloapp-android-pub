package com.halloapp.calling;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;

import androidx.annotation.IntDef;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.halloapp.AppContext;
import com.halloapp.Constants;
import com.halloapp.Notifications;
import com.halloapp.crypto.CryptoException;
import com.halloapp.id.UserId;
import com.halloapp.proto.server.CallType;
import com.halloapp.proto.server.EndCall;
import com.halloapp.proto.server.StartCallResult;
import com.halloapp.proto.server.StunServer;
import com.halloapp.proto.server.TurnServer;
import com.halloapp.proto.server.WebRtcSessionDescription;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.calls.CallsApi;
import com.halloapp.xmpp.calls.GetCallServersResponseIq;
import com.halloapp.xmpp.calls.StartCallResponseIq;
import com.halloapp.xmpp.util.Observable;
import com.halloapp.xmpp.util.ObservableErrorException;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CallManager {

    @IntDef({State.IDLE, State.CALLING, State.IN_CALL, State.RINGING, State.END})
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {
        int IDLE = 0;
        int CALLING = 1;
        int IN_CALL = 2;
        int RINGING = 3;
        int END = 4;
    }

    private @State int state;
    private boolean isInitiator;

    private boolean isMicrophoneMuted = false;
    private boolean isSpeakerPhoneOn = false;  // The default will have to change for video calls

    MediaConstraints audioConstraints;
    AudioSource audioSource;
    AudioTrack localAudioTrack;

    private PeerConnection peerConnection;
    private PeerConnectionFactory factory;

    private final CallsApi callsApi;
    private final CallAudioManager audioManager;
    private final OutgoingRingtone outgoingRingtone;

    private String callId;
    private UserId peerUid;

    private ComponentName callService;
    @Nullable
    private PowerManager.WakeLock proximityLock;

    @NonNull
    private final Timer timer = new Timer();

    @Nullable
    private TimerTask ringingTimeoutTimerTask;

    // Executor thread is started once in private ctor and is used for all
    // peer connection API calls to ensure new peer connection factory is
    // created on the same thread as previously destroyed factory.
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private static CallManager instance;
    private final Set<CallObserver> observers;
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
        this.appContext = AppContext.getInstance();
        this.callsApi = CallsApi.getInstance();
        this.outgoingRingtone = new OutgoingRingtone(appContext.get());
        this.proximityLock = createProximityLock();
        this.state = State.IDLE;

        this.audioManager = CallAudioManager.create(appContext.get());
        this.observers = new HashSet<>();
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

    @MainThread
    public void startCall(@NonNull UserId peerUid) {
        Log.i("startCall");
        this.callId = RandomId.create();
        this.peerUid = peerUid;
        this.isInitiator = true;
        this.callService = startCallService();
        acquireLock();
        this.state = State.CALLING;

        startAudioManager();

        executor.execute(this::setupWebrtc);
    }

    private ComponentName startCallService() {
        Log.i("startCallService");
        Intent serviceIntent = CallService.getIntent(peerUid);
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
    
    public void stop() {
        Log.i("stop callId: " + callId + " peerUid" + peerUid);
        stopAudioManager();
        stopOutgoingRingtone();
        if (localAudioTrack != null) {
            localAudioTrack.setEnabled(false);
            localAudioTrack = null;
        }
        if (peerConnection != null) {
            peerConnection.close();
            peerConnection = null;
        }
        if (callService != null) {
            appContext.get().stopService(new Intent(appContext.get(), CallService.class));
            callService = null;
        }
        cancelRingingTimeout();
        releaseLock();
        isInitiator = false;
        isMicrophoneMuted = false;
        isSpeakerPhoneOn = false;
        callId = null;
        peerUid = null;
        state = State.IDLE;
    }

    public void handleIncomingCall(@NonNull String callId, @NonNull UserId peerUid, @Nullable String webrtcOffer,
                                    @NonNull List<StunServer> stunServers, @NonNull List<TurnServer> turnServers,
                                    @NonNull Long timestamp) {
        Log.i("incoming call " + callId + " peerUid: " + peerUid);
        this.isInitiator = false;
        this.peerUid = peerUid;
        this.callId = callId;

        if (webrtcOffer == null) {
            Log.e("handleIncomingCall() Failed to decrypt webrtcOffer callId:" + callId);
            endCall(EndCall.Reason.REJECT);
            notifyOnEndCall();
            stop();
            return;
        }

        setupWebrtc();
        Log.i("Setting webrtc offer " + webrtcOffer);
        peerConnection.setRemoteDescription(
                new SimpleSdpObserver(),
                new SessionDescription(SessionDescription.Type.OFFER, webrtcOffer));
        setStunTurnServers(stunServers, turnServers);

        this.state = State.RINGING;
        notifyOnIncomingCall();
        Notifications.getInstance(appContext.get()).showIncomingCallNotification(callId, peerUid);
        callsApi.sendRinging(callId, peerUid);
        startRingingTimeoutTimer();
    }

    public void handleCallRinging(@NonNull String callId, @NonNull UserId peerUid,@NonNull Long timestamp) {
        Log.i("CallRinging callId: " + callId + " peerUid: " + peerUid + " ts: " + timestamp);
        if (this.callId == null || !this.callId.equals(callId) ) {
            Log.e("Error: got call ringing message for call " + callId +
                    " but my call id is " + this.callId);
            return;
        }
        // TODO(nikola): check the peerUid
        if (!this.isInitiator) {
            Log.e("Error: unexpected call ringing, not initiator");
            return;
        }
        notifyOnPeerIsRinging();
        startOutgoingRingtone();
    }

    public void handleAnswerCall(@NonNull String callId, @NonNull UserId peerUid, @Nullable String webrtcOffer, @NonNull Long timestamp) {
        Log.i("AnswerCall callId: " + callId + " peerUid: " + peerUid);

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
            // TODO(nikola): fix this reasons. Maybe add a new reason for e2e errors
            endCall(EndCall.Reason.REJECT);
            notifyOnEndCall();
            stop();
            return;
        }

        peerConnection.setRemoteDescription(new SimpleSdpObserver(), new SessionDescription(SessionDescription.Type.ANSWER, webrtcOffer));
        this.state = State.IN_CALL;
        notifyOnAnsweredCall();
    }

    public void handleEndCall(@NonNull String callId, @NonNull UserId peerUid,
                               @NonNull EndCall.Reason reason, @NonNull Long timestamp) {
        Log.i("got EndCall callId: " + callId + " peerUid: " + peerUid + " reason: " + reason.name());
        this.state = State.END;
        notifyOnEndCall();
        stopOutgoingRingtone();
        // TODO(nikola): Handle multiple calls at the same time. We should only cancel the right
        // notification
        Notifications.getInstance(appContext.get()).clearIncomingCallNotification();
    }

    public void handleIceCandidate(@NonNull String callId, @NonNull UserId peerUid,
                                    @NonNull String sdpMediaId, int sdpMediaLineIndex, @NonNull String sdp) {
        Log.i("CallManager: got IceCandidate callId: " + callId + " " + sdpMediaId + ":" + sdpMediaLineIndex + ": sdp: " + sdp);
        IceCandidate candidate = new IceCandidate(sdpMediaId, sdpMediaLineIndex, sdp);
        // TODO(nikola): more checks for callId and peerUid
        if (peerConnection != null) {
            peerConnection.addIceCandidate(candidate);
        }
    }

    @MainThread
    public void acceptCall() {
        if (this.isInitiator) {
            Log.e("ERROR user clicked accept call but is the call initiator callId: " + callId);
            return;
        }

        cancelRingingTimeout();
        doAnswer();
        this.state = State.IN_CALL;
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
                Log.i("onSignalingChange: ");
            }

            @Override
            public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
                Log.i("onIceConnectionChange: " + iceConnectionState);
            }

            @Override
            public void onIceConnectionReceivingChange(boolean b) {
                Log.i("onIceConnectionReceivingChange: ");
            }

            @Override
            public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
                Log.i("onIceGatheringChange: ");
            }

            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                Log.i("onIceCandidate: " + iceCandidate);
                callsApi.sendIceCandidate(callId, peerUid, iceCandidate);
            }

            @Override
            public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
                Log.i("onIceCandidatesRemoved: ");
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                Log.i("onAddStream: " + mediaStream.audioTracks.size() + " "
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
                Log.i("onRemoveStream: ");
            }

            @Override
            public void onDataChannel(DataChannel dataChannel) {
                Log.i("onDataChannel: ");
            }

            @Override
            public void onRenegotiationNeeded() {
                Log.i("onRenegotiationNeeded: ");
            }
        };

        return factory.createPeerConnection(rtcConfig, pcObserver);
    }

    private void getCallServersAndStartCall() {
        Observable<GetCallServersResponseIq> observable = callsApi.getCallServers(callId, peerUid, CallType.AUDIO);
        observable.onResponse(response -> {
            Log.i("CallManager: got call servers");
            setStunTurnServers(response.stunServers, response.turnServers);
            doStartCall();
        }).onError(e -> {
            Log.e("Failed to start call, did not get ice servers", e);
            // TODO: Should we call stop?
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
                        setStunTurnServers(response.stunServers, response.turnServers);
                        startRingingTimeoutTimer();
                    } else {
                        Log.w("StartCall failed " + response.result);
                        // TODO(nikola): handle call not ok
                    }
                    // TODO(nikola): handle the exceptions. Call stop()
                } catch (ObservableErrorException e) {
                    Log.e("CallManager: Failed to send the start call IQ callId: " + callId + " peerUid: " + peerUid, e);
                } catch (CryptoException e) {
                    Log.e("CallManager: CryptoException, Failed to send the start call IQ callId: " + callId + " peerUid: " + peerUid, e);
                } catch (InterruptedException e) {
                    Log.e("CallManager: Failed to send the start call IQ callId: " + callId + " peerUid: " + peerUid, e);
                }
            }
        }, sdpMediaConstraints);
    }

    private void setStunTurnServers(@NonNull List<StunServer> stunServers, @NonNull List<TurnServer> turnServers) {
        if (peerConnection == null) {
            Log.e("peerConnection is null");
            return;
        }

        // insert the stun and turn servers and update the peerConnection configuration.
        // stun/turn servers URLS look like this "stun:stun.l.google.com:19302";
        ArrayList<PeerConnection.IceServer> iceServers = new ArrayList<>();
        for (StunServer stunServer : stunServers) {
            String stunUrl = "stun:" + stunServer.getHost() + ":" + stunServer.getPort();
            iceServers.add(PeerConnection.IceServer.builder(stunUrl).createIceServer());
        }

        for (TurnServer turnServer : turnServers) {
            String turnUrl = "turn:" + turnServer.getHost() + ":" + turnServer.getPort();
            iceServers.add(PeerConnection.IceServer.builder(turnUrl)
                    .setUsername(turnServer.getUsername())
                    .setPassword(turnServer.getPassword())
                    .createIceServer());
        }
        Log.i("CallManager: iceservers: " + iceServers);

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

        peerConnection.createAnswer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(@NonNull SessionDescription sessionDescription) {
                // TODO(nikola): don't print this in the logs.
                Log.i("PeerConnection answer is ready " + sessionDescription);
                peerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);

                try {
                    WebRtcSessionDescription answer = CallsApi.encryptCallPayload(sessionDescription.description, peerUid);
                    Log.i("CallManager: encrypted answer: size:" +  answer.getEncPayload().size());
                    callsApi.sendAnswerCall(callId, peerUid, answer);
                } catch (CryptoException e) {
                    Log.e("CallManager: failed to encrypt webrtc Answer", e);
                    // TODO(nikola): change reason to CRYPTO_FAIL
                    endCall(EndCall.Reason.REJECT);
                    notifyOnEndCall();
                    stop();
                }
            }
        }, new MediaConstraints());
    }

    public boolean isMicrophoneMuted() {
        return isMicrophoneMuted;
    }

    public void setMicrophoneMute(boolean mute) {
        Log.i("CallManager.setMicrophoneMute(" + mute + ") was: " + this.isMicrophoneMuted);
        localAudioTrack.setEnabled(!mute);
        audioManager.setMicrophoneMute(mute);
        isMicrophoneMuted = mute;
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
        if (on) {
            audioManager.setDefaultAudioDevice(CallAudioManager.AudioDevice.SPEAKER_PHONE);
        } else {
            audioManager.setDefaultAudioDevice(CallAudioManager.AudioDevice.EARPIECE);
        }
        isSpeakerPhoneOn = on;
        notifyOnSpeakerPhoneToggle();
    }

    public void toggleSpeakerPhone() {
        setSpeakerPhoneOn(!isSpeakerPhoneOn());
    }

    public void endCall(@NonNull EndCall.Reason reason) {
        callsApi.sendEndCall(callId, peerUid, reason);
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

    private void startRingingTimeoutTimer() {
        synchronized (timer) {
            // TODO(nikola): maybe this should be always called on the executor?
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
            Log.i("onOutgoingCallTimeout");
            if (this.callId != null && this.callId.equals(callId)) {
                endCall(EndCall.Reason.TIMEOUT);
                notifyOnEndCall();
                // TODO(nikola): this could clear the wrong notification if we have multiple incoming calls.
                Notifications.getInstance(appContext.get()).clearIncomingCallNotification();
                // TODO(nikola): Cleanup the code path of who is calling the stop. Make stop private.
                // It is sometimes called from the UI and sometimes from here.
                //stop();
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
        executor.execute(() -> outgoingRingtone.start(OutgoingRingtone.Type.RINGING));
    }

    private void stopOutgoingRingtone() {
        executor.execute(outgoingRingtone::stop);
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
            case State.RINGING:
                return "ringing";
            case State.IN_CALL:
                return "in-call";
            case State.END:
                return "end";
            default:
                return "unknown";
        }
    }

    public String toString() {
        return "CallManager{state=" + stateToString(this.state) + ",callId=" + this.callId + ",peerUid=" + peerUid + "}";
    }

    @MainThread
    private void startAudioManager() {
        // Store existing audio settings and change audio mode to
        // MODE_IN_COMMUNICATION for best possible VoIP performance.
        Log.i("Starting the audio manager " + audioManager);
        audioManager.start((audioDevice, availableAudioDevices) -> {
            Log.i("onAudioManagerDevicesChanged: " + availableAudioDevices + ", " + "selected: " + audioDevice);
        });
    }

    private void stopAudioManager() {
        mainHandler.post(() -> audioManager.stop());
    }
}

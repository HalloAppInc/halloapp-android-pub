package com.halloapp.calling;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.google.firebase.crashlytics.internal.model.CrashlyticsReport;
import com.halloapp.AppContext;
import com.halloapp.ConnectionObservers;
import com.halloapp.Notifications;
import com.halloapp.id.UserId;
import com.halloapp.proto.server.CallType;
import com.halloapp.proto.server.EndCall;
import com.halloapp.proto.server.IceCandicate;
import com.halloapp.proto.server.Msg;
import com.halloapp.proto.server.Packet;
import com.halloapp.proto.server.StartCallResult;
import com.halloapp.proto.server.StunServer;
import com.halloapp.proto.server.TurnServer;
import com.halloapp.ui.calling.CallViewModel;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.ConnectionImpl;
import com.halloapp.xmpp.SeenReceiptElement;
import com.halloapp.xmpp.calls.AnswerCallElement;
import com.halloapp.xmpp.calls.CallsApi;
import com.halloapp.xmpp.calls.EndCallElement;
import com.halloapp.xmpp.calls.IceCandidateElement;
import com.halloapp.xmpp.calls.StartCallResponseIq;
import com.halloapp.xmpp.util.Observable;
import com.halloapp.xmpp.util.ObservableErrorException;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.socket.client.IO;
import io.socket.client.Socket;

public class CallManager {

    private Socket socket;
    private boolean isInitiator;
    private boolean isStarted;

    MediaConstraints audioConstraints;
    AudioSource audioSource;
    AudioTrack localAudioTrack;

    private PeerConnection peerConnection;
    private PeerConnectionFactory factory;

    private CallViewModel callViewModel;
    private Context context;
    private final CallsApi callsApi;

    private String callId;
    private UserId peerUid;

    private ComponentName callService;

    // Executor thread is started once in private ctor and is used for all
    // peer connection API calls to ensure new peer connection factory is
    // created on the same thread as previously destroyed factory.
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    private static CallManager instance;

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
        this.context = AppContext.getInstance().get();
        this.callsApi = CallsApi.getInstance();

        // adding listeners for incoming messages
        ConnectionObservers.getInstance().addObserver(new Connection.Observer() {
            @Override
            public void onIncomingCall(
                    @NonNull String callId, @NonNull UserId peerUid, @NonNull String webrtcOffer,
                    List<StunServer> stunServers, List<TurnServer> turnServers, Long timestamp, String ackId) {
                CallManager.getInstance().handleIncomingCall(
                        callId, peerUid, webrtcOffer, stunServers, turnServers, timestamp);
                Connection.getInstance().sendAck(ackId);
            }

            @Override
            public void onCallRinging(@NonNull String callId, @NonNull UserId peerUid, Long timestamp, String ackId) {
                CallManager.getInstance().handleCallRinging(callId, peerUid, timestamp);
                Connection.getInstance().sendAck(ackId);
            }

            @Override
            public void onAnswerCall(@NonNull String callId, @NonNull UserId peerUid, @NonNull String webrtcOffer,
                                     Long timestamp, String ackId) {
                CallManager.getInstance().handleAnswerCall(callId, peerUid, webrtcOffer, timestamp);
                Connection.getInstance().sendAck(ackId);
            }

            @Override
            public void onEndCall(@NonNull String callId, @NonNull UserId peerUid, @NonNull EndCall.Reason reason,
                                  Long timestamp, String ackId) {
                CallManager.getInstance().handleEndCall(callId, peerUid, reason, timestamp);
                Connection.getInstance().sendAck(ackId);
            }

            @Override
            public void onIceCandidate(@NonNull String callId, @NonNull UserId peerUid, @NonNull String sdpMediaId,
                                       int sdpMediaLineIndex, @NonNull String sdp, String ackId) {
                CallManager.getInstance().handleIceCandidate(callId, peerUid, sdpMediaId, sdpMediaLineIndex, sdp);
                Connection.getInstance().sendAck(ackId);
            }
        });

    }

    // TODO(nikola): Don't take the context as argument here use AppContext
    // TODO(nikola): Don't pass the callViewModel. Have the callViewModel subscribe on events
    public void startCall(CallViewModel callViewModel, UserId peerUid) {
        Log.i("startCall");
        this.callViewModel = callViewModel;
        this.callId = RandomId.create();
        this.peerUid = peerUid;
        this.isInitiator = true;
        this.callService = startCallService();


        // TODO(nikola): How to do this better, I need to execute some code on the background thread
        // that loads the webrtc library. We can load this on app start also.
        // After that I need to start the actual call doCall()
        executor.execute(() -> {
            setup();
        });
    }

    private ComponentName startCallService() {
        Log.i("startCallService");
        Intent serviceIntent = new Intent(context, CallService.class);
        if (Build.VERSION.SDK_INT >= 26) {
            return context.startForegroundService(serviceIntent);
        } else {
            return context.startService(serviceIntent);
        }
    }

    @WorkerThread
    private void setup() {
        // TODO(nikola): assert this is called not on the UI thread
        Log.d("Initialize WebRTC");
        initializePeerConnectionFactory();
        createAVTracks();
        initializePeerConnections();
        startStreams();
        if (isInitiator) {
            doCall();
        }
    }

    public void stop() {
        Log.i("stop callId: " + callId + " peerUid" + peerUid);
        if (localAudioTrack != null) {
            localAudioTrack.setEnabled(false);
            localAudioTrack = null;
        }
        if (peerConnection != null) {
            peerConnection.close();
            peerConnection = null;
        }
        if (callService != null) {
            context.stopService(new Intent(context, CallService.class));
            callService = null;
        }
        isInitiator = false;
        isStarted = false;
        callId = null;
        peerUid = null;
        callViewModel = null;
    }

    // TODO(nikola): temporary code until we do the model to subscribe to the CallManager for events
    // right now the CallManager calls functions in the model to change the UI
    public void setCallViewModel(CallViewModel callViewModel) {
        this.callViewModel = callViewModel;
    }

    private void handleIncomingCall(String callId, UserId peerUid, String webrtcOffer,
                                    List<StunServer> stunServers, List<TurnServer> turnServers, Long timestamp) {
        Log.i("incoming call " + callId + " peerUid: " + peerUid);
        this.isInitiator = false;
        this.peerUid = peerUid;
        this.callId = callId;
        // TODO(nikola): rename setup to something better
        setup();
        Log.i("Setting webrtc offer " + webrtcOffer);
        peerConnection.setRemoteDescription(
                new SimpleSdpObserver(),
                new SessionDescription(SessionDescription.Type.OFFER, webrtcOffer));
        setStunTurnServers(stunServers, turnServers);
        // if we have reference to the callViewModel the activity is already displayed.
        // TODO(nikola): always display the notification. The CallActivity should
        // not be displayed
        if (callViewModel != null) {
            callViewModel.onIncomingCall();
        } else {
            Notifications.getInstance(context).showIncomingCallNotification(callId, peerUid);
        }
    }

    private void handleCallRinging(@NonNull String callId, @NonNull UserId peerUid, Long timestamp) {
        Log.i("CallRinging callId: " + callId + " peerUid: " + peerUid + " ts: " + timestamp);
        if (this.callId == null && !this.callId.equals(callId) ) {
            Log.e("Error: got call ringing message for call " + callId +
                    " but my call id is " + this.callId);
            return;
        }
        // TODO(nikola): check the peerUid
        if (this.isInitiator == false) {
            Log.e("Error: unexpected call ringing, not initiator");
            return;
        }
        callViewModel.onPeerIsRinging();
    }

    private void handleAnswerCall(@NonNull String callId, @NonNull UserId peerUid,
                                  @NonNull String webrtcOffer, Long timestamp) {
        Log.i("AnswerCall callId: " + callId);

        peerConnection.setRemoteDescription(
                new SimpleSdpObserver(),
                new SessionDescription(SessionDescription.Type.ANSWER,
                        webrtcOffer));
        callViewModel.onAnswerCall();
    }

    private void handleEndCall(@NonNull String callId, @NonNull UserId peerUid,
                               @NonNull EndCall.Reason reason, Long timestamp) {
        Log.i("got EndCall callId: " + callId + " peerUid: " + peerUid + " reason: " + reason.name());
        if (callViewModel != null) {
            callViewModel.onEndCall();
        }
        // TODO(nikola): Handle multiple calls at the same time. We should only cancel the right
        // notification
        Notifications.getInstance(context).clearIncomingCallNotification();
    }

    private void handleIceCandidate(@NonNull String callId, @NonNull UserId peerUid,
                                    @NonNull String sdpMediaId, int sdpMediaLineIndex, @NonNull String sdp) {
        Log.i("got IceCandidate callId: " + callId + " sdp: " + sdp);
        IceCandidate candidate = new IceCandidate(
                sdpMediaId,
                sdpMediaLineIndex,
                sdp);
        // TODO(nikola): more checks for callId and peerUid
        if (peerConnection != null) {
            peerConnection.addIceCandidate(candidate);
        }
    }

    public void onAcceptCall() {
        if (this.isInitiator) {
            Log.e("ERROR user clicked accept call but is the call initiator callId: " + callId);
            return;
        }

        doAnswer();
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
                PeerConnectionFactory.InitializationOptions.builder(context)
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


    private PeerConnection createPeerConnection(PeerConnectionFactory factory) {
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

                IceCandidateElement iceCandidateElement = new IceCandidateElement(
                        callId, iceCandidate.sdpMid, iceCandidate.sdpMLineIndex, iceCandidate.sdp);
                String id = RandomId.create();
                Msg msg = Msg.newBuilder()
                        .setId(id)
                        .setIceCandidate(iceCandidateElement.toProto())
                        .setToUid(peerUid.rawIdLong())
                        .build();

                // TODO(nikola): Ask Android team about sending the messages like this.
                Connection.getInstance().sendCallMsg(msg);
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

    // TODO(nikola): rename to startCall
    private void doCall() {
        MediaConstraints sdpMediaConstraints = new MediaConstraints();

        sdpMediaConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        // TODO(nikola): add for video calls
//        sdpMediaConstraints.mandatory.add(
//                new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));

        peerConnection.createOffer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                Log.i("onCreateSuccess: ");
                peerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);

                Observable<StartCallResponseIq> observable = callsApi.startCall(
                        callId, peerUid, CallType.AUDIO, sessionDescription.description);
                try {
                    StartCallResponseIq response = observable.await();
                    Log.i("received StartCallResult " + response.result +
                            " turn " + response.turnServers +
                            " stun " + response.stunServers +
                            " ts " + response.timestamp);
                    if (response.result == StartCallResult.Result.OK) {
                        setStunTurnServers(response.stunServers, response.turnServers);
                    } else {
                        Log.w("StartCall failed " + response.result);
                        // TODO(nikola): handle call not ok
                    }
                    // TODO(nikola): handle the exceptions
                } catch (ObservableErrorException e) {
                    Log.e("Failed to send the start call IQ callId: " + callId + " peerUid: " + peerUid);
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    Log.e("Failed to send the start call IQ callId: " + callId + " peerUid: " + peerUid);
                    e.printStackTrace();
                }
            }
        }, sdpMediaConstraints);
    }

    private void setStunTurnServers(List<StunServer> stunServers, List<TurnServer> turnServers) {
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
        Log.i("ice servers: " + iceServers);

        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
        peerConnection.setConfiguration(rtcConfig);
    }

    private void doAnswer() {
        Log.i("Answering callId: " + callId + " peerUid: " + peerUid);
        if (this.callService == null) {
            this.callService = startCallService();
        }
        peerConnection.createAnswer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                // TODO(nikola): don't print this in the logs.
                Log.i("PeerConnection answer is ready " + sessionDescription);
                peerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);

                AnswerCallElement answerCallElement = new AnswerCallElement(
                        callId, sessionDescription.description);
                String id = RandomId.create();
                Msg msg = Msg.newBuilder()
                        .setId(id)
                        .setAnswerCall(answerCallElement.toProto())
                        .setToUid(peerUid.rawIdLong())
                        .build();
                Log.i("trying to send call_answer msg " + msg);
                ConnectionImpl.getInstance().sendCallMsg(msg);
            }
        }, new MediaConstraints());
    }

    public void onMute(boolean mute) {
        // TODO(nikola): Use the audioManager here
        localAudioTrack.setEnabled(!mute);
    }

    public void onEndCall(EndCall.Reason reason) {
        EndCallElement endCallElement = new EndCallElement(callId, reason);
        String id = RandomId.create();
        Msg msg = Msg.newBuilder()
                .setId(id)
                .setEndCall(endCallElement.toProto())
                .setToUid(peerUid.rawIdLong())
                .build();
        Log.i("trying to send end_call msg " + msg);
        ConnectionImpl.getInstance().sendCallMsg(msg);
    }
}

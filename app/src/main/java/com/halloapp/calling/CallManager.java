package com.halloapp.calling;

import android.content.Context;

import com.halloapp.ui.calling.CallViewModel;
import com.halloapp.util.logs.Log;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.socket.client.IO;
import io.socket.client.Socket;

public class CallManager {

    private Socket socket;
    private boolean isInitiator;
    private boolean isChannelReady;
    private boolean isStarted;

    MediaConstraints audioConstraints;
    AudioSource audioSource;
    AudioTrack localAudioTrack;

    private PeerConnection peerConnection;
    private PeerConnectionFactory factory;

    private CallViewModel callViewModel;
    private Context context;

    // Executor thread is started once in private ctor and is used for all
    // peer connection API calls to ensure new peer connection factory is
    // created on the same thread as previously destroyed factory.
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    // TODO(nikola): Don't pass the callViewModel. Have the callViewModel subscribe on events.
    public CallManager(CallViewModel callViewModel, Context context) {
        this.callViewModel = callViewModel;
        this.context = context;

        connectToSignallingServer();

        //initializePeerConnectionFactory();

        executor.execute(() -> {
            Log.d("Initialize WebRTC");
            initializePeerConnectionFactory();
            createAVTracks();
            initializePeerConnections();
            startStreams();
        });

    }

    public void stop() {
        if (socket != null) {
            sendMessage("bye");
            socket.disconnect();
            socket = null;
        }
        if (localAudioTrack != null) {
            localAudioTrack.setEnabled(false);
            localAudioTrack = null;
        }
        if (peerConnection != null) {
            peerConnection.close();
            peerConnection = null;
        }
        isInitiator = false;
        isStarted = false;
        isChannelReady = false;
    }

    // TODO(nikola): this code will be raplaced with signaling messages via noise
    // TODO(nikola): move this out to another file. Should not be in the activity.
    private void connectToSignallingServer() {

        try {
            String URL = "http://stun.halloapp.dev:3000/";
            Log.i("IO Socket:" + URL);
            socket = IO.socket(URL);

            socket.on(Socket.EVENT_CONNECT, args -> {
                Log.i("connectToSignallingServer: connect");
                socket.emit("create or join", "foo1 ");
            }).on("ipaddr", args -> {
                Log.i("connectToSignallingServer: ipaddr");
            }).on("created", args -> {
                Log.i("connectToSignallingServer: created");
                isInitiator = true;
            }).on("full", args -> {
                Log.i("connectToSignallingServer: full");
            }).on("join", args -> {
                Log.i("connectToSignallingServer: join");
                Log.i("connectToSignallingServer: Another peer made a request to join room");
                Log.i("connectToSignallingServer: This peer is the initiator of room");
                isChannelReady = true;
            }).on("joined", args -> {
                Log.i("connectToSignallingServer: joined");
                isChannelReady = true;
            }).on("log", args -> {
                for (Object arg : args) {
                    Log.i("connectToSignallingServer: " + arg);
                }
            }).on("message", args -> {
                Log.i("connectToSignallingServer: got a message");
            }).on("message", args -> {
                try {
                    if (args[0] instanceof String) {
                        String message = (String) args[0];
                        if (message.equals("got user media")) {
                            maybeStart();
                        }
                    } else {
                        JSONObject message = (JSONObject) args[0];
                        Log.d("connectToSignallingServer: got message " + message);
                        if (message.getString("type").equals("offer")) {
                            Log.i("connectToSignallingServer: received an offer " + isInitiator + " " + isStarted);
                            if (!isInitiator && !isStarted) {
                                // TODO(nikola): This can not be called.
                                maybeStart();
                            }
                            peerConnection.setRemoteDescription(
                                    new SimpleSdpObserver(),
                                    new SessionDescription(SessionDescription.Type.OFFER,
                                            message.getString("sdp")));
                            doAnswer();
                            callViewModel.onAcceptCall();
                        } else if (message.getString("type").equals("answer") && isStarted) {
                            Log.i("got answer " + message);
                            peerConnection.setRemoteDescription(
                                    new SimpleSdpObserver(),
                                    new SessionDescription(SessionDescription.Type.ANSWER,
                                            message.getString("sdp")));
                            callViewModel.onAcceptCall();
                        } else if (message.getString("type").equals("candidate") && isStarted) {
                            Log.i("connectToSignallingServer: receiving candidates");
                            IceCandidate candidate = new IceCandidate(
                                    message.getString("id"),
                                    message.getInt("label"),
                                    message.getString("candidate"));
                            peerConnection.addIceCandidate(candidate);
                        }
                        /*else if (message === 'bye' && isStarted) {
                        handleRemoteHangup();
                    }*/
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }).on(Socket.EVENT_DISCONNECT, args -> {
                Log.i("connectToSignallingServer: disconnect");
            });
            socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
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

        sendMessage("got user media");
    }


    private PeerConnection createPeerConnection(PeerConnectionFactory factory) {
        ArrayList<PeerConnection.IceServer> iceServers = new ArrayList<>();
        // TODO(nikola): This servers will be set by the backend.
//        String URL = "stun:stun.l.google.com:19302";
        String STUN_URL = "stun:stun.halloapp.dev:3478";
        String TURN_URL = "turn:turn.halloapp.dev:3478";
        iceServers.add(PeerConnection.IceServer.builder(STUN_URL).createIceServer());
        iceServers.add(PeerConnection.IceServer.builder(TURN_URL)
                .setUsername("clients")
                // TODO(nikola): This password will be set by the backend.
                .setPassword("2Nh57xoGpDy7Z7D1Sg0S")
                .createIceServer());
        Log.i("ice servers: " + iceServers);

        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);

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
                JSONObject message = new JSONObject();

                try {
                    message.put("type", "candidate");
                    message.put("label", iceCandidate.sdpMLineIndex);
                    message.put("id", iceCandidate.sdpMid);
                    message.put("candidate", iceCandidate.sdp);

                    Log.i("onIceCandidate: sending candidate " + message);
                    sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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

    private void sendMessage(Object message) {
        Log.i("sending " + message);
        socket.emit("message", message);
    }

    private void maybeStart() {
        Log.i("maybeStart: isInitiator " + isInitiator + " isStarted:" + isStarted
                + " isChannelReady:" + isChannelReady);
        if (!isStarted && isChannelReady) {
            isStarted = true;
            if (isInitiator) {
                doCall();
            }
        }
    }

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
                JSONObject message = new JSONObject();
                try {
                    message.put("type", "offer");
                    message.put("sdp", sessionDescription.description);
                    sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, sdpMediaConstraints);
    }

    private void doAnswer() {
        peerConnection.createAnswer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                peerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
                JSONObject message = new JSONObject();
                try {
                    message.put("type", "answer");
                    message.put("sdp", sessionDescription.description);
                    sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new MediaConstraints());
    }

    public void onMute(boolean mute) {
        // TODO(nikola): Use the audioManager here
        localAudioTrack.setEnabled(!mute);
    }
}

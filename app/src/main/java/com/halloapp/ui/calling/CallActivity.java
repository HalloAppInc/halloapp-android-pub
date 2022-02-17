package com.halloapp.ui.calling;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.Notifications;
import com.halloapp.R;
import com.halloapp.calling.CallManager;
import com.halloapp.calling.ProxyVideoSink;
import com.halloapp.calling.VideoUtils;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.id.UserId;
import com.halloapp.proto.server.CallType;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;

import org.webrtc.Camera2Enumerator;
import org.webrtc.EglBase;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoFrame;
import org.webrtc.VideoSink;
import org.webrtc.VideoSource;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class CallActivity extends HalloActivity implements EasyPermissions.PermissionCallbacks, EasyPermissions.RationaleCallbacks  {

    private static final String EXTRA_CALL_ID = "call_id";
    private static final String EXTRA_PEER_UID = "peer_uid";
    private static final String EXTRA_IS_INITIATOR = "is_initiator";
    private static final String EXTRA_CALL_TYPE = "call_type";

    private static final int REQUEST_START_CALL = 1;
    private static final int REQUEST_ANSWER_CALL = 2;

    private static final String ACTION_ACCEPT = "accept";

    private static final int CONTROLS_FADE_INITIAL_DELAY_MS = 2000;
    private static final int CONTROLS_FADE_ON_CLICK_DELAY_MS = 5000;


    public static Intent getStartCallIntent(@NonNull Context context, @NonNull UserId userId, @NonNull CallType callType) {
        Intent intent = new Intent(context, CallActivity.class);
        intent.putExtra(EXTRA_PEER_UID, userId.rawId());
        intent.putExtra(EXTRA_IS_INITIATOR, true);
        intent.putExtra(EXTRA_CALL_TYPE, callType.getNumber());
        return intent;
    }

    public static Intent getOngoingCallIntent(@NonNull Context context, @NonNull UserId userId, boolean isInitiator) {
        Intent intent = new Intent(context, CallActivity.class);
        intent.putExtra(EXTRA_PEER_UID, userId.rawId());
        intent.putExtra(EXTRA_IS_INITIATOR, isInitiator);
        return intent;
    }

    public static Intent getReturnToCallIntent(@NonNull Context context, @NonNull UserId peerUid) {
        Intent intent = new Intent(context, CallActivity.class);
        intent.putExtra(EXTRA_PEER_UID, peerUid.rawId());
        return intent;
    }

    public static Intent incomingCallIntent(@NonNull Context context, @NonNull String callId, @NonNull UserId peerUid, @NonNull CallType callType) {
        Intent intent = new Intent(context, CallActivity.class);
        intent.putExtra(EXTRA_CALL_ID, callId);
        intent.putExtra(EXTRA_PEER_UID, peerUid.rawId());
        intent.putExtra(EXTRA_IS_INITIATOR, false);
        intent.putExtra(EXTRA_CALL_TYPE, callType.getNumber());
        return intent;
    }

    public static Intent acceptCallIntent(@NonNull Context context, @NonNull String callId, @NonNull UserId peerUid, @NonNull CallType callType) {
        Intent intent = new Intent(context, CallActivity.class);
        intent.setAction(CallActivity.ACTION_ACCEPT);
        intent.putExtra(EXTRA_CALL_ID, callId);
        intent.putExtra(EXTRA_PEER_UID, peerUid.rawId());
        intent.putExtra(EXTRA_IS_INITIATOR, false);
        intent.putExtra(EXTRA_CALL_TYPE, callType.getNumber());
        return intent;
    }

    private final AvatarLoader avatarLoader = AvatarLoader.getInstance();
    private final CallManager callManager = CallManager.getInstance();

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private int shortAnimationDuration;

    private View topContainerView;
    private View controlsContainerView;

    private View ringingView;
    private View inCallView;

    private ImageView avatarView;
    private TextView nameTextView;
    private TextView titleTextView;
    private Chronometer callTimerView;
    private ImageView muteButtonView;
    private ImageView speakerButtonView;

    private TextView muteLabelView;
    private TextView speakerLabelView;

    private SurfaceViewRenderer remoteVideoView;
    private SurfaceViewRenderer localVideoView;

    private CallViewModel callViewModel;
    private UserId peerUid;
    private boolean isInitiator;
    private CallType callType;

    private ContactLoader contactLoader;

    private final ProxyVideoSink remoteProxyVideoSink = new ProxyVideoSink();
    private final ProxyVideoSink localProxyVideoSink = new ProxyVideoSink();

    // CallManager is responsible for cleaning up those.
    private VideoCapturer videoCapturer;
    private VideoSource videoSource;
    private SurfaceTextureHelper surfaceTextureHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        {
            String userUidStr = getIntent().getStringExtra(EXTRA_PEER_UID);
            Preconditions.checkNotNull(userUidStr);
            UserId peerUid = new UserId(userUidStr);
            Boolean isInitiator;
            if (getIntent().hasExtra(EXTRA_IS_INITIATOR)) {
                isInitiator = getIntent().getBooleanExtra(EXTRA_IS_INITIATOR, false);
            } else {
                isInitiator = null;
            }
            CallType callType = CallType.forNumber(getIntent().getIntExtra(EXTRA_CALL_TYPE, -1));
            Log.i("CallActivity/onCreate Extras peerUid: " + peerUid + " isInitiator: " + isInitiator + " callType: " + callType);
            callViewModel = new ViewModelProvider(this, new CallViewModel.Factory(getApplication(), peerUid, isInitiator, callType)).get(CallViewModel.class);
        }
        this.peerUid = callViewModel.getPeerUid();
        this.isInitiator = callViewModel.getIsInitiator();
        this.callType = callViewModel.getCallType();

        Log.i("CallActivity/onCreate peerUid: " + peerUid + " isInitiator: " + isInitiator + " callType: " + callType);

        int flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;
        if (callType == CallType.VIDEO) {
            flags = flags | WindowManager.LayoutParams.FLAG_FULLSCREEN;
        }
        getWindow().addFlags(flags);

        setContentView(R.layout.activity_call);

        ActionBar actionBar = Preconditions.checkNotNull(getSupportActionBar());
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("");
        if (callType == CallType.VIDEO) {
            actionBar.hide();
        }

        final EglBase eglBase = callManager.getEglBase();

        shortAnimationDuration = getResources().getInteger(android.R.integer.config_mediumAnimTime);

        contactLoader = new ContactLoader();

        topContainerView = findViewById(R.id.call_top_container);
        controlsContainerView = findViewById(R.id.controls_container);
        ringingView = findViewById(R.id.ringing_view);
        inCallView = findViewById(R.id.in_call_view);

        avatarView = findViewById(R.id.avatar);
        nameTextView = findViewById(R.id.name);
        titleTextView = findViewById(R.id.title);
        callTimerView = findViewById(R.id.call_timer);
        muteButtonView = findViewById(R.id.in_call_mute);
        speakerButtonView = findViewById(R.id.in_call_speaker);

        speakerLabelView = findViewById(R.id.speaker_label);
        muteLabelView = findViewById(R.id.mute_label);
        remoteVideoView = findViewById(R.id.call_remote_video);
        localVideoView = findViewById(R.id.call_local_video);

        if (callType == CallType.VIDEO) {
            localVideoView.init(eglBase.getEglBaseContext(), null);
            localVideoView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);

            remoteVideoView.init(eglBase.getEglBaseContext(), null);
            remoteVideoView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);

            localVideoView.setZOrderMediaOverlay(true);
            localVideoView.setEnableHardwareScaler(true);
            remoteVideoView.setEnableHardwareScaler(true);

            remoteProxyVideoSink.setTarget(remoteVideoView);
            localProxyVideoSink.setTarget(localVideoView);
            callManager.setVideoSinks(localProxyVideoSink, remoteProxyVideoSink);
        }

        callViewModel.getState().observe(this, state -> {
            ringingView.setVisibility(View.GONE);
            inCallView.setVisibility(View.GONE);
            callTimerView.setVisibility(View.GONE);
            titleTextView.setVisibility(View.VISIBLE);
            Log.i("CallActivity: State -> " + CallManager.stateToString(state));
            switch (state) {
                case CallManager.State.IDLE:
                    Log.i("CallActivity/State -> IDLE");
                    break;
                case CallManager.State.CALLING:
                    Log.i("CallActivity/State -> CALLING");
                    titleTextView.setText(R.string.calling);
                    inCallView.setVisibility(View.VISIBLE);
                    break;
                case CallManager.State.CALLING_RINGING:
                    Log.i("CallActivity/State -> CALLING_RINGING");
                    titleTextView.setText(R.string.ringing);
                    inCallView.setVisibility(View.VISIBLE);
                    break;
                case CallManager.State.IN_CALL_CONNECTING:
                    Log.i("CallActivity/State -> IN_CALL_CONNECTING");
                    inCallView.setVisibility(View.VISIBLE);
                    titleTextView.setText(R.string.connecting);
                    break;
                case CallManager.State.IN_CALL:
                    Log.i("CallActivity/State -> IN_CALL");
                    inCallView.setVisibility(View.VISIBLE);
                    callTimerView.setVisibility(View.VISIBLE);
                    titleTextView.setVisibility(View.GONE);
                    // TODO(clark): Make the state of the UI more explicit in the CallActivity
                    if (callViewModel.getCallType() == CallType.AUDIO) {
                        remoteVideoView.setVisibility(View.GONE);
                        localVideoView.setVisibility(View.GONE);
                    } else {
                        remoteVideoView.setVisibility(View.VISIBLE);
                        localVideoView.setVisibility(View.VISIBLE);
                        mainHandler.postDelayed(this::animateOutControls, CONTROLS_FADE_INITIAL_DELAY_MS);
                    }
                    startCallTimer();
                    break;
                case CallManager.State.INCOMING_RINGING:
                    Log.i("CallActivity/State -> RINGING");
                    if (callViewModel.getCallType() == CallType.AUDIO) {
                        titleTextView.setText(R.string.incoming_voice_call_notification_title);
                    } else {
                        titleTextView.setText(R.string.incoming_video_call_notification_title);
                    }
                    ringingView.setVisibility(View.VISIBLE);
                    break;
                case CallManager.State.END:
                    Log.i("CallActivity/State -> END");
                    Log.i("finishing the activity");
                    finish();
                    break;
            }
        });

        callViewModel.getIsMicrophoneMuted().observe(this, this::updateMicrophoneMutedUI);
        callViewModel.getIsSpeakerPhoneOn().observe(this, this::updateSpeakerPhoneUI);

        avatarLoader.load(avatarView, peerUid, false);
        contactLoader.load(nameTextView, peerUid, false);

        findViewById(R.id.in_call_mute).setOnClickListener(v -> {
            onMute();
        });
        findViewById(R.id.in_call_speaker).setOnClickListener(v -> {
            Log.i("CallActivity: in_call_speaker click");
            onSpeakerPhone();
        });
        findViewById(R.id.in_call_hangup).setOnClickListener(v -> {
            Log.i("CallActivity: in_call_hangup click");
            if (callViewModel.isCalling()) {
                onCancelCall();
            } else if (callViewModel.inCall() || callViewModel.isCallConnecting()) {
                onHangUp();
            }
        });

        findViewById(R.id.accept_view).setOnClickListener(v -> {
            if (!callViewModel.isRinging()) {
                return;
            }
            checkPermissionsThen(REQUEST_ANSWER_CALL);
        });
        findViewById(R.id.decline_view).setOnClickListener(v -> {
            if (!callViewModel.isRinging()) {
                return;
            }
            onDeclineCall();
        });

        // TODO(nikola): Maybe we should listen on touch.
        remoteVideoView.setOnClickListener(v -> {
            showControls();
            mainHandler.removeCallbacks(this::animateOutControls);
            mainHandler.postDelayed(this::animateOutControls, CONTROLS_FADE_ON_CLICK_DELAY_MS);
        });

        if (callType == CallType.VIDEO) {
            // remoteVideoView is made visible when we are are IN_CALL
            //remoteVideoView.setVisibility(View.VISIBLE);
            localVideoView.setVisibility(View.VISIBLE);
            hideSystemBars();
        } else {
            remoteVideoView.setVisibility(View.GONE);
            localVideoView.setVisibility(View.GONE);
        }

        if (isInitiator && callViewModel.isIdle()) {
            Log.i("CallActivity: Starting " + callType + " call");
            checkPermissionsThen(REQUEST_START_CALL);
        }
        if (!isInitiator && ACTION_ACCEPT.equals(getIntent().getAction()) && callViewModel.isRinging()) {
            Log.i("CallActivity: User accepted the call");
            checkPermissionsThen(REQUEST_ANSWER_CALL);
        }
        if (!isInitiator && !callManager.getIsInCall().getValue()) {
            Log.i("CallActivity: call probably ended while activity was starting");
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        contactLoader.destroy();
        localProxyVideoSink.setTarget(null);
        remoteProxyVideoSink.setTarget(null);
        if (localVideoView != null) {
            localVideoView.release();
            localVideoView = null;
        }
        if (remoteVideoView != null) {
            remoteVideoView.release();
            remoteVideoView = null;
        }
    }

    private void hideSystemBars() {
        WindowInsetsControllerCompat windowInsetsController = ViewCompat.getWindowInsetsController(this.getWindow().getDecorView());
        if (windowInsetsController == null) {
            return;
        }
        windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
    }

    @Override
    protected boolean considerUserAvailable() {
        return false;
    }

    private void checkPermissionsThen(int request) {
        String[] perms = getPerms();
        if (EasyPermissions.hasPermissions(this, perms)) {
            handleRequest(request);
        } else {
            String rationale = getPermissionsRationale();
            EasyPermissions.requestPermissions(this, rationale, request, perms);
        }
    }

    private String[] getPerms() {
        CallType callType = callViewModel.getCallType();
        if (callType == CallType.AUDIO) {
            return new String[] {Manifest.permission.RECORD_AUDIO};
        } else if (callType == CallType.VIDEO) {
            return new String[] {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        } else {
            return new String[] {Manifest.permission.RECORD_AUDIO};
        }
    }

    private String getPermissionsRationale() {
        CallType callType = callViewModel.getCallType();
        if (callType == CallType.AUDIO) {
            return getString(R.string.voice_call_record_audio_permission_rationale);
        } else if (callType == CallType.VIDEO) {
            return getString(R.string.voice_call_record_video_permission_rationale);
        } else {
            return getString(R.string.voice_call_record_audio_permission_rationale);
        }
    }

    private void handleRequest(int request) {
        if (request == REQUEST_START_CALL) {
            onStartCall(callViewModel.getCallType());
        } else if (request == REQUEST_ANSWER_CALL) {
            onAcceptCall();
        } else {
            Log.w("CallActivity.handleRequest unknown request " + request);
        }
    }

    private void createVideoCapturer() {
        surfaceTextureHelper = SurfaceTextureHelper.create("HACaptureThread", callManager.getEglBase().getEglBaseContext());
        videoCapturer = VideoUtils.createVideoCapturer(this);
        videoSource = callManager.createVideoSource(videoCapturer);
        videoCapturer.initialize(surfaceTextureHelper, this, videoSource.getCapturerObserver());
    }


    private void onStartCall(@NonNull CallType callType) {
        if (callType == CallType.VIDEO) {
            createVideoCapturer();
        }
        callViewModel.onStartCall(callType, videoCapturer, videoSource, surfaceTextureHelper);
    }

    private void onDeclineCall() {
        Notifications.getInstance(getApplicationContext()).clearIncomingCallNotification();
        callViewModel.onDeclineCall();
    }

    private void onAcceptCall() {
        Notifications.getInstance(getApplicationContext()).clearIncomingCallNotification();
        if (callViewModel.getCallType() == CallType.VIDEO) {
            createVideoCapturer();
        }
        callViewModel.onAcceptCall(videoCapturer, videoSource, surfaceTextureHelper);
    }

    private void onCancelCall() {
        callViewModel.onCancelCall();
    }

    private void onHangUp() {
        callViewModel.onHangUp();
    }

    private void onMute() {
        Log.i("CallActivity onMute called");
        callViewModel.toggleMicrophoneMute();
    }

    private void updateMicrophoneMutedUI(boolean mute) {
        Log.i("CallActivity muteButton mute: " + mute);
        muteButtonView.setSelected(mute);
    }

    private void onSpeakerPhone() {
        Log.i("CallActivity onSpeakerPhone called");
        callViewModel.toggleSpeakerPhone();
    }

    private void updateSpeakerPhoneUI(boolean on) {
        Log.i("CallActivity speakerButton on: " + on);
        speakerButtonView.setSelected(on);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Log.i("Call permissions Granted " + requestCode + " " + perms);
        // TODO(nikola): update here for video calls. If user does not give video permission maybe it is ok?
        if (perms.contains(Manifest.permission.RECORD_AUDIO)) {
            handleRequest(requestCode);
            return;
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Log.i("Call permissions Denied " + requestCode + " " + perms);
    }

    @Override
    public void onRationaleAccepted(int requestCode) {
        Log.i("onRationaleAccepted(int requestCode:" + requestCode + ")");
    }

    @Override
    public void onRationaleDenied(int requestCode) {
        Log.i("onRationaleDeclined(int requestCode:" + requestCode + ")");
    }

    private void startCallTimer() {
        long ts = callViewModel.getCallStartTime();
        if (ts != 0) {
            callTimerView.setBase(ts);
            callTimerView.start();
        } else {
            Log.e("CallActivity.startCallTimer the call start time is not set");
        }
    }

    private void animateOutControls() {
        topContainerView.animate()
                .alpha(0f)
                .setDuration(shortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        topContainerView.setVisibility(View.GONE);
                    }
                });
        controlsContainerView.animate()
                .alpha(0f)
                .setDuration(shortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        controlsContainerView.setVisibility(View.GONE);
                    }
                });
    }

    private void showControls() {
        topContainerView.setAlpha(1f);
        topContainerView.setVisibility(View.VISIBLE);
        controlsContainerView.setAlpha(1f);
        controlsContainerView.setVisibility(View.VISIBLE);
    }

}

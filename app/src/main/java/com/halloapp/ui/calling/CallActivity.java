package com.halloapp.ui.calling;

import android.Manifest;
import android.app.PictureInPictureParams;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.Notifications;
import com.halloapp.R;
import com.halloapp.calling.CallManager;
import com.halloapp.calling.HAVideoCapturer;
import com.halloapp.calling.VideoUtils;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.id.UserId;
import com.halloapp.proto.server.CallType;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.calling.CallParticipantsLayout;

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

    public static Intent getStartCallIntent(@NonNull Context context, @NonNull UserId userId, @NonNull CallType callType) {
        Intent intent = createBaseCallIntent(context);
        intent.putExtra(EXTRA_PEER_UID, userId.rawId());
        intent.putExtra(EXTRA_IS_INITIATOR, true);
        intent.putExtra(EXTRA_CALL_TYPE, callType.getNumber());
        return intent;
    }

    public static Intent getOngoingCallIntent(@NonNull Context context, @NonNull UserId userId, boolean isInitiator) {
        Intent intent = createBaseCallIntent(context);
        intent.putExtra(EXTRA_PEER_UID, userId.rawId());
        intent.putExtra(EXTRA_IS_INITIATOR, isInitiator);
        return intent;
    }

    public static Intent getReturnToCallIntent(@NonNull Context context, @NonNull UserId peerUid) {
        Intent intent = createBaseCallIntent(context);
        intent.putExtra(EXTRA_PEER_UID, peerUid.rawId());
        return intent;
    }

    public static Intent incomingCallIntent(@NonNull Context context, @NonNull String callId, @NonNull UserId peerUid, @NonNull CallType callType) {
        Intent intent = createBaseCallIntent(context);
        intent.putExtra(EXTRA_CALL_ID, callId);
        intent.putExtra(EXTRA_PEER_UID, peerUid.rawId());
        intent.putExtra(EXTRA_IS_INITIATOR, false);
        intent.putExtra(EXTRA_CALL_TYPE, callType.getNumber());
        return intent;
    }

    public static Intent acceptCallIntent(@NonNull Context context, @NonNull String callId, @NonNull UserId peerUid, @NonNull CallType callType) {
        Intent intent = createBaseCallIntent(context);
        intent.setAction(CallActivity.ACTION_ACCEPT);
        intent.putExtra(EXTRA_CALL_ID, callId);
        intent.putExtra(EXTRA_PEER_UID, peerUid.rawId());
        intent.putExtra(EXTRA_IS_INITIATOR, false);
        intent.putExtra(EXTRA_CALL_TYPE, callType.getNumber());
        return intent;
    }

    private static Intent createBaseCallIntent(@NonNull Context context) {
        Intent intent = new Intent(context, CallActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }

    private final AvatarLoader avatarLoader = AvatarLoader.getInstance();
    private final CallManager callManager = CallManager.getInstance();

    private View ringingView;
    private View inCallView;

    private ImageView avatarView;
    private TextView nameTextView;
    private TextView titleTextView;
    private Chronometer callTimerView;

    private ImageView muteButtonView;
    private ImageView speakerButtonView;

    @Nullable
    private ImageView flipCameraButtonView;

    private CallParticipantsLayout participantsLayout;

    private VideoCallControlsController videoCallControlsController;

    private CallViewModel callViewModel;
    private UserId peerUid;
    private boolean isInitiator;
    private CallType callType;

    private ContactLoader contactLoader;

    // CallManager is responsible for cleaning up those.
    private HAVideoCapturer videoCapturer;

    private boolean systemAllowsPip;

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode);
        if (participantsLayout != null) {
            if (isInPictureInPictureMode) {
                participantsLayout.enterPiPView();
                videoCallControlsController.hideControls();
            } else {
                participantsLayout.exitPiPView();
            }
        }
    }

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

        boolean isVideoCall = callType == CallType.VIDEO;
        if (isVideoCall) {
            flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        }
        getWindow().addFlags(flags);

        setContentView(isVideoCall ? R.layout.activity_call_video : R.layout.activity_call);

        ActionBar actionBar = Preconditions.checkNotNull(getSupportActionBar());
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("");

        if (isVideoCall) {
            actionBar.hide();
            if (Build.VERSION.SDK_INT >= 24) {
                systemAllowsPip = getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE);
                if (systemAllowsPip && Build.VERSION.SDK_INT >= 26) {
                    setPictureInPictureParams(createPiPParams());
                }
            }
        }

        contactLoader = new ContactLoader();

        ringingView = findViewById(R.id.ringing_view);
        inCallView = findViewById(R.id.in_call_view);

        avatarView = findViewById(R.id.avatar);
        nameTextView = findViewById(R.id.name);
        titleTextView = findViewById(R.id.title);
        callTimerView = findViewById(R.id.call_timer);
        muteButtonView = findViewById(R.id.in_call_mute);
        speakerButtonView = findViewById(R.id.in_call_speaker);
        flipCameraButtonView = findViewById(R.id.in_call_flip_camera);

        participantsLayout = findViewById(R.id.participants_view);

        if (callType == CallType.VIDEO) {
            participantsLayout.bind(callManager);
            flipCameraButtonView.setOnClickListener(v -> {
                onFlipCameraClick();
            });
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
                    if (callViewModel.getCallType() == CallType.VIDEO) {
                        videoCallControlsController.onCallStart();
                        if (videoCapturer == null) {
                            createVideoCapturer();
                            callManager.attachCapturer(videoCapturer);
                        }
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

        if (callType == CallType.VIDEO) {
            videoCallControlsController = new VideoCallControlsController(findViewById(R.id.call_top_container), findViewById(R.id.controls_container));
            videoCallControlsController.bindParticipantsView(participantsLayout);
            videoCallControlsController.showControlsForever();
            participantsLayout.setVisibility(View.VISIBLE);
            // remoteVideoView is made visible when we are are IN_CALL
            //remoteVideoView.setVisibility(View.VISIBLE);
            hideSystemBars();
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
    public void onBackPressed() {
        boolean enteredPip = false;
        if (shouldEnterPiP()) {
            enteredPip = tryEnterPiP();
        }
        if (!enteredPip) {
            super.onBackPressed();
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
    protected void onUserLeaveHint() {
        if (Build.VERSION.SDK_INT < 26 && shouldEnterPiP()) {
            tryEnterPiP();
        }
    }

    private boolean shouldEnterPiP() {
        return systemAllowsPip && callType == CallType.VIDEO;
    }

    @RequiresApi(api = 26)
    private PictureInPictureParams createPiPParams() {
        PictureInPictureParams.Builder builder = new PictureInPictureParams.Builder();
        builder.setAspectRatio(CallManager.getPiPAspectRatio());
        if (Build.VERSION.SDK_INT >= 31) {
            builder.setAutoEnterEnabled(true);
            builder.setSeamlessResizeEnabled(true);
        }
        return builder.build();
    }

    private boolean tryEnterPiP() {
        if (Build.VERSION.SDK_INT >= 24) {
            if (Build.VERSION.SDK_INT >= 26) {
                return enterPictureInPictureMode(createPiPParams());
            } else {
                enterPictureInPictureMode();
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        contactLoader.destroy();
        if (participantsLayout != null) {
            participantsLayout.destroy();
            participantsLayout = null;
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
        createVideoCapturer(callManager.isFrontFacing());
    }

    private void createVideoCapturer(boolean frontFacing) {
        videoCapturer = VideoUtils.createVideoCapturer(this, frontFacing);
    }

    private void onStartCall(@NonNull CallType callType) {
        if (callType == CallType.VIDEO) {
            createVideoCapturer();
        }
        callViewModel.onStartCall(callType, videoCapturer);
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
        callViewModel.onAcceptCall(videoCapturer);
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

    private void onFlipCameraClick() {
        boolean frontFacing = callManager.isFrontFacing();
        createVideoCapturer(!frontFacing);
        callManager.attachCapturer(videoCapturer);
        flipCameraButtonView.setSelected(!frontFacing);
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
}

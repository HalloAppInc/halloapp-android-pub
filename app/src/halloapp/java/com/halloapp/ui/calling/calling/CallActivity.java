package com.halloapp.ui.calling.calling;

import android.Manifest;
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

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.Notifications;
import com.halloapp.R;
import com.halloapp.calling.calling.CallManager;
import com.halloapp.calling.calling.HAVideoCapturer;
import com.halloapp.calling.calling.VideoUtils;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.id.UserId;
import com.halloapp.proto.server.CallType;
import com.halloapp.proto.server.EndCall;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.profile.ViewProfileActivity;
import com.halloapp.util.PictureInPictureUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.calling.calling.CallParticipantsLayout;

import java.util.Arrays;
import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class CallActivity extends HalloActivity implements EasyPermissions.PermissionCallbacks, EasyPermissions.RationaleCallbacks  {

    private static final String EXTRA_CALL_ID = "call_id";
    private static final String EXTRA_PEER_UID = "peer_uid";
    private static final String EXTRA_IS_INITIATOR = "is_initiator";
    private static final String EXTRA_CALL_TYPE = "call_type";

    private static final int REQUEST_START_CALL = 1;
    private static final int REQUEST_ANSWER_CALL = 2;

    private static final String ACTION_ACCEPT = "accept";

    public static Intent getStartCallIntent(@NonNull Context context, @NonNull UserId peerUid, @NonNull CallType callType) {
        Intent intent = createBaseCallIntent(context, peerUid, callType);
        intent.putExtra(EXTRA_IS_INITIATOR, true);
        return intent;
    }

    public static Intent getOngoingCallIntent(@NonNull Context context, @NonNull UserId peerUid, boolean isInitiator) {
        Intent intent = createBaseCallIntent(context, peerUid, CallManager.getInstance().getCallType());
        intent.putExtra(EXTRA_IS_INITIATOR, isInitiator);
        return intent;
    }

    public static Intent getReturnToCallIntent(@NonNull Context context, @NonNull UserId peerUid) {
        return createBaseCallIntent(context, peerUid, CallManager.getInstance().getCallType());
    }

    public static Intent incomingCallIntent(@NonNull Context context, @NonNull String callId, @NonNull UserId peerUid, @NonNull CallType callType) {
        Intent intent = createBaseCallIntent(context, peerUid, callType);
        intent.putExtra(EXTRA_CALL_ID, callId);
        intent.putExtra(EXTRA_IS_INITIATOR, false);
        return intent;
    }

    public static Intent acceptCallIntent(@NonNull Context context, @NonNull String callId, @NonNull UserId peerUid, @NonNull CallType callType) {
        Intent intent = createBaseCallIntent(context, peerUid, callType);
        intent.setAction(CallActivity.ACTION_ACCEPT);
        intent.putExtra(EXTRA_CALL_ID, callId);
        intent.putExtra(EXTRA_IS_INITIATOR, false);
        return intent;
    }

    private static Intent createBaseCallIntent(@NonNull Context context, @NonNull UserId peerUid, @NonNull CallType callType) {
        Intent intent = new Intent(context, CallActivity.class);
        intent.putExtra(EXTRA_PEER_UID, peerUid.rawId());
        intent.putExtra(EXTRA_CALL_TYPE, callType.getNumber());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }

    private final AvatarLoader avatarLoader = AvatarLoader.getInstance();
    private final CallManager callManager = CallManager.getInstance();

    private View ringingView;
    private View inCallView;

    private ImageView avatarView;
    private ImageView mutedAvatarView;
    private TextView nameTextView;
    private TextView titleTextView;
    private Chronometer callTimerView;
    private TextView onHoldView;

    private ImageView muteButtonView;
    @Nullable
    private ImageView speakerButtonView;
    @Nullable
    private ImageView muteCameraButtonView;
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

        String userUidStr = getIntent().getStringExtra(EXTRA_PEER_UID);
        Preconditions.checkNotNull(userUidStr);
        UserId peerUid = new UserId(userUidStr);
        boolean isInitiator = getIntent().getBooleanExtra(EXTRA_IS_INITIATOR, false);
        CallType callType = Preconditions.checkNotNull(CallType.forNumber(getIntent().getIntExtra(EXTRA_CALL_TYPE, -1)));
        Log.i("CallActivity/onCreate Extras peerUid: " + peerUid + " isInitiator: " + isInitiator + " callType: " + callType);
        callViewModel = new ViewModelProvider(this, new CallViewModel.Factory(getApplication(), peerUid, isInitiator, callType)).get(CallViewModel.class);
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
            setTheme(R.style.AppTheme_Video);
        }
        getWindow().addFlags(flags);

        setContentView(isVideoCall ? R.layout.activity_call_video : R.layout.activity_call);

        setSupportActionBar(findViewById(R.id.toolbar));
        ActionBar actionBar = Preconditions.checkNotNull(getSupportActionBar());
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("");

        if (isVideoCall) {
            if (Build.VERSION.SDK_INT >= 24) {
                systemAllowsPip = getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE);
                if (systemAllowsPip && Build.VERSION.SDK_INT >= 26) {
                    setPictureInPictureParams(PictureInPictureUtils.buildVideoCallParams());
                }
            }
        }

        contactLoader = new ContactLoader(userId -> {
            startActivity(ViewProfileActivity.viewProfile(this, userId));
            return null;
        });

        ringingView = findViewById(R.id.ringing_view);
        inCallView = findViewById(R.id.in_call_view);

        avatarView = findViewById(R.id.avatar);
        mutedAvatarView = findViewById(R.id.muted_avatar);
        nameTextView = findViewById(R.id.name);
        titleTextView = findViewById(R.id.title);
        onHoldView = findViewById(R.id.call_hold);
        callTimerView = findViewById(R.id.call_timer);
        muteButtonView = findViewById(R.id.in_call_mute);
        speakerButtonView = findViewById(R.id.in_call_speaker);
        muteCameraButtonView = findViewById(R.id.in_call_mute_camera);
        flipCameraButtonView = findViewById(R.id.in_call_flip_camera);

        participantsLayout = findViewById(R.id.participants_view);

        if (callType == CallType.VIDEO) {
            participantsLayout.bind(callManager);
            muteCameraButtonView.setOnClickListener(v -> {
                onMuteCameraClick();
            });
            flipCameraButtonView.setOnClickListener(v -> {
                onFlipCameraClick();
            });
        }

        onHoldView.setText(" / " + getString(R.string.call_on_hold));

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
                            callManager.attachCapturer(videoCapturer, false);
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
                    titleTextView.setText(R.string.disconnecting);
                    finishAndRemoveTask();
                    break;
            }
        });

        callViewModel.getIsRemoteVideoMuted().observe(this, this::updateRemoteVideoMutedUI);
        callViewModel.getIsMicrophoneMuted().observe(this, this::updateMicrophoneMutedUI);
        callViewModel.getIsSpeakerPhoneOn().observe(this, this::updateSpeakerPhoneUI);
        callViewModel.getIsOnHold().observe(this, this::updateOnHoldUi);

        avatarLoader.load(avatarView, peerUid, false);
        if (mutedAvatarView != null) {
            avatarLoader.load(mutedAvatarView, UserId.ME, false);
        }
        contactLoader.load(nameTextView, peerUid, false);

        muteButtonView.setOnClickListener(v -> {
            onMute();
        });
        if (speakerButtonView != null) {
            speakerButtonView.setOnClickListener(v -> {
                Log.i("CallActivity: in_call_speaker click");
                onSpeakerPhone();
            });
        }
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
            videoCallControlsController = new VideoCallControlsController(findViewById(R.id.action_bar_title), findViewById(R.id.call_top_container), findViewById(R.id.controls_container));
            videoCallControlsController.bindParticipantsView(participantsLayout);
            videoCallControlsController.showControlsForever();
            participantsLayout.setVisibility(View.VISIBLE);
            // remoteVideoView is made visible when we are are IN_CALL
            //remoteVideoView.setVisibility(View.VISIBLE);
        }

        if (isInitiator && callViewModel.isIdle()) {
            Log.i("CallActivity: Starting " + callType + " call");
            checkPermissionsThen(REQUEST_START_CALL);
        }
        if (!isInitiator && ACTION_ACCEPT.equals(getIntent().getAction()) && callViewModel.isRinging()) {
            Log.i("CallActivity: User accepted the call");
            Notifications.getInstance(this).clearIncomingCallNotification();
            checkPermissionsThen(REQUEST_ANSWER_CALL);
        }
        if (!isInitiator && !callManager.getIsInCall().getValue()) {
            Log.i("CallActivity: call probably ended while activity was starting");
            finishAndRemoveTask();
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


    private boolean tryEnterPiP() {
        if (Build.VERSION.SDK_INT >= 24) {
            if (Build.VERSION.SDK_INT >= 26) {
                return enterPictureInPictureMode(PictureInPictureUtils.buildVideoCallParams());
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

    @Override
    protected boolean considerUserAvailable() {
        return false;
    }

    private void checkPermissionsThen(int request) {
        String[] perms = getPerms();
        Log.i("CallActivity: checkPermissions: " + Arrays.toString(perms));
        if (EasyPermissions.hasPermissions(this, perms)) {
            Log.i("CallActivity: has permissions");
            handleRequest(request);
        } else {
            String rationale = getPermissionsRationale();
            Log.i("CallActivity: request permissions " + Arrays.toString(perms));
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
        participantsLayout.setMirrorLocal(frontFacing);
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

    private void updateRemoteVideoMutedUI(boolean muted) {
        if (participantsLayout != null) {
            participantsLayout.onRemoteVideoMuted(muted);
        }
        if (videoCallControlsController != null) {
            int state = callManager.getState();
            if (muted || state == CallManager.State.INCOMING_RINGING || state == CallManager.State.CALLING || state == CallManager.State.CALLING_RINGING) {
                videoCallControlsController.showControlsForever();
            } else {
                videoCallControlsController.hideControlsDelayed();
            }
        }
    }

    private void updateMicrophoneMutedUI(boolean mute) {
        Log.i("CallActivity muteButton mute: " + mute);
        muteButtonView.setSelected(mute);
        if (participantsLayout != null) {
            participantsLayout.onMicMuted(mute);
        }
    }

    private void onMuteCameraClick() {
        boolean cameraMute = !callManager.isCameraMuted();
        if (cameraMute) {
            callManager.detachCapturer();
            participantsLayout.onLocalCameraMute();
            muteCameraButtonView.setSelected(true);
        } else {
            createVideoCapturer();
            callManager.attachCapturer(videoCapturer, true);
            muteCameraButtonView.setSelected(false);
            participantsLayout.onLocalCameraUnmute();
        }
    }

    private void onFlipCameraClick() {
        boolean frontFacing = callManager.isFrontFacing();
        createVideoCapturer(!frontFacing);
        callManager.attachCapturer(videoCapturer, false);
    }

    @MainThread
    private void onSpeakerPhone() {
        Log.i("CallActivity onSpeakerPhone called");
        callViewModel.toggleSpeakerPhone();
    }

    private void updateSpeakerPhoneUI(boolean on) {
        Log.i("CallActivity speakerButton on: " + on);
        if (speakerButtonView != null) {
            speakerButtonView.setSelected(on);
        }
    }

    private void updateOnHoldUi(boolean hold) {
        Log.i("CallActivity updateOnHoldUi hold: " + hold);
        onHoldView.setVisibility(hold ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Log.i("CallActivity: permissions Granted " + requestCode + " " + perms);
        if (callType == CallType.AUDIO && perms.contains(Manifest.permission.RECORD_AUDIO)) {
            handleRequest(requestCode);
            return;
        }
        if (callType == CallType.VIDEO && perms.contains(Manifest.permission.RECORD_AUDIO) && perms.contains(Manifest.permission.CAMERA)) {
            handleRequest(requestCode);
            return;
        }
        Log.w("CallActivity: onPermissionsGranted unexpected state CallType: " + callType + " " + perms);
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Log.i("CallActivity: permissions Denied " + requestCode + " " + perms);
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            Log.i("CallActivity: some permissions permanently denied: " + perms);
            new AppSettingsDialog.Builder(this)
                    .setRationale(getString(R.string.call_record_audio_or_camera_permission_rationale_denied))
                    .build().show();
        } else {
            endCall();
        }
    }

    @Override
    public void onRationaleAccepted(int requestCode) {
        Log.i("onRationaleAccepted(int requestCode:" + requestCode + ")");
    }

    @Override
    public void onRationaleDenied(int requestCode) {
        Log.i("onRationaleDeclined(int requestCode:" + requestCode + ")");
    }

    private void endCall() {
        if (isInitiator) {
            // TODO(nikola): we should stats send to the server for this case of not granting permissions
            finishAndRemoveTask();
        } else {
            // TODO(nikola): we might want to send new special reason for no permissions
            callManager.endCall(EndCall.Reason.REJECT, true);
        }
    }

    private void startCallTimer() {
        long ts = callViewModel.getCallConnectTime();
        if (ts != 0) {
            callTimerView.setBase(ts);
            callTimerView.start();
        } else {
            Log.e("CallActivity.startCallTimer the call start time is not set");
        }
    }
}

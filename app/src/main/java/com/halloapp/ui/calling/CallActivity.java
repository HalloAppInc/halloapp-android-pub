package com.halloapp.ui.calling;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.halloapp.Notifications;
import com.halloapp.R;
import com.halloapp.calling.CallManager;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.id.UserId;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class CallActivity extends HalloActivity implements EasyPermissions.PermissionCallbacks, EasyPermissions.RationaleCallbacks  {

    public static final String EXTRA_CALL_ID = "call_id";
    public static final String EXTRA_PEER_UID = "peer_uid";
    public static final String EXTRA_IS_INITIATOR = "is_initiator";

    public static final int REQUEST_CODE_START_CALL = 1;
    public static final int REQUEST_CODE_INCOMING_CALL = 2;

    public static final String ACTION_ACCEPT = "accept";

    private final AvatarLoader avatarLoader = AvatarLoader.getInstance();

    private View callingView;
    private View ringingView;
    private View inCallView;
    private View initView;

    private ImageView avatarView;
    private TextView nameTextView;
    private TextView titleTextView;
    private ImageView muteButtonView;
    private ImageView speakerButtonView;

    private CallViewModel callViewModel;
    private UserId peerUid;

    private ContactLoader contactLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(getSystemUiVisibility());

        setContentView(R.layout.activity_call);

        contactLoader = new ContactLoader();

        callingView = findViewById(R.id.calling_view);
        ringingView = findViewById(R.id.ringing_view);
        inCallView = findViewById(R.id.in_call_view);
        initView = findViewById(R.id.init_view);

        avatarView = findViewById(R.id.avatar);
        nameTextView = findViewById(R.id.name);
        titleTextView = findViewById(R.id.title);
        muteButtonView = findViewById(R.id.in_call_mute);
        speakerButtonView = findViewById(R.id.in_call_speaker);

        callViewModel = new ViewModelProvider(this).get(CallViewModel.class);
        String userUidStr = getIntent().getStringExtra(EXTRA_PEER_UID);
        Preconditions.checkNotNull(userUidStr);
        this.peerUid = new UserId(userUidStr);
        Log.i("CallActivity/onCreate peerUid is " + peerUid);
        callViewModel.setPeerUid(peerUid);

        callViewModel.getState().observe(this, state -> {
            callingView.setVisibility(View.GONE);
            ringingView.setVisibility(View.GONE);
            inCallView.setVisibility(View.GONE);
            initView.setVisibility(View.GONE);
            Log.i("CallActivity: State -> " + CallManager.stateToString(state));
            switch (state) {
                case CallManager.State.IDLE:
                    Log.i("CallActivity/State -> IDLE");
                    initView.setVisibility(View.VISIBLE);
                    break;
                case CallManager.State.CALLING:
                    Log.i("CallActivity/State -> CALLING");
                    titleTextView.setText(R.string.calling);
                    callingView.setVisibility(View.VISIBLE);
                    break;
                case CallManager.State.IN_CALL:
                    Log.i("CallActivity/State -> IN_CALL");
                    titleTextView.setText("");
                    inCallView.setVisibility(View.VISIBLE);
                    break;
                case CallManager.State.RINGING:
                    Log.i("CallActivity/State -> RINGING");
                    titleTextView.setText(R.string.ringing);
                    ringingView.setVisibility(View.VISIBLE);
                    break;
                case CallManager.State.END:
                    Log.i("CallActivity/State -> END");
                    Log.i("finishing the activity");
                    callViewModel.onEndCallCleanup();
                    finish();
                    break;
            }
        });

        callViewModel.isPeerRinging().observe(this, isPeerRinging -> {
            if (callViewModel.isCalling()) {
                titleTextView.setText(R.string.ringing);
            }
        });

        callViewModel.getIsMicrophoneMuted().observe(this, this::updateMicrophoneMutedUI);
        callViewModel.getIsSpeakerPhoneOn().observe(this, this::updateSpeakerPhoneUI);

        avatarLoader.load(avatarView, peerUid, false);
        contactLoader.load(nameTextView, peerUid, false);

        findViewById(R.id.in_call_mute).setOnClickListener(v -> {
            if (!callViewModel.inCall()) {
                return;
            }
            onMute();
        });
        findViewById(R.id.in_call_speaker).setOnClickListener(v -> {
            if (!callViewModel.inCall()) {
                return;
            }
            onSpeakerPhone();
        });
        findViewById(R.id.in_call_hangup).setOnClickListener(v -> {
            if (!callViewModel.inCall()) {
                return;
            }
            onHangUp();
        });

        findViewById(R.id.accept_view).setOnClickListener(v -> {
            if (!callViewModel.isRinging()) {
                return;
            }
            checkPermissionsThenAcceptCall();
        });
        findViewById(R.id.decline_view).setOnClickListener(v -> {
            if (!callViewModel.isRinging()) {
                return;
            }
            onDeclineCall();
        });

        findViewById(R.id.calling_cancel).setOnClickListener(v -> {
            if (!callViewModel.isCalling()) {
                return;
            }
            onCancelCall();
        });

        boolean isInitiator = getIntent().getBooleanExtra(EXTRA_IS_INITIATOR, false);
        Log.i("CallActivity: isInitiator " + isInitiator);
        if (isInitiator && callViewModel.isIdle()) {
            Log.i("CallActivity: will start call");
            startCall();
        } else {
            if (ACTION_ACCEPT.equals(getIntent().getAction()) && callViewModel.isRinging()) {
                Log.i("User accepted the call");
                checkPermissionsThenAcceptCall();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        contactLoader.destroy();
    }

    private void startCall() {
        // TODO(nikola): When we want to do video.
        //String[] perms = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        String[] perms = {Manifest.permission.RECORD_AUDIO};
        if (EasyPermissions.hasPermissions(this, perms)) {
            Log.i("CallActivity/startCall we have permissions");
            onStartCall();
        } else {
            Log.i("StartCall needs permissions");
            EasyPermissions.requestPermissions(this, getString(R.string.voice_call_record_audio_permission_rationale), REQUEST_CODE_START_CALL, perms);
        }
    }

    private void checkPermissionsThenAcceptCall() {
        // TODO(nikola): When we want to do video.
        //String[] perms = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        String[] perms = {Manifest.permission.RECORD_AUDIO};
        if (EasyPermissions.hasPermissions(this, perms)) {
            Log.i("CallActivity/acceptCall we have permissions");
            onAcceptCall();
        } else {
            Log.i("AcceptCall needs permissions");
            EasyPermissions.requestPermissions(this, getString(R.string.voice_call_record_audio_permission_rationale), REQUEST_CODE_INCOMING_CALL, perms);
        }
    }

    private void onStartCall() {
        callViewModel.onStartCall();
    }

    private void onDeclineCall() {
        Notifications.getInstance(getApplicationContext()).clearIncomingCallNotification();
        callViewModel.onDeclineCall();
    }

    private void onAcceptCall() {
        Notifications.getInstance(getApplicationContext()).clearIncomingCallNotification();
        callViewModel.onAcceptCall();
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
        if (mute) {
            Log.i("CallActivity muteButton selected");
            muteButtonView.setSelected(true);
            muteButtonView.setBackgroundColor(Color.GRAY);
        } else {
            Log.i("CallActivity muteButton unselected");
            muteButtonView.setSelected(false);
            muteButtonView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private void onSpeakerPhone() {
        Log.i("CallActivity onSpeakerPhone called");
        callViewModel.toggleSpeakerPhone();
    }

    private void updateSpeakerPhoneUI(boolean on) {
        if (on) {
            Log.i("CallActivity speakerButton selected");
            speakerButtonView.setSelected(true);
            speakerButtonView.setBackgroundColor(Color.GRAY);
        } else {
            Log.i("CallActivity speakerButton unselected");
            speakerButtonView.setSelected(false);
            speakerButtonView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private static int getSystemUiVisibility() {
        return View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    }

    public static Intent getStartCallIntent(@NonNull Context context, @NonNull UserId userId) {
        Intent intent = new Intent(context, CallActivity.class);
        intent.putExtra(EXTRA_PEER_UID, userId.rawId());
        intent.putExtra(EXTRA_IS_INITIATOR, true);
        return intent;
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Log.i("Call permissions Granted " + requestCode + " " + perms);
        if (requestCode == REQUEST_CODE_START_CALL && perms.contains(Manifest.permission.RECORD_AUDIO)) {
            onStartCall();
            return;
        }
        if (requestCode == REQUEST_CODE_INCOMING_CALL && perms.contains(Manifest.permission.RECORD_AUDIO)) {
            onAcceptCall();
            return;
        }
        Log.w("Call permissions granted, but nothing is happening after it");
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

    public static Intent incomingCallIntent(@NonNull Context context, @NonNull String callId, @NonNull UserId peerUid) {
        Intent intent = new Intent(context, CallActivity.class);
        intent.putExtra(EXTRA_CALL_ID, callId);
        intent.putExtra(EXTRA_PEER_UID, peerUid.rawId());
        intent.putExtra(EXTRA_IS_INITIATOR, false);
        return intent;
    }

    public static Intent acceptCallIntent(@NonNull Context context, @NonNull String callId, @NonNull UserId peerUid) {
        Intent intent = new Intent(context, CallActivity.class);
        intent.setAction(CallActivity.ACTION_ACCEPT);
        intent.putExtra(EXTRA_CALL_ID, callId);
        intent.putExtra(EXTRA_PEER_UID, peerUid.rawId());
        intent.putExtra(EXTRA_IS_INITIATOR, false);
        return intent;
    }

}

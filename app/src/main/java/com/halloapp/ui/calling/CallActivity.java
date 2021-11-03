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
import com.halloapp.id.UserId;
import com.halloapp.ui.HalloActivity;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class CallActivity extends HalloActivity implements EasyPermissions.PermissionCallbacks, EasyPermissions.RationaleCallbacks  {

    public static final String EXTRA_CALL_ID = "call_id";
    public static final String EXTRA_PEER_UID = "peer_uid";
    public static final String EXTRA_IS_INITIATOR = "is_initiator";

    public static final String ACTION_ACCEPT = "accept";
    private View callingView;
    private View ringingView;
    private View inCallView;
    private View initView;

    private TextView callingTextView;
    private ImageView muteButtonView;
    private ImageView speakerButtonView;

    private CallViewModel callViewModel;
    private UserId peerUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("onCreate");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(getSystemUiVisibility());

        setContentView(R.layout.activity_call);

        callingView = findViewById(R.id.calling_view);
        ringingView = findViewById(R.id.ringing_view);
        inCallView = findViewById(R.id.in_call_view);
        initView = findViewById(R.id.init_view);

        callingTextView = findViewById(R.id.calling_text);
        callingTextView.setText(R.string.calling);
        muteButtonView = findViewById(R.id.in_call_mute);
        speakerButtonView = findViewById(R.id.in_call_speaker);

        callViewModel = new ViewModelProvider(this).get(CallViewModel.class);
        String userUidStr = getIntent().getStringExtra(EXTRA_PEER_UID);
        Preconditions.checkNotNull(userUidStr);
        this.peerUid = new UserId(userUidStr);
        Log.i("peerUid is " + peerUid);
        callViewModel.setPeerUid(peerUid);

        callViewModel.getState().observe(this, state -> {
            callingView.setVisibility(View.GONE);
            ringingView.setVisibility(View.GONE);
            inCallView.setVisibility(View.GONE);
            initView.setVisibility(View.GONE);
            // TODO (nikola): print human name sor the new state
            Log.i("State -> " + state);

            switch (state) {
                case CallManager.State.IDLE:
                    initView.setVisibility(View.VISIBLE);
                    break;
                case CallManager.State.CALLING:
                    callingView.setVisibility(View.VISIBLE);
                    break;
                case CallManager.State.IN_CALL:
                    inCallView.setVisibility(View.VISIBLE);
                    break;
                case CallManager.State.RINGING:
                    ringingView.setVisibility(View.VISIBLE);
                    break;
                case CallManager.State.END:
                    Log.i("finishing the activity");
                    callViewModel.onEndCallCleanup();
                    finish();
                    break;
            }
        });

        callViewModel.isPeerRinging().observe(this, isPeerRinging -> {
            callingTextView.setText(R.string.ringing);
        });

        callViewModel.getIsMicrophoneMuted().observe(this, this::updateMicrophoneMutedUI);
        callViewModel.getIsSpekearPhoneOn().observe(this, this::updateSpeakerPhoneUI);

        findViewById(R.id.init_start).setOnClickListener(v -> startCall());

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
            onAcceptCall();
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
        Log.i("isInitiator " + isInitiator);
        if (isInitiator) {
            Log.i("will start call");
            startCall();
        } else {
            if (ACTION_ACCEPT.equals(getIntent().getAction())) {
                Log.i("User accepted the call");
                onAcceptCall();
            } else {
                callViewModel.onIncomingCall();
            }
        }
    }

    private void startCall() {
        // TODO(nikola): When we want to do video.
        //String[] perms = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        String[] perms = {Manifest.permission.RECORD_AUDIO};
        if (EasyPermissions.hasPermissions(this, perms)) {
            Log.i("CallActivity.startCall we have permissions");
            onStartCall();
        } else {
            Log.i("Call needs permissions");
            // TODO(nikola): Add better rationale
            EasyPermissions.requestPermissions(this, "Need mic perms", 0, perms);
        }
    }

    private void onStartCall() {
        callViewModel.onStart(this.getApplicationContext());
    }

    private void onDeclineCall() {
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
        // TODO(nikola): make peer_uid constant?
        intent.putExtra("peer_uid", userId.rawId());
        intent.putExtra("is_initiator", true);
        return intent;
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Log.i("Call permissions Granted " + requestCode + " " + perms);
        // TODO(nikola): start/answer the call?
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


package com.halloapp.ui.calling;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.lifecycle.ViewModelProvider;

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

    public static final int REQUEST_START_CALL = 1;
    public static final int REQUEST_ANSWER_CALL = 2;

    public static final String ACTION_ACCEPT = "accept";

    private final AvatarLoader avatarLoader = AvatarLoader.getInstance();

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

    private CallViewModel callViewModel;
    private UserId peerUid;

    private ContactLoader contactLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        setContentView(R.layout.activity_call);

        ActionBar actionBar = Preconditions.checkNotNull(getSupportActionBar());
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("");

        contactLoader = new ContactLoader();

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

        callViewModel = new ViewModelProvider(this).get(CallViewModel.class);
        String userUidStr = getIntent().getStringExtra(EXTRA_PEER_UID);
        Preconditions.checkNotNull(userUidStr);
        this.peerUid = new UserId(userUidStr);
        Log.i("CallActivity/onCreate peerUid is " + peerUid);
        callViewModel.setPeerUid(peerUid);

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
                case CallManager.State.IN_CALL:
                    Log.i("CallActivity/State -> IN_CALL");
                    inCallView.setVisibility(View.VISIBLE);
                    callTimerView.setVisibility(View.VISIBLE);
                    titleTextView.setVisibility(View.GONE);
                    startCallTimer();
                    break;
                case CallManager.State.INCOMING_RINGING:
                    Log.i("CallActivity/State -> RINGING");
                    titleTextView.setText(R.string.incoming_call_notification_title);
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

        callViewModel.getIsMicrophoneMuted().observe(this, this::updateMicrophoneMutedUI);
        callViewModel.getIsSpeakerPhoneOn().observe(this, this::updateSpeakerPhoneUI);

        avatarLoader.load(avatarView, peerUid, false);
        contactLoader.load(nameTextView, peerUid, false);

        findViewById(R.id.in_call_mute).setOnClickListener(v -> {
            onMute();
        });
        findViewById(R.id.in_call_speaker).setOnClickListener(v -> {
            onSpeakerPhone();
        });
        findViewById(R.id.in_call_hangup).setOnClickListener(v -> {
            if (callViewModel.isCalling()) {
                onCancelCall();
            } else if (callViewModel.inCall()) {
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

        boolean isInitiator = getIntent().getBooleanExtra(EXTRA_IS_INITIATOR, false);
        Log.i("CallActivity: isInitiator=" + isInitiator);

        if (isInitiator && callViewModel.isIdle()) {
            Log.i("CallActivity: Starting call");
            checkPermissionsThen(REQUEST_START_CALL);
        } else if (ACTION_ACCEPT.equals(getIntent().getAction()) && callViewModel.isRinging()) {
            Log.i("CallActivity: User accepted the call");
            checkPermissionsThen(REQUEST_ANSWER_CALL);
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
    }

    private void checkPermissionsThen(int request) {
        // TODO(nikola): When we want to do video.
        //String[] perms = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        String[] perms = {Manifest.permission.RECORD_AUDIO};
        if (EasyPermissions.hasPermissions(this, perms)) {
            handleRequest(request);
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.voice_call_record_audio_permission_rationale), request, perms);
        }
    }

    private void handleRequest(int request) {
        if (request == REQUEST_START_CALL) {
            onStartCall();
        } else if (request == REQUEST_ANSWER_CALL) {
            onAcceptCall();
        } else {
            Log.w("CallActivity.handleRequest unknown request " + request);
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
            muteButtonView.setImageResource(R.drawable.ic_keyboard_voice);
            muteLabelView.setText(R.string.unmute);
        } else {
            Log.i("CallActivity muteButton unselected");
            muteButtonView.setSelected(false);
            muteButtonView.setImageResource(R.drawable.ic_mic_mute);
            muteLabelView.setText(R.string.mute);
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
            speakerButtonView.setImageResource(R.drawable.ic_speaker_phone_off);
            speakerLabelView.setText(R.string.call_speaker_off_button);
        } else {
            Log.i("CallActivity speakerButton unselected");
            speakerButtonView.setSelected(false);
            speakerButtonView.setImageResource(R.drawable.ic_speaker_phone);
            speakerLabelView.setText(R.string.call_speaker_button);
        }
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
        // TODO(nikola): update here for video calls
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


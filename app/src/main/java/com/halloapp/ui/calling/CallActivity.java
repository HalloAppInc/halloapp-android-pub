package com.halloapp.ui.calling;

import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.halloapp.R;
import com.halloapp.id.UserId;
import com.halloapp.ui.HalloActivity;
import com.halloapp.util.logs.Log;

import pub.devrel.easypermissions.EasyPermissions;

public class CallActivity extends HalloActivity {

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
        String userUidStr = (String) getIntent().getExtras().get("peer_uid");
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
                case CallViewModel.State.STATE_INIT:
                    initView.setVisibility(View.VISIBLE);
                    break;
                case CallViewModel.State.STATE_CALLING:
                    callingView.setVisibility(View.VISIBLE);
                    break;
                case CallViewModel.State.STATE_IN_CALL:
                    inCallView.setVisibility(View.VISIBLE);
                    break;
                case CallViewModel.State.STATE_RINGING:
                    ringingView.setVisibility(View.VISIBLE);
                    break;
                case CallViewModel.State.STATE_END:
                    Log.i("finishing the activity");
                    finish();
                    break;
            }
        });

        callViewModel.isPeerRinging().observe(this, isPeerRinging -> {
            callingTextView.setText(R.string.ringing);
        });

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

        callViewModel.initAudioManager(getApplicationContext());
    }

    private void startCall() {
        // TODO(nikola): When we want to do video.
        //String[] perms = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        String[] perms = {Manifest.permission.RECORD_AUDIO};
        if (EasyPermissions.hasPermissions(this, perms)) {
            onStartCall();
        } else {
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
        callViewModel.onAcceptCall();
    }

    private void onCancelCall() {
        callViewModel.onCancelCall();
    }

    private void onHangUp() {
        callViewModel.onHangUp();
    }

    private void onMute() {
        Log.i("onMute called");
        callViewModel.onMute();
        if (callViewModel.isMuted()) {
            // TODO(nikola): Selected does not seem to do anything
            muteButtonView.setSelected(true);
            muteButtonView.setBackgroundColor(Color.GRAY);
        } else {
            muteButtonView.setSelected(false);
            muteButtonView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private void onSpeakerPhone() {
        callViewModel.onSpeakerPhone();

        if(callViewModel.isSpeakerOn()) {
            Log.i("going to speakerphone");
            speakerButtonView.setSelected(true);
            speakerButtonView.setBackgroundColor(Color.GRAY);
        } else {
            Log.i("going to earpiece");
            speakerButtonView.setSelected(false);
            speakerButtonView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private static int getSystemUiVisibility() {
        return View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    }

    public static Intent getStartCallIntent(Context context, UserId userId) {
        Intent intent = new Intent(context, CallActivity.class);
        // TODO(nikola): make peer_uid constant?
        intent.putExtra("peer_uid", userId.rawId());
        return intent;
    }
}


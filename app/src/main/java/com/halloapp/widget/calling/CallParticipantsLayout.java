package com.halloapp.widget.calling;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.transition.TransitionManager;

import com.halloapp.R;
import com.halloapp.calling.CallManager;
import com.halloapp.calling.ProxyVideoSink;

import org.webrtc.EglBase;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

public class CallParticipantsLayout extends FrameLayout {

    private SurfaceViewRenderer remoteVideoView;
    private SurfaceViewRenderer localVideoView;

    private final ProxyVideoSink remoteProxyVideoSink = new ProxyVideoSink();
    private final ProxyVideoSink localProxyVideoSink = new ProxyVideoSink();

    private int smallViewWidth;
    private int smallViewHeight;
    private int smallViewMargins;

    private boolean inCallView = false;

    public CallParticipantsLayout(@NonNull Context context) {
        this(context, null);
    }

    public CallParticipantsLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CallParticipantsLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    private void init(@NonNull Context context) {
        inflate(context, R.layout.view_video_call_layout, this);

        smallViewWidth = getResources().getDimensionPixelSize(R.dimen.video_call_small_width);
        smallViewHeight = getResources().getDimensionPixelSize(R.dimen.video_call_small_height);
        smallViewMargins = getResources().getDimensionPixelSize(R.dimen.video_call_small_margins);

        remoteVideoView = findViewById(R.id.call_remote_video);
        localVideoView = findViewById(R.id.call_local_video);
        localVideoView.setOnClickListener(v -> {
        });
    }

    public void showInCallView() {
        TransitionManager.beginDelayedTransition(this);
        FrameLayout.LayoutParams localParams = (FrameLayout.LayoutParams) localVideoView.getLayoutParams();
        localParams.height = smallViewHeight;
        localParams.width = smallViewWidth;
        localParams.setMargins(smallViewMargins, smallViewMargins, smallViewMargins, smallViewMargins);
        localVideoView.setLayoutParams(localParams);
        remoteVideoView.setVisibility(View.VISIBLE);
        inCallView = true;
    }

    public void bind(@NonNull CallManager callManager) {
        final EglBase eglBase = callManager.getEglBase();

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

    public void translateLocalView(float y, int duration) {
        if (inCallView) {
            if (localVideoView != null) {
                localVideoView.animate()
                        .translationY(y)
                        .setDuration(duration);
            }
        }
    }

    public void destroy() {
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
}

package com.halloapp.widget.calling.calling;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.transition.TransitionManager;

import com.halloapp.Constants;
import com.halloapp.Preferences;
import com.halloapp.R;
import com.halloapp.calling.calling.CallManager;
import com.halloapp.calling.calling.ProxyVideoSink;
import com.halloapp.util.ViewUtils;
import com.halloapp.webrtc.HATextureViewRenderer;

import org.webrtc.EglBase;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class CallParticipantsLayout extends FrameLayout {

    private SurfaceViewRenderer remoteVideoView;
    private HATextureViewRenderer localVideoView;
    private View mutedRemoteOverlayView;

    private View localVideoViewContainer;
    private View mutedLocalOverlayView;
    private View mutedIconView;

    private final ProxyVideoSink remoteProxyVideoSink = new ProxyVideoSink();
    private final ProxyVideoSink localProxyVideoSink = new ProxyVideoSink();

    private int smallViewWidth;
    private int smallViewHeight;
    private int smallViewMargins;

    private boolean inCallView = false;
    private boolean mirrorLocal = true;

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

    private float startX;
    private float startY;

    private int localQuadrant;

    private int bottomMargin = 0;

    @SuppressLint("ClickableViewAccessibility")
    private void init(@NonNull Context context) {
        inflate(context, R.layout.view_video_call_layout, this);

        smallViewWidth = getResources().getDimensionPixelSize(R.dimen.video_call_small_width);
        smallViewHeight = getResources().getDimensionPixelSize(R.dimen.video_call_small_height);
        smallViewMargins = getResources().getDimensionPixelSize(R.dimen.video_call_small_margins);

        localVideoViewContainer = findViewById(R.id.local_video_container);
        mutedLocalOverlayView = findViewById(R.id.muted_local_overlay);
        mutedIconView = findViewById(R.id.muted_icon);
        mutedRemoteOverlayView = findViewById(R.id.remote_video_mute_overlay);

        remoteVideoView = findViewById(R.id.call_remote_video);
        localVideoView = findViewById(R.id.call_local_video);
        localVideoViewContainer.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                startX = event.getRawX();
                startY = event.getRawY();
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                localVideoViewContainer.setTranslationX(event.getRawX() - startX);
                localVideoViewContainer.setTranslationY(event.getRawY() - startY);
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                lockLocalToNearestCorner();
            }
            return false;
        });

        localQuadrant = Preferences.getInstance().getLocalVideoViewQuadrant();
    }

    private void lockLocalToNearestCorner() {
        int viewWidth = getWidth();
        int viewHeight = getHeight();

        float localCenterX = localVideoViewContainer.getX() + (localVideoViewContainer.getWidth() / 2f);
        float localCenterY = localVideoViewContainer.getY() + (localVideoViewContainer.getHeight() / 2f);

        float centerX = viewWidth / 2f;
        float centerY = viewHeight / 2f;

        float dX = localCenterX - centerX;
        float dY = localCenterY - centerY;

        if (dX >= 0) {
            updateQuadrant(dY >= 0 ? Constants.Quadrant.BOTTOM_RIGHT : Constants.Quadrant.TOP_RIGHT);
        } else {
            updateQuadrant(dY >= 0 ? Constants.Quadrant.BOTTOM_LEFT : Constants.Quadrant.TOP_LEFT);
        }
    }

    private void updateQuadrant(@Constants.Quadrant int newQuad) {
        if (newQuad != localQuadrant) {
            localQuadrant = newQuad;
            Preferences.getInstance().applyLocalVideoViewQuadrant(localQuadrant);
        }
        lockToQuadrant(localQuadrant, true);
    }

    @SuppressLint("RtlHardcoded")
    private void lockToQuadrant(int corner, boolean animate) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) localVideoViewContainer.getLayoutParams();
        float destX = 0;
        float destY = 0;
        switch (corner) {
            case Constants.Quadrant.TOP_LEFT: {
                layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
                destX = layoutParams.leftMargin;
                destY = layoutParams.topMargin;
                break;
            }
            case Constants.Quadrant.TOP_RIGHT: {
                layoutParams.gravity = Gravity.TOP | Gravity.RIGHT;
                destX = getWidth() - localVideoViewContainer.getWidth() - layoutParams.rightMargin;
                destY = layoutParams.topMargin;
                break;
            }
            case Constants.Quadrant.BOTTOM_RIGHT: {
                layoutParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
                destX = getWidth() - localVideoViewContainer.getWidth() - layoutParams.rightMargin;
                destY = getHeight() - layoutParams.bottomMargin - localVideoViewContainer.getHeight();
                break;
            }
            case Constants.Quadrant.BOTTOM_LEFT: {
                layoutParams.gravity = Gravity.BOTTOM | Gravity.LEFT;
                destX = layoutParams.leftMargin;
                destY = getHeight() - layoutParams.bottomMargin - localVideoViewContainer.getHeight();
                break;
            }
        }
        if (animate) {
            localVideoViewContainer.setTranslationY(localVideoViewContainer.getY() - destY);
            localVideoViewContainer.setTranslationX(localVideoViewContainer.getX() - destX);
            localVideoViewContainer.animate().translationY(0).translationX(0).setDuration(200);
        } else {
            localVideoViewContainer.setTranslationX(0);
            localVideoViewContainer.setTranslationY(0);
        }
        localVideoViewContainer.setLayoutParams(layoutParams);
    }

    public void enterPiPView() {
        localVideoViewContainer.setVisibility(View.GONE);
    }

    public void exitPiPView() {
        localVideoViewContainer.setVisibility(View.VISIBLE);
    }

    public void showInCallView() {
        TransitionManager.beginDelayedTransition(this);
        FrameLayout.LayoutParams localParams = (FrameLayout.LayoutParams) localVideoViewContainer.getLayoutParams();
        localParams.height = smallViewHeight;
        localParams.width = smallViewWidth;
        localParams.setMargins(smallViewMargins, smallViewMargins, smallViewMargins, smallViewMargins + bottomMargin);
        localVideoViewContainer.setLayoutParams(localParams);
        remoteVideoView.setVisibility(View.VISIBLE);
        ViewUtils.clipRoundedRect(localVideoViewContainer, R.dimen.call_local_preview_corner_radius);

        lockToQuadrant(localQuadrant, false);
        inCallView = true;
    }

    public void onRemoteVideoMuted(boolean muted) {
        remoteVideoView.setVisibility(muted ? View.GONE : View.VISIBLE);
        mutedRemoteOverlayView.setVisibility(muted ? View.VISIBLE : View.GONE);
    }

    public void onMicMuted(boolean muted) {
        mutedIconView.setVisibility(muted ? View.VISIBLE : View.GONE);
    }

    public void onLocalCameraMute() {
        localVideoView.clearImage();
        mutedLocalOverlayView.setVisibility(View.VISIBLE);
    }

    public void onLocalCameraUnmute() {
        mutedLocalOverlayView.setVisibility(View.GONE);
    }

    public void bind(@NonNull CallManager callManager) {
        final EglBase eglBase = callManager.getEglBase();

        localVideoView.init(eglBase.getEglBaseContext(), null);
        localVideoView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);

        remoteVideoView.init(eglBase.getEglBaseContext(), null);
        remoteVideoView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        remoteVideoView.setZOrderMediaOverlay(false);

        localVideoView.setEnableHardwareScaler(true);
        localVideoView.setMirror(mirrorLocal);
        remoteVideoView.setEnableHardwareScaler(true);

        remoteProxyVideoSink.setTarget(remoteVideoView);
        localProxyVideoSink.setTarget(localVideoView);

        callManager.setVideoSinks(localProxyVideoSink, remoteProxyVideoSink);
    }

    public void setMirrorLocal(boolean mirrorLocal) {
        if (mirrorLocal != this.mirrorLocal) {
            this.mirrorLocal = mirrorLocal;
            localVideoView.setMirror(mirrorLocal);
        }
    }

    public void updateLocalViewBottomMargin(float y, int duration) {
        if (inCallView) {
            if (localVideoViewContainer != null) {
                localVideoViewContainer.clearAnimation();
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) localVideoViewContainer.getLayoutParams();
                int bottomMarginStart = lp.bottomMargin;
                int bottomMarginEnd = (int) (smallViewMargins + y);
                Animation animation = new Animation() {
                    @Override
                    protected void applyTransformation(float interpolatedTime, Transformation t) {
                        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) localVideoViewContainer.getLayoutParams();
                        layoutParams.bottomMargin = bottomMarginStart + (int) ((bottomMarginEnd - bottomMarginStart) * interpolatedTime);
                        localVideoViewContainer.setLayoutParams(layoutParams);
                    }
                };
                bottomMargin = (int) y;
                animation.setDuration(duration);
                localVideoViewContainer.startAnimation(animation);
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

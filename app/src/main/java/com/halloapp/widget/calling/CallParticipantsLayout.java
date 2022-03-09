package com.halloapp.widget.calling;

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

import com.halloapp.Preferences;
import com.halloapp.R;
import com.halloapp.calling.CallManager;
import com.halloapp.calling.ProxyVideoSink;

import org.webrtc.EglBase;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class CallParticipantsLayout extends FrameLayout {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Quadrant.TOP_LEFT, Quadrant.TOP_RIGHT, Quadrant.BOTTOM_LEFT, Quadrant.BOTTOM_RIGHT, })
    public @interface Quadrant {
        int TOP_RIGHT = 0;
        int TOP_LEFT = 1;
        int BOTTOM_LEFT = 2;
        int BOTTOM_RIGHT = 3;
    }

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

        remoteVideoView = findViewById(R.id.call_remote_video);
        localVideoView = findViewById(R.id.call_local_video);
        localVideoView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                startX = event.getRawX();
                startY = event.getRawY();
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                localVideoView.setTranslationX(event.getRawX() - startX);
                localVideoView.setTranslationY(event.getRawY() - startY);
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

        float localCenterX = localVideoView.getX() + (localVideoView.getWidth() / 2f);
        float localCenterY = localVideoView.getY() + (localVideoView.getHeight() / 2f);

        float centerX = viewWidth / 2f;
        float centerY = viewHeight / 2f;

        float dX = localCenterX - centerX;
        float dY = localCenterY - centerY;

        if (dX >= 0) {
            updateQuadrant(dY >= 0 ? Quadrant.BOTTOM_RIGHT : Quadrant.TOP_RIGHT);
        } else {
            updateQuadrant(dY >= 0 ? Quadrant.BOTTOM_LEFT : Quadrant.TOP_LEFT);
        }
    }

    private void updateQuadrant(@Quadrant int newQuad) {
        if (newQuad != localQuadrant) {
            localQuadrant = newQuad;
            Preferences.getInstance().applyLocalVideoViewQuadrant(localQuadrant);
        }
        lockToQuadrant(localQuadrant, true);
    }

    private void lockToQuadrant(int corner, boolean animate) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) localVideoView.getLayoutParams();
        float destX = 0;
        float destY = 0;
        switch (corner) {
            case Quadrant.TOP_LEFT: {
                layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
                destX = layoutParams.leftMargin;
                destY = layoutParams.topMargin;
                break;
            }
            case Quadrant.TOP_RIGHT: {
                layoutParams.gravity = Gravity.TOP | Gravity.RIGHT;
                destX = getWidth() - localVideoView.getWidth() - layoutParams.rightMargin;
                destY = layoutParams.topMargin;
                break;
            }
            case Quadrant.BOTTOM_RIGHT: {
                layoutParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
                destX = getWidth() - localVideoView.getWidth() - layoutParams.rightMargin;
                destY = getHeight() - layoutParams.bottomMargin - localVideoView.getHeight();
                break;
            }
            case Quadrant.BOTTOM_LEFT: {
                layoutParams.gravity = Gravity.BOTTOM | Gravity.LEFT;
                destX = layoutParams.leftMargin;
                destY = getHeight() - layoutParams.bottomMargin - localVideoView.getHeight();
                break;
            }
        }
        if (animate) {
            localVideoView.setTranslationY(localVideoView.getY() - destY);
            localVideoView.setTranslationX(localVideoView.getX() - destX);
            localVideoView.animate().translationY(0).translationX(0).setDuration(200);
        } else {
            localVideoView.setTranslationX(0);
            localVideoView.setTranslationY(0);
        }
        localVideoView.setLayoutParams(layoutParams);
    }

    public void enterPiPView() {
        localVideoView.setVisibility(View.GONE);
    }

    public void exitPiPView() {
        localVideoView.setVisibility(View.VISIBLE);
    }

    public void showInCallView() {
        TransitionManager.beginDelayedTransition(this);
        FrameLayout.LayoutParams localParams = (FrameLayout.LayoutParams) localVideoView.getLayoutParams();
        localParams.height = smallViewHeight;
        localParams.width = smallViewWidth;
        localParams.setMargins(smallViewMargins, smallViewMargins, smallViewMargins, smallViewMargins + bottomMargin);
        localVideoView.setLayoutParams(localParams);
        remoteVideoView.setVisibility(View.VISIBLE);

        lockToQuadrant(localQuadrant, false);
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

    public void updateLocalViewBottomMargin(float y, int duration) {
        if (inCallView) {
            if (localVideoView != null) {
                localVideoView.clearAnimation();
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) localVideoView.getLayoutParams();
                int bottomMarginStart = lp.bottomMargin;
                int bottomMarginEnd = (int) (smallViewMargins + y);
                Animation animation = new Animation() {
                    @Override
                    protected void applyTransformation(float interpolatedTime, Transformation t) {
                        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) localVideoView.getLayoutParams();
                        layoutParams.bottomMargin = bottomMarginStart + (int) ((bottomMarginEnd - bottomMarginStart) * interpolatedTime);
                        localVideoView.setLayoutParams(layoutParams);
                    }
                };
                bottomMargin = (int) y;
                animation.setDuration(duration);
                localVideoView.startAnimation(animation);
            }
        }
    }

    // TODO (nikola): instead of clearing we should blur it.
    public void onLocalCameraMute() {
        localVideoView.clearImage();
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

package com.halloapp.ui.calling;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.SystemClock;
import android.view.View;

public class VideoCallControlsController implements View.OnClickListener {

    private static final int CONTROLS_FADE_INITIAL_DELAY_MS = 2000;
    private static final int CONTROLS_FADE_ON_CLICK_DELAY_MS = 5000;

    private final View topContainerView;
    private final View controlsContainerView;

    private final int shortAnimationDuration;

    private long lastInteraction;

    public VideoCallControlsController(View headerView, View controlsView) {
        this.topContainerView = headerView;
        this.controlsContainerView = controlsView;

        shortAnimationDuration = headerView.getResources().getInteger(android.R.integer.config_mediumAnimTime);
    }

    private final Runnable hideControlsRunnable = this::animateOutControls;

    private boolean controlsShowing = true;

    public void onCallStart() {
        controlsContainerView.removeCallbacks(hideControlsRunnable);
        controlsContainerView.postDelayed(hideControlsRunnable, CONTROLS_FADE_INITIAL_DELAY_MS);
    }

    public void showControlsForever() {
        controlsContainerView.removeCallbacks(hideControlsRunnable);
        showControls();
    }

    private void showControls() {
        clearAnimations();
        topContainerView.setAlpha(1f);
        topContainerView.setVisibility(View.VISIBLE);
        controlsContainerView.setAlpha(1f);
        controlsContainerView.setVisibility(View.VISIBLE);
    }

    private void animateOutControls() {
        clearAnimations();
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
        controlsShowing = false;
    }

    private void clearAnimations() {
        topContainerView.clearAnimation();
        controlsContainerView.clearAnimation();
    }

    private void animateInControls() {
        clearAnimations();
        topContainerView.setVisibility(View.VISIBLE);
        controlsContainerView.setVisibility(View.VISIBLE);

        topContainerView.animate()
                .alpha(1f)
                .setDuration(shortAnimationDuration)
                .setListener(null);
        controlsContainerView.animate()
                .alpha(1f)
                .setDuration(shortAnimationDuration)
                .setListener(null);
        controlsShowing = true;
    }

    @Override
    public void onClick(View v) {
        if (SystemClock.elapsedRealtime() - lastInteraction > 500) {
            lastInteraction = SystemClock.elapsedRealtime();
        } else {
            return;
        }
        if (controlsShowing) {
            animateOutControls();
            controlsContainerView.removeCallbacks(hideControlsRunnable);
        } else {
            animateInControls();
            controlsContainerView.removeCallbacks(hideControlsRunnable);
            controlsContainerView.postDelayed(hideControlsRunnable, CONTROLS_FADE_ON_CLICK_DELAY_MS);
        }
    }
}

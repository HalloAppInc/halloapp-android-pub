package com.halloapp.ui.calling.calling;

import android.os.SystemClock;
import android.view.View;

import androidx.annotation.NonNull;

import com.halloapp.widget.calling.calling.CallParticipantsLayout;

public class VideoCallControlsController implements View.OnClickListener {

    private static final int CONTROLS_FADE_INITIAL_DELAY_MS = 2000;
    private static final int CONTROLS_FADE_ON_CLICK_DELAY_MS = 5000;

    private final View titleView;
    private final View topContainerView;
    private final View controlsContainerView;

    private CallParticipantsLayout callParticipantsLayout;

    private final int shortAnimationDuration;

    private long lastInteraction;
    private boolean showControlsForever = false;

    public VideoCallControlsController(View titleView, View headerView, View controlsView) {
        this.titleView = titleView;
        this.topContainerView = headerView;
        this.controlsContainerView = controlsView;

        shortAnimationDuration = headerView.getResources().getInteger(android.R.integer.config_mediumAnimTime);
    }

    private final Runnable hideControlsRunnable = this::animateOutControls;

    private boolean controlsShowing = true;

    public void bindParticipantsView(@NonNull CallParticipantsLayout callParticipantsLayout) {
        this.callParticipantsLayout = callParticipantsLayout;

        callParticipantsLayout.setOnClickListener(this);
    }

    public void onCallStart() {
        controlsContainerView.removeCallbacks(hideControlsRunnable);
        controlsContainerView.postDelayed(hideControlsRunnable, CONTROLS_FADE_INITIAL_DELAY_MS);
        callParticipantsLayout.showInCallView();
        callParticipantsLayout.updateLocalViewBottomMargin(controlsContainerView.getHeight(), shortAnimationDuration);
        showControlsForever = false;
    }

    public void showControlsForever() {
        controlsContainerView.removeCallbacks(hideControlsRunnable);
        showControls();
        showControlsForever = true;
    }

    public void hideControlsDelayed() {
        controlsContainerView.removeCallbacks(hideControlsRunnable);
        controlsContainerView.postDelayed(hideControlsRunnable, CONTROLS_FADE_ON_CLICK_DELAY_MS);
        showControlsForever = false;
    }

    public void hideControls() {
        controlsContainerView.removeCallbacks(hideControlsRunnable);
        animateOutControls();
        showControlsForever = false;
    }

    private void showControls() {
        animateInControls();
    }

    private void animateOutControls() {
        int height = controlsContainerView.getHeight();
        callParticipantsLayout.updateLocalViewBottomMargin(0, shortAnimationDuration);
        topContainerView.animate()
                .alpha(0f)
                .setDuration(shortAnimationDuration);
        titleView.animate()
                .alpha(0f)
                .setDuration(shortAnimationDuration);
        controlsContainerView.animate()
                .translationY(height)
                .setDuration(shortAnimationDuration)
                .start();
        controlsShowing = false;
    }

    private void animateInControls() {
        int height = controlsContainerView.getHeight();
        callParticipantsLayout.updateLocalViewBottomMargin(height, shortAnimationDuration);
        topContainerView.animate()
                .alpha(1f)
                .setDuration(shortAnimationDuration);
        titleView.animate()
                .alpha(1f)
                .setDuration(shortAnimationDuration);
        controlsContainerView.animate()
                .translationY(0)
                .setDuration(shortAnimationDuration)
                .start();
        controlsShowing = true;
    }

    @Override
    public void onClick(View v) {
        if (showControlsForever) {
            return;
        }
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

package com.halloapp.katchup;

import android.animation.Animator;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.R;
import com.halloapp.ui.HalloFragment;

public class OnboardingSplashFragment extends HalloFragment {
    public interface OnSplashFadedHandler {
        void onSplashFaded();
    }

    private static final int SPLASH_FADE_IN_DURATION = 250;
    private static final int SPLASH_FADE_OUT_DURATION = 250;
    private static final int SPLASH_VISIBLE_DURATION = 1000;

    private static final String STATE_ANIMATION_IS_FINISHED = "animation_is_finished";

    private View root;
    private View splashImage;
    private boolean animationIsFinished;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_splash_screen, container, false);
        splashImage = root.findViewById(R.id.splash_image);

        animationIsFinished = savedInstanceState != null && savedInstanceState.getBoolean(STATE_ANIMATION_IS_FINISHED);
        if (animationIsFinished) {
            removeSplash();
        } else {
            animateSplashFadeIn();
            animateSplashFadeOut();
        }

        return root;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(STATE_ANIMATION_IS_FINISHED, animationIsFinished);
        super.onSaveInstanceState(outState);
    }

    private void animateSplashFadeIn() {
        splashImage.animate()
                .alpha(1)
                .setDuration(SPLASH_FADE_IN_DURATION)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        root.setBackgroundColor(Color.TRANSPARENT);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                }).start();
    }

    private void animateSplashFadeOut() {
        splashImage.postDelayed(() -> {
            splashImage.animate()
                    .alpha(0)
                    .setDuration(SPLASH_FADE_OUT_DURATION)
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            animationIsFinished = true;
                            removeSplash();
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {
                        }
                    }).start();
        }, SPLASH_VISIBLE_DURATION);
    }

    private void removeSplash() {
        root.setVisibility(View.GONE);
        final OnSplashFadedHandler handler = (OnSplashFadedHandler) getActivity();
        if (handler != null) {
            handler.onSplashFaded();
        }
    }

}

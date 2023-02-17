package com.halloapp.katchup;

import android.animation.Animator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.R;
import com.halloapp.ui.HalloFragment;

public class SplashScreenFragment extends HalloFragment {
    public interface OnSplashFadedHandler {
        void onSplashFaded();
    }

    private static final int SPLASH_FADE_OUT_DURATION = 500;
    private static final int SPLASH_VISIBLE_DURATION = 2500;

    private View splashImage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_splash_screen, container, false);

        splashImage = root.findViewById(R.id.splash_image);
        animateSplashFadeOut();

        return root;
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
                            splashImage.setVisibility(View.GONE);
                            final OnSplashFadedHandler handler = (OnSplashFadedHandler) getActivity();
                            if (handler != null) {
                                handler.onSplashFaded();
                            }
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {
                        }}).start();
        }, SPLASH_VISIBLE_DURATION);
    }

}

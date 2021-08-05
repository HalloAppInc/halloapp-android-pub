package com.halloapp.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.github.chrisbanes.photoview.PhotoView;
import com.halloapp.R;

public class DragDownToDismissHelper {

    private final PhotoView avatarView;
    private final View main;

    private final int swipeExitStartThreshold;
    private final float swipeExitTransDistance;

    private MotionEvent swipeExitStart;
    private boolean isSwipeExitInProgress = false;

    public interface DragDismissListener {
        void onDismiss();
    }

    private DragDismissListener listener;

    public DragDownToDismissHelper(PhotoView avatarView, View backgroundView) {
        this.avatarView = avatarView;
        this.main = backgroundView;

        swipeExitStartThreshold = avatarView.getResources().getDimensionPixelSize(R.dimen.swipe_exit_start_threshold);
        swipeExitTransDistance = avatarView.getResources().getDimension(R.dimen.swipe_exit_transition_distance);
    }

    public void setDragDismissListener(@Nullable DragDismissListener listener) {
        this.listener = listener;
    }

    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (event.getPointerCount() == 1 && shouldAllowSwipeExit()) {
                    swipeExitStart = MotionEvent.obtain(event);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (swipeExitStart != null && event.getPointerCount() > 1) {
                    cancelSwipeExit();
                } else if (isSwipeExitInProgress) {
                    onSwipeExitMove(event);
                } else if (swipeExitStart != null) {
                    float distanceX = Math.abs(event.getX() - swipeExitStart.getX());
                    float distanceY = Math.abs(event.getY() - swipeExitStart.getY());

                    if (distanceY > swipeExitStartThreshold && distanceY > distanceX) {
                        isSwipeExitInProgress = true;
                        onSwipeExitMove(event);
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                cancelSwipeExit();
                break;
            case MotionEvent.ACTION_UP:
                if (swipeExitStart != null) {
                    float distanceY = Math.abs(event.getY() - swipeExitStart.getY());

                    if (isSwipeExitInProgress && distanceY > Math.min(avatarView.getWidth(), avatarView.getHeight()) / 3f) {
                        finishSwipeExit();
                    } else {
                        cancelSwipeExit();
                    }
                }
                break;
        }

        return isSwipeExitInProgress;
    }

    private void onSwipeExitMove(MotionEvent event) {
        if (swipeExitStart != null && isSwipeExitInProgress) {
            final float swipeExitAlpha = 0.3f;

            float distanceY = event.getY() - swipeExitStart.getY();
            float progress = Math.min((distanceY ) / (swipeExitTransDistance), 1.0f);
            float scale = 1;
            float alpha = 1 - progress + swipeExitAlpha * progress;

            View view = avatarView;
            view.setTranslationY(distanceY);
            view.setScaleX(scale);
            view.setScaleY(scale);

            main.setAlpha(alpha);
        }
    }

    private void cancelSwipeExit() {
        if (swipeExitStart != null && isSwipeExitInProgress) {
            View view = avatarView;

            AnimatorSet set = new AnimatorSet();
            set.play(ObjectAnimator.ofFloat(main, "alpha", main.getAlpha(), 1.0f))
                    .with(ObjectAnimator.ofFloat(view, "translationX", view.getTranslationX(), 0f))
                    .with(ObjectAnimator.ofFloat(view, "translationY", view.getTranslationY(), 0f))
                    .with(ObjectAnimator.ofFloat(view, "scaleX", view.getScaleX(), 1.0f))
                    .with(ObjectAnimator.ofFloat(view, "scaleY", view.getScaleY(), 1.0f));
            set.setDuration(300);
            set.start();
        }

        swipeExitStart = null;
        isSwipeExitInProgress = false;
    }

    private void finishSwipeExit() {
        if (listener != null) {
            listener.onDismiss();
        }
    }

    private boolean shouldAllowSwipeExit() {
        if (avatarView != null) {
            return Math.abs(avatarView.getScale() - 1.0) < 0.2;
        }
        return true;
    }

}

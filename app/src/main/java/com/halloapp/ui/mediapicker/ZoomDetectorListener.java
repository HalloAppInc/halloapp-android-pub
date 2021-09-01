package com.halloapp.ui.mediapicker;

import android.view.ScaleGestureDetector;

import com.halloapp.util.Preconditions;

public class ZoomDetectorListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
    private static final int ZOOM_IN = 1;
    private static final int ZOOM_OUT = 2;

    private final ZoomAnimator animator;
    private final MediaPickerViewModel viewModel;
    private int direction = 0;

    public ZoomDetectorListener(ZoomAnimator animator, MediaPickerViewModel viewModel) {
        this.animator = animator;
        this.viewModel = viewModel;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        super.onScaleEnd(detector);
        animator.endManualAnimations();
    }

    private void zoomIn() {
        switch (Preconditions.checkNotNull(viewModel.getLayout().getValue())) {
            case MediaPickerViewModel.LAYOUT_MONTH: {
                viewModel.setLayout(MediaPickerViewModel.LAYOUT_DAY_SMALL);
                break;
            }
            case MediaPickerViewModel.LAYOUT_DAY_SMALL: {
                viewModel.setLayout(MediaPickerViewModel.LAYOUT_DAY_LARGE);
                break;
            }
        }
    }

    private void zoomOut() {
        switch (Preconditions.checkNotNull(viewModel.getLayout().getValue())) {
            case MediaPickerViewModel.LAYOUT_DAY_LARGE: {
                viewModel.setLayout(MediaPickerViewModel.LAYOUT_DAY_SMALL);
                break;
            }
            case MediaPickerViewModel.LAYOUT_DAY_SMALL: {
                viewModel.setLayout(MediaPickerViewModel.LAYOUT_MONTH);
                break;
            }
        }
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        if (detector.getScaleFactor() == 1.0) {
            return true;
        }

        if (!animator.isManualAnimationRunning()) {
            animator.beginManualAnimations();

            if (detector.getScaleFactor() < 1) {
                direction = ZOOM_OUT;
                zoomOut();
            } else {
                direction = ZOOM_IN;
                zoomIn();
            }
        } else {
            if (detector.getScaleFactor() > 1 && direction == ZOOM_IN) {
                animator.goForward();
            } else if (detector.getScaleFactor() < 1 && direction == ZOOM_IN) {
                animator.goBackward();
            } else if (detector.getScaleFactor() > 1 && direction == ZOOM_OUT) {
                animator.goBackward();
            } else if (detector.getScaleFactor() < 1 && direction == ZOOM_OUT) {
                animator.goForward();
            }
        }

        return true;
    }
}

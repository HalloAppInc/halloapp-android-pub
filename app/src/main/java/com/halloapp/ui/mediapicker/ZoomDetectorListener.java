package com.halloapp.ui.mediapicker;

import android.view.ScaleGestureDetector;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ZoomDetectorListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
    private static final int ZOOM_IN = 1;
    private static final int ZOOM_OUT = 2;

    private RecyclerView view;
    private int direction = 0;

    public ZoomDetectorListener(RecyclerView view) {
        this.view = view;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        super.onScaleEnd(detector);
        getAnimator().endManualAnimations();
    }

    private ZoomAnimator getAnimator() {
        return (ZoomAnimator) view.getItemAnimator();
    }

    private MediaPickerActivity.MediaItemsAdapter getAdapter() {
        return (MediaPickerActivity.MediaItemsAdapter) view.getAdapter();
    }

    private GridLayoutManager getLayoutManager() {
        return (GridLayoutManager) view.getLayoutManager();
    }

    private void zoomIn() {
        final MediaPickerActivity.MediaItemsAdapter adapter = getAdapter();
        final GridLayoutManager manager = getLayoutManager();
        final int layout = getAdapter().getGridLayout();

        switch (layout) {
            case MediaPickerActivity.MediaItemsAdapter.LAYOUT_MONTH: {
                adapter.setGridLayout(MediaPickerActivity.MediaItemsAdapter.LAYOUT_DAY_SMALL);
                manager.setSpanCount(MediaPickerActivity.MediaItemsAdapter.SPAN_COUNT_DAY_SMALL);
                manager.requestLayout();
                adapter.notifyDataSetChanged();
                break;
            }
            case MediaPickerActivity.MediaItemsAdapter.LAYOUT_DAY_SMALL: {
                adapter.setGridLayout(MediaPickerActivity.MediaItemsAdapter.LAYOUT_DAY_LARGE);
                manager.setSpanCount(MediaPickerActivity.MediaItemsAdapter.SPAN_COUNT_DAY_LARGE);
                manager.requestLayout();
                adapter.notifyDataSetChanged();
                break;
            }
        }
    }

    private void zoomOut() {
        final MediaPickerActivity.MediaItemsAdapter adapter = getAdapter();
        final GridLayoutManager manager = getLayoutManager();
        final int layout = getAdapter().getGridLayout();

        switch (layout) {
            case MediaPickerActivity.MediaItemsAdapter.LAYOUT_DAY_LARGE: {
                adapter.setGridLayout(MediaPickerActivity.MediaItemsAdapter.LAYOUT_DAY_SMALL);
                manager.setSpanCount(MediaPickerActivity.MediaItemsAdapter.SPAN_COUNT_DAY_SMALL);
                manager.requestLayout();
                adapter.notifyDataSetChanged();
                break;
            }
            case MediaPickerActivity.MediaItemsAdapter.LAYOUT_DAY_SMALL: {
                adapter.setGridLayout(MediaPickerActivity.MediaItemsAdapter.LAYOUT_MONTH);
                manager.setSpanCount(MediaPickerActivity.MediaItemsAdapter.SPAN_COUNT_MONTH);
                manager.requestLayout();
                adapter.notifyDataSetChanged();
                break;
            }
        }
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        if (detector.getScaleFactor() == 1.0) {
            return true;
        }

        final ZoomAnimator animator = getAnimator();

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

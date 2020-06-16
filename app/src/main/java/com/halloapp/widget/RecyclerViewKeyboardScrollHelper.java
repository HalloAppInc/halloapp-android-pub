package com.halloapp.widget;

import android.view.View;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.BuildConfig;
import com.halloapp.util.Log;

/**
 * Helper class for maintaining a consistent scroll position when the keyboard is opened
 * or closed (or when other views push up the recycler view)
 *
 * Will make sure the anchor view (typically bottom view) stays in the view
 */
@MainThread
public class RecyclerViewKeyboardScrollHelper {

    private int anchorBottom;
    private int anchorPosition;
    private int anchorBottomOffset;

    private int nextAnchorPosition;
    private int nextAnchorBottom;

    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;

    public RecyclerViewKeyboardScrollHelper(@NonNull RecyclerView recyclerView) {
        if (!(recyclerView.getLayoutManager() instanceof LinearLayoutManager)) {
            if (BuildConfig.DEBUG) {
                throw new IllegalArgumentException("RecyclerViewKeyboardScrollHelper only supports LinearLayoutManagers");
            }
            return;
        }
        this.recyclerView = recyclerView;
        this.layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

        init();
    }

    public void setAnchorForKeyboardChange(int position) {
        View nextAnchor = layoutManager.findViewByPosition(position);
        if (nextAnchor != null) {
            nextAnchorPosition = position;
            nextAnchorBottom = nextAnchor.getBottom();
        }
    }

    private void init() {
        // Listen for scroll events to recompute anchor position based on bottom
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int lastItem = layoutManager.findLastVisibleItemPosition();
                    View child = layoutManager.findViewByPosition(lastItem);
                    if (child != null) {
                        anchorPosition = lastItem;
                        anchorBottom = child.getBottom();
                        anchorBottomOffset = anchorBottom - recyclerView.getBottom();
                    } else {
                        Log.d("RecyclerViewKeyboardScrollHelper/scrollListener Failed to set anchor position");
                    }
                }
            }
        });
        recyclerView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            int heightChange = bottom - oldBottom;
            if (nextAnchorPosition != -1) {
                anchorPosition = nextAnchorPosition;
                anchorBottom = nextAnchorBottom;
                anchorBottomOffset = 0;
                nextAnchorPosition = -1;
            }
            int adjustedAnchorBottom = anchorBottom;
            if (heightChange > 0) {
                View anchor = layoutManager.findViewByPosition(anchorPosition);
                if (anchor != null) {
                    adjustedAnchorBottom = anchor.getBottom();
                }
            }
            int maxBottom = bottom + anchorBottomOffset;
            if (heightChange > 0 || adjustedAnchorBottom > maxBottom) {
                recyclerView.scrollBy(0, adjustedAnchorBottom - maxBottom);
                anchorBottom = maxBottom;
            }
        });
    }

}

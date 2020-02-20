package com.halloapp.widget;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class LinearSpacingItemDecoration extends RecyclerView.ItemDecoration {

    private LinearLayoutManager layoutManager;
    private int spacing;

    public LinearSpacingItemDecoration(@NonNull LinearLayoutManager layoutManager, int spacing) {
        this.layoutManager = layoutManager;
        this.spacing = spacing;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        if (layoutManager.getOrientation() == RecyclerView.HORIZONTAL) {
            final RecyclerView.Adapter adapter = parent.getAdapter();
            if (adapter != null) {
                outRect.left = position * spacing / adapter.getItemCount();
                outRect.right = spacing - (position + 1) * spacing / adapter.getItemCount();
            }
        } else {
            if (position > 0) {
                outRect.top = spacing;
            }
        }
    }
}

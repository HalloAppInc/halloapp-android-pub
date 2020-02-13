package com.halloapp.widget;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

    private GridLayoutManager layoutManager;
    private int spacing;

    public GridSpacingItemDecoration(GridLayoutManager layoutManager, int spacing) {
        this.layoutManager = layoutManager;
        this.spacing = spacing;
    }

    @Override
    public void getItemOffsets(Rect outRect, @NonNull View view, RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        int spanCount = layoutManager.getSpanCount();
        int column = position % spanCount;
        outRect.left = column * spacing / spanCount;
        outRect.right = spacing - (column + 1) * spacing / spanCount;
        if (position >= spanCount) {
            outRect.top = spacing;
        }
    }
}

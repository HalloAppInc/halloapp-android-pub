package com.halloapp.widget;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

    private final GridLayoutManager layoutManager;
    private final int spacing;

    public GridSpacingItemDecoration(@NonNull GridLayoutManager layoutManager, int spacing) {
        this.layoutManager = layoutManager;
        this.spacing = spacing;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, RecyclerView parent, @NonNull RecyclerView.State state) {
        final int position = parent.getChildAdapterPosition(view);
        final int spanCount = layoutManager.getSpanCount();
        final int column = position % spanCount;
        outRect.left = column * spacing / spanCount;
        outRect.right = spacing - (column + 1) * spacing / spanCount;
        if (position >= spanCount) {
            outRect.top = spacing;
        }
    }
}

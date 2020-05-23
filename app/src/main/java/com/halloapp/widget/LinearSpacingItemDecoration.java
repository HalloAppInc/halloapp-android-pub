package com.halloapp.widget;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.util.Rtl;

public class LinearSpacingItemDecoration extends RecyclerView.ItemDecoration {

    private final LinearLayoutManager layoutManager;
    private final int spacing;

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
                final int start = position * spacing / adapter.getItemCount();
                final int end = spacing - (position + 1) * spacing / adapter.getItemCount();
                if (Rtl.isRtl(parent.getContext())) {
                    outRect.right = start;
                    outRect.left = end;
                } else {
                    outRect.left = start;
                    outRect.right = end;
                }
            }
        } else {
            if (position > 0) {
                outRect.top = spacing;
            }
        }
    }
}

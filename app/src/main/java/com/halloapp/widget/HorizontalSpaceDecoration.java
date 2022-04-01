package com.halloapp.widget;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.util.Rtl;

public class HorizontalSpaceDecoration extends RecyclerView.ItemDecoration {

    private final int horizontalSpace;

    public HorizontalSpaceDecoration(int space) {
        this.horizontalSpace = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        if (Rtl.isRtl(parent.getContext())) {
            outRect.left = horizontalSpace;
        } else {
            outRect.right = horizontalSpace;
        }
    }
}

package com.halloapp.widget;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class VerticalSpaceDecoration extends RecyclerView.ItemDecoration {

    private final int verticalSpace;

    public VerticalSpaceDecoration(int space) {
        this.verticalSpace = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        outRect.bottom = verticalSpace;
    }
}

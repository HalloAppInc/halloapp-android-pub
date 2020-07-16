package com.halloapp.widget;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.R;
import com.halloapp.ui.mediapicker.MediaPickerActivity;

public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
    private final int spacing;

    public GridSpacingItemDecoration(int spacing) {
        this.spacing = spacing;
    }

    private boolean isItemView(View view) {
        return view.findViewById(R.id.thumbnail) != null;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, RecyclerView parent, @NonNull RecyclerView.State state) {
        if (isItemView(view)) {
            outRect.top = spacing;
            outRect.left = spacing / 2;
            outRect.right = spacing / 2;
        }
    }
}

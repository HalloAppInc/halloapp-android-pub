package com.halloapp.widget;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.ui.mediapicker.MediaPickerActivity;

public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
    private final int spacing;

    public GridSpacingItemDecoration(int spacing) {
        this.spacing = spacing;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, RecyclerView parent, @NonNull RecyclerView.State state) {
        final int position = parent.getChildAdapterPosition(view);

        if (parent.getAdapter() != null && parent.getAdapter().getItemViewType(position) == MediaPickerActivity.MediaItemsAdapter.TYPE_ITEM) {
            outRect.top = spacing;
            outRect.left = spacing / 2;
            outRect.right = spacing / 2;
        }
    }
}

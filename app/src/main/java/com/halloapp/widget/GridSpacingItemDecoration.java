package com.halloapp.widget;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.R;
import com.halloapp.ui.mediapicker.MediaPickerActivity;

public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
    private final MediaPickerActivity.MediaItemsAdapter adapter;
    private final int spacing;

    public GridSpacingItemDecoration(MediaPickerActivity.MediaItemsAdapter adapter, int spacing) {
        this.adapter = adapter;
        this.spacing = spacing;
    }

    private boolean isItemView(@NonNull View view) {
        return view.findViewById(R.id.thumbnail) != null;
    }

    private int getPositionFromHeader(@NonNull View view, RecyclerView parent) {
        final int position = parent.getChildAdapterPosition(view);

        if (adapter != null) {
            for (int i = position - 1; i >= 0; --i) {
                if (adapter.getItemViewType(i) == MediaPickerActivity.MediaItemsAdapter.TYPE_HEADER) {
                    return position - i - 1;
                }
            }
        }

        return -1;
    }

    private int columnFromPosition(int positionFromHeader) {
        switch (adapter.getGridLayout()) {
            case MediaPickerActivity.MediaItemsAdapter.LAYOUT_DAY_LARGE:
                int positionInBlock = positionFromHeader % MediaPickerActivity.MediaItemsAdapter.BLOCK_SIZE_DAY_LARGE;
                if (positionInBlock < MediaPickerActivity.MediaItemsAdapter.BLOCK_DAY_LARGE_SIZE_ROW_1) {
                    return positionInBlock % MediaPickerActivity.MediaItemsAdapter.BLOCK_DAY_LARGE_SIZE_ROW_1;
                } else {
                    return (positionInBlock - MediaPickerActivity.MediaItemsAdapter.BLOCK_DAY_LARGE_SIZE_ROW_1) % MediaPickerActivity.MediaItemsAdapter.BLOCK_DAY_LARGE_SIZE_ROW_2;
                }
            case MediaPickerActivity.MediaItemsAdapter.LAYOUT_DAY_SMALL:
                return positionFromHeader % MediaPickerActivity.MediaItemsAdapter.SPAN_COUNT_DAY_SMALL;
            case MediaPickerActivity.MediaItemsAdapter.LAYOUT_MONTH:
                return positionFromHeader % MediaPickerActivity.MediaItemsAdapter.SPAN_COUNT_MONTH;
        }

        return -1;
    }

    private int columnsInRow(int positionFromHeader) {
        switch (adapter.getGridLayout()) {
            case MediaPickerActivity.MediaItemsAdapter.LAYOUT_DAY_LARGE:
                int positionInBlock = positionFromHeader % MediaPickerActivity.MediaItemsAdapter.BLOCK_SIZE_DAY_LARGE;
                if (positionInBlock < MediaPickerActivity.MediaItemsAdapter.BLOCK_DAY_LARGE_SIZE_ROW_1) {
                    return MediaPickerActivity.MediaItemsAdapter.BLOCK_DAY_LARGE_SIZE_ROW_1;
                } else {
                    return MediaPickerActivity.MediaItemsAdapter.BLOCK_DAY_LARGE_SIZE_ROW_2;
                }
            case MediaPickerActivity.MediaItemsAdapter.LAYOUT_DAY_SMALL:
                return MediaPickerActivity.MediaItemsAdapter.SPAN_COUNT_DAY_SMALL;
            case MediaPickerActivity.MediaItemsAdapter.LAYOUT_MONTH:
                return MediaPickerActivity.MediaItemsAdapter.SPAN_COUNT_MONTH;
        }

        return -1;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, RecyclerView parent, @NonNull RecyclerView.State state) {
        if (isItemView(view)) {
            int positionFromHeader = getPositionFromHeader(view, parent);
            int column = columnFromPosition(positionFromHeader);
            int columnsCount = columnsInRow(positionFromHeader);

            outRect.bottom = spacing;
            outRect.left = column * spacing / columnsCount;
            outRect.right = spacing - (column + 1) * spacing / columnsCount;
        }
    }
}

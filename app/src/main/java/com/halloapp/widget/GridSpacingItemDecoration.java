package com.halloapp.widget;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.ui.mediapicker.MediaPickerViewModel;
import com.halloapp.util.Preconditions;

public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
    private final MediaPickerViewModel viewModel;
    private final int spacing;

    public GridSpacingItemDecoration(MediaPickerViewModel viewModel, int spacing) {
        this.viewModel = viewModel;
        this.spacing = spacing;
    }

    private int columnFromPosition(int position) {
        switch (Preconditions.checkNotNull(viewModel.getLayout().getValue())) {
            case MediaPickerViewModel.LAYOUT_DAY_LARGE:
                int positionInBlock = position % MediaPickerViewModel.BLOCK_SIZE_DAY_LARGE;

                if (positionInBlock < MediaPickerViewModel.BLOCK_DAY_LARGE_SIZE_ROW_1) {
                    return positionInBlock % MediaPickerViewModel.BLOCK_DAY_LARGE_SIZE_ROW_1;
                } else {
                    return (positionInBlock - MediaPickerViewModel.BLOCK_DAY_LARGE_SIZE_ROW_1) % MediaPickerViewModel.BLOCK_DAY_LARGE_SIZE_ROW_2;
                }
            case MediaPickerViewModel.LAYOUT_DAY_SMALL:
                return position % MediaPickerViewModel.SPAN_COUNT_DAY_SMALL;
            case MediaPickerViewModel.LAYOUT_MONTH:
                return position % MediaPickerViewModel.SPAN_COUNT_MONTH;
        }

        return -1;
    }

    private int columnsInRow(int position) {
        switch (Preconditions.checkNotNull(viewModel.getLayout().getValue())) {
            case MediaPickerViewModel.LAYOUT_DAY_LARGE:
                int positionInBlock = position % MediaPickerViewModel.BLOCK_SIZE_DAY_LARGE;

                if (positionInBlock < MediaPickerViewModel.BLOCK_DAY_LARGE_SIZE_ROW_1) {
                    return MediaPickerViewModel.BLOCK_DAY_LARGE_SIZE_ROW_1;
                } else {
                    return MediaPickerViewModel.BLOCK_DAY_LARGE_SIZE_ROW_2;
                }
            case MediaPickerViewModel.LAYOUT_DAY_SMALL:
                return MediaPickerViewModel.SPAN_COUNT_DAY_SMALL;
            case MediaPickerViewModel.LAYOUT_MONTH:
                return MediaPickerViewModel.SPAN_COUNT_MONTH;
        }

        return -1;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        int column = columnFromPosition(position);
        int columnsCount = columnsInRow(position);

        outRect.bottom = spacing;
        outRect.left = column * spacing / columnsCount;
        outRect.right = spacing - (column + 1) * spacing / columnsCount;
    }
}

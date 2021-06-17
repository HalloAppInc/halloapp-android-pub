package com.halloapp.ui.mediapicker;

import androidx.recyclerview.widget.GridLayoutManager;

import com.halloapp.util.Preconditions;

public class GallerySpanSizeLookup extends GridLayoutManager.SpanSizeLookup {
    private final MediaPickerViewModel viewModel;

    public GallerySpanSizeLookup(MediaPickerViewModel viewModel) {
        this.viewModel = viewModel;
    }

    private int getSpanSizeInDayLargeBlock(int position) {
        final int positionInBlock = position % MediaPickerViewModel.BLOCK_SIZE_DAY_LARGE;

        if (positionInBlock < MediaPickerViewModel.BLOCK_DAY_LARGE_SIZE_ROW_1) {
            return MediaPickerViewModel.SPAN_COUNT_DAY_LARGE / MediaPickerViewModel.BLOCK_DAY_LARGE_SIZE_ROW_1;
        } else {
            return MediaPickerViewModel.SPAN_COUNT_DAY_LARGE / MediaPickerViewModel.BLOCK_DAY_LARGE_SIZE_ROW_2;
        }
    }

    @Override
    public int getSpanSize(int position) {
        if (Preconditions.checkNotNull(viewModel.getLayout().getValue()) == MediaPickerViewModel.LAYOUT_DAY_LARGE) {
            return getSpanSizeInDayLargeBlock(position);
        }

        return 1;
    }
}

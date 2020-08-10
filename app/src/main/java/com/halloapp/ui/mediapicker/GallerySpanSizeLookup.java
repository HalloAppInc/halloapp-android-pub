package com.halloapp.ui.mediapicker;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class GallerySpanSizeLookup extends GridLayoutManager.SpanSizeLookup {
    private RecyclerView view;

    public GallerySpanSizeLookup(RecyclerView view) {
        this.view = view;
    }

    private MediaPickerActivity.MediaItemsAdapter getAdapter() {
        return (MediaPickerActivity.MediaItemsAdapter) view.getAdapter();
    }

    private GridLayoutManager getLayoutManager() {
        return (GridLayoutManager) view.getLayoutManager();
    }

    private int getPositionFromHeader(int position) {
        final MediaPickerActivity.MediaItemsAdapter adapter = getAdapter();

        for (int i = position; i >= 0; --i) {
            if (adapter.getItemViewType(i) == MediaPickerActivity.MediaItemsAdapter.TYPE_HEADER) {
                return position - i - 1;
            }
        }

        return position;
    }

    private int getSpanSizeInDayLargeBlock(int position) {
        final int positionInBlock = getPositionFromHeader(position) % MediaPickerActivity.MediaItemsAdapter.BLOCK_SIZE_DAY_LARGE;

        if (positionInBlock < MediaPickerActivity.MediaItemsAdapter.BLOCK_DAY_LARGE_SIZE_ROW_1) {
            return MediaPickerActivity.MediaItemsAdapter.SPAN_COUNT_DAY_LARGE / MediaPickerActivity.MediaItemsAdapter.BLOCK_DAY_LARGE_SIZE_ROW_1;
        } else {
            return MediaPickerActivity.MediaItemsAdapter.SPAN_COUNT_DAY_LARGE / MediaPickerActivity.MediaItemsAdapter.BLOCK_DAY_LARGE_SIZE_ROW_2;
        }
    }

    @Override
    public int getSpanSize(int position) {
        final MediaPickerActivity.MediaItemsAdapter adapter = getAdapter();
        final int count = getLayoutManager().getSpanCount();

        if (adapter.getItemViewType(position) == MediaPickerActivity.MediaItemsAdapter.TYPE_HEADER) {
            return count;
        }

        if (adapter.getGridLayout() == MediaPickerActivity.MediaItemsAdapter.LAYOUT_DAY_LARGE) {
            return getSpanSizeInDayLargeBlock(position);
        }

        return 1;
    }
}

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

    @Override
    public int getSpanSize(int position) {
        final MediaPickerActivity.MediaItemsAdapter adapter = getAdapter();
        final int count = getLayoutManager().getSpanCount();

        if (adapter.getItemViewType(position) == MediaPickerActivity.MediaItemsAdapter.TYPE_HEADER) {
            return count;
        }

        if (adapter.getGridLayout() == MediaPickerActivity.MediaItemsAdapter.LAYOUT_DAY_LARGE) {
            int relative = 0;
            for (int i = position; i >= 0; --i) {
                if (adapter.getItemViewType(i) == MediaPickerActivity.MediaItemsAdapter.TYPE_HEADER) {
                    relative = (position - i - 1) % 5;
                    break;
                }
            }

            if (relative < 2) {
                return 3;
            } else {
                return 2;
            }
        }

        return 1;
    }
}

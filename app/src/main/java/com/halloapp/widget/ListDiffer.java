package com.halloapp.widget;

import androidx.annotation.Nullable;
import androidx.paging.PagedList;

public interface ListDiffer<T> {
    int getItemCount();
    T getItem(int index);
    void submitList(@Nullable PagedList<T> pagedList, @Nullable final Runnable completion);
}

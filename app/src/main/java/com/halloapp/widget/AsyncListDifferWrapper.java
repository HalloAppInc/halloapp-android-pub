package com.halloapp.widget;

import androidx.annotation.Nullable;
import androidx.paging.AsyncPagedListDiffer;
import androidx.paging.PagedList;

public class AsyncListDifferWrapper<T> implements ListDiffer<T> {

    private final AsyncPagedListDiffer<T> differ;

    @Override
    public int getItemCount() {
        return differ.getItemCount();
    }

    @Override
    public T getItem(int index) {
        return differ.getItem(index);
    }

    @Override
    public void submitList(@Nullable PagedList<T> pagedList, @Nullable Runnable completion) {
        differ.submitList(pagedList, completion);
    }

    public AsyncListDifferWrapper(AsyncPagedListDiffer<T> differ) {
        this.differ = differ;
    }
}

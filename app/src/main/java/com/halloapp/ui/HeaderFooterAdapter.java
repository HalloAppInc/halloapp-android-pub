package com.halloapp.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.AsyncPagedListDiffer;
import androidx.paging.PagedList;

import com.halloapp.util.Preconditions;
import com.halloapp.widget.AsyncListDifferWrapper;
import com.halloapp.widget.ListDiffer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public abstract class HeaderFooterAdapter<Item> extends AdapterWithLifecycle<ViewHolderWithLifecycle> {

    private final HeaderFooterAdapterParent parent;
    private final List<View> headers = new ArrayList<>();
    private final List<View> footers = new ArrayList<>();

    // Exactly one of these two is used
    private List<Item> items = new ArrayList<>();
    private ListDiffer<Item> differ;

    public HeaderFooterAdapter(@NonNull HeaderFooterAdapterParent parent) {
        this.parent = parent;
        this.differ = null;
    }

    public void setDiffer(@NonNull AsyncPagedListDiffer<Item> differ) {
        this.differ = new AsyncListDifferWrapper<>(differ);
    }

    public void setDiffer(@NonNull ListDiffer<Item> differ) {
        this.differ = differ;
    }

    public View addHeader(@LayoutRes int layout) {
        View header = LayoutInflater.from(parent.getContext()).inflate(layout, parent.getParentViewGroup(), false);
        headers.add(header);
        return header;
    }

    public View addFooter(@LayoutRes int layout) {
        View footer = LayoutInflater.from(parent.getContext()).inflate(layout, parent.getParentViewGroup(), false);
        footers.add(footer);
        return footer;
    }

    protected int getHeaderCount() {
        return headers.size();
    }

    protected int getFooterCount() {
        return footers.size();
    }

    protected int getInternalItemCount() {
        return differ != null ? differ.getItemCount() : items.size();
    }

    private boolean isHeader(int position) {
        return position < getHeaderCount();
    }

    private boolean isFooter(int position) {
        return position >= getHeaderCount() + getInternalItemCount();
    }

    private boolean isMeta(int position) {
        return isHeader(position) || isFooter(position);
    }

    public void submitItems(@NonNull List<Item> items) {
        Preconditions.checkState(differ == null, "submitItems cannot be used with a non-null differ");
        this.items = items;
        notifyDataSetChanged();
    }

    public void submitList(@Nullable PagedList<Item> pagedList, @Nullable final Runnable completion) {
        differ.submitList(pagedList, completion);
    }

    @Nullable
    protected Item getItem(int position) {
        return isMeta(position) ? null : differ != null ? differ.getItem(position - getHeaderCount()) : items.get(position - getHeaderCount());
    }

    @Override
    public long getItemId(int position) {
        if (isMeta(position)) {
            return -position;
        }
        long id = getIdForItem(Preconditions.checkNotNull(getItem(position)));
        Preconditions.checkState(id >= 0, "only headers and footers can have negative ids");
        return id;
    }

    public abstract long getIdForItem(Item item);

    @Override
    public int getItemCount() {
        return getHeaderCount() + getInternalItemCount() + getFooterCount();
    }

    @Override
    public int getItemViewType(int position) {
        if (isMeta(position)) {
            return -position - 1;
        } else {
            return getViewTypeForItem(Preconditions.checkNotNull(getItem(position)));
        }
    }

    public abstract int getViewTypeForItem(Item item);

    @NonNull
    @Override
    public ViewHolderWithLifecycle onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType < 0) {
            int metaPosition = -viewType - 1;
            View view = isHeader(metaPosition) ? headers.get(metaPosition) : footers.get(metaPosition - getHeaderCount() - getInternalItemCount());
            if (view.getParent() != null) {
                ((ViewGroup)view.getParent()).removeView(view);
            }
            return new MetaViewHolder(view);
        }
        return createViewHolderForViewType(parent, viewType);
    }

    @NonNull
    public abstract ViewHolderWithLifecycle createViewHolderForViewType(@NonNull ViewGroup parent, int viewType);

    private static class MetaViewHolder extends ViewHolderWithLifecycle {
        MetaViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public interface HeaderFooterAdapterParent {
        @NonNull Context getContext();
        @NonNull ViewGroup getParentViewGroup();
    }
}

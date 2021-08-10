package com.halloapp.ui;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public abstract class AdapterWithLifecycle<T extends ViewHolderWithLifecycle> extends RecyclerView.Adapter<T> {

    @Override
    public void onViewRecycled(@NonNull T holder) {
        super.onViewRecycled(holder);
        holder.markRecycled();
    }

    @Override
    public void onViewAttachedToWindow(@NonNull T holder) {
        super.onViewAttachedToWindow(holder);
        holder.markAttach();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull T holder) {
        super.onViewDetachedFromWindow(holder);
        holder.markDetach();
    }
}

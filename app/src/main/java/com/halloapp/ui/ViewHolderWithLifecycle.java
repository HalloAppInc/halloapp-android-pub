package com.halloapp.ui;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.recyclerview.widget.RecyclerView;

public abstract class ViewHolderWithLifecycle extends RecyclerView.ViewHolder implements LifecycleOwner {

    private final LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);

    public ViewHolderWithLifecycle(@NonNull View itemView) {
        super(itemView);
        lifecycleRegistry.markState(Lifecycle.State.INITIALIZED);
    }

    public void markAttach() {
        lifecycleRegistry.markState(Lifecycle.State.STARTED);
    }

    public void markDetach() {
        lifecycleRegistry.markState(Lifecycle.State.CREATED);
    }

    public void markRecycled() {
        lifecycleRegistry.markState(Lifecycle.State.DESTROYED);
    }

    @Override
    @NonNull
    public Lifecycle getLifecycle() {
        return lifecycleRegistry;
    }
}

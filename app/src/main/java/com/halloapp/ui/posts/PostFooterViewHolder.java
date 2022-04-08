package com.halloapp.ui.posts;

import android.view.View;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import com.halloapp.R;
import com.halloapp.content.Post;
import com.halloapp.ui.ContentViewHolderParent;

public abstract class PostFooterViewHolder {
    protected static final float DISABLED_OPACITY = 0.35f;

    protected Post post;

    protected PostViewHolder.PostViewHolderParent parent;

    protected final View footerSpacing;

    protected View itemView;

    public PostFooterViewHolder(@NonNull View itemView, @NonNull PostViewHolder.PostViewHolderParent parent) {
        this.parent = parent;
        this.itemView = itemView;
        footerSpacing = itemView.findViewById(R.id.footer_spacing);
    }

    public abstract void setRegistered(boolean registered);

    @CallSuper
    public void bindTo(@NonNull Post post) {
        this.post = post;
    }
}

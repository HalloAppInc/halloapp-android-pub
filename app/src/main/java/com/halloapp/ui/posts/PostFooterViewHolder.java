package com.halloapp.ui.posts;

import android.view.View;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import com.halloapp.R;
import com.halloapp.content.Post;
import com.halloapp.ui.ContentViewHolderParent;

public abstract class PostFooterViewHolder {
    protected Post post;

    protected ContentViewHolderParent parent;

    protected final View footerSpacing;

    protected View itemView;

    public PostFooterViewHolder(@NonNull View itemView, @NonNull ContentViewHolderParent parent) {
        this.parent = parent;
        this.itemView = itemView;
        footerSpacing = itemView.findViewById(R.id.footer_spacing);
    }

    @CallSuper
    public void bindTo(@NonNull Post post) {
        this.post = post;
    }
}

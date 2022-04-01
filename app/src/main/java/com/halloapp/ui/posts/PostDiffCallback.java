package com.halloapp.ui.posts;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.halloapp.content.Post;

public class PostDiffCallback extends DiffUtil.ItemCallback<Post> {
    @Override
    public boolean areItemsTheSame(Post oldItem, Post newItem) {
        // The ID property identifies when items are the same.
        return oldItem.rowId == newItem.rowId;
    }

    @Override
    public boolean areContentsTheSame(@NonNull Post oldItem, @NonNull Post newItem) {
        return oldItem.equals(newItem);
    }
}

package com.halloapp.ui.posts;

import android.content.res.ColorStateList;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.halloapp.R;
import com.halloapp.content.Post;
import com.halloapp.ui.ViewHolderWithLifecycle;
import com.halloapp.util.TimeFormatter;

public class CollapsedPostViewHolder extends ViewHolderWithLifecycle {

    private final PostViewHolder.PostViewHolderParent parent;

    private final TextView textView;
    private final TextView timeView;

    private int theme;

    public CollapsedPostViewHolder(@NonNull View itemView, @NonNull PostViewHolder.PostViewHolderParent parent) {
        super(itemView);

        this.parent = parent;
        this.textView = itemView.findViewById(R.id.deleted_text);
        this.timeView = itemView.findViewById(R.id.time);
    }

    public void bindTo(PostListDiffer.PostCollection post, boolean firstPost) {
        if (timeView != null) {
            TimeFormatter.setTimePostsFormat(timeView, post.timestamp);
        }
        parent.getTimestampRefresher().scheduleTimestampRefresh(post.timestamp);

        if (firstPost) {
            itemView.setPadding(itemView.getPaddingLeft(), itemView.getPaddingBottom(), itemView.getPaddingRight(), itemView.getPaddingBottom());
        } else {
            itemView.setPadding(itemView.getPaddingLeft(), 0, itemView.getPaddingRight(), itemView.getPaddingBottom());
        }
        parent.getContactLoader().cancel(textView);
        if (post.type == Post.TYPE_USER || post.type == Post.TYPE_RETRACTED) {
            textView.setTextColor(ContextCompat.getColor(textView.getContext(), R.color.secondary_text));
            textView.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(textView.getContext(), theme == 0 ? R.color.deleted_post_bg : R.color.deleted_post_themed_bg)));
            if (post.type == Post.TYPE_RETRACTED) {
                textView.setText(textView.getResources().getQuantityString(R.plurals.collapsed_deleted_posts, post.size, post.size));
            }
        } else {
            textView.setTextColor(ContextCompat.getColor(textView.getContext(), R.color.system_post_text));
            textView.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(textView.getContext(), R.color.system_post_bg)));
            if (post.usage == Post.USAGE_ADD_MEMBERS) {
                textView.setText(textView.getResources().getQuantityString(R.plurals.collapsed_added_user_posts, post.size, post.size));
            } else if (post.usage == Post.USAGE_REMOVE_MEMBER) {
                textView.setText(textView.getResources().getQuantityString(R.plurals.collapsed_removed_user_posts, post.size, post.size));
            } else if (post.usage == Post.USAGE_MEMBER_LEFT) {
                textView.setText(textView.getResources().getQuantityString(R.plurals.collapsed_user_left_posts, post.size, post.size));
            } else if (post.usage == Post.USAGE_MEMBER_JOINED) {
                textView.setText(textView.getResources().getQuantityString(R.plurals.collapsed_user_joined_posts, post.size, post.size));
            }
        }
    }

    public void applyTheme(int theme) {
        this.theme = theme;
    }
}

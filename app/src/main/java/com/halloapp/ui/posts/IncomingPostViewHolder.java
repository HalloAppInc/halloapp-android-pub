package com.halloapp.ui.posts;

import android.content.Intent;
import android.view.View;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import com.halloapp.R;
import com.halloapp.posts.Post;
import com.halloapp.ui.CommentsActivity;

public class IncomingPostViewHolder extends PostViewHolder {

    private final View commentButton;
    private final View messageButton;
    private final View commentsIndicator;

    public IncomingPostViewHolder(@NonNull View itemView, @NonNull PostViewHolderParent parent) {
        super(itemView, parent);

        commentButton = itemView.findViewById(R.id.comment);
        messageButton = itemView.findViewById(R.id.message);
        commentsIndicator = itemView.findViewById(R.id.comments_indicator);

        commentButton.setOnClickListener(view -> {
            final Intent intent = new Intent(itemView.getContext(), CommentsActivity.class);
            intent.putExtra(CommentsActivity.EXTRA_POST_SENDER_USER_ID, post.senderUserId.rawId());
            intent.putExtra(CommentsActivity.EXTRA_POST_ID, post.postId);
            intent.putExtra(CommentsActivity.EXTRA_SHOW_KEYBOARD, post.commentCount == 0);
            parent.startActivity(intent);
        });
        messageButton.setOnClickListener(view -> {
            // TODO (ds): start message activity
        });
    }

    @CallSuper
    public void bindTo(@NonNull Post post) {
        super.bindTo(post);

        if (post.unseenCommentCount > 0) {
            commentsIndicator.setVisibility(View.VISIBLE);
            commentsIndicator.setBackgroundResource(R.drawable.new_comments_indicator);
        } else if (post.commentCount > 0) {
            commentsIndicator.setVisibility(View.VISIBLE);
            commentsIndicator.setBackgroundResource(R.drawable.old_comments_indicator);
        } else {
            commentsIndicator.setVisibility(View.GONE);
        }
    }
}

